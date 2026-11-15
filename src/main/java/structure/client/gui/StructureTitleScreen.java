package structure.client.gui;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import structure.client.StructureClient;
import structure.client.core.Mod;
import structure.client.modules.PlayerModules;
import structure.client.modules.UtilityModules;
import structure.client.util.Input;

/** Structure Client main menu. Everything is drawn in code (no image assets), scaled to any screen size. */
public class StructureTitleScreen extends Screen {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final int ACC = 0xFF9D4DFF, ACC_D = 0xFF5B21B6, SILVER = 0xFFD4D4DE, SOFT = 0xFFB9A9D6;
    private static final String[] ICONS = {"\u25B6", "\u25CE", "\u2726", "\u2699", "\u263B", "\u2716"};
    private static final String[] LABELS = {"Singleplayer", "Multiplayer", "Client Settings", "Settings", "Accounts", "Quit Game"};
    private static final String[][] SLIDES = {
            {"Welcome to", "Structure Client!", "Enjoy your game!"},
            {"Press Right Shift", "to open the ClickGUI", "Panic key: Delete"},
            {"Tap a module name", "to edit its settings", "Switch = on / off"}};

    private final float[][] stars = new float[70][3], towers = new float[34][4], pix = new float[170][4];
    private boolean pDown, rsHeld;
    private String toast = "";
    private long toastUntil;

    public StructureTitleScreen() {
        super(Text.literal("Structure Client"));
        Random r = new Random(7);
        for (float[] s : stars) { s[0] = r.nextFloat(); s[1] = r.nextFloat() * 0.6f; s[2] = 0.3f + r.nextFloat() * 0.7f; }
        for (float[] t : towers) { t[0] = 0.40f + r.nextFloat() * 0.60f; t[1] = r.nextFloat(); t[2] = 0.08f + r.nextFloat() * 0.34f; t[3] = r.nextFloat(); }
        for (float[] p : pix) { p[0] = r.nextFloat() * 0.26f; p[1] = 0.12f + r.nextFloat() * 0.42f; p[2] = r.nextFloat(); p[3] = r.nextFloat(); }
        rsHeld = Input.key(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    @Override public boolean shouldPause() { return false; }
    @Override public boolean shouldCloseOnEsc() { return false; }

    private static boolean in(int mx, int my, int x, int y, int w, int h) { return mx >= x && mx < x + w && my >= y && my < y + h; }

    private void border(DrawContext c, int x, int y, int w, int h, int col) {
        c.fill(x, y, x + w, y + 1, col);           // top
        c.fill(x, y + h - 1, x + w, y + h, col);   // bottom
        c.fill(x, y, x + 1, y + h, col);           // left
        c.fill(x + w - 1, y, x + w, y + h, col);   // right
    }

    private void txt(DrawContext c, String s, int x, int y, int col) { c.drawText(textRenderer, s, x, y, col, false); }
    private void panel(DrawContext c, int x, int y, int w, int h) { c.fill(x, y, x + w, y + h, 0xCC10051C); border(c, x, y, w, h, 0xFF3B1068); }

    private void big(DrawContext c, String s, float cx, float y, float sc, int col, int shadow) {
        int w = textRenderer.getWidth(s);
        var m = c.getMatrices();
        m.pushMatrix();
        m.translate(cx - w * sc / 2f, y);
        m.scale(sc, sc);
        c.drawText(textRenderer, s, 1, 1, shadow, false);
        c.drawText(textRenderer, s, 0, 0, col, false);
        m.popMatrix();
    }

    private void toggle(DrawContext c, int x, int y, boolean on) {
        c.fill(x, y, x + 20, y + 10, on ? ACC : 0xFF3A3050);
        c.fill(on ? x + 11 : x + 1, y + 1, on ? x + 19 : x + 9, y + 9, 0xFFF0F0F8);
    }

    private void say(String s) { toast = s; toastUntil = System.currentTimeMillis() + 2500; }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        boolean down = Input.mouse(0), click = down && !pDown;
        pDown = down;
        boolean rs = Input.key(GLFW.GLFW_KEY_RIGHT_SHIFT);
        if (rs && !rsHeld) { mc.setScreen(new ClickGuiScreen()); return; }
        rsHeld = rs;
        int sw = width, sh = height;
        long now = System.currentTimeMillis();

        // ---- background (all procedural) ----
        c.fillGradient(0, 0, sw, sh / 2, 0xFF0F031C, 0xFF2A0B4A);
        c.fillGradient(0, sh / 2, sw, sh, 0xFF2A0B4A, 0xFF0A0214);
        for (float[] s : stars) {
            int a = (int) (255 * s[2]);
            c.fill((int) (s[0] * sw), (int) (s[1] * sh), (int) (s[0] * sw) + 1, (int) (s[1] * sh) + 1, (a << 24) | 0xE9D5FF);
        }
        int ms = Math.max(8, sh / 9), mxp = (int) (sw * 0.78f), myp = (int) (sh * 0.08f);
        c.fill(mxp, myp, mxp + ms, myp + ms, 0xFFE9D5FF);
        c.fill(mxp + 2, myp + 2, mxp + ms - 2, myp + ms - 2, 0xFFC4A1F5);
        for (float[] t : towers) {
            int x = (int) (t[0] * sw), w = 3 + (int) (t[1] * 10), h = (int) (t[2] * sh);
            c.fill(x, sh - h, x + w, sh, 0xFF14062A);
            c.fill(x, sh - h, x + 1, sh, 0xFF2B0F52);
            if (t[3] > 0.6f) c.fill(x + w / 2, sh - h + h / 3, x + w / 2 + 1, sh - h + h / 3 + 2, ACC);
        }
        int[] leaf = {0xFF6D28D9, 0xFF7E22CE, 0xFF4C1D95, 0xFF9333EA};
        for (float[] p : pix) {
            int s = 2 + (int) (p[2] * 4);
            c.fill((int) (p[0] * sw), (int) (p[1] * sh), (int) (p[0] * sw) + s, (int) (p[1] * sh) + s, leaf[(int) (p[3] * 3.99f)]);
        }
        c.fillGradient(0, sh - sh / 5, sw, sh, 0x00000000, 0xAA000000);

        // ---- layout ----
        int bw = Math.min(220, (int) (sw * 0.44f));
        int bh = (int) Math.min(26, sh * 0.52 / 6.5);
        int gap = Math.max(2, (int) (bh * 0.2));
        int stack = 6 * bh + 5 * gap;
        int by0 = sh - 22 - stack;

        // ---- logo ----
        float cx = sw / 2f;
        int w1 = textRenderer.getWidth("STRUCTURE");
        float avail = by0 - 8 - 12;
        float sc = Math.max(1f, Math.min(sw * 0.40f / w1, (avail - 28) / 20f));
        int u = Math.max(1, Math.round(sc * 0.6f));
        int cy = 12;
        c.fill((int) cx - 6 * u, cy + 5 * u, (int) cx + 6 * u, cy + 7 * u, 0xFFC8A2FF);
        for (int i = -1; i <= 1; i++) c.fill((int) cx + i * 5 * u - u, cy + u, (int) cx + i * 5 * u + u, cy + 5 * u, 0xFFC8A2FF);
        float l1 = cy + 8 * u;
        big(c, "§lSTRUCTURE", cx, l1, sc, SILVER, 0xFF3B1068);
        float l2 = l1 + 9 * sc;
        big(c, "§l§oCLIENT", cx, l2, sc * 1.05f, ACC, 0xFF1E0646);
        String tagline = "P L A Y  •  C H E A T  •  D O M I N A T E";
        txt(c, tagline, (int) cx - textRenderer.getWidth(tagline) / 2, (int) (l2 + 9 * sc * 1.05f + 4), SOFT);

        // ---- buttons ----
        for (int i = 0; i < 6; i++) {
            int x = (sw - bw) / 2, y = by0 + i * (bh + gap);
            boolean hov = in(mx, my, x, y, bw, bh);
            c.fill(x, y, x + bw, y + bh, hov ? 0xFF3B1068 : 0xFF1A0A30);
            border(c, x, y, bw, bh, hov ? ACC : 0xFF4C2A80);
            txt(c, ICONS[i], x + 8, y + (bh - 8) / 2, ACC);
            txt(c, LABELS[i], x + (bw - textRenderer.getWidth(LABELS[i])) / 2, y + (bh - 8) / 2, 0xFFFFFFFF);
            if (hov && click) {
                switch (i) {
                    case 0 -> mc.setScreen(new SelectWorldScreen(this));
                    case 1 -> mc.setScreen(new MultiplayerScreen(this));
                    case 2 -> mc.setScreen(new ClickGuiScreen());
                    case 3 -> mc.setScreen(new OptionsScreen(this, mc.options));
                    case 4 -> say("Accounts are managed by your launcher.");
                    default -> mc.scheduleStop();
                }
                if (i != 4) return; // ออกจาก render ทันทีถ้าเปลี่ยนหน้าจอ
            }
        }

        // ---- top-left tag ----
        panel(c, 4, 4, 104, 24);
        c.fill(8, 8, 22, 24, ACC_D);
        txt(c, "S", 13, 12, 0xFFFFFFFF);
        txt(c, "Structure Client", 27, 8, 0xFFFFFFFF);
        txt(c, "v0.1.0-Alpha", 27, 18, ACC);

        // ---- side panels (hidden on very narrow screens) ----
        if (sw >= 420) {
            int pw = Math.min(116, (int) (sw * 0.23f));
            // Client Info
            int ix = 6, iy = (int) (sh * 0.40f);
            panel(c, ix, iy, pw, 48);
            txt(c, "★ Client Info", ix + 6, iy + 5, 0xFFFFFFFF);
            txt(c, "FPS: ", ix + 6, iy + 18, SOFT);
            txt(c, String.valueOf(mc.getCurrentFps()), ix + 6 + textRenderer.getWidth("FPS: "), iy + 18, 0xFF4ADE80);
            txt(c, "Time: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), ix + 6, iy + 30, SOFT);

            // Quick Features
            int qx = sw - pw - 6, qy = (int) (sh * 0.36f);
            panel(c, qx, qy, pw, 62);
            txt(c, "⚡ Quick Features", qx + 6, qy + 5, 0xFFFFFFFF);

            Mod[] qm = {
                StructureClient.modules.get(PlayerModules.ArmorHud.class),
                StructureClient.modules.get(UtilityModules.ClientInfo.class),
                StructureClient.modules.get(PlayerModules.Coordinates.class)
            };
            String[] ql = {"Armor HUD", "Client Info", "Coordinates"};
            for (int i = 0; i < 3; i++) {
                int ry = qy + 18 + i * 14;
                txt(c, ql[i], qx + 6, ry + 1, 0xFFFFFFFF);
                boolean enabled = qm[i] != null && qm[i].isEnabled();
                toggle(c, qx + pw - 26, ry, enabled);
                if (click && in(mx, my, qx, ry - 2, pw, 14) && qm[i] != null) {
                    qm[i].toggle();
                }
            }

            // Welcome card (slides)
            int wi = (int) ((now / 4000) % 3);
            int wy = sh - 22 - 42;
            panel(c, 6, wy, pw + 40, 42);
            txt(c, SLIDES[wi][0], 12, wy + 6, 0xFFFFFFFF);
            txt(c, SLIDES[wi][1], 12, wy + 16, ACC);
            txt(c, SLIDES[wi][2], 12, wy + 26, SOFT);
            for (int i = 0; i < 3; i++) c.fill(12 + i * 8, wy + 37, 16 + i * 8, wy + 40, i == wi ? ACC : 0xFF3A3050);

            // Tip
            int ty = sh - 22 - 40;
            panel(c, qx, ty, pw, 40);
            txt(c, "ⓘ Tip:", qx + 6, ty + 5, 0xFFFFFFFF);
            int yy = ty + 16;
            var lines = textRenderer.wrapLines(Text.literal("Press Right Shift to open ClickGUI!"), pw - 12);
            for (OrderedText ot : lines.subList(0, Math.min(2, lines.size()))) {
                c.drawText(textRenderer, ot, qx + 6, yy, SOFT, false);
                yy += 10;
            }
        }

        // ---- footer ----
        c.fill(0, sh - 16, sw, sh, 0xCC000000);
        txt(c, "◆ Powered by Fabric", 8, sh - 12, SOFT);
        String mw = "Made with ♥ by Structure Team";
        txt(c, mw, sw - 8 - textRenderer.getWidth(mw), sh - 12, SOFT);

        if (now < toastUntil) {
            int tw = textRenderer.getWidth(toast) + 12;
            panel(c, (sw - tw) / 2, by0 - 20, tw, 14);
            txt(c, toast, (sw - tw) / 2 + 6, by0 - 17, 0xFFFFFFFF);
        }
    }
}
