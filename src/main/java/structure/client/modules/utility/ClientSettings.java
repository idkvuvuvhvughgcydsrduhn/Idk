package dev.structure.client.module.utility;

import dev.structure.client.module.Category;
import dev.structure.client.module.Module;
import dev.structure.client.module.ModuleManager;
import dev.structure.client.setting.BoolSetting;
import dev.structure.client.setting.ColorSetting;

/** Always-on module that holds client-wide options (theme, keys, auto-save). */
public class ClientSettings extends Module {
	public final ColorSetting accent = add(new ColorSetting("Accent", 0xFF9B30FF));
	public final BoolSetting autoSave = add(new BoolSetting("Auto Save", true));
	public final BoolSetting welcome = add(new BoolSetting("Welcome Message", true));

	public ClientSettings() {
		super("Client", "Client-wide settings", Category.CLIENT);
		bind.hidden();
		setEnabled(true, false);
	}

	private static ClientSettings inst() {
		return ModuleManager.get(ClientSettings.class);
	}

	public static int accentColor() {
		ClientSettings s = inst();
		return s == null ? 0xFF9B30FF : s.accent.argb();
	}

	public static boolean autoSaveOn() {
		ClientSettings s = inst();
		return s == null || s.autoSave.on();
	}

	public static boolean welcomeOn() {
		ClientSettings s = inst();
		return s == null || s.welcome.on();
	}
}
