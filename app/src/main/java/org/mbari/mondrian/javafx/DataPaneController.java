package org.mbari.mondrian.javafx;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Image;

public class DataPaneController {

    private SplitPane splitPane;
    private ListView<Image> imageListView;
    private ListView<Annotation> annotationListView;
    private ComboBox<String> imageTypeComboBox;
}
