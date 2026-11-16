package dev.structure.client.module.utility;

import dev.structure.client.module.Category;
import dev.structure.client.module.Module;
import dev.structure.client.setting.BoolSetting;
import dev.structure.client.setting.NumberSetting;

public class Notifications extends Module {
	public final BoolSetting toggles = add(new BoolSetting("Toggle Messages", true));
	public final BoolSetting combat = add(new BoolSetting("Combat Alerts", true));
	public final BoolSetting chat = add(new BoolSetting("Mirror In Chat", false));
	public final NumberSetting duration = add(new NumberSetting("Duration", 2.5, 1.0, 8.0, 0.5));

	public Notifications() {
		super("Notifications", "Pop-up messages when modules or combat events change.", Category.UTILITY);
	}
}
