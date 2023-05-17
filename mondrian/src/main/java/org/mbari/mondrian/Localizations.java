package org.mbari.mondrian;


import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import org.mbari.imgfx.etc.rx.events.*;
import org.mbari.imgfx.roi.Localization;
import org.mbari.imgfx.etc.rx.EventBus;
import org.mbari.imgfx.roi.Data;
import org.mbari.imgfx.roi.DataView;
import org.mbari.imgfx.util.ListUtils;
import org.mbari.mondrian.etc.jdk.Logging;

import java.util.List;


public class Localizations {

    private final ObservableList<Localization<? extends DataView<? extends Data, ? extends Node>, ? extends Node>> localizations = FXCollections.observableArrayList();
    private final ObservableList<Localization<? extends DataView<? extends Data, ? extends Node>, ? extends Node>> selectedLocalizations = FXCollections.observableArrayList();
    private final ObjectProperty<Localization<? extends DataView<? extends Data, ? extends Node>, ? extends Node>> editedLocalization = new SimpleObjectProperty<>();
    private final ObservableMap<Object, Boolean> visibleDataViewTypes =  FXCollections.observableHashMap();
    private final Logging log = new Logging(getClass());

    public Localizations(EventBus eventBus) {
        init(eventBus);
    }

    private void init(EventBus eventBus) {

        // Edited localizations are always visible
        editedLocalization.addListener((obs, oldv, newv) -> {
            if (oldv != null) {
                oldv.getDataView().setEditing(false);
                var visible = visibleDataViewTypes.getOrDefault(oldv.getDataView().getClass(), true);
                oldv.setVisible(visible);
            }
            if (newv != null) {
                newv.getDataView().setEditing(true);
                newv.setVisible(true);
            }
        });

        visibleDataViewTypes.addListener((MapChangeListener<Object, ? super Boolean>) c -> {
            updateVisibility();
        });

        var rx = eventBus.toObserverable();

        rx.ofType(AddLocalizationEvent.class)
                .subscribe(a -> {
                    var visible = visibleDataViewTypes.getOrDefault(a.localization().getDataView().getClass(), true);
                    log.atDebug().log("setVisible(" + visible + ") for " + a.localization());
                    Platform.runLater(() -> {
                        localizations.add(a.localization());
                        a.localization().setVisible(visible);
                        if (a.isNew()) {
                            selectedLocalizations.clear();
                            selectedLocalizations.add(a.localization());
                        }
                    });

                });

        rx.ofType(RemoveLocalizationEvent.class)
                .subscribe(a -> {
                    Platform.runLater(() -> {
                        a.localization().setVisible(false);
                        localizations.remove(a.localization());
                        selectedLocalizations.remove(a.localization());

                        var edited = editedLocalization.get();
                        if (edited != null && edited == a.localization()) {
                            editedLocalization.set(null);
                        }
                    });

                });

        rx.ofType(EditLocalizationEvent.class)
                .subscribe(a -> Platform.runLater(() -> editedLocalization.set(a.localization())));

        rx.ofType(ClearLocalizations.class)
                .subscribe(a -> {
                    Platform.runLater(() -> {
                        localizations.forEach(loc -> loc.setVisible(false));
                        selectedLocalizations.forEach(loc -> loc.setVisible(false));
                        editedLocalization.set(null);
                        selectedLocalizations.clear();
                        localizations.clear();
                    });

                });

        rx.ofType(ShowDataViewType.class)
                .subscribe(show -> setVisibility(show.dataViewType(), true));

        rx.ofType(HideDataViewType.class)
                .subscribe(hide -> setVisibility(hide.dataViewType(), false));
    }

    public ObservableList<Localization<? extends DataView<? extends Data, ? extends Node>, ? extends Node>> getLocalizations() {
        return localizations;
    }

    public void setLocalizations(List<Localization<? extends DataView<? extends Data, ? extends Node>, ? extends Node>> localizations) {
        this.localizations.setAll(localizations);
    }

    public ObservableList<Localization<? extends DataView<? extends Data, ? extends Node>, ? extends Node>> getSelectedLocalizations() {
        return selectedLocalizations;
    }

    public void setSelectedLocalizations(List<Localization<? extends DataView<? extends Data, ? extends Node>, ? extends Node>> selectedLocalizations) {
        // Get intersection with current annotations.
        var existingAnnotations = ListUtils.intersection(localizations, selectedLocalizations);
        this.selectedLocalizations.setAll(existingAnnotations);
    }

    public Localization<? extends DataView<? extends Data, ? extends Node>, ? extends Node> getEditedLocalization() {
        return editedLocalization.get();
    }

    public ObjectProperty<Localization<? extends DataView<? extends Data, ? extends Node>, ? extends Node>> editedLocalizationProperty() {
        return editedLocalization;
    }

    public void setEditedLocalization(Localization<? extends DataView<? extends Data, ? extends Node>, ? extends Node> editedLocalization) {
        this.editedLocalization.set(editedLocalization);
    }

    public ObservableMap<Object, Boolean> getVisibleDataViewTypes() {
        return visibleDataViewTypes;
    }

    public void setVisibility(Object dataViewTypeKey, Boolean visible) {
        visibleDataViewTypes.put(dataViewTypeKey, visible);
    }

    private void updateVisibility() {
        for (var viz : visibleDataViewTypes.entrySet()) {
            var clazz = viz.getKey();
            var visible = viz.getValue();
            localizations.stream()
                    .filter(loc -> loc.getDataView().getClass() == clazz)
                    .forEach(loc -> loc.setVisible(visible));
        }
    }
}
