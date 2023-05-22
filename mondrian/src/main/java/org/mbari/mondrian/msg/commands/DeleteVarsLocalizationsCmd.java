package org.mbari.mondrian.msg.commands;

import javafx.scene.control.Alert;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.domain.Selection;
import org.mbari.mondrian.domain.VarsLocalization;
import org.mbari.mondrian.msg.events.AddLocalizationEvents;
import org.mbari.mondrian.msg.messages.AddVarsLocalizationMsg;
import org.mbari.mondrian.msg.messages.RemoveVarsLocalizationMsg;
import org.mbari.mondrian.msg.messages.ShowAlertMsg;

import java.util.Collection;
import java.util.List;

public class DeleteVarsLocalizationsCmd implements Command {

    private final Collection<VarsLocalization> localizations;

    public DeleteVarsLocalizationsCmd(Collection<VarsLocalization> localizations) {
        this.localizations = localizations;
    }

    @Override
    public void apply(ToolBox toolBox) {
        var observationUuids = localizations.stream()
                .map(vloc -> vloc.getAnnotation().getObservationUuid())
                .toList();
        toolBox.servicesProperty()
                .get()
                .annotationService()
                .delete(observationUuids)
                .handle((ok, ex) -> {
                    if (ex == null && ok) {
                        localizations.forEach(loc -> {
                            var msg = new RemoveVarsLocalizationMsg(new Selection<>(DeleteVarsLocalizationsCmd.this, loc));
                            toolBox.eventBus().publish(msg);
                        });
                    }
                    else {
                        var msg = new ShowAlertMsg(Alert.AlertType.ERROR,
                                "Mondrian - Error",
                                "Failed to delete",
                                "Failed to delete " + observationUuids.size() + " localizations",
                                ex);
                        toolBox.eventBus().publish(msg);
                    }
                    return null;
                });
    }

    @Override
    public void unapply(ToolBox toolBox) {
        var annotations = localizations.stream()
                        .map(VarsLocalization::getAnnotation)
                        .toList();
        // NOTE: If we don't clear the image list annosaurus will try to create new
        // images. This will fail as both the image's URL and UUID already exist in
        // the database and duplicates are not allowed. We just clear the list here
        // to avoid that.
        annotations.forEach(a -> a.setImages(List.of()));
        toolBox.servicesProperty()
                .get()
                .annotationService()
                .create(annotations)
                .thenAccept(xs -> {
                    localizations.forEach(varsLocalization ->
                        AddLocalizationEvents.from(varsLocalization.getLocalization(), false)
                                .ifPresent(evt -> {
                                    toolBox.eventBus().publish(evt);
                                    toolBox.eventBus().publish(new AddVarsLocalizationMsg(new Selection<>(DeleteVarsLocalizationsCmd.this, varsLocalization)));
                                })
                    );
                });
    }

    @Override
    public String getDescription() {
        return "Delete " + localizations.size() + " localizations";
    }
}
