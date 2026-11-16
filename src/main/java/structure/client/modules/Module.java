package dev.structure.client.module;

import dev.structure.client.StructureClient;
import dev.structure.client.config.ConfigManager;
import dev.structure.client.notify.NotificationManager;
import dev.structure.client.setting.KeySetting;
import dev.structure.client.setting.Setting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public abstract class Module {
	protected static final MinecraftClient mc = MinecraftClient.getInstance();

	public final String name;
	public final String description;
	public final Category category;
	public final KeySetting bind = new KeySetting("Keybind", -1);
	private final List<Setting<?>> settings = new ArrayList<>();
	private boolean enabled;

	protected Module(String name, String description, Category category) {
		this.name = name;
		this.description = description;
		this.category = category;
		this.settings.add(bind);
	}

	protected <S extends Setting<?>> S add(S s) {
		settings.add(s);
		return s;
	}

	public List<Setting<?>> settings() {
		return settings;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void toggle() {
		setEnabled(!enabled, true);
	}

	public void setEnabled(boolean on) {
		setEnabled(on, true);
	}

	public void setEnabled(boolean on, boolean notify) {
		if (on == enabled) return;
		enabled = on;
		try {
			if (on) onEnable();
			else onDisable();
		} catch (Throwable t) {
			StructureClient.LOGGER.error("Module {} failed in enable/disable", name, t);
		}
		if (notify) NotificationManager.toggle(this);
		ConfigManager.markDirty();
	}

	protected void defaultBind(int key) {
		bind.setDefault(key);
	}

	public void onEnable() {
	}

	public void onDisable() {
	}

	/** Runs at the very start of a client tick (before the player ticks): movement / combat logic. */
	public void onPreTick() {
	}

	/** Runs at the end of a client tick: bookkeeping, glow sync, HUD data. */
	public void onTick() {
	}

	/** Used by the config loader: restores state without notifications. */
	public void loadEnabled(boolean v) {
		setEnabled(v, false);
	}

	/** 2D overlay drawing (ESP etc.), runs while in a world. */
	public void onRender(DrawContext ctx, float delta) {
	}

	/** Short text shown next to the name in the module list HUD. */
	public String info() {
		return "";
	}

	protected static boolean inWorld() {
		return mc.player != null && mc.world != null;
	}
}
