package fi.dy.masa.picktweak.mixin;

import fi.dy.masa.picktweak.compat.AxiomCompat;
import fi.dy.masa.picktweak.pick.ClientPicker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 接管原版的中键拾取。
 *
 * <p>26.3 的原版拾取受交互距离限制（超出即 MISS，中键没有任何反应），
 * 这里改为按 PBP 配置的距离自己射线，并把请求交给服务端权威处理；
 * 服务端没装 PBP 时不接管，行为与原版完全一致。</p>
 *
 * <p>Axiom 也往 {@code pickBlockOrEntity} 里注入自己的逻辑（编辑器拾取、
 * builder 工具、{@code INFINITE_REACH} 能力）。这里不跟它抢同一个注入点，
 * 而是由 {@link AxiomCompat} 判断"Axiom 明确要接管这次中键"时才让位，
 * 这样无论两个 mixin 谁先执行，行为都一致。</p>
 */
@Mixin(value = Minecraft.class, priority = 2000)
public abstract class MinecraftMixin {

	@Inject(method = "pickBlockOrEntity", at = @At("HEAD"), cancellable = true)
	private void pickBlockPro$pick(CallbackInfo ci) {
		Minecraft mc = (Minecraft) (Object) this;

		try {
			// Axiom 明确要接管这次中键（编辑器 / builder 工具 / 无限距离）时让位。
			HitResult vanillaHit = mc.hitResult;
			boolean vanillaMissed = vanillaHit == null || vanillaHit.getType() == HitResult.Type.MISS;

			if (AxiomCompat.shouldYieldPickBlock(vanillaMissed)) {
				return;
			}

			if (ClientPicker.handlePick()) {
				ci.cancel();
			}
		} catch (Throwable t) {
			// 出错就不接管，让原版流程照常走，总比中键彻底失灵好
			fi.dy.masa.picktweak.Reference.LOGGER.error("[Pick Tweak] 中键拾取出错，已交回原版处理", t);
		}
	}
}
