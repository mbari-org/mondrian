package org.mbari.mondrian.javafx;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.mbari.imgfx.Autoscale;
import org.mbari.imgfx.imageview.ImageViewAutoscale;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.msg.messages.SetAnnotationsForSelectedImageMsg;
import org.mbari.mondrian.msg.messages.SetImagesMsg;
import org.mbari.mondrian.msg.messages.SetSelectedAnnotationsMsg;
import org.mbari.mondrian.util.FXMLUtils;
import org.mbari.mondrian.util.IPrefs;
import org.mbari.mondrian.util.URLUtils;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

// Multiplie imageview on image: https://www.youtube.com/watch?v=bWQGIIzl0Vc
// Model server front end: https://adamant.tator.io:8082/
// https://stackoverflow.com/questions/44432343/javafx-scrollpane-in-splitpane-cannot-scroll
public class DataSelectionPaneController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private CheckBox addCommentCb;

    @FXML
    private CheckBox annoExistingCb;

    @FXML
    private ListView<Annotation> annoListView;

    @FXML
    private ComboBox<String> imageTypeComboBox;

    @FXML
    private ListView<Image> imageListView;

    @FXML
    private ImageView imageView;

    @FXML
    private Slider magnificationSlider;

    @FXML
    private Button mlButton;

    @FXML
    private TextField mlUrlTextField;

    @FXML
    private VBox root;

    private ToolBox toolBox;

    private String imageExt = "";
    private Autoscale<ImageView> copyAutoscale;
    private Autoscale<ImageView> originalAutoscale;

    private static final Logger log = LoggerFactory.getLogger(DataSelectionPaneController.class);

    @FXML
    void initialize() {
        imageView.setPreserveRatio(true);
        copyAutoscale = new ImageViewAutoscale(imageView);
        imageListView.setCellFactory(new Callback<ListView<Image>, ListCell<Image>>() {
            @Override
            public ListCell<Image> call(ListView<Image> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Image item, boolean empty) {
                        Platform.runLater(() -> {
                            super.updateItem(item, empty);
                            if (item == null || empty) {
                                setText("");
                                setTooltip(null);
                            }
                            else {
                                var s = URLUtils.filename(item.getUrl());
                                setText(s);
                                setTooltip(new Tooltip(s));
                            }
                        });
                    }
                };
            }
        });

//        annoListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        annoListView.setCellFactory(new Callback<ListView<Annotation>, ListCell<Annotation>>() {
            @Override
            public ListCell<Annotation> call(ListView<Annotation> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Annotation item, boolean empty) {
                        Platform.runLater(() -> {
                            super.updateItem(item, empty);
                            getStyleClass().remove("ifx-localized-annotation");
                            if (item == null || empty) {
                                setText("");
                                setTooltip(null);
                            }
                            else {
                                var s = item.getConcept();
                                setText(s);
                                setTooltip(new Tooltip(s));
                            }
                        });
                    }
                };
            }
        });
    }

    private void postInitialize() {

        var allImages = toolBox.data().getImages();

        // Only show images of the desired type
        applyImageType();

        // When an image is selected in the list, show it in the editor pane
        imageListView.getSelectionModel()
                .getSelectedItems()
                .addListener((ListChangeListener<? super Image>) c -> {
                    var selected = imageListView.getSelectionModel().getSelectedItem();
                    toolBox.data().setSelectedImage(selected);
                });

        // When an annotation is selected. Set its selection in UIToolbox
        annoListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldv, newv) -> {
                    if (newv != null) {
                        var event = new SetSelectedAnnotationsMsg(DataSelectionPaneController.this,
                                List.of(newv));
                        toolBox.eventBus().publish(event);
                    }
                });

        // Populate the filter combobox
        allImages.addListener((ListChangeListener<? super Image>) c -> {
            var exts = allImages.stream()
                    .map(i -> URLUtils.extension(i.getUrl()))
                    .distinct()
                    .collect(Collectors.toList());
            Platform.runLater(() -> imageTypeComboBox.getItems().setAll(exts));
        });

        // When a filter is selected apply it
        imageTypeComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldv, newv) -> {
                    imageExt = newv;
                    applyImageType();
                });


        toolBox.data()
                .selectedImageProperty()
                .addListener((obs, oldv, newv) -> {
                    if (newv != null) {
                        setSelectedImage(newv);
                    }
                    else {
                        Platform.runLater(() -> annoListView.getItems().clear());
                    }
                });

        originalAutoscale.getView().addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
            var x = event.getSceneX();
            var y = event.getSceneY();
            var xy = new Point2D(x, y);
            var imageXy = originalAutoscale.sceneToUnscaled(xy);
//            var msg = String.format("scene=(%.1f,%.1f), original=(%.1f,%.1f), mag=(%.1f,%.1f)",
//                    x, y, imageXy.getX(), imageXy.getY(), magXy.getX(), magXy.getY());
//            log.atDebug().log("ZOOM: " + msg);
            var zoom = magnificationSlider.getValue();
            var portWidth = imageView.getImage().getWidth() / zoom;
            var portHeight = imageView.getImage().getHeight() / zoom;
            var magX = imageXy.getX() - portWidth / 2D;
            var magY = imageXy.getY() - portHeight / 2D;
            var viewPort = new Rectangle2D(magX, magY, portWidth, portHeight);
            imageView.setViewport(viewPort);
        });

        var rx = toolBox.eventBus().toObserverable();
        rx.ofType(SetSelectedAnnotationsMsg.class)
                .subscribe(event -> {
                    if (event.source() != DataSelectionPaneController.this) {
                        setSelectedAnnotations(event.annotations());
                    }
                });

        rx.ofType(SetImagesMsg.class)
                .subscribe(event -> {
                    var items = FXCollections.observableList(event.images().stream().toList());
                    imageListView.setItems(items);
//                    imageListView.getItems().setAll(event.images());
                });

        rx.ofType(SetAnnotationsForSelectedImageMsg.class)
                .subscribe(event ->
                    Platform.runLater(() -> annoListView.getItems().setAll(event.annotations()))
                );

    }

    private boolean showImageType(Image image) {
        var imageUrl = image.getUrl();
        if (imageExt == null || imageExt.isEmpty() || imageExt.isBlank()) {
            return true;
        }
        return imageUrl.toExternalForm().endsWith(imageExt);
    }

    private void applyImageType() {
        var filteredImages = toolBox.data().getImages()
                .filtered(this::showImageType);

        Platform.runLater(() -> imageListView.setItems(filteredImages));

    }

    private void setSelectedImage(Image image) {
        // Set image in magnified view
        var i = new javafx.scene.image.Image(image.getUrl().toExternalForm());
        imageView.setImage(i);
        if (image.getImageReferenceUuid() != null) {
            toolBox.servicesProperty()
                    .get()
                    .annotationService()
                    .findByImageUuid(image.getImageReferenceUuid())
                    .thenAccept(annotations -> toolBox.eventBus().publish(new SetAnnotationsForSelectedImageMsg(DataSelectionPaneController.this, annotations)));
        }
    }


    private void setSelectedAnnotations(Collection<Annotation> annotations) {

//        LookupUtil.getImagesForAnnotations(toolBox, annotations)
//                .stream()
//                .filter(this::showImageType)
//                .findFirst()
//                .ifPresent(image -> toolBox.getData().setSelectedImage(image));

        var selectedAnno = annotations.size() == 1 ?
                annotations.iterator().next() : null;

        Platform.runLater(() -> {
            annoListView.getSelectionModel()
                    .select(selectedAnno);
        });

    }

    public VBox getRoot() {
        return root;
    }



    public static DataSelectionPaneController newInstance(ToolBox toolBox, Autoscale<ImageView> autoscale) {
        ResourceBundle i18n = toolBox.i18n();
        var controller = FXMLUtils.newInstance(DataSelectionPaneController.class,
                "/fxml/DataSelectionPane.fxml",
                i18n);
        controller.toolBox = toolBox;
        controller.originalAutoscale = autoscale;
        var width = controller.imageView.getFitWidth();
        var height = controller.imageView.getFitHeight();
        controller.imageView.setFitWidth(width);
        controller.imageView.setFitHeight(height);
        controller.postInitialize();
        return controller;
    }

}


