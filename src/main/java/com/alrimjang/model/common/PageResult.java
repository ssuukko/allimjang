package com.alrimjang.model.common;

import lombok.Getter;

import java.util.List;

@Getter
public class PageResult<T> {
    private final List<T> items;
    private final int totalCount;
    private final int currentPage;
    private final int size;
    private final int totalPages;
    private final boolean hasPrev;
    private final boolean hasNext;

    private PageResult(List<T> items, int totalCount, int currentPage, int size) {
        this.items = items;
        this.totalCount = totalCount;
        this.currentPage = currentPage;
        this.size = size;
        this.totalPages = Math.max((int) Math.ceil((double) totalCount / size), 1);
        this.hasPrev = currentPage > 1;
        this.hasNext = currentPage < totalPages;
    }

    public static <T> PageResult<T> of(List<T> items, int totalCount, int currentPage, int size) {
        return new PageResult<>(items, totalCount, currentPage, size);
    }
}
