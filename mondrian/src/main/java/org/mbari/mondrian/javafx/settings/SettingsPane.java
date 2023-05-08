package org.mbari.mondrian.javafx.settings;

import javafx.scene.layout.Pane;
import org.mbari.mondrian.util.IPrefs;

/**
 * @author Brian Schlining
 * @since 2017-12-28T13:06:00
 */
public interface SettingsPane extends IPrefs {

    String getName();

    Pane getPane();
}

