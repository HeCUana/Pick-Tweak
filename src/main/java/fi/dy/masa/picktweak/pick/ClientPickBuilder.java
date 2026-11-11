package fi.dy.masa.picktweak.pick;

import fi.dy.masa.picktweak.util.BlockItems;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Optional;

/**
 * 客户端本地构造拾取物品。
 *
 * <p>服务端没装 Pick Tweak 时，客户端自己算物品并尽量发放，
 * 这样 Ctrl（附加 NBT）与 Alt（附加状态属性）在单机 / 无服务端组件时同样可用。</p>
 */
public final class ClientPickBuilder {

	/** 常见生物对应的头颅物品。 */
	private static final Map<String, Item> HEADS = Map.of(
			"minecraft:zombie", Items.ZOMBIE_HEAD,
			"minecraft:skeleton", Items.SKELETON_SKULL,
			"minecraft:creeper", Items.CREEPER_HEAD,
			"minecraft:piglin", Items.PIGLIN_HEAD,
			"minecraft:dragon", Items.DRAGON_HEAD
	);

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

	/** 构造实体物品。 */
	public static ItemStack buildEntityItem(ClientLevel level, Entity entity, boolean includeData) {
		ItemStack stack = specialEntityItem(entity, includeData);
		if (stack.isEmpty()) {
			ItemStack picked = entity.getPickResult();
			if (picked != null) {
				stack = picked.copy();
			}
		}
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		if (includeData) {
			CompoundTag tag = BlockPicker.entityNbt(level, entity);
			if (tag != null && !tag.isEmpty()) {
				stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
			}
		}
		return stack;
	}

	/** 玩家头颅 / 物品框 / 画作 / 生物头颅。 */
	private static ItemStack specialEntityItem(Entity entity, boolean includeData) {
		if (entity instanceof Player other) {
			ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
			try {
				stack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(other.getGameProfile()));
			} catch (Exception ignored) {
				// 皮肤拿不到时给一个普通头颅
			}
			return stack;
		}
		if (entity instanceof ItemFrame frame) {
			ItemStack stack = new ItemStack(Items.ITEM_FRAME);
			ItemStack framed = frame.getItem();
			if (includeData && !framed.isEmpty()) {
				stack.set(DataComponents.CUSTOM_NAME, framed.getHoverName());
			}
			return stack;
		}
		if (entity instanceof Painting painting) {
			ItemStack picked = painting.getPickResult();
			ItemStack stack = picked == null || picked.isEmpty() ? new ItemStack(Items.PAINTING) : picked.copy();
			if (includeData) {
				Optional<Component> title = painting.getVariant().value().title();
				title.ifPresent(component -> stack.set(DataComponents.CUSTOM_NAME, component));
			}
			return stack;
		}
		Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
		if (id != null) {
			Item head = HEADS.get(id.toString());
			if (head != null) {
				return new ItemStack(head);
			}
		}
		return ItemStack.EMPTY;
	}
}
