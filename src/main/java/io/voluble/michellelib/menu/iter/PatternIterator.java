package io.voluble.michellelib.menu.iter;

import java.util.Iterator;

public final class PatternIterator implements Iterator<Integer> {
    private final String[] rows;
    private final char target;
    private int index = 0;

    public PatternIterator(final String[] rows, final char target) {
        this.rows = rows;
        this.target = target;
    }

    @Override
    public boolean hasNext() {
        return nextIndex(false) >= 0;
    }

    @Override
    public Integer next() {
        return nextIndex(true);
    }

    private int nextIndex(final boolean advance) {
        for (int r = index / 9; r < rows.length; r++) {
            String row = rows[r];
            for (int c = (r == index / 9 ? index % 9 : 0); c < Math.min(9, row.length()); c++) {
                if (row.charAt(c) == target) {
                    int s = r * 9 + c;
                    if (advance) index = s + 1; // move past
                    return s;
                }
            }
        }
        return -1;
    }
}


