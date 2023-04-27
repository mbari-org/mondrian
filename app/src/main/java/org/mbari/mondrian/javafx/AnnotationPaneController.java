package org.mbari.mondrian.javafx;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.etc.javafx.controls.CrossHairs;
import org.mbari.imgfx.etc.rx.EventBus;
import org.mbari.imgfx.etc.rx.events.AddLocalizationEvent;
import org.mbari.imgfx.imageview.ImagePaneController;
import org.mbari.mondrian.AnnotationColors;
import org.mbari.mondrian.Localizations;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Collections;
import java.util.List;

public class AnnotationPaneController {

    private BorderPane pane;
    private AutoscalePaneController<ImageView> autoscalePaneController;
    private ToolsPaneController toolsPaneController;
    private AnnotationColorPaneController annotationColorPaneController;
    private ConceptPaneController conceptPaneController;
    private CrossHairs crossHairs;
    private final EventBus eventBus;
    private final Localizations localizations;
    private final ObservableList<String> concepts;
    private static final Logger log = System.getLogger(AutoscalePaneController.class.getName());



    public AnnotationPaneController(EventBus eventBus, ObservableList<String> concepts) {
        this.eventBus = eventBus;
        this.localizations = new Localizations(eventBus);
        this.concepts = concepts;
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
        conceptPaneController = new ConceptPaneController(this);

        var autoscalePane = autoscalePaneController.getPane();
        autoscalePane.getChildren().addAll(crossHairs.getNodes());
        pane = new BorderPane(autoscalePaneController.getPane());
        pane.setBottom(conceptPaneController.getPane());
        pane.getStylesheets()
                .addAll("imgfx.css");

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
        log.log(Level.DEBUG, () -> String.format("resetUsingImage(%s)", image.getUrl()));
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

    public Localizations getLocalizations() {
        return localizations;
    }

    public ObservableList<String> getConcepts() {
        return concepts;
    }

    public void setConcepts(List<String> concepts) {
        this.concepts.setAll(concepts);
    }

    public void setSelectedConcept(String concept) {
        if (this.concepts.contains(concept)) {
            conceptPaneController.getConceptComboBox()
                    .getSelectionModel().select(concept);
        }
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public AnnotationColors getAnnotationColors() {
        return annotationColorPaneController.getAnnotationColors();
    }


}
