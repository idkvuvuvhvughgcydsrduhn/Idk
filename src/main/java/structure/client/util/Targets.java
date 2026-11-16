package dev.structure.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class Targets {
	private Targets() {
	}

	private static final MinecraftClient mc = MinecraftClient.getInstance();

	public static boolean isHostile(Entity e) {
		return e instanceof Monster;
	}

	/** Players that are not in the tab list are usually server-side NPC / bot entities. */
	public static boolean isRealPlayer(PlayerEntity p) {
		if (mc.getNetworkHandler() == null) return true;
		return mc.getNetworkHandler().getPlayerListEntry(p.getUuid()) != null;
	}

	public static boolean valid(Entity e, boolean players, boolean hostile, boolean passive, boolean invisible, boolean antiBot) {
		if (!(e instanceof LivingEntity le) || e == mc.player || !le.isAlive()) return false;
		if (e instanceof ArmorStandEntity) return false;
		if (!invisible && le.isInvisible()) return false;
		if (e instanceof PlayerEntity p) {
			if (!players || p.isSpectator()) return false;
			return !antiBot || isRealPlayer(p);
		}
		if (isHostile(e)) return hostile;
		return passive;
	}

	/** Distance from the player's eyes to the closest point of the entity's hitbox. */
	public static double reach(Entity e) {
		if (mc.player == null) return Double.MAX_VALUE;
		Vec3d eye = mc.player.getEyePos();
		Box b = e.getBoundingBox();
		double dx = MathHelper.clamp(eye.x, b.minX, b.maxX) - eye.x;
		double dy = MathHelper.clamp(eye.y, b.minY, b.maxY) - eye.y;
		double dz = MathHelper.clamp(eye.z, b.minZ, b.maxZ) - eye.z;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	public static float angleTo(Entity e) {
		if (mc.player == null) return 0f;
		Vec3d eye = mc.player.getEyePos();
		double dx = e.getX() - eye.x;
		double dz = e.getZ() - eye.z;
		float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
		return Math.abs(MathHelper.wrapDegrees(yaw - mc.player.getYaw()));
	}
}
