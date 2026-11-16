package dev.structure.client.module.player;

import dev.structure.client.module.Category;
import dev.structure.client.module.HudModule;
import dev.structure.client.util.Draw;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

public class PotionHud extends HudModule {
	private static final class Line {
		final String text;
		final int rgb;

		Line(String text, int rgb) {
			this.text = text;
			this.rgb = rgb;
		}
	}

	private List<Line> cache = new ArrayList<>();
	private int w;
	private int h;

	public PotionHud() {
		super("Potion HUD", "Shows your active effects.", Category.PLAYER, 0.99, 0.2);
	}

	private static String roman(int n) {
		String[] r = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
		return n >= 1 && n < r.length ? r[n] : String.valueOf(n);
	}

	@Override
	public void prepare(boolean editing) {
		List<Line> out = new ArrayList<>();
		ClientPlayerEntity p = mc.player;
		if (p != null) {
			for (StatusEffectInstance e : p.getStatusEffects()) {
				String t;
				if (e.isInfinite()) {
					t = "inf";
				} else {
					int s = e.getDuration() / 20;
					t = (s / 60) + ":" + String.format("%02d", s % 60);
				}
				String name = e.getEffectType().value().getName().getString();
				out.add(new Line(name + " " + roman(e.getAmplifier() + 1) + "  " + t, e.getEffectType().value().getColor()));
			}
		}
		if (out.isEmpty() && editing) out.add(new Line("Speed II  0:30", 0x7CAFC6));
		cache = out;
		int mw = 0;
		for (Line l : out) mw = Math.max(mw, Draw.width(l.text));
		w = out.isEmpty() ? 0 : mw + 16;
		h = out.isEmpty() ? 0 : out.size() * (Draw.fontH() + 3) + 5;
	}

	@Override
	public int contentWidth() {
		return w;
	}

	@Override
	public int contentHeight() {
		return h;
	}

	@Override
	public void drawContent(DrawContext ctx, float delta, boolean editing) {
		ctx.fill(0, 0, w, h, 0x99080410);
		ctx.fill(0, 0, 2, h, 0xFF000000 | (color.argb() & 0xFFFFFF));
		int y = 4;
		for (Line l : cache) {
			ctx.fill(6, y + 1, 10, y + 6, 0xFF000000 | l.rgb);
			Draw.text(ctx, l.text, 13, y, color.argb());
			y += Draw.fontH() + 3;
		}
	}
}
