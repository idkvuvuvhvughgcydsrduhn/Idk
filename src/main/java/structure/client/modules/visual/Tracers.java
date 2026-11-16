package dev.structure.client.module.visual;

import dev.structure.client.setting.ModeSetting;
import dev.structure.client.setting.NumberSetting;
import dev.structure.client.util.Draw;
import dev.structure.client.util.Projector;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;

public class Tracers extends EntityVisual {
	private final ModeSetting origin = add(new ModeSetting("Origin", "Crosshair", "Crosshair", "Bottom"));
	private final NumberSetting maxLines = add(new NumberSetting("Max Lines", 12, 1, 32, 1));
	private final NumberSetting thickness = add(new NumberSetting("Thickness", 2, 1, 4, 1));

	public Tracers() {
		super("Tracers", "Draws lines to nearby entities.", 96, true, false, false);
	}

	@Override
	public void onRender(DrawContext ctx, float delta) {
		if (!Projector.ready) return;
		int sw = Projector.sw;
		int sh = Projector.sh;
		double ox = sw / 2.0;
		double oy = origin.is("Bottom") ? sh : sh / 2.0;
		int t = thickness.i();
		double[] o = new double[2];
		int count = 0;
		for (LivingEntity e : visible()) {
			if (count++ >= maxLines.i()) break;
			Box b = lerpedBox(e, delta);
			double cx = (b.minX + b.maxX) / 2.0;
			double cy = (b.minY + b.maxY) / 2.0;
			double cz = (b.minZ + b.maxZ) / 2.0;
			double ex;
			double ey;
			if (Projector.project(cx, cy, cz, o)) {
				ex = o[0];
				ey = o[1];
			} else {
				Projector.direction(cx, cy, cz, o);
				double len = Math.sqrt(o[0] * o[0] + o[1] * o[1]);
				if (len < 1e-6) {
					o[0] = 0;
					o[1] = 1;
					len = 1;
				}
				ex = ox + o[0] / len * 4000;
				ey = oy + o[1] / len * 4000;
			}
			// keep the end point on screen so we never draw thousands of pixels
			double dx = ex - ox;
			double dy = ey - oy;
			double k = 1.0;
			if (ex < 0) k = Math.min(k, (0 - ox) / dx);
			if (ex > sw) k = Math.min(k, (sw - ox) / dx);
			if (ey < 0) k = Math.min(k, (0 - oy) / dy);
			if (ey > sh) k = Math.min(k, (sh - oy) / dy);
			if (k < 1.0) {
				ex = ox + dx * k;
				ey = oy + dy * k;
			}
			Draw.line(ctx, ox, oy, ex, ey, colorFor(e), t);
		}
	}
}
