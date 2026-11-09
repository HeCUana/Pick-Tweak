package fi.dy.masa.picktweak.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.picktweak.Reference;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Pick Tweak 的配置读写（MaLiLib 风格）。
 *
 * <p>MaLiLib 负责在面板里编辑这些选项，这里只负责把值存到
 * {@code config/picktweak.json} 并在改动后刷新运行时快照。</p>
 */
public class ConfigHandler implements IConfigHandler {

	public static final String CONFIG_FILE_NAME = "picktweak.json";

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	@Override
	public void load() {
		Path path = configPath();
		if (Files.exists(path)) {
			try {
				String json = Files.readString(path, StandardCharsets.UTF_8);
				JsonObject root = JsonParser.parseString(json).getAsJsonObject();
				ConfigUtils.readConfigBase(root, Reference.MOD_ID, Configs.ALL_CONFIGS);
				ConfigUtils.readHotkeys(root, Reference.MOD_ID, Hotkeys.ALL_HOTKEYS);
			} catch (Exception e) {
				System.err.println("[Pick Tweak] 读取配置失败，使用默认值：" + e.getMessage());
			}
		}
		Configs.refresh();
	}

	@Override
	public void save() {
		Path path = configPath();
		try {
			Files.createDirectories(path.getParent());
			JsonObject root = new JsonObject();
			ConfigUtils.writeConfigBase(root, Reference.MOD_ID, Configs.ALL_CONFIGS);
			ConfigUtils.writeHotkeys(root, Reference.MOD_ID, Hotkeys.ALL_HOTKEYS);
			Files.writeString(path, GSON.toJson(root), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.err.println("[Pick Tweak] 保存配置失败：" + e.getMessage());
		}
		Configs.refresh();
	}

	@Override
	public void onConfigsChanged() {
		Configs.refresh();
	}

	private static Path configPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
	}
}
