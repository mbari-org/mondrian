package org.mbari.mondrian.javafx.roi;

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

    private static RoiTranslatorBoundingBox roiBox = new RoiTranslatorBoundingBox();
    private static RoiTranslatorLine roiLine = new RoiTranslatorLine();
    private static RoiTranslatorMarker roiMarker = new RoiTranslatorMarker();
    private static RoiTranslatorPolygon roiPolygon = new RoiTranslatorPolygon();

    private static final Map<String, RoiTranslator<? extends DataView<? extends Data, ? extends Shape>>> LINK_VALUES_TO_ROI_MAP = Map.of(
            RoiTranslatorBoundingBox.LINK_NAME, roiBox,
            RoiTranslatorLine.LINK_NAME, roiLine,
            RoiTranslatorMarker.LINK_NAME, roiMarker,
            RoiTranslatorPolygon.LINK_NAME, roiPolygon
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

    public static Optional<RoiTranslator<? extends DataView<? extends Data, ? extends Shape>>> findByLocalization(Localization<? extends DataView<? extends Data, ? extends Shape>, ImageView> localization) {
        var dataView = localization.getDataView();
        if (dataView instanceof MarkerView) {
            return Optional.of(roiMarker);
        }
        else if (dataView instanceof LineView) {
            return Optional.of(roiLine);
        }
        else if (dataView instanceof RectangleView) {
            return Optional.of(roiBox);
        }
        else if (dataView instanceof PolygonView) {
            return Optional.of(roiPolygon);
        }
        else {
            return Optional.empty();
        }
    }

//    public static Optional<Association> fromLocalization(Localization<? extends DataView<? extends Data, ? extends Shape>, ImageView> localization,
public static Optional<Association> fromLocalization(Localization<?, ?> localization,
                                 UUID imageReferenceUuid,
                                 String comment) {

        var dataView = localization.getDataView();
        Association association = null;

        if (dataView instanceof MarkerView) {
            var loc = (Localization<MarkerView, ImageView>) localization;
            association = roiMarker.fromLocalization(loc, imageReferenceUuid, comment);
        }
        else if (dataView instanceof LineView) {
            var loc = (Localization<LineView, ImageView>) localization;
            association = roiLine.fromLocalization(loc, imageReferenceUuid, comment);
        }
        else if (dataView  instanceof RectangleView) {
            var loc = (Localization<RectangleView, ImageView>) localization;
            association = roiBox.fromLocalization(loc, imageReferenceUuid, comment);
        }
        else if (dataView instanceof PolygonView) {
            var loc = (Localization<PolygonView, ImageView>) localization;
            association = roiPolygon.fromLocalization(loc, imageReferenceUuid, comment);
        }
        return Optional.ofNullable(association);

    }


}
