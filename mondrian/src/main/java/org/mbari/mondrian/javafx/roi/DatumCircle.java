package org.mbari.mondrian.javafx.roi;

import org.mbari.imgfx.roi.CircleData;
import org.mbari.vars.services.model.Association;

import java.util.Optional;

public record DatumCircle(double centerX, double centerY, double radius) implements Datum<CircleData>{

    public static DatumCircle from(CircleData data) {
        return new DatumCircle(data.getCenterX(), data.getCenterY(), data.getRadius());
    }

    @Override
    public void update(CircleData data) {
        data.setCenterX(centerX);
        data.setCenterY(centerY);
        data.setRadius(radius);
    }
}
