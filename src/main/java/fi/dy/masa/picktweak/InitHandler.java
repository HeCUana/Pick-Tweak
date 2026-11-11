package fi.dy.masa.picktweak;

import fi.dy.masa.picktweak.net.PbpServerNetworking;

/**
 * 公共初始化：注册数据包、载入服务端配置与自定义映射。
 *
 * <p><b>这里刻意不实现 MaLiLib 的 {@code IInitializationHandler}、也不 import 任何 MaLiLib 类</b>：
 * 服务端（以及没装 MaLiLib 的客户端）必须能正常加载本模组。</p>
 */
public final class InitHandler {

	private static boolean initialized;

	private InitHandler() {
	}

	/** 供入口类调用；重复调用是安全的。 */
	public static void init() {
		if (initialized) {
			return;
		}
		initialized = true;

		// register() 内部会先载入服务端配置与自定义映射（幂等）
		PbpServerNetworking.register();
	}
}