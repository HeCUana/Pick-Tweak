package fi.dy.masa.picktweak;

import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import fi.dy.masa.picktweak.net.PbpOverrides;
import fi.dy.masa.picktweak.net.PbpPayloads;
import fi.dy.masa.picktweak.net.PbpServerConfig;
import fi.dy.masa.picktweak.net.ServerPickService;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * 初始化：注册数据包类型与服务端拾取服务。
 */
public class InitHandler implements IInitializationHandler {

	@Override
	public void registerModHandlers() {
		// 数据包类型（客户端与服务端都要注册）
		PayloadTypeRegistry.serverboundPlay().register(PbpPayloads.PickBlock.TYPE, PbpPayloads.PickBlock.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(PbpPayloads.PickEntity.TYPE, PbpPayloads.PickEntity.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(PbpPayloads.GiveTool.TYPE, PbpPayloads.GiveTool.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(PbpPayloads.PickCelestial.TYPE, PbpPayloads.PickCelestial.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(PbpPayloads.Reload.TYPE, PbpPayloads.Reload.CODEC);

		// 服务端：注册拾取服务
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			ServerPlayNetworking.registerGlobalReceiver(PbpPayloads.PickBlock.TYPE,
					(payload, context) -> context.server().execute(() ->
							ServerPickService.handlePickBlock(context.player(), payload.pos(), payload.includeData(), payload.includeState())));
			ServerPlayNetworking.registerGlobalReceiver(PbpPayloads.PickEntity.TYPE,
					(payload, context) -> context.server().execute(() ->
							ServerPickService.handlePickEntity(context.player(), payload.entityId(), payload.includeData())));
			ServerPlayNetworking.registerGlobalReceiver(PbpPayloads.GiveTool.TYPE,
					(payload, context) -> context.server().execute(() ->
							ServerPickService.handleGiveTool(context.player(), payload.request())));
			ServerPlayNetworking.registerGlobalReceiver(PbpPayloads.PickCelestial.TYPE,
					(payload, context) -> context.server().execute(() ->
							ServerPickService.handlePickCelestial(context.player(), payload.kind())));
			ServerPlayNetworking.registerGlobalReceiver(PbpPayloads.Reload.TYPE,
					(payload, context) -> context.server().execute(() -> {
						PbpOverrides.reload();
						context.player().sendSystemMessage(net.minecraft.network.chat.Component
								.translatable("picktweak.command.reload_success"));
					}));
		});

		PbpServerConfig.load();
		PbpOverrides.load();
	}

	/** 供入口类调用。 */
	public static void init() {
		new InitHandler().registerModHandlers();
	}
}
