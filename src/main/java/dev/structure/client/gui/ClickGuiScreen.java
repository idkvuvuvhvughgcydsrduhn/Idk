package dev.structure.client.gui;

import dev.structure.client.StructureClient;
import dev.structure.client.module.Category;
import dev.structure.client.module.Module;
import dev.structure.client.module.ModuleManager;
import dev.structure.client.module.utility.ClientSettings;
import dev.structure.client.setting.BoolSetting;
import dev.structure.client.setting.ColorSetting;
import dev.structure.client.setting.KeySetting;
import dev.structure.client.setting.ModeSetting;
import dev.structure.client.setting.NumberSetting;
import dev.structure.client.setting.Setting;
import dev.structure.client.util.Draw;
import dev.structure.client.util.Input;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * ClickGUI. Works with mouse, touch (Zalith forwards touches as mouse events) and keyboard.
 * Layout is computed from the window size every frame, so it scales from phones to desktop.
 */
public class ClickGuiScreen extends Screen {
	private final Hits hits = new Hits();
	private Category cat = Category.COMBAT;
	private Module sel;
	private Setting<?> dragging;
	private Module binding;
	private final TextBox search = new TextBox("Search modules...", 24);
	private final TextBox cfgName = new TextBox("config name", 24);
	private boolean searchFocus;
	private boolean cfgFocus;
	private int scrollList;
	private int scrollSet;
	private String status = "";
	private long statusUntil;
	private String cfgSel;

	public ClickGuiScreen() {
		super(Text.literal("Structure Client"));
		cfgSel = dev.structure.client.config.ConfigManager.current();
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	private void say(String s) {
		status = s;
		statusUntil = System.currentTimeMillis() + 2500;
	}

	// ---------- layout ----------
	private int pw, ph, px, py, sideW, setW, lx, lw, setX, top, bot;
	private boolean narrow;

	private void layout() {
		int sw = width, sh = height;
		pw = Math.min(sw - 6, 580);
		ph = Math.min(sh - 6, 330);
		px = (sw - pw) / 2;
		py = (sh - ph) / 2;
		narrow = pw < 420;
		sideW = narrow ? 58 : 84;
		setW = narrow ? 118 : 156;
		top = py + 28;
		bot = py + ph - 26;
		int sx = px + 6;
		setX = px + pw - setW - 6;
		lx = sx + sideW + 4;
		lw = setX - 4 - lx;
	}

	// ---------- input ----------
	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		return handleClick(click.x(), click.y());
	}

	@Override
	public boolean mouseReleased(Click click) {
		dragging = null;
		return super.mouseReleased(click);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double h, double v) {
		int d = (int) (-v * 22);
		if (Draw.inside(mx, my, setX, top, setW, bot - top)) scrollSet += d;
		else scrollList += d;
		return true;
	}

	@Override
	public boolean charTyped(net.minecraft.client.input.CharInput input) {
		int cp = input.codepoint();
		String s = new String(Character.toChars(cp));
		if (binding != null) return true;
		if (searchFocus) {
			search.type(s);
			scrollList = 0;
			return true;
		}
		if (cfgFocus) {
			cfgName.type(s);
			return true;
		}
		return super.charTyped(input);
	}

	@Override
	public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
		int k = input.key();
		if (binding != null) {
			if (k == GLFW.GLFW_KEY_ESCAPE) binding = null;
			else if (k == GLFW.GLFW_KEY_BACKSPACE || k == GLFW.GLFW_KEY_DELETE) {
				binding.bind.set(-1);
				binding = null;
			} else {
				binding.bind.set(k);
				binding = null;
			}
			return true;
		}
		if (searchFocus) {
			if (k == GLFW.GLFW_KEY_BACKSPACE) {
				search.backspace();
				scrollList = 0;
				return true;
			}
			if (k == GLFW.GLFW_KEY_ENTER || k == GLFW.GLFW_KEY_ESCAPE) {
				searchFocus = false;
				return true;
			}
		}
		if (cfgFocus) {
			if (k == GLFW.GLFW_KEY_BACKSPACE) {
				cfgName.backspace();
				return true;
			}
			if (k == GLFW.GLFW_KEY_ENTER) {
				doSaveAs();
				return true;
			}
			if (k == GLFW.GLFW_KEY_ESCAPE) {
				cfgFocus = false;
				return true;
			}
		}
		return super.keyPressed(input);
	}

	private void doSaveAs() {
		String n = dev.structure.client.config.ConfigManager.clean(cfgName.text);
		if (n.isEmpty()) {
			say("Type a name first");
			return;
		}
		if (dev.structure.client.config.ConfigManager.saveAs(n)) {
			cfgSel = n;
			cfgName.text = "";
			cfgFocus = false;
			say("Saved as " + n);
		} else say("Save failed");
	}

	private boolean handleClick(double mx, double my) {
		lastX = mx;
		lastY = my;
		Hits.Hit h = hits.at(mx, my);
		if (h == null) {
			searchFocus = false;
			cfgFocus = false;
			return true;
		}
		if (h.drag != null) {
			dragging = null;
			h.drag.apply(mx, my);
			return true;
		}
		if (h.tap != null) h.tap.run();
		return true;
	}

	// ---------- render ----------
	@Override
	public void render(DrawContext ctx, int mx, int my, float delta) {
		layout();
		hits.clear();
		mouseX = mx;
		mouseY = my;
		// continuous drag of sliders / colour pickers
		if (dragging != null) {
			double[] p = currentMouse();
			applyDrag(p[0], p[1]);
		}
		int sw = width, sh = height;
		ctx.fill(0, 0, sw, sh, 0x99000000);
		ctx.fill(px, py, px + pw, py + ph, Theme.BG);
		Draw.outline(ctx, px, py, pw, ph, Theme.accent());
		drawHeader(ctx);
		drawSidebar(ctx);
		drawModules(ctx);
		drawSettings(ctx);
		drawConfigBar(ctx);
	}

	private double[] currentMouse() {
		var win = net.minecraft.client.MinecraftClient.getInstance().getWindow();
		double[] x = new double[1];
		double[] y = new double[1];
		org.lwjgl.glfw.GLFW.glfwGetCursorPos(win.getHandle(), x, y);
		return new double[] {x[0] * width / (double) win.getWidth(), y[0] * height / (double) win.getHeight()};
	}

	private void applyDrag(double mx, double my) {
		if (dragging instanceof NumberSetting n) {
			int tx = setX + 6, tw = setW - 12;
			double t = Math.max(0, Math.min(1, (mx - tx) / (double) tw));
			n.set(n.min + t * (n.max - n.min));
		} else if (dragging instanceof ColorSetting cs) {
			// dragging state holds the picker geometry via dragRect
			if (dragRect != null) {
				if (dragRect[4] == 0) ColorPicker.dragSv(cs, dragRect[0], dragRect[1], dragRect[2], dragRect[3], mx, my);
				else ColorPicker.dragHue(cs, dragRect[1], dragRect[3], my);
			}
		}
	}

	private int[] dragRect;

	private void drawHeader(DrawContext ctx) {
		int acc = Theme.accent();
		ctx.fill(px + 6, py + 6, px + 20, py + 20, Draw.darken(acc, 0.5f));
		Draw.textBold(ctx, "S", px + 11, py + 9, 0xFFFFFFFF);
		Draw.textBold(ctx, "STRUCTURE", px + 25, py + 9, Theme.SILVER);
		Draw.textBold(ctx, "CLIENT", px + 25 + Draw.widthBold("STRUCTURE ") , py + 9, acc);
		int bx = px + pw - 18;
		drawButton(ctx, bx, py + 6, 12, 12, "x", false, () -> close());
	}

	private void drawButton(DrawContext ctx, int x, int y, int w, int h, String label, boolean hot, Runnable tap) {
		boolean hov = Draw.inside(mouseX, mouseY, x, y, w, h);
		int acc = Theme.accent();
		ctx.fill(x, y, x + w, y + h, hot ? Draw.darken(acc, 0.5f) : hov ? Theme.ROW_H : Theme.ROW);
		Draw.outline(ctx, x, y, w, h, hov || hot ? acc : Theme.LINE);
		Draw.textCenter(ctx, label, x + w / 2, y + (h - 8) / 2, 0xFFFFFFFF);
		hits.add(x, y, w, h, tap);
	}

	private void drawSidebar(DrawContext ctx) {
		int sx = px + 6;
		int yy = top;
		int acc = Theme.accent();
		for (Category cg : Category.values()) {
			boolean on = cg == cat && search.text.isEmpty();
			boolean hov = Draw.inside(mouseX, mouseY, sx, yy, sideW, 20);
			ctx.fill(sx, yy, sx + sideW, yy + 20, on ? Draw.darken(acc, 0.5f) : hov ? Theme.ROW_H : Theme.ROW);
			if (on) ctx.fill(sx, yy, sx + 2, yy + 20, acc);
			Draw.text(ctx, Draw.fit(cg.label, sideW - 8), sx + 6, yy + 6, 0xFFFFFFFF);
			final Category c = cg;
			hits.add(sx, yy, sideW, 20, () -> {
				cat = c;
				search.text = "";
				scrollList = 0;
			});
			yy += 22;
		}
		drawButton(ctx, sx, yy + 2, sideW, 20, narrow ? "HUD" : "HUD Editor", false,
			() -> client.setScreen(new HudEditorScreen(this)));
	}

	private void drawModules(DrawContext ctx) {
		int acc = Theme.accent();
		// search bar
		int sy = top;
		search.draw(ctx, lx, sy, lw, 16, searchFocus);
		hits.add(lx, sy, lw, 16, () -> {
			searchFocus = true;
			cfgFocus = false;
		});

		List<Module> ms = new ArrayList<>();
		String q = search.text.toLowerCase();
		if (q.isEmpty()) ms.addAll(ModuleManager.in(cat));
		else for (Module m : ModuleManager.all()) if (m.name.toLowerCase().contains(q)) ms.add(m);

		int ly = top + 20;
		int lh = bot - 18 - ly;
		ctx.fill(lx, ly, lx + lw, ly + lh, Theme.PANEL);
		int rowH = 22;
		scrollList = Math.max(0, Math.min(scrollList, Math.max(0, ms.size() * rowH - lh)));
		ctx.enableScissor(lx, ly, lx + lw, ly + lh);
		hits.setClip(lx, ly, lx + lw, ly + lh);
		for (int i = 0; i < ms.size(); i++) {
			Module m = ms.get(i);
			int ry = ly + i * rowH - scrollList;
			if (ry + 20 < ly || ry > ly + lh) continue;
			boolean hov = Draw.inside(mouseX, mouseY, lx, ry, lw, 20);
			ctx.fill(lx + 1, ry, lx + lw - 1, ry + 20, m == sel ? Theme.SEL : hov ? Theme.ROW_H : Theme.ROW);
			if (m.isEnabled()) ctx.fill(lx + 1, ry, lx + 3, ry + 20, acc);
			Draw.text(ctx, Draw.fit(m.name, lw - 60), lx + 7, ry + 6, 0xFFFFFFFF);
			String info = m.info();
			if (!info.isEmpty() && !narrow) Draw.textRight(ctx, Draw.fit(info, 60), lx + lw - 34, ry + 6, Theme.DIM);
			int swx = lx + lw - 25;
			if (m instanceof dev.structure.client.module.ActionModule) {
				Draw.text(ctx, ">", swx + 8, ry + 6, Theme.SILVER);
			} else {
				Draw.toggle(ctx, swx, ry + 5, 20, 10, m.isEnabled(), acc);
			}
			final Module mm = m;
			// row first, toggle second: Hits.at() returns the last-added (topmost) region
			hits.add(lx, ry, lw, 20, () -> {
				sel = mm;
				scrollSet = 0;
			});
			hits.add(swx - 4, ry, 30, 20, () -> mm.toggle());
		}
		hits.clearClip();
		ctx.disableScissor();
		drawArrows(ctx, lx, ly + lh + 2, lw, () -> scrollList -= 44, () -> scrollList += 44);
	}

	private void drawArrows(DrawContext ctx, int x, int y, int w, Runnable up, Runnable down) {
		int bw = 26, cx = x + w / 2;
		drawButton(ctx, cx - bw - 2, y, bw, 14, "^", false, up);
		drawButton(ctx, cx + 2, y, bw, 14, "v", false, down);
	}

	private double lastX, lastY;
	private int mouseX, mouseY;

	private void drawSettings(DrawContext ctx) {
		int sy = top;
		int sH = bot - 18 - sy;
		ctx.fill(setX, sy, setX + setW, sy + sH, Theme.PANEL);
		Draw.outline(ctx, setX, sy, setW, sH, Theme.LINE);
		if (sel == null) {
			Draw.text(ctx, "Tap a module name", setX + 6, sy + 8, Theme.DIM);
			Draw.text(ctx, "to edit its settings", setX + 6, sy + 20, Theme.DIM);
			return;
		}
		Draw.text(ctx, Draw.fit(sel.name, setW - 10), setX + 6, sy + 5, Theme.accent());
		int dy = sy + 17;
		for (String line : wrap(sel.description, setW - 12, 2)) {
			Draw.text(ctx, line, setX + 6, dy, Theme.DIM);
			dy += 10;
		}
		int vy = dy + 3;
		int vh = sy + sH - vy;
		ctx.fill(setX + 4, vy - 2, setX + setW - 4, vy - 1, Theme.LINE);
		int total = 22;
		List<Setting<?>> vis = new ArrayList<>();
		for (Setting<?> s : sel.settings()) if (s.isVisible()) vis.add(s);
		for (Setting<?> s : vis) total += (s instanceof NumberSetting || s instanceof ColorSetting) ? 30 : 22;
		scrollSet = Math.max(0, Math.min(scrollSet, Math.max(0, total - vh)));
		ctx.enableScissor(setX, vy, setX + setW, vy + vh);
		hits.setClip(setX, vy, setX + setW, vy + vh);
		int ry = vy - scrollSet;
		int rx = setX + 6, rw = setW - 12;

		String bl = binding == sel ? "Press a key... (Esc cancel)" : "Bind: " + sel.bind.display();
		drawButton(ctx, rx, ry, rw, 18, Draw.fit(bl, rw - 6), binding == sel, () -> binding = sel);
		ry += 22;

		for (Setting<?> s : vis) {
			if (s instanceof BoolSetting b) {
				Draw.text(ctx, Draw.fit(b.name, rw - 26), rx, ry + 5, 0xFFFFFFFF);
				Draw.toggle(ctx, rx + rw - 20, ry + 4, 20, 10, b.on(), Theme.accent());
				hits.add(rx, ry, rw, 20, () -> b.toggle());
				ry += 22;
			} else if (s instanceof ModeSetting m) {
				Draw.text(ctx, Draw.fit(m.name, rw / 2 - 2), rx, ry + 5, 0xFFFFFFFF);
				drawButton(ctx, rx + rw / 2, ry, rw - rw / 2, 18, Draw.fit(m.get(), rw / 2 - 6), false, m::cycle);
				ry += 22;
			} else if (s instanceof NumberSetting n) {
				Draw.text(ctx, Draw.fit(n.name, rw - 40), rx, ry + 2, 0xFFFFFFFF);
				String v = n.display();
				Draw.textRight(ctx, v, rx + rw, ry + 2, Theme.SILVER);
				int ty = ry + 15;
				ctx.fill(rx, ty, rx + rw, ty + 6, Theme.OFF);
				int fw = (int) Math.round(rw * n.progress());
				ctx.fill(rx, ty, rx + fw, ty + 6, Theme.accent());
				ctx.fill(rx + fw - 1, ty - 2, rx + fw + 2, ty + 8, 0xFFF0F0F8);
				hits.addDrag(rx, ty - 4, rw, 14, (x, y) -> {
					dragging = n;
					dragRect = null;
					applyDrag(x, y);
				});
				ry += 30;
			} else if (s instanceof ColorSetting cs) {
				Draw.text(ctx, Draw.fit(cs.name, rw - 16), rx, ry + 2, 0xFFFFFFFF);
				ctx.fill(rx + rw - 12, ry + 1, rx + rw, ry + 11, cs.argb());
				int ty = ry + 14;
				int ph2 = 26;
				ColorPicker.draw(ctx, cs, rx, ty, rw, ph2);
				int[] rect = {rx, ty, ColorPicker.svWidth(rw), ph2};
				hits.addDrag(rx, ty, ColorPicker.svWidth(rw), ph2, (x, y) -> {
					dragging = cs;
					dragRect = new int[] {rect[0], rect[1], rect[2], rect[3], 0};
					ColorPicker.dragSv(cs, rect[0], rect[1], rect[2], rect[3], x, y);
				});
				int hx = rx + ColorPicker.svWidth(rw) + ColorPicker.GAP;
				hits.addDrag(hx - 2, ty, ColorPicker.HUE_W + 4, ph2, (x, y) -> {
					dragging = cs;
					dragRect = new int[] {hx, ty, ColorPicker.HUE_W, ph2, 1};
					ColorPicker.dragHue(cs, ty, ph2, y);
				});
				ry += 22 + ph2 - 14;
			} else if (s instanceof KeySetting k) {
				Draw.text(ctx, Draw.fit(k.name, rw - 60), rx, ry + 5, 0xFFFFFFFF);
				Draw.textRight(ctx, k.display(), rx + rw, ry + 5, Theme.SILVER);
				ry += 22;
			}
		}
		hits.clearClip();
		ctx.disableScissor();
		drawArrows(ctx, setX, sy + sH + 2, setW, () -> scrollSet -= 44, () -> scrollSet += 44);
	}

	private void drawConfigBar(DrawContext ctx) {
		int cy = py + ph - 22;
		int acc = Theme.accent();
		boolean show = System.currentTimeMillis() < statusUntil;
		String left = show ? status : "Config: " + dev.structure.client.config.ConfigManager.current();
		Draw.text(ctx, Draw.fit(left, Math.max(40, px + pw - setX - 12)), px + 8, cy + 5, show ? acc : Theme.SILVER);
		int bw = narrow ? 30 : 36;
		int gap = 2;
		String[] labels = {"<", ">", "Load", "Save", "Del"};
		int bx = setX;
		for (int i = 0; i < labels.length; i++) {
			final int idx = i;
			drawButton(ctx, bx + i * (bw + gap), cy, bw, 18, labels[i], false, () -> configAction(idx));
		}
		// name field (typed name -> Save As)
		int fx = px + 8;
		int fw = Math.max(60, setX - fx - 8);
		// shown in the status row; clicking the bar text opens name entry
		int ny = cy - 22;
		if (ny > top + 10) {
			cfgName.draw(ctx, px + 8, ny + 2, Math.min(fw, 140), 16, cfgFocus);
			hits.add(px + 8, ny + 2, Math.min(fw, 140), 16, () -> {
				cfgFocus = true;
				searchFocus = false;
			});
		}
	}

	private void configAction(int i) {
		var names = dev.structure.client.config.ConfigManager.list();
		switch (i) {
			case 0, 1 -> {
				int idx = names.indexOf(cfgSel);
				if (idx < 0) idx = 0;
				if (!names.isEmpty()) cfgSel = names.get((idx + (i == 0 ? -1 : 1) + names.size()) % names.size());
			}
			case 2 -> say(dev.structure.client.config.ConfigManager.load(cfgSel) ? "Loaded " + cfgSel : "Load failed");
			case 3 -> say(dev.structure.client.config.ConfigManager.save(dev.structure.client.config.ConfigManager.current()) ? "Saved" : "Save failed");
			case 4 -> {
				String d = cfgSel;
				if (dev.structure.client.config.ConfigManager.delete(d)) {
					say("Deleted " + d);
					cfgSel = dev.structure.client.config.ConfigManager.current();
				} else say("Can't delete that");
			}
			default -> {
			}
		}
	}

	private static List<String> wrap(String s, int maxW, int maxLines) {
		List<String> out = new ArrayList<>();
		if (s == null) return out;
		StringBuilder cur = new StringBuilder();
		for (String word : s.split(" ")) {
			String t = cur.length() == 0 ? word : cur + " " + word;
			if (Draw.width(t) > maxW && cur.length() > 0) {
				out.add(cur.toString());
				cur = new StringBuilder(word);
				if (out.size() >= maxLines) return out;
			} else cur = new StringBuilder(t);
		}
		if (cur.length() > 0 && out.size() < maxLines) out.add(cur.toString());
		return out;
	}

	@Override
	public void close() {
		dev.structure.client.config.ConfigManager.saveCurrent();
		super.close();
	}
}
