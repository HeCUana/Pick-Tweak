package fi.dy.masa.picktweak.pick;

import fi.dy.masa.picktweak.config.Configs;
import fi.dy.masa.picktweak.util.ItemNbtMarker;
import fi.dy.masa.picktweak.net.ItemStackParser;
import fi.dy.masa.picktweak.net.PbpOverrides;
import fi.dy.masa.picktweak.util.BlockItems;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 客户端本地构造拾取物品。
 *
 * <p>服务端没装 Pick Tweak 时，客户端自己算物品并尽量发放，
 * 这样 Ctrl（附加 NBT）与 Alt（附加状态属性）在单机 / 无服务端组件时同样可用。
 * 实体部分与服务端共用 {@link EntityPickBuilder}，避免两边行为漂移。</p>
 */
public final class ClientPickBuilder {

	private ClientPickBuilder() {
	}

	/** 构造方块物品。 */
	public static ItemStack buildBlockItem(ClientLevel level, BlockPos pos, boolean includeData, boolean includeState,
			boolean pickFluids) {
		BlockState state = level.getBlockState(pos);
		ItemStack stack = ItemStack.EMPTY;

		// 1) 自定义映射（picktweak-overrides.json）优先：完全自定义，含完整 NBT / 组件
		String override = PbpOverrides.getBlockOverride(BlockPicker.blockId(state));

		if (override != null && !override.isBlank()) {
			stack = ItemStackParser.parse(override, level.registryAccess());

			if (!stack.isEmpty()) {
				return stack;
			}
		}

		// 2) 流体（和粉雪）拾取成装满的桶
		if (pickFluids) {
			stack = BlockItems.fluidBucket(state);
		}

		// 3) 火方块 → 打火石
		if (stack.isEmpty()) {
			stack = BlockItems.firePickItem(state);
		}

		// 4) 方块本身对应的物品
		if (stack.isEmpty()) {
			stack = state.getCloneItemStack(level, pos, false);
		}

		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		// 5) 刷怪笼：按住 Ctrl 或本身就是刷怪笼时，都必须带方块实体数据（否则放下没有意义）
		if (state.hasBlockEntity() && (includeData || BlockItems.isSpawner(state))) {
			BlockEntity blockEntity = level.getBlockEntity(pos);

			if (blockEntity != null) {
				BlockPicker.applyBlockEntityData(level, blockEntity, stack);
				ItemNbtMarker.markNbt(stack);
			}
		}

		if (includeState) {
			BlockPicker.applyBlockState(stack, state);
		}

		return stack;
	}

	/**
	 * 构造实体物品。
	 *
	 * <p>先查自定义映射（完全自定义，含完整 NBT / 组件）；没有映射时委托
	 * {@link EntityPickBuilder}：Ctrl 时给「放下能还原实体的物品 + entity_data」
	 * （生物→刷怪蛋、盔甲架→盔甲架、展示框→展示框、画→画作 + painting_variant，
	 * 下落方块→方块物品 + 方块状态），不按 Ctrl 时沿用头颅 / 刷怪蛋 / 本体物品规则。</p>
	 */
	public static ItemStack buildEntityItem(ClientLevel level, Entity entity, boolean includeData) {
		Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
		String override = id == null ? null : PbpOverrides.getEntityOverride(id.toString());

		if (override != null && !override.isBlank()) {
			ItemStack stack = ItemStackParser.parse(override, level.registryAccess());

			if (!stack.isEmpty()) {
				return stack;
			}
		}

		return EntityPickBuilder.build(level, entity, includeData, Configs.blockPicker.preferEntityHeads);
	}
}