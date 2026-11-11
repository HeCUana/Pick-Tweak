package fi.dy.masa.picktweak;

import com.mojang.blaze3d.platform.InputConstants;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.picktweak.compat.LitematicaCompat;
import fi.dy.masa.picktweak.compat.MalilibIntegration;
import fi.dy.masa.picktweak.config.Configs;
import fi.dy.masa.picktweak.config.HotkeyBinds;
import fi.dy.masa.picktweak.config.Hotkeys;
import fi.dy.masa.picktweak.id.IdPicker;
import fi.dy.masa.picktweak.tool.ToolPicker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 客户端入口。
 *
 * <p>装了 MaLiLib 时，热键与配置面板都走 MaLiLib（masa 系列的做法）；
 * 没装 MaLiLib 时退回到原版按键，设置使用默认值。</p>
 */
public class PickBlockProClient implements ClientModInitializer {

	/** 没有 MaLiLib 时的备用按键。 */
	private static KeyMapping idKey;
	private static KeyMapping toolKey;

	@Override
	public void onInitializeClient() {
		if (MalilibIntegration.isAvailable()) {
			MalilibIntegration.init();
		} else {
			// 没有 MaLiLib：使用默认配置 + 原版按键
			Configs.refresh();
			idKey = new KeyMapping("key.picktweak.id", InputConstants.Type.KEYBOARD, InputConstants.KEY_V, category());
			toolKey = new KeyMapping("key.picktweak.tool", InputConstants.Type.KEYBOARD, InputConstants.KEY_G, category());
		}

		ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
		ClientCommandRegistrationCallback.EVENT.register(fi.dy.masa.picktweak.command.PbpCommands::register);
		registerCreativeTab();
		registerChatShortcut();
		LitematicaCompat.apply();
	}

	private static KeyMapping.Category category() {
		try {
			return KeyMapping.Category.register(Identifier.withDefaultNamespace(Reference.MOD_ID));
		} catch (Exception e) {
			return KeyMapping.Category.MISC;
		}
	}

	private void onEndTick(Minecraft mc) {
		// MaLiLib 在场时由 MaLiLib 处理热键，这里不再重复触发。
		if (idKey == null || toolKey == null) {
			return;
		}
		while (idKey.consumeClick()) {
			IdPicker.pickId();
		}
		while (toolKey.consumeClick()) {
			ToolPicker.pick();
		}
	}

	/** 向创造模式物品栏注入配置好的附魔工具 / 武器。 */
	private void registerCreativeTab() {
		Identifier tabId = Identifier.tryParse(Configs.creative.injectTab);
		if (tabId == null) {
			tabId = Identifier.withDefaultNamespace("op_blocks");
		}
		CreativeModeTabEvents.modifyOutputEvent(
						ResourceKey.create(Registries.CREATIVE_MODE_TAB, tabId))
				.register(output -> {
					Minecraft mc = Minecraft.getInstance();
					if (mc.level == null) {
						return;
					}
					if (Configs.creative.injectTools) {
						for (String kind : ToolPicker.TOOL_KINDS) {
							addItem(output, mc, kind, false);
						}
					}
					if (Configs.creative.injectWeapons) {
						for (String kind : ToolPicker.WEAPON_KINDS) {
							addItem(output, mc, kind, true);
						}
					}
				});
	}

	private void addItem(net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput output,
			Minecraft mc, String kind, boolean weapon) {
		ItemStack stack = ToolPicker.generateTool(mc.level, Configs.toolPicker, kind, weapon);
		if (!stack.isEmpty()) {
			output.accept(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
		}
	}

	/** 聊天框内的快捷键：把快捷栏物品 ID 写进输入框（默认 Ctrl + 1…0）。 */
	private void registerChatShortcut() {
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			if (screen instanceof ChatScreen) {
				ScreenKeyboardEvents.afterKeyPress(screen).register(this::onChatKeyPress);
			}
		});
	}

	private void onChatKeyPress(net.minecraft.client.gui.screens.Screen screen, KeyEvent event) {
		if (!Configs.idPicker.chatShortcut) {
			return;
		}
		int slot = chatSlotForKey(event);
		if (slot < 0) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return;
		}
		ItemStack stack = mc.player.getInventory().getItem(slot);
		String id = IdPicker.itemIdText(stack, Configs.idPicker);
		if (id.isEmpty()) {
			return;
		}
		if (screen instanceof ChatScreen chat) {
			chat.insertText(id, false);
		}
	}

	/**
	 * 聊天框里按下的键命中了哪个槽位快捷键。
	 *
	 * <p>绑定在「热键」页里逐个改（默认 Ctrl + 1…0）。</p>
	 */
	private static int chatSlotForKey(KeyEvent event) {
		List<ConfigHotkey> binds = Hotkeys.CHAT_SLOT_KEYS;
		int key = event.input();

		for (int slot = 0; slot < binds.size(); slot++) {
			if (HotkeyBinds.matchesChatBind(binds.get(slot), key)) {
				return slot;
			}
		}

		return -1;
	}
}
