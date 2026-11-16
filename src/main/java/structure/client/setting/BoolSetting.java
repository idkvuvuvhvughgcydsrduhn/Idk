package dev.structure.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.function.BooleanSupplier;

public class BoolSetting extends Setting<Boolean> {
	public BoolSetting(String name, boolean def) {
		super(name, def);
	}

	public boolean on() {
		return value;
	}

	public void toggle() {
		set(!value);
	}

	@Override
	public BoolSetting hidden() {
		super.hidden();
		return this;
	}

	@Override
	public BoolSetting visibleIf(BooleanSupplier s) {
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
			value = e.getAsBoolean();
		} catch (Exception ignored) {
		}
	}
}
