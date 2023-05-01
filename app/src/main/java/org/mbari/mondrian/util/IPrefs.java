package org.mbari.mondrian.util;

public interface IPrefs {

    /**
     * Loads the appropriate preferences data and sets the correct fields
     * in the UI
     */
    void load();

    /**
     * Saves the preferences
     */
    void save();
}
