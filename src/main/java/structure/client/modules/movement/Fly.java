package dev.structure.client.module.movement;

import dev.structure.client.module.Category;
import dev.structure.client.module.Module;
import dev.structure.client.setting.BoolSetting;
import dev.structure.client.setting.ModeSetting;
import dev.structure.client.setting.NumberSetting;
import dev.structure.client.util.Move;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerAbilities;

public class Fly extends Module {
	private final ModeSetting mode = add(new ModeSetting("Mode", "Creative", "Creative", "Motion"));
	private final NumberSetting speed = add(new NumberSetting("Speed", 2, 0.5, 10, 0.5));
	private final BoolSetting autoFly = add(new BoolSetting("Auto Fly", true).visibleIf(() -> mode.is("Creative")));
	private final BoolSetting antiKick = add(new BoolSetting("Anti Kick", true));
	private int ticks;

	public Fly() {
		super("Fly", "Lets you fly.", Category.MOVEMENT);
	}

	@Override
	public String info() {
		return mode.get();
	}

	@Override
	public void onPreTick() {
		ClientPlayerEntity p = mc.player;
		if (p == null) return;
		ticks++;
		if (mode.is("Creative")) {
			PlayerAbilities a = p.getAbilities();
			a.allowFlying = true;
			if (autoFly.on() && !p.isSpectator()) a.flying = true;
			a.setFlySpeed((float) (speed.d() * 0.05));
		} else {
			double s = speed.d() * 0.1;
			double[] d = Move.dir();
			double vy = mc.options.jumpKey.isPressed() ? s : mc.options.sneakKey.isPressed() ? -s : 0;
			if (antiKick.on() && vy == 0 && ticks % 30 == 0) vy = -0.04;
			p.setVelocity(d[0] * s, vy, d[1] * s);
			p.fallDistance = 0;
		}
	}

	@Override
	public void onDisable() {
		ClientPlayerEntity p = mc.player;
		if (p == null) return;
		PlayerAbilities a = p.getAbilities();
		a.setFlySpeed(0.05f);
		if (!a.creativeMode && !p.isSpectator()) {
			a.allowFlying = false;
			a.flying = false;
		}
	}
}
