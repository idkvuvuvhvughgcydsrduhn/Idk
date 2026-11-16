package dev.structure.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.function.BooleanSupplier;
import net.minecraft.util.math.MathHelper;

public class ColorSetting extends Setting<Integer> {
	public float h;
	public float s;
	public float v;

	public ColorSetting(String name, int argb) {
		super(name, 0xFF000000 | argb);
		updateHsv();
	}

	@Override
	public void set(Integer c) {
		super.set(0xFF000000 | c);
		updateHsv();
	}

	public int argb() {
		return value;
	}

	public int withAlpha(int alpha) {
		return (alpha << 24) | (value & 0xFFFFFF);
	}

	public void setHsv(float h, float s, float v) {
		this.h = MathHelper.clamp(h, 0f, 1f);
		this.s = MathHelper.clamp(s, 0f, 1f);
		this.v = MathHelper.clamp(v, 0f, 1f);
		super.set(0xFF000000 | MathHelper.hsvToRgb(this.h, this.s, this.v));
	}

	private void updateHsv() {
		int c = value;
		float r = ((c >> 16) & 255) / 255f;
		float g = ((c >> 8) & 255) / 255f;
		float b = (c & 255) / 255f;
		float max = Math.max(r, Math.max(g, b));
		float min = Math.min(r, Math.min(g, b));
		float d = max - min;
		float hh = this.h;
		if (d > 1e-5f) {
			if (max == r) hh = ((g - b) / d) % 6f;
			else if (max == g) hh = (b - r) / d + 2f;
			else hh = (r - g) / d + 4f;
			hh /= 6f;
			if (hh < 0f) hh += 1f;
		}
		this.h = hh;
		this.s = max <= 0f ? 0f : d / max;
		this.v = max;
	}

	@Override
	public ColorSetting hidden() {
		super.hidden();
		return this;
	}

	@Override
	public ColorSetting visibleIf(BooleanSupplier s) {
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
			value = 0xFF000000 | e.getAsInt();
			updateHsv();
		} catch (Exception ignored) {
		}
	}
}
