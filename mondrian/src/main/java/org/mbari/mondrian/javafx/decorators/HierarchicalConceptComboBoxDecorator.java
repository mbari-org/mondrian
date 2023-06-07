package org.mbari.mondrian.javafx.decorators;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.etc.jdk.Logging;

import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-07-19T16:04:00
 */
public class HierarchicalConceptComboBoxDecorator {

    private final ComboBox<String> comboBox;
    private final ToolBox toolBox;
    private static final Logging log = new Logging(HierarchicalConceptComboBoxDecorator.class);

    public HierarchicalConceptComboBoxDecorator(ComboBox<String> comboBox, ToolBox toolBox) {
        this.comboBox = comboBox;
        this.toolBox = toolBox;
        comboBox.setItems(FXCollections.observableArrayList());
    }

    public void setConcept(String concept) {
        setConcept(concept, concept);
    }

    public void setConcept(String concept, String selectedConcept) {
//        log.debug("Setting concept to " + concept);
        Platform.runLater(() -> {
            if (concept != null) {
                toolBox.servicesProperty()
                        .get()
                        .namesService()
                        .findDescendants(concept)
                        .handle((names, ex) -> {

                            // Build list of concepts to display
                            ObservableList<String> items = FXCollections.observableArrayList();
                            if (ex != null) {
                                log.withCause(ex).atWarn().log("Failed to look up " + concept);
                                items.add(concept);
                            } else {
                                items.addAll(names);
                            }

                            Platform.runLater(() -> {
                                comboBox.setItems(items);
                                String selected = items.contains(selectedConcept) ? selectedConcept : concept;
                                comboBox.getSelectionModel().select(selected);
                            });
                            return null;
                        });
            }
        });
    }
}
