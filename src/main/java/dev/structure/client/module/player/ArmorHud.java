package dev.structure.client.module.player;

import dev.structure.client.module.Category;
import dev.structure.client.module.HudModule;
import dev.structure.client.setting.BoolSetting;
import dev.structure.client.util.Draw;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class ArmorHud extends HudModule {
	private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
	private final BoolSetting percent = add(new BoolSetting("Durability %", true));

	public ArmorHud() {
		super("Armor HUD", "Shows your armor and durability.", Category.PLAYER, 0.5, 0.8);
	}

	@Override
	public int contentWidth() {
		return 4 * 20 + 2;
	}

	@Override
	public int contentHeight() {
		return percent.on() ? 32 : 22;
	}

	@Override
	public void drawContent(DrawContext ctx, float delta, boolean editing) {
		if (mc.player == null) return;
		ctx.fill(0, 0, contentWidth(), contentHeight(), 0x77080410);
		ctx.fill(0, 0, contentWidth(), 1, 0xFF000000 | (color.argb() & 0xFFFFFF));
		int x = 2;
		for (EquipmentSlot s : SLOTS) {
			ItemStack st = mc.player.getEquippedStack(s);
			if (!st.isEmpty()) {
				ctx.drawItem(st, x, 3);
				ctx.drawStackOverlay(mc.textRenderer, st, x, 3);
				if (percent.on() && st.getMaxDamage() > 0) {
					int pct = Math.round(100f * (st.getMaxDamage() - st.getDamage()) / st.getMaxDamage());
					Draw.textCenter(ctx, pct + "%", x + 8, 21, color.argb());
				}
			}
			x += 20;
		}
	}
}
