package org.mbari.mondrian.javafx.roi;

import org.mbari.imgfx.roi.*;

import java.util.Optional;

public class Datums {

    /**
     * Given a Data object return a datum of it's current state
     * @param data The data object from a localization
     * @return A Datum for the localization. Empty if no matching
     *  Datum type is found.
     */
    public static Optional<Datum<?>> from(Data data) {
        if (data instanceof CircleData d ) {
            return Optional.of(DatumCircle.from(d));
        }
        else if (data instanceof LineData d) {
            return Optional.of(DatumLine.from(d));
        }
        else if (data instanceof PolygonData d) {
            return Optional.of(DatumPolygon.from(d));
        }
        else if (data instanceof RectangleData d) {
            return Optional.of(DatumRectangle.from(d));
        }
        return Optional.empty();
    }

    /**
     * Update the data object with the data stored in a datum. (i.e
     * restore a previous state from a datum)
     * @param data The localization's data object
     * @param datum The datum containing a previous snapshot
     * @return true if the data was updated, false if it was not.
     */
    public static boolean update(Data data, Datum<?> datum) {
        if (data instanceof CircleData d && datum instanceof DatumCircle e) {
            e.update(d);
            return true;
        }
        else if (data instanceof LineData d  && datum instanceof DatumLine e) {
            e.update(d);
            return true;
        }
        else if (data instanceof PolygonData d && datum instanceof DatumPolygon e) {
            e.update(d);
            return true;
        }
        else if (data instanceof RectangleData d && datum instanceof DatumRectangle e) {
            e.update(d);
            return true;
        }
        return false;
    }

}
