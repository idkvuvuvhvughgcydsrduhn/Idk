package dev.structure.client.gui;

import dev.structure.client.setting.ColorSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

/** Saturation/value square + hue bar. Shared by the ClickGUI and the HUD editor. */
public final class ColorPicker {
	public static final int HUE_W = 12;
	public static final int GAP = 6;

	private ColorPicker() {
	}

	public static int svWidth(int w) {
		return w - HUE_W - GAP;
	}

	public static void draw(DrawContext ctx, ColorSetting cs, int x, int y, int w, int h) {
		int svW = svWidth(w);
		for (int c = 0; c < svW; c += 2) {
			float s = c / (float) Math.max(1, svW - 1);
			int top = 0xFF000000 | MathHelper.hsvToRgb(cs.h, s, 1f);
			ctx.fillGradient(x + c, y, Math.min(x + c + 2, x + svW), y + h, top, 0xFF000000);
		}
		int mx = x + Math.round(cs.s * (svW - 1));
		int my = y + Math.round((1f - cs.v) * (h - 1));
		ctx.fill(mx - 3, my - 3, mx + 4, my + 4, 0xFF000000);
		ctx.fill(mx - 2, my - 2, mx + 3, my + 3, 0xFFFFFFFF);
		ctx.fill(mx - 1, my - 1, mx + 2, my + 2, cs.argb());
		int hx = x + svW + GAP;
		for (int r = 0; r < h; r += 2) {
			float hue = r / (float) Math.max(1, h - 1);
			ctx.fill(hx, y + r, hx + HUE_W, Math.min(y + r + 2, y + h), 0xFF000000 | MathHelper.hsvToRgb(hue, 1f, 1f));
		}
		int hy = y + Math.round(cs.h * (h - 1));
		ctx.fill(hx - 2, hy - 1, hx + HUE_W + 2, hy + 2, 0xFFFFFFFF);
	}

	public static void dragSv(ColorSetting cs, int x, int y, int w, int h, double mx, double my) {
		int svW = svWidth(w);
		float s = (float) MathHelper.clamp((mx - x) / Math.max(1, svW - 1), 0.0, 1.0);
		float v = 1f - (float) MathHelper.clamp((my - y) / Math.max(1, h - 1), 0.0, 1.0);
		cs.setHsv(cs.h, s, v);
	}

	public static void dragHue(ColorSetting cs, int y, int h, double my) {
		float hue = (float) MathHelper.clamp((my - y) / Math.max(1, h - 1), 0.0, 1.0);
		cs.setHsv(hue, cs.s < 0.05f ? 1f : cs.s, cs.v < 0.05f ? 1f : cs.v);
	}
}
