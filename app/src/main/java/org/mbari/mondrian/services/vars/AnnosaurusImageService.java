package org.mbari.mondrian.services.vars;

import org.mbari.mondrian.domain.Page;
import org.mbari.mondrian.etc.okhttp3.ClientSupport;
import org.mbari.mondrian.services.ImageService;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.MediaService;
import org.mbari.vars.services.model.Image;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AnnosaurusImageService implements ImageService {

    private final org.mbari.vars.services.AnnotationService annotationService;
    private final MediaService mediaService;
    private final ClientSupport clientSupport;

    public AnnosaurusImageService(AnnotationService annotationService,
                                  MediaService mediaService,
                                  ClientSupport clientSupport) {
        this.annotationService = annotationService;
        this.mediaService = mediaService;
        this.clientSupport = clientSupport;
    }

    @Override
    public CompletableFuture<Optional<Image>> findByUuid(UUID uuid) {
        return annotationService.findImageByUuid(uuid)
                .thenApply(Optional::ofNullable);
    }

    @Override
    public CompletableFuture<Page<Image>> findByMediaUuid(UUID mediaUuid, int size, int page) {
        var limit = (long) size;
        var offset = (long) page * size;
        return annotationService.count(mediaUuid)
                .thenCompose(annotationCount ->
                        annotationService.findAnnotations(mediaUuid, limit, offset)
                                .thenApply(annos -> new Page<>(annos, size, page, annotationCount.getCount().longValue()))
                );
    }

    @Override
    public CompletableFuture<Page<Image>> findByVideoSequenceName(String videoSequenceName, int size, int page) {
        return null;
    }

    @Override
    public CompletableFuture<Page<Image>> findByVideoName(String videoName, int size, int page) {
        return null;
    }

    @Override
    public CompletableFuture<Page<Image>> findByConceptName(String conceptName, int size, int page, boolean includeDescendants) {
        return null;
    }
}
