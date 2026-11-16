package dev.structure.client.module.player;

import dev.structure.client.module.Category;
import dev.structure.client.module.Module;
import dev.structure.client.module.ModuleManager;
import dev.structure.client.setting.NumberSetting;

/** Game speed. The actual work happens in RenderTickCounterMixin (fail-soft). */
public class TimerModule extends Module {
	private final NumberSetting speed = add(new NumberSetting("Speed", 1.5, 0.1, 5.0, 0.1));

	public TimerModule() {
		super("Timer", "Changes the game speed.", Category.PLAYER);
	}

	@Override
	public String info() {
		return speed.display() + "x";
	}

	public static float multiplier() {
		TimerModule t = ModuleManager.get(TimerModule.class);
		return (t != null && t.isEnabled() && !ModuleManager.panicked()) ? t.speed.f() : 1.0f;
	}
}
