package structure.client.gui;

import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import structure.client.StructureClient;
import structure.client.core.HudMod;
import structure.client.util.Input;

/** Drag HUD elements, resize them, change their color. Positions are saved as screen fractions. */
public class HudEditorScreen extends Screen {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final int[] PRESETS = {0xA855F7, 0xC8C8D2, 0xFFFFFF, 0x38BDF8, 0x4ADE80, 0xF87171, 0xFBBF24};
    private HudMod sel, drag;
    private int offX, offY;
    private boolean pDown;

    public HudEditorScreen() {
        super(Text.literal("HUD Editor"));
        HudMod.editing = true;
    }

    @Override public boolean shouldPause() { return false; }

    @Override
    public void removed() {
        HudMod.editing = false;
    }

    @Override
    public void close() {
        HudMod.editing = false;
        StructureClient.config.save(StructureClient.config.current);
        super.close();
    }

    private boolean in(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private void border(DrawContext c, int x, int y, int w, int h, int col) {
        c.fill(x, y, x + w, y + 1, col);           // top
        c.fill(x, y + h - 1, x + w, y + h, col);   // bottom
        c.fill(x, y, x + 1, y + h, col);           // left
        c.fill(x + w - 1, y, x + w, y + h, col);   // right
    }

    private boolean button(DrawContext c, int mx, int my, boolean click, int x, int y, int w, int h, String l, boolean hot) {
        boolean hov = in(mx, my, x, y, w, h);
        c.fill(x, y, x + w, y + h, hot ? 0xFF5B21B6 : hov ? 0xFF1E1233 : 0xFF150E22);
        border(c, x, y, w, h, hov || hot ? 0xFF9D4DFF : 0xFF2E2244);
        c.drawText(textRenderer, l, x + (w - textRenderer.getWidth(l)) / 2, y + (h - 8) / 2, 0xFFFFFFFF, false);
        return hov && click;
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        boolean down = Input.mouse(0);
        boolean click = down && !pDown;
        pDown = down;
        if (!down) drag = null;

        int sw = width, sh = height;
        c.fill(0, 0, sw, sh, 0x44000000);

        // chips: tap to enable/disable HUD elements
        List<HudMod> huds = StructureClient.modules.huds();
        int cx = 4, cy = 4;
        boolean chipHit = false;
        for (HudMod h : huds) {
            if (h == null) continue;
            int w = textRenderer.getWidth(h.name) + 10;
            if (cx + w > sw - 4) { cx = 4; cy += 16; }
            if (button(c, mx, my, click, cx, cy, w, 14, h.name, h.isEnabled())) {
                h.toggle();
                chipHit = true;
            }
            if (in(mx, my, cx, cy, w, 14)) chipHit = true;
            cx += w + 3;
        }

        // bottom bar
        int by = sh - 22;
        boolean barHit = my >= by - 2;
        if (sel != null) {
            c.drawText(textRenderer, sel.name, 6, by - 12, 0xFFC8C8D2, true);
            if (button(c, mx, my, click, 6, by, 40, 18, "Size -", false)) {
                sel.scale.set(Math.max(0.5, sel.scale.value - 0.1));
            }
            if (button(c, mx, my, click, 50, by, 40, 18, "Size +", false)) {
                sel.scale.set(Math.min(3.0, sel.scale.value + 0.1));
            }
            if (button(c, mx, my, click, 94, by, 44, 18, "Color", false)) {
                int idx = 0;
                for (int i = 0; i < PRESETS.length; i++) {
                    if (PRESETS[i] == sel.color.rgb) { idx = i; break; }
                }
                sel.color.rgb = PRESETS[(idx + 1) % PRESETS.length];
            }
        } else {
            c.drawText(textRenderer, "Drag an element. Tap names above to show/hide.", 6, by + 5, 0xFF8A8AA0, true);
        }
        if (button(c, mx, my, click, sw - 50, by, 44, 18, "Done", false)) {
            close();
            return;
        }

        // dragging
        if (click && !chipHit && !barHit) {
            sel = null;
            for (int i = huds.size() - 1; i >= 0; i--) {
                HudMod h = huds.get(i);
                if (h == null || !h.isEnabled()) continue;
                int hw = (int) (h.w * h.scale.value);
                int hh = (int) (h.h * h.scale.value);
                if (in(mx, my, h.bx, h.by, hw, hh)) {
                    sel = h;
                    drag = h;
                    offX = mx - h.bx;
                    offY = my - h.by;
                    break;
                }
            }
        }
        if (drag != null && down) {
            double fw = Math.max(1, sw - drag.w * drag.scale.value);
            double fh = Math.max(1, sh - drag.h * drag.scale.value);
            drag.x.set((mx - offX) / fw);
            drag.y.set((my - offY) / fh);
        }

        // outlines
        for (HudMod h : huds) {
            if (h == null || !h.isEnabled()) continue;
            int hw = (int) (h.w * h.scale.value);
            int hh = (int) (h.h * h.scale.value);
            border(c, h.bx - 1, h.by - 1, hw + 2, hh + 2, h == sel ? 0xFFFFFFFF : 0xFF9D4DFF);
        }
    }
}
