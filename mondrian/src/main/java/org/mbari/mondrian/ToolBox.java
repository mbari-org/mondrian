package org.mbari.mondrian;

import javafx.beans.property.ObjectProperty;
import javafx.scene.image.ImageView;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.etc.rx.EventBus;
import org.mbari.mondrian.domain.Selection;
import org.mbari.mondrian.etc.jdk.Logging;
import org.mbari.mondrian.javafx.AnnotationPaneController;
import org.mbari.mondrian.javafx.AppPaneController;
import org.mbari.mondrian.msg.messages.SetImagesMsg;
import org.mbari.vars.core.crypto.AES;
import org.mbari.vars.services.model.Image;

import java.util.Collection;
import java.util.Comparator;
import java.util.ResourceBundle;

/**
 * Common objects used widely in the application
 *
 * @param eventBus The global event bus for the app
 * @param i18n Internationalized resources
 * @param data Data about the currently loaded images
 * @param localizations
 * @param annotationColors
 * @param servicesProperty
 * @param stylesheets
 * @param aes
 */
public record ToolBox(EventBus eventBus,
                      ResourceBundle i18n,
                      Data data,
                      Localizations localizations,
                      AnnotationColors annotationColors,
                      ObjectProperty<Services> servicesProperty,
                      Collection<String> stylesheets,
                      AES aes,
                      ObjectProperty<AnnotationPaneController> annotationPaneControllerProperty) {

    private static Logging log = new Logging(ToolBox.class);



}
