package dev.structure.client.module.utility;

import dev.structure.client.module.Category;
import dev.structure.client.module.TextHud;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.network.PlayerListEntry;

public class ClientInfo extends TextHud {
	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

	public ClientInfo() {
		super("Client Info", "FPS, ping and clock.", Category.UTILITY, 0.01, 0.01);
	}

	@Override
	protected List<String> lines(boolean editing) {
		List<String> l = new ArrayList<>();
		if (mc.player == null) return l;
		PlayerListEntry e = mc.getNetworkHandler() == null ? null : mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
		l.add("FPS: " + mc.getCurrentFps());
		l.add("Ping: " + (e == null ? 0 : e.getLatency()) + "ms");
		l.add("Time: " + LocalTime.now().format(FMT));
		return l;
	}
}
