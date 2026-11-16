package dev.structure.client.module.movement;

import dev.structure.client.module.Category;
import dev.structure.client.module.Module;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall extends Module {
	public NoFall() {
		super("No Fall", "Cancels fall damage.", Category.MOVEMENT);
	}

	@Override
	public void onTick() {
		ClientPlayerEntity p = mc.player;
		if (p == null || mc.getNetworkHandler() == null) return;
		if (p.fallDistance > 2.5 && !p.getAbilities().flying && !p.isOnGround()) {
			mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, p.horizontalCollision));
		}
	}
}
