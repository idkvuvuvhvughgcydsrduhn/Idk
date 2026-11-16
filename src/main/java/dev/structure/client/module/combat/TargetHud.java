package dev.structure.client.module.combat;

import dev.structure.client.module.Category;
import dev.structure.client.module.HudModule;
import dev.structure.client.module.utility.ClientSettings;
import dev.structure.client.util.Draw;
import java.util.Locale;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class TargetHud extends HudModule {
	private LivingEntity shown;
	private long lastSeen;
	private float animHp = -1f;
	private boolean editingNow;

	public TargetHud() {
		super("Target HUD", "Shows health and distance of your target.", Category.COMBAT, 0.5, 0.62);
	}

	private LivingEntity find() {
		LivingEntity a = AuraBase.activeTarget();
		if (a != null) return a;
		Entity t = mc.targetedEntity;
		if (t instanceof LivingEntity le && le.isAlive()) return le;
		return null;
	}

	@Override
	public void prepare(boolean editing) {
		editingNow = editing;
		LivingEntity t = mc.player == null ? null : find();
		if (t != null) {
			shown = t;
			lastSeen = System.currentTimeMillis();
		} else if (shown != null && (System.currentTimeMillis() - lastSeen > 2500 || !shown.isAlive())) {
			shown = null;
			animHp = -1f;
		}
	}

	@Override
	public int contentWidth() {
		return (shown != null || editingNow) ? 150 : 0;
	}

	@Override
	public int contentHeight() {
		return (shown != null || editingNow) ? 42 : 0;
	}

	@Override
	public void drawContent(DrawContext ctx, float delta, boolean editing) {
		int accent = ClientSettings.accentColor();
		String name = shown != null ? shown.getName().getString() : "Target";
		float hp = shown != null ? shown.getHealth() : 20f;
		float max = shown != null ? Math.max(1f, shown.getMaxHealth()) : 20f;
		float abs = shown != null ? shown.getAbsorptionAmount() : 0f;
		if (animHp < 0f) animHp = hp;
		animHp += (hp - animHp) * 0.2f;
		ctx.fill(0, 0, 150, 42, 0xB0080410);
		Draw.outline(ctx, 0, 0, 150, 42, Draw.alpha(accent, 0.8f));
		Draw.text(ctx, Draw.fit(name, 138), 6, 5, color.argb());
		float pct = Math.max(0f, Math.min(1f, animHp / max));
		ctx.fill(6, 18, 144, 24, 0xFF2A2238);
		ctx.fillGradient(6, 18, 6 + (int) (138 * pct), 24, accent, Draw.darken(accent, 0.55f));
		double dist = shown != null && mc.player != null ? mc.player.distanceTo(shown) : 0.0;
		int armor = shown != null ? shown.getArmor() : 0;
		String info = String.format(Locale.ROOT, "HP %.1f/%.0f%s   Armor %d   %.1fm", hp, max, abs > 0 ? " +" + (int) abs : "", armor, dist);
		Draw.text(ctx, Draw.fit(info, 138), 6, 28, 0xFFC8C4DC);
	}
}
