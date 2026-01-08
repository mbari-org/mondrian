package org.mbari.mondrian.msg.commands;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.domain.VarsLocalization;
import org.mbari.mondrian.javafx.roi.Datum;
import org.mbari.mondrian.javafx.roi.Datums;
import org.mbari.mondrian.msg.messages.ShowAlertMsg;
import org.mbari.mondrian.msg.messages.UpdateVarsLocalizationMsg;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;

public class UpdateLocalizationCmd implements Command {

    /** This is a snapshot base on the localization at the time the command is created */
    private final VarsLocalization varsLocalization;

    /** This is the fetched from the database association before we make any changes */
    private Association originalAssociation;
    private Datum<?> datum;

    public UpdateLocalizationCmd(VarsLocalization varsLocalization) {
        this.varsLocalization = varsLocalization.updateUsingLocalizationData();
        this.datum = Datums.from(varsLocalization.getLocalization().getDataView().getData())
                .orElseThrow();
    }

    @Override
    public void apply(ToolBox toolBox) {
        // Get original association from database and save it
        var service = toolBox.servicesProperty()
                .get()
                .associationService();

        service.findByUuid(varsLocalization.getAssociation().getUuid())
                .thenAccept(opt -> {
                    opt.ifPresent(ass -> {
                        originalAssociation = ass;
                    });
                })
                .thenCompose(Void -> service.update(varsLocalization.getAssociation()))
                .thenAccept(ass -> {
                    var msg = new UpdateVarsLocalizationMsg(varsLocalization);
                    toolBox.eventBus().publish(msg);
                })
                .exceptionally(ex -> {
                    var msg = new ShowAlertMsg(Alert.AlertType.WARNING,
                            "Mondrian",
                            "Failed to update an association in a localization",
                            "Something bad happened when updating " + originalAssociation,
                            ex);
                    toolBox.eventBus().publish(msg);
                    return null;
                });
    }

    @Override
    public void unapply(ToolBox toolBox) {
        var service = toolBox.servicesProperty()
                .get()
                .associationService();
        service.update(originalAssociation)
                .thenAccept(ass0 -> {
                    var dataView = varsLocalization.getLocalization().getDataView();

                    Platform.runLater(() -> {
                        var ok = Datums.update(dataView.getData(), datum);
                        // TODO if not OK?
                        dataView.updateView();
                    });
                    var newVLoc = varsLocalization.updateUsingLocalizationData();
                    var msg = new UpdateVarsLocalizationMsg(newVLoc);
                    toolBox.eventBus().publish(msg);
                })
                .exceptionally(ex -> {
                    var msg = new ShowAlertMsg(Alert.AlertType.WARNING,
                            "Mondrian",
                            "Failed to undo the update of an association in a localization",
                            "Something bad happened when undoing the update of " + originalAssociation,
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
