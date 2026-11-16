package dev.structure.client.module.hud;

import dev.structure.client.config.ConfigManager;
import dev.structure.client.module.Category;
import dev.structure.client.module.Module;
import dev.structure.client.module.ModuleManager;
import dev.structure.client.module.TextHud;
import java.util.ArrayList;
import java.util.List;

public class ClientStatus extends TextHud {
	public ClientStatus() {
		super("Client Status", "Enabled module count and active config.", Category.HUD, 1.0, 1.0);
	}

	@Override
	protected List<String> lines(boolean editing) {
		int on = 0;
		int total = 0;
		for (Module m : ModuleManager.all()) {
			if (m.category == Category.CLIENT) continue;
			total++;
			if (m.isEnabled()) on++;
		}
		List<String> l = new ArrayList<>();
		l.add("Client Info");
		l.add("Modules: " + on + "/" + total);
		l.add("Config: " + ConfigManager.current());
		return l;
	}
}
