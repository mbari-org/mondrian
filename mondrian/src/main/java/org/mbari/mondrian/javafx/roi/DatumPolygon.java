package org.mbari.mondrian.javafx.roi;

import javafx.geometry.Point2D;
import org.mbari.imgfx.roi.PolygonData;

import java.util.List;

public record DatumPolygon(List<Point2D> points)
        implements Datum<PolygonData> {

    public static DatumPolygon from(PolygonData data) {
        var xs = List.copyOf(data.getPoints());
        return new DatumPolygon(xs);
    }

    @Override
    public void update(PolygonData data) {
        data.getPoints().setAll(points);
    }
}
