package dev.structure.client.module.hud;

import dev.structure.client.module.Category;
import dev.structure.client.module.Module;
import dev.structure.client.module.ModuleManager;
import dev.structure.client.module.TextHud;
import dev.structure.client.util.Draw;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModuleList extends TextHud {
	public ModuleList() {
		super("Module List", "Lists the modules you have enabled.", Category.HUD, 1.0, 0.18);
	}

	@Override
	protected List<String> lines(boolean editing) {
		List<String> l = new ArrayList<>();
		for (Module m : ModuleManager.all()) {
			if (!m.isEnabled() || m.category == Category.CLIENT || m == null) continue;
			String inf = m.info();
			l.add(inf.isEmpty() ? m.name : m.name + " " + inf);
		}
		l.sort(Comparator.comparingInt((String s) -> -Draw.width(s)));
		if (l.isEmpty() && editing) l.add("Module List");
		return l;
	}
}
