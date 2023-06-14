package org.mbari.mondrian.javafx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;


public class AnnotationColors {
    private final ObjectProperty<Color> editedColor = new SimpleObjectProperty<>(Color.valueOf("#D65109"));
    private final ObjectProperty<Color> defaultColor = new SimpleObjectProperty<>(Color.valueOf("#4BA3C3"));
    private final ObjectProperty<Color> selectedColor = new SimpleObjectProperty<>(Color.valueOf("#2C4249"));

    public Color getEditedColor() {
        return editedColor.get();
    }

    public ObjectProperty<Color> editedColorProperty() {
        return editedColor;
    }

    public void setEditedColor(Color editedColor) {
        this.editedColor.set(editedColor);
    }

    public Color getDefaultColor() {
        return defaultColor.get();
    }

    public ObjectProperty<Color> defaultColorProperty() {
        return defaultColor;
    }

    public void setDefaultColor(Color defaultColor) {
        this.defaultColor.set(defaultColor);
    }

    public Color getSelectedColor() {
        return selectedColor.get();
    }

    public ObjectProperty<Color> selectedColorProperty() {
        return selectedColor;
    }

    public void setSelectedColor(Color selectedColor) {
        this.selectedColor.set(selectedColor);
    }
}
