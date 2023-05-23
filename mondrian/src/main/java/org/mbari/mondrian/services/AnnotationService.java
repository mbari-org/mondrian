package org.mbari.mondrian.services;

import org.mbari.mondrian.domain.Page;
import org.mbari.vars.services.model.Annotation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AnnotationService {

    CompletableFuture<Collection<Annotation>> create(Collection<Annotation> annotations);
    CompletableFuture<Annotation> update(Annotation annotation);
    CompletableFuture<Collection<Annotation>> updateAll(Collection<Annotation> annotations);
    CompletableFuture<Boolean> deleteAll(Collection<UUID> observationUuids);
    CompletableFuture<Optional<Annotation>> findByUuid(UUID uuid);
    CompletableFuture<List<Annotation>> findByImageUuid(UUID imageUuid);
    CompletableFuture<Page<Annotation>> findByMediaUuid(UUID mediaUuid, int size, int page);
    CompletableFuture<Page<Annotation>> findByVideoSequenceName(String videoSequenceName, int size, int page);
    CompletableFuture<Page<Annotation>> findByVideoName(String videoName, int size, int page);
    CompletableFuture<Page<Annotation>> findByConceptName(String conceptName, int size, int page, boolean includeDescendants);

}
