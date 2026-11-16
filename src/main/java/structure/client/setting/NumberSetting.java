package dev.structure.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Locale;
import java.util.function.BooleanSupplier;

public class NumberSetting extends Setting<Double> {
	public final double min;
	public final double max;
	public final double step;

	public NumberSetting(String name, double def, double min, double max, double step) {
		super(name, def);
		this.min = min;
		this.max = max;
		this.step = step;
	}

	private double fix(double v) {
		double c = Math.max(min, Math.min(max, v));
		if (step > 0) c = Math.round(c / step) * step;
		c = Math.round(c * 100000.0) / 100000.0;
		return Math.max(min, Math.min(max, c));
	}

	@Override
	public void set(Double v) {
		super.set(fix(v));
	}

	public double d() {
		return value;
	}

	public float f() {
		return value.floatValue();
	}

	public int i() {
		return (int) Math.round(value);
	}

	public double progress() {
		return max <= min ? 0 : (value - min) / (max - min);
	}

	public String display() {
		if (step >= 1.0) return String.valueOf(i());
		if (step >= 0.1) return String.format(Locale.ROOT, "%.1f", value);
		return String.format(Locale.ROOT, "%.2f", value);
	}

	@Override
	public NumberSetting hidden() {
		super.hidden();
		return this;
	}

	@Override
	public NumberSetting visibleIf(BooleanSupplier s) {
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
			value = fix(e.getAsDouble());
		} catch (Exception ignored) {
		}
	}
}
