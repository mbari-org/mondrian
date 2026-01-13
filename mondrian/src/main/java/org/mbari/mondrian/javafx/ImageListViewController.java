package org.mbari.mondrian.javafx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.mbari.imgfx.etc.rx.EventBus;
import org.mbari.mondrian.domain.Selection;
import org.mbari.mondrian.javafx.controls.PagerPaneController;
import org.mbari.mondrian.util.URLUtils;
import org.mbari.vars.annosaurus.sdk.r1.models.Image;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Displays a filterable and selectable list of VARS Images. General usage is:
 *
 * <pre>
 *     <code>
 *         // onImageSelection does work when an image is selected
 *         var controller = new ImageListViewController()
 *         Consumer&lt;Image&gt; onImageSelection = selection -> {
 *             if (selection.source() != controller) {
 *                 System.out.println(image.getUrl + "");
 *             }
 *         };
 *         var vbox = controller.getPane();
 *         // add vbox to scene
 *         controller.setImages(myImages);
 *     </code>
 * </pre>
 */
public class ImageListViewController {

    private final ResourceBundle i18n;
    private final EventBus eventBus;
    private ListView<Image> listView;
    private ComboBox<String> imageTypeComboBox;
    private Label label;
    private HBox hBox;
    private VBox vbox;
    private PagerPaneController pagerPaneController;
    Consumer<Selection<Image>> onImageSelection = imageSelection -> {};
    ObservableList<Image> images = FXCollections.observableArrayList();

    public ImageListViewController(ResourceBundle i18n, EventBus eventBus) {
        this.i18n = i18n;
        this.eventBus = eventBus;
        init();
    }

    public void setOnImageSelection(Consumer<Selection<Image>> onImageSelection) {
        if (onImageSelection == null) {
            onImageSelection = imageSelection -> {};
        }
        this.onImageSelection = onImageSelection;
    }

    public void setImages(Collection<Image> images) {
        Platform.runLater(() -> {
            this.images.setAll(images);
            applyImageType();
        });
    }

    public void setSelectedImage(Image image) {
        Platform.runLater(() -> {
            if (image != null && listView.getItems().contains(image)) {
                listView.getSelectionModel().select(image);
            }
            else {
                listView.getSelectionModel().clearSelection();
            }
        });
    }

    public VBox getPane() {
        return vbox;
    }

    private void init() {
        imageTypeComboBox = new ComboBox<>();
        label = new Label(i18n.getString("vip.image.filter"));
        hBox = new HBox(label, imageTypeComboBox);

        listView = new ListView<>();
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listView.setPrefHeight(2000);

        pagerPaneController = new PagerPaneController(eventBus);

        vbox = new VBox(hBox, listView, pagerPaneController.getPane());
        vbox.setAlignment(Pos.CENTER);

        listView.setCellFactory(new Callback<>() {
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
                            } else {
                                var s = URLUtils.filename(item.getUrl());
                                setText(s);
                                setTooltip(new Tooltip(s));
                            }
                        });
                    }
                };
            }
        });

        // When an image is selected in the list, show it in the editor pane
        listView.getSelectionModel()
                .getSelectedItems()
                .addListener((ListChangeListener<? super Image>) c -> {
                    var item = listView.getSelectionModel().getSelectedItem();
                    var selection = new Selection<>(ImageListViewController.this, item);
                    onImageSelection.accept(selection);
                });

        // When a filter is selected apply it
        imageTypeComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldv, newv) -> {
                    applyImageType();
                });

        // Populate the filter combobox
        images.addListener((ListChangeListener<? super Image>) c -> {
            var exts = images.stream()
                    .map(i -> URLUtils.extension(i.getUrl()))
                    .distinct()
                    .collect(Collectors.toList());
            var allExts = new ArrayList<>(exts);
            allExts.add("");
            Platform.runLater(() -> imageTypeComboBox.getItems().setAll(allExts));
        });

        applyImageType();
    }

    private void applyImageType() {
        var filteredImages = images.filtered(this::isImageTypeShown);
        Platform.runLater(() -> listView.setItems(filteredImages));
    }

    private boolean isImageTypeShown(Image image) {
        var imageUrl = image.getUrl();
        var imageExt = imageTypeComboBox.getSelectionModel().getSelectedItem();
        if (imageExt == null || imageExt.isEmpty() || imageExt.isBlank()) {
            return true;
        }
        return imageUrl.toExternalForm().endsWith(imageExt);
    }
}
