package dev.structure.client.module.world;

import dev.structure.client.module.Category;
import dev.structure.client.module.TextHud;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;

public class EntityInfo extends TextHud {
	public EntityInfo() {
		super("Entity Information", "Info about the entity you look at.", Category.WORLD, 0.5, 0.42);
	}

	@Override
	protected List<String> lines(boolean editing) {
		List<String> l = new ArrayList<>();
		if (mc.player == null || !(mc.crosshairTarget instanceof EntityHitResult r)) {
			if (editing) l.add("Entity Information");
			return l;
		}
		Entity e = r.getEntity();
		l.add(e.getName().getString());
		l.add("Type: " + e.getType().getName().getString());
		l.add(e instanceof LivingEntity le ? String.format(Locale.ROOT, "HP: %.1f / %.1f", le.getHealth(), le.getMaxHealth()) : "HP: -");
		l.add(String.format(Locale.ROOT, "Dist: %.1f", mc.player.distanceTo(e)));
		return l;
	}
}
