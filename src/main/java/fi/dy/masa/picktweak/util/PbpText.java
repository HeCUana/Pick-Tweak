package fi.dy.masa.picktweak.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** 聊天栏输出与剪贴板的小工具。 */
public final class PbpText {

	private PbpText() {
	}

	/** 在聊天栏打印一条本地消息。 */
	public static void chat(Component message) {
		try {
			Minecraft mc = Minecraft.getInstance();
			if (mc.gui != null && mc.gui.hud != null) {
				mc.gui.hud.getChat().addClientSystemMessage(message);
			}
		} catch (Throwable t) {
			// 聊天栏还没准备好的时候忽略即可
		}
	}

	/** 复制文本到系统剪贴板。 */
	public static boolean copyToClipboard(String text) {
		if (text == null || text.isEmpty()) {
			return false;
		}
		try {
			Minecraft mc = Minecraft.getInstance();
			if (mc.keyboardHandler == null) {
				return false;
			}
			mc.keyboardHandler.setClipboard(text);
			return true;
		} catch (Throwable t) {
			// 某些平台（Wayland、无头环境）拿不到剪贴板，不该因此影响拾取
			return false;
		}
	}
}
