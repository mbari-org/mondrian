package org.mbari.mondrian.services.vars;

import org.mbari.mondrian.services.MediaService;
import org.mbari.vars.services.model.Media;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VampireSquidService implements MediaService {

    private final org.mbari.vars.services.MediaService mediaService;

    public VampireSquidService(org.mbari.vars.services.MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @Override
    public CompletableFuture<List<String>> findAllCameraDeployments() {
        return mediaService.findAllVideoSequenceNames();
    }

    @Override
    public CompletableFuture<List<UUID>> findMediaByCameraDeployment(String videoSequenceName) {
        return mediaService.findByVideoName(videoSequenceName)
                .thenApply(media -> media.stream().map(Media::getVideoReferenceUuid).toList());
    }
}
