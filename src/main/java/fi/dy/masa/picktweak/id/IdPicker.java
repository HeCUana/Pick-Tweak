package fi.dy.masa.picktweak.id;

import fi.dy.masa.picktweak.config.Configs;
import fi.dy.masa.picktweak.config.HotkeyBinds;
import fi.dy.masa.picktweak.pick.BlockPicker;
import fi.dy.masa.picktweak.pick.ClientPicker;
import fi.dy.masa.picktweak.pick.ClientRaycast;
import fi.dy.masa.picktweak.util.NbtFormatter;
import fi.dy.masa.picktweak.util.PbpText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Map;

/**
 * ID 拾取：把目标的标识信息输出到聊天栏（并可选复制到剪贴板）。
 */
public final class IdPicker {

	/** 物品 -> 方块 的反向映射（桶不是 BlockItem，只能显式列出）。 */
	private static final Map<Item, Block> ITEM_TO_BLOCK = Map.of(
			Items.WATER_BUCKET, Blocks.WATER,
			Items.LAVA_BUCKET, Blocks.LAVA,
			Items.POWDER_SNOW_BUCKET, Blocks.POWDER_SNOW
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

		// NBT 输出：配置项 includeNbt 与 NBT 修饰键（默认 Ctrl）异或，
		// 也就是按着修饰键时临时切换一次，和配置里的默认值无关。
		boolean nbt = config.includeNbt ^ HotkeyBinds.isModifierActive(HotkeyBinds.Modifier.NBT);
		String text = null;

		// 原版的 hitResult 受交互距离限制，这里自己按配置的距离找目标
		double blockRange = ClientPicker.effectiveRange(config.range, config.useInteractionRange, config.creativeRange, player);
		double entityRange = ClientPicker.effectiveRange(config.entityRange, config.useInteractionRange, config.creativeRange, player);
		HitResult hit = ClientRaycast.resolveHit(mc, Math.max(blockRange, entityRange));

		if (hit == null) {
			return;
		}

		if (hit.getType() == HitResult.Type.BLOCK && config.pickBlocks) {
			BlockHitResult blockHit = (BlockHitResult) hit;
			if (!ClientRaycast.withinRange(mc, hit, blockRange)) {
				return;
			}
			BlockState state = level.getBlockState(blockHit.getBlockPos());
			text = nbt ? blockNbtText(level, state, blockHit.getBlockPos(), config)
					: blockIdText(state, config);
		} else if (hit.getType() == HitResult.Type.ENTITY) {
			EntityHitResult entityHit = (EntityHitResult) hit;
			if (!ClientRaycast.withinRange(mc, hit, entityRange)) {
				return;
			}
			Entity entity = entityHit.getEntity();
			if (entity instanceof ItemEntity itemEntity && config.pickItems) {
				// 掉落物：输出物品 ID（物品本身没有 NBT 视图，切换 NBT 无效）
				text = itemIdText(itemEntity.getItem(), config);
			} else if (config.pickEntities) {
				text = nbt ? entityNbtText(level, entity, config) : entityIdText(entity, config);
			}
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
		// 状态属性文本与 Alt + 中键复制共用 BlockPicker.blockStateText（含属性黑名单）
		String properties = BlockPicker.blockStateText(state, config.statePropertyBlacklist);
		return properties.isEmpty() ? base : base + "[" + properties + "]";
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
		Item item = stack.getItem();
		if (config.itemToBlockId) {
			// BlockItem 直接查它对应的方块；桶之类不是 BlockItem 的物品查显式映射。
			Block block = item instanceof BlockItem blockItem ? blockItem.getBlock() : ITEM_TO_BLOCK.get(item);
			if (block != null) {
				return formatId(BuiltInRegistries.BLOCK.getKey(block), config.includeNamespace);
			}
		}
		return formatId(BuiltInRegistries.ITEM.getKey(item), config.includeNamespace);
	}

	/** 方块实体的 NBT 文本（与 Ctrl + 中键复制共用 {@link BlockPicker#blockEntityNbt}，含 components）。 */
	public static String blockNbtText(ClientLevel level, BlockState state, BlockPos pos, Configs.IdPicker config) {
		if (!state.hasBlockEntity()) {
			return blockIdText(state, config);
		}
		CompoundTag tag = BlockPicker.blockEntityNbt(level, pos);
		if (tag == null) {
			return blockIdText(state, config);
		}
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

}
