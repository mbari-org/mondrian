package org.mbari.mondrian.msg.commands;

import javafx.scene.control.Alert;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.msg.messages.ShowAlertMsg;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-05-10T10:06:00
 */
public class UpdateAssociationCmd implements AnnotationCommand {

    private final UUID observationUuid;
    private final Association oldAssociation;
    private final Association newAssociation;

    public UpdateAssociationCmd(UUID observationUuid, Association oldAssociation, Association newAssociation) {
        this.observationUuid = observationUuid;
        this.oldAssociation = oldAssociation;
        this.newAssociation = newAssociation;
    }

    @Override
    public void apply(ToolBox toolBox) {
        var conceptService = toolBox.servicesProperty().get().namesService();
        // Make sure we're using a primary name in the toConcept
        conceptService.findConcept(newAssociation.getToConcept())
                .thenCompose(opt -> {
                    Association a = opt.map(c -> new Association(newAssociation.getLinkName(),
                            opt.get().primaryName(),
                            newAssociation.getLinkValue(),
                            newAssociation.getMimeType(),
                            newAssociation.getUuid())).orElse(newAssociation);
                    return doUpdate(toolBox, a);
                })
                .exceptionally(ex -> {
                    var msg = new ShowAlertMsg(Alert.AlertType.WARNING,
                            "Mondrian",
                            "Failed to update an association",
                            "Something bad happened when updating " + oldAssociation + " to " + newAssociation,
                            ex);
                    toolBox.eventBus().publish(msg);
                    return null;
                });
    }

    @Override
    public void unapply(ToolBox toolBox) {
        doUpdate(toolBox, oldAssociation)
                .exceptionally(ex -> {
                    var msg = new ShowAlertMsg(Alert.AlertType.WARNING,
                            "Mondrian",
                            "Failed to undo the update of an association",
                            "Something bad happened when updating " + newAssociation + " to " + oldAssociation,
                            ex);
                    toolBox.eventBus().publish(msg);
                    return null;
                });
    }

    private CompletableFuture<Void> doUpdate(ToolBox toolBox, Association association) {
        var associationService = toolBox.servicesProperty().get().associationService();
        var annotationService = toolBox.servicesProperty().get().annotationService();
        return associationService.update(association)
                .thenCompose(a -> annotationService.findByUuid(observationUuid))
                .thenAccept(opt -> {
                    var anno = opt.orElse(null);
                    handleUpdatedAnnotation(toolBox, anno, "Failed to update the association: " + association);
                });
    }

    @Override
    public String getDescription() {
        return "Update Association: " + newAssociation;
    }
}