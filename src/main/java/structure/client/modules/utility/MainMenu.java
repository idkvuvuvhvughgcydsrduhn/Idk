package dev.structure.client.module.utility;

import dev.structure.client.module.Category;
import dev.structure.client.module.Module;

/** Marker module: when enabled the title screen is replaced by StructureTitleScreen. */
public class MainMenu extends Module {
	public MainMenu() {
		super("Custom Main Menu", "Replaces the title screen with the Structure menu.", Category.UTILITY);
	}
}
