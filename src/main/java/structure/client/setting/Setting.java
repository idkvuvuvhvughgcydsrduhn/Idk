package dev.structure.client.setting;

import com.google.gson.JsonElement;
import dev.structure.client.config.ConfigManager;
import java.util.function.BooleanSupplier;

public abstract class Setting<T> {
	public final String name;
	protected T value;
	protected T def;
	private boolean hidden;
	private BooleanSupplier visibleIf;

	protected Setting(String name, T def) {
		this.name = name;
		this.value = def;
		this.def = def;
	}

	public T get() {
		return value;
	}

	public void set(T v) {
		this.value = v;
		ConfigManager.markDirty();
	}

	public void reset() {
		set(def);
	}

	/** Changes the value that reset() restores (used for per-module default keybinds). */
	public void setDefault(T v) {
		this.def = v;
		this.value = v;
	}

	public Setting<T> hidden() {
		this.hidden = true;
		return this;
	}

	public Setting<T> visibleIf(BooleanSupplier s) {
		this.visibleIf = s;
		return this;
	}

	public boolean isVisible() {
		return !hidden && (visibleIf == null || visibleIf.getAsBoolean());
	}

	public abstract JsonElement save();

	public abstract void load(JsonElement e);
}
