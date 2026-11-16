package dev.structure.client.module.world;

import dev.structure.client.module.Category;
import dev.structure.client.module.TextHud;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class BlockInfo extends TextHud {
	public BlockInfo() {
		super("Block Information", "Info about the block you look at.", Category.WORLD, 0.5, 0.3);
	}

	@Override
	protected List<String> lines(boolean editing) {
		List<String> l = new ArrayList<>();
		if (mc.world == null || !(mc.crosshairTarget instanceof BlockHitResult b) || b.getType() != HitResult.Type.BLOCK) {
			if (editing) l.add("Block Information");
			return l;
		}
		BlockPos pos = b.getBlockPos();
		BlockState st = mc.world.getBlockState(pos);
		l.add(st.getBlock().getName().getString());
		l.add(pos.getX() + " " + pos.getY() + " " + pos.getZ());
		l.add("Light: " + mc.world.getLightLevel(pos.offset(b.getSide())));
		l.add(String.format(Locale.ROOT, "Hardness: %.1f", st.getHardness(mc.world, pos)));
		return l;
	}
}
