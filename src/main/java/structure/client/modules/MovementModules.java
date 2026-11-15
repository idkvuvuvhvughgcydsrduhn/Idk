package structure.client.modules;

import java.util.List;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import structure.client.core.Category;
import structure.client.core.HudMod;
import structure.client.core.Mod;
import structure.client.core.Setting;
import structure.client.util.Move;

public final class MovementModules {
    private MovementModules() {}

    public static class Fly extends Mod {
        final Setting.Mode mode = mode("Mode", "Creative", "Motion");
        final Setting.Num speed = num("Speed", 2, 0.5, 10, 0.5);
        public Fly() { super("Fly", "Lets you fly.", Category.MOVEMENT); }
        @Override public void onPreTick() {
            var p = mc.player;
            if (mode.is("Creative")) {
                var a = p.getAbilities();
                a.allowFlying = true;
                a.setFlySpeed((float) (speed.value * 0.05));
            } else {
                double s = speed.value * 0.1;
                double[] d = Move.dir();
                double vy = mc.options.jumpKey.isPressed() ? s : mc.options.sneakKey.isPressed() ? -s : 0;
                p.setVelocity(d[0] * s, vy, d[1] * s);
                p.fallDistance = 0;
            }
        }
        @Override public void onDisable() {
            var p = mc.player;
            if (p == null) return;
            var a = p.getAbilities();
            a.setFlySpeed(0.05f);
            if (!p.isCreative() && !p.isSpectator()) { a.allowFlying = false; a.flying = false; }
        }
    }

    public static class Speed extends Mod {
        final Setting.Num speed = num("Speed", 0.5, 0.1, 3, 0.05);
        final Setting.Bool autoJump = bool("Auto Jump", false);
        public Speed() { super("Speed", "Move faster on the ground and in the air.", Category.MOVEMENT); }
        @Override public void onPreTick() {
            var p = mc.player;
            if (!Move.moving()) return;
            double[] d = Move.dir();
            Vec3d v = p.getVelocity();
            double vy = (autoJump.value && p.isOnGround()) ? 0.42 : v.y;
            p.setVelocity(d[0] * speed.value, vy, d[1] * speed.value);
        }
    }

    public static class HighJump extends Mod {
        final Setting.Num height = num("Height", 0.8, 0.42, 3, 0.02);
        public HighJump() { super("High Jump", "Jump much higher.", Category.MOVEMENT); }
        @Override public void onPreTick() {
            var p = mc.player;
            if (mc.options.jumpKey.isPressed() && p.isOnGround()) {
                Vec3d v = p.getVelocity();
                p.setVelocity(v.x, height.value, v.z);
            }
        }
    }

    public static class Sprint extends Mod {
        public Sprint() { super("Sprint", "Always sprint while moving forward.", Category.MOVEMENT); }
        @Override public void onPreTick() {
            var o = mc.options;
            if (o.forwardKey.isPressed() && !o.sneakKey.isPressed()) o.sprintKey.setPressed(true);
        }
        @Override public void onDisable() { mc.options.sprintKey.setPressed(false); }
    }

    public static class NoFall extends Mod {
        public NoFall() { super("No Fall", "Cancels fall damage.", Category.MOVEMENT); }
        @Override public void onTick() {
            var p = mc.player;
            if (p.fallDistance > 2.5 && !p.getAbilities().flying)
                p.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, p.horizontalCollision));
        }
    }

    public static class MovementHud extends HudMod {
        private double lx, lz, spd;
        private boolean init;
        public MovementHud() { super("Movement HUD", "Speed, sprint and rotation.", Category.MOVEMENT, 0.01, 0.35, 0xA855F7); }
        @Override public void onTick() {
            var p = mc.player;
            if (init) spd = Math.sqrt((p.getX() - lx) * (p.getX() - lx) + (p.getZ() - lz) * (p.getZ() - lz)) * 20;
            lx = p.getX(); lz = p.getZ(); init = true;
        }
        @Override public List<String> lines() {
            var p = mc.player;
            if (p == null) return List.of();
            return List.of(String.format("Speed %.2f b/s", spd),
                    "Sprint " + (p.isSprinting() ? "on" : "off"),
                    String.format("Yaw %.0f  Pitch %.0f", p.getYaw(), p.getPitch()));
        }
    }
}
