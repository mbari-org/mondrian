package org.mbari.mondrian.domain;

public record Selection<T>(Object source, T selected) {
}
