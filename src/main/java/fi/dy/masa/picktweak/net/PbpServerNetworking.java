package fi.dy.masa.picktweak.net;

import com.mojang.logging.LogUtils;
import fi.dy.masa.picktweak.Reference;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 服务端网络接线：注册 {@link PbpPayloads} 的通道类型与接收器。
 *
 * <p>{@link #register()} 是幂等的：只有第一次调用真正注册，之后直接返回。
 * 由 {@code InitHandler} 在模组初始化阶段调用一次（不要放到
 * {@code SERVER_STARTING} 之类的服务端生命周期事件里——payload 类型必须在初始化期注册）。</p>
 */
public final class PbpServerNetworking {

	private static final Logger LOGGER = LogUtils.getLogger();

	private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

	private PbpServerNetworking() {
	}

	/** 幂等的注册入口。 */
	public static void register() {
		if (!REGISTERED.compareAndSet(false, true)) {
			return;
		}

		// 服务端配置与自定义映射（客户端也会读同一份 overrides 用于本地回退，见 InitHandler）
		PbpServerConfig.load();
		PbpOverrides.load();

		// 通道类型：客户端与服务端都要注册，客户端的 canSend() 依赖它
		PayloadTypeRegistry.serverboundPlay().register(PbpPayloads.PickBlock.TYPE, PbpPayloads.PickBlock.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(PbpPayloads.PickEntity.TYPE, PbpPayloads.PickEntity.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(PbpPayloads.GiveTool.TYPE, PbpPayloads.GiveTool.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(PbpPayloads.PickCelestial.TYPE, PbpPayloads.PickCelestial.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(PbpPayloads.Reload.TYPE, PbpPayloads.Reload.CODEC);

		// 接收器：一律回到服务端主线程再处理，避免在网络线程里碰世界状态
		ServerPlayNetworking.registerGlobalReceiver(PbpPayloads.PickBlock.TYPE,
				(payload, context) -> {
					ServerPlayer player = context.player();
					context.server().execute(() -> ServerPickService.handlePickBlock(
							player, payload.pos(), payload.includeData(), payload.includeState()));
				});
		ServerPlayNetworking.registerGlobalReceiver(PbpPayloads.PickEntity.TYPE,
				(payload, context) -> {
					ServerPlayer player = context.player();
					context.server().execute(() -> ServerPickService.handlePickEntity(
							player, payload.entityId(), payload.includeData()));
				});
		ServerPlayNetworking.registerGlobalReceiver(PbpPayloads.GiveTool.TYPE,
				(payload, context) -> {
					ServerPlayer player = context.player();
					context.server().execute(() -> ServerPickService.handleGiveTool(player, payload.request()));
				});
		ServerPlayNetworking.registerGlobalReceiver(PbpPayloads.PickCelestial.TYPE,
				(payload, context) -> {
					ServerPlayer player = context.player();
					context.server().execute(() -> ServerPickService.handlePickCelestial(player, payload.kind()));
				});
		ServerPlayNetworking.registerGlobalReceiver(PbpPayloads.Reload.TYPE,
				(payload, context) -> {
					ServerPlayer player = context.player();
					context.server().execute(() -> handleReload(player));
				});

		LOGGER.info("[{}] 服务端网络通道已注册", Reference.MOD_NAME);
	}

	/**
	 * 26.3 的权限判定：等价于旧版的 OP 等级 >= 2。
	 *
	 * <p>26.3 移除了 {@code ServerPlayer#hasPermissions(int)}，改用
	 * {@code player.permissions().hasPermission(...)}。</p>
	 */
	static boolean isModerator(ServerPlayer player) {
		return player.permissions().hasPermission(Permissions.COMMANDS_MODERATOR);
	}

	/** 处理客户端发来的重载请求：默认仅 OP 可用。 */
	private static void handleReload(ServerPlayer player) {
		PbpServerConfig config = PbpServerConfig.get();

		if (config.requireOpForReload && !isModerator(player)) {
			player.sendSystemMessage(Component.translatable("picktweak.message.reload_denied"));
			return;
		}

		PbpServerConfig.load();
		PbpOverrides.reload();
		player.sendSystemMessage(Component.translatable("picktweak.command.reload_success"));
		LOGGER.info("[{}] {} 重载了服务端配置与自定义映射", Reference.MOD_NAME, player.getName().getString());
	}
}
