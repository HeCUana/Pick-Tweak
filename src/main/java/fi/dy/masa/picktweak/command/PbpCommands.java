package fi.dy.masa.picktweak.command;

import fi.dy.masa.picktweak.config.Configs;
import fi.dy.masa.picktweak.inv.InventoryManager;
import fi.dy.masa.picktweak.net.PbpPayloads;
import fi.dy.masa.picktweak.tool.ToolPicker;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

/**
 * {@code /pbp} 客户端命令。
 *
 * <pre>
 *   /pbp reload
 *   /pbp give tool   &lt;pickaxe|axe|shovel|hoe|sword|shears|bucket|fishing_rod|brush&gt;
 *   /pbp give weapon &lt;sword|axe|bow|crossbow|trident|mace&gt;
 * </pre>
 */
public final class PbpCommands {

	private PbpCommands() {
	}

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext context) {
		dispatcher.register(literal("pbp")
				.then(literal("reload").executes(ctx -> {
					fi.dy.masa.picktweak.net.PbpOverrides.reload();
					if (ClientPlayNetworking.canSend(PbpPayloads.Reload.TYPE)) {
						ClientPlayNetworking.send(new PbpPayloads.Reload());
					}
					ctx.getSource().sendFeedback(Component.translatable("picktweak.command.reload_success"));
					return 1;
				}))
				.then(literal("give")
						.then(literal("tool")
								.then(ClientCommands.argument("type", StringArgumentType.word())
										.suggests((ctx, builder) -> {
											for (String type : ToolPicker.TOOL_KINDS) {
												builder.suggest(type);
											}
											return builder.buildFuture();
										})
										.executes(ctx -> give(ctx.getSource(),
												StringArgumentType.getString(ctx, "type"), false))))
						.then(literal("weapon")
								.then(ClientCommands.argument("type", StringArgumentType.word())
										.suggests((ctx, builder) -> {
											for (String type : ToolPicker.WEAPON_KINDS) {
												builder.suggest(type);
											}
											return builder.buildFuture();
										})
										.executes(ctx -> give(ctx.getSource(),
												StringArgumentType.getString(ctx, "type"), true))))));
	}

	private static int give(FabricClientCommandSource source, String type, boolean weapon) {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		LocalPlayer player = mc.player;
		if (level == null || player == null) {
			source.sendFeedback(Component.translatable("picktweak.command.not_in_world"));
			return 0;
		}
		String normalized = type.toLowerCase(Locale.ROOT);
		List<String> allowed = weapon ? ToolPicker.WEAPON_KINDS : ToolPicker.TOOL_KINDS;
		if (!allowed.contains(normalized)) {
			source.sendFeedback(Component.translatable("picktweak.command.unknown_type", type));
			return 0;
		}

		// 优先让服务端发放（服务端权威）。
		if (ToolPicker.requestGive(mc, Configs.toolPicker, normalized, weapon)) {
			source.sendFeedback(Component.translatable("picktweak.command.given", normalized));
			return 1;
		}

		// 服务端没装 PBP：本地生成，仅创造模式可用。
		if (!player.hasInfiniteMaterials()) {
			source.sendFeedback(Component.translatable("picktweak.command.need_creative"));
			return 0;
		}
		ItemStack stack = ToolPicker.generateTool(level, Configs.toolPicker, normalized, weapon);
		if (stack.isEmpty()) {
			source.sendFeedback(Component.translatable("picktweak.command.generate_failed", type));
			return 0;
		}
		InventoryManager.giveCreative(stack);
		source.sendFeedback(Component.translatable("picktweak.command.given", stack.getHoverName()));
		return 1;
	}
}
