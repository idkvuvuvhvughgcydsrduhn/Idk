package structure.client.core;

public enum Category {
    COMBAT("Combat"), MOVEMENT("Movement"), VISUAL("Visual"), PLAYER("Player"), WORLD("World"), UTILITY("Utility");
    public final String label;
    Category(String l) { label = l; }
}
