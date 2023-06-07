package org.mbari.mondrian.msg.commands;

import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.msg.messages.RerenderAnnotationsMsg;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Add an associa
 * @author Brian Schlining
 * @since 2017-07-20T17:35:00
 */
public class CreateAssociationsCmd implements Command {

    private final Association associationTemplate;
    private final Collection<Annotation> originalAnnotations;
    private Collection<Association> addedAssociations = new CopyOnWriteArrayList<>();

    public CreateAssociationsCmd(Association associationTemplate, Collection<Annotation> originalAnnotations) {
        this.associationTemplate = associationTemplate;
        this.originalAnnotations = Collections.unmodifiableCollection(originalAnnotations);

    }

    public Association getAssociationTemplate() {
        return associationTemplate;
    }

    @Override
    public void apply(ToolBox toolBox) {
        var associationService = toolBox.servicesProperty().get().associationService();
        var futures = originalAnnotations.stream()
                .map(anno -> associationService.create(anno.getObservationUuid(), associationTemplate)
                        .thenAccept(association -> addedAssociations.add(association)))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures)
                .thenAccept(v -> {
//                    Set<UUID> uuids = originalAnnotations.stream()
//                            .map(Annotation::getObservationUuid)
//                            .collect(Collectors.toSet());
                    toolBox.eventBus().publish(new RerenderAnnotationsMsg());
                });
    }

    @Override
    public void unapply(ToolBox toolBox) {
        var associationService = toolBox.servicesProperty().get().associationService();
        List<UUID> uuids = addedAssociations.stream()
                .map(Association::getUuid)
                .collect(Collectors.toList());
        associationService.deleteAll(uuids)
                .thenAccept(v -> {
                    addedAssociations.clear();
//                    Set<UUID> uuids0 = originalAnnotations.stream()
//                            .map(Annotation::getObservationUuid)
//                            .collect(Collectors.toSet());
                    toolBox.eventBus().publish(new RerenderAnnotationsMsg());
                });

    }

    @Override
    public String getDescription() {
        return "Add Association: " + associationTemplate;
    }
}
