package fi.dy.masa.picktweak.compat;

import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.util.input.ScanCodes;
import fi.dy.masa.picktweak.config.HotkeyBinds;
import fi.dy.masa.picktweak.config.Hotkeys;

import java.util.List;

/**
 * MaLiLib 热键桥。
 *
 * <p><b>只有 MaLiLib 存在时才会被加载</b>（入口是 {@link HotkeyBinds}，它会先判断模组是否加载），
 * 这样没装 MaLiLib 的客户端/服务端不会碰到 MaLiLib 的类。</p>
 *
 * <p>MaLiLib 的热键只在它自己的 tick 里触发，而中键拾取是在
 * {@code Minecraft.pickBlockOrEntity} 里处理的，两者时机不同，
 * 所以这里直接按扫描码做一次匹配。</p>
 */
public final class MalilibHotkeyBridge {

	/** 中键的扫描码（MaLiLib 里鼠标键是负数）。 */
	private static final int MOUSE_MIDDLE = ScanCodes.MOUSE_MIDDLE;

	private MalilibHotkeyBridge() {
	}

	/** 拾取修饰键当前是否处于「按下」状态。 */
	public static boolean modifierActive(HotkeyBinds.Modifier modifier) {
		ConfigHotkey hotkey = switch (modifier) {
			case NBT -> Hotkeys.PICK_WITH_NBT;
			case STATE -> Hotkeys.PICK_WITH_STATE;
		};
		List<Integer> keys = hotkey.getKeybind().getKeys();

		if (keys.isEmpty()) {
			return false;
		}

		for (int key : keys) {
			if (key == MOUSE_MIDDLE) {
				// 鼠标键不查按下状态：拾取是在 tick 里处理的，那时按键可能已经松开
				continue;
			}
			if (!isDown(key)) {
				return false;
			}
		}

		return true;
	}

	/** 聊天框里按下的键是否命中某个槽位绑定。 */
	public static boolean matchesChatSlot(int slot, int pressedKey) {
		List<ConfigHotkey> binds = Hotkeys.CHAT_SLOT_KEYS;

		if (slot < 0 || slot >= binds.size()) {
			return false;
		}

		List<Integer> keys = binds.get(slot).getKeybind().getKeys();

		if (keys.isEmpty() || !keys.contains(pressedKey)) {
			return false;
		}

		for (int key : keys) {
			if (key == pressedKey) {
				continue;
			}
			if (!isDown(key)) {
				return false;
			}
		}

		return true;
	}

	/** 某个扫描码当前是否按着。 */
	private static boolean isDown(int scanCode) {
		try {
			return KeybindMulti.isKeyDown(scanCode);
		} catch (Throwable t) {
			return false;
		}
	}
}
