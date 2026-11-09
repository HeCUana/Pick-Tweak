package fi.dy.masa.picktweak.config;

import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.picktweak.Reference;

import java.util.List;

/**
 * Pick Tweak 的热键（MaLiLib 热键，面板里可直接改键）。
 */
public final class Hotkeys {

	/** 拾取目标 ID。 */
	public static final ConfigHotkey PICK_ID = hotkey("hotkeyPickId", "V", "Pick the ID of the targeted block or entity");
	/** 拾取工具 / 武器。 */
	public static final ConfigHotkey PICK_TOOL = hotkey("hotkeyPickTool", "G", "Pick the best tool or weapon for the target");
	/** 打开配置界面。 */
	public static final ConfigHotkey OPEN_CONFIG = hotkey("hotkeyOpenConfig", "K", "Open the Pick Tweak config GUI");

	public static final List<IHotkey> ALL_HOTKEYS = List.of(PICK_ID, PICK_TOOL, OPEN_CONFIG);

	private Hotkeys() {
	}

	private static ConfigHotkey hotkey(String name, String defaultKey, String comment) {
		ConfigHotkey option = new ConfigHotkey(name, defaultKey, comment);
		option.translatedName(Reference.CONFIG_KEY_PREFIX + name);
		return option;
	}
}
