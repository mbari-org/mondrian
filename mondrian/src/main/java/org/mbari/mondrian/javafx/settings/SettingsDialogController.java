package org.mbari.mondrian.javafx.settings;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.text.Text;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.javafx.Icons;

import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-08-09T12:50:00
 */
public class SettingsDialogController {

    private final SettingsPaneController paneController;
    private final ToolBox toolBox;

    public SettingsDialogController(ToolBox toolBox) {
        this.toolBox = toolBox;
        paneController = new SettingsPaneController(toolBox);
    }

    public void show() {
        paneController.load();
        ResourceBundle i18n = toolBox.i18n();
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(i18n.getString("dialogs.settings.title"));
        dialog.setHeaderText(i18n.getString("dialogs.settings.header"));
//        Text settingsIcon = gf.createIcon(MaterialIcon.SETTINGS, "30px");
        Text settingsIcon = Icons.SETTINGS.standardSize();
        dialog.setGraphic(settingsIcon);
        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane()
                .setContent(paneController.getRoot());
        dialog.getDialogPane()
                .getStylesheets()
                .addAll(toolBox.stylesheets());
        Optional<ButtonType> buttonType = dialog.showAndWait();
        buttonType.ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                paneController.save();
            }
        });
    }
}
