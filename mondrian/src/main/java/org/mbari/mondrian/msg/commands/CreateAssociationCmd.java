package org.mbari.mondrian.msg.commands;

import org.mbari.mondrian.ToolBox;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;

import java.util.*;

/**
 * Add an associa
 * @author Brian Schlining
 * @since 2017-07-20T17:35:00
 */
public class CreateAssociationCmd implements AnnotationCommand {

    private final Association associationTemplate;
    private final Annotation originalAnnotations;
    private Association addedAssociation;

    public CreateAssociationCmd(Association associationTemplate, Annotation originalAnnotations) {
        this.associationTemplate = associationTemplate;
        this.originalAnnotations = originalAnnotations;

    }

    @Override
    public void apply(ToolBox toolBox) {
        var associationService = toolBox.servicesProperty().get().associationService();
        var annotationService = toolBox.servicesProperty().get().annotationService();
        associationService.create(originalAnnotations.getObservationUuid(), associationTemplate)
                .thenCompose(association -> {
                    addedAssociation = association;
                    return annotationService.findByUuid(originalAnnotations.getObservationUuid());
                })
                .thenAccept(opt -> {
                    var anno = opt.orElse(null);
                    handleUpdatedAnnotation(toolBox, anno, "Failed to find annotation after updating it");
                });

    }

    @Override
    public void unapply(ToolBox toolBox) {
        var associationService = toolBox.servicesProperty().get().associationService();
        var annotationService = toolBox.servicesProperty().get().annotationService();
        List<UUID> uuids = List.of(addedAssociation.getUuid());
        associationService.deleteAll(uuids)
                .thenCompose(v -> {
                    addedAssociation = null;
                    return annotationService.findByUuid(originalAnnotations.getObservationUuid());
                })
                .thenAccept(opt -> {
                    var anno = opt.orElse(null);
                    handleUpdatedAnnotation(toolBox, anno, "Failed to find annotation after removing an association from it");
                });

    }

    @Override
    public String getDescription() {
        return "Add Association: " + associationTemplate;
    }
}
