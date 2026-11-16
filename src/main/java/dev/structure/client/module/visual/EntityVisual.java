package dev.structure.client.module.visual;

import dev.structure.client.module.Category;
import dev.structure.client.module.Module;
import dev.structure.client.setting.BoolSetting;
import dev.structure.client.setting.ColorSetting;
import dev.structure.client.setting.NumberSetting;
import dev.structure.client.util.Targets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/** Shared target filter, colours and (optional) glow outline for ESP-style modules. */
public abstract class EntityVisual extends Module {
	protected final NumberSetting range;
	protected final BoolSetting players;
	protected final BoolSetting monsters;
	protected final BoolSetting animals;
	protected final ColorSetting playerColor = add(new ColorSetting("Player Color", 0xFFB06CFF));
	protected final ColorSetting monsterColor = add(new ColorSetting("Monster Color", 0xFFFF4D6D));
	protected final ColorSetting animalColor = add(new ColorSetting("Animal Color", 0xFF7CFF8A));
	private final Set<Entity> glowing = Collections.newSetFromMap(new WeakHashMap<>());

	protected EntityVisual(String name, String description, double defRange, boolean pl, boolean mo, boolean an) {
		super(name, description, Category.VISUAL);
		range = add(new NumberSetting("Range", defRange, 8, 128, 4));
		players = add(new BoolSetting("Players", pl));
		monsters = add(new BoolSetting("Monsters", mo));
		animals = add(new BoolSetting("Animals", an));
	}

	/** Subclasses that use the glow outline return true while that mode is active. */
	protected boolean wantsGlow() {
		return false;
	}

	protected boolean match(Entity e) {
		if (e == mc.player || !(e instanceof LivingEntity le) || !le.isAlive() || e instanceof ArmorStandEntity) return false;
		if (e instanceof PlayerEntity) return players.on();
		return Targets.isHostile(e) ? monsters.on() : animals.on();
	}

	protected int colorFor(Entity e) {
		if (e instanceof PlayerEntity) return playerColor.argb();
		return Targets.isHostile(e) ? monsterColor.argb() : animalColor.argb();
	}

	protected List<LivingEntity> visible() {
		List<LivingEntity> out = new ArrayList<>();
		ClientPlayerEntity p = mc.player;
		if (p == null || mc.world == null) return out;
		double r = range.d();
		for (Entity e : mc.world.getEntities()) {
			if (match(e) && p.distanceTo(e) <= r) out.add((LivingEntity) e);
		}
		out.sort(Comparator.comparingDouble(p::distanceTo));
		return out;
	}

	/** Entity hit box interpolated to the current frame. */
	protected static Box lerpedBox(Entity e, float delta) {
		Vec3d lp = e.getLerpedPos(delta);
		return e.getBoundingBox().offset(lp.x - e.getX(), lp.y - e.getY(), lp.z - e.getZ());
	}

	@Override
	public void onTick() {
		ClientPlayerEntity p = mc.player;
		if (p == null || mc.world == null) return;
		boolean glow = wantsGlow();
		double r = range.d();
		for (Entity e : mc.world.getEntities()) {
			if (e == p) continue;
			if (glow && match(e) && p.distanceTo(e) <= r) {
				e.setGlowing(true);
				glowing.add(e);
			} else if (glowing.remove(e)) {
				e.setGlowing(false);
			}
		}
	}

	@Override
	public void onDisable() {
		for (Entity e : new ArrayList<>(glowing)) e.setGlowing(false);
		glowing.clear();
	}
}
