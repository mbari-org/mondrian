package org.mbari.mondrian.services.vars;

import org.mbari.mondrian.services.MediaService;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VampireSquidService implements MediaService {

    private final org.mbari.vars.vampiresquid.sdk.r1.MediaService mediaService;

    public VampireSquidService(org.mbari.vars.vampiresquid.sdk.r1.MediaService mediaService) {
        this.mediaService = mediaService;
    }

    public org.mbari.vars.vampiresquid.sdk.r1.MediaService getMediaService() {
        return mediaService;
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
