package org.mbari.mondrian.javafx;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.controlsfx.control.PopOver;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.etc.jdk.Logging;
import org.mbari.mondrian.javafx.dialogs.CameraDeploymentDialogController;
import org.mbari.mondrian.javafx.dialogs.ConceptDialogController;
import org.mbari.mondrian.javafx.settings.SettingsDialogController;
import org.mbari.mondrian.msg.messages.*;

public class AppPaneController {

    private AnnotationPaneController annotationPaneController;
    private final ToolBox toolBox;
    private ToolBar toolBar;
    private CameraDeploymentDialogController cameraDeploymentDialog;
    private ConceptDialogController conceptDialogController;
    private SettingsDialogController settingsDialogController;
//    private DataSelectionPaneController dataSelectionPaneController;
    private DataPaneController dataPaneController;
    private PopOver openPopOver;
    private final Logging log = new Logging(getClass());

    public AppPaneController(ToolBox toolBox) {
        this.toolBox = toolBox;
        init();
    }

    private void init() {


        var rx = toolBox.eventBus().toObserverable();

        rx.ofType(SetSelectedImageMsg.class)
                .subscribe(msg -> {
                    var image = msg.image() == null ? null : new Image(msg.image().getUrl().toExternalForm());
                    getAnnotationPaneController().resetUsingImage(image);
                });

        getRoot().getStylesheets().addAll(toolBox.stylesheets());
    }

    public void setImage(org.mbari.vars.services.model.Image image) {

    }

    public AnnotationPaneController getAnnotationPaneController() {
        if (annotationPaneController == null) {
            annotationPaneController = new AnnotationPaneController(toolBox);
        }
        return annotationPaneController;
    }

    public BorderPane getRoot() {
        var rootBorderPane = getAnnotationPaneController().getPane();
        rootBorderPane.setRight(getRightPane());
        rootBorderPane.setTop(getToolBar());
        return rootBorderPane;
    }

    public ToolBar getToolBar() {
        if (toolBar == null) {
            Text openIcon = Icons.VIDEO_LIBRARY.standardSize();
            Button openButton = new Button();
            openButton.setGraphic(openIcon);
            openButton.setOnAction(e -> getOpenPopOver().show(openButton));
            openButton.setTooltip(new Tooltip(toolBox.i18n().getString("app.toolbar.button.open")));

            Text settingsIcon = Icons.SETTINGS.standardSize();
            Button settingsButton = new Button();
            settingsButton.setGraphic(settingsIcon);
            settingsButton.setTooltip(new Tooltip(toolBox.i18n().getString("app.toolbar.button.settings")));
            settingsButton.setOnAction(e -> getSettingsDialogController().show());

            toolBar = new ToolBar(openButton, settingsButton);
        }
        return toolBar;
    }

    public PopOver getOpenPopOver() {
        if (openPopOver == null) {
            var i18n = toolBox.i18n();

            Text cameraDeploymentIcon = Icons.VIDEO_LIBRARY.standardSize();
            Button cameraDeploymentButton = new Button(null, cameraDeploymentIcon);
            cameraDeploymentButton.setTooltip(new Tooltip(i18n.getString("app.button.open.deployment")));
            cameraDeploymentButton.setOnAction(evt -> {
                var dialog = getCameraDeploymentDialog();
                dialog.focus().ifPresent(s -> {
                    var pageSize = toolBox.data().getPageSize();
                    var msg = new OpenImagesByCameraDeploymentMsg(AppPaneController.this, s, pageSize, 0);
                    toolBox.eventBus().publish(msg);
                });
            });

            Text conceptIcon = Icons.BUG_REPORT.standardSize();
            Button conceptButton = new Button(null, conceptIcon);
            conceptButton.setTooltip(new Tooltip(i18n.getString("app.button.open.concept")));
            conceptButton.setOnAction(evt -> {
                var dialog = getConceptDialogController();
                dialog.focus().ifPresent(s -> {
                    var pageSize = toolBox.data().getPageSize();
                    var msg = new OpenImagesByConceptMsg(AppPaneController.this, s.concept(), s.includeDescendants(), pageSize, 0);
                    toolBox.eventBus().publish(msg);
                });
            });

            // TODO add open by concept button
            var vbox = new VBox(cameraDeploymentButton, conceptButton);
            openPopOver = new PopOver(vbox);
        }
        return openPopOver;
    }

    private CameraDeploymentDialogController getCameraDeploymentDialog() {
        if (cameraDeploymentDialog == null) {
            cameraDeploymentDialog = new CameraDeploymentDialogController(toolBox);
        }
        return cameraDeploymentDialog;
    }

    public ConceptDialogController getConceptDialogController() {
        if (conceptDialogController == null) {
            conceptDialogController = new ConceptDialogController(toolBox);
        }
        return conceptDialogController;

    }

    public DataPaneController getDataPaneController() {
        if (dataPaneController == null) {
            dataPaneController = new DataPaneController(toolBox,getAnnotationPaneController().getAutoscalePaneController().getAutoscale());
        }
        return dataPaneController;
    }

    public SettingsDialogController getSettingsDialogController() {
        if (settingsDialogController == null) {
            settingsDialogController = new SettingsDialogController(toolBox);
        }
        return settingsDialogController;
    }

    private Pane getRightPane() {
        return getDataPaneController().getPane();
    }
}
