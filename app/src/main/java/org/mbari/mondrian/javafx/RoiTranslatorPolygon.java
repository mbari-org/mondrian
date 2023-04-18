package org.mbari.mondrian.javafx;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.roi.Localization;
import org.mbari.imgfx.roi.PolygonView;
import org.mbari.mondrian.domain.Points;
import org.mbari.mondrian.etc.gson.Json;
import org.mbari.vars.services.model.Association;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class RoiTranslatorPolygon implements RoiTranslator<PolygonView> {

    public static String LINK_NAME = "localization-polygon";

    @Override
    public <C extends Node> Optional<Localization<PolygonView, C>> fromAssociation(String concept,
                                                                                   Association association,
                                                                                   AutoscalePaneController<C> paneController,
                                                                                   ObjectProperty<Color> editedColor) {
        var points = Json.GSON.fromJson(association.getLinkValue(), Points.class);
        var points2D = IntStream.range(0, points.getX().size())
                .mapToObj(i -> {
                    var x = points.getX().get(i);
                    var y = points.getY().get(i);
                    return new Point2D(x, y);
                })
                .collect(Collectors.toList());
        return PolygonView.fromImageCoords(points2D, paneController.getAutoscale())
                .map(dataView -> new Localization<>(dataView, paneController, association.getUuid(), concept));

    }

    @Override
    public Association fromLocalization(Localization<PolygonView, ? extends Node> localization,
                                        UUID imageReferenceUuid,
                                        String comment) {
        var polygon = localization.getDataView().getData();
        var xs = polygon.getPoints()
                .stream()
                .map(p -> toInt(p.getX()))
                .collect(Collectors.toList());
        var ys = polygon.getPoints()
                .stream()
                .map(p -> toInt(p.getY()))
                .collect(Collectors.toList());
        var points = new Points(xs, ys, imageReferenceUuid, comment);
        var linkValue = Json.GSON.toJson(points);
        return new Association(LINK_NAME, Association.VALUE_SELF, linkValue, MEDIA_TYPE, localization.getUuid());
    }
}
