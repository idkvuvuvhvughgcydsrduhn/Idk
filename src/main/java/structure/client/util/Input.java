package structure.client.util;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public final class Input {
    private Input() {}
    private static long h() { return MinecraftClient.getInstance().getWindow().getHandle(); }
    public static boolean key(int k) { return GLFW.glfwGetKey(h(), k) == GLFW.GLFW_PRESS; }
    public static boolean mouse(int b) { return GLFW.glfwGetMouseButton(h(), b) == GLFW.GLFW_PRESS; }

    public static String keyName(int k) {
        if (k < 0) return "None";
        switch (k) {
            case 32: return "Space"; case 256: return "Esc"; case 257: return "Enter"; case 258: return "Tab";
            case 259: return "Backspace"; case 260: return "Insert"; case 261: return "Delete";
            case 262: return "Right"; case 263: return "Left"; case 264: return "Down"; case 265: return "Up";
            case 266: return "PgUp"; case 267: return "PgDn"; case 268: return "Home"; case 269: return "End";
            case 340: return "L-Shift"; case 344: return "R-Shift"; case 341: return "L-Ctrl"; case 345: return "R-Ctrl";
            case 342: return "L-Alt"; case 346: return "R-Alt";
        }
        if (k >= 290 && k <= 301) return "F" + (k - 289);
        if (k >= 320 && k <= 329) return "Num" + (k - 320);
        String n = GLFW.glfwGetKeyName(k, 0);
        return n != null ? n.toUpperCase() : "Key" + k;
    }
}
