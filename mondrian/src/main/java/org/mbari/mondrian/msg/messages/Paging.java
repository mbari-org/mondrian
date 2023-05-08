package org.mbari.mondrian.msg.messages;

public interface Paging<T> {

    T nextPage();

    T previousPage();

    /**
     * Resets paging to first page using the given page size
     * @param pageSize The new page size to use
     * @return
     */
    T withPageSize(int pageSize);
}
