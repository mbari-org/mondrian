package org.mbari.mondrian.services.vars;

import org.mbari.mondrian.services.AssociationService;
import org.mbari.vars.annosaurus.sdk.r1.AnnotationService;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;


import java.util.Collection;
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
    public CompletableFuture<Association> create(UUID observationUuid, Association association) {
        return annotationService.createAssociation(observationUuid, association);
    }

    @Override
    public CompletableFuture<Association> update(Association association) {
        return annotationService.updateAssociation(association);
    }

    @Override
    public CompletableFuture<Boolean> delete(UUID associationUuid) {
        return annotationService.deleteAssociation(associationUuid);
    }

    @Override
    public CompletableFuture<Boolean> deleteAll(Collection<UUID> associationUuids) {
        return annotationService.deleteAssociations(associationUuids).thenApply(Void -> true);
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
        return annotationService.findByUuid(observationUuid)
                .thenApply(Annotation::getAssociations);
    }
}
