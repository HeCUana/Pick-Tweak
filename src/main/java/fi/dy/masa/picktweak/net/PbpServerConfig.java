package fi.dy.masa.picktweak.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 服务端配置（{@code config/pickblockpro-server.json}）。
 *
 * <p>服务端是拾取的权威方：它决定最大拾取距离、是否允许附带 NBT / 状态属性，
 * 以及发放工具时使用哪种物品。</p>
 */
public final class PbpServerConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	/** 最大拾取距离（方块）。 */
	public double maxPickDistance = 100.0D;
	/** 最大实体拾取距离。 */
	public double maxEntityPickDistance = 100.0D;
	/** 是否允许附带方块实体 / 实体 NBT。 */
	public boolean allowIncludeData = true;
	/** 是否允许附带方块状态属性。 */
	public boolean allowStateProperties = true;
	/** 是否允许客户端请求发放工具 / 武器。 */
	public boolean allowClientGive = true;
	/**
	 * 生存模式是否也真正发放物品。
	 *
	 * <p>原版拾取在生存模式只会在背包里已有同类物品时切换过去，不会给新物品。
	 * PBP 的服务端拾取默认会真正发放（含 NBT / 状态属性的物品与背包里的不同，
	 * 不发放就等于功能没生效），可按需关闭。</p>
	 */
	public boolean giveInSurvival = true;
	/** 拾取生物时优先给出头颅（僵尸 -> 僵尸头）而不是刷怪蛋。 */
	public boolean preferEntityHeads = true;
	/** 拾取流体（和粉雪）时发放装满的桶。 */
	public boolean pickFluids = true;
	/** 拾取太阳时发放的物品（默认是「光源方块」）。 */
	public String sunItem = "minecraft:light";
	/** 拾取月亮时发放的物品。 */
	public String moonItem = "minecraft:moonstone";
	/** 允许的最大附魔等级。 */
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

	public static void load() {
		Path dir = FabricLoader.getInstance().getConfigDir();
		path = dir.resolve("pickblockpro-server.json");
		if (Files.exists(path)) {
			try {
				PbpServerConfig loaded = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), PbpServerConfig.class);
				if (loaded != null) {
					instance = loaded;
				}
			} catch (Exception e) {
				System.err.println("[Pick Block Pro] 读取服务端配置失败，使用默认配置：" + e.getMessage());
			}
		} else {
			save();
		}
	}

	public static void save() {
		if (path == null) {
			return;
		}
		try {
			Files.createDirectories(path.getParent());
			Files.writeString(path, GSON.toJson(instance), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.err.println("[Pick Block Pro] 保存服务端配置失败：" + e.getMessage());
		}
	}

	public static Path path() {
		return path;
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
