package org.mbari.mondrian.msg.commands;

import javafx.scene.control.Alert;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.msg.messages.ShowAlertMsg;
import org.mbari.mondrian.msg.messages.UpdateAnnotationInViewMsg;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;

import java.util.*;

/**
 * Add an associa
 * @author Brian Schlining
 * @since 2017-07-20T17:35:00
 */
public class CreateAssociationCmd implements Command {

    private final Association associationTemplate;
    private final Annotation originalAnnotations;
    private Association addedAssociation;

    public CreateAssociationCmd(Association associationTemplate, Annotation originalAnnotations) {
        this.associationTemplate = associationTemplate;
        this.originalAnnotations = originalAnnotations;

    }

    public Association getAssociationTemplate() {
        return associationTemplate;
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
                    if (opt.isEmpty()) {
                        var msg = new ShowAlertMsg(Alert.AlertType.WARNING, "Mondrian",
                                "Oops",
                                "Failed to find annotation after updating it");
                        toolBox.eventBus().publish(msg);

                    }
                    else {
                        var annotation = opt.get();
                        var msg = new UpdateAnnotationInViewMsg(annotation);
                        toolBox.eventBus().publish(msg);
                    }
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
                    if (opt.isEmpty()) {
                        var msg = new ShowAlertMsg(Alert.AlertType.WARNING, "Mondrian",
                                "Oops",
                                "Failed to find annotation after removing an association from it");
                        toolBox.eventBus().publish(msg);

                    }
                    else {
                        var annotation = opt.get();
                        var msg = new UpdateAnnotationInViewMsg(annotation);
                        toolBox.eventBus().publish(msg);
                    }
                });

    }

    @Override
    public String getDescription() {
        return "Add Association: " + associationTemplate;
    }
}
