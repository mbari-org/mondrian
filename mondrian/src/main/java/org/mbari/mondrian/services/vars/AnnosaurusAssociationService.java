package org.mbari.mondrian.services.vars;

import org.mbari.mondrian.services.AssociationService;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.model.Association;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AnnosaurusAssociationService implements AssociationService  {

    private final AnnotationService annotationService;

    public AnnosaurusAssociationService(AnnotationService annotationService) {
        this.annotationService = annotationService;
    }

    @Override
    public CompletableFuture<Association> create(Association association) {
        return null;
    }

    @Override
    public CompletableFuture<Association> update(Association association) {
        return annotationService.updateAssociation(association);
    }

    @Override
    public CompletableFuture<Boolean> delete(Association association) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<Association>> findByUuid(UUID uuid) {
        return annotationService.findAssociationByUuid(uuid)
                .handle((ass, ex) -> {
                    if (ex != null) {
                        return Optional.empty();
                    }
                    else {
                        return Optional.of(ass);
                    }
                });

    }

    @Override
    public CompletableFuture<List<Association>> findByObservationUuid(UUID observationUuid) {
        return null;
    }
}
