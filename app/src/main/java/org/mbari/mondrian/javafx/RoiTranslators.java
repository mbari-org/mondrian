package org.mbari.mondrian.javafx;

import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Shape;
import org.mbari.imgfx.roi.*;
import org.mbari.vars.services.model.Association;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class RoiTranslators {

    private RoiTranslators() {
        // No instantiation.
    }

    private static final Map<String, RoiTranslator<? extends DataView<? extends Data, ? extends Shape>>> LINK_VALUES_TO_ROI_MAP = Map.of(
            RoiTranslatorBoundingBox.LINK_NAME, new RoiTranslatorBoundingBox(),
            RoiTranslatorLine.LINK_NAME, new RoiTranslatorLine(),
            RoiTranslatorMarker.LINK_NAME, new RoiTranslatorMarker(),
            RoiTranslatorPolygon.LINK_NAME, new RoiTranslatorPolygon()
    );

    /**
     * @param linkName The association's link name. This describes the type of localization
     * @return A translator appropriate for the given link name
     */
    public static Optional<RoiTranslator<? extends DataView<? extends Data, ? extends Shape>>> findByLinkName(String linkName) {
        if (LINK_VALUES_TO_ROI_MAP.containsKey(linkName)) {
            return Optional.of(LINK_VALUES_TO_ROI_MAP.get(linkName));
        }
        else {
            return Optional.empty();
        }
    }

    public static Optional<RoiTranslator<? extends DataView<? extends Data, ? extends Shape>>> findByLocalization(Localization<? extends DataView<? extends Data, ? extends Shape>, ? extends Node> localization) {
        var dataView = localization.getDataView();
        if (dataView instanceof MarkerView) {
            return Optional.of(LINK_VALUES_TO_ROI_MAP.get(RoiTranslatorMarker.LINK_NAME));
        }
        else if (dataView instanceof LineView) {
            return Optional.of(LINK_VALUES_TO_ROI_MAP.get(RoiTranslatorLine.LINK_NAME));
        }
        else if (dataView instanceof RectangleView) {
            return Optional.of(LINK_VALUES_TO_ROI_MAP.get(RoiTranslatorBoundingBox.LINK_NAME));
        }
        else if (dataView instanceof PolygonView) {
            return Optional.of(LINK_VALUES_TO_ROI_MAP.get(RoiTranslatorPolygon.LINK_NAME));
        }
        else {
            return Optional.empty();
        }
    }

    public static Optional<Association> fromLocalization(Localization<? extends DataView<? extends Data, ? extends Shape>, ? extends Node> localization,
                                               UUID imageReferenceUuid,
                                               String comment) {
        return findByLocalization(localization).map(t -> t.fromLocalization(localization, imageReferenceUuid, comment));
    }


}
