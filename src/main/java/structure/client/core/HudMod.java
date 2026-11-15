package structure.client.core;

import java.util.List;
import net.minecraft.client.gui.DrawContext;

/** A module drawn on the HUD. Position is stored as a 0..1 fraction so it fits every screen size. */
public abstract class HudMod extends Mod {
    public static boolean editing;
    public final Setting.Num x, y, scale;
    public final Setting.Color color;
    public int w = 50, h = 10, bx, by;

    protected HudMod(String n, String d, Category c, double dx, double dy, int rgb) {
        super(n, d, c);
        x = num("HUD X", dx, 0, 1, 0.001);
        y = num("HUD Y", dy, 0, 1, 0.001);
        scale = num("HUD Scale", 1, 0.5, 3, 0.1);
        color = color("HUD Color", rgb);
    }

    public List<String> lines() { return List.of(); }

    protected void body(DrawContext c) {
        List<String> ls = lines();
        if (ls.isEmpty()) {
            if (!editing) { w = 0; h = 0; return; }
            ls = List.of(name);
        }
        var tr = mc.textRenderer;
        int mw = 30;
        for (String s : ls) mw = Math.max(mw, tr.getWidth(s));
        w = mw + 8; h = ls.size() * 10 + 4;
        c.fill(0, 0, w, h, 0x99000000);
        c.fill(0, 0, 2, h, 0xFF000000 | color.rgb);
        int yy = 3;
        for (String s : ls) { c.drawText(tr, s, 5, yy, 0xFFFFFFFF, true); yy += 10; }
    }

    public final void draw(DrawContext c) {
        int sw = c.getScaledWindowWidth(), sh = c.getScaledWindowHeight();
        float sc = (float) scale.value;
        bx = (int) (x.value * Math.max(0, sw - w * sc));
        by = (int) (y.value * Math.max(0, sh - h * sc));
        var m = c.getMatrices();
        m.pushMatrix();
        m.translate(bx, by);
        m.scale(sc, sc);
        body(c);
        m.popMatrix();
    }
}
