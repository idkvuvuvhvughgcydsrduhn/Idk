package structure.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import structure.client.core.ConfigManager;
import net.minecraft.client.gui.screen.TitleScreen;
import structure.client.core.ModuleManager;
import structure.client.gui.StructureTitleScreen;
import structure.client.modules.UtilityModules;

public class StructureClient implements ClientModInitializer {
    public static ModuleManager modules;
    public static ConfigManager config;
    private int ticks;

    @Override
    public void onInitializeClient() {
        modules = new ModuleManager();
        config = new ConfigManager();
        config.init();
        ClientTickEvents.START_CLIENT_TICK.register(c -> modules.preTick());
        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            var mm = modules.get(UtilityModules.MainMenu.class);
            if (mm != null && mm.isEnabled() && c.world == null && c.currentScreen != null
                    && c.currentScreen.getClass() == TitleScreen.class) c.setScreen(new StructureTitleScreen());
            modules.pollKeys();
            modules.tick();
            if (++ticks % 1200 == 0) config.save(config.current); // auto save every minute
        });
        HudRenderCallback.EVENT.register((ctx, tc) -> modules.renderHud(ctx));
        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> config.save(config.current));
    }
}
