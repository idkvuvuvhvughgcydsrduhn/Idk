package dev.structure.client.module.hud;

import dev.structure.client.module.Category;
import dev.structure.client.module.HudModule;
import dev.structure.client.module.utility.ClientSettings;
import dev.structure.client.setting.BoolSetting;
import dev.structure.client.util.Draw;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;

public class Keystrokes extends HudModule {
	private static final int K = 18;
	private static final int G = 2;
	private final BoolSetting space = add(new BoolSetting("Space Bar", true));

	public Keystrokes() {
		super("Keystrokes", "Shows W A S D and Space.", Category.HUD, 1.0, 0.0);
	}

	@Override
	public int contentWidth() {
		return 3 * K + 2 * G;
	}

	@Override
	public int contentHeight() {
		return 2 * K + G + (space.on() ? K / 2 + G : 0);
	}

	private void key(DrawContext ctx, int x, int y, int w, int h, String label, KeyBinding kb) {
		boolean on = kb != null && kb.isPressed();
		int acc = ClientSettings.accentColor();
		ctx.fill(x, y, x + w, y + h, on ? Draw.alpha(acc, 0.85f) : 0x99080410);
		Draw.outline(ctx, x, y, w, h, Draw.alpha(acc, on ? 1f : 0.5f));
		Draw.textCenter(ctx, label, x + w / 2, y + (h - 8) / 2, on ? 0xFFFFFFFF : color.argb());
	}

	@Override
	public void drawContent(DrawContext ctx, float delta, boolean editing) {
		var o = mc.options;
		key(ctx, K + G, 0, K, K, "W", o.forwardKey);
		key(ctx, 0, K + G, K, K, "A", o.leftKey);
		key(ctx, K + G, K + G, K, K, "S", o.backKey);
		key(ctx, 2 * (K + G), K + G, K, K, "D", o.rightKey);
		if (space.on()) key(ctx, 0, 2 * K + 2 * G, contentWidth(), K / 2, "-", o.jumpKey);
	}
}
