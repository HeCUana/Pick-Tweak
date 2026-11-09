package fi.dy.masa.picktweak.id;

import fi.dy.masa.picktweak.config.Configs;
import fi.dy.masa.picktweak.config.Configs;
import fi.dy.masa.picktweak.pick.BlockPicker;
import fi.dy.masa.picktweak.pick.ClientPicker;
import fi.dy.masa.picktweak.pick.ClientRaycast;
import fi.dy.masa.picktweak.util.NbtFormatter;
import fi.dy.masa.picktweak.util.PbpText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ID 拾取：把目标的标识信息输出到聊天栏（并可选复制到剪贴板）。
 */
public final class IdPicker {

	/** 物品 -> 方块 的反向映射（桶等特殊物品）。 */
	private static final Map<Item, String> ITEM_TO_BLOCK = Map.of(
			Items.WATER_BUCKET, "minecraft:water",
			Items.LAVA_BUCKET, "minecraft:lava",
			Items.POWDER_SNOW_BUCKET, "minecraft:powder_snow"
	);

	private IdPicker() {
	}

	/** 处理一次 ID 拾取。 */
	public static void pickId() {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		LocalPlayer player = mc.player;
		Configs.IdPicker config = Configs.idPicker;

		if (level == null || player == null || !config.enabled) {
			return;
		}
		HitResult hit = mc.hitResult;
		if (hit == null) {
			return;
		}

		boolean ctrl = mc.hasControlDown();
		String text = null;

		if (hit.getType() == HitResult.Type.BLOCK && config.pickBlocks) {
			BlockHitResult blockHit = (BlockHitResult) hit;
			double range = ClientPicker.effectiveRange(config.range, config.useInteractionRange, config.creativeRange, player);
			if (!ClientPicker.inBlockRange(player, blockHit.getBlockPos(), range)) {
				return;
			}
			BlockState state = level.getBlockState(blockHit.getBlockPos());
			text = ctrl ? blockNbtText(level, state, blockHit.getBlockPos(), config)
					: blockIdText(state, config);
		} else if (hit.getType() == HitResult.Type.ENTITY && config.pickEntities) {
			EntityHitResult entityHit = (EntityHitResult) hit;
			double range = ClientPicker.effectiveRange(config.entityRange, config.useInteractionRange, config.creativeRange, player);
			if (!ClientPicker.inEntityRange(player, entityHit.getEntity(), range)) {
				return;
			}
			Entity entity = entityHit.getEntity();
			text = ctrl ? entityNbtText(level, entity, config) : entityIdText(entity, config);
		} else if (hit.getType() == HitResult.Type.MISS && config.pickBlocks) {
			// 指着天空：可能是太阳 / 月亮。
			String celestial = ClientRaycast.pickCelestial(mc);
			if (celestial != null) {
				text = "minecraft:" + celestial;
			}
		}

		if (text == null || text.isEmpty()) {
			return;
		}

		if (config.copyToClipboard) {
			PbpText.copyToClipboard(text);
		}
		PbpText.chat(Component.literal(text));
	}

	/** 方块 ID 文本（可带状态属性）。 */
	public static String blockIdText(BlockState state, Configs.IdPicker config) {
		Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
		String base = formatId(id, config.includeNamespace);
		if (!config.includeStateProperties) {
			return base;
		}
		List<String> properties = new ArrayList<>();
		for (Property<?> property : state.getProperties()) {
			String name = property.getName();
			if (isBlacklisted(name, config.statePropertyBlacklist)) {
				continue;
			}
			properties.add(name + "=" + propertyName(state, property));
		}		if (properties.isEmpty()) {
			return base;
		}
		return base + "[" + String.join(",", properties) + "]";
	}

	/** 实体 ID 文本。 */
	public static String entityIdText(Entity entity, Configs.IdPicker config) {
		Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
		return formatId(id, config.includeNamespace);
	}

	/** 物品 ID 文本（可按配置转换成方块 ID）。 */
	public static String itemIdText(ItemStack stack, Configs.IdPicker config) {
		if (stack == null || stack.isEmpty()) {
			return "";
		}
		if (config.itemToBlockId) {
			String mapped = ITEM_TO_BLOCK.get(stack.getItem());
			if (mapped != null) {
				return mapped;
			}
			Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
			String text = id == null ? "" : id.toString();
			if (text.endsWith("_wall")) {
				return stripNamespace(text.substring(0, text.length() - "_wall".length()));
			}
			if (text.endsWith("_stairs")) {
				return stripNamespace(text.substring(0, text.length() - "_stairs".length()));
			}
			if (text.endsWith("_slab")) {
				return stripNamespace(text.substring(0, text.length() - "_slab".length()));
			}
		}
		Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
		return formatId(id, config.includeNamespace);
	}

	/** 方块实体的 NBT 文本。 */
	public static String blockNbtText(ClientLevel level, BlockState state, net.minecraft.core.BlockPos pos, Configs.IdPicker config) {
		if (!state.hasBlockEntity()) {
			return blockIdText(state, config);
		}
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity == null) {
			return blockIdText(state, config);
		}
		CompoundTag tag = blockEntity.saveCustomOnly(level.registryAccess());
		CompoundTag filtered = NbtFormatter.filter(tag, config.nbtBlacklist);
		return NbtFormatter.format(filtered, config.prettyNbt);
	}

	/** 实体的 NBT 文本。 */
	public static String entityNbtText(ClientLevel level, Entity entity, Configs.IdPicker config) {
		CompoundTag tag = BlockPicker.entityNbt(level, entity);
		if (tag == null) {
			return entityIdText(entity, config);
		}
		CompoundTag filtered = NbtFormatter.filter(tag, config.nbtBlacklist);
		return NbtFormatter.format(filtered, config.prettyNbt);
	}

	private static String formatId(Identifier id, boolean withNamespace) {
		if (id == null) {
			return "";
		}
		return withNamespace ? id.toString() : id.getPath();
	}

	private static String stripNamespace(String id) {
		int index = id.indexOf(':');
		return index >= 0 ? id.substring(index + 1) : id;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static String propertyName(BlockState state, Property<?> property) {
		return ((Property) property).getName(state.getValue(property));
	}

	private static boolean isBlacklisted(String name, List<String> blacklist) {
		if (blacklist == null) {
			return false;
		}
		for (String entry : blacklist) {
			if (entry != null && entry.equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
}
