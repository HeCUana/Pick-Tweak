package fi.dy.masa.picktweak.config;

import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import fi.dy.masa.picktweak.Reference;

import java.util.ArrayList;
import java.util.List;

/**
 * Pick Tweak 的全部快捷方式（MaLiLib 热键，面板里可以逐个改键）。
 *
 * <p>中键本身的按键是原版的「拾取」选项，在原版控制项里改；
 * 这里登记的是我们自己定义的那些，全部能在「热键」页里改：</p>
 *
 * <ul>
 *   <li>{@link #PICK_WITH_NBT}：中键 + 修饰键 → 附加 NBT，并把目标的 NBT 复制到剪贴板</li>
 *   <li>{@link #PICK_WITH_STATE}：中键 + 修饰键 → 附加方块状态属性</li>
 *   <li>{@link #PICK_ID} / {@link #PICK_TOOL} / {@link #OPEN_CONFIG}</li>
 *   <li>{@link #CHAT_SLOT_KEYS}：聊天框里写快捷栏物品 ID（默认 Ctrl + 1…0）</li>
 * </ul>
 *
 * <p>修饰键型热键用的是 {@link KeybindSettings#MODIFIER_INGAME}：
 * 不吞按键事件，顺序也不敏感，所以 Ctrl + Shift + 中键这种也认。</p>
 */
public final class Hotkeys {

	/** 中键 + Ctrl：附加 NBT，并把目标的 NBT 复制到剪贴板。 */
	public static final ConfigHotkey PICK_WITH_NBT = modifierHotkey(
			"hotkeyPickWithNbt", "LEFT_CONTROL,MOUSE_MIDDLE",
			"Pick the target with its full NBT (and copy that NBT to the clipboard)");

	/** 中键 + Alt：附加方块状态属性。 */
	public static final ConfigHotkey PICK_WITH_STATE = modifierHotkey(
			"hotkeyPickWithState", "LEFT_ALT,MOUSE_MIDDLE",
			"Pick the target with its block state properties");

	/** 拾取目标 ID。 */
	public static final ConfigHotkey PICK_ID = hotkey(
			"hotkeyPickId", "V", "Pick the ID of the targeted block or entity");

	/** 拾取工具 / 武器。 */
	public static final ConfigHotkey PICK_TOOL = hotkey(
			"hotkeyPickTool", "G", "Pick the best tool or weapon for the target");

	/** 打开配置界面。 */
	public static final ConfigHotkey OPEN_CONFIG = hotkey(
			"hotkeyOpenConfig", "K", "Open the Pick Tweak config GUI");

	/** 聊天框内写快捷栏物品 ID：默认 Ctrl + 1…0（最后一个是 Ctrl + 0 对应第 10 格）。 */
	public static final List<ConfigHotkey> CHAT_SLOT_KEYS = chatSlotHotkeys();

	/** 面板里显示的全部热键。 */
	public static final List<IHotkey> ALL_HOTKEYS = allHotkeys();

	private Hotkeys() {
	}

	private static ConfigHotkey hotkey(String name, String defaultKey, String comment) {
		ConfigHotkey option = new ConfigHotkey(name, defaultKey, comment);
		option.translatedName(Reference.CONFIG_KEY_PREFIX + name);
		return option;
	}

	/** 修饰键型热键：不吞事件、不较顺序。 */
	private static ConfigHotkey modifierHotkey(String name, String defaultKey, String comment) {
		ConfigHotkey option = new ConfigHotkey(name, defaultKey, KeybindSettings.MODIFIER_INGAME, comment);
		option.translatedName(Reference.CONFIG_KEY_PREFIX + name);
		return option;
	}

	private static List<ConfigHotkey> chatSlotHotkeys() {
		List<ConfigHotkey> list = new ArrayList<>();

		for (int slot = 1; slot <= 9; slot++) {
			list.add(modifierHotkey("hotkeyChatSlot" + slot, "LEFT_CONTROL," + slot,
					"Write the ID of the hotbar slot " + slot + " into the chat box"));
		}

		list.add(modifierHotkey("hotkeyChatSlot0", "LEFT_CONTROL,0",
				"Write the ID of the hotbar slot 10 into the chat box"));

		return List.copyOf(list);
	}

	private static List<IHotkey> allHotkeys() {
		List<IHotkey> list = new ArrayList<>();
		list.add(PICK_WITH_NBT);
		list.add(PICK_WITH_STATE);
		list.add(PICK_ID);
		list.add(PICK_TOOL);
		list.add(OPEN_CONFIG);
		list.addAll(CHAT_SLOT_KEYS);
		return List.copyOf(list);
	}
}
