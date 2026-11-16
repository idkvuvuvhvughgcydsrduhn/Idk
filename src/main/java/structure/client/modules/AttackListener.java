package dev.structure.client.module;

import net.minecraft.entity.Entity;

/** Implemented by modules that need to act right before the client sends an attack. */
public interface AttackListener {
	void onAttack(Entity target);
}
