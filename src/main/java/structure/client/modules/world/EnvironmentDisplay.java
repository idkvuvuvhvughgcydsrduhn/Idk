package dev.structure.client.module.world;

import dev.structure.client.module.Category;
import dev.structure.client.module.TextHud;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

public class EnvironmentDisplay extends TextHud {
	public EnvironmentDisplay() {
		super("Environment Display", "Light levels around you.", Category.WORLD, 0.01, 0.75);
	}

	@Override
	protected List<String> lines(boolean editing) {
		List<String> l = new ArrayList<>();
		if (mc.world == null || mc.player == null) return l;
		BlockPos pos = mc.player.getBlockPos();
		int bl = mc.world.getLightLevel(LightType.BLOCK, pos);
		int sl = mc.world.getLightLevel(LightType.SKY, pos);
		l.add("Block light: " + bl);
		l.add("Sky light: " + sl);
		l.add("Mobs can spawn: " + (bl == 0 ? "yes" : "no"));
		return l;
	}
}
