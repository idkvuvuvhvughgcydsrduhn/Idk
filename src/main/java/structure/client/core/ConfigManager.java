package structure.client.core;

import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.loader.api.FabricLoader;
import structure.client.StructureClient;

public class ConfigManager {
    private final Path dir = FabricLoader.getInstance().getConfigDir().resolve("structure-client");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public String current = "default";

    public ConfigManager() {
        try {
            Files.createDirectories(dir);
            Path c = dir.resolve("current.txt");
            if (Files.exists(c)) current = Files.readString(c).trim();
        } catch (Exception ignored) {}
        if (current.isEmpty()) current = "default";
    }

    public void init() {
        if (!load(current)) { current = "default"; save(current); }
    }

    private Path file(String n) { return dir.resolve(n.replaceAll("[^A-Za-z0-9_-]", "_") + ".json"); }

    public List<String> list() {
        List<String> out = new ArrayList<>();
        try (var s = Files.list(dir)) {
            s.forEach(p -> { String f = p.getFileName().toString(); if (f.endsWith(".json")) out.add(f.substring(0, f.length() - 5)); });
        } catch (Exception ignored) {}
        if (!out.contains("default")) out.add("default");
        Collections.sort(out);
        return out;
    }

    public boolean save(String name) {
        try {
            JsonObject mods = new JsonObject();
            for (Mod m : StructureClient.modules.all) {
                JsonObject o = new JsonObject();
                o.addProperty("enabled", m.isEnabled());
                o.addProperty("key", m.key);
                JsonObject s = new JsonObject();
                for (Setting st : m.settings) s.add(st.name, st.save());
                o.add("settings", s);
                mods.add(m.name, o);
            }
            JsonObject root = new JsonObject();
            root.add("modules", mods);
            Files.writeString(file(name), gson.toJson(root));
            current = name;
            Files.writeString(dir.resolve("current.txt"), name);
            return true;
        } catch (Exception e) { return false; }
    }

    public boolean load(String name) {
        try {
            Path f = file(name);
            if (!Files.exists(f)) return false;
            JsonObject mods = JsonParser.parseString(Files.readString(f)).getAsJsonObject().getAsJsonObject("modules");
            for (Mod m : StructureClient.modules.all) {
                if (!mods.has(m.name)) continue;
                JsonObject o = mods.getAsJsonObject(m.name);
                if (o.has("key")) m.key = o.get("key").getAsInt();
                if (o.has("settings")) {
                    JsonObject s = o.getAsJsonObject("settings");
                    for (Setting st : m.settings)
                        if (s.has(st.name)) { try { st.load(s.get(st.name)); } catch (Exception ignored) {} }
                }
                if (o.has("enabled")) m.loadEnabled(o.get("enabled").getAsBoolean());
            }
            current = name;
            Files.writeString(dir.resolve("current.txt"), name);
            return true;
        } catch (Exception e) { return false; }
    }

    public String create() {
        for (int i = 1; ; i++) {
            String n = "config-" + i;
            if (!Files.exists(file(n))) { save(n); return n; }
        }
    }

    public boolean delete(String name) {
        if (name.equals("default")) return false;
        try {
            Files.deleteIfExists(file(name));
            if (current.equals(name)) load("default");
            return true;
        } catch (Exception e) { return false; }
    }
}
