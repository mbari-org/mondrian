package org.mbari.mondrian.javafx.dialogs;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.BorderPane;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.domain.ConceptSelection;
import org.mbari.mondrian.javafx.decorators.FilteredComboBoxDecorator;
import org.mbari.mondrian.msg.messages.ReloadMsg;

import java.util.Optional;

public class ConceptDialogController {
    private Dialog<ConceptSelection> dialog;
    private ComboBox<String> comboBox;
    private final String KEY_TITLE = "dialogs.concept.title";
    private final String KEY_HEADER = "dialogs.concept.header";
    private final String KEY_CHECKBOX = "dialogs.concept.checkbox";
    private final ToolBox toolBox;

    public ConceptDialogController(ToolBox toolBox) {
        this.toolBox = toolBox;
        init();
    }

    private void init() {
        toolBox.eventBus()
                .toObserverable()
                .ofType(ReloadMsg.class)
                .subscribe(msg -> {
                    dialog = null;
                    comboBox = null;
                });
    }

    public Optional<ConceptSelection> focus() {
        var dialog = getDialog();
        Platform.runLater(() -> comboBox.requestFocus());
        return dialog.showAndWait();
    }

    public Dialog<ConceptSelection> getDialog() {
        if (dialog == null) {
            comboBox = new ComboBox<String>();
            dialog = new Dialog<>();
            dialog.setTitle(toolBox.i18n().getString(KEY_TITLE));
            dialog.setHeaderText(toolBox.i18n().getString(KEY_HEADER));
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            var text = toolBox.i18n().getString(KEY_CHECKBOX);
            var checkBox = new CheckBox(text);
            var borderPane = new BorderPane(comboBox);
            borderPane.setBottom(checkBox);
            dialog.getDialogPane().setContent(borderPane);
            comboBox.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((obs, oldv, newv) -> {
                        var disable = newv == null || newv.trim().isEmpty();
                        var button = dialog.getDialogPane().lookupButton(ButtonType.OK);
                        button.setDisable(disable);
                    });
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    var concept = comboBox.getSelectionModel().getSelectedItem();
                    var includeDescendants = checkBox.isSelected();
                    return new ConceptSelection(concept, includeDescendants);
                }
                return null;
            });

            // TODO Hack using fixed page size to fetch all concepts. Won't work with WoRMS
            var page = toolBox.servicesProperty()
                    .get()
                    .namesService()
                    .listNames(10000, 0)
                    .join();

            var concepts = page.content();

            Platform.runLater(() -> {
                var observableList = FXCollections.observableList(concepts);
                comboBox.setItems(observableList);
                new FilteredComboBoxDecorator<>(comboBox, FilteredComboBoxDecorator.CONTAINS_CHARS_IN_ORDER);
            });

            dialog.getDialogPane()
                    .getStylesheets()
                    .addAll(toolBox.stylesheets());
        }
        return dialog;
    }
}
