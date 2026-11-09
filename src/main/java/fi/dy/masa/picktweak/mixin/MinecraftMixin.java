package fi.dy.masa.picktweak.mixin;

import fi.dy.masa.picktweak.pick.ClientPicker;
import net.minecraft.client.Minecraft;
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
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

	@Inject(method = "pickBlockOrEntity", at = @At("HEAD"), cancellable = true)
	private void pickBlockPro$pick(CallbackInfo ci) {
		if (ClientPicker.handlePick()) {
			ci.cancel();
		}
	}
}
