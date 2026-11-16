package dev.structure.client.module.movement;

import dev.structure.client.module.Category;
import dev.structure.client.module.Module;
import dev.structure.client.setting.BoolSetting;
import dev.structure.client.setting.ModeSetting;
import dev.structure.client.setting.NumberSetting;
import dev.structure.client.util.Move;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class Speed extends Module {
	private final ModeSetting mode = add(new ModeSetting("Mode", "Strafe", "Strafe", "Ground"));
	private final NumberSetting speed = add(new NumberSetting("Speed", 0.5, 0.1, 3, 0.05));
	private final BoolSetting autoJump = add(new BoolSetting("Auto Jump", false));

	public Speed() {
		super("Speed", "Move faster on the ground and in the air.", Category.MOVEMENT);
	}

	@Override
	public String info() {
		return speed.display();
	}

	@Override
	public void onPreTick() {
		ClientPlayerEntity p = mc.player;
		if (p == null || !Move.moving() || p.isTouchingWater() || p.isInLava() || p.getAbilities().flying) return;
		if (mode.is("Ground") && !p.isOnGround()) return;
		double[] d = Move.dir();
		Vec3d v = p.getVelocity();
		double vy = (autoJump.on() && p.isOnGround()) ? 0.42 : v.y;
		p.setVelocity(d[0] * speed.d(), vy, d[1] * speed.d());
	}
}
