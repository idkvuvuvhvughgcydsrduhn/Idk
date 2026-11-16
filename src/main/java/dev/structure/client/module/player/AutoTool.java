package dev.structure.client.module.player;

import dev.structure.client.module.Category;
import dev.structure.client.module.Module;
import dev.structure.client.setting.BoolSetting;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoTool extends Module {
	private final BoolSetting switchBack = add(new BoolSetting("Switch Back", true));
	private int previous = -1;

	public AutoTool() {
		super("Auto Tool", "Picks the fastest tool for the block you mine.", Category.PLAYER);
	}

	@Override
	public void onDisable() {
		previous = -1;
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null) return;
		PlayerInventory inv = mc.player.getInventory();
		boolean mining = mc.currentScreen == null && mc.options.attackKey.isPressed();
		if (!mining) {
			if (switchBack.on() && previous >= 0) {
				inv.setSelectedSlot(previous);
				previous = -1;
			}
			return;
		}
		if (!(mc.crosshairTarget instanceof BlockHitResult b) || b.getType() != HitResult.Type.BLOCK) return;
		BlockState st = mc.world.getBlockState(b.getBlockPos());
		if (st.isAir()) return;
		int cur = inv.getSelectedSlot();
		int best = cur;
		float bs = inv.getStack(cur).getMiningSpeedMultiplier(st);
		for (int i = 0; i < 9; i++) {
			float s = inv.getStack(i).getMiningSpeedMultiplier(st);
			if (s > bs + 0.01f) {
				bs = s;
				best = i;
			}
		}
		if (best != cur) {
			if (previous < 0) previous = cur;
			inv.setSelectedSlot(best);
		}
	}
}
