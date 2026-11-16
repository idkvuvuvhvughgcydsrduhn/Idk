package dev.structure.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class CombatUtil {
	private CombatUtil() {
	}

	private static final MinecraftClient mc = MinecraftClient.getInstance();

	/**
	 * Attack through the normal interaction manager. Fabric's AttackEntityCallback fires from there,
	 * so Criticals / AutoWeapon apply to module attacks and to manual attacks alike.
	 */
	public static void attack(Entity target, boolean keepSprint) {
		ClientPlayerEntity p = mc.player;
		if (p == null || mc.interactionManager == null || target == null) return;
		boolean sprint = p.isSprinting();
		Vec3d vel = p.getVelocity();
		mc.interactionManager.attackEntity(p, target);
		p.swingHand(Hand.MAIN_HAND);
		if (keepSprint) {
			p.setSprinting(sprint);
			Vec3d now = p.getVelocity();
			p.setVelocity(vel.x, now.y, vel.z);
		}
	}

	public static void face(Entity e) {
		ClientPlayerEntity p = mc.player;
		if (p == null) return;
		Vec3d eye = p.getEyePos();
		Box b = e.getBoundingBox();
		double tx = (b.minX + b.maxX) / 2.0;
		double tz = (b.minZ + b.maxZ) / 2.0;
		double ty = MathHelper.clamp(eye.y, b.minY + 0.1, b.maxY - 0.1);
		double dx = tx - eye.x;
		double dy = ty - eye.y;
		double dz = tz - eye.z;
		double h = Math.sqrt(dx * dx + dz * dz);
		float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, h));
		p.setYaw(yaw);
		p.setPitch(MathHelper.clamp(pitch, -90f, 90f));
	}
}
