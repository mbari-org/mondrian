package org.mbari.mondrian.services;

import org.mbari.vars.services.model.Media;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface MediaService {

    CompletableFuture<List<String>> findAllCameraDeployments();

    CompletableFuture<List<UUID>> findMediaByCameraDeployment(String videoSequenceName);
}
