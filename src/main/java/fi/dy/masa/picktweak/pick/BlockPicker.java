package fi.dy.masa.picktweak.pick;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import org.slf4j.Logger;

/**
 * 拾取相关的通用小工具：方块状态属性、方块实体 NBT、实体 NBT。
 *
 * <p>拾取动作本身由服务端权威处理（见 {@code ServerPickService}），
 * 这里保留的是客户端 ID 拾取等场景要用的数据读取。</p>
 */
public final class BlockPicker {

	private static final Logger LOGGER = LogUtils.getLogger();

	private BlockPicker() {
	}

	/** 把方块状态属性写进物品的 block_state 组件。 */
	public static void applyBlockState(ItemStack stack, BlockState state) {
		BlockItemStateProperties properties = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
		for (Property<?> property : state.getProperties()) {
			properties = properties.with(property, state);
		}
		stack.set(DataComponents.BLOCK_STATE, properties);
	}

	/** 把方块实体的数据写进物品。 */
	public static void applyBlockEntityData(ClientLevel level, BlockEntity blockEntity, ItemStack stack) {
		try (ProblemReporter.ScopedCollector collector = new ProblemReporter.ScopedCollector(LOGGER)) {
			TagValueOutput output = TagValueOutput.createWithContext(collector, level.registryAccess());
			blockEntity.saveCustomOnly(output);
			blockEntity.removeComponentsFromTag(output);
			net.minecraft.world.item.BlockItem.setBlockEntityData(stack, blockEntity.getType(), output);
			stack.applyComponents(blockEntity.collectComponents());
		} catch (Exception e) {
			LOGGER.debug("[Pick Block Pro] 读取方块实体数据失败", e);
		}
	}

	/** 读取实体 NBT（不含 id）。 */
	public static CompoundTag entityNbt(ClientLevel level, Entity entity) {
		try (ProblemReporter.ScopedCollector collector = new ProblemReporter.ScopedCollector(LOGGER)) {
			TagValueOutput output = TagValueOutput.createWithContext(collector, level.registryAccess());
			entity.saveWithoutId(output);
			CompoundTag tag = output.buildResult();
			tag.remove("id");
			return tag;
		} catch (Exception e) {
			return null;
		}
	}
}
