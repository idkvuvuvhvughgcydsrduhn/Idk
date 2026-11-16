package dev.structure.client.gui;

import dev.structure.client.util.Draw;
import net.minecraft.client.gui.DrawContext;

/** Tiny single-line text input used by the search bar and the config name field. */
public final class TextBox {
	public String text = "";
	public final String hint;
	private final int max;

	public TextBox(String hint, int max) {
		this.hint = hint;
		this.max = max;
	}

	public void type(String s) {
		for (int i = 0; i < s.length() && text.length() < max; i++) {
			char c = s.charAt(i);
			if (c >= 32 && c != 127) text += c;
		}
	}

	public void backspace() {
		if (!text.isEmpty()) text = text.substring(0, text.length() - 1);
	}

	public void draw(DrawContext ctx, int x, int y, int w, int h, boolean focused) {
		ctx.fill(x, y, x + w, y + h, Theme.PANEL);
		Draw.outline(ctx, x, y, w, h, focused ? Theme.accent() : Theme.LINE);
		boolean caret = focused && (System.currentTimeMillis() / 500) % 2 == 0;
		String shown = text.isEmpty() && !focused ? hint : text + (caret ? "_" : "");
		int col = text.isEmpty() && !focused ? Theme.DIM : Theme.TEXT;
		String fitted = shown;
		while (fitted.length() > 1 && Draw.width(fitted) > w - 10) fitted = fitted.substring(1);
		Draw.textFlat(ctx, fitted, x + 5, y + (h - 8) / 2, col);
	}
}
