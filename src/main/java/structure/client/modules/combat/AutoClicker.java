package dev.structure.client.module.combat;

import dev.structure.client.module.Category;
import dev.structure.client.module.Module;
import dev.structure.client.setting.BoolSetting;
import dev.structure.client.setting.NumberSetting;
import dev.structure.client.util.CombatUtil;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoClicker extends Module {
	private final NumberSetting cps = add(new NumberSetting("CPS", 14, 1, 20, 1));
	private final BoolSetting randomize = add(new BoolSetting("Randomize", true));
	private final BoolSetting swingAir = add(new BoolSetting("Swing On Miss", true));
	private final BoolSetting keepSprint = add(new BoolSetting("Keep Sprint", true));
	private double clicks;

	public AutoClicker() {
		super("AutoClicker", "Clicks automatically while attack is held", Category.COMBAT);
	}

	@Override
	public void onDisable() {
		clicks = 0;
	}

	@Override
	public String info() {
		return cps.display();
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.interactionManager == null) return;
		if (mc.currentScreen != null || !mc.options.attackKey.isPressed()) {
			clicks = 0;
			return;
		}
		HitResult hr = mc.crosshairTarget;
		boolean onEntity = hr instanceof EntityHitResult;
		if (!onEntity && !(swingAir.on() && hr != null && hr.getType() == HitResult.Type.MISS)) return;
		double jitter = randomize.on() ? (0.8 + Math.random() * 0.4) : 1.0;
		clicks = Math.min(clicks + cps.d() / 20.0 * jitter, 2.0);
		if (clicks < 1.0) return;
		clicks -= 1.0;
		if (onEntity) CombatUtil.attack(((EntityHitResult) hr).getEntity(), keepSprint.on());
		else mc.player.swingHand(Hand.MAIN_HAND);
	}
}
