package dev.structure.client.mixin;

import dev.structure.client.module.player.TimerModule;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Timer: scales the wall-clock delta that drives the render tick counter.
 * require = 0 keeps the game running if the target method is missing in a future build;
 * only the Timer module stops working in that case.
 */
@Mixin(RenderTickCounter.Dynamic.class)
public abstract class RenderTickCounterMixin {
	private long structure$last = -1L;
	private double structure$virtual;

	@ModifyVariable(method = "beginRenderTick(J)V", at = @At("HEAD"), argsOnly = true, require = 0)
	private long structure$scale(long timeMillis) {
		float mult = TimerModule.multiplier();
		if (structure$last < 0L) {
			structure$last = timeMillis;
			structure$virtual = timeMillis;
		}
		long delta = Math.max(0L, timeMillis - structure$last);
		structure$last = timeMillis;
		if (mult == 1.0f) {
			structure$virtual = timeMillis;
			return timeMillis;
		}
		structure$virtual += delta * (double) mult;
		return (long) structure$virtual;
	}
}
