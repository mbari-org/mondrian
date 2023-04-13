package org.mbari.mondrian.domain;

import java.util.List;

public record Page<T>(List<T> content, long size, long page, Long totalSize) {

    public long limit() {
        return size;
    }

    public long offset() {
        return page * size;
    }
}
