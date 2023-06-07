package org.mbari.mondrian.msg.commands;

import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.msg.messages.RerenderAnnotationsMsg;
import org.mbari.vars.core.util.Preconditions;
import org.mbari.vars.services.model.Association;


import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-05-11T13:06:00
 */
public class DeleteAssociationsCmd implements Command {

    /** key = an association attached to that obervaton, value = observationUuid, */
    private Map<Association, UUID> associationMap;

    public DeleteAssociationsCmd(Map<Association, UUID> associations) {
        Preconditions.checkArgument(associations != null,
                "Can not delete a null assotation map");
        Preconditions.checkArgument(!associations.isEmpty(),
                "Can not delete an empty association map");
        this.associationMap = Collections.unmodifiableMap(new HashMap<>(associations));
    }

    @Override
    public void apply(ToolBox toolBox) {
        var service = toolBox.servicesProperty().get().associationService();
        Collection<UUID> uuids = associationMap.keySet()
                .stream()
                .map(Association::getUuid)
                .collect(Collectors.toList());
        service.deleteAll(uuids)
                .thenAccept(v -> {
//                    Set<UUID> observationUuids = new HashSet<>(associationMap.values());
                    toolBox.eventBus().publish(new RerenderAnnotationsMsg());
                });
    }

    @Override
    public void unapply(ToolBox toolBox) {
        var service = toolBox.servicesProperty().get().associationService();
        Map<Association, UUID> newMap = new ConcurrentHashMap<>();
        CompletableFuture[] futures = associationMap.entrySet()
                .stream()
                .map(e -> service.create(e.getValue(), e.getKey())
                        .thenAccept(a -> newMap.put(a, e.getValue())))  // Need to collect new Associations as the UUID will have changed
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures)
                .thenAccept(v -> {
                    associationMap = newMap;  // Update stored associations with the new keys
//                    Set<UUID> observationUuids = new HashSet<>(associationMap.values());
                    toolBox.eventBus().publish(new RerenderAnnotationsMsg());
                });
    }

    @Override
    public String getDescription() {
        return "Delete Associations";
    }

}
