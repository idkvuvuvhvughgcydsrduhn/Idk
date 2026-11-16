package dev.structure.client.module.visual;

import dev.structure.client.setting.BoolSetting;
import dev.structure.client.setting.ModeSetting;
import dev.structure.client.setting.NumberSetting;
import dev.structure.client.util.Draw;
import dev.structure.client.util.Projector;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;

public class Esp extends EntityVisual {
	private final ModeSetting mode = add(new ModeSetting("Mode", "Box", "Box", "Glow", "Both"));
	private final NumberSetting thickness = add(new NumberSetting("Thickness", 1, 1, 3, 1));
	private final BoolSetting healthBar = add(new BoolSetting("Health Bar", true));

	public Esp() {
		super("ESP", "Highlights players through walls.", 96, true, false, false);
	}

	@Override
	public String info() {
		return mode.get();
	}

	@Override
	protected boolean wantsGlow() {
		return !mode.is("Box");
	}

	@Override
	public void onRender(DrawContext ctx, float delta) {
		if (!Projector.ready || mode.is("Glow")) return;
		int t = thickness.i();
		for (LivingEntity e : visible()) {
			double[] b = Projector.bounds(lerpedBox(e, delta));
			if (b == null) continue;
			int x1 = (int) Math.floor(b[0]);
			int y1 = (int) Math.floor(b[1]);
			int x2 = (int) Math.ceil(b[2]);
			int y2 = (int) Math.ceil(b[3]);
			if (x2 < 0 || y2 < 0 || x1 > Projector.sw || y1 > Projector.sh || x2 - x1 > Projector.sw * 4) continue;
			int col = colorFor(e);
			Draw.outlineBox(ctx, x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xAA000000, 1);
			Draw.outlineBox(ctx, x1, y1, x2, y2, col, t);
			if (healthBar.on()) {
				float pct = Math.max(0f, Math.min(1f, e.getHealth() / Math.max(1f, e.getMaxHealth())));
				int bx = x1 - 4 - t;
				ctx.fill(bx - 1, y1 - 1, bx + 3, y2 + 1, 0xAA000000);
				int hh = (int) ((y2 - y1) * pct);
				ctx.fill(bx, y2 - hh, bx + 2, y2, Draw.mix(0xFFFF3B3B, 0xFF4DFF6A, pct));
			}
		}
	}
}
