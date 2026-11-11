package fi.dy.masa.picktweak.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 方块 → 物品的一些通用处理（客户端和服务端都能用）。
 */
public final class BlockItems {

	private BlockItems() {
	}

	/**
	 * 流体（以及粉雪）对应的桶。
	 *
	 * <p>原版拾取流体给的是空桶，这里改成装满的桶，
	 * 对应面板上的「流体拾取成桶」开关。</p>
	 *
	 * @return 对应的桶；不是流体时返回 {@link ItemStack#EMPTY}
	 */
	public static ItemStack fluidBucket(BlockState state) {
		Item bucket = fluidBucketItem(state);
		return bucket == null ? ItemStack.EMPTY : new ItemStack(bucket);
	}

	/**
	 * 流体（以及粉雪）对应的桶物品。
	 *
	 * @return 对应的桶；不是流体时返回 {@code null}
	 */
	public static Item fluidBucketItem(BlockState state) {
		if (state == null) {
			return null;
		}

		Block block = state.getBlock();

		if (block == Blocks.WATER) {
			return Items.WATER_BUCKET;
		}
		if (block == Blocks.LAVA) {
			return Items.LAVA_BUCKET;
		}
		if (block == Blocks.POWDER_SNOW) {
			return Items.POWDER_SNOW_BUCKET;
		}

		return null;
	}

	/** 这个方块是不是流体（含粉雪）。 */
	public static boolean isFluid(BlockState state) {
		if (state == null) {
			return false;
		}

		Block block = state.getBlock();
		return block == Blocks.WATER || block == Blocks.LAVA || block == Blocks.POWDER_SNOW;
	}
}
