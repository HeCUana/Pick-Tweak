package fi.dy.masa.picktweak.net;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import fi.dy.masa.picktweak.util.BlockItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.TagValueOutput;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;

/**
 * 服务端拾取服务：接收客户端的拾取请求，权威地算出物品并发放给玩家。
 */
public final class ServerPickService {

	private static final Logger LOGGER = LogUtils.getLogger();

	private ServerPickService() {
	}

	/** 处理拾取方块的请求。 */
	public static void handlePickBlock(ServerPlayer player, BlockPos pos, boolean includeData, boolean includeState) {
		ServerLevel level = player.level();
		PbpServerConfig config = PbpServerConfig.get();
		if (!level.isLoaded(pos)) {
			return;
		}
		if (!withinDistance(player, pos, config.maxPickDistance)) {
			// 超出服务端允许的距离：静默忽略，不打扰玩家。
			return;
		}

		BlockState state = level.getBlockState(pos);
		ItemStack stack = ItemStack.EMPTY;

		String override = PbpOverrides.getBlockOverride(blockId(state));
		if (override != null) {
			stack = ItemStackParser.parse(override, level.registryAccess());
		}
		if (stack.isEmpty() && config.pickFluids) {
			// 流体（和粉雪）拾取成装满的桶
			stack = BlockItems.fluidBucket(state);
		}
		if (stack.isEmpty()) {
			stack = state.getCloneItemStack(level, pos, includeData && config.allowIncludeData);
		}
		if (stack.isEmpty()) {
			return;
		}
		if (includeData && config.allowIncludeData) {
			attachBlockEntityData(level, state, pos, stack);
		}
		if (includeState && config.allowStateProperties) {
			attachBlockState(stack, state);
		}
		give(player, stack);
	}

	/** 处理拾取实体的请求。 */
	public static void handlePickEntity(ServerPlayer player, int entityId, boolean includeData) {
		ServerLevel level = player.level();
		PbpServerConfig config = PbpServerConfig.get();
		Entity entity = level.getEntity(entityId);
		if (entity == null) {
			return;
		}
		if (!player.isWithinEntityInteractionRange(entity, config.maxEntityPickDistance)) {
			// 超出服务端允许的距离：静默忽略，不打扰玩家。
			return;
		}

		ItemStack stack = ItemStack.EMPTY;
		String override = PbpOverrides.getEntityOverride(entityId(entity));
		if (override != null) {
			stack = ItemStackParser.parse(override, level.registryAccess());
		}
		if (stack.isEmpty()) {
			stack = specialEntityItem(entity, includeData, config);
		}
		if (stack.isEmpty()) {
			ItemStack picked = entity.getPickResult();
			if (picked != null) {
				stack = picked.copy();
			}
		}
		if (stack.isEmpty()) {
			return;
		}
		if (includeData && config.allowIncludeData) {
			CompoundTag tag = entityNbt(level, entity);
			if (tag != null && !tag.isEmpty()) {
				stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
			}
		}
		give(player, stack);
	}

	/** 玩家头颅 / 物品框 / 画作 / 生物头颅。 */
	private static ItemStack specialEntityItem(Entity entity, boolean includeData, PbpServerConfig config) {
		if (entity instanceof net.minecraft.world.entity.player.Player other) {
			ItemStack stack = new ItemStack(net.minecraft.world.item.Items.PLAYER_HEAD);
			try {
				stack.set(DataComponents.PROFILE,
						net.minecraft.world.item.component.ResolvableProfile.createResolved(other.getGameProfile()));
			} catch (Exception ignored) {
				// 皮肤拿不到时给一个普通头颅
			}
			return stack;
		}
		if (entity instanceof net.minecraft.world.entity.decoration.ItemFrame frame) {
			ItemStack stack = new ItemStack(net.minecraft.world.item.Items.ITEM_FRAME);
			ItemStack framed = frame.getItem();
			if (includeData && !framed.isEmpty()) {
				stack.set(DataComponents.CUSTOM_NAME, framed.getHoverName());
			}
			return stack;
		}
		if (entity instanceof net.minecraft.world.entity.decoration.painting.Painting painting) {
			ItemStack picked = painting.getPickResult();
			ItemStack stack = picked == null || picked.isEmpty()
					? new ItemStack(net.minecraft.world.item.Items.PAINTING) : picked.copy();
			if (includeData) {
				Optional<net.minecraft.network.chat.Component> title = painting.getVariant().value().title();
				title.ifPresent(component -> stack.set(DataComponents.CUSTOM_NAME, component));
			}
			return stack;
		}
		if (config.preferEntityHeads) {
			Item head = HEADS.get(entityId(entity));
			if (head != null) {
				return new ItemStack(head);
			}
		}
		return ItemStack.EMPTY;
	}

	/** 常见生物对应的头颅物品。 */
	private static final Map<String, Item> HEADS = Map.of(
			"minecraft:zombie", net.minecraft.world.item.Items.ZOMBIE_HEAD,
			"minecraft:skeleton", net.minecraft.world.item.Items.SKELETON_SKULL,
			"minecraft:creeper", net.minecraft.world.item.Items.CREEPER_HEAD,
			"minecraft:piglin", net.minecraft.world.item.Items.PIGLIN_HEAD,
			"minecraft:dragon", net.minecraft.world.item.Items.DRAGON_HEAD
	);

	/**
	 * 处理拾取太阳 / 月亮的请求。
	 *
	 * <p>26.3 的太阳 / 月亮只是天空渲染层画出来的贴图，既不是方块也不是实体，
	 * 所以由客户端判断视线方向，服务端按配置发放对应的物品。</p>
	 */
	public static void handlePickCelestial(ServerPlayer player, String kind) {
		PbpServerConfig config = PbpServerConfig.get();
		String itemId = "moon".equalsIgnoreCase(kind) ? config.moonItem : config.sunItem;
		Identifier id = Identifier.tryParse(itemId);
		Item item = id == null ? null : BuiltInRegistries.ITEM.getValue(id);
		if (item == null) {
			warn(player, "picktweak.command.generate_failed", itemId);
			return;
		}
		give(player, new ItemStack(item));
	}

	/** 处理客户端请求发放工具 / 武器。 */
	public static void handleGiveTool(ServerPlayer player, String request) {
		PbpServerConfig config = PbpServerConfig.get();
		if (!config.allowClientGive) {
			warn(player, "picktweak.message.give_denied");
			return;
		}
		try {
			JsonObject json = JsonParser.parseString(request).getAsJsonObject();
			String kind = json.has("kind") ? json.get("kind").getAsString() : "";
			boolean weapon = json.has("weapon") && json.get("weapon").getAsBoolean();
			Map<String, String> items = weapon ? config.weaponItems : config.toolItems;
			String itemId = items.get(kind);
			if (itemId == null) {
				warn(player, "picktweak.command.unknown_type", kind);
				return;
			}
			Identifier id = Identifier.tryParse(itemId);
			Item item = id == null ? null : BuiltInRegistries.ITEM.getValue(id);
			if (item == null) {
				warn(player, "picktweak.command.generate_failed", kind);
				return;
			}
			ItemStack stack = new ItemStack(item);
			applyEnchantments(player.level(), stack, json, config.maxEnchantLevel);
			give(player, stack);
		} catch (Exception e) {
			LOGGER.warn("[Pick Block Pro] 处理 give 请求失败", e);
		}
	}

	/** 附魔：只接受注册表里存在的附魔，等级受服务端上限约束。 */
	private static void applyEnchantments(ServerLevel level, ItemStack stack, JsonObject json, int maxLevel) {
		if (!json.has("enchantments") || !json.get("enchantments").isJsonObject()) {
			return;
		}
		HolderLookup.RegistryLookup<Enchantment> lookup = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		JsonObject enchants = json.getAsJsonObject("enchantments");
		EnchantmentHelper.updateEnchantments(stack, mutable -> {
			for (Map.Entry<String, JsonElement> entry : enchants.entrySet()) {
				Identifier enchantId = Identifier.tryParse(entry.getKey());
				if (enchantId == null) {
					continue;
				}
				Optional<Holder.Reference<Enchantment>> holder = lookup.get(ResourceKey.create(Registries.ENCHANTMENT, enchantId));
				if (holder.isPresent()) {
					int level1 = Math.max(1, Math.min(maxLevel, entry.getValue().getAsInt()));
					mutable.set(holder.get(), level1);
				}
			}
		});
	}

	/** 附上方块实体的数据。 */
	private static void attachBlockEntityData(ServerLevel level, BlockState state, BlockPos pos, ItemStack stack) {
		if (!state.hasBlockEntity()) {
			return;
		}
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity == null) {
			return;
		}
		try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), LOGGER)) {
			TagValueOutput output = TagValueOutput.createWithContext(reporter, level.registryAccess());
			blockEntity.saveCustomOnly(output);
			blockEntity.removeComponentsFromTag(output);
			BlockItem.setBlockEntityData(stack, blockEntity.getType(), output);
			stack.applyComponents(blockEntity.collectComponents());
		} catch (Exception e) {
			LOGGER.debug("[Pick Block Pro] 读取方块实体数据失败", e);
		}
	}

	/** 附上方块状态属性。 */
	private static void attachBlockState(ItemStack stack, BlockState state) {
		BlockItemStateProperties properties = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
		for (Property<?> property : state.getProperties()) {
			properties = properties.with(property, state);
		}
		stack.set(DataComponents.BLOCK_STATE, properties);
	}

	/** 读取实体 NBT。 */
	private static CompoundTag entityNbt(ServerLevel level, Entity entity) {
		try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(LOGGER)) {
			TagValueOutput output = TagValueOutput.createWithContext(reporter, level.registryAccess());
			entity.saveWithoutId(output);
			CompoundTag tag = output.buildResult();
			tag.remove("id");
			return tag;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 发放物品。
	 *
	 * <p>背包里已有完全相同的物品时直接切过去；否则按服务端配置决定是否真正发放。
	 * 默认（生存 + 创造）都会发放——否则带 NBT / 状态属性的拾取会毫无反应。</p>
	 */
	private static void give(ServerPlayer player, ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}
		PbpServerConfig config = PbpServerConfig.get();
		Inventory inventory = player.getInventory();
		int slot = inventory.findSlotMatchingItem(stack);
		if (slot != -1) {
			if (Inventory.isHotbarSlot(slot)) {
				inventory.setSelectedSlot(slot);
			} else {
				inventory.pickSlot(slot);
			}
		} else if (player.hasInfiniteMaterials() || config.giveInSurvival) {
			inventory.addAndPickItem(stack);
		} else {
			// 生存模式且服务端关闭了发放：至少切换到同类物品，别让玩家以为没反应。
			int same = findSameItemSlot(inventory, stack);
			if (same != -1) {
				inventory.pickSlot(same);
			}
		}
		player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket(inventory.getSelectedSlot()));
		player.inventoryMenu.broadcastChanges();
	}

	/** 背包里是否有同类（物品相同、组件不同）的物品。 */
	private static int findSameItemSlot(Inventory inventory, ItemStack stack) {
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack candidate = inventory.getItem(i);
			if (!candidate.isEmpty() && candidate.getItem() == stack.getItem()) {
				return i;
			}
		}
		return -1;
	}

	private static boolean withinDistance(ServerPlayer player, BlockPos pos, double range) {
		double dx = player.getX() - (pos.getX() + 0.5D);
		double dy = player.getY() - (pos.getY() + 0.5D);
		double dz = player.getZ() - (pos.getZ() + 0.5D);
		return dx * dx + dy * dy + dz * dz <= range * range;
	}

	private static String blockId(BlockState state) {
		Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
		return id == null ? "minecraft:air" : id.toString();
	}

	private static String entityId(Entity entity) {
		Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
		return id == null ? "minecraft:pig" : id.toString();
	}

	private static void warn(ServerPlayer player, String key, Object... args) {
		player.sendSystemMessage(Component.translatable(key, args));
	}
}
