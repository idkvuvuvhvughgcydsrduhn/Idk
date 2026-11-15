package structure.client.core;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import structure.client.modules.*;
import structure.client.util.Input;

public class ModuleManager {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public final List<Mod> all = new ArrayList<>();

    public ModuleManager() {
        add(new CombatModules.KillAura());
        add(new CombatModules.MobAura());
        add(new CombatModules.AutoClicker());
        add(new CombatModules.Criticals());
        add(new CombatModules.TargetHud());
        add(new CombatModules.AutoWeapon());
        add(new CombatModules.CombatNotifications());
        add(new MovementModules.Fly());
        add(new MovementModules.Speed());
        add(new MovementModules.HighJump());
        add(new MovementModules.Sprint());
        add(new MovementModules.NoFall());
        add(new MovementModules.MovementHud());
        add(new VisualModules.Esp());
        add(new VisualModules.Fullbright());
        add(new VisualModules.ItemEsp());
        add(new VisualModules.EntityHighlight());
        add(new PlayerModules.AutoTool());
        add(new PlayerModules.ArmorHud());
        add(new PlayerModules.InventoryHud());
        add(new PlayerModules.Coordinates());
        add(new PlayerModules.PotionHud());
        add(new WorldModules.WorldInfo());
        add(new WorldModules.BlockInfo());
        add(new WorldModules.EntityInfo());
        add(new WorldModules.Environment());
        add(new UtilityModules.ClickGui());
        add(new UtilityModules.HudEditor());
        add(new UtilityModules.Panic());
        add(new UtilityModules.Notifications());
        add(new UtilityModules.MainMenu());
        add(new UtilityModules.ClientInfo());
        // defaults for a fresh install
        get(UtilityModules.Notifications.class).loadEnabled(true);
        get(UtilityModules.ClientInfo.class).loadEnabled(true);
        get(UtilityModules.MainMenu.class).loadEnabled(true);
        get(PlayerModules.Coordinates.class).loadEnabled(true);
        get(PlayerModules.ArmorHud.class).loadEnabled(true);
    }

    private void add(Mod m) { all.add(m); }

    public <T extends Mod> T get(Class<T> c) {
        for (Mod m : all) if (c.isInstance(m)) return c.cast(m);
        return null;
    }

    public List<Mod> in(Category c) {
        List<Mod> out = new ArrayList<>();
        for (Mod m : all) if (m.category == c) out.add(m);
        return out;
    }

    public List<HudMod> huds() {
        List<HudMod> out = new ArrayList<>();
        for (Mod m : all) if (m instanceof HudMod h) out.add(h);
        return out;
    }

    private void run(Mod m, boolean pre) {
        try { if (pre) m.onPreTick(); else m.onTick(); }
        catch (Throwable t) { t.printStackTrace(); Notify.muted = true; m.setEnabled(false); Notify.muted = false; Notify.chat(m.name + " crashed and was disabled."); }
    }

    public void preTick() {
        if (mc.player == null || mc.world == null) return;
        for (int i = 0; i < all.size(); i++) { Mod m = all.get(i); if (m.isEnabled()) run(m, true); }
    }

    public void tick() {
        if (mc.player == null || mc.world == null) return;
        for (int i = 0; i < all.size(); i++) { Mod m = all.get(i); if (m.isEnabled()) run(m, false); }
    }

    /** Keybind manager: toggles modules whose bound key was just pressed. */
    public void pollKeys() {
        if (mc.currentScreen != null || mc.player == null) return;
        for (int i = 0; i < all.size(); i++) {
            Mod m = all.get(i);
            if (m.key <= 0) { m.keyHeld = false; continue; }
            boolean d = Input.key(m.key);
            if (d && !m.keyHeld) m.toggle();
            m.keyHeld = d;
        }
    }

    public void renderHud(DrawContext c) {
        if (mc.player == null || mc.options.hudHidden) return;
        for (Mod m : all) {
            if (m instanceof HudMod h && h.isEnabled()) {
                try { h.draw(c); } catch (Throwable ignored) {}
            }
        }
    }

    public void disableAll() {
        Notify.muted = true;
        for (Mod m : all) if (m.isEnabled()) m.setEnabled(false);
        Notify.muted = false;
    }
}
