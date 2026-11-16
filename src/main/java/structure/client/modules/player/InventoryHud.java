package dev.structure.client.module.player;

import dev.structure.client.module.Category;
import dev.structure.client.module.HudModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class InventoryHud extends HudModule {
	public InventoryHud() {
		super("Inventory HUD", "Shows your inventory on screen.", Category.PLAYER, 0.99, 0.5);
	}

	@Override
	public int contentWidth() {
		return 9 * 18 + 4;
	}

	@Override
	public int contentHeight() {
		return 3 * 18 + 4;
	}

	@Override
	public void drawContent(DrawContext ctx, float delta, boolean editing) {
		if (mc.player == null) return;
		ctx.fill(0, 0, contentWidth(), contentHeight(), 0x88080410);
		ctx.fill(0, 0, contentWidth(), 1, 0xFF000000 | (color.argb() & 0xFFFFFF));
		PlayerInventory inv = mc.player.getInventory();
		for (int i = 0; i < 27; i++) {
			ItemStack st = inv.getStack(9 + i);
			if (st.isEmpty()) continue;
			int x = 2 + (i % 9) * 18;
			int y = 2 + (i / 9) * 18;
			ctx.drawItem(st, x, y);
			ctx.drawStackOverlay(mc.textRenderer, st, x, y);
		}
	}
}
