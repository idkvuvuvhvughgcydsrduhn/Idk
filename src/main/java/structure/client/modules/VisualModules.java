package structure.client.modules;

import java.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import structure.client.core.Category;
import structure.client.core.Mod;
import structure.client.core.Setting;

public final class VisualModules {
    private VisualModules() {}

    /** ESP through the game's glow outline: visible through walls. */
    public abstract static class GlowBase extends Mod {
        protected final Setting.Num range = num("Range", 64, 8, 128, 4);
        private final Set<Entity> tracked = Collections.newSetFromMap(new WeakHashMap<>());
        protected GlowBase(String n, String d) { super(n, d, Category.VISUAL); }
        protected abstract boolean match(Entity e);

        @Override public void onTick() {
            var p = mc.player;
            for (Entity e : mc.world.getEntities()) {
                if (e == p) continue;
                if (match(e) && p.distanceTo(e) <= range.value) { e.setGlowing(true); tracked.add(e); }
                else if (tracked.remove(e)) e.setGlowing(false);
            }
        }
        @Override public void onDisable() {
            for (Entity e : new ArrayList<>(tracked)) e.setGlowing(false);
            tracked.clear();
        }
    }

    public static class Esp extends GlowBase {
        public Esp() { super("ESP", "Highlights players through walls."); }
        @Override protected boolean match(Entity e) { return e instanceof PlayerEntity; }
    }

    public static class ItemEsp extends GlowBase {
        public ItemEsp() { super("Item ESP", "Highlights dropped items through walls."); }
        @Override protected boolean match(Entity e) { return e instanceof ItemEntity; }
    }

    public static class EntityHighlight extends GlowBase {
        final Setting.Bool monsters = bool("Monsters", true);
        final Setting.Bool animals = bool("Animals", true);
        public EntityHighlight() { super("Entity Highlight", "Highlights mobs and animals through walls."); }
        @Override protected boolean match(Entity e) {
            if (!(e instanceof LivingEntity) || e instanceof PlayerEntity) return false;
            return e instanceof Monster ? monsters.value : animals.value;
        }
    }

    public static class Fullbright extends Mod {
        private boolean given;
        public Fullbright() { super("Fullbright", "Maximum brightness everywhere.", Category.VISUAL); }
        @Override public void onTick() {
            var p = mc.player;
            var cur = p.getStatusEffect(StatusEffects.NIGHT_VISION);
            if (cur == null || cur.getDuration() < 300) {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1000, 0, false, false, false));
                given = true;
            }
        }
        @Override public void onDisable() {
            if (given && mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            given = false;
        }
    }
}
