package dev.structure.client.module;

import dev.structure.client.setting.ColorSetting;
import dev.structure.client.setting.NumberSetting;
import net.minecraft.client.gui.DrawContext;

/**
 * A module that draws a movable HUD element. Position is stored as a 0..1 fraction of the
 * free space on screen, so layouts stay valid on every screen size and GUI scale.
 */
public abstract class HudModule extends Module {
	public final NumberSetting posX;
	public final NumberSetting posY;
	public final NumberSetting scale = add(new NumberSetting("HUD Scale", 1.0, 0.5, 3.0, 0.05));
	public final ColorSetting color = add(new ColorSetting("HUD Color", 0xFFE2D4FF));

	protected HudModule(String name, String description, Category category, double x, double y) {
		super(name, description, category);
		posX = add(new NumberSetting("HUD X", x, 0.0, 1.0, 0.0005).hidden());
		posY = add(new NumberSetting("HUD Y", y, 0.0, 1.0, 0.0005).hidden());
	}

	/** Called before sizes are read; refresh cached content here. */
	public void prepare(boolean editing) {
	}

	public abstract int contentWidth();

	public abstract int contentHeight();

	public abstract void drawContent(DrawContext ctx, float delta, boolean editing);

	public float scaleF() {
		return scale.f();
	}

	public int boxW() {
		return Math.round(contentWidth() * scaleF());
	}

	public int boxH() {
		return Math.round(contentHeight() * scaleF());
	}

	public int screenX(int sw) {
		return Math.round((float) posX.d() * Math.max(0, sw - boxW()));
	}

	public int screenY(int sh) {
		return Math.round((float) posY.d() * Math.max(0, sh - boxH()));
	}

	public void moveTo(int x, int y, int sw, int sh) {
		int freeX = Math.max(1, sw - boxW());
		int freeY = Math.max(1, sh - boxH());
		posX.set(Math.max(0.0, Math.min(1.0, x / (double) freeX)));
		posY.set(Math.max(0.0, Math.min(1.0, y / (double) freeY)));
	}

	public final void renderHud(DrawContext ctx, float delta, boolean editing, int sw, int sh) {
		prepare(editing);
		if (contentWidth() <= 0 || contentHeight() <= 0) return;
		int x = screenX(sw);
		int y = screenY(sh);
		float s = scaleF();
		ctx.getMatrices().pushMatrix();
		ctx.getMatrices().translate((float) x, (float) y);
		ctx.getMatrices().scale(s, s);
		drawContent(ctx, delta, editing);
		ctx.getMatrices().popMatrix();
	}
}
