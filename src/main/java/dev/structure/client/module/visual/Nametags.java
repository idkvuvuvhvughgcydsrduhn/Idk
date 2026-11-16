package dev.structure.client.module.visual;

import dev.structure.client.setting.BoolSetting;
import dev.structure.client.util.Draw;
import dev.structure.client.util.Projector;
import java.util.Locale;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;

public class Nametags extends EntityVisual {
	private final BoolSetting health = add(new BoolSetting("Health", true));
	private final BoolSetting distance = add(new BoolSetting("Distance", true));

	public Nametags() {
		super("Nametags", "Readable name, health and distance above entities.", 64, true, false, false);
	}

	@Override
	public void onRender(DrawContext ctx, float delta) {
		if (!Projector.ready || mc.player == null) return;
		double[] o = new double[2];
		int shown = 0;
		for (LivingEntity e : visible()) {
			if (shown >= 24) break;
			Box b = lerpedBox(e, delta);
			if (!Projector.project((b.minX + b.maxX) / 2.0, b.maxY + 0.35, (b.minZ + b.maxZ) / 2.0, o)) continue;
			String name = e.getName().getString();
			String hp = health.on() ? String.format(Locale.ROOT, "  %.1f", e.getHealth()) : "";
			String dist = distance.on() ? String.format(Locale.ROOT, "  %.0fm", mc.player.distanceTo(e)) : "";
			int w = Draw.width(name + hp + dist) + 8;
			int x = (int) o[0] - w / 2;
			int y = (int) o[1] - 12;
			if (x + w < 0 || x > Projector.sw || y < -12 || y > Projector.sh) continue;
			ctx.fill(x, y, x + w, y + 11, 0xB0080410);
			ctx.fill(x, y + 10, x + w, y + 11, colorFor(e));
			int tx = x + 4;
			Draw.textFlat(ctx, name, tx, y + 2, 0xFFFFFFFF);
			tx += Draw.width(name);
			if (!hp.isEmpty()) {
				float pct = Math.max(0f, Math.min(1f, e.getHealth() / Math.max(1f, e.getMaxHealth())));
				Draw.textFlat(ctx, hp, tx, y + 2, Draw.mix(0xFFFF3B3B, 0xFF4DFF6A, pct));
				tx += Draw.width(hp);
			}
			if (!dist.isEmpty()) Draw.textFlat(ctx, dist, tx, y + 2, 0xFFB8B4C8);
			shown++;
		}
	}
}
