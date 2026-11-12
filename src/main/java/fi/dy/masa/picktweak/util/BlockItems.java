package fi.dy.masa.picktweak.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

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
	 * 对应面板 / 服务端配置里的「流体拾取成桶」开关（默认关闭）。</p>
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

	/**
	 * 火方块（普通火 / 灵魂火）的拾取物品：打火石（1.16+ 的参考行为）。
	 *
	 * @return 打火石；不是火方块时返回 {@link ItemStack#EMPTY}
	 */
	public static ItemStack firePickItem(BlockState state) {
		if (state == null) {
			return ItemStack.EMPTY;
		}
		Block block = state.getBlock();
		if (block == Blocks.FIRE || block == Blocks.SOUL_FIRE) {
			return new ItemStack(Items.FLINT_AND_STEEL);
		}
		return ItemStack.EMPTY;
	}

	/** 是否是刷怪笼（普通 / 试炼）。这类方块默认要带 NBT 才有意义。 */
	public static boolean isSpawner(BlockState state) {
		if (state == null) {
			return false;
		}
		Block block = state.getBlock();
		return block == Blocks.SPAWNER || block == Blocks.TRIAL_SPAWNER;
	}

	/** 生成指定光照等级的光源方块物品（level 夹取到 0..15）。 */
	public static ItemStack lightBlock(int level) {
		int clamped = Math.max(0, Math.min(LightBlock.MAX_LEVEL, level));
		ItemStack stack = new ItemStack(Items.LIGHT);
		BlockItemStateProperties properties = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
		properties = properties.with(LightBlock.LEVEL, clamped);
		stack.set(DataComponents.BLOCK_STATE, properties);
		return stack;
	}

	/**
	 * 物品当前的光照等级。
	 *
	 * @return 0..15；不是光源方块物品时返回 -1
	 */
	public static int lightLevel(ItemStack stack) {
		if (stack == null || stack.isEmpty() || stack.getItem() != Items.LIGHT) {
			return -1;
		}
		BlockItemStateProperties properties = stack.get(DataComponents.BLOCK_STATE);
		Integer level = properties == null ? null : properties.get(LightBlock.LEVEL);
		// 原版光源方块物品默认是 15 级
		return level == null ? LightBlock.MAX_LEVEL : level;
	}

	/** 循环到下一个光照等级（15 → 0）。 */
	public static int nextLightLevel(int current) {
		return current >= LightBlock.MAX_LEVEL ? 0 : Math.max(0, current + 1);
	}

	/** 把方块状态属性写进物品（服务端 / 客户端共用的通用版本）。 */
	public static void applyState(ItemStack stack, BlockState state) {
		if (stack == null || stack.isEmpty() || state == null) {
			return;
		}
		BlockItemStateProperties properties = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
		for (Property<?> property : state.getProperties()) {
			properties = properties.with(property, state);
		}
		stack.set(DataComponents.BLOCK_STATE, properties);
	}
}
