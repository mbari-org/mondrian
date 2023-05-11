package org.mbari.mondrian.msg.commands;

import org.mbari.imgfx.roi.Localization;
import org.mbari.mondrian.Data;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.javafx.roi.Datum;
import org.mbari.mondrian.javafx.roi.Datums;
import org.mbari.vars.services.model.Image;

import java.awt.*;

public class CreateAnnotationWithLocalizationCmd implements Command {

    private final Image image;
    private final String concept;
    private final Localization<? extends Data, ? extends Shape> localization;
    private final Datum<?> datum;


    public CreateAnnotationWithLocalizationCmd(Image image, String concept, Localization<? extends Data, ? extends Shape> localization) {
        this.image = image;
        this.concept = concept;
        this.localization = localization;
        var opt = Datums.from(localization.getDataView().getData());
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Unable to extract valid datum from " + localization);
        }
        this.datum = opt.get();
    }

    @Override
    public void apply(ToolBox toolBox) {

    }

    @Override
    public void unapply(ToolBox toolBox) {

    }

    @Override
    public String getDescription() {
        return null;
    }
}
