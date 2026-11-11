package fi.dy.masa.picktweak.tool;

import fi.dy.masa.picktweak.config.Configs;
import fi.dy.masa.picktweak.inv.InventoryManager;
import fi.dy.masa.picktweak.pick.ClientPicker;
import fi.dy.masa.picktweak.pick.ClientRaycast;
import fi.dy.masa.picktweak.util.PbpText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.SolidBucketItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 工具 / 武器拾取：按综合评分挑出最合适的工具，生存模式切换到该槽位，
 * 创造模式直接生成带满附魔的工具。
 */
public final class ToolPicker {

	/** 剪刀可以剪的方块。 */
	private static final Set<String> SHEARABLE = Set.of(
			"minecraft:cobweb",
			"minecraft:vine",
			"minecraft:cave_vines",
			"minecraft:hanging_roots",
			"minecraft:short_grass",
			"minecraft:tall_grass",
			"minecraft:fern",
			"minecraft:large_fern",
			"minecraft:glow_lichen",
			"minecraft:seagrass",
			"minecraft:tall_seagrass",
			"minecraft:kelp",
			"minecraft:kelp_plant",
			"minecraft:grass"
	);

	/** 需要用剑砍的方块。 */
	private static final Set<String> SWORD_PREFERRED = Set.of(
			"minecraft:bamboo",
			"minecraft:bamboo_block",
			"minecraft:bamboo_mosaic",
			"minecraft:bamboo_mosaic_slab",
			"minecraft:bamboo_mosaic_stairs",
			"minecraft:bamboo_slab",
			"minecraft:bamboo_stairs",
			"minecraft:bamboo_fence",
			"minecraft:bamboo_fence_gate",
			"minecraft:bamboo_door",
			"minecraft:bamboo_trapdoor",
			"minecraft:bamboo_sign",
			"minecraft:bamboo_pressure_plate"
	);

	/** 工具种类 -> 配置里的键。 */
	public static final List<String> TOOL_KINDS = List.of(
			"pickaxe", "axe", "shovel", "hoe", "sword", "shears", "bucket", "fishing_rod", "brush");

	/** 武器种类 -> 配置里的键。 */
	public static final List<String> WEAPON_KINDS = List.of(
			"sword", "axe", "bow", "crossbow", "trident", "mace");

	private ToolPicker() {
	}

	/** 处理一次工具 / 武器拾取。 */
	public static void pick() {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		LocalPlayer player = mc.player;
		Configs.ToolPicker config = Configs.toolPicker;

		if (level == null || player == null || !config.enabled) {
			return;
		}

		// 原版的 hitResult 受交互距离限制，这里自己按配置的距离找目标
		double range = ClientPicker.effectiveRange(config.range, false, config.creativeRange, player);
		HitResult hit = ClientRaycast.resolveHit(mc, range);

		if (hit == null) {
			return;
		}
		if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit) {
			if (!ClientRaycast.withinRange(mc, hit, range)) {
				return;
			}
			handleBlock(mc, level, player, config, blockHit.getBlockPos());
		} else if (hit.getType() == HitResult.Type.ENTITY && hit instanceof EntityHitResult entityHit) {
			if (!ClientRaycast.withinRange(mc, hit, range)) {
				return;
			}
			handleEntity(mc, level, player, config, entityHit.getEntity());
		}
	}

	private static void handleBlock(Minecraft mc, ClientLevel level, LocalPlayer player, Configs.ToolPicker config, net.minecraft.core.BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		String kind = determineToolKind(state, config);
		if (kind == null) {
			PbpText.chat(Component.translatable("picktweak.message.no_tool"));
			return;
		}

		if (player.hasInfiniteMaterials() && config.generateInCreative) {
			if (requestGive(mc, config, kind, false)) {
				return;
			}
			ItemStack generated = generateTool(level, config, kind, false);
			if (generated.isEmpty()) {
				PbpText.chat(Component.translatable("picktweak.message.generate_failed"));
				return;
			}
			InventoryManager.giveCreative(generated);
			PbpText.chat(Component.translatable("picktweak.message.generated", generated.getHoverName()));
			return;
		}

		int best = findBestTool(player, state, config, kind);
		if (best < 0) {
			PbpText.chat(Component.translatable("picktweak.message.no_tool_in_inventory"));
			return;
		}
		InventoryManager.selectOrSwap(best);
	}

	private static void handleEntity(Minecraft mc, ClientLevel level, LocalPlayer player, Configs.ToolPicker config, Entity entity) {
		if (player.hasInfiniteMaterials() && config.generateInCreative) {
			String kind = preferredWeapon(config, entity);
			if (requestGive(mc, config, kind, true)) {
				return;
			}
			ItemStack generated = generateTool(level, config, kind, true);
			if (generated.isEmpty()) {
				PbpText.chat(Component.translatable("picktweak.message.generate_failed"));
				return;
			}
			InventoryManager.giveCreative(generated);
			PbpText.chat(Component.translatable("picktweak.message.generated", generated.getHoverName()));
			return;
		}

		int best = findBestWeapon(player, entity, config);
		if (best < 0) {
			PbpText.chat(Component.translatable("picktweak.message.no_weapon_in_inventory"));
			return;
		}
		InventoryManager.selectOrSwap(best);
	}

	/**
	 * 请求服务端发放工具 / 武器。
	 *
	 * @return 服务端是否受理（false 表示服务端没装 PBP，调用方需要自己本地生成）
	 */
	public static boolean requestGive(Minecraft mc, Configs.ToolPicker config, String kind, boolean weapon) {
		if (!net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
				.canSend(fi.dy.masa.picktweak.net.PbpPayloads.GiveTool.TYPE)) {
			return false;
		}
		StringBuilder json = new StringBuilder();
		json.append("{\"kind\":\"").append(kind).append("\",\"weapon\":").append(weapon).append(",\"enchantments\":{");
		boolean first = true;
		for (Map.Entry<String, Integer> entry : config.tools.getOrDefault(kind, new Configs.ToolSpec()).enchantments.entrySet()) {
			if (!first) {
				json.append(',');
			}
			first = false;
			json.append('"').append(entry.getKey()).append("\":").append(entry.getValue());
		}
		json.append("}}");
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
				.send(new fi.dy.masa.picktweak.net.PbpPayloads.GiveTool(json.toString()));
		return true;
	}

	/** 判断某个方块应该用哪种工具。 */
	public static String determineToolKind(BlockState state, Configs.ToolPicker config) {
		Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
		String path = id == null ? "" : id.toString();

		if (config.pickFluid && !state.getFluidState().isEmpty()) {
			return "bucket";
		}
		if (state.getBlock() instanceof BrushableBlock) {
			return "brush";
		}
		if (SHEARABLE.contains(path)) {
			return "shears";
		}
		if (config.bambooPrefersSword && SWORD_PREFERRED.contains(path)) {
			return "sword";
		}
		Block block = state.getBlock();
		if (block.defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE)) {
			return "pickaxe";
		}
		if (block.defaultBlockState().is(BlockTags.MINEABLE_WITH_AXE)) {
			return "axe";
		}
		if (block.defaultBlockState().is(BlockTags.MINEABLE_WITH_SHOVEL)) {
			return "shovel";
		}
		if (block.defaultBlockState().is(BlockTags.MINEABLE_WITH_HOE)) {
			return "hoe";
		}
		return null;
	}

	/** 在背包中找出对某个方块最合适的工具。 */
	public static int findBestTool(LocalPlayer player, BlockState state, Configs.ToolPicker config, String kind) {
		net.minecraft.world.entity.player.Inventory inventory = player.getInventory();
		boolean searchContainers = Configs.inventory.searchContainers;
		int bestSlot = -1;
		double bestScore = Double.NEGATIVE_INFINITY;

		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.isEmpty()) {
				continue;
			}
			// 潜影盒 / 收纳袋：看看里面有没有合适的工具，有就切到容器所在槽位。
			if (searchContainers && fi.dy.masa.picktweak.inv.InventoryManager.isContainer(stack)) {
				ItemStack inner = fi.dy.masa.picktweak.inv.InventoryManager.findInContainer(stack,
						candidate -> matchesKind(candidate, kind, state) && !isTooDamaged(candidate, config));
				if (!inner.isEmpty()) {
					double score = scoreTool(inner, state, config);
					if (score > bestScore) {
						bestScore = score;
						bestSlot = slot;
					}
				}
				continue;
			}
			if (!matchesKind(stack, kind, state)) {
				continue;
			}
			if (isTooDamaged(stack, config)) {
				continue;
			}
			double score = scoreTool(stack, state, config);
			if (score > bestScore) {
				bestScore = score;
				bestSlot = slot;
			}
		}
		return bestSlot;
	}

	/** 物品是否属于该工具种类。 */
	private static boolean matchesKind(ItemStack stack, String kind, BlockState state) {
		Item item = stack.getItem();
		switch (kind) {
			case "shears":
				return item instanceof ShearsItem;
			case "bucket":
				return item instanceof BucketItem || item instanceof SolidBucketItem;
			case "brush":
				return item instanceof BrushItem;
			case "fishing_rod":
				return item instanceof FishingRodItem;
			default:
				break;
		}
		Tool tool = stack.get(DataComponents.TOOL);
		if (tool == null) {
			return false;
		}
		return tool.isCorrectForDrops(state) || tool.getMiningSpeed(state) > 1.0F;
	}

	/** 剩余耐久是否低于阈值。 */
	private static boolean isTooDamaged(ItemStack stack, Configs.ToolPicker config) {
		if (!stack.isDamageableItem()) {
			return false;
		}
		int remaining = stack.getMaxDamage() - stack.getDamageValue();
		return remaining < config.durabilityThreshold;
	}

	/** 工具评分。 */
	public static double scoreTool(ItemStack stack, BlockState state, Configs.ToolPicker config) {
		double score = 0.0D;
		Tool tool = stack.get(DataComponents.TOOL);
		if (tool != null) {
			score += tool.getMiningSpeed(state) * 100.0D;
			if (tool.isCorrectForDrops(state)) {
				score += 500.0D;
			}
		} else {
			score += stack.getDestroySpeed(state) * 100.0D;
		}
		score += stack.getMaxDamage() / 1000.0D;
		score += enchantScore(stack, config);
		return score;
	}

	/** 附魔带来的评分加成。 */
	private static double enchantScore(ItemStack stack, Configs.ToolPicker config) {
		HolderLookup.RegistryLookup<Enchantment> lookup = enchantLookup();
		if (lookup == null) {
			return 0.0D;
		}
		double score = 0.0D;
		score += level(lookup, stack, "silk_touch") * (config.preferSilkTouch ? 30.0D : 2.0D);
		score += level(lookup, stack, "fortune") * (config.preferFortune ? 20.0D : 2.0D);
		score += level(lookup, stack, "efficiency") * (config.preferEfficiency ? 15.0D : 2.0D);
		score += level(lookup, stack, "unbreaking") * 3.0D;
		score += level(lookup, stack, "mending") * 5.0D;
		return score;
	}

	private static int level(HolderLookup.RegistryLookup<Enchantment> lookup, ItemStack stack, String id) {
		Optional<Holder.Reference<Enchantment>> holder = lookup.get(ResourceKey.create(Registries.ENCHANTMENT, Identifier.withDefaultNamespace(id)));
		return holder.map(h -> EnchantmentHelper.getItemEnchantmentLevel(h, stack)).orElse(0);
	}

	/** 在背包中找出对某个实体最合适的武器。 */
	public static int findBestWeapon(LocalPlayer player, Entity entity, Configs.ToolPicker config) {
		net.minecraft.world.entity.player.Inventory inventory = player.getInventory();
		int bestSlot = -1;
		double bestScore = Double.NEGATIVE_INFINITY;
		String preferred = preferredWeapon(config, entity);

		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.isEmpty()) {
				continue;
			}
			if (!isWeapon(stack)) {
				continue;
			}
			if (isTooDamaged(stack, config)) {
				continue;
			}
			double score = scoreWeapon(stack, entity, config);
			if (weaponKind(stack).equals(preferred)) {
				score += 1000.0D;
			}
			if (score > bestScore) {
				bestScore = score;
				bestSlot = slot;
			}
		}
		return bestSlot;
	}

	/** 判断物品是否算武器。 */
	public static boolean isWeapon(ItemStack stack) {
		Item item = stack.getItem();
		String id = itemId(item);
		return id.endsWith("_sword") || id.endsWith("_axe") || id.endsWith("_mace")
				|| id.equals("minecraft:trident") || id.equals("minecraft:bow")
				|| id.equals("minecraft:crossbow");
	}

	/** 武器种类：sword / axe / bow / crossbow / trident / mace。 */
	public static String weaponKind(ItemStack stack) {
		String id = itemId(stack.getItem());
		if (id.equals("minecraft:bow")) {
			return "bow";
		}
		if (id.equals("minecraft:crossbow")) {
			return "crossbow";
		}
		if (id.equals("minecraft:trident")) {
			return "trident";
		}
		if (id.endsWith("_mace")) {
			return "mace";
		}
		if (id.endsWith("_axe")) {
			return "axe";
		}
		return "sword";
	}

	/** 武器评分：以基础攻击伤害为主，附带附魔收益。 */
	public static double scoreWeapon(ItemStack stack, Entity entity, Configs.ToolPicker config) {
		double damage = 1.0D;
		ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
		if (modifiers != null) {
			for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
				Holder<Attribute> attribute = entry.attribute();
				if (attribute != null && attribute.is(Attributes.ATTACK_DAMAGE)) {
					AttributeModifier modifier = entry.modifier();
					if (modifier != null && modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
						damage += modifier.amount();
					}
				}
			}
		}
		double score = damage * 100.0D;

		HolderLookup.RegistryLookup<Enchantment> lookup = enchantLookup();
		if (lookup != null) {
			score += level(lookup, stack, "sharpness") * 20.0D;
			score += level(lookup, stack, "smite") * (isUndead(entity) ? 25.0D : 1.0D);
			score += level(lookup, stack, "bane_of_arthropods") * (isArthropod(entity) ? 25.0D : 1.0D);
			score += level(lookup, stack, "impaling") * (isAquatic(entity) ? 25.0D : 1.0D);
			score += level(lookup, stack, "fire_aspect") * 8.0D;
			score += level(lookup, stack, "looting") * 2.0D;
		}
		score += stack.getMaxDamage() / 2000.0D;
		return score;
	}

	private static boolean isUndead(Entity entity) {
		EntityType<?> type = entity.getType();
		return type != null && BuiltInRegistries.ENTITY_TYPE.getKey(type) != null
				&& BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath().contains("skeleton");
	}

	private static boolean isArthropod(Entity entity) {
		EntityType<?> type = entity.getType();
		return type != null && BuiltInRegistries.ENTITY_TYPE.getKey(type) != null
				&& BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath().contains("spider");
	}

	private static boolean isAquatic(Entity entity) {
		return entity.isInWater() || entity.isUnderWater();
	}

	/** 根据偏好表决定使用哪种武器。 */
	public static String preferredWeapon(Configs.ToolPicker config, Entity entity) {
		Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
		if (id != null) {
			String preference = config.weaponPreference.get(id.toString());
			if (preference == null) {
				preference = config.weaponPreference.get(id.getPath());
			}
			if (preference != null) {
				return preference.toLowerCase(Locale.ROOT);
			}
		}
		return "sword";
	}

	/** 按配置生成一件带附魔的工具 / 武器。 */
	public static ItemStack generateTool(ClientLevel level, Configs.ToolPicker config, String kind, boolean weapon) {
		Map<String, Configs.ToolSpec> source = weapon ? config.weapons : config.tools;
		Configs.ToolSpec spec = source.get(kind);
		if (spec == null || !spec.enabled) {
			spec = source.get(kind);
		}
		if (spec == null) {
			return ItemStack.EMPTY;
		}
		Identifier id = Identifier.tryParse(spec.item);
		if (id == null) {
			return ItemStack.EMPTY;
		}
		Item item = BuiltInRegistries.ITEM.getValue(id);
		if (item == null) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = new ItemStack(item);

		Map<String, Integer> enchantments = new LinkedHashMap<>(spec.enchantments);
		if (!config.allowIncompatible) {
			enchantments = resolveConflicts(enchantments, config);
		}

		HolderLookup.RegistryLookup<Enchantment> lookup = enchantLookup();
		if (lookup != null && !enchantments.isEmpty()) {
			final Map<String, Integer> finalEnchantments = enchantments;
			EnchantmentHelper.updateEnchantments(stack, mutable -> {
				for (Map.Entry<String, Integer> entry : finalEnchantments.entrySet()) {
					Identifier enchantId = Identifier.tryParse(entry.getKey());
					if (enchantId == null) {
						continue;
					}
					Optional<Holder.Reference<Enchantment>> holder = lookup.get(ResourceKey.create(Registries.ENCHANTMENT, enchantId));
					if (holder.isPresent()) {
						int lvl = Math.max(1, Math.min(255, entry.getValue()));
						mutable.set(holder.get(), lvl);
					}
				}
			});
		}
		return stack;
	}

	/**
	 * 互相冲突的附魔分组，同组内只保留优先级最高的一个。
	 *
	 * <p>组内顺序即优先级（靠前的优先），保证结果稳定可预期。</p>
	 */
	private static final List<List<String>> CONFLICT_GROUPS = List.of(
			List.of("minecraft:smite", "minecraft:bane_of_arthropods", "minecraft:sharpness"),
			List.of("minecraft:silk_touch", "minecraft:fortune"),
			List.of("minecraft:infinity", "minecraft:mending"),
			List.of("minecraft:multishot", "minecraft:piercing"),
			List.of("minecraft:riptide", "minecraft:channeling", "minecraft:loyalty")
	);

	/** 剔除互相冲突的附魔。 */
	public static Map<String, Integer> resolveConflicts(Map<String, Integer> enchantments, Configs.ToolPicker config) {
		Map<String, Integer> result = new LinkedHashMap<>(enchantments);
		for (List<String> group : CONFLICT_GROUPS) {
			String winner = null;
			// 精准采集 / 时运 按配置偏好决定。
			if (group.contains("minecraft:silk_touch") && group.contains("minecraft:fortune")) {
				if (config.preferSilkTouch && result.containsKey("minecraft:silk_touch")) {
					winner = "minecraft:silk_touch";
				} else if (config.preferFortune && result.containsKey("minecraft:fortune")) {
					winner = "minecraft:fortune";
				}
			}
			if (winner == null) {
				for (String key : group) {
					if (result.containsKey(key)) {
						winner = key;
						break;
					}
				}
			}
			if (winner != null) {
				for (String key : group) {
					if (!key.equals(winner)) {
						result.remove(key);
					}
				}
			}
		}
		return result;
	}

	private static HolderLookup.RegistryLookup<Enchantment> enchantLookup() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) {
			return null;
		}
		try {
			return mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		} catch (Exception e) {
			return null;
		}
	}

	private static String itemId(Item item) {
		Identifier id = BuiltInRegistries.ITEM.getKey(item);
		return id == null ? "minecraft:air" : id.toString();
	}

	/** 供创造模式物品栏注入使用。 */
	public static Set<Item> allConfiguredItems(Configs.ToolPicker config) {
		Set<Item> items = new HashSet<>();
		collectItems(config.tools, items);
		collectItems(config.weapons, items);
		return items;
	}

	private static void collectItems(Map<String, Configs.ToolSpec> specs, Set<Item> items) {
		for (Configs.ToolSpec spec : specs.values()) {
			Identifier id = Identifier.tryParse(spec.item);
			if (id == null) {
				continue;
			}
			Item item = BuiltInRegistries.ITEM.getValue(id);
			if (item != null) {
				items.add(item);
			}
		}
	}

	/** 画作变体的标题（供配置界面显示附魔名称时复用）。 */
	public static String paintingTitle(PaintingVariant variant) {
		return variant.title().map(c -> c.getString()).orElse("");
	}
}
