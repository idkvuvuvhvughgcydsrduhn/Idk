package dev.structure.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.structure.client.util.Keys;
import java.util.function.BooleanSupplier;

public class KeySetting extends Setting<Integer> {
	public KeySetting(String name, int def) {
		super(name, def);
	}

	public int key() {
		return value;
	}

	public String display() {
		return Keys.name(value);
	}

	@Override
	public KeySetting hidden() {
		super.hidden();
		return this;
	}

	@Override
	public KeySetting visibleIf(BooleanSupplier s) {
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
			value = e.getAsInt();
		} catch (Exception ignored) {
		}
	}
}
