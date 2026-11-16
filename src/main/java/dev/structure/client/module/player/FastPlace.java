package dev.structure.client.module.player;

import dev.structure.client.mixin.MinecraftClientAccessor;
import dev.structure.client.module.Category;
import dev.structure.client.module.Module;
import dev.structure.client.setting.NumberSetting;

/** Lowers the client-side delay between block placements. */
public class FastPlace extends Module {
	private final NumberSetting delay = add(new NumberSetting("Delay", 0, 0, 4, 1));

	public FastPlace() {
		super("Fast Place", "Lowers the client-side block placement delay.", Category.PLAYER);
	}

	@Override
	public String info() {
		return delay.display() + " tick";
	}

	@Override
	public void onPreTick() {
		if (mc.field_1724 == null || mc.field_1687 == null) return;
		MinecraftClientAccessor acc = (MinecraftClientAccessor) (Object) mc;
		int cooldown = acc.structure$getItemUseCooldown();
		if (cooldown > delay.i()) acc.structure$setItemUseCooldown(delay.i());
	}
}
