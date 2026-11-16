package dev.structure.client.module.hud;

import dev.structure.client.StructureClient;
import dev.structure.client.module.Category;
import dev.structure.client.module.HudModule;
import dev.structure.client.module.utility.ClientSettings;
import dev.structure.client.util.Draw;
import net.minecraft.client.gui.DrawContext;

public class Watermark extends HudModule {
	public Watermark() {
		super("Watermark", "Structure Client logo and version.", Category.HUD, 0.0, 0.0);
	}

	@Override
	public int contentWidth() {
		return 30 + Math.max(Draw.widthBold("STRUCTURE CLIENT"), Draw.width(StructureClient.VERSION)) + 8;
	}

	@Override
	public int contentHeight() {
		return 26;
	}

	@Override
	public void drawContent(DrawContext ctx, float delta, boolean editing) {
		int acc = ClientSettings.accentColor();
		ctx.fill(0, 0, contentWidth(), contentHeight(), 0xCC0B0614);
		Draw.outline(ctx, 0, 0, contentWidth(), contentHeight(), Draw.alpha(acc, 0.8f));
		ctx.fill(4, 4, 22, 22, Draw.darken(acc, 0.6f));
		Draw.outline(ctx, 4, 4, 18, 18, acc);
		Draw.textBold(ctx, "S", 11, 9, 0xFFFFFFFF);
		Draw.textBold(ctx, "STRUCTURE CLIENT", 28, 5, 0xFFE6E6F0);
		Draw.text(ctx, StructureClient.VERSION, 28, 15, acc);
	}
}
