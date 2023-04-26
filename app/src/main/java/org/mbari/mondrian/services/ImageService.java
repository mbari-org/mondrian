package org.mbari.mondrian.services;

import org.mbari.mondrian.domain.Counter;
import org.mbari.mondrian.domain.Page;
import org.mbari.vars.services.model.Image;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ImageService {

    CompletableFuture<Optional<Image>> findByUuid(UUID uuid);
    CompletableFuture<Page<Image>> findByMediaUuid(UUID mediaUuid, int size, int page);
    CompletableFuture<Page<Image>> findByVideoSequenceName(String videoSequenceName, int size, int page);
    CompletableFuture<Page<Image>> findByVideoName(String videoName, int size, int page);
    CompletableFuture<Page<Image>> findByConceptName(String conceptName, int size, int page, boolean includeDescendants);
    CompletableFuture<Counter> countImagesByMediaUuid(UUID mediaUuid);

}
