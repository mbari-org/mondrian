package org.mbari.mondrian.javafx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import org.controlsfx.control.PopOver;
import org.mbari.imgfx.roi.Data;
import org.mbari.imgfx.roi.DataView;
import org.mbari.imgfx.roi.Localization;
import org.mbari.mondrian.AppController;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.domain.Selection;
import org.mbari.mondrian.domain.VarsLocalization;
import org.mbari.mondrian.etc.jdk.Logging;
import org.mbari.mondrian.javafx.controls.PageLabelController;
import org.mbari.mondrian.javafx.decorators.FilteredComboBoxDecorator;
import org.mbari.mondrian.javafx.dialogs.CameraDeploymentDialogController;
import org.mbari.mondrian.javafx.dialogs.ConceptDialogController;
import org.mbari.mondrian.javafx.settings.SettingsDialogController;
import org.mbari.mondrian.msg.events.AddLocalizationEvents;
import org.mbari.mondrian.msg.messages.*;
import org.mbari.mondrian.util.SupportUtil;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

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
    private ComboBox<String> usersComboBox;
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

        rx.ofType(SetAnnotationsForSelectedImageMsg.class)
                        .subscribe(msg -> setAnnotationsForSelectedImage(msg.annotations()));

        getRoot().getStylesheets().addAll(toolBox.stylesheets());
    }

    public void setAnnotationsForSelectedImage(Collection<Annotation> annotations) {
        var paneController = getAnnotationPaneController();
        var varsLocalizations = VarsLocalization.from(annotations,
                paneController.getAutoscalePaneController(),
                paneController.getAnnotationColors().editedColorProperty());
//        toolBox.data().getVarsLocalizations().setAll(varsLocalizations);
        // Map localization to correct EventMessage. Set is new to false.send it
        varsLocalizations.forEach(vloc -> {
            SupportUtil.publishVarsLocalization(vloc, false, toolBox.eventBus(), AppPaneController.this);
//            AddLocalizationEvents.from(vloc.getLocalization(), false)
//                    .ifPresent(evt -> {
//                        toolBox.eventBus().publish(evt);
//                        toolBox.eventBus().publish(new AddVarsLocalizationMsg(new Selection<>(AppPaneController.this, vloc)));
//                    });
        });
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

            Text undoIcon = Icons.UNDO.standardSize();
            Button undoButton = new Button();
            undoButton.setGraphic(undoIcon);
            undoButton.setOnAction(e -> toolBox.eventBus().publish(new UndoMsg()));
            undoButton.setTooltip(new Tooltip(toolBox.i18n().getString("app.toolbar.button.undo")));

            Text redoIcon = Icons.REDO.standardSize();
            Button redoButton = new Button();
            redoButton.setGraphic(redoIcon);
            redoButton.setOnAction(e -> toolBox.eventBus().publish(new RedoMsg()));
            redoButton.setTooltip(new Tooltip(toolBox.i18n().getString("app.toolbar.button.redo")));

            Text settingsIcon = Icons.SETTINGS.standardSize();
            Button settingsButton = new Button();
            settingsButton.setGraphic(settingsIcon);
            settingsButton.setTooltip(new Tooltip(toolBox.i18n().getString("app.toolbar.button.settings")));
            settingsButton.setOnAction(e -> getSettingsDialogController().show());

            var pageLabelController = new PageLabelController(toolBox.eventBus());

            toolBar = new ToolBar(openButton, undoButton, redoButton, settingsButton, getUsersComboBox(), pageLabelController.getLabel());
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

    public ComboBox<String> getUsersComboBox() {
        if (usersComboBox == null) {

            usersComboBox = new ComboBox<>();
            new FilteredComboBoxDecorator<>(usersComboBox, FilteredComboBoxDecorator.CONTAINS_CHARS_IN_ORDER);
            var sorter = Comparator.comparing(String::toString, String.CASE_INSENSITIVE_ORDER);

            // Listen to UserAddedEvent and add it to the combobox
            var eventBus = toolBox.eventBus();
//            eventBus.toObserverable()
//                    .ofType(UserAddedEvent.class)
//                    .subscribe(event -> {
//                        Platform.runLater(() -> {
//                            User user = event.get();
//                            // M3-53 fix. Copy to editable arraylist first
//                            List<String> newItems = new ArrayList<>(usersComboBox.getItems());
//                            newItems.add(user.getUsername());
//                            Collections.sort(newItems, sorter);
//                            ObservableList<String> items = FXCollections.observableList(newItems);
//                            usersComboBox.setItems(items);
//                            usersComboBox.getSelectionModel().select(user.getUsername());
//                        });
//                    });

            // When a username is selected send a change event
            usersComboBox.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((obs, oldv, newv) -> {
                        if (newv != null) {
                            toolBox.servicesProperty()
                                    .get()
                                    .usersService()
                                    .findAllUsers()
                                    .thenAccept(users -> {
                                        var opt = users.stream()
                                                .filter(u -> u.getUsername().equals(newv))
                                                .findFirst();
                                        opt.ifPresent(user -> eventBus.publish(new SetUserMsg(user)));
                                    });
                        }
                    });

            loadUsers();

            // Listen for new services event and update users after service is changed.
            eventBus.toObserverable()
                    .ofType(ReloadMsg.class)
                    .subscribe(evt -> loadUsers());

        }
        return usersComboBox;
    }

    /**
     * Populate the user combobox and select the user from the OS
     */
    private void loadUsers() {
        var sorter = Comparator.comparing(String::toString, String.CASE_INSENSITIVE_ORDER);
        usersComboBox.setItems(FXCollections.observableList(new ArrayList<>()));
        toolBox.servicesProperty()
                .get()
                .usersService()
                .findAllUsers()
                .thenAccept(users -> {
                    var usernames = users.stream()
                            .map(User::getUsername)
                            .sorted(sorter)
                            .toList();
                    Platform.runLater(() -> {
                        usersComboBox.setItems(FXCollections.observableList(usernames));
                        String defaultUser = System.getProperty("user.name");
                        if (usernames.contains(defaultUser)) {
                            usersComboBox.getSelectionModel().select(defaultUser);
                        }
                    });
                });
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
