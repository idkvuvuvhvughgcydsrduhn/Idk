package dev.structure.client.module.combat;

import dev.structure.client.setting.BoolSetting;
import dev.structure.client.util.Targets;
import net.minecraft.entity.Entity;

public class MobAura extends AuraBase {
	private final BoolSetting hostile = add(new BoolSetting("Monsters", true));
	private final BoolSetting passive = add(new BoolSetting("Animals", false));

	public MobAura() {
		super("MobAura", "Automatically attacks nearby mobs");
	}

	@Override
	protected boolean accepts(Entity e) {
		return Targets.valid(e, false, hostile.on(), passive.on(), true, false);
	}
}
