package structure.client.modules;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import structure.client.core.Category;
import structure.client.core.HudMod;
import structure.client.core.Mod;

public final class PlayerModules {
    private PlayerModules() {}

    public static class AutoTool extends Mod {
        public AutoTool() { super("Auto Tool", "Picks the fastest tool for the block you mine.", Category.PLAYER); }
        @Override public void onTick() {
            if (mc.currentScreen != null || !mc.options.attackKey.isPressed()) return;
            if (!(mc.crosshairTarget instanceof BlockHitResult b) || b.getType() != HitResult.Type.BLOCK) return;
            var st = mc.world.getBlockState(b.getBlockPos());
            if (st.isAir()) return;
            var inv = mc.player.getInventory();
            int best = inv.getSelectedSlot();
            float bs = inv.getStack(best).getMiningSpeedMultiplier(st);
            for (int i = 0; i < 9; i++) {
                float s = inv.getStack(i).getMiningSpeedMultiplier(st);
                if (s > bs + 0.01f) { bs = s; best = i; }
            }
            if (best != inv.getSelectedSlot()) inv.setSelectedSlot(best);
        }
    }

    public static class ArmorHud extends HudMod {
        public ArmorHud() { super("Armor HUD", "Shows your armor and durability.", Category.PLAYER, 0.5, 0.8, 0xA855F7); }
        @Override protected void body(DrawContext c) {
            var p = mc.player;
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            w = 4 * 18 + 2; h = 22;
            c.fill(0, 0, w, h, 0x66000000);
            int x = 1;
            for (EquipmentSlot s : slots) {
                ItemStack st = p.getEquippedStack(s);
                if (!st.isEmpty()) { c.drawItem(st, x, 3); c.drawStackOverlay(mc.textRenderer, st, x, 3); }
                x += 18;
            }
        }
    }

    public static class InventoryHud extends HudMod {
        public InventoryHud() { super("Inventory HUD", "Shows your inventory on screen.", Category.PLAYER, 0.99, 0.5, 0xA855F7); }
        @Override protected void body(DrawContext c) {
            var inv = mc.player.getInventory();
            w = 9 * 18 + 4; h = 3 * 18 + 4;
            c.fill(0, 0, w, h, 0x77000000);
            for (int i = 0; i < 27; i++) {
                ItemStack st = inv.getStack(9 + i);
                if (st.isEmpty()) continue;
                int x = 2 + (i % 9) * 18, y = 2 + (i / 9) * 18;
                c.drawItem(st, x, y);
                c.drawStackOverlay(mc.textRenderer, st, x, y);
            }
        }
    }

    public static class Coordinates extends HudMod {
        public Coordinates() { super("Coordinates", "Shows your position and direction.", Category.PLAYER, 0.01, 0.15, 0xA855F7); }
        @Override public List<String> lines() {
            var p = mc.player;
            if (p == null) return List.of();
            return List.of(String.format("X: %.1f", p.getX()), String.format("Y: %.1f", p.getY()),
                    String.format("Z: %.1f", p.getZ()), "Facing: " + p.getHorizontalFacing().asString());
        }
    }

    public static class PotionHud extends HudMod {
        public PotionHud() { super("Potion HUD", "Shows your active effects.", Category.PLAYER, 0.99, 0.2, 0xA855F7); }
        @Override public List<String> lines() {
            var p = mc.player;
            if (p == null) return List.of();
            List<String> out = new ArrayList<>();
            for (var e : p.getStatusEffects()) {
                String t;
                if (e.isInfinite()) t = "inf";
                else { int s = e.getDuration() / 20; t = (s / 60) + ":" + String.format("%02d", s % 60); }
                out.add(e.getEffectType().value().getName().getString() + " " + (e.getAmplifier() + 1) + "  " + t);
            }
            return out;
        }
    }
}
