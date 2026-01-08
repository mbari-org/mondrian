package org.mbari.mondrian.services.noop;

import org.mbari.mondrian.domain.Concept;
import org.mbari.mondrian.domain.Page;
import org.mbari.mondrian.services.NamesService;
import org.mbari.vars.oni.sdk.r1.models.ConceptAssociationTemplate;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class NoopNamesService implements NamesService {
    @Override
    public CompletableFuture<Page<String>> listNames(long size, long page) {
        return CompletableFuture.completedFuture(new Page<>(List.of(), size, page, 0L));
    }

    @Override
    public CompletableFuture<List<String>> findNamesContaining(String substring) {
        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public CompletableFuture<List<String>> findNamesStartingWith(String prefix) {
        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public CompletableFuture<Optional<Concept>> findConcept(String name) {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletableFuture<String> findDefaultName() {
        return CompletableFuture.completedFuture("object");
    }

    @Override
    public CompletableFuture<List<String>> findDescendants(String name) {
        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public CompletableFuture<List<ConceptAssociationTemplate>> findTemplates(String name) {
        return CompletableFuture.completedFuture(List.of());
    }
}
