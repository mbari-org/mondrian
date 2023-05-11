package org.mbari.mondrian.javafx.roi;

import org.mbari.imgfx.roi.LineData;

public record DatumLine(double startX, double startY, double endX, double endY)
        implements Datum<LineData> {

    public static DatumLine from(LineData data) {
        return new DatumLine(data.getStartX(), data.getStartY(), data.getEndX(), data.getEndY());
    }

    @Override
    public void update(LineData data) {
        data.setStartX(startX);
        data.setStartY(startY);
        data.setEndX(endX);
        data.setEndY(endY);
    }
}
