package fi.dy.masa.picktweak.config;

import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.util.input.ScanCodes;

import java.util.List;

/**
 * 把 MaLiLib 热键的绑定和「当前按下了什么」对上。
 *
 * <p>MaLiLib 的热键本身只在它自己的 tick 里触发，而我们的中键拾取
 * 是在 {@code Minecraft.pickBlockOrEntity} 里处理的，两者时机不同，
 * 所以这里直接按扫描码做一次匹配。</p>
 */
public final class HotkeyBinds {

	/** 中键的扫描码（MaLiLib 里鼠标键是负数）。 */
	private static final int MOUSE_MIDDLE = ScanCodes.MOUSE_MIDDLE;

	private HotkeyBinds() {
	}

	/**
	 * 拾取的修饰键当前是否处于「按下」状态。
	 *
	 * <p>绑定里的键都要按着才算触发；中键本身不用写进绑定里
	 * （中键是原版的拾取按键，在原版控制项里改）。</p>
	 *
	 * <p>默认绑定是 {@code LEFT_CONTROL,MOUSE_MIDDLE}，也就是 Ctrl + 中键。
	 * 把绑定清空则这个功能关闭。</p>
	 */
	public static boolean isPickModifierActive(ConfigHotkey hotkey) {
		if (hotkey == null) {
			return false;
		}

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

	/**
	 * 聊天框里按下的键是否命中某个绑定。
	 *
	 * <p>事件本身的键必须在绑定里，绑定里的其它键（修饰键）必须都按着。</p>
	 */
	public static boolean matchesChatBind(ConfigHotkey hotkey, int pressedKey) {
		if (hotkey == null) {
			return false;
		}

		List<Integer> keys = hotkey.getKeybind().getKeys();

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
