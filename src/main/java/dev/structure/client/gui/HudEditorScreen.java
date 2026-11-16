package dev.structure.client.gui;

import dev.structure.client.config.ConfigManager;
import dev.structure.client.module.HudModule;
import dev.structure.client.module.ModuleManager;
import dev.structure.client.module.utility.ClientSettings;
import dev.structure.client.setting.ColorSetting;
import dev.structure.client.util.Draw;
import java.util.List;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Drag HUD elements, resize them, recolour them. Positions are stored as 0..1 fractions of the free space,
 * so a layout made on one screen keeps working on any other size.
 */
public class HudEditorScreen extends Screen {
	private final Screen parent;
	private final Hits hits = new Hits();
	private HudModule sel;
	private HudModule drag;
	private int offX, offY;
	private int mouseX, mouseY;

	public HudEditorScreen(Screen parent) {
		super(Text.literal("HUD Editor"));
		this.parent = parent;
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void close() {
		ConfigManager.saveCurrent();
		client.setScreen(parent);
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		int mx = (int) click.x(), my = (int) click.y();
		Hits.Hit h = hits.at(mx, my);
		if (h != null) {
			if (h.drag != null) h.drag.apply(mx, my);
			else if (h.tap != null) h.tap.run();
			return true;
		}
		// pick the topmost HUD element under the pointer and start dragging it
		List<HudModule> huds = ModuleManager.hudModules();
		for (int i = huds.size() - 1; i >= 0; i--) {
			HudModule m = huds.get(i);
			if (!m.isEnabled()) continue;
			int x = m.screenX(width), y = m.screenY(height);
			if (Draw.inside(mx, my, x, y, m.boxW(), m.boxH())) {
				sel = m;
				drag = m;
				offX = mx - x;
				offY = my - y;
				return true;
			}
		}
		sel = null;
		return true;
	}

	@Override
	public boolean mouseReleased(Click click) {
		if (drag != null) ConfigManager.markDirty();
		drag = null;
		return true;
	}

	@Override
	public void render(DrawContext ctx, int mx, int my, float delta) {
		hits.clear();
		mouseX = mx;
		mouseY = my;
		ctx.fill(0, 0, width, height, 0x55000000);

		if (drag != null) {
			int nx = mx - offX, ny = my - offY;
			drag.moveTo(nx, ny, width, height);
		}

		// draw every module's preview while editing (even when its content is empty)
		List<HudModule> huds = ModuleManager.hudModules();
		for (HudModule m : huds) {
			if (!m.isEnabled()) continue;
			m.renderHud(ctx, delta, true, width, height);
		}

		// outlines for every enabled HUD element, the selected one is white
		for (HudModule m : huds) {
			if (!m.isEnabled() || m.boxW() <= 0) continue;
			int x = m.screenX(width), y = m.screenY(height);
			int acc = Theme.accent();
			Draw.outline(ctx, x - 1, y - 1, m.boxW() + 2, m.boxH() + 2, m == sel ? 0xFFFFFFFF : Draw.alpha(acc, 0.9f));
		}

		drawToolbar(ctx);
	}

	private void drawToolbar(DrawContext ctx) {
		int sw = width, sh = height;
		int acc = Theme.accent();

		// chips: tap a name to show / hide the element
		int cx = 4, cy = 4;
		for (HudModule h : ModuleManager.hudModules()) {
			int w = Draw.width(h.name) + 10;
			if (cx + w > sw - 4) {
				cx = 4;
				cy += 16;
			}
			boolean on = h.isEnabled();
			ctx.fill(cx, cy, cx + w, cy + 14, on ? Draw.darken(acc, 0.5f) : Theme.ROW);
			Draw.outline(ctx, cx, cy, w, 14, on ? acc : Theme.LINE);
			Draw.textCenter(ctx, h.name, cx + w / 2, cy + 3, 0xFFFFFFFF);
			final HudModule hm = h;
			hits.add(cx, cy, w, 14, () -> hm.toggle());
			cx += w + 3;
		}

		int by = sh - 22;
		if (sel != null) {
			Draw.text(ctx, sel.name, 6, by - 12, Theme.SILVER);
			button(ctx, 6, by, 46, 18, "Size -", () -> sel.scale.set(sel.scale.d() - 0.1));
			button(ctx, 56, by, 46, 18, "Size +", () -> sel.scale.set(sel.scale.d() + 0.1));
			button(ctx, 106, by, 50, 18, "Color", this::nextColor);
			button(ctx, 160, by, 58, 18, "Reset", this::resetPos);
		} else {
			Draw.text(ctx, "Drag an element. Tap a name above to show or hide it.", 6, by + 5, Theme.DIM);
		}
		button(ctx, sw - 50, by, 44, 18, "Done", this::close);
	}

	private void button(DrawContext ctx, int x, int y, int w, int h, String label, Runnable tap) {
		boolean hov = Draw.inside(mouseX, mouseY, x, y, w, h);
		ctx.fill(x, y, x + w, y + h, hov ? Theme.ROW_H : Theme.ROW);
		Draw.outline(ctx, x, y, w, h, hov ? Theme.accent() : Theme.LINE);
		Draw.textCenter(ctx, label, x + w / 2, y + (h - 8) / 2, 0xFFFFFFFF);
		hits.add(x, y, w, h, tap);
	}

	private static final int[] PRESETS = {0xA855F7, 0xC8C8D2, 0xFFFFFF, 0x38BDF8, 0x4ADE80, 0xF87171, 0xFBBF24};

	private void nextColor() {
		if (sel == null) return;
		ColorSetting c = sel.color;
		int idx = 0;
		for (int i = 0; i < PRESETS.length; i++) {
			if ((PRESETS[i] & 0xFFFFFF) == (c.argb() & 0xFFFFFF)) {
				idx = i;
				break;
			}
		}
		c.set(PRESETS[(idx + 1) % PRESETS.length]);
	}

	private void resetPos() {
		if (sel == null) return;
		sel.posX.reset();
		sel.posY.reset();
		sel.scale.reset();
	}
}
