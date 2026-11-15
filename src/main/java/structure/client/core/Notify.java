package structure.client.core;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import structure.client.StructureClient;
import structure.client.modules.UtilityModules;

public final class Notify {
    public static boolean muted;
    private Notify() {}

    public static void chat(String s) {
        var p = MinecraftClient.getInstance().player;
        if (p != null) p.sendMessage(Text.literal("\u00a75[Structure] \u00a7f" + s), false);
    }

    public static void toggled(Mod m) {
        if (muted || m instanceof TriggerMod || StructureClient.modules == null) return;
        var n = StructureClient.modules.get(UtilityModules.Notifications.class);
        if (n != null && n.isEnabled() && n.toggleMsgs.value)
            chat(m.name + (m.isEnabled() ? " \u00a7aenabled" : " \u00a7cdisabled"));
    }
}
