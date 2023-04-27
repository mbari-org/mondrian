package org.mbari.mondrian.javafx;


import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import org.mbari.imgfx.etc.javafx.controls.FilteredComboBoxDecorator;
import org.mbari.imgfx.etc.rx.events.AddLocalizationEvent;
import org.mbari.imgfx.etc.rx.events.UpdatedLocalizationsEvent;
import org.mbari.imgfx.roi.Data;
import org.mbari.imgfx.roi.DataView;
import org.mbari.imgfx.roi.Localization;
import org.mbari.mondrian.ToolBox;

import java.util.ArrayList;

public class ConceptPaneController {
    private HBox pane;
    private final ToolBox toolBox;
    private ComboBox<String> conceptComboBox;

    public ConceptPaneController(ToolBox toolBox) {
        this.toolBox = toolBox;
        init();
    }

    private void init() {
        pane = new HBox();
        pane.getStyleClass().add("concept-pane");
        conceptComboBox = new ComboBox<>(toolBox.data().getConcepts());
        conceptComboBox.getStyleClass().add("concept-combo-box");
        new FilteredComboBoxDecorator<>(conceptComboBox,
                FilteredComboBoxDecorator.STARTSWITH_IGNORE_SPACES);
        conceptComboBox.setEditable(false);
        conceptComboBox.setOnKeyReleased(v -> {
            if (v.getCode() == KeyCode.ENTER) {
                var item = conceptComboBox.getValue();
                var selected = new ArrayList<>(toolBox.localizations().getSelectedLocalizations());
                if (item != null && !selected.isEmpty()) {
                    selected.forEach(loc -> loc.setLabel(item));
                    toolBox.eventBus().publish(new UpdatedLocalizationsEvent(selected));
                }
            }
        });
        var tooltip = new Tooltip();
        tooltip.getStyleClass().add("tooltip-combobox");
        conceptComboBox.setTooltip(tooltip);

        pane.getChildren().add(conceptComboBox);


        toolBox.localizations()
                .getSelectedLocalizations()
                .addListener((ListChangeListener<? super Localization<? extends DataView<? extends Data,? extends Node>,? extends Node>>) c -> {
                    Platform.runLater(() -> {
                        conceptComboBox.requestFocus();
                        conceptComboBox.getEditor().selectAll();
                        conceptComboBox.getEditor().requestFocus();
                    });
                });


        toolBox.eventBus()
                .toObserverable()
                .ofType(AddLocalizationEvent.class)
                .subscribe(event -> {
                    var loc = event.localization();
                    var selectedConcept = conceptComboBox.getSelectionModel().getSelectedItem();
                    if (selectedConcept != null) {
                        loc.setLabel(selectedConcept);
                    }
                });

    }

    public HBox getPane() {
        return pane;
    }

    public ComboBox<String> getConceptComboBox() {
        return conceptComboBox;
    }
}

