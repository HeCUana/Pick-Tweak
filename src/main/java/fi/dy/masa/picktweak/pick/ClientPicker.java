package fi.dy.masa.picktweak.pick;

import fi.dy.masa.picktweak.config.Configs;
import fi.dy.masa.picktweak.config.HotkeyBinds;
import fi.dy.masa.picktweak.net.PbpPayloads;
import fi.dy.masa.picktweak.util.NbtFormatter;
import fi.dy.masa.picktweak.util.PbpText;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

import java.util.List;

/**
 * 客户端的中键拾取处理。
 *
 * <p>服务端安装了 PBP 时，客户端按配置的距离自己射线一次，把请求发给服务端，
 * 由服务端权威地计算并发放物品；服务端没装时退回客户端本地计算，
 * Ctrl / Alt 功能两条路径都可用。</p>
 */
public final class ClientPicker {

	private static long lastMissingServerHint = 0L;

	private ClientPicker() {
	}

	/**
	 * 处理一次中键拾取。
	 *
	 * @return 是否已经接管（true 表示不再走原版流程）
	 */
	public static boolean handlePick() {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;

		if (player == null || mc.level == null) {
			return false;
		}

		Configs.BlockPicker config = Configs.blockPicker;

		if (!config.enabled) {
			return false;
		}

		// 修饰键走 MaLiLib 热键，绑定在「热键」页里可以改
		boolean ctrl = HotkeyBinds.isModifierActive(HotkeyBinds.Modifier.NBT);
		boolean alt = HotkeyBinds.isModifierActive(HotkeyBinds.Modifier.STATE);

		boolean serverSupport = ClientPlayNetworking.canSend(PbpPayloads.PickBlock.TYPE);

		if (!serverSupport) {
			// 服务端没有本模组：扩展拾取需要服务端权威计算，
			// 这里不再在客户端本地伪造物品，而是完全交回原版拾取。
			// 仅保留纯客户端的 Ctrl/Alt 剪贴板复制（不产生任何物品）。
			hintMissingServer();
			copyClipboardIfRequested(mc, config, ctrl, alt);
			return false;
		}

		double blockRange = effectiveRange(config.range, config.useInteractionRange, config.creativeRange, player);
		double entityRange = effectiveRange(config.entityRange, config.useInteractionRange, config.creativeRange, player);
		HitResult hit = resolveHit(mc, Math.max(blockRange, entityRange));

		if (hit == null) {
			// 什么都没瞄到：不接管，交给原版（原版这时候也拾取不到）
			return false;
		}

		if (hit.getType() == HitResult.Type.BLOCK) {
			if (!config.pickBlocks) {
				return false;
			}

			BlockHitResult blockResult = (BlockHitResult) hit;

			if (!withinRange(mc, hit, blockRange)) {
				return false;
			}

			return pickBlock(mc, blockResult, config, ctrl, alt);
		}

		if (hit.getType() == HitResult.Type.ENTITY) {
			if (!config.pickEntities) {
				return false;
			}

			EntityHitResult entityResult = (EntityHitResult) hit;

			if (!withinRange(mc, hit, entityRange)) {
				return false;
			}

			return pickEntity(mc, entityResult.getEntity(), config, ctrl);
		}

		// 什么都没瞄到：看看是不是指着太阳 / 月亮（这个只有服务端能做）。
		if (config.pickLightSources) {
			String celestial = ClientRaycast.pickCelestial(mc);

			if (celestial != null) {
				ClientPlayNetworking.send(new PbpPayloads.PickCelestial(celestial));
				feedback(config, celestial, ctrl, alt);
				return true;
			}
		}

		return false;
	}

	/** 拾取方块。 */
	private static boolean pickBlock(Minecraft mc, BlockHitResult blockResult, Configs.BlockPicker config,
			boolean ctrl, boolean alt) {
		BlockPos pos = blockResult.getBlockPos();

		try {
			if (ctrl) {
				// Ctrl：把方块实体的 NBT 复制到剪贴板（可直接粘进 /data 或 /summon）。
				copyBlockNbt(mc, pos);
			}

			if (alt) {
				// Alt：把方块状态属性复制到剪贴板（例如 facing=north,half=top）。
				copyBlockState(mc, pos, config);
			}

			ClientPlayNetworking.send(new PbpPayloads.PickBlock(pos, ctrl, alt));
		} catch (Throwable t) {
			fi.dy.masa.picktweak.Reference.LOGGER.error("[Pick Tweak] 拾取方块出错", t);
			return false;
		}

		feedback(config, pos.toShortString(), ctrl, alt);
		return true;
	}

	/** 拾取实体。 */
	private static boolean pickEntity(Minecraft mc, Entity entity, Configs.BlockPicker config,
			boolean ctrl) {
		try {
			if (ctrl) {
				// Ctrl：把实体的完整 NBT 复制到剪贴板（盔甲架的装备、药水、属性等）。
				copyEntityNbt(mc, entity);
			}

			ClientPlayNetworking.send(new PbpPayloads.PickEntity(entity.getId(), ctrl));
		} catch (Throwable t) {
			fi.dy.masa.picktweak.Reference.LOGGER.error("[Pick Tweak] 拾取实体出错", t);
			return false;
		}

		feedback(config, entity.getName().getString(), ctrl, false);
		return true;
	}

	/**
	 * 找出这次要拾取的目标。
	 *
	 * <p>原版每个 tick 都会算一次 {@code hitResult}（交互距离内），
	 * 能直接用就复用，省掉一次重复的方块 + 实体射线；
	 * 只有原版打空（目标在交互距离之外）时才自己做远距离射线。</p>
	 */
	private static HitResult resolveHit(Minecraft mc, double range) {
		return ClientRaycast.resolveHit(mc, range);
	}

	/** 命中点是否在范围内。 */
	private static boolean withinRange(Minecraft mc, HitResult hit, double range) {
		return ClientRaycast.withinRange(mc, hit, range);
	}

	/**
	 * 服务端没有本模组时的降级路径：中键拾取完全交回原版，
	 * 但 Ctrl / Alt 仍提供纯客户端的剪贴板复制（不产生任何物品）。
	 */
	private static void copyClipboardIfRequested(Minecraft mc, Configs.BlockPicker config, boolean ctrl, boolean alt) {
		if (!ctrl && !alt) {
			return;
		}

		LocalPlayer player = mc.player;

		if (player == null) {
			return;
		}

		double blockRange = effectiveRange(config.range, config.useInteractionRange, config.creativeRange, player);
		double entityRange = effectiveRange(config.entityRange, config.useInteractionRange, config.creativeRange, player);
		HitResult hit = resolveHit(mc, Math.max(blockRange, entityRange));

		if (hit == null) {
			return;
		}

		if (hit.getType() == HitResult.Type.BLOCK && config.pickBlocks) {
			if (!withinRange(mc, hit, blockRange)) {
				return;
			}
			BlockPos pos = ((BlockHitResult) hit).getBlockPos();
			if (ctrl) {
				copyBlockNbt(mc, pos);
			}
			if (alt) {
				copyBlockState(mc, pos, config);
			}
		} else if (hit.getType() == HitResult.Type.ENTITY && config.pickEntities) {
			if (!withinRange(mc, hit, entityRange)) {
				return;
			}
			if (ctrl) {
				copyEntityNbt(mc, ((EntityHitResult) hit).getEntity());
			}
		}
	}

	/**
	 * Ctrl + 中键（方块）：把方块实体的 NBT 复制到剪贴板。
	 *
	 * <p>普通方块没有方块实体，也就没有 NBT 可复制，这时只提示一句。</p>
	 */
	private static void copyBlockNbt(Minecraft mc, BlockPos pos) {
		Configs.BlockPicker config = Configs.blockPicker;

		if (!config.copyNbt) {
			return;
		}

		CompoundTag tag = BlockPicker.blockEntityNbt(mc.level, pos);
		String label = BlockPicker.blockId(mc.level.getBlockState(pos));
		copyNbt(tag, label, config.blockEntityNbtBlacklist, config.showCopiedNbt, config.copyNbtChatFeedback);
	}

	/**
	 * Alt + 中键（方块）：把方块状态属性写进剪贴板。
	 *
	 * <p>物品本身已经通过 {@code block_state} 组件带上了状态（放置时会还原朝向、
	 * 营火点燃状态等），这里再复制一份文本，方便直接粘到命令或文档里。</p>
	 */
	private static void copyBlockState(Minecraft mc, BlockPos pos, Configs.BlockPicker config) {
		if (!config.copyState) {
			return;
		}

		BlockState state = mc.level.getBlockState(pos);
		String label = BlockPicker.blockId(state);
		String text = BlockPicker.blockStateText(state);

		if (text.isEmpty()) {
			if (config.copyStateChatFeedback) {
				PbpText.chat(Component.translatable("picktweak.message.no_block_state", label));
			}
			return;
		}

		PbpText.copyToClipboard(text);
		if (config.copyStateChatFeedback) {
			PbpText.chat(Component.translatable("picktweak.message.state_copied", label, text));
		}
	}

	/**
	 * Ctrl + 中键（实体）：把实体的 NBT 复制到剪贴板。
	 *
	 * <p>26.3 的实体装备存在 {@code equipment} 键里，穿装备的盔甲架也能完整复制。</p>
	 */
	private static void copyEntityNbt(Minecraft mc, Entity entity) {
		Configs.BlockPicker config = Configs.blockPicker;

		if (!config.copyNbt) {
			return;
		}

		CompoundTag tag = BlockPicker.entityNbt(mc.level, entity);
		String label = entity.getName().getString();
		copyNbt(tag, label, config.entityNbtBlacklist, config.showCopiedNbt, config.copyNbtChatFeedback);
	}

	/**
	 * 把目标的 NBT 复制到剪贴板，并按配置决定是否在聊天栏给出提示。
	 *
	 * <p>剪贴板始终写入单行 SNBT（不含 id），可以直接粘进 {@code /data} 或 {@code /summon}；
	 * {@code chatFeedback} 为 true 时才输出「已复制 / 没有 NBT」提示，并在 {@code showInChat}
	 * 开启时额外打印缩进版。</p>
	 */
	private static void copyNbt(CompoundTag tag, String label, List<String> blacklist, boolean showInChat, boolean chatFeedback) {
		CompoundTag filtered = NbtFormatter.filter(tag, blacklist);

		if (filtered == null || filtered.isEmpty()) {
			if (chatFeedback) {
				PbpText.chat(Component.translatable("picktweak.message.no_nbt", label));
			}
			return;
		}

		PbpText.copyToClipboard(NbtFormatter.compact(filtered));

		if (chatFeedback) {
			PbpText.chat(Component.translatable("picktweak.message.nbt_copied", label));

			if (showInChat) {
				PbpText.chat(Component.literal(NbtFormatter.pretty(filtered)));
			}
		}
	}

	/** 拾取反馈（可在配置里打开，用来确认修饰键是否生效）。 */
	private static void feedback(Configs.BlockPicker config, String target, boolean ctrl, boolean alt) {
		if (!config.showPickFeedback) {
			return;
		}

		StringBuilder suffix = new StringBuilder();

		if (ctrl) {
			suffix.append(" +NBT");
		}

		if (alt) {
			suffix.append(" +状态");
		}

		PbpText.chat(Component.translatable("picktweak.message.picked", target + suffix));
	}

	/** 计算实际使用的拾取距离。 */
	public static double effectiveRange(double configured, boolean useInteractionRange, double creativeRange, LocalPlayer player) {
		if (player != null && player.hasInfiniteMaterials()) {
			return creativeRange;
		}

		if (useInteractionRange && player != null) {
			return Math.max(player.blockInteractionRange(), player.entityInteractionRange());
		}

		return configured;
	}

	/** 方块是否在拾取范围内。 */
	public static boolean inBlockRange(LocalPlayer player, net.minecraft.core.BlockPos pos, double range) {
		double dx = player.getX() - (pos.getX() + 0.5D);
		double dy = player.getY() - (pos.getY() + 0.5D);
		double dz = player.getZ() - (pos.getZ() + 0.5D);
		return dx * dx + dy * dy + dz * dz <= range * range;
	}

	/** 实体是否在拾取范围内。 */
	public static boolean inEntityRange(LocalPlayer player, net.minecraft.world.entity.Entity entity, double range) {
		return player.distanceToSqr(entity) <= range * range;
	}

	private static void hintMissingServer() {
		long now = System.currentTimeMillis();

		if (now - lastMissingServerHint < 30_000L) {
			return;
		}

		lastMissingServerHint = now;
		PbpText.chat(Component.translatable("picktweak.message.server_mod_missing"));
	}
}