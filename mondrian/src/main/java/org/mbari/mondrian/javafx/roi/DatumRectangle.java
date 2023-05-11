package org.mbari.mondrian.javafx.roi;

import org.mbari.imgfx.roi.RectangleData;

public record DatumRectangle(double x, double y, double width, double height)
        implements Datum<RectangleData> {

    public static DatumRectangle from(RectangleData data) {
        return new DatumRectangle(data.getX(), data.getY(), data.getWidth(), data.getHeight());
    }

    @Override
    public void update(RectangleData data) {
        data.setX(x);
        data.setY(y);
        data.setWidth(width);
        data.setHeight(height);
    }
}
