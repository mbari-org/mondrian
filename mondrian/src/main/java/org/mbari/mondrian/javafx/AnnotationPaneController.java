package org.mbari.mondrian.javafx;


import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.etc.javafx.controls.CrossHairs;
import org.mbari.imgfx.etc.rx.EventBus;
import org.mbari.imgfx.etc.rx.events.AddLocalizationEvent;
import org.mbari.imgfx.etc.rx.events.AddMarkerEvent;
import org.mbari.imgfx.imageview.ImagePaneController;
import org.mbari.imgfx.roi.CircleData;
import org.mbari.mondrian.AnnotationColors;
import org.mbari.mondrian.Localizations;
import org.mbari.mondrian.ToolBox;

import javax.imageio.ImageIO;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AnnotationPaneController {

    private BorderPane pane;
    private AutoscalePaneController<ImageView> autoscalePaneController;
    private ToolsPaneController toolsPaneController;
    private AnnotationColorPaneController annotationColorPaneController;
    private ConceptPaneController conceptPaneController;
    private CrossHairs crossHairs;
    private final EventBus eventBus;
    private final Localizations localizations;
    private final ToolBox toolBox;
    private static final Logger log = System.getLogger(AutoscalePaneController.class.getName());


    public AnnotationPaneController(ToolBox toolBox) {
        this.toolBox = toolBox;
        this.eventBus = toolBox.eventBus();
        this.localizations = toolBox.localizations();
        init();
    }


    private void init() {

        annotationColorPaneController = new AnnotationColorPaneController(localizations);
        crossHairs = new CrossHairs();
        autoscalePaneController = new ImagePaneController(new ImageView());
        toolsPaneController = new ToolsPaneController(autoscalePaneController,
                eventBus,
                annotationColorPaneController.getAnnotationColors(),
                localizations);
        conceptPaneController = new ConceptPaneController(toolBox);

        var autoscalePane = autoscalePaneController.getPane();
        autoscalePane.getStyleClass().add("autoscale-pane");
        autoscalePane.getChildren().addAll(crossHairs.getNodes());
        pane = new BorderPane(autoscalePaneController.getPane());
        pane.setBottom(conceptPaneController.getPane());
        pane.getStylesheets()
                .addAll(toolBox.stylesheets());

        var leftPane = new VBox();
        leftPane.setSpacing(5);

        var centerWrapper0 = new HBox(toolsPaneController.getPane());
        centerWrapper0.setAlignment(Pos.CENTER);
        leftPane.getChildren().add(centerWrapper0);

        var centerWrapper1 = new HBox(annotationColorPaneController.getPane());
        centerWrapper1.setAlignment(Pos.CENTER);
        leftPane.getChildren().add(centerWrapper1);

        pane.setLeft(leftPane);


        // This is important or center pane doesn't shrink when stage is shrunk
        pane.setMinSize(0, 0);

        var rx = eventBus.toObserverable();


        // TODO This is a HACK to make marker scaled to image size. Need to add a user setting for this.
        rx.ofType(AddMarkerEvent.class)
                .subscribe(evt -> {
                    var data = (CircleData) evt.localization().getDataView().getData();
                    var image = toolBox.data().getSelectedImage();
                    if (image != null) {
                        if (image.getWidth() == null) {
                            // TODO read image
                            var img = ImageIO.read(image.getUrl());
                            image.setWidth(img.getWidth());
                            image.setHeight(img.getHeight());
                        }
                        var radius = image.getWidth() / 100D;
                        Platform.runLater(() -> data.radiusProperty().set(radius));
                    }
                });

    }

    public AnnotationColors getAnnotationColors() {
        return annotationColorPaneController.getAnnotationColors();
    }

    public AutoscalePaneController<ImageView> getAutoscalePaneController() {
        return autoscalePaneController;
    }

    public void resetUsingImage(Image image) {
        log.log(Level.DEBUG, () -> String.format("resetUsingImage(%s)",
                Optional.ofNullable(image).map(Image::getUrl).orElse("null")));
        Platform.runLater(() -> {
            localizations.getLocalizations()
                    .forEach(loc -> loc.setVisible(false));
            localizations.getSelectedLocalizations().clear();
            localizations.setEditedLocalization(null);
            localizations.getLocalizations().clear();
            autoscalePaneController.getView().setImage(image);
        });
    }

    public BorderPane getPane() {
        return pane;
    }

    public CrossHairs getCrossHairs() {
        return crossHairs;
    }



    public EventBus getEventBus() {
        return eventBus;
    }



}
