package structure.client.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public abstract class Setting {
    public final String name;
    protected Setting(String name) { this.name = name; }
    public abstract JsonElement save();
    public abstract void load(JsonElement e);

    public static class Bool extends Setting {
        public boolean value;
        public Bool(String n, boolean v) { super(n); value = v; }
        public JsonElement save() { return new JsonPrimitive(value); }
        public void load(JsonElement e) { value = e.getAsBoolean(); }
    }

    public static class Num extends Setting {
        public double value;
        public final double min, max, step;
        public Num(String n, double v, double min, double max, double step) {
            super(n); this.min = min; this.max = max; this.step = step; set(v);
        }
        public void set(double v) {
            v = Math.max(min, Math.min(max, v));
            if (step > 0) v = Math.round(v / step) * step;
            value = Math.max(min, Math.min(max, v));
        }
        public int i() { return (int) Math.round(value); }
        public JsonElement save() { return new JsonPrimitive(value); }
        public void load(JsonElement e) { set(e.getAsDouble()); }
    }

    public static class Mode extends Setting {
        public final String[] modes;
        public int index;
        public Mode(String n, String... modes) { super(n); this.modes = modes; }
        public String get() { return modes[index]; }
        public boolean is(String s) { return modes[index].equals(s); }
        public void cycle() { index = (index + 1) % modes.length; }
        public JsonElement save() { return new JsonPrimitive(get()); }
        public void load(JsonElement e) {
            for (int i = 0; i < modes.length; i++) if (modes[i].equals(e.getAsString())) index = i;
        }
    }

    public static class Color extends Setting {
        public int rgb;
        public Color(String n, int rgb) { super(n); this.rgb = rgb & 0xFFFFFF; }
        public JsonElement save() { return new JsonPrimitive(rgb); }
        public void load(JsonElement e) { rgb = e.getAsInt() & 0xFFFFFF; }
    }
}
