package dev.structure.client.util;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public final class Input {
	private Input() {
	}

	private static long handle() {
		return MinecraftClient.getInstance().getWindow().getHandle();
	}

	public static boolean key(int k) {
		return k > 0 && GLFW.glfwGetKey(handle(), k) == GLFW.GLFW_PRESS;
	}

	public static boolean mouse(int b) {
		return GLFW.glfwGetMouseButton(handle(), b) == GLFW.GLFW_PRESS;
	}
}
