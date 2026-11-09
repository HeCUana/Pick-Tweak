package fi.dy.masa.picktweak.pick;

import fi.dy.masa.picktweak.config.Configs;
import fi.dy.masa.picktweak.config.Configs;
import fi.dy.masa.picktweak.net.PbpPayloads;
import fi.dy.masa.picktweak.util.PbpText;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * 客户端的中键拾取处理。
 *
 * <p>服务端安装了 PBP 时，客户端按配置的距离自己射线一次，把请求发给服务端，
 * 由服务端权威地计算并发放物品；服务端没装时完全不接管，走原版流程。</p>
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

		boolean ctrl = mc.hasControlDown();
		boolean alt = mc.hasAltDown();

		if (!ClientPlayNetworking.canSend(PbpPayloads.PickBlock.TYPE)) {
			if (ctrl || alt) {
				hintMissingServer();
			}
			return false;
		}

		double blockRange = effectiveRange(config.range, config.useInteractionRange, config.creativeRange, player);
		double entityRange = effectiveRange(config.entityRange, config.useInteractionRange, config.creativeRange, player);
		HitResult hit = ClientRaycast.pick(mc, Math.max(blockRange, entityRange));
		if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
			BlockHitResult blockResult = (BlockHitResult) hit;
			if (!withinRange(mc, hit, blockRange)) {
				return true;
			}
			ClientPlayNetworking.send(new PbpPayloads.PickBlock(blockResult.getBlockPos(), ctrl, alt));
			feedback(config, blockResult.getBlockPos().toShortString(), ctrl, alt);
			return true;
		}
		if (hit != null && hit.getType() == HitResult.Type.ENTITY) {
			EntityHitResult entityResult = (EntityHitResult) hit;
			if (!withinRange(mc, hit, entityRange)) {
				return true;
			}
			ClientPlayNetworking.send(new PbpPayloads.PickEntity(entityResult.getEntity().getId(), ctrl));
			feedback(config, entityResult.getEntity().getName().getString(), ctrl, false);
			return true;
		}

		// 什么都没瞄到：看看是不是指着太阳 / 月亮。
		if (config.pickLightSources) {
			String celestial = ClientRaycast.pickCelestial(mc);
			if (celestial != null) {
				ClientPlayNetworking.send(new PbpPayloads.PickCelestial(celestial));
				feedback(config, celestial, ctrl, alt);
				return true;
			}
		}
		// 我们的范围内什么都没瞄到：直接吞掉这次点击（避免原版在更短的距离上误触）。
		return true;
	}

	/** 命中点是否在范围内。 */
	private static boolean withinRange(Minecraft mc, HitResult hit, double range) {
		net.minecraft.world.entity.Entity camera = mc.getCameraEntity();
		if (camera == null) {
			return false;
		}
		return hit.getLocation().distanceToSqr(camera.getEyePosition(1.0F)) <= range * range;
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
