package org.mbari.mondrian.msg.commands;

import org.mbari.mondrian.ToolBox;
import org.mbari.vars.services.model.Association;

import java.util.UUID;

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
                .thenAccept(opt -> {
                    Association a = opt.map(c -> new Association(newAssociation.getLinkName(),
                            opt.get().primaryName(),
                            newAssociation.getLinkValue(),
                            newAssociation.getMimeType(),
                            newAssociation.getUuid())).orElse(newAssociation);
                    doUpdate(toolBox, a);
                });
    }

    @Override
    public void unapply(ToolBox toolBox) {
        doUpdate(toolBox, oldAssociation);
    }

    private void doUpdate(ToolBox toolBox, Association association) {
        var associationService = toolBox.servicesProperty().get().associationService();
        var annotationService = toolBox.servicesProperty().get().annotationService();
        associationService.update(association)
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