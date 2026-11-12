package fi.dy.masa.picktweak.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import fi.dy.masa.picktweak.Reference;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 服务端配置（{@code config/picktweak-server.json}）。
 *
 * <p>服务端是拾取的权威方：它决定最大拾取距离、是否允许附带 NBT / 状态属性、
 * 权限要求、限流速率，以及发放工具时使用哪种物品。</p>
 *
 * <p>旧版本的 {@code pickblockpro-server.json} 在首次加载时会自动迁移读取，
 * 并写出新的 {@code picktweak-server.json}。Gson 缺失的字段保留 Java 默认值，
 * 已有的非法值会在 {@link #normalize()} 里被夹取 / 回落并记录日志。</p>
 */
public final class PbpServerConfig {

	private static final Logger LOGGER = LogUtils.getLogger();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	private static final String FILE_NAME = "picktweak-server.json";
	private static final String LEGACY_FILE_NAME = "pickblockpro-server.json";

	private static final double MIN_DISTANCE = 1.0D;
	private static final double MAX_DISTANCE = 512.0D;
	private static final int MIN_RATE_LIMIT = 1;
	private static final int MAX_RATE_LIMIT = 200;
	private static final int MIN_ENCHANT_LEVEL = 1;
	private static final int MAX_ENCHANT_LEVEL = 255;

	/** 拾取太阳的默认物品。 */
	public static final String DEFAULT_SUN_ITEM = "minecraft:light";
	/** 拾取月亮的默认物品（太阳 / 月亮都发放光源方块；26.3 没有 moonstone）。 */
	public static final String DEFAULT_MOON_ITEM = "minecraft:light";

	/** 最大拾取距离（方块），加载时夹取到 {@value #MIN_DISTANCE}..{@value #MAX_DISTANCE}。 */
	public double maxPickDistance = 100.0D;
	/** 最大实体拾取距离，加载时夹取到 {@value #MIN_DISTANCE}..{@value #MAX_DISTANCE}。 */
	public double maxEntityPickDistance = 100.0D;
	/** 是否允许附带方块实体 / 实体 NBT。 */
	public boolean allowIncludeData = true;
	/** 是否允许附带方块状态属性。 */
	public boolean allowStateProperties = true;
	/** 是否允许客户端请求发放工具 / 武器（默认关闭）。 */
	public boolean allowClientGive = false;
	/**
	 * 生存模式是否也真正发放物品（默认关闭）。
	 *
	 * <p>关闭时生存玩家只能切换到背包里已有的同类物品；创造模式不受此限制。</p>
	 */
	public boolean giveInSurvival = false;
	/** 发放工具 / 武器是否要求 OP（权限等级 >= 2，默认要求）。 */
	public boolean requireOpForGive = true;
	/** 客户端重载配置是否要求 OP（权限等级 >= 2，默认要求）。 */
	public boolean requireOpForReload = true;
	/** 是否允许拾取太阳 / 月亮。 */
	public boolean allowCelestial = true;
	/** 每玩家令牌桶速率（次 / 秒），加载时夹取到 {@value #MIN_RATE_LIMIT}..{@value #MAX_RATE_LIMIT}。 */
	public int rateLimitPerSecond = 20;
	/** 拾取生物时优先给出头颅（僵尸 -> 僵尸头）而不是刷怪蛋。 */
	public boolean preferEntityHeads = true;
	/** 拾取流体（和粉雪）时发放装满的桶（默认关闭）。 */
	public boolean pickFluids = false;
	/** 拾取太阳时发放的物品。 */
	public String sunItem = DEFAULT_SUN_ITEM;
	/** 拾取月亮时发放的物品。 */
	public String moonItem = DEFAULT_MOON_ITEM;
	/** 允许的最大附魔等级，加载时夹取到 {@value #MIN_ENCHANT_LEVEL}..{@value #MAX_ENCHANT_LEVEL}。 */
	public int maxEnchantLevel = 255;
	/** 各类工具使用的物品。 */
	public Map<String, String> toolItems = defaultToolItems();
	/** 各类武器使用的物品。 */
	public Map<String, String> weaponItems = defaultWeaponItems();

	private static PbpServerConfig instance = new PbpServerConfig();
	private static Path path;

	public static PbpServerConfig get() {
		return instance;
	}

	/** 载入配置；新文件不存在而旧文件存在时先迁移读取，最后按需写回。 */
	public static synchronized void load() {
		Path dir = FabricLoader.getInstance().getConfigDir();
		path = dir.resolve(FILE_NAME);
		Path legacy = dir.resolve(LEGACY_FILE_NAME);
		boolean migrated = false;
		Path readFrom = path;

		if (!Files.exists(path) && Files.exists(legacy)) {
			readFrom = legacy;
			migrated = true;
		}

		PbpServerConfig loaded = null;

		if (Files.exists(readFrom)) {
			try {
				loaded = GSON.fromJson(Files.readString(readFrom, StandardCharsets.UTF_8), PbpServerConfig.class);
			} catch (Exception e) {
				LOGGER.warn("[{}] 读取服务端配置失败，使用默认配置：{}", Reference.MOD_NAME, e.toString());
			}
		}

		instance = loaded != null ? loaded : new PbpServerConfig();
		boolean changed = instance.normalize();

		if (migrated) {
			LOGGER.info("[{}] 已从旧配置文件 {} 迁移到 {}",
					Reference.MOD_NAME, LEGACY_FILE_NAME, FILE_NAME);
		}

		if (!Files.exists(path) || migrated || changed) {
			save();
		}
	}

	/** 重新载入（供 reload 命令 / 网络包使用）。 */
	public static void reload() {
		load();
	}

	public static void save() {
		if (path == null) {
			return;
		}

		try {
			Files.createDirectories(path.getParent());
			Files.writeString(path, GSON.toJson(instance), StandardCharsets.UTF_8);
		} catch (IOException e) {
			LOGGER.warn("[{}] 保存服务端配置失败：{}", Reference.MOD_NAME, e.toString());
		}
	}

	public static Path path() {
		return path;
	}

	/**
	 * 夹取 / 校验所有配置值。
	 *
	 * @return 是否有值被修改（用于决定是否写回文件）
	 */
	private boolean normalize() {
		boolean changed = false;

		double blockDistance = normalizeDistance(maxPickDistance, "maxPickDistance");
		if (Double.compare(blockDistance, maxPickDistance) != 0) {
			maxPickDistance = blockDistance;
			changed = true;
		}

		double entityDistance = normalizeDistance(maxEntityPickDistance, "maxEntityPickDistance");
		if (Double.compare(entityDistance, maxEntityPickDistance) != 0) {
			maxEntityPickDistance = entityDistance;
			changed = true;
		}

		int rate = Math.max(MIN_RATE_LIMIT, Math.min(MAX_RATE_LIMIT, rateLimitPerSecond));
		if (rate != rateLimitPerSecond) {
			LOGGER.warn("[{}] 服务端配置 rateLimitPerSecond={} 超出范围，夹取为 {}",
					Reference.MOD_NAME, rateLimitPerSecond, rate);
			rateLimitPerSecond = rate;
			changed = true;
		}

		int enchantLevel = Math.max(MIN_ENCHANT_LEVEL, Math.min(MAX_ENCHANT_LEVEL, maxEnchantLevel));
		if (enchantLevel != maxEnchantLevel) {
			LOGGER.warn("[{}] 服务端配置 maxEnchantLevel={} 超出范围，夹取为 {}",
					Reference.MOD_NAME, maxEnchantLevel, enchantLevel);
			maxEnchantLevel = enchantLevel;
			changed = true;
		}

		String sun = normalizeItemId(sunItem, DEFAULT_SUN_ITEM, "sunItem");
		if (!sun.equals(sunItem)) {
			sunItem = sun;
			changed = true;
		}

		String moon = normalizeItemId(moonItem, DEFAULT_MOON_ITEM, "moonItem");
		if (!moon.equals(moonItem)) {
			moonItem = moon;
			changed = true;
		}

		Map<String, String> tools = normalizeItemMap(toolItems, defaultToolItems(), "toolItems");
		if (!tools.equals(toolItems)) {
			toolItems = tools;
			changed = true;
		}

		Map<String, String> weapons = normalizeItemMap(weaponItems, defaultWeaponItems(), "weaponItems");
		if (!weapons.equals(weaponItems)) {
			weaponItems = weapons;
			changed = true;
		}

		return changed;
	}

	private static double normalizeDistance(double value, String field) {
		double result = Double.isFinite(value)
				? Math.max(MIN_DISTANCE, Math.min(MAX_DISTANCE, value))
				: MIN_DISTANCE;

		if (Double.compare(result, value) != 0) {
			LOGGER.warn("[{}] 服务端配置 {}={} 超出范围，夹取为 {}",
					Reference.MOD_NAME, field, value, result);
		}

		return result;
	}

	private static String normalizeItemId(String id, String fallback, String field) {
		String trimmed = id == null ? null : id.trim();

		if (trimmed != null && isValidItemId(trimmed)) {
			return trimmed;
		}

		LOGGER.warn("[{}] 服务端配置 {} 的物品 ID '{}' 无效，已回落到 {}",
				Reference.MOD_NAME, field, id, fallback);
		return fallback;
	}

	/** 保底：默认表里的每个 key 都有合法物品；额外 key 只在合法时保留。 */
	private static Map<String, String> normalizeItemMap(Map<String, String> map, Map<String, String> defaults, String field) {
		Map<String, String> result = new LinkedHashMap<>();

		for (Map.Entry<String, String> entry : defaults.entrySet()) {
			String value = map == null ? null : map.get(entry.getKey());
			result.put(entry.getKey(), normalizeItemId(value, entry.getValue(), field + "." + entry.getKey()));
		}

		if (map != null) {
			for (Map.Entry<String, String> entry : map.entrySet()) {
				if (result.containsKey(entry.getKey())) {
					continue;
				}

				String value = entry.getValue() == null ? null : entry.getValue().trim();

				if (value != null && isValidItemId(value)) {
					result.put(entry.getKey(), value);
				} else {
					LOGGER.warn("[{}] 服务端配置 {}.{} 的物品 ID '{}' 无效，已移除",
							Reference.MOD_NAME, field, entry.getKey(), entry.getValue());
				}
			}
		}

		return result;
	}

	private static boolean isValidItemId(String id) {
		Identifier key = Identifier.tryParse(id);

		if (key == null) {
			return false;
		}

		Item item = BuiltInRegistries.ITEM.getValue(key);
		return item != null && item != Items.AIR;
	}

	private static Map<String, String> defaultToolItems() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put("pickaxe", "minecraft:diamond_pickaxe");
		map.put("axe", "minecraft:diamond_axe");
		map.put("shovel", "minecraft:diamond_shovel");
		map.put("hoe", "minecraft:diamond_hoe");
		map.put("sword", "minecraft:diamond_sword");
		map.put("shears", "minecraft:shears");
		map.put("bucket", "minecraft:bucket");
		map.put("fishing_rod", "minecraft:fishing_rod");
		map.put("brush", "minecraft:brush");
		return map;
	}

	private static Map<String, String> defaultWeaponItems() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put("sword", "minecraft:diamond_sword");
		map.put("axe", "minecraft:diamond_axe");
		map.put("bow", "minecraft:bow");
		map.put("crossbow", "minecraft:crossbow");
		map.put("trident", "minecraft:trident");
		map.put("mace", "minecraft:mace");
		return map;
	}
}
