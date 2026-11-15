package structure.client.modules;

import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import structure.client.core.Category;
import structure.client.core.HudMod;
import structure.client.core.Mod;
import structure.client.core.Notify;
import structure.client.core.Setting;
import structure.client.util.Combat;

public final class CombatModules {
    private CombatModules() {}

    public abstract static class Aura extends Mod {
        protected final Setting.Mode mode = mode("Mode", "Single", "Multi");
        protected final Setting.Num range = num("Range", 4.5, 2, 8, 0.1);
        protected final Setting.Num cps = num("CPS", 12, 1, 20, 1);
        protected final Setting.Bool rotate = bool("Rotate", true);
        protected final Setting.Bool walls = bool("Through Walls", false);
        private long last;

        protected Aura(String n, String d) { super(n, d, Category.COMBAT); }
        protected abstract List<LivingEntity> find();

        @Override public void onTick() {
            if (mc.currentScreen != null) return;
            long now = System.currentTimeMillis();
            if (now - last < 1000.0 / cps.value) return;
            List<LivingEntity> t = find();
            if (t.isEmpty()) return;
            last = now;
            if (mode.is("Multi")) {
                for (int i = 0; i < Math.min(t.size(), 5); i++) Combat.attack(t.get(i), rotate.value && i == 0);
            } else Combat.attack(t.get(0), rotate.value);
        }
    }

    public static class KillAura extends Aura {
        final Setting.Bool players = bool("Players", true);
        final Setting.Bool mobs = bool("Mobs", false);
        final Setting.Bool animals = bool("Animals", false);
        public KillAura() { super("KillAura", "Automatically attacks nearby players."); }
        @Override protected List<LivingEntity> find() { return Combat.targets(range.value, players.value, mobs.value, animals.value, walls.value); }
    }

    public static class MobAura extends Aura {
        final Setting.Bool monsters = bool("Monsters", true);
        final Setting.Bool animals = bool("Animals", false);
        public MobAura() { super("MobAura", "Automatically attacks nearby mobs."); }
        @Override protected List<LivingEntity> find() { return Combat.targets(range.value, false, monsters.value, animals.value, walls.value); }
    }

    public static class AutoClicker extends Mod {
        final Setting.Num cps = num("CPS", 14, 1, 20, 1);
        final Setting.Bool swingMiss = bool("Swing On Miss", true);
        private long last;
        public AutoClicker() { super("AutoClicker", "Clicks for you while you hold attack.", Category.COMBAT); }
        @Override public void onTick() {
            if (mc.currentScreen != null || !mc.options.attackKey.isPressed()) return;
            long now = System.currentTimeMillis();
            if (now - last < 1000.0 / cps.value) return;
            last = now;
            if (mc.crosshairTarget instanceof EntityHitResult r && r.getEntity() instanceof LivingEntity le) Combat.attack(le, false);
            else if (swingMiss.value) mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    public static class Criticals extends Mod {
        public Criticals() { super("Criticals", "Every hit becomes a critical hit.", Category.COMBAT); }
        public void crit() {
            var p = mc.player;
            if (!p.isOnGround() || p.isTouchingWater() || p.isInLava()) return;
            double x = p.getX(), y = p.getY(), z = p.getZ();
            boolean hc = p.horizontalCollision;
            p.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0625, z, false, hc));
            p.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false, hc));
        }
    }

    public static class TargetHud extends HudMod {
        public TargetHud() { super("Target HUD", "Shows info about your target.", Category.COMBAT, 0.5, 0.62, 0xA855F7); }
        @Override public List<String> lines() {
            LivingEntity t = Combat.current();
            if (t == null) return List.of();
            return List.of(t.getName().getString(),
                    String.format("HP %.1f / %.1f", t.getHealth(), t.getMaxHealth()),
                    String.format("Dist %.1f", mc.player.distanceTo(t)));
        }
    }

    public static class AutoWeapon extends Mod {
        public AutoWeapon() { super("Auto Weapon", "Switches to your best weapon when fighting.", Category.COMBAT); }
        @Override public void onTick() {
            if (mc.currentScreen != null || Combat.current() == null) return;
            var inv = mc.player.getInventory();
            int best = -1, bestScore = 0;
            for (int i = 0; i < 9; i++) {
                ItemStack s = inv.getStack(i);
                int sc = s.isIn(ItemTags.SWORDS) ? 2_000_000 : s.isIn(ItemTags.AXES) ? 1_000_000 : 0;
                if (sc > 0) { sc += s.getMaxDamage(); if (sc > bestScore) { bestScore = sc; best = i; } }
            }
            if (best >= 0 && best != inv.getSelectedSlot()) inv.setSelectedSlot(best);
        }
    }

    public static class CombatNotifications extends Mod {
        private LivingEntity last;
        public CombatNotifications() { super("Combat Notifications", "Chat message when you start attacking someone.", Category.COMBAT); }
        @Override public void onTick() {
            LivingEntity t = Combat.lastTarget;
            if (t != null && t != last && System.currentTimeMillis() - Combat.lastTime < 500) {
                last = t;
                Notify.chat("Attacking \u00a7d" + t.getName().getString());
            }
        }
    }
}
