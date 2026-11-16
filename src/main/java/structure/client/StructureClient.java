package dev.structure.client;

import dev.structure.client.config.ConfigManager;
import dev.structure.client.gui.StructureTitleScreen;
import dev.structure.client.module.ModuleManager;
import dev.structure.client.module.utility.ClientSettings;
import dev.structure.client.module.utility.MainMenu;
import dev.structure.client.notify.NotificationManager;
import java.util.Random;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StructureClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("structure-client");
	public static final String VERSION = "v0.2.0-Beta";

	private static int ticks;
	private static boolean greeted;

	@Override
	public void onInitializeClient() {
		ModuleManager.init();
		ConfigManager.init();

		ClientTickEvents.START_CLIENT_TICK.register(mc -> ModuleManager.preTick());
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			swapTitleScreen(mc);
			ModuleManager.pollKeys(mc);
			ModuleManager.tick();
			ConfigManager.tickAutosave();
			if (++ticks % 1200 == 0 && ClientSettings.autoSaveOn()) ConfigManager.saveCurrent();
			if (!greeted && mc.player != null && ClientSettings.welcomeOn()) {
				greeted = true;
				NotificationManager.push("Structure", "Welcome to Structure Client!", 0xFF9D4DFF);
			}
		});
		HudRenderCallback.EVENT.register((ctx, delta) -> {
			ModuleManager.renderOverlays(ctx, delta.getTickProgress(true));
			ModuleManager.renderHud(ctx, delta.getTickProgress(true));
			NotificationManager.render(ctx, delta.getTickProgress(true));
		});
		ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> ConfigManager.saveCurrent());
	}

	private static void swapTitleScreen(MinecraftClient mc) {
		MainMenu menu = ModuleManager.get(MainMenu.class);
		if (menu == null || !menu.isEnabled() || mc.world != null || mc.player != null) return;
		if (mc.currentScreen != null && mc.currentScreen.getClass() == TitleScreen.class) {
			mc.setScreen(new StructureTitleScreen());
		}
	}
}
