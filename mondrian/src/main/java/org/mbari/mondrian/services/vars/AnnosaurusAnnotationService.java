package org.mbari.mondrian.services.vars;

import org.mbari.mondrian.domain.Page;
import org.mbari.mondrian.etc.okhttp3.ClientSupport;
import org.mbari.mondrian.services.AnnotationService;
import org.mbari.vars.services.MediaService;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.services.model.MultiRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AnnosaurusAnnotationService implements AnnotationService {

    private final org.mbari.vars.services.AnnotationService annotationService;
    private final MediaService mediaService;

    public AnnosaurusAnnotationService(org.mbari.vars.services.AnnotationService annotationService,
                                       MediaService mediaService) {
        this.annotationService = annotationService;
        this.mediaService = mediaService;
    }

    @Override
    public CompletableFuture<Annotation> create(Annotation annotation) {
        return annotationService.createAnnotations(List.of(annotation))
                .thenApply(xs ->
                     xs.stream()
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Expected to create 1 annotation but found " + xs.size()))
                );
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
        var limit = (long) size;
        var offset = (long) page * size;
        return annotationService.countAnnotations(mediaUuid)
                .thenCompose(annotationCount ->
                    annotationService.findAnnotations(mediaUuid, limit, offset)
                            .thenApply(annos -> new Page<>(annos, size, page, annotationCount.getCount().longValue()))
                );
    }

    @Override
    public CompletableFuture<Page<Annotation>> findByVideoSequenceName(String videoSequenceName, int size, int page) {
        var limit = (long) size;
        var offset = (long) page * size;
        return mediaService.findByVideoSequenceName(videoSequenceName)
                .thenCompose(media -> {
                    var videoReferenceUuids = media.stream().map(Media::getVideoReferenceUuid).toList();
                    var multiRequest = new MultiRequest(videoReferenceUuids);
                    return annotationService.countByMultiRequest(multiRequest)
                            .thenCompose(mrc ->
                                annotationService.findByMultiRequest(multiRequest, limit, offset)
                                        .thenApply(annos -> new Page<>(annos, size, page, mrc.getCount()))
                            );
                });
    }

    @Override
    public CompletableFuture<Page<Annotation>> findByVideoName(String videoName, int size, int page) {
        var limit = (long) size;
        var offset = (long) page * size;
        return mediaService.findByVideoName(videoName)
                .thenCompose(media -> {
                    var videoReferenceUuids = media.stream().map(Media::getVideoReferenceUuid).toList();
                    var multiRequest = new MultiRequest(videoReferenceUuids);
                    return annotationService.countByMultiRequest(multiRequest)
                            .thenCompose(mrc ->
                                    annotationService.findByMultiRequest(multiRequest, limit, offset)
                                            .thenApply(annos -> new Page<>(annos, size, page, mrc.getCount()))
                            );
                });
    }

    @Override
    public CompletableFuture<Page<Annotation>> findByConceptName(String conceptName, int size, int page, boolean includeDescendants) {
        var limit = (long) size;
        var offset = (long) page * size;
        return annotationService.countObservationsByConcept(conceptName)
                        .thenCompose(cc ->
                            annotationService.findByConcept(conceptName, limit, offset, true)
                                    .thenApply(annos -> new Page<>(annos, size, page, cc.getCount().longValue()))
                        );
    }
}
