package dev.structure.client.module.player;

import dev.structure.client.module.Category;
import dev.structure.client.module.TextHud;
import dev.structure.client.setting.BoolSetting;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.network.ClientPlayerEntity;

public class Coordinates extends TextHud {
	private final BoolSetting biome = add(new BoolSetting("Show Biome", true));
	private final BoolSetting other = add(new BoolSetting("Nether / Overworld", false));

	public Coordinates() {
		super("Coordinates", "Shows your position and direction.", Category.PLAYER, 0.01, 0.15);
	}

	@Override
	protected List<String> lines(boolean editing) {
		List<String> l = new ArrayList<>();
		ClientPlayerEntity p = mc.player;
		if (p == null || mc.world == null) return l;
		l.add(String.format(Locale.ROOT, "X: %.1f", p.getX()));
		l.add(String.format(Locale.ROOT, "Y: %.1f", p.getY()));
		l.add(String.format(Locale.ROOT, "Z: %.1f", p.getZ()));
		l.add("Facing: " + p.getHorizontalFacing().asString());
		if (other.on()) {
			String dim = mc.world.getRegistryKey().getValue().getPath();
			if (dim.contains("nether")) l.add(String.format(Locale.ROOT, "Overworld: %.0f, %.0f", p.getX() * 8, p.getZ() * 8));
			else if (dim.equals("overworld")) l.add(String.format(Locale.ROOT, "Nether: %.0f, %.0f", p.getX() / 8, p.getZ() / 8));
		}
		if (biome.on()) {
			String b = mc.world.getBiome(p.getBlockPos()).getKey().map(k -> k.getValue().getPath()).orElse("?");
			l.add("Biome: " + b.replace('_', ' '));
		}
		return l;
	}
}
