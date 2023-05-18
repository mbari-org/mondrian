package org.mbari.mondrian.services.vars;

import org.mbari.mondrian.domain.Concept;
import org.mbari.mondrian.domain.Page;
import org.mbari.mondrian.etc.jdk.Logging;
import org.mbari.mondrian.services.NamesService;
import org.mbari.vars.services.ConceptService;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class KbNamesService implements NamesService {

    private final ConceptService conceptService;

    private static final Logging log = new Logging(KbNamesService.class);

    public KbNamesService(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    @Override
    public CompletableFuture<Page<String>> listNames(long size, long page) {

        int a = (int) (size * page);
        int b = (int) (a + size);
        log.atInfo().log("Loading concept names in range " + a + "-" + b);
        return conceptService.findAllNames()
                .thenApply(names -> {
                    var c = Math.min(b, names.size());
                    log.atInfo().log("Loaded " + names.size()
                            + " concept names. Taking sublist of " + a + "-" + c);
                    var xs = names.subList(a, c);
//                    var xs = names.subList(a, b);
                    return new Page<>(xs, size, page, (long) names.size());
                });
    }

    @Override
    public CompletableFuture<List<String>> findNamesContaining(String substring) {
        var s = substring.toLowerCase();
        return conceptService.findAllNames()
                .thenApply(names -> names.stream()
                        .filter(n -> n.toLowerCase().contains(s))
                        .toList());
    }

    @Override
    public CompletableFuture<List<String>> findNamesStartingWith(String prefix) {
        var s = prefix.toLowerCase();
        return conceptService.findAllNames()
                .thenApply(names -> names.stream()
                        .filter(n -> n.toLowerCase().startsWith(s))
                        .toList());
    }

    @Override
    public CompletableFuture<Optional<Concept>> findConcept(String name) {
        return conceptService.findConcept(name)
                .thenApply(opt ->
                        opt.map(c -> new Concept(c.getName(), c.getAlternativeNames(), c.getRank())));

    }

    @Override
    public CompletableFuture<String> findDefaultName() {
        return conceptService.findRoot().thenApply(org.mbari.vars.services.model.Concept::getName);
    }
}
