package dev.structure.client.module.visual;

import dev.structure.client.setting.BoolSetting;
import dev.structure.client.util.Draw;
import dev.structure.client.util.Projector;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;

public class EntityHighlight extends EntityVisual {
	private final BoolSetting glow = add(new BoolSetting("Glow Outline", true));
	private final BoolSetting fill = add(new BoolSetting("Tint Fill", true));

	public EntityHighlight() {
		super("Entity Highlight", "Highlights mobs and animals through walls.", 64, false, true, true);
	}

	@Override
	protected boolean wantsGlow() {
		return glow.on();
	}

	@Override
	public void onRender(DrawContext ctx, float delta) {
		if (!Projector.ready || !fill.on()) return;
		for (LivingEntity e : visible()) {
			double[] b = Projector.bounds(lerpedBox(e, delta));
			if (b == null) continue;
			int x1 = (int) b[0], y1 = (int) b[1], x2 = (int) b[2], y2 = (int) b[3];
			if (x2 < 0 || y2 < 0 || x1 > Projector.sw || y1 > Projector.sh || x2 - x1 > Projector.sw * 4) continue;
			ctx.fill(x1, y1, x2, y2, Draw.alpha(colorFor(e), 0.22f));
		}
	}
}
