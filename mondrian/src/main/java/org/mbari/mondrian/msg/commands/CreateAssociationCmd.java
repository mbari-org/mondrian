package org.mbari.mondrian.msg.commands;

import javafx.scene.control.Alert;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.msg.messages.ShowAlertMsg;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;


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
                })
                .exceptionally(ex -> {
                    var msg = new ShowAlertMsg(Alert.AlertType.WARNING,
                            "Mondrian",
                            "Failed to create an association",
                            "Something bad happened when creating " + associationTemplate,
                            ex);
                    toolBox.eventBus().publish(msg);
                    return null;
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
                })
                .exceptionally(ex -> {
                    var msg = new ShowAlertMsg(Alert.AlertType.WARNING,
                            "Mondrian",
                            "Failed to create an association",
                            "Something bad happened when undoing creating " + associationTemplate,
                            ex);
                    toolBox.eventBus().publish(msg);
                    return null;
                });

    }

    @Override
    public String getDescription() {
        return "Add Association: " + associationTemplate;
    }
}
