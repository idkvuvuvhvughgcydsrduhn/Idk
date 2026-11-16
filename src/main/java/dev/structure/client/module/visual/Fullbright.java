package dev.structure.client.module.visual;

import dev.structure.client.module.Category;
import dev.structure.client.module.Module;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class Fullbright extends Module {
	private boolean given;

	public Fullbright() {
		super("Fullbright", "Maximum brightness everywhere.", Category.VISUAL);
	}

	@Override
	public void onTick() {
		ClientPlayerEntity p = mc.player;
		if (p == null) return;
		StatusEffectInstance cur = p.getStatusEffect(StatusEffects.NIGHT_VISION);
		if (cur == null || (!cur.isInfinite() && cur.getDuration() < 300)) {
			p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1000, 0, false, false, false));
			given = true;
		}
	}

	@Override
	public void onDisable() {
		if (given && mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
		given = false;
	}
}
