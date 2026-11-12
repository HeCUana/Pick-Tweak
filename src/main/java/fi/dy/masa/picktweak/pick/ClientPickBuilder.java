package fi.dy.masa.picktweak.pick;

import fi.dy.masa.picktweak.config.Configs;
import fi.dy.masa.picktweak.util.BlockItems;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
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

		// 流体（和粉雪）拾取成装满的桶
		if (pickFluids) {
			stack = BlockItems.fluidBucket(state);
		}

		if (stack.isEmpty()) {
			stack = state.getCloneItemStack(level, pos, false);
		}

		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		if (includeData && state.hasBlockEntity()) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity != null) {
				BlockPicker.applyBlockEntityData(level, blockEntity, stack);
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
	 * <p>委托 {@link EntityPickBuilder}：Ctrl 时给「放下能还原实体的物品 + entity_data」
	 * （生物→刷怪蛋、盔甲架→盔甲架、展示框→展示框、画→画作 + painting_variant），
	 * 不按 Ctrl 时沿用头颅 / 刷怪蛋 / 本体物品的原有规则。</p>
	 */
	public static ItemStack buildEntityItem(ClientLevel level, Entity entity, boolean includeData) {
		return EntityPickBuilder.build(level, entity, includeData, Configs.blockPicker.preferEntityHeads);
	}
}
