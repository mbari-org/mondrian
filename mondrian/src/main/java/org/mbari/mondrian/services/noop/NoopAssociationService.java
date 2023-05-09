package org.mbari.mondrian.services.noop;

import org.mbari.mondrian.services.AssociationService;
import org.mbari.vars.services.model.Association;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NoopAssociationService implements AssociationService {
    @Override
    public CompletableFuture<Association> create(Association association) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not implemented"));
    }

    @Override
    public CompletableFuture<Association> update(Association association) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not implemented"));
    }

    @Override
    public CompletableFuture<Boolean> delete(Association association) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not implemented"));
    }

    @Override
    public CompletableFuture<Optional<Association>> findByUuid(UUID uuid) {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletableFuture<List<Association>> findByObservationUuid(UUID observationUuid) {
        return CompletableFuture.completedFuture(List.of());
    }
}
