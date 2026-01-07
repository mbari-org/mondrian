package org.mbari.mondrian.services;

import org.mbari.vars.oni.sdk.r1.models.Association;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AssociationService {

    CompletableFuture<Association> create(UUID observationUuid, Association association);
    CompletableFuture<Association> update(Association association);
    CompletableFuture<Boolean> delete(UUID associationUuid);
    CompletableFuture<Boolean> deleteAll(Collection<UUID> associationUuids);
    CompletableFuture<Optional<Association>> findByUuid(UUID uuid);
    CompletableFuture<List<Association>> findByObservationUuid(UUID observationUuid);

}
