package org.mbari.mondrian.msg.commands;

import javafx.scene.control.Alert;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.domain.VarsLocalization;
import org.mbari.mondrian.msg.messages.RerenderAnnotationsMsg;
import org.mbari.mondrian.msg.messages.ShowAlertMsg;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.oni.sdk.r1.models.User;


import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UpdateAnnotationsConceptCmd implements Command {

    private final List<VarsLocalization> varsLocalizations;
    private final List<Annotation> originalAnnotations;
    private final boolean updateUser;
    private final String newConceptName;

    public UpdateAnnotationsConceptCmd(Collection<VarsLocalization> varsLocalizations,
                                       String newConceptName,
                                       boolean updateUser) {
        this.varsLocalizations = new ArrayList<>(varsLocalizations);
        this.originalAnnotations = this.varsLocalizations
                .stream()
                .map(vloc -> new Annotation(vloc.getAnnotation()))
                .toList();
        this.newConceptName = newConceptName;
        this.updateUser = updateUser;
    }

    @Override
    public void apply(ToolBox toolBox) {
        var now = Instant.now();
        var annotations = varsLocalizations.stream()
                .peek(vloc -> {
                    var anno = vloc.getAnnotation();
                    anno.setConcept(newConceptName);
                    anno.setObservationTimestamp(now);
                    if (updateUser) {
                        final User user = toolBox.data().getUser();
                        if (user != null) {
                            anno.setObserver(user.getUsername());
                        }
                    }
                    vloc.getLocalization().setLabel(newConceptName);
                })
                .map(VarsLocalization::getAnnotation)
                .toList();
        toolBox.servicesProperty()
                .get()
                .annotationService()
                .updateAll(annotations)
                .thenAccept(annos -> {
                    var msg = new RerenderAnnotationsMsg();
                    toolBox.eventBus().publish(msg);
                })
                .exceptionally(ex -> {
                    var msg = new ShowAlertMsg(Alert.AlertType.WARNING,
                            "Mondrian",
                            "Failed to update annotations",
                            "Something bad happened when updating annotations",
                            ex);
                    toolBox.eventBus().publish(msg);
                    return null;
                });
    }

    @Override
    public void unapply(ToolBox toolBox) {
        toolBox.servicesProperty()
                .get()
                .annotationService()
                .updateAll(originalAnnotations)
                .thenAccept(annos -> {
                    for (int i = 0; i < originalAnnotations.size(); i++) {
                        var copy = originalAnnotations.get(i);
                        var vloc = varsLocalizations.get(i);
                        var anno = vloc.getAnnotation();
                        anno.setConcept(copy.getConcept());
                        anno.setObserver(copy.getObserver());
                        anno.setObservationTimestamp(copy.getObservationTimestamp());
                        vloc.getLocalization().setLabel(copy.getConcept());
                    }
                    var msg = new RerenderAnnotationsMsg();
                    toolBox.eventBus().publish(msg);
                })
                .exceptionally(ex -> {
                    var msg = new ShowAlertMsg(Alert.AlertType.WARNING,
                            "Mondrian",
                            "Failed to undo the update of annotations",
                            "Something bad happened when undoing the update of annotations",
                            ex);
                    toolBox.eventBus().publish(msg);
                    return null;
                });
    }

    @Override
    public String getDescription() {
        return null;
    }
}
