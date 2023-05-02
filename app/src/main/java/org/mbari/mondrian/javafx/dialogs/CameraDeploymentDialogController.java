package org.mbari.mondrian.javafx.dialogs;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.BorderPane;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.javafx.decorators.FilteredComboBoxDecorator;
import org.mbari.mondrian.msg.messages.ReloadMsg;

import java.util.Optional;

public class CameraDeploymentDialogController {

    private ComboBox<String> comboBox = new ComboBox<>();
    private Dialog<String> dialog;
    private final String KEY_TITLE = "dialogs.camera.deployment.title";
    private final String KEY_HEADER = "dialogs.camera.deployment.header";
    private final ToolBox toolBox;

    public CameraDeploymentDialogController(ToolBox toolBox) {
        this.toolBox = toolBox;
        init();
    }

    private void init() {
        toolBox.eventBus()
                .toObserverable()
                .ofType(ReloadMsg.class)
                .subscribe(msg -> {
                    dialog = null;
                });
    }

    public Optional<String> focus() {
        var dialog = getDialog();
        Platform.runLater(() -> comboBox.requestFocus());
        return dialog.showAndWait();
    }

    public Dialog<String> getDialog() {
        if (dialog == null) {
            dialog = new Dialog<>();
            dialog.setTitle(toolBox.i18n().getString(KEY_TITLE));
            dialog.setHeaderText(toolBox.i18n().getString(KEY_HEADER));
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            var borderPane = new BorderPane(comboBox);
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
                    return comboBox.getSelectionModel().getSelectedItem();
                }
                return null;
            });

            var cameraDeployments = toolBox.servicesProperty()
                    .get()
                    .mediaService()
                    .findAllCameraDeployments()
                    .join();

            Platform.runLater(() -> {
                var observableList = FXCollections.observableList(cameraDeployments);
                comboBox.setItems(observableList);
                new FilteredComboBoxDecorator<>(comboBox, FilteredComboBoxDecorator.CONTAINS_CHARS_IN_ORDER);
            });

            dialog.getDialogPane()
                    .getStylesheets()
                    .addAll(toolBox.stylesheets());
        }

        // Reload deployments

//        comboBox.getItems().setAll(cameraDeployments);



        return dialog;
    }
}
