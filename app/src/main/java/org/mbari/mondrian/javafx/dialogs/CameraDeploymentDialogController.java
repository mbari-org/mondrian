package org.mbari.mondrian.javafx.dialogs;

import io.github.palexdev.materialfx.controls.MFXFilterComboBox;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.BorderPane;
import org.mbari.mondrian.ToolBox;

public class CameraDeploymentDialogController {

    private MFXFilterComboBox<String> comboBox = new MFXFilterComboBox<>();
    private Dialog<String> dialog;
    private final String KEY_TITLE = "dialogs.camera.deployment.title";
    private final String KEY_HEADER = "dialogs.camera.deployment.header";

    public Dialog<String> getDialog(ToolBox toolBox) {
        if (dialog == null) {
            dialog = new Dialog<>();
            dialog.setTitle(toolBox.i18n().getString(KEY_TITLE));
            dialog.setHeaderText(toolBox.i18n().getString(KEY_HEADER));
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            var borderPane = new BorderPane(comboBox);
            dialog.getDialogPane().setContent(borderPane);
            comboBox.selectedTextProperty().addListener((obs, oldv, newv) -> {
                var disable = newv == null || newv.trim().isEmpty();
                var button = dialog.getDialogPane().lookupButton(ButtonType.OK);
                button.setDisable(disable);
            });
        }

        // Reload deployments
        comboBox.getItems().clear();
        var cameraDeployments = toolBox.servicesProperty()
                .get()
                .mediaService()
                .findAllCameraDeployments()
                .join();
        var observableList = FXCollections.observableList(cameraDeployments);
        comboBox.setItems(observableList);

        return dialog;
    }
}
