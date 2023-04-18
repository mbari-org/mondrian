package org.mbari.mondrian.javafx;

import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.roi.Localization;
import org.mbari.imgfx.roi.MarkerView;
import org.mbari.mondrian.domain.Points;
import org.mbari.mondrian.etc.gson.Json;
import org.mbari.vars.services.model.Association;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RoiTranslatorMarker implements RoiTranslator<MarkerView> {

    public static final String LINK_NAME = "localization-point";
    private static final Double DEFAULT_RADIUS = 10D;

    @Override
    public <C extends Node> Optional<Localization<MarkerView, C>> fromAssociation(String concept,
                                                                                         Association association, AutoscalePaneController<C> paneController, ObjectProperty<Color> editedColor) {
        var points = Json.GSON.fromJson(association.getLinkValue(), Points.class);
        var view = paneController.getView();
        var radius = 1D;
        if (paneController instanceof ImageView iv) {
            radius = estimateRadius(iv);
        }
        return MarkerView.fromImageCoords(points.getX().get(0).doubleValue(),
                points.getY().get(0).doubleValue(),
                radius,
                paneController.getAutoscale())
            .map(dataView -> new Localization<>(dataView, paneController, association.getUuid(), concept));
    }

    @Override
    public Association fromLocalization(Localization<MarkerView, ? extends Node> localization, UUID imageReferenceUuid, String comment) {
        var marker = localization.getDataView().getData();
        var x = toInt(marker.getCenterX());
        var y = toInt(marker.getCenterY());
        var points = new Points(List.of(x), List.of(y), imageReferenceUuid, comment);
        var linkValue = Json.GSON.toJson(points);
        return new Association(LINK_NAME, Association.VALUE_SELF, linkValue, MEDIA_TYPE, localization.getUuid());
    }

    private double estimateRadius(ImageView imageView) {
        var image = imageView.getImage();
        if (image == null) {
            return DEFAULT_RADIUS;
        }
        else {
            return image.getWidth() / 60D;
        }
    }
}
