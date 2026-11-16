package dev.structure.client.module.player;

import dev.structure.client.mixin.MinecraftClientAccessor;
import dev.structure.client.module.Category;
import dev.structure.client.module.Module;
import dev.structure.client.setting.NumberSetting;

/**
 * Lowers the delay between block placements. Only active in singleplayer: on servers the
 * placement rate is checked by the server, so this module does nothing there.
 */
public class FastPlace extends Module {
	private final NumberSetting delay = add(new NumberSetting("Delay", 0, 0, 4, 1));

	public FastPlace() {
		super("Fast Place", "Lowers the block placement delay (singleplayer only).", Category.PLAYER);
	}

	@Override
	public String info() {
		return delay.display() + " tick";
	}

	@Override
	public void onPreTick() {
		if (mc.player == null || mc.world == null || !mc.isInSingleplayer()) return;
		MinecraftClientAccessor acc = (MinecraftClientAccessor) (Object) mc;
		int cooldown = acc.structure$getItemUseCooldown();
		if (cooldown > delay.i()) acc.structure$setItemUseCooldown(delay.i());
	}
}
