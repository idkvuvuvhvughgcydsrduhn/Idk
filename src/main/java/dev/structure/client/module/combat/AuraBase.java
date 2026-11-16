package dev.structure.client.module.combat;

import dev.structure.client.module.Category;
import dev.structure.client.module.Module;
import dev.structure.client.setting.BoolSetting;
import dev.structure.client.setting.ModeSetting;
import dev.structure.client.setting.NumberSetting;
import dev.structure.client.util.CombatUtil;
import dev.structure.client.util.Targets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

/** Shared logic for KillAura and MobAura. */
public abstract class AuraBase extends Module {
	protected final NumberSetting range = add(new NumberSetting("Range", 4.5, 2.0, 8.0, 0.1));
	protected final NumberSetting cps = add(new NumberSetting("CPS", 12, 1, 20, 1));
	protected final ModeSetting mode = add(new ModeSetting("Mode", "Single", "Single", "Switch", "Multi"));
	protected final ModeSetting sort = add(new ModeSetting("Sort", "Distance", "Distance", "Health", "Angle"));
	protected final BoolSetting rotate = add(new BoolSetting("Rotate", true));
	protected final NumberSetting maxTargets = add(new NumberSetting("Max Targets", 3, 1, 8, 1).visibleIf(() -> mode.is("Multi")));
	protected final BoolSetting autoSwitch = add(new BoolSetting("Auto Switch", true));
	protected final BoolSetting keepSprint = add(new BoolSetting("Keep Sprint", true));
	protected final BoolSetting throughWalls = add(new BoolSetting("Through Walls", false));
	protected final BoolSetting cooldown = add(new BoolSetting("1.9 Cooldown", false));

	private LivingEntity target;
	private double clicks;
	private int switchIdx;

	protected AuraBase(String name, String description) {
		super(name, description, Category.COMBAT);
	}

	protected abstract boolean accepts(Entity e);

	public LivingEntity target() {
		return target;
	}

	@Override
	public void onDisable() {
		target = null;
		clicks = 0;
	}

	@Override
	public String info() {
		return mode.get();
	}

	/** Target of whichever aura is currently running (KillAura first). */
	public static LivingEntity activeTarget() {
		for (Class<? extends AuraBase> c : java.util.List.of(KillAura.class, MobAura.class)) {
			AuraBase a = dev.structure.client.module.ModuleManager.get(c);
			if (a != null && a.isEnabled() && a.target() != null) return a.target();
		}
		return null;
	}

	@Override
	public void onPreTick() {
		ClientPlayerEntity p = mc.player;
		if (p == null || mc.world == null || mc.interactionManager == null) return;
		if (mc.currentScreen != null || p.isSpectator() || !p.isAlive()) {
			target = null;
			return;
		}
		List<LivingEntity> list = new ArrayList<>();
		double r = range.d();
		for (Entity e : mc.world.getEntities()) {
			if (!accepts(e)) continue;
			if (Targets.reach(e) > r) continue;
			if (!throughWalls.on() && !p.canSee(e)) continue;
			list.add((LivingEntity) e);
		}
		if (list.isEmpty()) {
			target = null;
			clicks = Math.min(clicks, 1.0);
			return;
		}
		switch (sort.get()) {
			case "Health" -> list.sort(Comparator.comparingDouble(LivingEntity::getHealth));
			case "Angle" -> list.sort(Comparator.comparingDouble(Targets::angleTo));
			default -> list.sort(Comparator.comparingDouble(Targets::reach));
		}
		List<LivingEntity> hit = new ArrayList<>();
		switch (mode.get()) {
			case "Multi" -> {
				int n = Math.min(list.size(), maxTargets.i());
				for (int i = 0; i < n; i++) hit.add(list.get(i));
				target = list.get(0);
			}
			case "Switch" -> {
				switchIdx = switchIdx % list.size();
				target = list.get(switchIdx);
				hit.add(target);
			}
			default -> {
				if (autoSwitch.on() || target == null || !list.contains(target)) target = list.get(0);
				hit.add(target);
			}
		}
		if (rotate.on()) CombatUtil.face(target);

		boolean ready;
		if (cooldown.on()) {
			ready = p.getAttackCooldownProgress(0.0f) >= 0.99f;
		} else {
			clicks = Math.min(clicks + cps.d() / 20.0 * (0.85 + Math.random() * 0.3), 2.0);
			ready = clicks >= 1.0;
		}
		if (!ready) return;
		for (LivingEntity t : hit) CombatUtil.attack(t, keepSprint.on());
		if (!cooldown.on()) clicks -= 1.0;
		if (mode.is("Switch")) switchIdx++;
	}
}
