package dev.structure.client.notify;

import dev.structure.client.module.Module;
import dev.structure.client.module.ModuleManager;
import dev.structure.client.module.utility.ClientSettings;
import dev.structure.client.module.utility.Notifications;
import dev.structure.client.util.Draw;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Toast notifications, drawn at the top centre of the screen. */
public final class NotificationManager {
	private static final class Toast {
		final String title;
		final String msg;
		final int color;
		final long born;
		final long life;

		Toast(String title, String msg, int color, long life) {
			this.title = title;
			this.msg = msg;
			this.color = color;
			this.born = System.currentTimeMillis();
			this.life = life;
		}
	}

	private static final List<Toast> TOASTS = new ArrayList<>();

	private NotificationManager() {
	}

	public static void push(String title, String msg, int color) {
		Notifications n = ModuleManager.get(Notifications.class);
		if (n != null && !n.isEnabled()) return;
		long life = n == null ? 2500L : (long) (n.duration.d() * 1000.0);
		push(title, msg, color, life);
	}

	private static void push(String title, String msg, int color, long life) {
		if (ModuleManager.panicked()) return;
		Notifications n = ModuleManager.get(Notifications.class);
		TOASTS.add(new Toast(title, msg, color, life));
		if (n != null && n.chat.on()) dev.structure.client.util.Chat.say(title + ": " + msg);
		while (TOASTS.size() > 5) TOASTS.remove(0);
	}

	public static void toggle(Module m) {
		Notifications n = ModuleManager.get(Notifications.class);
		if (n != null && !n.toggles.on()) return;
		if (m.category == dev.structure.client.module.Category.CLIENT) return;
		push(m.name, m.isEnabled() ? "Enabled" : "Disabled", m.isEnabled() ? 0xFF7CFF8A : 0xFFFF6B8A);
	}

	/** For combat events; respects the Combat toggle in the Notifications module. */
	public static void combat(String title, String msg, int color) {
		Notifications n = ModuleManager.get(Notifications.class);
		if (n != null && !n.combat.on()) return;
		push(title, msg, color);
	}

	public static void render(DrawContext ctx, float delta) {
		if (TOASTS.isEmpty()) return;
		MinecraftClient mc = MinecraftClient.getInstance();
		int sw = mc.getWindow().getScaledWidth();
		long now = System.currentTimeMillis();
		int y = 6;
		Iterator<Toast> it = TOASTS.iterator();
		while (it.hasNext()) {
			Toast t = it.next();
			long age = now - t.born;
			if (age > t.life) {
				it.remove();
				continue;
			}
			float in = Math.min(1f, age / 220f);
			float out = Math.min(1f, (t.life - age) / 260f);
			float a = Math.min(in, out);
			float ease = 1f - (1f - in) * (1f - in);
			int w = Math.max(120, Math.max(Draw.widthBold(t.title), Draw.width(t.msg)) + 22);
			int h = 26;
			int x = sw / 2 - w / 2;
			int yy = y - (int) ((1f - ease) * 30f);
			int accent = ClientSettings.accentColor();
			Draw.round(ctx, x, yy, w, h, Draw.alpha(0xF0100818, a));
			Draw.rect(ctx, x, yy + 2, 3, h - 4, Draw.alpha(t.color, a));
			Draw.outline(ctx, x, yy, w, h, Draw.alpha(accent, a * 0.6f));
			Draw.textBold(ctx, t.title, x + 9, yy + 4, Draw.alpha(0xFFE6E6F0, Math.max(a, 0.05f)));
			Draw.text(ctx, t.msg, x + 9, yy + 14, Draw.alpha(0xFFA9A3C0, Math.max(a, 0.05f)));
			y += h + 4;
		}
	}
}
