package structure.client.modules;

import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.LightType;
import structure.client.core.Category;
import structure.client.core.HudMod;

public final class WorldModules {
    private WorldModules() {}

    public static class WorldInfo extends HudMod {
        public WorldInfo() { super("World Information", "Biome, time, weather and dimension.", Category.WORLD, 0.01, 0.55, 0xA855F7); }
        @Override public List<String> lines() {
            var w = mc.world; var p = mc.player;
            if (w == null || p == null) return List.of();
            String biome = w.getBiome(p.getBlockPos()).getKey().map(k -> k.getValue().getPath()).orElse("?");
            long t = w.getTimeOfDay();
            int hh = (int) ((t / 1000 + 6) % 24), mm = (int) ((t % 1000) * 60 / 1000);
            String weather = w.isThundering() ? "Thunder" : w.isRaining() ? "Rain" : "Clear";
            return List.of("Biome: " + biome, String.format("Time: %02d:%02d (day %d)", hh, mm, t / 24000),
                    "Weather: " + weather, "Dim: " + w.getRegistryKey().getValue().getPath());
        }
    }

    public static class BlockInfo extends HudMod {
        public BlockInfo() { super("Block Information", "Info about the block you look at.", Category.WORLD, 0.5, 0.3, 0xA855F7); }
        @Override public List<String> lines() {
            if (!(mc.crosshairTarget instanceof BlockHitResult b) || b.getType() != HitResult.Type.BLOCK) return List.of();
            var pos = b.getBlockPos();
            var st = mc.world.getBlockState(pos);
            return List.of(st.getBlock().getName().getString(),
                    pos.getX() + " " + pos.getY() + " " + pos.getZ(),
                    "Light: " + mc.world.getLightLevel(pos.offset(b.getSide())),
                    String.format("Hardness: %.1f", st.getHardness(mc.world, pos)));
        }
    }

    public static class EntityInfo extends HudMod {
        public EntityInfo() { super("Entity Information", "Info about the entity you look at.", Category.WORLD, 0.5, 0.42, 0xA855F7); }
        @Override public List<String> lines() {
            if (!(mc.crosshairTarget instanceof EntityHitResult r)) return List.of();
            var e = r.getEntity();
            String hp = e instanceof LivingEntity le ? String.format("HP: %.1f / %.1f", le.getHealth(), le.getMaxHealth()) : "HP: -";
            return List.of(e.getName().getString(), "Type: " + e.getType().getName().getString(), hp,
                    String.format("Dist: %.1f", mc.player.distanceTo(e)));
        }
    }

    public static class Environment extends HudMod {
        public Environment() { super("Environment Display", "Light levels around you.", Category.WORLD, 0.01, 0.75, 0xA855F7); }
        @Override public List<String> lines() {
            var w = mc.world; var p = mc.player;
            if (w == null || p == null) return List.of();
            var pos = p.getBlockPos();
            int bl = w.getLightLevel(LightType.BLOCK, pos), sl = w.getLightLevel(LightType.SKY, pos);
            return List.of("Block light: " + bl, "Sky light: " + sl, "Mobs can spawn: " + (bl == 0 ? "yes" : "no"));
        }
    }
}
