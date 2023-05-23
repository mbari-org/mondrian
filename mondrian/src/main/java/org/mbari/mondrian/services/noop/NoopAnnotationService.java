package org.mbari.mondrian.services.noop;

import org.mbari.mondrian.domain.Page;
import org.mbari.mondrian.services.AnnotationService;
import org.mbari.vars.services.model.Annotation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NoopAnnotationService implements AnnotationService {
    @Override
    public CompletableFuture<Collection<Annotation>> create(Collection<Annotation> annotations) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not implemented"));
    }

    @Override
    public CompletableFuture<Annotation> update(Annotation annotation) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not implemented"));
    }

    @Override
    public CompletableFuture<Collection<Annotation>> updateAll(Collection<Annotation> annotations) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not implemented"));
    }

    @Override
    public CompletableFuture<Boolean> deleteAll(Collection<UUID> observationUuids) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not implemented"));
    }

    @Override
    public CompletableFuture<Optional<Annotation>> findByUuid(UUID uuid) {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletableFuture<List<Annotation>> findByImageUuid(UUID imageUuid) {
        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public CompletableFuture<Page<Annotation>> findByMediaUuid(UUID mediaUuid, int size, int page) {
        return CompletableFuture.completedFuture(new Page<>(List.of(), size, page, 0L));
    }

    @Override
    public CompletableFuture<Page<Annotation>> findByVideoSequenceName(String videoSequenceName, int size, int page) {
        return CompletableFuture.completedFuture(new Page<>(List.of(), size, page, 0L));
    }

    @Override
    public CompletableFuture<Page<Annotation>> findByVideoName(String videoName, int size, int page) {
        return CompletableFuture.completedFuture(new Page<>(List.of(), size, page, 0L));
    }

    @Override
    public CompletableFuture<Page<Annotation>> findByConceptName(String conceptName, int size, int page, boolean includeDescendants) {
        return CompletableFuture.completedFuture(new Page<>(List.of(), size, page, 0L));
    }
}
