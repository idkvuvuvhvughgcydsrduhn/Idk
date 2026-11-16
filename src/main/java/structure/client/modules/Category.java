package dev.structure.client.module;

public enum Category {
	COMBAT("Combat"),
	MOVEMENT("Movement"),
	VISUAL("Visual"),
	PLAYER("Player"),
	WORLD("World"),
	UTILITY("Utility"),
	HUD("HUD"),
	CLIENT("Client");

	public final String label;

	Category(String label) {
		this.label = label;
	}
}
