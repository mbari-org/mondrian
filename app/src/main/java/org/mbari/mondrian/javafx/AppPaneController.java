package org.mbari.mondrian.javafx;

import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.etc.jdk.Logging;
import org.mbari.mondrian.javafx.dialogs.CameraDeploymentDialogController;
import org.mbari.mondrian.msg.messages.SetAnnotationsForSelectedImageMsg;
import org.mbari.mondrian.msg.messages.SetImagesMsg;
import org.mbari.mondrian.msg.messages.SetSelectedImageMsg;

import java.util.Comparator;

public class AppPaneController {

    private AnnotationPaneController annotationPaneController;
    private final ToolBox toolBox;
    private ToolBar toolBar;
    private CameraDeploymentDialogController cameraDeploymentDialog;
    private DataSelectionPaneController dataSelectionPaneController;
    private final Logging log = new Logging(getClass());

    public AppPaneController(ToolBox toolBox) {
        this.toolBox = toolBox;
        init();
    }

    private void init() {

        toolBox.data()
                .selectedImageProperty()
                .addListener((obs, oldv, newv) -> {
                    var image = newv == null ? null : new Image(newv.getUrl().toExternalForm());
                    getAnnotationPaneController().resetUsingImage(image);
                });

        var rx = toolBox.eventBus().toObserverable();
//        rx.ofType(SetSelectedImageMsg.class)
//                .subscribe(event -> getDataSelectionPaneController())

        getRoot().getStylesheets().addAll(toolBox.stylesheets());
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
            openButton.setOnAction(evt -> {
                var dialog = getCameraDeploymentDialog();
                dialog.focus().ifPresent(s -> {
                    // TODO Hack we've hardwired the page
                    System.out.println("HERE I am");
                    toolBox.servicesProperty()
                            .get()
                            .imageService()
                            .findByVideoSequenceName(s, 10000, 0)
                            .handle((page, ex) -> {
                                if (ex != null) {
                                    log.atWarn().withCause(ex).log("Failed to get images");
                                }
                                else {
                                    var images = page.content()
                                                    .stream()
                                                    .sorted(Comparator.comparing(org.mbari.vars.services.model.Image::getRecordedTimestamp))
                                                    .distinct()
                                                    .toList();
                                    toolBox.eventBus().publish(new SetImagesMsg(images));
                                }
                                return null;
                            });

                });
            });
            toolBar = new ToolBar(openButton);
        }
        return toolBar;
    }

    private CameraDeploymentDialogController getCameraDeploymentDialog() {
        if (cameraDeploymentDialog == null) {
            cameraDeploymentDialog = new CameraDeploymentDialogController(toolBox);
        }
        return cameraDeploymentDialog;
    }

    private DataSelectionPaneController getDataSelectionPaneController() {
        if (dataSelectionPaneController == null) {
            dataSelectionPaneController = DataSelectionPaneController.newInstance(toolBox,
                    annotationPaneController.getAutoscalePaneController().getAutoscale());
        }
        return dataSelectionPaneController;
    }


    private Pane getRightPane() {
        return getDataSelectionPaneController().getRoot();
    }
}
