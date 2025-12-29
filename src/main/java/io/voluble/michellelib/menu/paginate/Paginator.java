package io.voluble.michellelib.menu.paginate;

import java.util.List;

public final class Paginator<T> {
    private final List<T> all;
    private final int pageSize;
    private int page = 0;

    public Paginator(final List<T> all, final int pageSize) {
        this.all = all;
        this.pageSize = pageSize;
    }

    public int page() {
        return page;
    }

    public void next() {
        if ((page + 1) * pageSize < all.size()) page++;
    }

    public void prev() {
        if (page > 0) page--;
    }

    public List<T> pageItems() {
        int from = page * pageSize;
        int to = Math.min(from + pageSize, all.size());
        return all.subList(from, to);
    }

    public int totalPages() {
        if (pageSize <= 0) return 0;
        return (int) Math.ceil(all.size() / (double) pageSize);
    }

    public void setPage(final int pageIndex) {
        if (pageIndex < 0) page = 0;
        else if (pageIndex >= totalPages()) page = Math.max(0, totalPages() - 1);
        else page = pageIndex;
    }
}


