package fi.dy.masa.picktweak.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import fi.dy.masa.picktweak.Reference;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 自定义映射（overrides）：方块 / 实体 -> 物品。
 *
 * <p>客户端与服务端共用同一份 {@code picktweak-overrides.json}，
 * 单人游戏里两者在同一个配置目录；专用服务器上则各用各的。
 * 旧文件名 {@code pickblockpro-overrides.json} 会在首次加载时自动迁移。</p>
 */
public final class PbpOverrides {

	private static final Logger LOGGER = LogUtils.getLogger();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	private static final String FILE_NAME = "picktweak-overrides.json";
	private static final String LEGACY_FILE_NAME = "pickblockpro-overrides.json";

	private static final Map<String, String> BLOCK_OVERRIDES = new LinkedHashMap<>();
	private static final Map<String, String> ENTITY_OVERRIDES = new LinkedHashMap<>();

	private static Path path;

	private PbpOverrides() {
	}

	/** 载入（并在缺失时写出默认模板）。 */
	public static synchronized void load() {
		Path dir = FabricLoader.getInstance().getConfigDir();
		path = dir.resolve(FILE_NAME);
		Path legacy = dir.resolve(LEGACY_FILE_NAME);

		BLOCK_OVERRIDES.clear();
		ENTITY_OVERRIDES.clear();

		if (!Files.exists(path) && Files.exists(legacy)) {
			migrate(legacy);
		}

		if (!Files.exists(path)) {
			writeDefault();
			return;
		}

		try {
			JsonObject root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
			readSection(root, "blocks", BLOCK_OVERRIDES);
			readSection(root, "entities", ENTITY_OVERRIDES);
		} catch (Exception e) {
			LOGGER.warn("[{}] 读取 overrides 文件失败：{}", Reference.MOD_NAME, e.toString());
		}
	}

	/** 重新载入。 */
	public static synchronized void reload() {
		load();
	}

	/** 把旧文件名复制为新文件名（保留旧文件不删，方便回滚）。 */
	private static void migrate(Path legacy) {
		try {
			String content = Files.readString(legacy, StandardCharsets.UTF_8);
			Files.createDirectories(path.getParent());
			Files.writeString(path, content, StandardCharsets.UTF_8);
			LOGGER.info("[{}] 已从旧文件 {} 迁移到 {}",
					Reference.MOD_NAME, LEGACY_FILE_NAME, FILE_NAME);
		} catch (IOException e) {
			LOGGER.warn("[{}] 迁移旧 overrides 文件失败，将写出默认模板：{}",
					Reference.MOD_NAME, e.toString());
		}
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
			LOGGER.warn("[{}] 写入默认 overrides 文件失败：{}", Reference.MOD_NAME, e.toString());
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
