package dev.structure.client.gui;

import java.util.ArrayList;
import java.util.List;

/**
 * Immediate-mode hit regions: widgets register a rectangle while they are drawn, input handlers look the
 * regions up afterwards. Drawing and hit-testing therefore can never disagree (important for scrolled lists).
 */
public final class Hits {
	public interface Drag {
		void apply(double mx, double my);
	}

	public static final class Hit {
		final int x, y, w, h;
		final int[] clip;
		public Runnable tap;
		public Drag drag;

		Hit(int x, int y, int w, int h, int[] clip) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.clip = clip;
		}

		boolean contains(double mx, double my) {
			if (mx < x || mx >= x + w || my < y || my >= y + h) return false;
			return clip == null || (mx >= clip[0] && mx < clip[2] && my >= clip[1] && my < clip[3]);
		}
	}

	private final List<Hit> list = new ArrayList<>();
	private int[] clip;

	public void clear() {
		list.clear();
		clip = null;
	}

	public void setClip(int x1, int y1, int x2, int y2) {
		clip = new int[] {x1, y1, x2, y2};
	}

	public void clearClip() {
		clip = null;
	}

	public Hit add(int x, int y, int w, int h, Runnable tap) {
		Hit hit = new Hit(x, y, w, h, clip);
		hit.tap = tap;
		list.add(hit);
		return hit;
	}

	public Hit addDrag(int x, int y, int w, int h, Drag drag) {
		Hit hit = new Hit(x, y, w, h, clip);
		hit.drag = drag;
		list.add(hit);
		return hit;
	}

	/** Topmost region under the pointer (regions added later are drawn later, so they win). */
	public Hit at(double mx, double my) {
		for (int i = list.size() - 1; i >= 0; i--) {
			if (list.get(i).contains(mx, my)) return list.get(i);
		}
		return null;
	}
}
