package fi.dy.masa.picktweak;

import net.fabricmc.api.ModInitializer;

/**
 * Pick Tweak —— masa 系列风格的客户端拾取增强。
 *
 * <p>客户端部分使用 MaLiLib（面板 / 热键 / 配置），
 * 服务端部分提供权威拾取服务（远距离、NBT、状态属性）。</p>
 */
public class PickTweak implements ModInitializer {

	@Override
	public void onInitialize() {
		InitHandler.init();
	}
}
