package dev.structure.client.module;

import dev.structure.client.StructureClient;
import dev.structure.client.module.combat.*;
import dev.structure.client.module.hud.*;
import dev.structure.client.module.movement.*;
import dev.structure.client.module.player.*;
import dev.structure.client.module.utility.*;
import dev.structure.client.module.visual.*;
import dev.structure.client.module.world.*;
import dev.structure.client.notify.NotificationManager;
import dev.structure.client.util.Projector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import org.lwjgl.glfw.GLFW;

/** Owns every module, runs their lifecycle hooks, and polls keybinds. */
public final class ModuleManager {
	private static final List<Module> MODULES = new ArrayList<>();
	private static final Map<Class<?>, Module> BY_CLASS = new HashMap<>();
	private static final Set<Integer> HELD = new HashSet<>();
	private static boolean panicked;

	private ModuleManager() {
	}

	public static void init() {
		register(new ClientSettings(),
			// Combat
			new KillAura(), new MobAura(), new AutoClicker(), new Criticals(), new TargetHud(), new AutoWeapon(), new CombatNotifications(),
			// Movement
			new Fly(), new Speed(), new HighJump(), new Sprint(), new NoFall(), new MovementHud(),
			// Visual
			new Esp(), new Fullbright(), new Tracers(), new Nametags(), new ItemEsp(), new StorageEsp(), new EntityHighlight(),
			// Player
			new AutoTool(), new ArmorHud(), new InventoryHud(), new Coordinates(), new TimerModule(), new PotionHud(),
			// World
			new WorldInfo(), new BlockInfo(), new EntityInfo(), new EnvironmentDisplay(),
			// Utility
			new ClickGui(), new HudEditor(), new Panic(), new Notifications(), new MainMenu(), new ClientInfo(),
			// HUD extras
			new Watermark(), new Keystrokes(), new ClientStatus(), new ModuleList());

		// defaults for a fresh install (a saved config overrides these)
		List<Class<? extends Module>> onByDefault = List.of(
			Notifications.class, ClientInfo.class, MainMenu.class, Coordinates.class,
			ArmorHud.class, Watermark.class, Keystrokes.class);
		for (Class<? extends Module> c : onByDefault) {
			Module m = BY_CLASS.get(c);
			if (m != null) m.loadEnabled(true);
		}
		StructureClient.LOGGER.info("Registered {} modules", MODULES.size());
	}

	private static void register(Module... ms) {
		for (Module m : ms) {
			MODULES.add(m);
			BY_CLASS.put(m.getClass(), m);
		}
	}

	public static <T extends Module> T get(Class<T> c) {
		Module m = BY_CLASS.get(c);
		return m == null ? null : c.cast(m);
	}

	public static boolean isOn(Class<? extends Module> c) {
		Module m = BY_CLASS.get(c);
		return m != null && m.isEnabled();
	}

	public static List<Module> all() {
		return MODULES;
	}

	public static List<Module> in(Category c) {
		List<Module> l = new ArrayList<>();
		for (Module m : MODULES) if (m.category == c) l.add(m);
		return l;
	}

	public static List<HudModule> hudModules() {
		List<HudModule> l = new ArrayList<>();
		for (Module m : MODULES) if (m instanceof HudModule h) l.add(h);
		return l;
	}

	public static boolean panicked() {
		return panicked;
	}

	/** Disables every module. With lock = true the client stays hidden until unpanicked. */
	public static void panic(boolean lock) {
		for (Module m : MODULES) {
			if (m.category != Category.CLIENT && m.isEnabled()) m.setEnabled(false, false);
		}
		panicked = lock;
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.currentScreen instanceof dev.structure.client.gui.ClickGuiScreen
				|| mc.currentScreen instanceof dev.structure.client.gui.HudEditorScreen) {
			mc.setScreen(null);
		}
	}

	public static void unpanic() {
		panicked = false;
		NotificationManager.push("Structure", "Client restored", 0xFF7CFF8A);
	}

	public static void preTick() {
		run(true);
	}

	public static void tick() {
		run(false);
	}

	private static void run(boolean pre) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (panicked || mc.player == null || mc.world == null) return;
		for (int i = 0; i < MODULES.size(); i++) {
			Module m = MODULES.get(i);
			if (!m.isEnabled()) continue;
			try {
				if (pre) m.onPreTick();
				else m.onTick();
			} catch (Throwable t) {
				StructureClient.LOGGER.error("Module {} crashed; disabling it", m.name, t);
				m.setEnabled(false, false);
				NotificationManager.push(m.name, "Crashed and was disabled", 0xFFFF6B8A);
			}
		}
	}

	/** 3D-anchored overlays (ESP, tracers, nametags). Projector is refreshed once per frame here. */
	public static void renderOverlays(DrawContext ctx, float delta) {
		if (panicked) return;
		MinecraftClient mc = MinecraftClient.getInstance();
		Projector.begin(mc, delta);
		for (int i = 0; i < MODULES.size(); i++) {
			Module m = MODULES.get(i);
			if (!m.isEnabled() || m instanceof HudModule) continue;
			try {
				m.onRender(ctx, delta);
			} catch (Throwable t) {
				StructureClient.LOGGER.error("Module {} crashed in render; disabling it", m.name, t);
				m.setEnabled(false, false);
			}
		}
	}

	public static void renderHud(DrawContext ctx, float delta) {
		if (panicked) return;
		MinecraftClient mc = MinecraftClient.getInstance();
		int sw = mc.getWindow().getScaledWidth();
		int sh = mc.getWindow().getScaledHeight();
		for (int i = 0; i < MODULES.size(); i++) {
			Module m = MODULES.get(i);
			if (!m.isEnabled() || !(m instanceof HudModule h)) continue;
			try {
				h.renderHud(ctx, delta, false, sw, sh);
			} catch (Throwable t) {
				StructureClient.LOGGER.error("HUD {} crashed; disabling it", m.name, t);
				m.setEnabled(false, false);
			}
		}
	}

	/** Forwards an attack to every enabled module that wants to know (Auto Weapon, Criticals...). */
	public static void fireAttack(Entity target) {
		for (int i = 0; i < MODULES.size(); i++) {
			Module m = MODULES.get(i);
			if (m.isEnabled() && m instanceof AttackListener l) {
				try {
					l.onAttack(target);
				} catch (Throwable t) {
					StructureClient.LOGGER.error("Module {} crashed in onAttack", m.name, t);
				}
			}
		}
	}

	private static boolean pressedEdge(long handle, int key) {
		if (key <= 0) return false;
		boolean down = GLFW.glfwGetKey(handle, key) == GLFW.GLFW_PRESS;
		if (down) return HELD.add(key);
		HELD.remove(key);
		return false;
	}

	/**
	 * Keybind manager. Polls GLFW directly so it works regardless of input source.
	 * Panic has its own bind and still works while the client is locked.
	 */
	public static void pollKeys(MinecraftClient mc) {
		if (mc.getWindow() == null) return;
		long h = mc.getWindow().getHandle();
		boolean inGame = mc.currentScreen == null && mc.player != null;
		for (int i = 0; i < MODULES.size(); i++) {
			Module m = MODULES.get(i);
			boolean edge = pressedEdge(h, m.bind.key());
			if (!edge || m.category == Category.CLIENT) continue;
			if (m instanceof Panic) {
				m.toggle();
				continue;
			}
			if (!inGame || panicked) continue;
			m.toggle();
		}
	}
}
