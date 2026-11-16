package dev.structure.client.module.combat;

import dev.structure.client.module.AttackListener;
import dev.structure.client.module.Category;
import dev.structure.client.module.Module;
import dev.structure.client.setting.ModeSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Criticals extends Module implements AttackListener {
	private final ModeSetting mode = add(new ModeSetting("Mode", "Packet", "Packet", "Strict"));

	public Criticals() {
		super("Criticals", "Automatically lands critical hits", Category.COMBAT);
	}

	@Override
	public String info() {
		return mode.get();
	}

	@Override
	public void onAttack(Entity target) {
		ClientPlayerEntity p = mc.player;
		if (p == null || mc.getNetworkHandler() == null || !(target instanceof LivingEntity)) return;
		if (!p.isOnGround() || p.isTouchingWater() || p.isInLava() || p.isClimbing() || p.hasVehicle()) return;
		double x = p.getX();
		double y = p.getY();
		double z = p.getZ();
		boolean hc = p.horizontalCollision;
		if (mode.is("Strict")) {
			send(x, y + 0.11, z, hc);
			send(x, y + 0.1100013579, z, hc);
			send(x, y + 0.0000013579, z, hc);
		} else {
			send(x, y + 0.0625, z, hc);
			send(x, y, z, hc);
		}
	}

	private void send(double x, double y, double z, boolean hc) {
		mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false, hc));
	}
}
