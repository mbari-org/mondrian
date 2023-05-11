package org.mbari.mondrian.javafx.roi;

/**
 * An immutable capture of the coordinates of a localization. Datums
 * are used to undo/redo changes made to a localization
 * @param <T>
 */
public interface Datum<T> {

    /**
     * Update the data object in a localization with the data stored
     * in the datum object. This is useful for reverting a localization
     * to a previous state.
     * @param t The data object to update
     */
    void update(T t);
}
