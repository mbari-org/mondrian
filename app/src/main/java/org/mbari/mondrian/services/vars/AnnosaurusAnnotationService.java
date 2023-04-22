package org.mbari.mondrian.services.vars;

import org.mbari.mondrian.domain.Page;
import org.mbari.mondrian.services.AnnotationService;
import org.mbari.vars.services.MediaService;
import org.mbari.vars.services.model.Annotation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AnnosaurusAnnotationService implements AnnotationService {

    private final org.mbari.vars.services.AnnotationService annotationService;
    private final MediaService mediaService;

    public AnnosaurusAnnotationService(org.mbari.vars.services.AnnotationService annotationService, MediaService mediaService) {
        this.annotationService = annotationService;
        this.mediaService = mediaService;
    }

    @Override
    public CompletableFuture<Annotation> create(Annotation annotation) {
        return annotationService.createAnnotation(annotation);
    }

    @Override
    public CompletableFuture<Annotation> update(Annotation annotation) {
        return annotationService.updateAnnotation(annotation);
    }

    @Override
    public CompletableFuture<Boolean> delete(Annotation annotation) {
        return annotationService.deleteAnnotation(annotation.getObservationUuid());
    }

    @Override
    public CompletableFuture<Optional<Annotation>> findByUuid(UUID uuid) {
        return annotationService.findByUuid(uuid)
                .thenApply(Optional::ofNullable);
    }

    @Override
    public CompletableFuture<List<Annotation>> findByImageUuid(UUID imageUuid) {
        return annotationService.findByImageReference(imageUuid);
    }

    @Override
    public CompletableFuture<Page<Annotation>> findByMediaUuid(UUID mediaUuid, int size, int page) {
        return null;
    }

    @Override
    public CompletableFuture<Page<Annotation>> findByVideoSequenceName(String videoSequenceName, int size, int page) {

        return null;
    }

    @Override
    public CompletableFuture<Page<Annotation>> findByVideoName(String videoName, int size, int page) {
        return null;
    }

    @Override
    public CompletableFuture<Page<Annotation>> findByConceptName(String conceptName, int size, int page, boolean includeDescendants) {
        return null;
    }
}
