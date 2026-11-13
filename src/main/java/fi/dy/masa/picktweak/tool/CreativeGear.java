package fi.dy.masa.picktweak.tool;

import fi.dy.masa.picktweak.config.Configs;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 创造模式物品栏注入用的满附魔装备方案表。
 *
 * <p>每种装备按「互斥附魔的每一种取舍」生成一件，并叠加该类别必选的通用附魔：</p>
 *
 * <ul>
 *   <li>武器：剑 / 斧 / 矛（三种伤害附魔三选一，剑另含刷怪场无火焰附加版本）、
 *       重锤（致密 / 破甲 / 亡灵杀手 / 节肢杀手 四选一 + 风爆）、
 *       弓（经验修补 / 无限 二选一）、弩（多重射击 / 穿透 二选一）、
 *       三叉戟（忠诚+引雷 / 激流 二选一）</li>
 *   <li>工具：镐 / 斧 / 锹 / 锄（时运 / 精准采集 二选一），剪刀、钓鱼竿、打火石</li>
 *   <li>盔甲：头盔 / 胸甲 / 护腿 / 靴子（保护四选一），靴子另有深海探索者 / 冰霜行者两种；
 *       以及盾牌、鞘翅</li>
 * </ul>
 *
 * <p>方案表是纯数据（{@link Scheme}），实际建物品时才解析物品与附魔注册表；
 * 缺失的物品 / 附魔会被静默跳过，保证跨版本不崩。</p>
 */
public final class CreativeGear {

	/** 类别：武器。 */
	public static final String CATEGORY_WEAPON = "weapon";
	/** 类别：工具。 */
	public static final String CATEGORY_TOOL = "tool";
	/** 类别：盔甲与其他。 */
	public static final String CATEGORY_ARMOR = "armor";

	/** 一条装备方案：类别 + 显示名 + 配置种类（可空）+ 回退物品 ID + 附魔表。 */
	public record Scheme(String category, String label, String kind, String fallbackItemId,
			Map<String, Integer> enchantments) {
	}

	private static final List<Scheme> SCHEMES = buildSchemes();

	private CreativeGear() {
	}

	/** 全部方案（冒烟测试 / 文档用）。 */
	public static List<Scheme> schemes() {
		return SCHEMES;
	}

	/** 武器方案（创造物品栏用）。 */
	public static List<ItemStack> weaponVariants(Level level) {
		return buildVariants(level, CATEGORY_WEAPON);
	}

	/** 工具方案（创造物品栏用）。 */
	public static List<ItemStack> toolVariants(Level level) {
		return buildVariants(level, CATEGORY_TOOL);
	}

	/** 盔甲与其他装备方案（创造物品栏用）。 */
	public static List<ItemStack> armorVariants(Level level) {
		return buildVariants(level, CATEGORY_ARMOR);
	}

	private static List<ItemStack> buildVariants(Level level, String category) {
		List<ItemStack> result = new ArrayList<>();

		for (Scheme scheme : SCHEMES) {
			if (!scheme.category().equals(category)) {
				continue;
			}

			ItemStack stack = create(level, scheme);
			if (!stack.isEmpty()) {
				result.add(stack);
			}
		}

		return result;
	}

	/** 按方案生成一件物品；物品或附魔不存在时安全跳过。 */
	private static ItemStack create(Level level, Scheme scheme) {
		if (level == null) {
			return ItemStack.EMPTY;
		}

		String itemId = resolveItemId(scheme);
		Identifier id = Identifier.tryParse(itemId);
		Item item = id == null ? null : BuiltInRegistries.ITEM.getValue(id);

		if (item == null) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = new ItemStack(item);
		HolderLookup.RegistryLookup<Enchantment> lookup = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

		EnchantmentHelper.updateEnchantments(stack, mutable -> {
			for (Map.Entry<String, Integer> entry : scheme.enchantments().entrySet()) {
				Identifier enchantId = Identifier.tryParse(entry.getKey());
				if (enchantId == null) {
					continue;
				}
				Optional<Holder.Reference<Enchantment>> holder = lookup.get(ResourceKey.create(Registries.ENCHANTMENT, enchantId));
				holder.ifPresent(holderRef -> mutable.set(holderRef, entry.getValue()));
			}
		});

		return stack;
	}

	/** 优先使用客户端预设里的物品，其次用方案里的回退物品。 */
	private static String resolveItemId(Scheme scheme) {
		String kind = scheme.kind();

		if (kind != null) {
			Configs.ToolSpec spec = scheme.category().equals(CATEGORY_WEAPON)
					? Configs.toolPicker.weapons.get(kind)
					: Configs.toolPicker.tools.get(kind);

			if (spec != null && spec.item != null && !spec.item.isBlank()) {
				Identifier id = Identifier.tryParse(spec.item);
				if (id != null && BuiltInRegistries.ITEM.getValue(id) != null) {
					return spec.item;
				}
			}
		}

		return scheme.fallbackItemId();
	}

	private static List<Scheme> buildSchemes() {
		List<Scheme> list = new ArrayList<>();

		// ==================== 武器 ====================
		String[] damageEnchants = {"minecraft:sharpness", "minecraft:smite", "minecraft:bane_of_arthropods"};
		String[] damageLabels = {"sharpness", "smite", "bane"};

		for (int i = 0; i < damageEnchants.length; i++) {
			// 剑：通用版（火焰附加 + 击退）与刷怪场版（无火焰附加）
			list.add(new Scheme(CATEGORY_WEAPON, "sword_" + damageLabels[i] + "_fire", "sword",
					"minecraft:diamond_sword",
					ench(damageEnchants[i], 5, "minecraft:looting", 3, "minecraft:unbreaking", 3,
							"minecraft:mending", 1, "minecraft:sweeping_edge", 3,
							"minecraft:fire_aspect", 2, "minecraft:knockback", 2)));
			list.add(new Scheme(CATEGORY_WEAPON, "sword_" + damageLabels[i] + "_farm", "sword",
					"minecraft:diamond_sword",
					ench(damageEnchants[i], 5, "minecraft:looting", 3, "minecraft:unbreaking", 3,
							"minecraft:mending", 1, "minecraft:sweeping_edge", 3,
							"minecraft:knockback", 2)));

			// 斧
			list.add(new Scheme(CATEGORY_WEAPON, "axe_" + damageLabels[i], "axe",
					"minecraft:diamond_axe",
					ench(damageEnchants[i], 5, "minecraft:looting", 3, "minecraft:unbreaking", 3,
							"minecraft:mending", 1, "minecraft:fire_aspect", 2, "minecraft:knockback", 2)));

			// 矛（26.3 新增武器，专属突进附魔）
			list.add(new Scheme(CATEGORY_WEAPON, "spear_" + damageLabels[i], "spear",
					"minecraft:diamond_spear",
					ench(damageEnchants[i], 5, "minecraft:looting", 3, "minecraft:unbreaking", 3,
							"minecraft:mending", 1, "minecraft:lunge", 3,
							"minecraft:fire_aspect", 2, "minecraft:knockback", 2)));
		}

		// 重锤：致密 / 破甲 / 亡灵杀手 / 节肢杀手 四选一 + 风爆
		list.add(new Scheme(CATEGORY_WEAPON, "mace_density", "mace", "minecraft:mace",
				ench("minecraft:density", 5, "minecraft:unbreaking", 3, "minecraft:mending", 1,
						"minecraft:wind_burst", 3, "minecraft:fire_aspect", 2)));
		list.add(new Scheme(CATEGORY_WEAPON, "mace_breach", "mace", "minecraft:mace",
				ench("minecraft:breach", 4, "minecraft:unbreaking", 3, "minecraft:mending", 1,
						"minecraft:wind_burst", 3, "minecraft:fire_aspect", 2)));
		list.add(new Scheme(CATEGORY_WEAPON, "mace_smite", "mace", "minecraft:mace",
				ench("minecraft:smite", 5, "minecraft:unbreaking", 3, "minecraft:mending", 1,
						"minecraft:wind_burst", 3, "minecraft:fire_aspect", 2)));
		list.add(new Scheme(CATEGORY_WEAPON, "mace_bane", "mace", "minecraft:mace",
				ench("minecraft:bane_of_arthropods", 5, "minecraft:unbreaking", 3, "minecraft:mending", 1,
						"minecraft:wind_burst", 3, "minecraft:fire_aspect", 2)));

		// 弓：经验修补 / 无限 二选一
		list.add(new Scheme(CATEGORY_WEAPON, "bow_mending", "bow", "minecraft:bow",
				ench("minecraft:power", 5, "minecraft:unbreaking", 3, "minecraft:mending", 1,
						"minecraft:flame", 1, "minecraft:punch", 2)));
		list.add(new Scheme(CATEGORY_WEAPON, "bow_infinity", "bow", "minecraft:bow",
				ench("minecraft:power", 5, "minecraft:unbreaking", 3, "minecraft:infinity", 1,
						"minecraft:flame", 1, "minecraft:punch", 2)));

		// 弩：多重射击 / 穿透 二选一
		list.add(new Scheme(CATEGORY_WEAPON, "crossbow_multishot", "crossbow", "minecraft:crossbow",
				ench("minecraft:multishot", 1, "minecraft:quick_charge", 3, "minecraft:unbreaking", 3,
						"minecraft:mending", 1)));
		list.add(new Scheme(CATEGORY_WEAPON, "crossbow_piercing", "crossbow", "minecraft:crossbow",
				ench("minecraft:piercing", 4, "minecraft:quick_charge", 3, "minecraft:unbreaking", 3,
						"minecraft:mending", 1)));

		// 三叉戟：投掷流（忠诚 + 引雷）/ 激流流
		list.add(new Scheme(CATEGORY_WEAPON, "trident_throwing", "trident", "minecraft:trident",
				ench("minecraft:loyalty", 3, "minecraft:channeling", 1, "minecraft:impaling", 5,
						"minecraft:unbreaking", 3, "minecraft:mending", 1)));
		list.add(new Scheme(CATEGORY_WEAPON, "trident_riptide", "trident", "minecraft:trident",
				ench("minecraft:riptide", 3, "minecraft:impaling", 5, "minecraft:unbreaking", 3,
						"minecraft:mending", 1)));

		// ==================== 工具 ====================
		String[] toolKinds = {"pickaxe", "axe", "shovel", "hoe"};
		String[] toolItems = {"minecraft:diamond_pickaxe", "minecraft:diamond_axe",
				"minecraft:diamond_shovel", "minecraft:diamond_hoe"};

		for (int i = 0; i < toolKinds.length; i++) {
			list.add(new Scheme(CATEGORY_TOOL, toolKinds[i] + "_fortune", toolKinds[i], toolItems[i],
					ench("minecraft:fortune", 3, "minecraft:efficiency", 5,
							"minecraft:unbreaking", 3, "minecraft:mending", 1)));
			list.add(new Scheme(CATEGORY_TOOL, toolKinds[i] + "_silk", toolKinds[i], toolItems[i],
					ench("minecraft:silk_touch", 1, "minecraft:efficiency", 5,
							"minecraft:unbreaking", 3, "minecraft:mending", 1)));
		}

		list.add(new Scheme(CATEGORY_TOOL, "shears", "shears", "minecraft:shears",
				ench("minecraft:efficiency", 5, "minecraft:unbreaking", 3, "minecraft:mending", 1)));
		list.add(new Scheme(CATEGORY_TOOL, "fishing_rod", "fishing_rod", "minecraft:fishing_rod",
				ench("minecraft:luck_of_the_sea", 3, "minecraft:lure", 3,
						"minecraft:unbreaking", 3, "minecraft:mending", 1)));
		list.add(new Scheme(CATEGORY_TOOL, "flint_and_steel", null, "minecraft:flint_and_steel",
				ench("minecraft:unbreaking", 3, "minecraft:mending", 1)));

		// ==================== 盔甲与其他 ====================
		String[] protections = {"minecraft:protection", "minecraft:fire_protection",
				"minecraft:blast_protection", "minecraft:projectile_protection"};
		String[] protectionLabels = {"protection", "fire_protection", "blast_protection", "projectile_protection"};

		for (int i = 0; i < protections.length; i++) {
			// 头盔
			list.add(new Scheme(CATEGORY_ARMOR, "helmet_" + protectionLabels[i], null,
					"minecraft:netherite_helmet",
					ench(protections[i], 4, "minecraft:unbreaking", 3, "minecraft:mending", 1,
							"minecraft:thorns", 3, "minecraft:respiration", 3,
							"minecraft:aqua_affinity", 1)));

			// 胸甲
			list.add(new Scheme(CATEGORY_ARMOR, "chestplate_" + protectionLabels[i], null,
					"minecraft:netherite_chestplate",
					ench(protections[i], 4, "minecraft:unbreaking", 3, "minecraft:mending", 1,
							"minecraft:thorns", 3)));

			// 护腿
			list.add(new Scheme(CATEGORY_ARMOR, "leggings_" + protectionLabels[i], null,
					"minecraft:netherite_leggings",
					ench(protections[i], 4, "minecraft:unbreaking", 3, "minecraft:mending", 1,
							"minecraft:thorns", 3, "minecraft:swift_sneak", 3)));

			// 靴子：深海探索者 / 冰霜行者 两种
			list.add(new Scheme(CATEGORY_ARMOR, "boots_" + protectionLabels[i] + "_depth", null,
					"minecraft:netherite_boots",
					ench(protections[i], 4, "minecraft:unbreaking", 3, "minecraft:mending", 1,
							"minecraft:thorns", 3, "minecraft:feather_falling", 4,
							"minecraft:depth_strider", 3, "minecraft:soul_speed", 3)));
			list.add(new Scheme(CATEGORY_ARMOR, "boots_" + protectionLabels[i] + "_frost", null,
					"minecraft:netherite_boots",
					ench(protections[i], 4, "minecraft:unbreaking", 3, "minecraft:mending", 1,
							"minecraft:thorns", 3, "minecraft:feather_falling", 4,
							"minecraft:frost_walker", 2, "minecraft:soul_speed", 3)));
		}

		list.add(new Scheme(CATEGORY_ARMOR, "shield", null, "minecraft:shield",
				ench("minecraft:unbreaking", 3, "minecraft:mending", 1)));
		list.add(new Scheme(CATEGORY_ARMOR, "elytra", null, "minecraft:elytra",
				ench("minecraft:unbreaking", 3, "minecraft:mending", 1)));

		return List.copyOf(list);
	}

	/** 构造附魔表：参数为 附魔ID,等级 交替出现。 */
	private static Map<String, Integer> ench(Object... pairs) {
		Map<String, Integer> map = new LinkedHashMap<>();

		for (int i = 0; i + 1 < pairs.length; i += 2) {
			map.put((String) pairs[i], (Integer) pairs[i + 1]);
		}

		return Map.copyOf(map);
	}
}
