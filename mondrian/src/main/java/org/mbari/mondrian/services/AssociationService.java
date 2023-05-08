package org.mbari.mondrian.services;

import org.mbari.vars.services.model.Association;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AssociationService {

    CompletableFuture<Association> create(Association association);
    CompletableFuture<Association> update(Association association);
    CompletableFuture<Boolean> delete(Association association);
    CompletableFuture<Optional<Association>> findByUuid(UUID uuid);
    CompletableFuture<List<Association>> findByObservationUuid(UUID observationUuid);
}
