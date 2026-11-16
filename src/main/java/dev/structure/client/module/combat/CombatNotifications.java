package dev.structure.client.module.combat;

import dev.structure.client.module.Category;
import dev.structure.client.module.Module;
import dev.structure.client.notify.NotificationManager;
import dev.structure.client.setting.BoolSetting;
import dev.structure.client.setting.NumberSetting;
import net.minecraft.entity.LivingEntity;

public class CombatNotifications extends Module {
	private final BoolSetting targetAlerts = add(new BoolSetting("Target Alerts", true));
	private final BoolSetting killAlerts = add(new BoolSetting("Kill Alerts", true));
	private final BoolSetting lowHealth = add(new BoolSetting("Low Health Alert", true));
	private final NumberSetting limit = add(new NumberSetting("Low Health At", 6, 1, 19, 1));
	private LivingEntity last;
	private boolean warned;

	public CombatNotifications() {
		super("Combat Notifications", "Alerts for new targets, kills and low health.", Category.COMBAT);
	}

	@Override
	public void onDisable() {
		last = null;
		warned = false;
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		if (last != null) {
			if (!last.isAlive() && !last.isRemoved()) {
				if (killAlerts.on()) NotificationManager.combat("Kill", last.getName().getString(), 0xFF7CFF8A);
				last = null;
			} else if (last.isRemoved()) {
				last = null;
			}
		}
		LivingEntity t = AuraBase.activeTarget();
		if (t != null && t != last) {
			last = t;
			if (targetAlerts.on()) NotificationManager.combat("Target", t.getName().getString(), 0xFFFF4D6D);
		}
		float hp = mc.player.getHealth();
		if (lowHealth.on()) {
			if (hp <= limit.f() && !warned) {
				warned = true;
				NotificationManager.combat("Warning", "Low health: " + Math.round(hp), 0xFFFFC857);
			} else if (hp > limit.f() + 2) {
				warned = false;
			}
		}
	}
}
