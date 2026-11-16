package dev.structure.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.structure.client.StructureClient;
import dev.structure.client.module.Module;
import dev.structure.client.module.ModuleManager;
import dev.structure.client.module.utility.ClientSettings;
import dev.structure.client.setting.Setting;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Config system. Files live in <game dir>/structure/configs/NAME.json.
 * Everything (module state, settings, keybinds, HUD positions/sizes/colors) is in one JSON.
 */
public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static Path root;
	private static Path configDir;
	private static String current = "default";
	private static boolean dirty;
	private static boolean loading;
	private static int sinceSave;

	private ConfigManager() {
	}

	public static void init() {
		// same folder as the 0.1.0 alpha, so existing configs keep working
		root = FabricLoader.getInstance().getConfigDir().resolve("structure-client");
		configDir = root.resolve("configs");
		try {
			Files.createDirectories(configDir);
			Path cur = root.resolve("current.txt");
			if (Files.exists(cur)) {
				String n = clean(Files.readString(cur, StandardCharsets.UTF_8).trim());
				if (!n.isEmpty()) current = n;
			}
		} catch (IOException e) {
			StructureClient.LOGGER.warn("Config dir init failed", e);
		}
		if (!load(current)) {
			current = "default";
			save(current);
		}
	}

	public static String current() {
		return current;
	}

	public static String clean(String name) {
		String n = name == null ? "" : name.replaceAll("[^A-Za-z0-9_\\- ]", "").trim();
		return n.length() > 24 ? n.substring(0, 24) : n;
	}

	private static Path file(String name) {
		return configDir.resolve(name + ".json");
	}

	public static Path directory() {
		return root;
	}

	public static List<String> list() {
		List<String> out = new ArrayList<>();
		if (configDir == null) return out;
		try (Stream<Path> s = Files.list(configDir)) {
			s.forEach(p -> {
				String f = p.getFileName().toString();
				if (f.endsWith(".json")) out.add(f.substring(0, f.length() - 5));
			});
		} catch (IOException ignored) {
		}
		Collections.sort(out, String.CASE_INSENSITIVE_ORDER);
		return out;
	}

	public static boolean exists(String name) {
		return configDir != null && Files.exists(file(clean(name)));
	}

	private static JsonObject serialize() {
		JsonObject rootObj = new JsonObject();
		rootObj.addProperty("version", 1);
		JsonObject mods = new JsonObject();
		for (Module m : ModuleManager.all()) {
			JsonObject mo = new JsonObject();
			mo.addProperty("enabled", m.isEnabled());
			JsonObject so = new JsonObject();
			for (Setting<?> s : m.settings()) so.add(s.name, s.save());
			mo.add("settings", so);
			mods.add(m.name, mo);
		}
		rootObj.add("modules", mods);
		return rootObj;
	}

	private static void deserialize(JsonObject obj) {
		loading = true;
		try {
			JsonObject mods = obj.has("modules") ? obj.getAsJsonObject("modules") : new JsonObject();
			for (Module m : ModuleManager.all()) {
				if (!mods.has(m.name)) continue;
				JsonObject mo = mods.getAsJsonObject(m.name);
				if (mo.has("key") && mo.get("key").isJsonPrimitive()) m.bind.set(mo.get("key").getAsInt()); // 0.1.0 format
				if (mo.has("settings")) {
					JsonObject so = mo.getAsJsonObject("settings");
					for (Setting<?> s : m.settings()) {
						JsonElement e = so.get(s.name);
						if (e != null) s.load(e);
					}
				}
				if (mo.has("enabled") && m.category != dev.structure.client.module.Category.CLIENT) {
					m.loadEnabled(mo.get("enabled").getAsBoolean());
				}
			}
		} finally {
			loading = false;
		}
	}

	public static boolean save(String name) {
		name = clean(name);
		if (name.isEmpty() || configDir == null) return false;
		try {
			Files.createDirectories(configDir);
			Files.writeString(file(name), GSON.toJson(serialize()), StandardCharsets.UTF_8);
			if (name.equals(current)) dirty = false;
			return true;
		} catch (IOException e) {
			StructureClient.LOGGER.warn("Failed to save config {}", name, e);
			return false;
		}
	}

	public static boolean saveCurrent() {
		return save(current);
	}

	public static boolean load(String name) {
		name = clean(name);
		if (name.isEmpty() || configDir == null || !Files.exists(file(name))) return false;
		try {
			JsonObject obj = JsonParser.parseString(Files.readString(file(name), StandardCharsets.UTF_8)).getAsJsonObject();
			// turn everything off first so a loaded config fully replaces the old state
			for (Module m : ModuleManager.all()) {
				if (m.category != dev.structure.client.module.Category.CLIENT && m.isEnabled()) m.setEnabled(false, false);
			}
			deserialize(obj);
			current = name;
			Files.writeString(root.resolve("current.txt"), current, StandardCharsets.UTF_8);
			dirty = false;
			return true;
		} catch (Exception e) {
			StructureClient.LOGGER.warn("Failed to load config {}", name, e);
			return false;
		}
	}

	/** Saves the current state under a name and makes it the active config. */
	public static boolean saveAs(String name) {
		name = clean(name);
		if (name.isEmpty() || !save(name)) return false;
		current = name;
		try {
			Files.writeString(root.resolve("current.txt"), current, StandardCharsets.UTF_8);
		} catch (IOException ignored) {
		}
		dirty = false;
		return true;
	}

	/** Creates a new config from the current state and switches to it. */
	public static boolean create(String name) {
		name = clean(name);
		if (name.isEmpty() || exists(name)) return false;
		current = name;
		try {
			Files.writeString(root.resolve("current.txt"), current, StandardCharsets.UTF_8);
		} catch (IOException ignored) {
		}
		return save(name);
	}

	public static boolean delete(String name) {
		name = clean(name);
		if (name.isEmpty() || name.equals("default") || !exists(name)) return false;
		try {
			Files.delete(file(name));
		} catch (IOException e) {
			return false;
		}
		if (name.equals(current)) {
			current = "default";
			if (!exists("default")) save("default");
			else load("default");
		}
		return true;
	}

	public static void markDirty() {
		if (!loading) dirty = true;
	}

	/** Auto Save: writes the active config shortly after something changed. */
	public static void tickAutosave() {
		if (!dirty || !ClientSettings.autoSaveOn()) return;
		if (++sinceSave >= 100) {
			sinceSave = 0;
			saveCurrent();
		}
	}
}
