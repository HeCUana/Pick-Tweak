package fi.dy.masa.picktweak.compat;

import com.mojang.logging.LogUtils;
import fi.dy.masa.picktweak.config.Configs;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Axiom 适配。
 *
 * <p>Axiom 也往 {@code Minecraft.pickBlockOrEntity} 里注入自己的逻辑，一共三处：</p>
 * <ul>
 *   <li>{@code MixinMinecraftPickBlock}：编辑器模式下中键是「拾取方块」，
 *       builder 工具槽激活时中键也给 builder 工具用</li>
 *   <li>{@code MixinMinecraft.pickBlockInfinite}：{@code INFINITE_REACH} 能力，
 *       在原版打空时用射线做超远拾取</li>
 * </ul>
 *
 * <p>我们的 mixin 优先级比 Axiom 高，所以默认由我们先处理；
 * 只有在「Axiom 明确要接管这次中键」的时候才让位。
 * 判断用的都是 Axiom 的只读查询方法，反射取不到时退回到「不让位」，
 * 也就是维持本模组原本的行为，不会因为 Axiom 改版而崩。</p>
 */
public final class AxiomCompat {

	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String MOD_ID = "axiom";

	private static Boolean loaded;
	private static Boolean reflectionAvailable;

	private static Method editorUiIsEnabled;
	private static Method builderToolSlotActive;
	private static Object infiniteReachCapability;
	private static Method capabilityIsEnabled;

	private AxiomCompat() {
	}

	/** Axiom 是否装了（第一次调用时会把反射接口初始化好）。 */
	public static boolean isLoaded() {
		if (loaded == null) {
			loaded = FabricLoader.getInstance().isModLoaded(MOD_ID);

			if (loaded) {
				initReflection();
			}
		}
		return loaded;
	}

	/**
	 * 这次中键该不该让给 Axiom。
	 *
	 * @param vanillaHitMissed 原版这次有没有打空（Axiom 的无限距离只在打空时才会接管）
	 */
	public static boolean shouldYieldPickBlock(boolean vanillaHitMissed) {
		if (!isLoaded()) {
			return false;
		}

		// 编辑器模式：那里的中键就是「拾取方块」，必须让给它
		if (isEditorMode()) {
			return true;
		}

		if (Configs.blockPicker.axiomMode == Configs.AxiomMode.OVERRIDE) {
			// 覆盖模式：除了编辑器模式，一律我们自己接管
			return false;
		}

		// builder 工具槽激活时，中键归 builder 工具
		if (isBuilderToolActive()) {
			return true;
		}

		// Axiom 的无限距离：只有在原版打空（也就是够不着）时它才会接管
		return vanillaHitMissed && isInfiniteReachEnabled();
	}

	/** Axiom 的编辑器界面是否开着。 */
	public static boolean isEditorMode() {
		return callBoolean(editorUiIsEnabled);
	}

	/** builder 工具槽是否可用。 */
	public static boolean isBuilderToolActive() {
		return callBoolean(builderToolSlotActive);
	}

	/** Axiom 的无限距离能力是否开着。 */
	public static boolean isInfiniteReachEnabled() {
		if (infiniteReachCapability == null || capabilityIsEnabled == null) {
			return false;
		}

		try {
			return Boolean.TRUE.equals(capabilityIsEnabled.invoke(infiniteReachCapability));
		} catch (Throwable t) {
			return false;
		}
	}

	/** 反射是否可用（不可用时一律不让位）。 */
	public static boolean isReflectionAvailable() {
		if (reflectionAvailable == null) {
			initReflection();
		}
		return reflectionAvailable;
	}

	private static boolean callBoolean(Method method) {
		if (method == null) {
			return false;
		}

		try {
			return Boolean.TRUE.equals(method.invoke(null));
		} catch (Throwable t) {
			return false;
		}
	}

	/** 反射初始化（只做一次）。取不到就把「反射可用」标记为 false。 */
	private static synchronized void initReflection() {
		if (reflectionAvailable != null) {
			return;
		}

		reflectionAvailable = Boolean.FALSE;

		try {
			Class<?> editorUi = Class.forName("com.moulberry.axiom.editor.EditorUI");
			editorUiIsEnabled = editorUi.getMethod("isEnabled");

			Class<?> toolManager = Class.forName("com.moulberry.axiom.buildertools.BuilderToolManager");
			builderToolSlotActive = toolManager.getMethod("isToolSlotActive");

			Class<?> capability = Class.forName("com.moulberry.axiom.capabilities.Capability");
			Field field = capability.getField("INFINITE_REACH");
			infiniteReachCapability = field.get(null);
			capabilityIsEnabled = capability.getMethod("isEnabled");

			reflectionAvailable = Boolean.TRUE;
		} catch (Throwable t) {
			LOGGER.debug("[Pick Tweak] 取不到 Axiom 的接口，按原样处理中键拾取", t);
		}
	}
}
