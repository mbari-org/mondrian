package org.mbari.mondrian.javafx;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.mbari.imgfx.Autoscale;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.domain.Selection;
import org.mbari.mondrian.msg.messages.*;
import org.mbari.mondrian.util.IPrefs;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Image;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

public class DataPaneController implements IPrefs {

    private ImageListViewController imageListViewController;
    private ImageViewZoomController imageViewZoomController;
    private AnnotationListViewController annotationListViewController;
    private final ToolBox toolBox;
    private final Autoscale<ImageView> originalAutoscale;
    private static final String KEY_ZOOM = "zoom";

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

        // List view of images
        imageListViewController = new ImageListViewController(toolBox.i18n(), toolBox.eventBus());
        Consumer<Selection<Image>> onImageSelected = selection -> {
            if (selection.source() == imageListViewController) {
                var msg = new SetSelectedImageMsg(selection);
                toolBox.eventBus().publish(msg);
            }
        };
        imageListViewController.setOnImageSelection(onImageSelected);
        rx.ofType(SetImagesMsg.class)
                .subscribe(msg -> imageListViewController.setImages(msg.images()));
        rx.ofType(SetSelectedImageMsg.class)
                .filter(msg -> msg.selection().source() != imageListViewController)
                .subscribe(msg ->  imageListViewController.setSelectedImage(msg.image()));

        // List view of annotations for the selected image
        annotationListViewController = new AnnotationListViewController();
        Consumer<Selection<Collection<Annotation>>> onAnnotationsSelected = selection -> {
            if (selection.source() != annotationListViewController) {
                var msg = new SetSelectedAnnotationsMsg(selection);
                toolBox.eventBus().publish(msg);
            }
        };
        annotationListViewController.setOnAnnotationSelection(onAnnotationsSelected);
        rx.ofType(SetAnnotationsForSelectedImageMsg.class)
                .filter(msg -> msg.selection().source() != annotationListViewController)
                .subscribe(msg -> annotationListViewController.setAnnotations(msg.annotations()));

        rx.ofType(SetSelectedAnnotationsMsg.class)
                .filter(msg -> msg.selection().source() != annotationListViewController)
                .subscribe(msg -> annotationListViewController.setSelectedAnnotations(msg.annotations()));


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

        vBox = new VBox(imageViewZoomController.getImageView(), slider, splitPane);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(15));


        rx.ofType(PrepareForShutdownMsg.class)
                .subscribe(msg -> save());

        load();

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
}
