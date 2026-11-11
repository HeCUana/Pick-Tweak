package fi.dy.masa.picktweak.compat;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import fi.dy.masa.picktweak.Reference;
import fi.dy.masa.picktweak.config.ConfigHandler;
import fi.dy.masa.picktweak.config.Hotkeys;
import fi.dy.masa.picktweak.event.HotkeyHandler;
import fi.dy.masa.picktweak.gui.GuiConfigs;
import net.fabricmc.loader.api.FabricLoader;

/**
 * MaLiLib 集成：配置面板、热键、配置界面注册。
 *
 * <p>MaLiLib 本身带有 ModMenu 入口，所以这里**不需要**再写一份 ModMenu 集成：
 * 只要把配置界面注册到 {@code Registry.CONFIG_SCREEN}，MaLiLib 会负责在 Mod Menu 里显示按钮。</p>
 */
public final class MalilibIntegration {

	private MalilibIntegration() {
	}

	/** MaLiLib 是否可用（服务端不需要它）。 */
	public static boolean isAvailable() {
		return FabricLoader.getInstance().isModLoaded("malilib");
	}

	/** 初始化 MaLiLib 集成（仅在 MaLiLib 存在时调用）。 */
	public static void init() {
		// 配置处理器：MaLiLib 负责面板里的编辑与保存
		ConfigHandler handler = new ConfigHandler();
		ConfigManager.getInstance().registerConfigHandler(Reference.MOD_ID, handler);
		handler.load();

		// 热键
		fi.dy.masa.malilib.event.InputEventHandler.getKeybindManager().registerKeybindProvider(new HotkeyProvider());
		IHotkeyCallback callback = new HotkeyHandler();
		Hotkeys.PICK_ID.getKeybind().setCallback(callback);
		Hotkeys.PICK_TOOL.getKeybind().setCallback(callback);
		Hotkeys.OPEN_CONFIG.getKeybind().setCallback(callback);

		// 配置界面：注册后 MaLiLib 会把它挂到自己的 Mod Menu 入口上
		Registry.CONFIG_SCREEN.registerConfigScreenFactory(new ModInfo(
				Reference.MOD_ID, Reference.MOD_NAME, () -> new GuiConfigs(null)));
	}

	/** 把 Pick Tweak 的热键登记到 MaLiLib 的按键系统。 */
	private static class HotkeyProvider implements IKeybindProvider {
		@Override
		public void addKeysToMap(IKeybindManager manager) {
			for (IHotkey hotkey : Hotkeys.ALL_HOTKEYS) {
				manager.addKeybindToMap(hotkey.getKeybind());
			}
		}

		@Override
		public void addHotkeys(IKeybindManager manager) {
			manager.addHotkeysForCategory(Reference.MOD_NAME, Reference.MOD_NAME, Hotkeys.ALL_HOTKEYS);
		}
	}
}
