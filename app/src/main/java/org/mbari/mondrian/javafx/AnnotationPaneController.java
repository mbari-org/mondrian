package org.mbari.mondrian.javafx;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.etc.javafx.controls.CrossHairs;
import org.mbari.imgfx.etc.rx.EventBus;
import org.mbari.imgfx.etc.rx.events.AddLocalizationEvent;
import org.mbari.imgfx.imageview.ImagePaneController;
import org.mbari.mondrian.AnnotationColors;
import org.mbari.mondrian.Localizations;
import org.mbari.mondrian.ToolBox;

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
//        autoscalePane.setBackground(new Background(new BackgroundFill(Color.DARKGRAY, null, null)));
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

        eventBus.toObserverable()
                .ofType(AddLocalizationEvent.class)
                .subscribe(event -> {
                    localizations.setSelectedLocalizations(Collections.emptyList());
                    var loc = event.localization();
                    loc.setVisible(true);
                    localizations.setSelectedLocalizations(List.of(loc));
                });

    }

    public AutoscalePaneController<ImageView> getAutoscalePaneController() {
        return autoscalePaneController;
    }

    public void resetUsingImage(Image image) {
        log.log(Level.DEBUG, () -> String.format("resetUsingImage(%s)",
                Optional.ofNullable(image).map(Image::getUrl).orElse("null")));
        localizations.getLocalizations()
                .forEach(loc -> loc.setVisible(false));
        localizations.getSelectedLocalizations().clear();
        localizations.setEditedLocalization(null);
        localizations.getLocalizations().clear();
        autoscalePaneController.getView().setImage(image);
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
