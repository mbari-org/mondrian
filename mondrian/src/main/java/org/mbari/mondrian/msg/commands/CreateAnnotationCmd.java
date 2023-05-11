package org.mbari.mondrian.msg.commands;

import org.mbari.mondrian.ToolBox;
import org.mbari.vars.services.model.Annotation;

/**
 * @author Brian Schlining
 * @since 2017-05-10T10:02:00
 */
public class CreateAnnotationCmd implements Command {
    private final Annotation annotationTemplate;
    private Annotation annotation;

    public CreateAnnotationCmd(Annotation annotationTemplate) {
        this.annotationTemplate = annotationTemplate;
    }

    @Override
    public void apply(ToolBox toolBox) {
        // Timecode/elapsedtime should have already been captured  from video
//        var services = toolBox.servicesProperty().get();
//        services.namesService()
//                .findConcept(annotation.getConcept())
//                .thenAccept(opt -> {
//                    if (opt.isPresent()) {
//                        // Update to primary name
//                        annotationTemplate.setConcept(opt.get().primaryName());
//                        services.annotationService()
//                                .create(annotationTemplate)
//                                .thenAccept(a -> {
//                                    annotation = a;
//                                    toolBox.eventBus()
//                                            .publish(new AnnotationsAddedEvent(a));
//                                });
//                    }
//                });

    }

    @Override
    public void unapply(ToolBox toolBox) {
//        if (annotation != null) {
//            toolBox.getServices()
//                    .getAnnotationService()
//                    .deleteAnnotation(annotation.getObservationUuid())
//                    .thenAccept(b -> {
//                        annotation = null;
//                        toolBox.getEventBus().send(new AnnotationsRemovedEvent(annotation));
//                    });
//        }
    }


    @Override
    public String getDescription() {
        return "Create an annotation";
    }
}