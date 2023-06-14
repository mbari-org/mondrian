package org.mbari.mondrian.javafx;


import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.mbari.imgfx.roi.Data;
import org.mbari.imgfx.roi.DataView;
import org.mbari.imgfx.roi.Localization;

import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class AnnotationColorPaneController {
    private final GridPane pane = new GridPane();
    private final AnnotationColors annotationColors = new AnnotationColors();
    private final Localizations localizations;

    private final String SELECTED_KEY = "selectedColor";
    private final String EDITED_KEY = "editedColor";
    private final String DEFAULT_KEY = "defaultColor";

    public AnnotationColorPaneController(Localizations localizations) {
        this.localizations = localizations;
        init();
    }

    private void init() {

        pane.getStyleClass().add("annotation-color-pane");

        annotationColors.editedColorProperty().addListener((obs, oldv, newv) -> {
            var editedAnno = localizations.getEditedLocalization();
            if (editedAnno != null) {
                editedAnno.getDataView().setColor(newv);
            }
        });

        annotationColors.selectedColorProperty().addListener((obs, oldv, newv) -> {
            localizations.getSelectedLocalizations()
                    .stream()
                    .forEach(a -> a.getDataView().setColor(newv));
        });

        annotationColors.defaultColorProperty().addListener((obs, oldv, newv) -> {
            // Only change annotation colors that aren't selected
            var selected = localizations.getSelectedLocalizations();
            var collect = localizations.getLocalizations()
                    .stream()
                    .collect(Collectors.partitioningBy(selected::contains));
            collect.get(false)
                    .stream()
                    .forEach(a -> a.getDataView().setColor(newv));
        });

        localizations.getSelectedLocalizations()
                .addListener((ListChangeListener<? super Localization<? extends DataView<? extends Data,? extends Node>,? extends Node>>) c -> {
                    updateColors();
                });

        localizations.getLocalizations()
                .addListener((ListChangeListener<? super Localization<? extends DataView<? extends Data,? extends Node>,? extends Node>>) c -> {
                    updateColors();
                });

        var selectedPicker = new ColorPicker();
        selectedPicker.setStyle("-fx-color-label-visible: false ;");
        selectedPicker.setTooltip(new Tooltip("Selected"));
        var editedPicker = new ColorPicker();
        editedPicker.setStyle("-fx-color-label-visible: false ;");
        editedPicker.setTooltip(new Tooltip("Editing"));
        var defaultPicker = new ColorPicker();
        defaultPicker.setStyle("-fx-color-label-visible: false ;");
        defaultPicker.setTooltip(new Tooltip("Default"));

        // When a picker is set pass that color on to the AnnotationColors object
        selectedPicker.setOnAction(actionEvent ->
                annotationColors.setSelectedColor(selectedPicker.getValue()));
        editedPicker.setOnAction(actionEvent ->
                annotationColors.setEditedColor(editedPicker.getValue()));
        defaultPicker.setOnAction(actionEvent ->
                annotationColors.setDefaultColor(defaultPicker.getValue()));

        // If an annotationColor is changed update the picker
        annotationColors.selectedColorProperty()
                .addListener((obs, oldv, newv) -> selectedPicker.setValue(newv));
        annotationColors.editedColorProperty()
                .addListener((obs, oldv, newv) -> editedPicker.setValue(newv));
        annotationColors.defaultColorProperty()
                .addListener((obs, oldv, newv) -> defaultPicker.setValue(newv));

        load();
        Runtime.getRuntime().addShutdownHook(new Thread(this::save));

        pane.add(new Label("Colors"), 1, 0);
        pane.add(new Label("Selected"), 0, 1);
        pane.add(selectedPicker, 1, 1);
        pane.add(new Label("Editing"), 0, 2);
        pane.add(editedPicker, 1, 2);
        pane.add(new Label("Default"), 0, 3);
        pane.add(defaultPicker, 1, 3);
        pane.setVgap(5);
        pane.setHgap(5);

    }

    private void load() {
        var prefs = Preferences.userNodeForPackage(getClass());
        var edited = prefs.get(EDITED_KEY, "0xD65109AA");
        var selected = prefs.get(SELECTED_KEY, "0x4BA3C3AA");
        var regular = prefs.get(DEFAULT_KEY, "0x2C4249AA");

        annotationColors.setEditedColor(Color.valueOf(edited));
        annotationColors.setSelectedColor(Color.valueOf(selected));
        annotationColors.setDefaultColor(Color.valueOf(regular));
    }

    private void save() {
        var prefs = Preferences.userNodeForPackage(getClass());
        prefs.put(EDITED_KEY, annotationColors.getEditedColor().toString());
        prefs.put(SELECTED_KEY, annotationColors.getSelectedColor().toString());
        prefs.put(DEFAULT_KEY, annotationColors.getDefaultColor().toString());
    }

    private void updateColors() {
        localizations.getLocalizations()
                .forEach(loc -> loc.getDataView().setColor(annotationColors.getDefaultColor()));
        localizations.getSelectedLocalizations()
                .forEach(loc -> loc.getDataView().setColor(annotationColors.getSelectedColor()));

        var loc = localizations.getEditedLocalization();
        if (loc != null) {
            loc.getDataView().setColor(annotationColors.getEditedColor());
        }
    }

    public GridPane getPane() {
        return pane;
    }

    public AnnotationColors getAnnotationColors() {
        return annotationColors;
    }
}
