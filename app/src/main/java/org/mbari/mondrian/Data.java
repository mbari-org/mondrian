package org.mbari.mondrian;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.mbari.mondrian.domain.VarsLocalization;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Image;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class contains the current images in the annotation application. These images can also
 * be ones without any annotations. It has the following fields:
 *
 * <ul>
 *     <li>images - All images in the currently opened media</li>
 *     <li>selectedImage - The image that is currently selected and being annotated. This may be null</li>
 *     <li>varsLocalization - All the localizations found in the annotations/associations for the currently selected image.</li>
 * </ul>
 */
public class Data {

    private final ObservableList<Image> images = FXCollections.observableArrayList();
    private final ObjectProperty<Image> selectedImage = new SimpleObjectProperty<>();
    private final ObservableList<Annotation> annotationsForSelectedImage = FXCollections.observableArrayList();
    private final ObservableList<VarsLocalization> varsLocalizations = FXCollections.observableArrayList();
    private final ObservableList<String> concepts = FXCollections.observableArrayList();
    private final StringProperty selectedConcept = new SimpleStringProperty();

    public Image getSelectedImage() {
        return selectedImage.get();
    }

    public ObjectProperty<Image> selectedImageProperty() {
        return selectedImage;
    }

    public void setSelectedImage(Image selectedImage) {
        this.selectedImage.set(selectedImage);
    }

    public ObservableList<Image> getImages() {
        return images;
    }

    public ObservableList<Annotation> getAnnotationsForSelectedImage() {
        return annotationsForSelectedImage;
    }

    public ObservableList<VarsLocalization> getVarsLocalizations() {
        return varsLocalizations;
    }

    public ObservableList<String> getConcepts() {
        return concepts;
    }

    public String getSelectedConcept() {
        return selectedConcept.get();
    }

    public StringProperty selectedConceptProperty() {
        return selectedConcept;
    }

    public void setSelectedConcept(String selectedConcept) {
        this.selectedConcept.set(selectedConcept);
    }
}

