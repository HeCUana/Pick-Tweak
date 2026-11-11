package fi.dy.masa.picktweak.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;

/**
 * 把「拾取修饰键」和「聊天框槽位快捷键」对上，<b>不依赖 MaLiLib</b>。
 *
 * <p>两种运行方式：</p>
 * <ul>
 *   <li>装了 MaLiLib：走 {@link fi.dy.masa.picktweak.compat.MalilibHotkeyBridge}，用面板里
 *       配置的绑定（默认 Ctrl / Alt / Ctrl+1…0）；</li>
 *   <li>没装 MaLiLib：退回原版按键判断 —— 修饰键固定 Ctrl / Alt，聊天槽位固定 Ctrl + 1…0。</li>
 * </ul>
 *
 * <p>中键本身是原版的「拾取」按键，不在本类里判断。</p>
 */
public final class HotkeyBinds {

	/** 两种中键修饰键。 */
	public enum Modifier {
		/** Ctrl + 中键：附加 NBT，并把 NBT 复制到剪贴板。 */
		NBT,
		/** Alt + 中键：附加方块状态属性。 */
		STATE
	}

	/** 聊天框槽位键（默认 Ctrl + 1…9、Ctrl + 0）。 */
	private static final int CHAT_SLOT_COUNT = 10;

	private static Boolean malilibLoaded;

	private HotkeyBinds() {
	}

	/** 拾取用的修饰键当前是否按着。 */
	public static boolean isModifierActive(Modifier modifier) {
		if (modifier == null) {
			return false;
		}
		if (isMalilibAvailable()) {
			return fi.dy.masa.picktweak.compat.MalilibHotkeyBridge.modifierActive(modifier);
		}
		Minecraft mc = Minecraft.getInstance();
		return switch (modifier) {
			case NBT -> mc.hasControlDown();
			case STATE -> mc.hasAltDown();
		};
	}

	/**
	 * 聊天框里按下的键是否命中第 {@code slot} 个快捷栏槽位（0…8 对应 1…9，9 对应 0）。
	 *
	 * <p>{@code pressedKey} 是 GLFW 键码（{@code KeyEvent.input()}）。</p>
	 */
	public static boolean matchesChatSlot(int slot, int pressedKey) {
		if (slot < 0 || slot >= CHAT_SLOT_COUNT) {
			return false;
		}
		if (isMalilibAvailable()) {
			return fi.dy.masa.picktweak.compat.MalilibHotkeyBridge.matchesChatSlot(slot, pressedKey);
		}
		int expectedKey = slot < 9 ? InputConstants.KEY_1 + slot : InputConstants.KEY_0;
		return pressedKey == expectedKey && Minecraft.getInstance().hasControlDown();
	}

	private static boolean isMalilibAvailable() {
		if (malilibLoaded == null) {
			malilibLoaded = FabricLoader.getInstance().isModLoaded("malilib");
		}
		return malilibLoaded;
	}
}