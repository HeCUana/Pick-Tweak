package fi.dy.masa.picktweak.net;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import fi.dy.masa.picktweak.Reference;
import fi.dy.masa.picktweak.pick.EntityPickBuilder;
import fi.dy.masa.picktweak.util.BlockItems;
import fi.dy.masa.picktweak.util.ItemNbtMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.TagValueOutput;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端拾取服务：接收客户端的拾取请求，权威地算出物品并发放给玩家。
 */
public final class ServerPickService {

	private static final Logger LOGGER = LogUtils.getLogger();

	/** 每玩家令牌桶：限制单位时间内的拾取 / 发放 / 天体请求数。 */
	private static final Map<UUID, TokenBucket> RATE_LIMIT = new ConcurrentHashMap<>();
	/** 令牌桶条目数的清理阈值。 */
	private static final int MAX_TRACKED_BUCKETS = 1024;
	/** 令牌桶空闲多久后清理（纳秒）。 */
	private static final long BUCKET_IDLE_NANOS = 300_000_000_000L;

	private ServerPickService() {
	}

	/**
	 * 每玩家令牌桶限流。
	 *
	 * @return false 表示超出速率，调用方应静默丢弃这次请求
	 */
	private static boolean allowRequest(ServerPlayer player) {
		PbpServerConfig config = PbpServerConfig.get();
		int rate = Math.max(1, Math.min(200, config.rateLimitPerSecond));
		long now = System.nanoTime();

		pruneBuckets(now);
		return RATE_LIMIT.computeIfAbsent(player.getUUID(), id -> new TokenBucket(now)).tryConsume(now, rate);
	}

	private static void pruneBuckets(long now) {
		if (RATE_LIMIT.size() <= MAX_TRACKED_BUCKETS) {
			return;
		}

		RATE_LIMIT.entrySet().removeIf(entry -> entry.getValue().isIdle(now, BUCKET_IDLE_NANOS));
	}

	/** 简单令牌桶：容量等于每秒速率，允许最多 1 秒的突发。 */
	private static final class TokenBucket {
		private double tokens;
		private long lastRefill;
		private long lastUsed;

		private TokenBucket(long now) {
			// 初始只给 1 个令牌，避免登录瞬间被一波请求打爆
			this.tokens = 1.0D;
			this.lastRefill = now;
			this.lastUsed = now;
		}

		private synchronized boolean tryConsume(long now, int rate) {
			double elapsed = (now - this.lastRefill) / 1_000_000_000.0D;
			this.tokens = Math.min(rate, this.tokens + elapsed * rate);
			this.lastRefill = now;
			this.lastUsed = now;

			if (this.tokens < 1.0D) {
				return false;
			}

			this.tokens -= 1.0D;
			return true;
		}

		private synchronized boolean isIdle(long now, long idleNanos) {
			return now - this.lastUsed > idleNanos;
		}
	}

	/** 处理拾取方块的请求。 */
	public static void handlePickBlock(ServerPlayer player, BlockPos pos, boolean includeData, boolean includeState) {
		if (!allowRequest(player)) {
			return;
		}

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
		if (stack.isEmpty()) {
			// 火 / 灵魂火 → 打火石
			stack = BlockItems.firePickItem(state);
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
		// 刷怪笼默认带 NBT（SpawnData / SpawnPotentials），否则放下后是空笼子
		if ((includeData || BlockItems.isSpawner(state)) && config.allowIncludeData) {
			attachBlockEntityData(level, state, pos, stack);
		}
		if (includeState && config.allowStateProperties) {
			attachBlockState(stack, state);
		}
		give(player, stack);
	}

	/** 处理拾取实体的请求。 */
	public static void handlePickEntity(ServerPlayer player, int entityId, boolean includeData) {
		if (!allowRequest(player)) {
			return;
		}

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
			// 统一构建器（客户端回退同源）：
			// 普通拾取按配置给头颅 / 刷怪蛋 / 本体物品；
			// Ctrl 拾取则写入 minecraft:entity_data，放下后还原实体数据。
			stack = EntityPickBuilder.build(level, entity,
					includeData && config.allowIncludeData, config.preferEntityHeads);
		}
		if (stack.isEmpty()) {
			return;
		}
		give(player, stack);
	}

	/**
	 * 处理拾取太阳 / 月亮的请求。
	 *
	 * <p>26.3 的太阳 / 月亮只是天空渲染层画出来的贴图，既不是方块也不是实体，
	 * 所以由客户端判断视线方向，服务端按配置发放对应的物品。</p>
	 */
	public static void handlePickCelestial(ServerPlayer player, String kind) {
		if (!allowRequest(player)) {
			return;
		}

		PbpServerConfig config = PbpServerConfig.get();

		if (!config.allowCelestial) {
			return;
		}

		String normalized = kind == null ? "" : kind.trim().toLowerCase(Locale.ROOT);

		// 只接受 sun / moon，非法 kind 静默丢弃
		if (!"sun".equals(normalized) && !"moon".equals(normalized)) {
			return;
		}

		// 手持光源方块时：按光照等级循环发放（太阳 / 月亮都是光源方块）
		int heldLevel = BlockItems.lightLevel(player.getMainHandItem());

		if (heldLevel < 0) {
			heldLevel = BlockItems.lightLevel(player.getOffhandItem());
		}

		if (heldLevel >= 0) {
			int next = BlockItems.nextLightLevel(heldLevel);
			give(player, BlockItems.lightBlock(next));
			player.sendSystemMessage(Component.translatable("picktweak.message.light_level", next));
			return;
		}

		String itemId = "moon".equals(normalized) ? config.moonItem : config.sunItem;
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
		if (!allowRequest(player)) {
			return;
		}

		PbpServerConfig config = PbpServerConfig.get();

		if (!config.allowClientGive) {
			warn(player, "picktweak.message.give_denied");
			return;
		}

		if (config.requireOpForGive && !PbpServerNetworking.isModerator(player)) {
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
			LOGGER.warn("[{}] 处理 give 请求失败", Reference.MOD_NAME, e);
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

				if (holder.isEmpty()) {
					continue;
				}

				int requested;

				try {
					requested = entry.getValue().getAsInt();
				} catch (RuntimeException e) {
					continue;
				}

				int clamped = Math.max(1, Math.min(maxLevel, requested));
				mutable.set(holder.get(), clamped);
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
			// 带方块实体 NBT 的物品加一行紫色 +NBT 副标题
			ItemNbtMarker.markNbt(stack);
		} catch (Exception e) {
			LOGGER.debug("[{}] 读取方块实体数据失败", Reference.MOD_NAME, e);
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

	/**
	 * 发放物品。
	 *
	 * <p>背包里已有完全相同的物品时直接切过去；否则只有创造模式（或服务端显式开启
	 * {@code giveInSurvival}）才真正发放。生存模式默认只切换到同类物品，
	 * 避免拾取变成免费的物品生成器。</p>
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