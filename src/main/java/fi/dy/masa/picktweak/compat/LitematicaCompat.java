package fi.dy.masa.picktweak.compat;

import fi.dy.masa.picktweak.config.Configs;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Litematica 兼容：把 Litematica 自带的 Pick Block 快捷键解绑，
 * 让 PBP 统一接管拾取行为。
 *
 * <p>全部通过反射完成，Litematica / MaLiLib 没有安装时不会有任何影响。</p>
 */
public final class LitematicaCompat {

	private static final String LITEMATICA_HOTKEYS = "fi.dy.masa.litematica.config.Hotkeys";
	private static final String[] PICK_BLOCK_HOTKEYS = {"PICK_BLOCK_FIRST", "PICK_BLOCK_LAST", "PICK_BLOCK_TOGGLE"};

	private LitematicaCompat() {
	}

	/** 如果配置要求接管，就解绑 Litematica 的拾取快捷键。 */
	public static void apply() {
		if (!Configs.blockPicker.takeOverLitematica) {
			return;
		}
		try {
			Class<?> hotkeys = Class.forName(LITEMATICA_HOTKEYS);
			for (String name : PICK_BLOCK_HOTKEYS) {
				Field field;
				try {
					field = hotkeys.getField(name);
				} catch (NoSuchFieldException e) {
					continue;
				}
				Object hotkey = field.get(null);
				if (hotkey == null) {
					continue;
				}
				Method getKeybind = findGetKeybind(hotkey.getClass());
				if (getKeybind == null) {
					continue;
				}
				Object keyMapping = getKeybind.invoke(hotkey);
				if (keyMapping instanceof KeyMapping mapping) {
					mapping.setKey(InputConstants.UNKNOWN);
				}
			}
		} catch (Throwable ignored) {
			// Litematica 没装或 API 变了：保持原样即可
		}
	}

	private static Method findGetKeybind(Class<?> clazz) {
		for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {
			for (Method method : current.getDeclaredMethods()) {
				if (method.getName().equals("getKeybind") && method.getParameterCount() == 0) {
					method.setAccessible(true);
					return method;
				}
			}
		}
		return null;
	}
}
