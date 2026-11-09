package fi.dy.masa.picktweak.smoke;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.dy.masa.picktweak.config.Configs;
import fi.dy.masa.picktweak.net.ItemStackParser;
import fi.dy.masa.picktweak.net.PbpPayloads;
import fi.dy.masa.picktweak.net.PbpServerConfig;
import fi.dy.masa.picktweak.tool.ToolPicker;
import fi.dy.masa.picktweak.util.NbtFormatter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.Bootstrap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 冒烟测试：不启动游戏，直接跑纯逻辑（配置、附魔冲突、NBT 处理、overrides 解析、天体方向）。
 *
 * <p>运行方式：{@code ./gradlew smoke}</p>
 */
public class SmokeTest {

	static int failures = 0;

	static void check(String name, boolean ok, String detail) {
		System.out.printf("%-46s %s%s%n", name, ok ? "PASS" : "FAIL", detail == null || detail.isEmpty() ? "" : "  -> " + detail);
		if (!ok) {
			failures++;
		}
	}

	public static void main(String[] args) {
		net.minecraft.SharedConstants.setVersion(net.minecraft.DetectedVersion.BUILT_IN);
		Bootstrap.bootStrap();

		// 1. 配置：MaLiLib 选项 + 运行时快照
		Configs.refresh();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(Configs.BLOCK_PICKER_ENABLED.getAsJsonElement());
		check("config option json round-trip", json != null && json.contains("true"), json);
		check("config snapshot", Configs.blockPicker.enabled && Configs.blockPicker.creativeRange == 100.0D
						&& Configs.blockPicker.copyNbt && Configs.blockPicker.showCopiedNbt
						&& Configs.toolPicker.tools.size() == 9 && Configs.toolPicker.weapons.size() == 4
						&& Configs.inventory.lockedSlots.length == 9,
				"creative=" + Configs.blockPicker.creativeRange
						+ " tools=" + Configs.toolPicker.tools.size()
						+ " weapons=" + Configs.toolPicker.weapons.size());

		// 2. 附魔冲突处理
		Map<String, Integer> enchants = new LinkedHashMap<>();
		enchants.put("minecraft:sharpness", 5);
		enchants.put("minecraft:smite", 5);
		enchants.put("minecraft:silk_touch", 5);
		enchants.put("minecraft:fortune", 5);
		enchants.put("minecraft:infinity", 1);
		enchants.put("minecraft:mending", 1);
		Map<String, Integer> resolved = ToolPicker.resolveConflicts(enchants, Configs.toolPicker);
		check("conflict: silk_touch vs fortune (fortune preferred)",
				resolved.containsKey("minecraft:fortune") && !resolved.containsKey("minecraft:silk_touch"),
				resolved.keySet().toString());
		check("conflict: infinity vs mending (infinity first)",
				resolved.containsKey("minecraft:infinity") && !resolved.containsKey("minecraft:mending"),
				resolved.keySet().toString());
		check("conflict: smite vs sharpness (smite first)",
				resolved.containsKey("minecraft:smite") && !resolved.containsKey("minecraft:sharpness"),
				resolved.keySet().toString());
		check("conflict result is deterministic", resolved.equals(
						ToolPicker.resolveConflicts(enchants, Configs.toolPicker)),
				resolved.keySet().toString());

		// 3. NBT 黑名单过滤 + 单行 / 缩进两种 NBT 格式化
		CompoundTag tag = new CompoundTag();
		tag.putString("id", "minecraft:chest");
		tag.putInt("x", 10);
		tag.putInt("y", 64);
		tag.putInt("z", -3);
		ListTag items = new ListTag();
		CompoundTag item = new CompoundTag();
		item.putString("id", "minecraft:diamond");
		item.putInt("Count", 3);
		items.addAndUnwrap(item);
		tag.put("Items", items);

		CompoundTag filtered = NbtFormatter.filter(tag, List.of("id"));
		check("nbt blacklist removes top-level id",
				!filtered.contains("id") && filtered.contains("Items"),
				String.valueOf(filtered.keySet()));
		var nested = (CompoundTag) filtered.getListOrEmpty("Items").get(0);
		check("nbt blacklist keeps nested item id",
				nested.contains("id") && nested.contains("Count"),
				"nested keys = " + nested.keySet());

		CompoundTag deepFiltered = NbtFormatter.filter(tag, List.of("Items.id"));
		check("nbt blacklist matches nested paths",
				deepFiltered.contains("id")
						&& !((CompoundTag) deepFiltered.getListOrEmpty("Items").get(0)).contains("id"),
				"nested keys = " + ((CompoundTag) deepFiltered.getListOrEmpty("Items").get(0)).keySet());

		String compact = NbtFormatter.compact(filtered);
		String pretty = NbtFormatter.pretty(filtered);
		check("nbt compact is single line", !compact.contains("\n"), compact);
		check("nbt pretty is indented", pretty.contains("\n"), pretty.replace("\n", "\\n"));

		// 3b. Ctrl + 中键复制 NBT：实体装备要完整保留
		CompoundTag entity = new CompoundTag();
		entity.putString("id", "minecraft:armor_stand");
		entity.putString("UUID", "0d9d2b0e-0000-4000-8000-000000000000");
		ListTag pos = new ListTag();
		pos.add(net.minecraft.nbt.DoubleTag.valueOf(1.0D));
		entity.put("Pos", pos);
		CompoundTag equipment = new CompoundTag();
		CompoundTag helmet = new CompoundTag();
		helmet.putString("id", "minecraft:diamond_helmet");
		helmet.putInt("count", 1);
		equipment.put("head", helmet);
		entity.put("equipment", equipment);
		entity.remove("id");   // BlockPicker.entityNbt() 就是这么做的

		String entityCopy = NbtFormatter.copyText(entity, Configs.blockPicker.entityNbtBlacklist, false);
		check("entity nbt copy drops volatile keys",
				entityCopy != null && !entityCopy.contains("UUID") && !entityCopy.contains("Pos"),
				entityCopy);
		check("entity nbt copy keeps equipment and its item id",
				entityCopy != null && entityCopy.contains("equipment")
						&& entityCopy.contains("minecraft:diamond_helmet"),
				entityCopy);
		check("entity nbt copy has no top-level id",
				entityCopy != null && !entityCopy.contains("minecraft:armor_stand"),
				entityCopy);
		check("empty nbt copy is null",
				NbtFormatter.copyText(new CompoundTag(), List.of(), false) == null,
				"ok");

		// 4. overrides 物品字符串解析
		var swordItem = BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse("minecraft:diamond_sword"));
		check("override item id lookup", swordItem != null, String.valueOf(swordItem));
		try {
			var stack = ItemStackParser.parse("minecraft:diamond_sword",
					net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
			check("override parse plain item", !stack.isEmpty(),
					stack.isEmpty() ? "empty" : stack.getItem().toString());
			var withData = ItemStackParser.parse(
					"minecraft:chest[custom_data={foo:1}]",
					net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
			check("override parse custom_data", !withData.isEmpty()
							&& withData.get(DataComponents.CUSTOM_DATA) != null,
					withData.isEmpty() ? "empty" : String.valueOf(withData.get(DataComponents.CUSTOM_DATA)));
		} catch (Throwable t) {
			System.out.println("    (skipped ItemStack construction: vanilla data pack is not loaded in a bare JVM)");
		}

		// 5. 工具 / 武器种类
		check("tool kinds", ToolPicker.TOOL_KINDS.size() == 9, ToolPicker.TOOL_KINDS.toString());
		check("weapon kinds", ToolPicker.WEAPON_KINDS.size() == 6, ToolPicker.WEAPON_KINDS.toString());

		// 6. 服务端配置默认值
		PbpServerConfig serverConfig = PbpServerConfig.get();
		check("server config defaults", serverConfig.maxPickDistance == 100.0D
						&& serverConfig.allowIncludeData && serverConfig.allowStateProperties
						&& serverConfig.giveInSurvival && serverConfig.preferEntityHeads
						&& serverConfig.toolItems.size() == 9 && serverConfig.weaponItems.size() == 6,
				"maxDist=" + serverConfig.maxPickDistance
						+ " tools=" + serverConfig.toolItems.size()
						+ " weapons=" + serverConfig.weaponItems.size());

		// 7. 拾取数据包类型
		check("payload types", PbpPayloads.PickBlock.TYPE.id() != null
						&& PbpPayloads.PickEntity.TYPE.id() != null
						&& PbpPayloads.GiveTool.TYPE.id() != null
						&& PbpPayloads.PickCelestial.TYPE.id() != null
						&& PbpPayloads.Reload.TYPE.id() != null,
				"ok");

		// 8. 太阳 / 月亮方向公式（与原版天空渲染一致）
		var up = fi.dy.masa.picktweak.pick.ClientRaycast.celestialDirection(0.0D);
		var side = fi.dy.masa.picktweak.pick.ClientRaycast.celestialDirection(90.0D);
		check("celestial direction: 0deg is straight up",
				Math.abs(up.x) < 1.0E-6D && Math.abs(up.y - 1.0D) < 1.0E-6D && Math.abs(up.z) < 1.0E-6D,
				up.toString());
		check("celestial direction: 90deg is horizon",
				Math.abs(side.x + 1.0D) < 1.0E-6D && Math.abs(side.y) < 1.0E-6D,
				side.toString());
		check("celestial direction is normalized",
				Math.abs(up.length() - 1.0D) < 1.0E-6D, String.valueOf(up.length()));

		System.out.println();
		System.out.println(failures == 0 ? "ALL SMOKE TESTS PASSED" : (failures + " SMOKE TEST(S) FAILED"));
		if (failures > 0) {
			System.exit(1);
		}
	}
}
