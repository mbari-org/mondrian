package org.mbari.mondrian.javafx;

import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.roi.Data;
import org.mbari.imgfx.roi.DataView;
import org.mbari.imgfx.roi.Localization;
import org.mbari.vars.services.model.Association;

import java.util.Optional;
import java.util.UUID;

/**
 * Provides two-way translations between VARS associations (how data is stored in a database)
 * and imgfx Localizations (how data is represented in the UI)
 * @param <C>
 */
public interface RoiTranslator<C extends DataView<? extends Data, ? extends Shape>> {

    String MEDIA_TYPE = "application/json";

    <D extends Node> Optional<Localization<C, D>> fromAssociation(String concept,
                                                         Association association,
                                                         AutoscalePaneController<D> paneController,
                                                         ObjectProperty<Color> editedColor);

    Association fromLocalization(Localization<C, ? extends Node> localization, UUID imageReferenceUuid, String comment);

    /**
     * Data in the UI is represented as double values. We convert to pixels (integer)
     * when storing in the database.
     * @param d
     * @return
     */
    default int toInt(Double d) {
        return (int) Math.round(d);
    }
}
