package structure.client.core;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;

/** Base class of every module. */
public abstract class Mod {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();
    public final String name, description;
    public final Category category;
    public int key = -1;
    public boolean keyHeld;
    private boolean enabled;
    public final List<Setting> settings = new ArrayList<>();

    protected Mod(String name, String description, Category category) {
        this.name = name; this.description = description; this.category = category;
    }

    protected Setting.Bool bool(String n, boolean d) { var s = new Setting.Bool(n, d); settings.add(s); return s; }
    protected Setting.Num num(String n, double d, double min, double max, double step) { var s = new Setting.Num(n, d, min, max, step); settings.add(s); return s; }
    protected Setting.Mode mode(String n, String... m) { var s = new Setting.Mode(n, m); settings.add(s); return s; }
    protected Setting.Color color(String n, int rgb) { var s = new Setting.Color(n, rgb); settings.add(s); return s; }

    public boolean isEnabled() { return enabled; }
    public void toggle() { setEnabled(!isEnabled()); }

    public void setEnabled(boolean v) {
        if (v == enabled) return;
        enabled = v;
        if (v) onEnable(); else onDisable();
        Notify.toggled(this);
    }

    /** Used by the config loader: no chat notification. */
    public void loadEnabled(boolean v) {
        if (v == enabled) return;
        enabled = v;
        if (!v) onDisable();
    }

    public void onEnable() {}
    public void onDisable() {}
    /** Runs before the player ticks. */
    public void onPreTick() {}
    /** Runs after everything ticked. */
    public void onTick() {}
}
