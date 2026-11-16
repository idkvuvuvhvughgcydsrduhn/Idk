package dev.structure.client.module.utility;

import dev.structure.client.module.ActionModule;
import dev.structure.client.module.Category;
import dev.structure.client.module.ModuleManager;
import dev.structure.client.setting.BoolSetting;
import org.lwjgl.glfw.GLFW;

public class Panic extends ActionModule {
	public final BoolSetting lock = add(new BoolSetting("Lock Client", true));

	public Panic() {
		super("Panic Button", "Disables every module and hides the client. Press again to restore.", Category.UTILITY);
		defaultBind(GLFW.GLFW_KEY_DELETE);
	}

	@Override
	protected void fire() {
		if (ModuleManager.panicked()) ModuleManager.unpanic();
		else ModuleManager.panic(lock.on());
	}
}
