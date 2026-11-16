package dev.structure.client.module.utility;

import dev.structure.client.gui.ClickGuiScreen;
import dev.structure.client.module.ActionModule;
import dev.structure.client.module.Category;
import org.lwjgl.glfw.GLFW;

public class ClickGui extends ActionModule {
	public ClickGui() {
		super("ClickGUI", "Opens the Structure Client menu.", Category.UTILITY);
		defaultBind(GLFW.GLFW_KEY_RIGHT_SHIFT);
	}

	@Override
	protected void fire() {
		mc.setScreen(new ClickGuiScreen());
	}
}
