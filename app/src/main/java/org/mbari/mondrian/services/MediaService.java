package org.mbari.mondrian.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MediaService {

    CompletableFuture<List<String>> findAllCameraDeployments();
}
