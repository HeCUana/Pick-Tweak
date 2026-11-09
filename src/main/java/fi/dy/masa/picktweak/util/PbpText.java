package fi.dy.masa.picktweak.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** 聊天栏输出与剪贴板的小工具。 */
public final class PbpText {

	private PbpText() {
	}

	/** 在聊天栏打印一条本地消息。 */
	public static void chat(Component message) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.gui != null && mc.gui.hud != null) {
			mc.gui.hud.getChat().addClientSystemMessage(message);
		}
	}

	/** 复制文本到系统剪贴板。 */
	public static boolean copyToClipboard(String text) {
		if (text == null || text.isEmpty()) {
			return false;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.keyboardHandler == null) {
			return false;
		}
		mc.keyboardHandler.setClipboard(text);
		return true;
	}
}
