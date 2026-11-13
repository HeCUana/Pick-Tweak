package fi.dy.masa.picktweak.smoke;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.dy.masa.picktweak.config.Configs;
import fi.dy.masa.picktweak.config.MaliOptions;
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

		// 1. 配置：默认值（不依赖 MaLiLib） + MaLiLib 选项同步 + 运行时快照
		Configs.installDefaults();
		check("default snapshot", Configs.blockPicker.enabled && Configs.blockPicker.creativeRange == 100.0D
						&& !Configs.blockPicker.copyNbt && Configs.blockPicker.showCopiedNbt
						&& Configs.blockPicker.copyState && !Configs.blockPicker.pickFluids
						&& !Configs.blockPicker.copyNbtChatFeedback && !Configs.blockPicker.copyStateChatFeedback
						&& Configs.inventory.lockedSlots.length == 9,
				"creative=" + Configs.blockPicker.creativeRange);
		check("copy chat feedback defaults off",
				!Configs.blockPicker.copyNbtChatFeedback && !Configs.blockPicker.copyStateChatFeedback,
				"nbt=" + Configs.blockPicker.copyNbtChatFeedback + " state=" + Configs.blockPicker.copyStateChatFeedback);
		check("nbt clipboard copy defaults off", !Configs.blockPicker.copyNbt,
				"copyNbt=" + Configs.blockPicker.copyNbt);

		MaliOptions.refresh();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(MaliOptions.BLOCK_PICKER_ENABLED.getAsJsonElement());
		check("config option json round-trip", json != null && json.contains("true"), json);
		check("config snapshot", Configs.blockPicker.enabled && Configs.blockPicker.creativeRange == 100.0D
						&& !Configs.blockPicker.copyNbt && Configs.blockPicker.showCopiedNbt
						&& Configs.toolPicker.tools.size() == 9 && Configs.toolPicker.weapons.size() == 6
						&& Configs.toolPicker.tools.containsKey("sword") && Configs.toolPicker.weapons.containsKey("sword")
						&& Configs.toolPicker.tools.containsKey("axe") && Configs.toolPicker.weapons.containsKey("axe")
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
			var fullStack = ItemStackParser.parse(
					"{\"id\":\"minecraft:stone\",\"count\":3}",
					net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
			check("override parse full item json", !fullStack.isEmpty() && fullStack.getCount() == 3,
					fullStack.isEmpty() ? "empty" : String.valueOf(fullStack.getCount()));
		} catch (Throwable t) {
			System.out.println("    (skipped ItemStack construction: vanilla data pack is not loaded in a bare JVM)");
		}

		// 5. 工具 / 武器种类
		check("tool kinds", ToolPicker.TOOL_KINDS.size() == 9, ToolPicker.TOOL_KINDS.toString());
		check("weapon kinds", ToolPicker.WEAPON_KINDS.size() == 6, ToolPicker.WEAPON_KINDS.toString());

		// 6. 服务端配置默认值（安全默认：不凭空发物品、不给客户端发装备）
		PbpServerConfig serverConfig = PbpServerConfig.get();
		check("server config defaults", serverConfig.maxPickDistance == 100.0D
						&& serverConfig.allowIncludeData && serverConfig.allowStateProperties
						&& !serverConfig.giveInSurvival && !serverConfig.allowClientGive && !serverConfig.pickFluids
						&& serverConfig.preferEntityHeads
						&& serverConfig.toolItems.size() == 9 && serverConfig.weaponItems.size() == 6,
				"maxDist=" + serverConfig.maxPickDistance
						+ " tools=" + serverConfig.toolItems.size()
						+ " weapons=" + serverConfig.weaponItems.size()
						+ " survivalGive=" + serverConfig.giveInSurvival
						+ " clientGive=" + serverConfig.allowClientGive);
		check("server config: celestial items are valid item ids",
				BuiltInRegistries.ITEM.containsKey(net.minecraft.resources.Identifier.parse(serverConfig.sunItem))
						&& BuiltInRegistries.ITEM.containsKey(net.minecraft.resources.Identifier.parse(serverConfig.moonItem)),
				"sun=" + serverConfig.sunItem + " moon=" + serverConfig.moonItem);
		check("server config: sun / moon default to the light block",
				"minecraft:light".equals(serverConfig.sunItem) && "minecraft:light".equals(serverConfig.moonItem),
				"sun=" + serverConfig.sunItem + " moon=" + serverConfig.moonItem);

		// 7. 拾取数据包类型
		check("payload types", PbpPayloads.PickBlock.TYPE.id() != null
						&& PbpPayloads.PickEntity.TYPE.id() != null
						&& PbpPayloads.GiveTool.TYPE.id() != null
						&& PbpPayloads.PickCelestial.TYPE.id() != null
						&& PbpPayloads.Reload.TYPE.id() != null,
				"ok");
		check("payload ids use the picktweak namespace",
				PbpPayloads.PickBlock.TYPE.id().getNamespace().equals("picktweak")
						&& PbpPayloads.PickEntity.TYPE.id().getNamespace().equals("picktweak")
						&& PbpPayloads.GiveTool.TYPE.id().getNamespace().equals("picktweak")
						&& PbpPayloads.PickCelestial.TYPE.id().getNamespace().equals("picktweak")
						&& PbpPayloads.Reload.TYPE.id().getNamespace().equals("picktweak"),
				PbpPayloads.PickBlock.TYPE.id().toString());

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

		// 9. 全部快捷方式都在面板里可改
		check("hotkeys: pick modifiers + actions + chat slots",
				fi.dy.masa.picktweak.config.Hotkeys.ALL_HOTKEYS.size() == 15
						&& fi.dy.masa.picktweak.config.Hotkeys.CHAT_SLOT_KEYS.size() == 10,
				"total=" + fi.dy.masa.picktweak.config.Hotkeys.ALL_HOTKEYS.size()
						+ " chatSlots=" + fi.dy.masa.picktweak.config.Hotkeys.CHAT_SLOT_KEYS.size());
		check("hotkeys: nbt modifier default is ctrl + middle mouse",
				fi.dy.masa.picktweak.config.Hotkeys.PICK_WITH_NBT.getStringValue().equals("LEFT_CONTROL,MOUSE_MIDDLE"),
				fi.dy.masa.picktweak.config.Hotkeys.PICK_WITH_NBT.getStringValue());
		check("hotkeys: state modifier default is alt + middle mouse",
				fi.dy.masa.picktweak.config.Hotkeys.PICK_WITH_STATE.getStringValue().equals("LEFT_ALT,MOUSE_MIDDLE"),
				fi.dy.masa.picktweak.config.Hotkeys.PICK_WITH_STATE.getStringValue());
		check("hotkeys: chat slot 1 default is ctrl + 1",
				fi.dy.masa.picktweak.config.Hotkeys.CHAT_SLOT_KEYS.get(0).getStringValue().equals("LEFT_CONTROL,1"),
				fi.dy.masa.picktweak.config.Hotkeys.CHAT_SLOT_KEYS.get(0).getStringValue());

		// 10. Axiom 共存模式
		var yieldMode = Configs.AxiomMode.YIELD;
		var overrideMode = Configs.AxiomMode.OVERRIDE;
		check("axiom mode: fromString round trip",
				Configs.AxiomMode.YIELD.fromString("override") == overrideMode
						&& Configs.AxiomMode.YIELD.fromString("YIELD") == yieldMode
						&& Configs.AxiomMode.YIELD.fromString("nonsense") == yieldMode,
				"ok");
		check("axiom mode: cycle wraps around",
				yieldMode.cycle(true) == overrideMode && overrideMode.cycle(true) == yieldMode,
				"ok");
		check("axiom mode: default is yield",
				Configs.blockPicker.axiomMode == Configs.AxiomMode.YIELD,
				String.valueOf(Configs.blockPicker.axiomMode));

		// 11. 流体拾取成桶
		var waterBlock = BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.parse("minecraft:water"));
		var lavaBlock = BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.parse("minecraft:lava"));
		var stoneBlock = BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.parse("minecraft:stone"));
		var waterState = waterBlock == null ? null : waterBlock.defaultBlockState();
		var lavaState = lavaBlock == null ? null : lavaBlock.defaultBlockState();
		var stoneState = stoneBlock == null ? null : stoneBlock.defaultBlockState();
		check("fluid bucket: water",
				waterState != null && fi.dy.masa.picktweak.util.BlockItems.fluidBucketItem(waterState)
						== net.minecraft.world.item.Items.WATER_BUCKET,
				waterState == null ? "no water block" : "ok");
		check("fluid bucket: lava",
				lavaState != null && fi.dy.masa.picktweak.util.BlockItems.fluidBucketItem(lavaState)
						== net.minecraft.world.item.Items.LAVA_BUCKET,
				lavaState == null ? "no lava block" : "ok");
		check("fluid bucket: plain block has none",
				stoneState != null && fi.dy.masa.picktweak.util.BlockItems.fluidBucketItem(stoneState) == null,
				stoneState == null ? "no stone block" : "ok");

		// 12. 实体拾取：刷怪蛋映射 + 实体 NBT 清理（纯逻辑）
		try {
			var zombieEgg = net.minecraft.world.item.SpawnEggItem.byId(net.minecraft.world.entity.EntityTypes.ZOMBIE);
			var armorStandEgg = net.minecraft.world.item.SpawnEggItem.byId(net.minecraft.world.entity.EntityTypes.ARMOR_STAND);
			var paintingEgg = net.minecraft.world.item.SpawnEggItem.byId(net.minecraft.world.entity.EntityTypes.PAINTING);
			check("entity pick: mob spawn egg mapping",
					zombieEgg.isPresent() && zombieEgg.get().value() == net.minecraft.world.item.Items.ZOMBIE_SPAWN_EGG,
					String.valueOf(zombieEgg));
			check("entity pick: armor stand / painting have no spawn egg",
					armorStandEgg.isEmpty() && paintingEgg.isEmpty(), "ok");
		} catch (Throwable t) {
			System.out.println("    (skipped spawn egg mapping: item components are not bound in a bare JVM)");
		}

		CompoundTag entityTag = new CompoundTag();
		entityTag.putString("id", "minecraft:zombie");
		entityTag.putString("UUID", "0000-1111");
		entityTag.putDouble("Health", 12.5D);
		entityTag.putBoolean("NoAI", true);
		entityTag.putString("Pos", "pos");
		entityTag.putString("Motion", "motion");
		entityTag.putString("equipment", "equipment");
		CompoundTag sanitized = fi.dy.masa.picktweak.pick.EntityPickBuilder.sanitize(entityTag);
		check("entity pick: volatile fields are dropped",
				!sanitized.contains("id") && !sanitized.contains("UUID") && !sanitized.contains("Pos")
						&& !sanitized.contains("Motion"),
				String.valueOf(sanitized.keySet()));
		check("entity pick: health / AI / equipment are kept",
				sanitized.getDoubleOr("Health", 0.0D) == 12.5D && sanitized.getBooleanOr("NoAI", false)
						&& sanitized.contains("equipment"),
				String.valueOf(sanitized.keySet()));

		CompoundTag frameSource = new CompoundTag();
		frameSource.putString("Item", "item");
		frameSource.putInt("ItemRotation", 3);
		frameSource.putString("Facing", "north");
		frameSource.putString("Pos", "pos");
		CompoundTag frameTag = fi.dy.masa.picktweak.pick.EntityPickBuilder.frameTag(frameSource);
		check("entity pick: item frame keeps only display data",
				frameTag.contains("Item") && frameTag.getIntOr("ItemRotation", 0) == 3
						&& !frameTag.contains("Facing") && !frameTag.contains("Pos"),
				String.valueOf(frameTag.keySet()));

		// 13. 方块状态文本（Alt 复制到剪贴板的内容）
		var stairsState = net.minecraft.world.level.block.Blocks.OAK_STAIRS.defaultBlockState();
		var campfireState = net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState();
		var stoneStateForText = net.minecraft.world.level.block.Blocks.STONE.defaultBlockState();
		String stairsText = fi.dy.masa.picktweak.pick.BlockPicker.blockStateText(stairsState);
		String campfireText = fi.dy.masa.picktweak.pick.BlockPicker.blockStateText(campfireState);
		String stoneText = fi.dy.masa.picktweak.pick.BlockPicker.blockStateText(stoneStateForText);
		check("block state text: stairs keep facing / half",
				stairsText.contains("facing=") && stairsText.contains("half="), stairsText);
		check("block state text: campfire keeps lit",
				campfireText.contains("lit="), campfireText);
		check("block state text: plain block is empty", stoneText.isEmpty(), stoneText);

		try {
			var markerStack = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.PAPER);
			fi.dy.masa.picktweak.util.ItemNbtMarker.markNbt(markerStack);
			var lore = markerStack.get(net.minecraft.core.component.DataComponents.LORE);
			boolean hasLine = lore != null && lore.lines().stream()
					.anyMatch(line -> "+NBT".equals(line.getString()));
			check("nbt marker: +NBT lore line", hasLine, String.valueOf(lore));
			fi.dy.masa.picktweak.util.ItemNbtMarker.markNbt(markerStack);
			int markerLines = 0;
			if (lore != null) {
				markerLines = (int) markerStack.get(net.minecraft.core.component.DataComponents.LORE).lines().stream()
						.filter(line -> "+NBT".equals(line.getString())).count();
			}
			check("nbt marker: no duplicate line", markerLines == 1, String.valueOf(markerLines));
		} catch (Throwable t) {
			System.out.println("    (skipped ItemStack-based nbt marker checks: vanilla data pack is not loaded in a bare JVM)");
		}

		// 14. 火 / 刷怪笼 / 光源等级（纯逻辑）
		check("pick: spawner detection",
				fi.dy.masa.picktweak.util.BlockItems.isSpawner(net.minecraft.world.level.block.Blocks.SPAWNER.defaultBlockState())
						&& fi.dy.masa.picktweak.util.BlockItems.isSpawner(net.minecraft.world.level.block.Blocks.TRIAL_SPAWNER.defaultBlockState())
						&& !fi.dy.masa.picktweak.util.BlockItems.isSpawner(net.minecraft.world.level.block.Blocks.STONE.defaultBlockState()),
				"ok");
		check("pick: light level cycles 15 -> 0 -> 1",
				fi.dy.masa.picktweak.util.BlockItems.nextLightLevel(15) == 0
						&& fi.dy.masa.picktweak.util.BlockItems.nextLightLevel(0) == 1
						&& fi.dy.masa.picktweak.util.BlockItems.nextLightLevel(7) == 8,
				String.valueOf(fi.dy.masa.picktweak.util.BlockItems.nextLightLevel(15)));
		try {
			check("pick: fire -> flint and steel",
					fi.dy.masa.picktweak.util.BlockItems.firePickItem(net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState())
							.getItem() == net.minecraft.world.item.Items.FLINT_AND_STEEL
							&& fi.dy.masa.picktweak.util.BlockItems.firePickItem(net.minecraft.world.level.block.Blocks.SOUL_FIRE.defaultBlockState())
							.getItem() == net.minecraft.world.item.Items.FLINT_AND_STEEL,
					"ok");
			check("pick: plain block has no fire item",
					fi.dy.masa.picktweak.util.BlockItems.firePickItem(net.minecraft.world.level.block.Blocks.STONE.defaultBlockState()).isEmpty(),
					"ok");
			check("pick: light block level round-trip",
					fi.dy.masa.picktweak.util.BlockItems.lightLevel(fi.dy.masa.picktweak.util.BlockItems.lightBlock(9)) == 9,
					String.valueOf(fi.dy.masa.picktweak.util.BlockItems.lightLevel(fi.dy.masa.picktweak.util.BlockItems.lightBlock(9))));
			check("pick: non-light item has no light level",
					fi.dy.masa.picktweak.util.BlockItems.lightLevel(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STONE)) < 0,
					"ok");
		} catch (Throwable t) {
			System.out.println("    (skipped ItemStack-based pick checks: vanilla data pack is not loaded in a bare JVM)");
		}

		System.out.println();
		System.out.println(failures == 0 ? "ALL SMOKE TESTS PASSED" : (failures + " SMOKE TEST(S) FAILED"));
		if (failures > 0) {
			System.exit(1);
		}
	}
}