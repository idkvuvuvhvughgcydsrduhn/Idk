package dev.structure.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Small 2D drawing helpers built only on stable DrawContext calls. */
public final class Draw {
	private Draw() {
	}

	public static TextRenderer tr() {
		return MinecraftClient.getInstance().textRenderer;
	}

	public static int fontH() {
		return tr().fontHeight;
	}

	public static int width(String s) {
		return tr().getWidth(s);
	}

	public static int widthBold(String s) {
		return tr().getWidth(Text.literal(s).formatted(Formatting.BOLD));
	}

	public static int alpha(int argb, float a) {
		int al = Math.max(0, Math.min(255, (int) (((argb >>> 24) & 255) * a)));
		return (al << 24) | (argb & 0xFFFFFF);
	}

	public static int mix(int c1, int c2, float t) {
		t = Math.max(0f, Math.min(1f, t));
		int a = (int) (((c1 >>> 24) & 255) * (1 - t) + ((c2 >>> 24) & 255) * t);
		int r = (int) (((c1 >> 16) & 255) * (1 - t) + ((c2 >> 16) & 255) * t);
		int g = (int) (((c1 >> 8) & 255) * (1 - t) + ((c2 >> 8) & 255) * t);
		int b = (int) ((c1 & 255) * (1 - t) + (c2 & 255) * t);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	public static int darken(int argb, float f) {
		return mix(argb, 0xFF000000 | 0, 1f - f);
	}

	public static void rect(DrawContext c, int x, int y, int w, int h, int color) {
		c.fill(x, y, x + w, y + h, color);
	}

	/** Rectangle with 1px cut corners (cheap rounded look). */
	public static void round(DrawContext c, int x, int y, int w, int h, int color) {
		if (w < 4 || h < 4) {
			c.fill(x, y, x + w, y + h, color);
			return;
		}
		c.fill(x + 1, y, x + w - 1, y + h, color);
		c.fill(x, y + 1, x + 1, y + h - 1, color);
		c.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
	}

	public static void outline(DrawContext c, int x, int y, int w, int h, int color) {
		c.fill(x, y, x + w, y + 1, color);
		c.fill(x, y + h - 1, x + w, y + h, color);
		c.fill(x, y + 1, x + 1, y + h - 1, color);
		c.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
	}

	public static void outlineBox(DrawContext c, int x1, int y1, int x2, int y2, int color, int t) {
		c.fill(x1, y1, x2, y1 + t, color);
		c.fill(x1, y2 - t, x2, y2, color);
		c.fill(x1, y1 + t, x1 + t, y2 - t, color);
		c.fill(x2 - t, y1 + t, x2, y2 - t, color);
	}

	public static void text(DrawContext c, String s, int x, int y, int color) {
		c.drawText(tr(), s, x, y, color, true);
	}

	public static void textFlat(DrawContext c, String s, int x, int y, int color) {
		c.drawText(tr(), s, x, y, color, false);
	}

	public static void textBold(DrawContext c, String s, int x, int y, int color) {
		c.drawText(tr(), Text.literal(s).formatted(Formatting.BOLD), x, y, color, true);
	}

	public static void textRight(DrawContext c, String s, int xRight, int y, int color) {
		text(c, s, xRight - width(s), y, color);
	}

	public static void textCenter(DrawContext c, String s, int cx, int y, int color) {
		text(c, s, cx - width(s) / 2, y, color);
	}

	/** Trim text with ".." to fit a pixel width. */
	public static String fit(String s, int maxW) {
		if (width(s) <= maxW) return s;
		while (s.length() > 1 && width(s + "..") > maxW) s = s.substring(0, s.length() - 1);
		return s + "..";
	}

	/** Pill toggle switch. */
	public static void toggle(DrawContext c, int x, int y, int w, int h, boolean on, int accent) {
		round(c, x, y, w, h, on ? accent : 0xFF3A3450);
		int k = h - 4;
		int kx = on ? x + w - k - 2 : x + 2;
		round(c, kx, y + 2, k, k, on ? 0xFFFFFFFF : 0xFFB8B4C8);
	}

	/** Dotted/stepped line made of small squares; fine for a handful of tracers. */
	public static void line(DrawContext c, double x1, double y1, double x2, double y2, int color, int t) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		double len = Math.sqrt(dx * dx + dy * dy);
		if (len < 1.0) {
			c.fill((int) x1, (int) y1, (int) x1 + t, (int) y1 + t, color);
			return;
		}
		int steps = (int) Math.min(len / (t * 0.9), 600);
		if (steps < 1) steps = 1;
		double sx = dx / steps;
		double sy = dy / steps;
		for (int i = 0; i <= steps; i++) {
			int x = (int) Math.round(x1 + sx * i);
			int y = (int) Math.round(y1 + sy * i);
			c.fill(x, y, x + t, y + t, color);
		}
	}

	public static boolean inside(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}
}
