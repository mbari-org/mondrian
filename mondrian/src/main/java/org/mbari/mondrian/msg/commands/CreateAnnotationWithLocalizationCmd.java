package org.mbari.mondrian.msg.commands;

import javafx.scene.image.ImageView;
import javafx.scene.shape.Shape;
import org.mbari.imgfx.roi.DataView;
import org.mbari.imgfx.roi.Localization;
import org.mbari.mondrian.Data;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.javafx.roi.Datum;
import org.mbari.mondrian.javafx.roi.Datums;
import org.mbari.mondrian.javafx.roi.RoiTranslators;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Image;

import java.util.List;

public class CreateAnnotationWithLocalizationCmd implements Command {

    private final String observer;
    private final Image image;
    private final String concept;
    private final Localization<? extends DataView<? extends Data, ? extends Shape>, ImageView> localization;
    private final String comment;
    private final Datum<?> datum;


    public CreateAnnotationWithLocalizationCmd(String observer,
                                               Image image,
                                               String concept,
                                               Localization<? extends DataView<? extends Data, ? extends Shape>, ImageView> localization,
                                               String comment) {
        this.observer = observer;
        this.image = image;
        this.concept = concept;
        this.localization = localization;
        this.comment = comment;
        var opt = Datums.from(localization.getDataView().getData());
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Unable to extract valid datum from " + localization);
        }
        this.datum = opt.get();
    }

    @Override
    public void apply(ToolBox toolBox) {
        if (datum != null) {
            var opt = RoiTranslators.fromLocalization(localization,
                    image.getImageReferenceUuid(),
                    comment);
            if (opt.isPresent()) {
                var association = opt.get();
                // TODO need login
                var annotation = new Annotation(concept, observer, image.getVideoIndex(), image.getVideoReferenceUuid());
                annotation.setAssociations(List.of(association));
                toolBox.servicesProperty()
                        .get()
                        .annotationService()
                        .create(annotation)
                        .thenAccept(a -> {
                            var assoc = a.getAssociations().get(0);
                            localization.setUuid(assoc.getUuid());
                            // l
                        });
            }
            else {

            }
        }
    }

    @Override
    public void unapply(ToolBox toolBox) {
        if (datum != null) {

        }
    }

    @Override
    public String getDescription() {
        return null;
    }
}
