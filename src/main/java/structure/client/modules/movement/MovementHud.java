package dev.structure.client.module.movement;

import dev.structure.client.module.Category;
import dev.structure.client.module.TextHud;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.network.ClientPlayerEntity;

public class MovementHud extends TextHud {
	private double lx, lz, spd;
	private boolean init;

	public MovementHud() {
		super("Movement HUD", "Speed, sprint and rotation.", Category.MOVEMENT, 0.01, 0.38);
	}

	@Override
	public void onTick() {
		ClientPlayerEntity p = mc.player;
		if (p == null) return;
		if (init) {
			double dx = p.getX() - lx;
			double dz = p.getZ() - lz;
			spd = Math.sqrt(dx * dx + dz * dz) * 20.0;
		}
		lx = p.getX();
		lz = p.getZ();
		init = true;
	}

	@Override
	protected List<String> lines(boolean editing) {
		ClientPlayerEntity p = mc.player;
		List<String> l = new ArrayList<>();
		if (p == null) return l;
		l.add(String.format(Locale.ROOT, "Speed %.2f b/s", spd));
		String state = p.getAbilities().flying ? "Flying" : p.isSprinting() ? "Sprinting" : p.isSneaking() ? "Sneaking" : p.isOnGround() ? "Walking" : "Airborne";
		l.add("State " + state);
		l.add(String.format(Locale.ROOT, "Yaw %.0f  Pitch %.0f", p.getYaw(), p.getPitch()));
		return l;
	}
}
