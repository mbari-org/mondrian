package org.mbari.mondrian.msg.commands;

import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.domain.Selection;
import org.mbari.mondrian.domain.VarsLocalization;
import org.mbari.mondrian.msg.messages.RemoveVarsLocalizationMsg;
import org.mbari.mondrian.util.SupportUtils;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vcr4j.util.Preconditions;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This is a specialized create an annotation along with any dependents, like
 * imagedMoments or associations. This will also handle cases where the annotation,
 * or associations have predefined UUIDs
 *
 * If that is not your use case, use `CreateAssociationCmd` instead.
 */
public class BulkCreateAnnotations implements Command {

    private final Collection<Annotation> annotations;
    private Collection<VarsLocalization> addedLocalizations;

    public BulkCreateAnnotations(Collection<Annotation> annotations) {
        Preconditions.checkArgument(annotations != null, "You provided a null collection ... bad!!");
        Preconditions.checkArgument(!annotations.isEmpty(), "You are attempting to create annotations from an empty collection. You get nothing!!");
        this.annotations = annotations;
    }

    public Collection<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public void apply(ToolBox toolBox) {
        var annotationService = toolBox.servicesProperty().get().annotationService();
        annotationService.create(annotations)
                .thenAccept(annos -> {

                    // There may not be always be a one to one mapping of annos and
                    // varslocaliations. Hang on to the creat
                    var annotationPaneController = toolBox.annotationPaneControllerProperty().get();
                    addedLocalizations = VarsLocalization.from(annos,
                            annotationPaneController.getAutoscalePaneController(),
                            annotationPaneController.getAnnotationColors().editedColorProperty());
                    // Map localization to correct EventMessage. Set is new to false.send it
                    addedLocalizations.forEach(vloc -> {
                        SupportUtils.publishVarsLocalization(vloc, false, toolBox.eventBus(), BulkCreateAnnotations.this);
                    });

                });
    }

    @Override
    public void unapply(ToolBox toolBox) {
        if (addedLocalizations != null) {
            var annotationService = toolBox.servicesProperty().get().annotationService();
            Set<UUID> observationUuids = addedLocalizations.stream()
                    .map(VarsLocalization::getAnnotation)
                    .map(Annotation::getObservationUuid)
                    .collect(Collectors.toSet());
            annotationService.deleteAll(observationUuids)
                    .thenAccept(ok -> {
                        if (ok) {
                            addedLocalizations.forEach(loc -> {
                                var msg = new RemoveVarsLocalizationMsg(new Selection<>(BulkCreateAnnotations.this, loc));
                                toolBox.eventBus().publish(msg);
                            });
                            addedLocalizations = null;
                        }
                    });
        }
    }

    @Override
    public String getDescription() {
        return "Create " + annotations.size() + " annotations in bulk";
    }
}
