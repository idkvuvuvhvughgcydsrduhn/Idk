package structure.client.modules;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.lwjgl.glfw.GLFW;
import structure.client.StructureClient;
import structure.client.core.Category;
import structure.client.core.HudMod;
import structure.client.core.Mod;
import structure.client.core.Notify;
import structure.client.core.Setting;
import structure.client.core.TriggerMod;
import structure.client.gui.ClickGuiScreen;
import structure.client.gui.HudEditorScreen;

public final class UtilityModules {
    private UtilityModules() {}

    public static class ClickGui extends TriggerMod {
        public ClickGui() { super("ClickGUI", "Opens the Structure Client menu.", Category.UTILITY); key = GLFW.GLFW_KEY_RIGHT_SHIFT; }
        @Override protected void fire() { mc.setScreen(new ClickGuiScreen()); }
    }

    public static class HudEditor extends TriggerMod {
        public HudEditor() { super("HUD Editor", "Drag, resize and recolor HUD elements.", Category.UTILITY); }
        @Override protected void fire() { mc.setScreen(new HudEditorScreen()); }
    }

    public static class Panic extends TriggerMod {
        public Panic() { super("Panic Button", "Instantly disables every module.", Category.UTILITY); key = GLFW.GLFW_KEY_DELETE; }
        @Override protected void fire() {
            StructureClient.modules.disableAll();
            mc.setScreen(null);
            Notify.chat("Panic: all modules disabled.");
        }
    }

    public static class MainMenu extends Mod {
        public MainMenu() { super("Custom Main Menu", "Replaces the title screen with the Structure menu.", Category.UTILITY); }
    }

    public static class Notifications extends Mod {
        public final Setting.Bool toggleMsgs = bool("Toggle Messages", true);
        public Notifications() { super("Notifications", "Chat messages when modules change.", Category.UTILITY); }
    }

    public static class ClientInfo extends HudMod {
        public ClientInfo() { super("Client Info", "FPS, ping and clock.", Category.UTILITY, 0.01, 0.01, 0xA855F7); }
        @Override public List<String> lines() {
            if (mc.player == null) return List.of();
            var e = mc.getNetworkHandler() == null ? null : mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            return List.of("FPS: " + mc.getCurrentFps(), "Ping: " + (e == null ? 0 : e.getLatency()) + "ms",
                    "Time: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }
    }
}
