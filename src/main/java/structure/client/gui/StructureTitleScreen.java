package dev.structure.client.gui;

import dev.structure.client.StructureClient;
import dev.structure.client.module.ModuleManager;
import dev.structure.client.module.utility.ClientSettings;
import dev.structure.client.util.Draw;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.text.Text;

/** Structure Client main menu. Drawn procedurally, so it needs no image assets and scales to any screen. */
public class StructureTitleScreen extends Screen {
	private static final String[] LABELS = {"Singleplayer", "Multiplayer", "Client Settings", "Options", "Quit Game"};
	private static final String[] ICONS = {">", "*", "+", "~", "x"};
	private static final String[][] SLIDES = {
		{"Welcome to", "Structure Client!", "Enjoy your game!"},
		{"Press Right Shift", "to open the ClickGUI", "Panic key: Delete"},
		{"Tap a module name", "to edit its settings", "Switch = on / off"}};

	private final Hits hits = new Hits();
	private final float[][] stars = new float[70][3];
	private final float[][] towers = new float[34][4];
	private final float[][] pix = new float[170][4];
	private int mouseX, mouseY;

	public StructureTitleScreen() {
		super(Text.literal("Structure Client"));
		Random r = new Random(7);
		for (float[] s : stars) {
			s[0] = r.nextFloat();
			s[1] = r.nextFloat() * 0.6f;
			s[2] = 0.3f + r.nextFloat() * 0.7f;
		}
		for (float[] t : towers) {
			t[0] = 0.40f + r.nextFloat() * 0.60f;
			t[1] = r.nextFloat();
			t[2] = 0.08f + r.nextFloat() * 0.34f;
			t[3] = r.nextFloat();
		}
		for (float[] p : pix) {
			p[0] = r.nextFloat() * 0.26f;
			p[1] = 0.12f + r.nextFloat() * 0.42f;
			p[2] = r.nextFloat();
			p[3] = r.nextFloat();
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		Hits.Hit h = hits.at(click.x(), click.y());
		if (h != null && h.tap != null) h.tap.run();
		return true;
	}

	private void button(DrawContext ctx, int x, int y, int w, int h, String label, String icon, Runnable tap) {
		boolean hov = Draw.inside(mouseX, mouseY, x, y, w, h);
		int acc = ClientSettings.accentColor();
		ctx.fill(x, y, x + w, y + h, hov ? Draw.darken(acc, 0.5f) : 0xFF1A0A30);
		Draw.outline(ctx, x, y, w, h, hov ? acc : 0xFF4C2A80);
		Draw.text(ctx, icon, x + 8, y + (h - 8) / 2, acc);
		Draw.textCenter(ctx, label, x + w / 2, y + (h - 8) / 2, 0xFFFFFFFF);
		hits.add(x, y, w, h, tap);
	}

	@Override
	public void render(DrawContext ctx, int mx, int my, float delta) {
		hits.clear();
		mouseX = mx;
		mouseY = my;
		int sw = width, sh = height;
		long now = System.currentTimeMillis();
		int acc = ClientSettings.accentColor();

		// background
		ctx.fillGradient(0, 0, sw, sh / 2, 0xFF0F031C, 0xFF2A0B4A);
		ctx.fillGradient(0, sh / 2, sw, sh, 0xFF2A0B4A, 0xFF0A0214);
		for (float[] s : stars) {
			int a = (int) (255 * s[2]);
			int x = (int) (s[0] * sw), y = (int) (s[1] * sh);
			ctx.fill(x, y, x + 1, y + 1, (a << 24) | 0xE9D5FF);
		}
		int ms = Math.max(8, sh / 9), mxp = (int) (sw * 0.78f), myp = (int) (sh * 0.08f);
		ctx.fill(mxp, myp, mxp + ms, myp + ms, 0xFFE9D5FF);
		ctx.fill(mxp + 2, myp + 2, mxp + ms - 2, myp + ms - 2, 0xFFC4A1F5);
		for (float[] t : towers) {
			int x = (int) (t[0] * sw), w = 3 + (int) (t[1] * 10), h = (int) (t[2] * sh);
			ctx.fill(x, sh - h, x + w, sh, 0xFF14062A);
			ctx.fill(x, sh - h, x + 1, sh, 0xFF2B0F52);
			if (t[3] > 0.6f) ctx.fill(x + w / 2, sh - h + h / 3, x + w / 2 + 1, sh - h + h / 3 + 2, acc);
		}
		int[] leaf = {0xFF6D28D9, 0xFF7E22CE, 0xFF4C1D95, 0xFF9333EA};
		for (float[] p : pix) {
			int s = 2 + (int) (p[2] * 4);
			int x = (int) (p[0] * sw), y = (int) (p[1] * sh);
			ctx.fill(x, y, x + s, y + s, leaf[(int) (p[3] * 3.99f)]);
		}
		ctx.fillGradient(0, sh - sh / 5, sw, sh, 0x00000000, 0xAA000000);

		// layout
		int bw = Math.min(220, (int) (sw * 0.44f));
		int bh = (int) Math.min(26, sh * 0.52 / 6.5);
		int gap = Math.max(2, (int) (bh * 0.2));
		int stack = LABELS.length * bh + (LABELS.length - 1) * gap;
		int by0 = sh - 22 - stack;

		// logo
		float cx = sw / 2f;
		String logo = "STRUCTURE";
		int w1 = Draw.width(logo);
		float avail = by0 - 8 - 12;
		float sc = Math.max(1f, Math.min(sw * 0.40f / w1, (avail - 28) / 20f));
		int u = Math.max(1, Math.round(sc * 0.6f));
		int cy = 12;
		ctx.fill((int) cx - 6 * u, cy + 5 * u, (int) cx + 6 * u, cy + 7 * u, 0xFFC8A2FF);
		for (int i = -1; i <= 1; i++) ctx.fill((int) cx + i * 5 * u - u, cy + u, (int) cx + i * 5 * u + u, cy + 5 * u, 0xFFC8A2FF);
		float l1 = cy + 8 * u;
		big(ctx, logo, cx, l1, sc, Theme.SILVER, 0xFF3B1068);
		float l2 = l1 + 9 * sc;
		big(ctx, "CLIENT", cx, l2, sc * 1.05f, acc, 0xFF1E0646);
		String tagline = "P L A Y   \u2022   C H E A T   \u2022   D O M I N A T E";
		Draw.textCenter(ctx, tagline, (int) cx, (int) (l2 + 9 * sc * 1.05f + 4), 0xFFB9A9D6);

		// buttons
		for (int i = 0; i < LABELS.length; i++) {
			int x = (sw - bw) / 2, y = by0 + i * (bh + gap);
			final int idx = i;
			button(ctx, x, y, bw, bh, LABELS[i], ICONS[i], () -> action(idx));
		}

		// top-left tag
		ctx.fill(4, 4, 108, 28, 0xCC10051C);
		Draw.outline(ctx, 4, 4, 104, 24, 0xFF3B1068);
		ctx.fill(8, 8, 22, 24, Draw.darken(acc, 0.5f));
		Draw.text(ctx, "S", 13, 12, 0xFFFFFFFF);
		Draw.text(ctx, "Structure Client", 27, 8, 0xFFFFFFFF);
		Draw.text(ctx, StructureClient.VERSION, 27, 18, acc);

		// side panels
		if (sw >= 420) {
			int pw = Math.min(116, (int) (sw * 0.23f));
			int ix = 6, iy = (int) (sh * 0.40f);
			panel(ctx, ix, iy, pw, 48);
			Draw.text(ctx, "Client Info", ix + 6, iy + 5, 0xFFFFFFFF);
			Draw.text(ctx, "FPS: " + MinecraftClient.getInstance().getCurrentFps(), ix + 6, iy + 18, 0xFF4ADE80);
			Draw.text(ctx, "Time: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), ix + 6, iy + 30, 0xFFB9A9D6);

			int qx = sw - pw - 6, qy = (int) (sh * 0.36f);
			panel(ctx, qx, qy, pw, 62);
			Draw.text(ctx, "Quick Features", qx + 6, qy + 5, 0xFFFFFFFF);
			String[] ql = {"Armor HUD", "Client Info", "Coordinates"};
			Class<?>[] qc = {dev.structure.client.module.player.ArmorHud.class,
				dev.structure.client.module.utility.ClientInfo.class,
				dev.structure.client.module.player.Coordinates.class};
			for (int i = 0; i < 3; i++) {
				int ry = qy + 18 + i * 14;
				Draw.text(ctx, ql[i], qx + 6, ry + 1, 0xFFFFFFFF);
				var m = ModuleManager.get(qc[i].asSubclass(dev.structure.client.module.Module.class));
				boolean on = m != null && m.isEnabled();
				Draw.toggle(ctx, qx + pw - 26, ry, 20, 10, on, acc);
				final var mm = m;
				hits.add(qx, ry - 2, pw, 14, () -> {
					if (mm != null) mm.toggle();
				});
			}

			int wi = (int) ((now / 4000) % SLIDES.length);
			int wy = sh - 22 - 42;
			panel(ctx, 6, wy, pw + 40, 42);
			Draw.text(ctx, SLIDES[wi][0], 12, wy + 6, 0xFFFFFFFF);
			Draw.text(ctx, SLIDES[wi][1], 12, wy + 16, acc);
			Draw.text(ctx, SLIDES[wi][2], 12, wy + 26, 0xFFB9A9D6);
			for (int i = 0; i < SLIDES.length; i++) ctx.fill(12 + i * 8, wy + 37, 16 + i * 8, wy + 40, i == wi ? acc : 0xFF3A3050);
		}

		// footer
		ctx.fill(0, sh - 16, sw, sh, 0xCC000000);
		Draw.text(ctx, "Powered by Fabric", 8, sh - 12, 0xFFB9A9D6);
		String credit = "Made by Structure Team";
		Draw.text(ctx, credit, sw - 8 - Draw.width(credit), sh - 12, 0xFFB9A9D6);
	}

	private void action(int i) {
		MinecraftClient mc = MinecraftClient.getInstance();
		switch (i) {
			case 0 -> mc.setScreen(new SelectWorldScreen(this));
			case 1 -> mc.setScreen(new MultiplayerScreen(this));
			case 2 -> mc.setScreen(new ClickGuiScreen());
			case 3 -> mc.setScreen(new OptionsScreen(this, mc.options));
			default -> mc.scheduleStop();
		}
	}

	private void big(DrawContext ctx, String s, float cx, float y, float sc, int col, int shadow) {
		int w = Draw.width(s);
		var m = ctx.getMatrices();
		m.pushMatrix();
		m.translate(cx - w * sc / 2f, y);
		m.scale(sc, sc);
		Draw.textFlat(ctx, s, 1, 1, shadow);
		Draw.textFlat(ctx, s, 0, 0, col);
		m.popMatrix();
	}

	private void panel(DrawContext ctx, int x, int y, int w, int h) {
		ctx.fill(x, y, x + w, y + h, 0xCC10051C);
		Draw.outline(ctx, x, y, w, h, 0xFF3B1068);
	}
}
