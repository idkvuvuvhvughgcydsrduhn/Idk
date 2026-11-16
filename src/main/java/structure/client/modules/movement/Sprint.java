package dev.structure.client.module.movement;

import dev.structure.client.module.Category;
import dev.structure.client.module.Module;
import dev.structure.client.setting.BoolSetting;
import dev.structure.client.util.Move;
import net.minecraft.client.network.ClientPlayerEntity;

public class Sprint extends Module {
	private final BoolSetting omni = add(new BoolSetting("All Directions", false));

	public Sprint() {
		super("Sprint", "Always sprint while moving.", Category.MOVEMENT);
	}

	@Override
	public void onPreTick() {
		ClientPlayerEntity p = mc.player;
		if (p == null || mc.options.sneakKey.isPressed()) return;
		if (omni.on()) {
			if (Move.moving() && !p.isUsingItem()) p.setSprinting(true);
		} else if (mc.options.forwardKey.isPressed()) {
			mc.options.sprintKey.setPressed(true);
		}
	}

	@Override
	public void onDisable() {
		mc.options.sprintKey.setPressed(false);
	}
}
