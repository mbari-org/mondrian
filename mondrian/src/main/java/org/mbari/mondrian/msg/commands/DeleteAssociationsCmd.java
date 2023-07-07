package org.mbari.mondrian.msg.commands;

import javafx.scene.control.Alert;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.msg.messages.ShowAlertMsg;
import org.mbari.mondrian.msg.messages.UpdateAnnotationInViewMsg;
import org.mbari.vars.core.util.Preconditions;
import org.mbari.vars.services.model.Association;


import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-05-11T13:06:00
 */
public class DeleteAssociationsCmd implements AnnotationCommand {

    /** key = an association attached to that obervaton, value = observationUuid, */
    private final UUID observationUuid;
    private Map<Association, UUID> associationMap;


    public DeleteAssociationsCmd(UUID observationUuid, Collection<Association> associations) {
        Preconditions.checkArgument(observationUuid != null,
                "Missing an observationUuid.");
        Preconditions.checkArgument(associations != null,
                "associations is null");
        Preconditions.checkArgument(!associations.isEmpty(),
                "associations is empty");
        this.observationUuid = observationUuid;
        Function<Association, UUID> fn = a -> observationUuid;
        this.associationMap = associations.stream()
                .collect(Collectors.toMap(Function.identity(), fn));
    }

    @Override
    public void apply(ToolBox toolBox) {
        var associationService = toolBox.servicesProperty().get().associationService();
        var annotationService = toolBox.servicesProperty().get().annotationService();
        Collection<UUID> uuids = associationMap.keySet()
                .stream()
                .map(Association::getUuid)
                .collect(Collectors.toList());
        associationService.deleteAll(uuids)
                .thenCompose(v -> annotationService.findByUuid(observationUuid))
                .thenAccept(opt -> {
                    var anno = opt.orElse(null);
                    handleUpdatedAnnotation(toolBox, anno, "Failed to find annotation with uuid = " + observationUuid);
                });
    }

    @Override
    public void unapply(ToolBox toolBox) {
        var associationService = toolBox.servicesProperty().get().associationService();
        var annotationService = toolBox.servicesProperty().get().annotationService();
        Map<Association, UUID> newMap = new ConcurrentHashMap<>();
        CompletableFuture[] futures = associationMap.entrySet()
                .stream()
                .map(e -> associationService.create(e.getValue(), e.getKey())
                        .thenAccept(a -> newMap.put(a, e.getValue())))  // Need to collect new Associations as the UUID will have changed
                .toArray(i -> new CompletableFuture[i]);
        CompletableFuture.allOf(futures)
                .thenCompose(v -> {
                    associationMap = newMap;  // Update stored associations with the new keys
                    return annotationService.findByUuid(observationUuid);
                })
                .thenAccept(opt -> {
                    var anno = opt.orElse(null);
                    handleUpdatedAnnotation(toolBox, anno, "Failed to find annotation with uuid = " + observationUuid);
                });

    }

    @Override
    public String getDescription() {
        return "Delete Associations";
    }

}
