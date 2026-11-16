package dev.structure.client.module.visual;

import dev.structure.client.module.Category;
import dev.structure.client.module.Module;
import dev.structure.client.setting.BoolSetting;
import dev.structure.client.setting.ColorSetting;
import dev.structure.client.setting.ModeSetting;
import dev.structure.client.setting.NumberSetting;
import dev.structure.client.util.Draw;
import dev.structure.client.util.Projector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ItemEsp extends Module {
	private final NumberSetting range = add(new NumberSetting("Range", 64, 8, 128, 4));
	private final ModeSetting mode = add(new ModeSetting("Mode", "Both", "Box", "Glow", "Both"));
	private final BoolSetting labels = add(new BoolSetting("Labels", true));
	private final ColorSetting color = add(new ColorSetting("Color", 0xFFFFC857));
	private final Set<Entity> glowing = Collections.newSetFromMap(new WeakHashMap<>());

	public ItemEsp() {
		super("Item ESP", "Highlights dropped items through walls.", Category.VISUAL);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null) return;
		boolean glow = !mode.is("Box");
		for (Entity e : mc.world.getEntities()) {
			if (!(e instanceof ItemEntity)) continue;
			if (glow && mc.player.distanceTo(e) <= range.d()) {
				e.setGlowing(true);
				glowing.add(e);
			} else if (glowing.remove(e)) {
				e.setGlowing(false);
			}
		}
	}

	@Override
	public void onDisable() {
		for (Entity e : new ArrayList<>(glowing)) e.setGlowing(false);
		glowing.clear();
	}

	@Override
	public void onRender(DrawContext ctx, float delta) {
		if (!Projector.ready || mc.world == null || mc.player == null) return;
		boolean box = !mode.is("Glow");
		double[] o = new double[2];
		int n = 0;
		List<ItemEntity> items = new ArrayList<>();
		for (Entity e : mc.world.getEntities()) {
			if (e instanceof ItemEntity ie && mc.player.distanceTo(e) <= range.d()) items.add(ie);
		}
		for (ItemEntity ie : items) {
			if (n++ > 48) break;
			Vec3d lp = ie.getLerpedPos(delta);
			Box b = ie.getBoundingBox().offset(lp.x - ie.getX(), lp.y - ie.getY(), lp.z - ie.getZ()).expand(0.08);
			double[] bd = Projector.bounds(b);
			if (bd == null) continue;
			int x1 = (int) bd[0], y1 = (int) bd[1], x2 = (int) bd[2], y2 = (int) bd[3];
			if (x2 < 0 || y2 < 0 || x1 > Projector.sw || y1 > Projector.sh) continue;
			if (box) Draw.outlineBox(ctx, x1, y1, x2, y2, color.argb(), 1);
			if (labels.on() && Projector.project((b.minX + b.maxX) / 2.0, b.maxY + 0.2, (b.minZ + b.maxZ) / 2.0, o)) {
				String s = ie.getStack().getName().getString() + (ie.getStack().getCount() > 1 ? " x" + ie.getStack().getCount() : "");
				int w = Draw.width(s) + 6;
				ctx.fill((int) o[0] - w / 2, (int) o[1] - 10, (int) o[0] + w / 2, (int) o[1], 0xA0080410);
				Draw.textFlat(ctx, s, (int) o[0] - w / 2 + 3, (int) o[1] - 9, color.argb());
			}
		}
	}
}
