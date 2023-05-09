package org.mbari.mondrian.services.noop;

import org.mbari.mondrian.services.MediaService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NoopMediaService implements MediaService {
    @Override
    public CompletableFuture<List<String>> findAllCameraDeployments() {
        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public CompletableFuture<List<UUID>> findMediaByCameraDeployment(String videoSequenceName) {
        return CompletableFuture.completedFuture(List.of());
    }
}
