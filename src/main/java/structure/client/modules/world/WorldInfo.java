package dev.structure.client.module.world;

import dev.structure.client.module.Category;
import dev.structure.client.module.TextHud;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorldInfo extends TextHud {
	public WorldInfo() {
		super("World Information", "Time, weather and dimension.", Category.WORLD, 0.01, 0.56);
	}

	@Override
	protected List<String> lines(boolean editing) {
		List<String> l = new ArrayList<>();
		if (mc.world == null || mc.player == null) return l;
		long t = mc.world.getTimeOfDay();
		int hh = (int) ((t / 1000 + 6) % 24);
		int mm = (int) ((t % 1000) * 60 / 1000);
		String weather = mc.world.isThundering() ? "Thunder" : mc.world.isRaining() ? "Rain" : "Clear";
		int moon = (int) ((t / 24000L) % 8L);
		l.add(String.format(Locale.ROOT, "Time: %02d:%02d (day %d)", hh, mm, t / 24000));
		l.add("Weather: " + weather);
		l.add("Dimension: " + mc.world.getRegistryKey().getValue().getPath());
		l.add("Moon phase: " + moon);
		l.add("Difficulty: " + mc.world.getDifficulty().name().toLowerCase(Locale.ROOT));
		return l;
	}
}
