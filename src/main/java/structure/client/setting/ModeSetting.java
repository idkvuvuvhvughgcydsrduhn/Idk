package dev.structure.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.function.BooleanSupplier;

public class ModeSetting extends Setting<String> {
	public final String[] modes;

	public ModeSetting(String name, String def, String... modes) {
		super(name, def);
		this.modes = modes;
	}

	public boolean is(String m) {
		return value.equalsIgnoreCase(m);
	}

	public int index() {
		for (int i = 0; i < modes.length; i++) {
			if (modes[i].equalsIgnoreCase(value)) return i;
		}
		return 0;
	}

	public void cycle() {
		set(modes[(index() + 1) % modes.length]);
	}

	@Override
	public ModeSetting hidden() {
		super.hidden();
		return this;
	}

	@Override
	public ModeSetting visibleIf(BooleanSupplier s) {
		super.visibleIf(s);
		return this;
	}

	@Override
	public JsonElement save() {
		return new JsonPrimitive(value);
	}

	@Override
	public void load(JsonElement e) {
		try {
			String s = e.getAsString();
			for (String m : modes) {
				if (m.equalsIgnoreCase(s)) {
					value = m;
					return;
				}
			}
		} catch (Exception ignored) {
		}
	}
}
