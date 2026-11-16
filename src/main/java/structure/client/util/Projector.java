package dev.structure.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * World -> screen projection done on the CPU so that all ESP-style modules draw as
 * plain 2D HUD overlays. This avoids depending on the world render pipeline, which
 * changed a lot between 1.21.x versions (and is the most fragile part on Android GL).
 */
public final class Projector {
	public static boolean ready;
	public static int sw;
	public static int sh;
	private static double cx, cy, cz;
	private static double fx, fy, fz;
	private static double rx, rz;
	private static double ux, uy, uz;
	private static double focal;

	private Projector() {
	}

	public static boolean begin(MinecraftClient mc, float delta) {
		ready = false;
		ClientPlayerEntity p = mc.player;
		if (p == null || mc.world == null) return false;
		if (!mc.options.getPerspective().isFirstPerson()) return false;
		Camera cam = mc.gameRenderer.getCamera();
		Vec3d pos = p.getLerpedPos(delta);
		cx = pos.x;
		cy = pos.y + (p.getEyeY() - p.getY());
		cz = pos.z;
		double yaw = Math.toRadians(cam.getYaw());
		double pitch = Math.toRadians(cam.getPitch());
		double cp = Math.cos(pitch);
		fx = -Math.sin(yaw) * cp;
		fy = -Math.sin(pitch);
		fz = Math.cos(yaw) * cp;
		rx = -Math.cos(yaw);
		rz = -Math.sin(yaw);
		// up = right x forward (right.y == 0)
		ux = -rz * fy;
		uy = rz * fx - rx * fz;
		uz = rx * fy;
		sw = mc.getWindow().getScaledWidth();
		sh = mc.getWindow().getScaledHeight();
		double fov = mc.options.getFov().getValue();
		focal = (sh / 2.0) / Math.tan(Math.toRadians(fov) / 2.0);
		ready = true;
		return true;
	}

	/** Projects a world point. Returns false when the point is behind the camera. */
	public static boolean project(double x, double y, double z, double[] out) {
		double dx = x - cx;
		double dy = y - cy;
		double dz = z - cz;
		double d = dx * fx + dy * fy + dz * fz;
		if (d < 0.05) return false;
		double r = dx * rx + dz * rz;
		double u = dx * ux + dy * uy + dz * uz;
		out[0] = sw / 2.0 + r / d * focal;
		out[1] = sh / 2.0 - u / d * focal;
		return true;
	}

	/** Screen-space direction (not normalised) from the crosshair towards a point, also valid behind the camera. */
	public static void direction(double x, double y, double z, double[] out) {
		double dx = x - cx;
		double dy = y - cy;
		double dz = z - cz;
		double r = dx * rx + dz * rz;
		double u = dx * ux + dy * uy + dz * uz;
		out[0] = r;
		out[1] = -u;
	}

	/** 2D bounds of a box: {minX, minY, maxX, maxY}, or null when any corner is behind the camera. */
	public static double[] bounds(Box b) {
		double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
		double[] o = new double[2];
		for (int i = 0; i < 8; i++) {
			double x = (i & 1) == 0 ? b.minX : b.maxX;
			double y = (i & 2) == 0 ? b.minY : b.maxY;
			double z = (i & 4) == 0 ? b.minZ : b.maxZ;
			if (!project(x, y, z, o)) return null;
			minX = Math.min(minX, o[0]);
			maxX = Math.max(maxX, o[0]);
			minY = Math.min(minY, o[1]);
			maxY = Math.max(maxY, o[1]);
		}
		return new double[] {minX, minY, maxX, maxY};
	}
}
