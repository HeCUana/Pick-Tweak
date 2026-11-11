package fi.dy.masa.picktweak;

import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

/**
 * Pick Tweak 的常量。
 */
public final class Reference {

	public static final String MOD_ID = "picktweak";
	public static final String MOD_NAME = "Pick Tweak";
	public static final String MOD_ID_FULL = "picktweak";

	public static final Logger LOGGER = LogUtils.getLogger();

	/** 配置项的翻译键前缀。 */
	public static final String CONFIG_KEY_PREFIX = MOD_ID + ".config.";

	/** 模组版本（从 Fabric Loader 读，和 gradle.properties 里的 version 保持一致）。 */
	public static final String MOD_VERSION = readVersion();

	private Reference() {
	}

	private static String readVersion() {
		try {
			var container = FabricLoader.getInstance().getModContainer(MOD_ID);
			if (container.isPresent()) {
				return container.get().getMetadata().getVersion().getFriendlyString();
			}
		} catch (Throwable t) {
			// 读不到就用下面的兜底值
		}
		return "unknown";
	}
}
