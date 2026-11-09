package fi.dy.masa.picktweak.pick;

import net.minecraft.client.Minecraft;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 客户端自定义射线拾取。
 *
 * <p>26.3 的原版 {@code hitResult} 受交互距离限制（超出即 MISS，中键直接无效），
 * 这里按配置的拾取范围自己射线一次，方块与实体都算，取更近的那个。</p>
 */
public final class ClientRaycast {

	/** 判定"指着太阳 / 月亮"的最大夹角（度）。 */
	private static final double CELESTIAL_MAX_ANGLE = 25.0D;

	private ClientRaycast() {
	}

	/** 按给定距离射线，返回命中的方块或实体（没命中时类型为 MISS）。 */
	public static HitResult pick(Minecraft mc, double range) {
		Entity camera = mc.getCameraEntity();
		if (camera == null || mc.level == null) {
			return null;
		}
		float partialTicks = 1.0F;
		Vec3 from = camera.getEyePosition(partialTicks);
		Vec3 direction = camera.getViewVector(partialTicks);
		Vec3 to = from.add(direction.scale(range));

		HitResult blockHit = camera.pick(range, partialTicks, false);
		double blockDistanceSq = blockHit.getLocation().distanceToSqr(from);

		AABB box = camera.getBoundingBox().expandTowards(direction.scale(range)).inflate(1.0D, 1.0D, 1.0D);
		EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
				camera, from, to, box, EntitySelector.CAN_BE_PICKED, range * range);
		if (entityHit != null && entityHit.getLocation().distanceToSqr(from) < blockDistanceSq) {
			return entityHit;
		}
		return blockHit;
	}

	/**
	 * 判断视线是否指着太阳或月亮。
	 *
	 * <p>26.3 的太阳 / 月亮只是天空渲染层画出来的贴图，既不是方块也不是实体，
	 * 所以这里按原版天空的变换公式自己算它们的方向：</p>
	 * <ul>
	 *   <li>天空整体先绕 Y 轴转 -90°</li>
	 *   <li>天体本身位于局部坐标 {@code (0, 100, 0)}</li>
	 *   <li>再绕 X 轴转太阳角 / 月亮角</li>
	 * </ul>
	 * 得到的单位方向为 {@code (-sinθ, cosθ, 0)}。
	 *
	 * @return {@code "sun"} / {@code "moon"} / {@code null}
	 */
	public static String pickCelestial(Minecraft mc) {
		Entity camera = mc.getCameraEntity();
		if (camera == null || mc.level == null) {
			return null;
		}
		float partialTicks = 1.0F;
		Vec3 view = camera.getViewVector(partialTicks).normalize();

		Vec3 sun = celestialDirection(mc, EnvironmentAttributes.SUN_ANGLE, camera);
		if (sun != null && isAboveHorizon(sun) && view.dot(sun) >= Math.cos(Math.toRadians(CELESTIAL_MAX_ANGLE))) {
			return "sun";
		}
		Vec3 moon = celestialDirection(mc, EnvironmentAttributes.MOON_ANGLE, camera);
		if (moon != null && isAboveHorizon(moon) && view.dot(moon) >= Math.cos(Math.toRadians(CELESTIAL_MAX_ANGLE))) {
			return "moon";
		}
		return null;
	}

	private static Vec3 celestialDirection(Minecraft mc, net.minecraft.world.attribute.EnvironmentAttribute<Float> attribute, Entity camera) {
		try {
			float degrees = mc.level.environmentAttributes().getValue(attribute, camera.position());
			return celestialDirection(degrees);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 天体方向：天空绕 Y 轴 -90°，天体本地坐标 (0, 100, 0)，再绕 X 轴转角度，
	 * 得到单位方向 {@code (-sinθ, cosθ, 0)}。
	 *
	 * @param degrees 角度（度），即 {@code EnvironmentAttributes.SUN_ANGLE} / {@code MOON_ANGLE}
	 */
	public static Vec3 celestialDirection(double degrees) {
		double theta = Math.toRadians(degrees);
		return new Vec3(-Math.sin(theta), Math.cos(theta), 0.0D);
	}

	private static boolean isAboveHorizon(Vec3 direction) {
		return direction.y > 0.0D;
	}
}
