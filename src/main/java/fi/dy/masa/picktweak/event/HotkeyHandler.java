package fi.dy.masa.picktweak.event;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.picktweak.config.Hotkeys;
import fi.dy.masa.picktweak.gui.GuiConfigs;
import fi.dy.masa.picktweak.id.IdPicker;
import fi.dy.masa.picktweak.tool.ToolPicker;

/**
 * MaLiLib 热键回调。
 */
public class HotkeyHandler implements IHotkeyCallback {

	@Override
	public boolean onKeyAction(KeyAction action, IKeybind key) {
		if (action != KeyAction.PRESS) {
			return false;
		}
		if (key == Hotkeys.PICK_ID.getKeybind()) {
			IdPicker.pickId();
			return true;
		}
		if (key == Hotkeys.PICK_TOOL.getKeybind()) {
			ToolPicker.pick();
			return true;
		}
		if (key == Hotkeys.OPEN_CONFIG.getKeybind()) {
			GuiBase.openGui(new GuiConfigs(null));
			return true;
		}
		return false;
	}
}
