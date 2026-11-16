package dev.structure.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;

public final class Move {
	private static final MinecraftClient mc = MinecraftClient.getInstance();

	private Move() {
	}

	public static boolean moving() {
		GameOptions o = mc.options;
		return o.forwardKey.isPressed() || o.backKey.isPressed() || o.leftKey.isPressed() || o.rightKey.isPressed();
	}

	/** Normalised world-space {x, z} direction built from the movement keys. */
	public static double[] dir() {
		GameOptions o = mc.options;
		double f = (o.forwardKey.isPressed() ? 1 : 0) - (o.backKey.isPressed() ? 1 : 0);
		double s = (o.leftKey.isPressed() ? 1 : 0) - (o.rightKey.isPressed() ? 1 : 0);
		if (f == 0 && s == 0 || mc.player == null) return new double[] {0, 0};
		double yaw = Math.toRadians(mc.player.getYaw());
		double dx = -Math.sin(yaw) * f + Math.cos(yaw) * s;
		double dz = Math.cos(yaw) * f + Math.sin(yaw) * s;
		double l = Math.sqrt(dx * dx + dz * dz);
		return new double[] {dx / l, dz / l};
	}
}
