package dev.structure.client.module.combat;

import dev.structure.client.module.AttackListener;
import dev.structure.client.module.Category;
import dev.structure.client.module.Module;
import dev.structure.client.setting.ModeSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

public class AutoWeapon extends Module implements AttackListener {
	private final ModeSetting prefer = add(new ModeSetting("Prefer", "Sword", "Sword", "Axe"));

	public AutoWeapon() {
		super("Auto Weapon", "Switches to the best weapon before attacking", Category.COMBAT);
	}

	@Override
	public String info() {
		return prefer.get();
	}

	private double score(ItemStack s) {
		if (s.isEmpty()) return 0;
		String id = Registries.ITEM.getId(s.getItem()).getPath();
		double base;
		if (id.endsWith("sword")) base = prefer.is("Sword") ? 8 : 6;
		else if (id.endsWith("_axe")) base = prefer.is("Axe") ? 8 : 6;
		else if (id.equals("mace")) base = 5;
		else if (id.equals("trident")) base = 4;
		else return 0;
		double tier = 1;
		if (id.contains("netherite")) tier = 6;
		else if (id.contains("diamond")) tier = 5;
		else if (id.contains("copper")) tier = 3.5;
		else if (id.contains("iron")) tier = 3;
		else if (id.contains("stone")) tier = 2;
		else if (id.contains("golden") || id.contains("gold")) tier = 1.5;
		return base * 10 + tier;
	}

	@Override
	public void onAttack(Entity target) {
		if (mc.player == null || !(target instanceof LivingEntity)) return;
		int best = -1;
		double bestScore = 0;
		for (int i = 0; i < 9; i++) {
			double sc = score(mc.player.getInventory().getStack(i));
			if (sc > bestScore) {
				bestScore = sc;
				best = i;
			}
		}
		if (best >= 0 && best != mc.player.getInventory().getSelectedSlot()) {
			mc.player.getInventory().setSelectedSlot(best);
		}
	}
}
