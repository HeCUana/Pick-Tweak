package fi.dy.masa.picktweak.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import fi.dy.masa.picktweak.gui.GuiConfigs;

/**
 * Mod Menu 入口：在 Mod Menu 的 Pick Tweak 条目上直接显示「配置」按钮。
 *
 * <p>Mod Menu 是可选依赖（{@code compileOnly}）：没装 Mod Menu 时 Fabric 不会加载这个入口类，
 * 所以不会因为找不到类而崩溃。</p>
 */
public class ModMenuImpl implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return GuiConfigs::new;
	}
}
