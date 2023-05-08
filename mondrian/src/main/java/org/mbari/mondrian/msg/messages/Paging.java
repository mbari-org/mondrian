package org.mbari.mondrian.msg.messages;

public interface Paging<T> {

    T nextPage();

    T previousPage();
}
