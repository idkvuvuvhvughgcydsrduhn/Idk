package structure.client.util;

import java.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import structure.client.StructureClient;
import structure.client.modules.CombatModules;

public final class Combat {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public static LivingEntity lastTarget;
    public static long lastTime;
    private Combat() {}

    public static List<LivingEntity> targets(double range, boolean players, boolean mobs, boolean animals, boolean walls) {
        List<LivingEntity> out = new ArrayList<>();
        var p = mc.player;
        if (p == null || mc.world == null) return out;
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le) || le == p || !le.isAlive() || e instanceof ArmorStandEntity) continue;
            boolean ok = (players && e instanceof PlayerEntity) || (mobs && e instanceof Monster) || (animals && e instanceof PassiveEntity);
            if (!ok || p.distanceTo(le) > range) continue;
            if (!walls && !p.canSee(le)) continue;
            out.add(le);
        }
        out.sort(Comparator.comparingDouble(x -> p.distanceTo(x)));
        return out;
    }

    public static void face(Entity t) {
        var p = mc.player;
        double dx = t.getX() - p.getX();
        double dy = (t.getY() + t.getHeight() * 0.6) - p.getEyeY();
        double dz = t.getZ() - p.getZ();
        p.setYaw((float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
        p.setPitch((float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
    }

    public static void attack(LivingEntity t, boolean rotate) {
        if (rotate) face(t);
        var c = StructureClient.modules.get(CombatModules.Criticals.class);
        if (c != null && c.isEnabled()) c.crit();
        mc.interactionManager.attackEntity(mc.player, t);
        mc.player.swingHand(Hand.MAIN_HAND);
        lastTarget = t;
        lastTime = System.currentTimeMillis();
    }

    /** Current target: last attacked (3 s) or the entity under the crosshair. */
    public static LivingEntity current() {
        if (lastTarget != null && lastTarget.isAlive() && System.currentTimeMillis() - lastTime < 3000) return lastTarget;
        if (mc.crosshairTarget instanceof EntityHitResult r && r.getEntity() instanceof LivingEntity le) return le;
        return null;
    }
}
