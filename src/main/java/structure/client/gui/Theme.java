package dev.structure.client.gui;

import dev.structure.client.module.utility.ClientSettings;
import dev.structure.client.util.Draw;

/** Purple / Black / Silver palette. The accent colour is user-configurable (Client tab). */
public final class Theme {
	public static final int BG = 0xF00B0714;
	public static final int PANEL = 0xFF120C1E;
	public static final int ROW = 0xFF150E22;
	public static final int ROW_H = 0xFF1E1233;
	public static final int SEL = 0xFF2A1646;
	public static final int TEXT = 0xFFF0F0F8;
	public static final int SILVER = 0xFFC8C8D2;
	public static final int DIM = 0xFF8A8AA0;
	public static final int OFF = 0xFF3A3050;
	public static final int LINE = 0xFF2E2244;

	private Theme() {
	}

	public static int accent() {
		return ClientSettings.accentColor();
	}

	public static int accentDark() {
		return Draw.darken(accent(), 0.5f);
	}
}
