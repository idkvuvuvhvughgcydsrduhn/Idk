package dev.structure.client.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Read/write access to MinecraftClient.itemUseCooldown (private in the game code).
 * Used by FastPlace to shorten the delay between block placements.
 */
@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
	@Accessor("itemUseCooldown")
	int structure$getItemUseCooldown();

	@Accessor("itemUseCooldown")
	void structure$setItemUseCooldown(int value);
}
