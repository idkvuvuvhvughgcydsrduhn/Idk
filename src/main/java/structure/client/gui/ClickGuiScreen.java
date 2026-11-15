package structure.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import structure.client.StructureClient;
import structure.client.core.Category;
import structure.client.core.Mod;
import structure.client.core.Setting;
import structure.client.core.TriggerMod;
import structure.client.util.Input;

/**
 * ClickGUI. Input is polled (mouse + keyboard) so it works the same with touch
 * (Zalith maps touches to the mouse), a real mouse and a keyboard.
 */
public class ClickGuiScreen extends Screen {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    static final int BG = 0xF00B0714, PANEL = 0xFF120C1E, ROW = 0xFF150E22, ROW_H = 0xFF1E1233, SEL = 0xFF2A1646,
            ACC = 0xFF9D4DFF, ACC_D = 0xFF5B21B6, SILVER = 0xFFC8C8D2, GRAY = 0xFF8A8AA0, OFF = 0xFF3A3050;

    private Category cat = Category.COMBAT;
    private Mod sel, binding;
    private int scrollL, scrollS;
    private boolean pDown, rsHeld = true, focus;
    private String query = "", selCfg, status = "";
    private long statusUntil;
    private Setting dragging;
    private int dragX, dragW;
    private final boolean[] ks = new boolean[400], edges = new boolean[400];
    private int[] listBox = new int[4], setBox = new int[4];

    public ClickGuiScreen() {
        super(Text.literal("Structure Client"));
        selCfg = StructureClient.config.current;
        scanKeys();
    }

    @Override public boolean shouldPause() { return false; }

    public void close() {
        StructureClient.config.save(StructureClient.config.current);
        super.close();
    }

    public boolean mouseScrolled(double mx, double my, double h, double v) {
        int d = (int) (-v * 22);
        if (in((int) mx, (int) my, setBox[0], setBox[1], setBox[2], setBox[3])) scrollS += d; else scrollL += d;
        return true;
    }

    private static boolean in(int mx, int my, int x, int y, int w, int h) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    private void t(DrawContext c, String s, int x, int y, int col) { c.drawText(textRenderer, s, x, y, col, false); }
    private String fit(String s, int w) { return textRenderer.getWidth(s) <= w ? s : textRenderer.trimToWidth(s, Math.max(0, w - 6)) + ".."; }
    private void scanKeys() { for (int k = 32; k < 349; k++) { boolean d = Input.key(k); edges[k] = d && !ks[k]; ks[k] = d; } }
    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

    static int hsv(float h, float s, float v) {
        int i = (int) (h * 6); float f = h * 6 - i, p = v * (1 - s), q = v * (1 - f * s), t = v * (1 - (1 - f) * s), r, g, b;
        switch (i % 6) {
            case 0: r = v; g = t; b = p; break; case 1: r = q; g = v; b = p; break; case 2: r = p; g = v; b = t; break;
            case 3: r = p; g = q; b = v; break; case 4: r = t; g = p; b = v; break; default: r = v; g = p; b = q;
        }
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }

    private void toggleSwitch(DrawContext c, int x, int y, boolean on) {
        c.fill(x, y, x + 20, y + 10, on ? ACC : OFF);
        c.fill(on ? x + 11 : x + 1, y + 1, on ? x + 19 : x + 9, y + 9, 0xFFF0F0F8);
    }

    private boolean button(DrawContext c, int mx, int my, boolean click, int x, int y, int w, int h, String label, boolean hot) {
        boolean hov = in(mx, my, x, y, w, h);
        c.fill(x, y, x + w, y + h, hot ? ACC_D : hov ? ROW_H : ROW);
        c.drawBorder(x, y, w, h, hov ? ACC : 0xFF2E2244);
        t(c, label, x + (w - textRenderer.getWidth(label)) / 2, y + (h - 8) / 2, 0xFFFFFFFF);
        return hov && click;
    }

    /** Two scroll arrows; returns the scroll delta while held. */
    private int arrows(DrawContext c, int mx, int my, boolean down, int x, int y, int w) {
        int d = 0, bw = 26, cx = x + w / 2;
        for (int i = 0; i < 2; i++) {
            int bx = i == 0 ? cx - bw - 2 : cx + 2;
            boolean hov = in(mx, my, bx, y, bw, 14);
            c.fill(bx, y, bx + bw, y + 14, hov ? ACC_D : ROW);
            c.drawBorder(bx, y, bw, 14, 0xFF2E2244);
            t(c, i == 0 ? "\u25B2" : "\u25BC", bx + 9, y + 3, 0xFFFFFFFF);
            if (hov && down) d += i == 0 ? -4 : 4;
        }
        return d;
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        var mgr = StructureClient.modules;
        var cfg = StructureClient.config;
        boolean down = Input.mouse(0);
        boolean click = down && !pDown;
        pDown = down;
        scanKeys();
        boolean rs = Input.key(GLFW.GLFW_KEY_RIGHT_SHIFT);
        if (rs && !rsHeld) { close(); return; }
        rsHeld = rs;
        if (!down) dragging = null;
        if (click) focus = false;

        int sw = width, sh = height;
        int pw = Math.min(sw - 6, 580), ph = Math.min(sh - 6, 330);
        int px = (sw - pw) / 2, py = (sh - ph) / 2;
        boolean narrow = pw < 420;
        int sideW = narrow ? 58 : 84, setW = narrow ? 118 : 156;
        int top = py + 28, bot = py + ph - 26;
        int sx = px + 6, lx = sx + sideW + 4, setX = px + pw - setW - 6, lw = setX - 4 - lx;

        // ----- key capture (bind / search) -----
        if (binding != null) {
            for (int k = 32; k < 349; k++) {
                if (!edges[k] || k == GLFW.GLFW_KEY_RIGHT_SHIFT) continue;
                if (k == GLFW.GLFW_KEY_ESCAPE) { binding = null; break; }
                binding.key = (k == GLFW.GLFW_KEY_BACKSPACE || k == GLFW.GLFW_KEY_DELETE) ? -1 : k;
                binding = null;
                break;
            }
        } else if (focus) {
            for (int k = 32; k < 349; k++) {
                if (!edges[k]) continue;
                if (k == GLFW.GLFW_KEY_BACKSPACE && !query.isEmpty()) { query = query.substring(0, query.length() - 1); scrollL = 0; }
                else if (query.length() < 20 && ((k >= 65 && k <= 90) || (k >= 48 && k <= 57) || k == 32)) {
                    query += (char) (k >= 65 && k <= 90 ? k + 32 : k); scrollL = 0;
                }
            }
        }

        // ----- frame -----
        c.fill(0, 0, sw, sh, 0x99000000);
        c.fill(px, py, px + pw, py + ph, BG);
        c.drawBorder(px, py, pw, ph, ACC);
        c.fill(px + 6, py + 6, px + 20, py + 20, ACC_D);
        t(c, "S", px + 11, py + 9, 0xFFFFFFFF);
        t(c, "STRUCTURE", px + 25, py + 9, SILVER);
        t(c, "CLIENT", px + 25 + textRenderer.getWidth("STRUCTURE") + 4, py + 9, ACC);
        if (button(c, mx, my, click, px + pw - 18, py + 6, 12, 12, "x", false)) { close(); return; }

        // ----- sidebar -----
        int yy = top;
        for (Category cg : Category.values()) {
            boolean hov = in(mx, my, sx, yy, sideW, 20);
            c.fill(sx, yy, sx + sideW, yy + 20, cg == cat && query.isEmpty() ? ACC_D : hov ? ROW_H : ROW);
            t(c, fit(cg.label, sideW - 6), sx + 5, yy + 6, 0xFFFFFFFF);
            if (hov && click) { cat = cg; query = ""; scrollL = 0; }
            yy += 22;
        }
        if (button(c, mx, my, click, sx, yy + 2, sideW, 20, narrow ? "HUD" : "HUD Editor", false)) { mc.setScreen(new HudEditorScreen()); return; }

        // ----- search -----
        c.fill(lx, top, lx + lw, top + 16, PANEL);
        c.drawBorder(lx, top, lw, 16, focus ? ACC : 0xFF2E2244);
        if (in(mx, my, lx, top, lw, 16) && click) focus = true;
        String shown = query.isEmpty() && !focus ? "Search modules..." : query + (focus && (System.currentTimeMillis() / 500) % 2 == 0 ? "_" : "");
        t(c, fit(shown, lw - 8), lx + 4, top + 4, query.isEmpty() && !focus ? GRAY : 0xFFFFFFFF);

        // ----- module list -----
        List<Mod> ms = new ArrayList<>();
        if (query.isEmpty()) ms.addAll(mgr.in(cat));
        else for (Mod m : mgr.all) if (m.name.toLowerCase().contains(query)) ms.add(m);
        int ly = top + 20, lh = bot - 18 - ly;
        listBox = new int[]{lx, ly, lw, lh};
        c.fill(lx, ly, lx + lw, ly + lh, PANEL);
        scrollL = clamp(scrollL, 0, Math.max(0, ms.size() * 22 - lh));
        c.enableScissor(lx, ly, lx + lw, ly + lh);
        for (int i = 0; i < ms.size(); i++) {
            Mod m = ms.get(i);
            int ry = ly + i * 22 - scrollL;
            if (ry + 20 < ly || ry > ly + lh) continue;
            boolean hov = in(mx, my, lx, ry, lw, 20) && in(mx, my, lx, ly, lw, lh);
            c.fill(lx + 1, ry, lx + lw - 1, ry + 20, m == sel ? SEL : hov ? ROW_H : ROW);
            if (m.isEnabled()) c.fill(lx + 1, ry, lx + 3, ry + 20, ACC);
            t(c, fit(m.name, lw - 36), lx + 7, ry + 6, 0xFFFFFFFF);
            int swx = lx + lw - 25;
            if (m instanceof TriggerMod) t(c, ">", swx + 8, ry + 6, SILVER); else toggleSwitch(c, swx, ry + 5, m.isEnabled());
            if (hov && click) {
                if (in(mx, my, swx - 2, ry, 26, 20)) { m.toggle(); if (m instanceof TriggerMod) return; }
                else { sel = m; scrollS = 0; }
            }
        }
        c.disableScissor();
        scrollL += arrows(c, mx, my, down, lx, ly + lh + 2, lw);

        // ----- settings panel -----
        int sy = top, sH = bot - 18 - sy;
        setBox = new int[]{setX, sy, setW, sH};
        c.fill(setX, sy, setX + setW, sy + sH, PANEL);
        if (sel == null) {
            t(c, "Tap a module name", setX + 6, sy + 8, GRAY);
            t(c, "to edit settings", setX + 6, sy + 20, GRAY);
        } else {
            t(c, fit(sel.name, setW - 10), setX + 6, sy + 5, ACC);
            int dy = sy + 17;
            for (OrderedText ot : textRenderer.wrapLines(Text.literal(sel.description), setW - 12).subList(0, Math.min(2, textRenderer.wrapLines(Text.literal(sel.description), setW - 12).size()))) {
                c.drawText(textRenderer, ot, setX + 6, dy, GRAY, false); dy += 10;
            }
            int vy = dy + 3, vh = sy + sH - vy;
            c.fill(setX + 4, vy - 2, setX + setW - 4, vy - 1, 0xFF2E2244);
            int total = 22; for (Setting s : sel.settings) total += (s instanceof Setting.Num || s instanceof Setting.Color) ? 28 : 22;
            scrollS = clamp(scrollS, 0, Math.max(0, total - vh));
            boolean inView = in(mx, my, setX, vy, setW, vh);
            c.enableScissor(setX, vy, setX + setW, vy + vh);
            int ry = vy - scrollS;
            // bind row
            String bl = binding == sel ? "Press a key... (Bksp = clear)" : "Bind: " + Input.keyName(sel.key);
            if (button(c, mx, my, click && inView, setX + 6, ry, setW - 12, 18, fit(bl, setW - 16), binding == sel)) binding = sel;
            ry += 22;
            for (Setting s : sel.settings) {
                int rx = setX + 6, rw = setW - 12;
                if (s instanceof Setting.Bool b) {
                    t(c, fit(b.name, rw - 26), rx, ry + 5, 0xFFFFFFFF);
                    toggleSwitch(c, rx + rw - 20, ry + 4, b.value);
                    if (click && inView && in(mx, my, rx, ry, rw, 20)) b.value = !b.value;
                    ry += 22;
                } else if (s instanceof Setting.Mode m) {
                    t(c, fit(m.name, rw / 2 - 2), rx, ry + 5, 0xFFFFFFFF);
                    if (button(c, mx, my, click && inView, rx + rw / 2, ry, rw - rw / 2, 18, fit(m.get(), rw / 2 - 4), false)) m.cycle();
                    ry += 22;
                } else if (s instanceof Setting.Num n) {
                    String v = n.step >= 1 ? String.valueOf(n.i()) : (n.step < 0.1 ? String.format("%.2f", n.value) : String.format("%.1f", n.value));
                    t(c, fit(n.name, rw - 34), rx, ry + 2, 0xFFFFFFFF);
                    t(c, v, rx + rw - textRenderer.getWidth(v), ry + 2, SILVER);
                    int tx = rx, tw = rw, ty = ry + 15;
                    c.fill(tx, ty, tx + tw, ty + 6, OFF);
                    int fw = (int) (tw * (n.value - n.min) / (n.max - n.min));
                    c.fill(tx, ty, tx + fw, ty + 6, ACC);
                    c.fill(tx + fw - 1, ty - 2, tx + fw + 2, ty + 8, 0xFFF0F0F8);
                    if (click && inView && in(mx, my, tx, ty - 4, tw, 14)) { dragging = n; dragX = tx; dragW = tw; }
                    if (dragging == n && down) n.set(n.min + Math.max(0, Math.min(1, (mx - dragX) / (double) dragW)) * (n.max - n.min));
                    ry += 28;
                } else if (s instanceof Setting.Color cs) {
                    t(c, fit(cs.name, rw - 16), rx, ry + 2, 0xFFFFFFFF);
                    c.fill(rx + rw - 12, ry + 1, rx + rw, ry + 11, 0xFF000000 | cs.rgb);
                    int tx = rx, tw = rw, ty = ry + 14, seg = 48;
                    for (int i = 0; i < seg; i++) {
                        int x1 = tx + i * tw / seg, x2 = tx + (i + 1) * tw / seg;
                        float tt = (i + 0.5f) / seg;
                        int col = tt > 0.93f ? 0xFFFFFF : hsv(tt / 0.93f, 1f, 1f);
                        c.fill(x1, ty, x2, ty + 9, 0xFF000000 | col);
                    }
                    if (click && inView && in(mx, my, tx, ty - 2, tw, 13)) { dragging = cs; dragX = tx; dragW = tw; }
                    if (dragging == cs && down) {
                        float tt = Math.max(0f, Math.min(1f, (mx - dragX) / (float) dragW));
                        cs.rgb = tt > 0.93f ? 0xFFFFFF : hsv(tt / 0.93f, 1f, 1f);
                    }
                    ry += 28;
                }
            }
            c.disableScissor();
            scrollS += arrows(c, mx, my, down, setX, sy + sH + 2, setW);
        }

        // ----- config bar -----
        int cy = py + ph - 22;
        String[] labels = {"<", ">", "Save", "Load", "New", "Del"};
        int bw = narrow ? 26 : 34, bx0 = px + pw - 6 - labels.length * (bw + 2) + 2;
        boolean show = System.currentTimeMillis() < statusUntil;
        t(c, fit(show ? status : "Config: " + selCfg, bx0 - px - 12), px + 8, cy + 5, show ? ACC : SILVER);
        for (int i = 0; i < labels.length; i++) {
            if (!button(c, mx, my, click, bx0 + i * (bw + 2), cy, bw, 18, labels[i], false)) continue;
            List<String> names = cfg.list();
            switch (i) {
                case 0, 1 -> { int idx = names.indexOf(selCfg); if (idx < 0) idx = 0; selCfg = names.get((idx + (i == 0 ? -1 : 1) + names.size()) % names.size()); }
                case 2 -> say(cfg.save(selCfg) ? "Saved " + selCfg : "Save failed");
                case 3 -> say(cfg.load(selCfg) ? "Loaded " + selCfg : "No such config");
                case 4 -> { selCfg = cfg.create(); say("Created " + selCfg); }
                case 5 -> { String d = selCfg; if (cfg.delete(d)) { selCfg = "default"; say("Deleted " + d); } else say("Can't delete that"); }
            }
        }
    }

    private void say(String s) { status = s; statusUntil = System.currentTimeMillis() + 2500; }
}
