package com.xixin.health.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private List<T> list;
    private long total;
    private int pageNum;
    private int pageSize;
    private int pages;

    public static <T> PageResult<T> of(List<T> list, long total, int pageNum, int pageSize) {
        int pages = (int) Math.ceil((double) total / pageSize);
        return new PageResult<>(list, total, pageNum, pageSize, pages);
    }
}
