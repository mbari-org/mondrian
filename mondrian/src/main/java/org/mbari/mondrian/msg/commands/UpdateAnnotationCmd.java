package org.mbari.mondrian.msg.commands;

import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.msg.messages.UpdateAnnotationInViewMsg;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * @author Brian Schlining
 * @since 2017-05-10T10:05:00
 */
public class UpdateAnnotationCmd implements Command {

    private final Annotation oldAnnotation;
    private final Annotation newAnnotation;

    public UpdateAnnotationCmd(Annotation oldAnnotation, Annotation newAnnotation) {
        this.oldAnnotation = oldAnnotation;
        this.newAnnotation = newAnnotation;
    }

    @Override
    public void apply(ToolBox toolBox) {
        newAnnotation.setObservationTimestamp(Instant.now());
        Optional.ofNullable(toolBox.data().getUser())
                .ifPresent(user -> newAnnotation.setObserver(user.getUsername()));
        doAction(toolBox, newAnnotation);
    }

    @Override
    public void unapply(ToolBox toolBox) {
        doAction(toolBox, oldAnnotation);
    }

    private void doAction(ToolBox toolBox, Annotation annotation) {
        toolBox.servicesProperty()
                .get()
                .namesService()
                .findConcept(annotation.getConcept())
                .thenAccept(opt -> {
                    if (opt.isPresent()) {
                        // Update to primary concept name
                        newAnnotation.setConcept(opt.get().primaryName());
                         toolBox.servicesProperty()
                                 .get()
                                .annotationService()
                                .update(annotation)
                                .thenAccept(a -> {
//                                    toolBox.eventBus()
//                                            .publish(new RerenderAnnotationsMsg());
                                    toolBox.eventBus().publish(new UpdateAnnotationInViewMsg(a));
                                });
                    }
                });

    }

    @Override
    public String getDescription() {
        return "Update annotation";
    }

    @Override
    public String toString() {
        return "UpdateAnnotationCmd{" +
                "oldAnnotation=" + oldAnnotation +
                ", newAnnotation=" + newAnnotation +
                '}';
    }
}