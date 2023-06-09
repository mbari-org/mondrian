package org.mbari.mondrian.javafx;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Shape;
import org.mbari.imgfx.Autoscale;
import org.mbari.imgfx.roi.Data;
import org.mbari.imgfx.roi.DataView;
import org.mbari.imgfx.roi.Localization;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.domain.Selection;
import org.mbari.mondrian.domain.VarsLocalization;
import org.mbari.mondrian.etc.jdk.Logging;
import org.mbari.mondrian.javafx.roweditor.RowEditorController;
import org.mbari.mondrian.msg.messages.*;
import org.mbari.mondrian.util.IPrefs;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Image;

import java.util.*;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

public class DataPaneController implements IPrefs {

    private ImageListViewController imageListViewController;
    private ImageViewZoomController imageViewZoomController;
    private AnnotationListViewController annotationListViewController;
    private RowEditorController rowEditorController;
    private final ToolBox toolBox;
    private final Autoscale<ImageView> originalAutoscale;
    private static final String KEY_ZOOM = "zoom";
    private Logging log = new Logging(getClass());

    private VBox vBox;
    private SplitPane splitPane;
    private Slider slider;

    public DataPaneController(ToolBox toolBox, Autoscale<ImageView> originalAutoscale) {
        this.toolBox = toolBox;
        this.originalAutoscale = originalAutoscale;
        init();
    }

    private void init() {

        var rx = toolBox.eventBus().toObserverable();

        // Editor for a row
        rowEditorController = new RowEditorController();
        rowEditorController.getRoot().setMinHeight(200);

        // List view of images
        imageListViewController = new ImageListViewController(toolBox.i18n(), toolBox.eventBus());
        Consumer<Selection<Image>> onImageSelected = selection -> {
            if (selection.source() == imageListViewController) {
                var msg = new SetSelectedImageMsg(selection);
                toolBox.eventBus().publish(msg);
            }
        };
        imageListViewController.setOnImageSelection(onImageSelected);

        rx.ofType(AddVarsLocalizationMsg.class)
                .subscribe(msg -> annotationListViewController.addAnnotation(msg.varsLocalization().getAnnotation()), this::logError);

        rx.ofType(RemoveVarsLocalizationMsg.class)
                .subscribe(msg -> annotationListViewController.removeAnnotation(msg.varsLocalization().getAnnotation()), this::logError);

        rx.ofType(UpdateAnnotationInViewMsg.class)
                .subscribe(msg -> {
                    var items = annotationListViewController.getItems();
                    var idx = items.indexOf(msg.annotation());
                    if (idx > -1) {
                        items.set(idx, msg.annotation());
                    }
                }, this::logError);

        rx.ofType(SetImagesMsg.class)
                .subscribe(msg -> {
                    imageListViewController.setSelectedImage(null);
                    imageListViewController.setImages(msg.images());

                }, this::logError);
        rx.ofType(SetSelectedImageMsg.class)
                .filter(msg -> msg.selection().source() != imageListViewController)
                .subscribe(msg ->  imageListViewController.setSelectedImage(msg.image()), this::logError);

        // List view of annotations for the selected image
        annotationListViewController = new AnnotationListViewController();
        Consumer<Selection<Collection<Annotation>>> onAnnotationsSelected = selection -> {
            var msg = new SetSelectedAnnotationsMsg(selection);
            toolBox.eventBus().publish(msg);
        };
        annotationListViewController.setOnAnnotationSelection(onAnnotationsSelected);
        rx.ofType(SetAnnotationsForSelectedImageMsg.class)
                .filter(msg -> msg.selection().source() != annotationListViewController)
                .subscribe(msg -> annotationListViewController.setAnnotations(List.of()), this::logError);

        rx.ofType(RerenderAnnotationsMsg.class)
                .subscribe(msg -> {
                    var selected = VarsLocalization.localizationIntersection(toolBox.data().getVarsLocalizations(), toolBox.localizations().getSelectedLocalizations());
                    if (selected.size() != 1) {
                        Platform.runLater(() -> annotationListViewController.getListView().refresh());
                    }
                    else {
                        var head = selected.stream().findFirst().map(VarsLocalization::getAnnotation).orElse(null);
                        Platform.runLater(() -> {
                            annotationListViewController.getListView().refresh();
                            rowEditorController.setAnnotation(head);
                        });

                    }

                }, this::logError);

        rx.ofType(SetSelectedAnnotationsMsg.class)
                .subscribe(msg -> {
                    var selection = msg.selection();
                    if (selection.source() != annotationListViewController) {
                        Platform.runLater(() -> annotationListViewController.setSelectedAnnotations(msg.annotations()));
                    }
                    setSelectedAnnotations(selection.selected());
                }, this::logError);

        toolBox.localizations()
                .getSelectedLocalizations()
                .addListener((ListChangeListener<Localization<? extends DataView<? extends Data, ? extends Shape>, ? extends Node>>) c -> {
                    var selected = VarsLocalization.localizationIntersection(toolBox.data().getVarsLocalizations(), toolBox.localizations().getSelectedLocalizations());
                    var selectedInListView = annotationListViewController.getListView()
                            .getSelectionModel()
                            .getSelectedItems();

                    var selectedAnnos0 = selected.stream()
                            .map(VarsLocalization::getAnnotation)
                            .sorted(Comparator.comparing(Annotation::getObservationUuid))
                            .toList();
                    if (selected.size() != selectedInListView.size()) {
                        annotationListViewController.setSelectedAnnotations(selectedAnnos0);
                        setSelectedAnnotations(selectedAnnos0);
                    }
                    else {
                        var selectedAnnos1 = annotationListViewController.getListView()
                                .getSelectionModel()
                                .getSelectedItems()
                                .stream()
                                .sorted(Comparator.comparing(Annotation::getObservationUuid))
                                .toList();
                        if (!selectedAnnos0.equals(selectedAnnos1)) {
                            annotationListViewController.setSelectedAnnotations(selectedAnnos0);
                            setSelectedAnnotations(selectedAnnos1);
                        }
                    }

                });

        // Zoom image window
        slider = new Slider(2.0, 20.0, 8.0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(2);
        imageViewZoomController = new ImageViewZoomController(originalAutoscale);
        imageViewZoomController.zoomProperty().bind(slider.valueProperty());
        var imageView = imageViewZoomController.getImageView();
        imageView.setFitWidth(400);
        imageView.setFitHeight(400);
        rx.ofType(SetSelectedImageMsg.class)
                .subscribe(msg -> {
                    var image = msg.image() == null ? null : new javafx.scene.image.Image(msg.image().getUrl().toString(), true);
                    imageViewZoomController.getImageView().setImage(image);
                });

        splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.getItems()
                .addAll(imageListViewController.getPane(), annotationListViewController.getListView());
        splitPane.setPrefHeight(2000);

        vBox = new VBox(imageViewZoomController.getImageView(), slider, splitPane, rowEditorController.getRoot());
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(15));


        rx.ofType(PrepareForShutdownMsg.class)
                .subscribe(msg -> save());

        load();

    }

    private void logError(Throwable ex) {
        log.atWarn().withCause(ex).log("An error occurred in an RX subscriber");
    }

    public VBox getPane() {
        return vBox;
    }

    @Override
    public void load() {
        var prefs = Preferences.userRoot();
        var node = prefs.node(getClass().getName());
        var defaultZoom = node.getDouble(KEY_ZOOM, slider.getValue());
        slider.setValue(defaultZoom);
    }

    @Override
    public void save() {
        var prefs = Preferences.userRoot();
        var node = prefs.node(getClass().getName());
        node.putDouble(KEY_ZOOM, slider.getValue());
    }

    private void setSelectedAnnotations(Collection<Annotation> selectedAnnotations) {
        Platform.runLater(() -> {
            if (selectedAnnotations.size() != 1) {
                rowEditorController.setAnnotation(null);
            }
            else {
                selectedAnnotations.stream()
                        .findFirst()
                        .ifPresent(rowEditorController::setAnnotation);
            }
        });
    }

//    private void reloadSelectedAnnotation() {
//        var anno = rowEditorController.
//    }
}
