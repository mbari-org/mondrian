package org.mbari.mondrian.services;

import org.mbari.mondrian.domain.Concept;
import org.mbari.mondrian.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface NamesService {

    CompletableFuture<Page<String>> listNames(long size, long page);

    CompletableFuture<List<String>> findNamesContaining(String substring);

    CompletableFuture<List<String>> findNamesStartingWith(String prefix);

    CompletableFuture<Optional<Concept>> findConcept(String name);
}
