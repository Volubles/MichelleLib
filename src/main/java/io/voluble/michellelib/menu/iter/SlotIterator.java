package io.voluble.michellelib.menu.iter;

import java.util.Iterator;

public interface SlotIterator extends Iterator<Integer> {
	static SlotIterator range(final int startInclusive, final int endInclusive) {
		return new SlotIterator() {
			int cur = startInclusive;

			@Override
			public boolean hasNext() {
				return cur <= endInclusive;
			}

			@Override
			public Integer next() {
				return cur++;
			}
		};
	}

	static SlotIterator grid(final int rows, final int cols, final int[] skip) {
		return new SlotIterator() {
			int slot = 0;

			private boolean isSkip(final int s) {
				for (int k : skip) if (k == s) return true;
				return false;
			}

			@Override
			public boolean hasNext() {
				return slot < rows * cols;
			}

			@Override
			public Integer next() {
				while (isSkip(slot)) slot++;
				return slot++;
			}
		};
	}
}