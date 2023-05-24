package org.mbari.mondrian.msg.commands;

import javafx.application.Platform;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.domain.VarsLocalization;
import org.mbari.mondrian.javafx.roi.Datum;
import org.mbari.mondrian.javafx.roi.Datums;
import org.mbari.mondrian.msg.messages.UpdateVarsLocalizationMsg;
import org.mbari.vars.services.model.Association;

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
                });
    }

    @Override
    public String getDescription() {
        return null;
    }

}
