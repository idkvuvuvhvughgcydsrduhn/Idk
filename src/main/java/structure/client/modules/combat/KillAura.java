package dev.structure.client.module.combat;

import dev.structure.client.setting.BoolSetting;
import dev.structure.client.util.Targets;
import net.minecraft.entity.Entity;

public class KillAura extends AuraBase {
	private final BoolSetting players = add(new BoolSetting("Players", true));
	private final BoolSetting hostile = add(new BoolSetting("Mobs", false));
	private final BoolSetting animals = add(new BoolSetting("Animals", false));
	private final BoolSetting invisible = add(new BoolSetting("Invisible", false));
	private final BoolSetting antiBot = add(new BoolSetting("Anti-Bot", true));

	public KillAura() {
		super("KillAura", "Automatically attacks nearby targets");
	}

	@Override
	protected boolean accepts(Entity e) {
		return Targets.valid(e, players.on(), hostile.on(), animals.on(), invisible.on(), antiBot.on());
	}
}
