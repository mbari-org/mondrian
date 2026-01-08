package org.mbari.mondrian.services.noop;

import org.mbari.mondrian.domain.Counter;
import org.mbari.mondrian.domain.Page;
import org.mbari.mondrian.services.ImageService;
import org.mbari.vars.annosaurus.sdk.r1.models.Image;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NoopImageService implements ImageService {
    @Override
    public CompletableFuture<Optional<Image>> findByUuid(UUID uuid) {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletableFuture<Page<Image>> findByMediaUuid(UUID mediaUuid, int size, int page) {
        return CompletableFuture.completedFuture(new Page<>(List.of(), size, page, 0L));
    }

    @Override
    public CompletableFuture<Page<Image>> findByVideoSequenceName(String videoSequenceName, int size, int page) {
        return CompletableFuture.completedFuture(new Page<>(List.of(), size, page, 0L));
    }

    @Override
    public CompletableFuture<Page<Image>> findByVideoName(String videoName, int size, int page) {
        return CompletableFuture.completedFuture(new Page<>(List.of(), size, page, 0L));
    }

    @Override
    public CompletableFuture<Page<Image>> findByConceptName(String conceptName, int size, int page, boolean includeDescendants) {
        return CompletableFuture.completedFuture(new Page<>(List.of(), size, page, 0L));
    }

    @Override
    public CompletableFuture<Counter> countImagesByMediaUuid(UUID mediaUuid) {
        return CompletableFuture.completedFuture(new Counter(0));
    }
}
