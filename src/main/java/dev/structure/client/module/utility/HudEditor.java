package dev.structure.client.module.utility;

import dev.structure.client.gui.HudEditorScreen;
import dev.structure.client.module.ActionModule;
import dev.structure.client.module.Category;

public class HudEditor extends ActionModule {
	public HudEditor() {
		super("HUD Editor", "Drag, resize and recolor HUD elements.", Category.UTILITY);
	}

	@Override
	protected void fire() {
		mc.setScreen(new HudEditorScreen(mc.currentScreen));
	}
}
