package fi.dy.masa.picktweak.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 自定义映射（overrides）：方块 / 实体 -> 物品。
 *
 * <p>客户端与服务端共用同一份 {@code pickblockpro-overrides.json}，
 * 单人游戏里两者在同一个配置目录；专用服务器上则各用各的。</p>
 */
public final class PbpOverrides {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	private static final Map<String, String> BLOCK_OVERRIDES = new LinkedHashMap<>();
	private static final Map<String, String> ENTITY_OVERRIDES = new LinkedHashMap<>();

	private static Path path;

	private PbpOverrides() {
	}

	/** 载入（并在缺失时写出默认模板）。 */
	public static synchronized void load() {
		Path dir = FabricLoader.getInstance().getConfigDir();
		path = dir.resolve("pickblockpro-overrides.json");
		BLOCK_OVERRIDES.clear();
		ENTITY_OVERRIDES.clear();
		if (!Files.exists(path)) {
			writeDefault();
			return;
		}
		try {
			JsonObject root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
			readSection(root, "blocks", BLOCK_OVERRIDES);
			readSection(root, "entities", ENTITY_OVERRIDES);
		} catch (Exception e) {
			System.err.println("[Pick Block Pro] 读取 overrides.json 失败：" + e.getMessage());
		}
	}

	/** 重新载入。 */
	public static synchronized void reload() {
		load();
	}

	private static void readSection(JsonObject root, String name, Map<String, String> target) {
		if (!root.has(name) || !root.get(name).isJsonObject()) {
			return;
		}
		for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject(name).entrySet()) {
			if (entry.getValue().isJsonPrimitive()) {
				target.put(entry.getKey(), entry.getValue().getAsString());
			}
		}
	}

	private static void writeDefault() {
		try {
			Files.createDirectories(path.getParent());
			JsonObject root = new JsonObject();
			JsonObject blocks = new JsonObject();
			blocks.addProperty("minecraft:iron_block", "minecraft:iron_ingot");
			root.add("blocks", blocks);
			JsonObject entities = new JsonObject();
			entities.addProperty("minecraft:pig", "minecraft:porkchop");
			root.add("entities", entities);
			Files.writeString(path, GSON.toJson(root), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.err.println("[Pick Block Pro] 写入默认 overrides.json 失败：" + e.getMessage());
		}
	}

	public static String getBlockOverride(String id) {
		return BLOCK_OVERRIDES.get(id);
	}

	public static String getEntityOverride(String id) {
		return ENTITY_OVERRIDES.get(id);
	}

	public static Path path() {
		return path;
	}
}
