package dev.structure.client.module;

import dev.structure.client.module.utility.ClientSettings;
import dev.structure.client.util.Draw;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;

/** HUD element made of text lines on a dark panel with an accent edge. */
public abstract class TextHud extends HudModule {
	private List<String> cache = new ArrayList<>();
	private int w;
	private int h;

	protected TextHud(String name, String description, Category category, double x, double y) {
		super(name, description, category, x, y);
	}

	protected abstract List<String> lines(boolean editing);

	@Override
	public void prepare(boolean editing) {
		List<String> l;
		try {
			l = lines(editing);
		} catch (Throwable t) {
			l = new ArrayList<>();
		}
		cache = l == null ? new ArrayList<>() : l;
		int mw = 0;
		for (String s : cache) mw = Math.max(mw, Draw.width(s));
		w = cache.isEmpty() ? 0 : mw + 10;
		h = cache.isEmpty() ? 0 : cache.size() * (Draw.fontH() + 2) + 6;
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
		Draw.rect(ctx, 0, 0, w, h, 0x99080410);
		Draw.rect(ctx, 0, 0, 2, h, ClientSettings.accentColor());
		int y = 4;
		for (String s : cache) {
			Draw.text(ctx, s, 6, y, color.argb());
			y += Draw.fontH() + 2;
		}
	}
}
