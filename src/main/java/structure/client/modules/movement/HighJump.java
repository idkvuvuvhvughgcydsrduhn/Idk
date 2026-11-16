package dev.structure.client.module.movement;

import dev.structure.client.module.Category;
import dev.structure.client.module.Module;
import dev.structure.client.setting.NumberSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class HighJump extends Module {
	private final NumberSetting height = add(new NumberSetting("Height", 0.8, 0.42, 3, 0.02));

	public HighJump() {
		super("High Jump", "Jump much higher.", Category.MOVEMENT);
	}

	@Override
	public String info() {
		return height.display();
	}

	@Override
	public void onPreTick() {
		ClientPlayerEntity p = mc.player;
		if (p != null && mc.options.jumpKey.isPressed() && p.isOnGround()) {
			Vec3d v = p.getVelocity();
			p.setVelocity(v.x, height.d(), v.z);
		}
	}
}
