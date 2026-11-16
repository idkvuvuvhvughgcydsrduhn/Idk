package dev.structure.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public final class Chat {
	private Chat() {
	}

	/** Client-side chat line: [Structure] message (never sent to the server). */
	public static void say(String msg) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null) return;
		mc.player.sendMessage(Text.literal("\u00a75[Structure] \u00a7f" + msg), false);
	}
}
