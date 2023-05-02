package org.mbari.mondrian.javafx;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import org.mbari.imgfx.etc.javafx.controls.FilteredComboBoxDecorator;
import org.mbari.imgfx.roi.Data;
import org.mbari.imgfx.roi.DataView;
import org.mbari.imgfx.roi.Localization;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.etc.jdk.Logging;
import org.mbari.mondrian.msg.messages.ReloadMsg;
import org.mbari.mondrian.msg.messages.SetSelectedConceptMsg;

public class ConceptPaneController {
    private HBox pane;
    private final ToolBox toolBox;
    private ComboBox<String> conceptComboBox;
    private final Logging log = new Logging(getClass());
    private static final String KEY_CONCEPT_DEFAULT = "data.concept.default";

    public ConceptPaneController(ToolBox toolBox) {
        this.toolBox = toolBox;
        init();
    }

    private void init() {
        pane = new HBox();
        pane.getStyleClass().add("concept-pane");
        conceptComboBox = new ComboBox<>();
        conceptComboBox.getStyleClass().add("concept-combo-box");
        new FilteredComboBoxDecorator<>(conceptComboBox,
                FilteredComboBoxDecorator.STARTSWITH_IGNORE_SPACES);
        conceptComboBox.setEditable(false);
        conceptComboBox.setOnKeyReleased(v -> {
            if (v.getCode() == KeyCode.ENTER) {
                updateSelectedConcept();
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

        var rx = toolBox.eventBus().toObserverable();

        rx.ofType(ReloadMsg.class)
                        .subscribe(e -> loadConcepts());

        loadConcepts();
        log.atDebug().log("Initialized");

    }

    private void updateSelectedConcept() {
        var item = conceptComboBox.getValue();
        if (item != null) {
            toolBox.servicesProperty()
                    .get()
                    .namesService()
                    .findConcept(item)
                    .thenAccept(opt -> {
                        opt.ifPresent(concept -> {
                            var msg = new SetSelectedConceptMsg(concept.primaryName());
                            toolBox.eventBus().publish(msg);
                        });
                    });
        }
    }

    private void loadConcepts() {
        // TODO - This is a hack. Need to page for WoRMS
        toolBox.servicesProperty()
                .get()
                .namesService()
                .listNames(10000, 0)
                .thenAccept(page -> {
                    log.atInfo().log("Using " + page.content().size() + " concepts");
                    var observableList = FXCollections.observableList(page.content());
                    Platform.runLater(() -> conceptComboBox.setItems(observableList));
                });
    }



    public HBox getPane() {
        return pane;
    }

    public ComboBox<String> getConceptComboBox() {
        return conceptComboBox;
    }
}

