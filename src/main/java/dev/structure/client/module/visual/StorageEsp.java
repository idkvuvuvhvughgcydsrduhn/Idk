package dev.structure.client.module.visual;

import dev.structure.client.module.Category;
import dev.structure.client.module.Module;
import dev.structure.client.setting.BoolSetting;
import dev.structure.client.setting.NumberSetting;
import dev.structure.client.util.Draw;
import dev.structure.client.util.Projector;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

public class StorageEsp extends Module {
	private static final class Entry {
		final BlockPos pos;
		final int color;

		Entry(BlockPos pos, int color) {
			this.pos = pos;
			this.color = color;
		}
	}

	private final NumberSetting range = add(new NumberSetting("Range", 48, 8, 96, 4));
	private final BoolSetting chests = add(new BoolSetting("Chests", true));
	private final BoolSetting ender = add(new BoolSetting("Ender Chests", true));
	private final BoolSetting barrels = add(new BoolSetting("Barrels", true));
	private final BoolSetting shulkers = add(new BoolSetting("Shulker Boxes", true));
	private final BoolSetting others = add(new BoolSetting("Hoppers / Furnaces", false));
	private List<Entry> cache = new ArrayList<>();
	private int timer;

	public StorageEsp() {
		super("Storage ESP", "Shows chests, barrels and shulker boxes.", Category.VISUAL);
	}

	@Override
	public void onDisable() {
		cache = new ArrayList<>();
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null) return;
		if (timer-- > 0) return;
		timer = 10;
		List<Entry> out = new ArrayList<>();
		ChunkPos cp = mc.player.getChunkPos();
		int r = (int) Math.ceil(range.d() / 16.0);
		double r2 = range.d() * range.d();
		for (int cx = cp.x - r; cx <= cp.x + r; cx++) {
			for (int cz = cp.z - r; cz <= cp.z + r; cz++) {
				WorldChunk ch = mc.world.getChunkManager().getWorldChunk(cx, cz);
				if (ch == null) continue;
				for (BlockEntity be : ch.getBlockEntities().values()) {
					int col = colorOf(be);
					if (col == 0) continue;
					BlockPos bp = be.getPos();
					if (mc.player.squaredDistanceTo(bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5) > r2) continue;
					out.add(new Entry(bp, col));
				}
			}
		}
		cache = out;
	}

	private int colorOf(BlockEntity be) {
		if (be instanceof EnderChestBlockEntity) return ender.on() ? 0xFFB06CFF : 0;
		if (be instanceof ChestBlockEntity) return chests.on() ? 0xFFFFA43B : 0;
		if (be instanceof BarrelBlockEntity) return barrels.on() ? 0xFFC08A4D : 0;
		if (be instanceof ShulkerBoxBlockEntity) return shulkers.on() ? 0xFFFF6BD6 : 0;
		if (be instanceof HopperBlockEntity || be instanceof DispenserBlockEntity || be instanceof AbstractFurnaceBlockEntity) {
			return others.on() ? 0xFF9AA0B8 : 0;
		}
		return 0;
	}

	@Override
	public void onRender(DrawContext ctx, float delta) {
		if (!Projector.ready) return;
		for (Entry e : cache) {
			double[] b = Projector.bounds(new Box(e.pos).contract(0.02));
			if (b == null) continue;
			int x1 = (int) b[0], y1 = (int) b[1], x2 = (int) b[2], y2 = (int) b[3];
			if (x2 < 0 || y2 < 0 || x1 > Projector.sw || y1 > Projector.sh || x2 - x1 > Projector.sw * 4) continue;
			ctx.fill(x1, y1, x2, y2, Draw.alpha(e.color, 0.16f));
			Draw.outlineBox(ctx, x1, y1, x2, y2, e.color, 1);
		}
	}
}
