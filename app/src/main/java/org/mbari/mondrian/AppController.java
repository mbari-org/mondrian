package org.mbari.mondrian;

import javafx.application.Platform;

import javafx.scene.shape.Shape;
import org.mbari.imgfx.etc.rx.events.AddLocalizationEvent;
import org.mbari.imgfx.etc.rx.events.AddMarkerEvent;
import org.mbari.imgfx.etc.rx.events.UpdatedLocalizationsEvent;
import org.mbari.imgfx.roi.CircleData;
import org.mbari.mondrian.etc.jdk.Logging;
import org.mbari.mondrian.msg.messages.*;

import javax.imageio.ImageIO;
import java.util.ArrayList;

public class AppController {

    private final ToolBox toolBox;
    private final Logging log = new Logging(getClass());
    private static final String KEY_CONCEPT_DEFAULT = "data.concept.default";

    public AppController(ToolBox toolBox) {
        this.toolBox = toolBox;
        init();
    }

    private void init() {
        var rx = toolBox.eventBus().toObserverable();

        // Reload/refresh data from services
        rx.ofType(ReloadMsg.class)
                .subscribe(
                        msg -> reload(),
                        throwable -> log.atError()
                                .withCause(throwable)
                                .log("An error occurred while handling a ReloadMsg"));

        // Sets the concept use for new annotations
        rx.ofType(SetSelectedConceptMsg.class)
                .subscribe(msg -> setSelectedConcept(msg.concept()));

        rx.ofType(SetSelectedImageMsg.class)
                .subscribe(msg -> Platform.runLater(() -> toolBox.data().setSelectedImage(msg.image())));

        rx.ofType(SetImagesMsg.class)
                .subscribe(msg -> Platform.runLater(() -> toolBox.data().getImages().setAll(msg.images())));

        rx.ofType(SetAnnotationsForSelectedImageMsg.class)
                    .subscribe(msg -> Platform.runLater(() -> toolBox.data().getAnnotationsForSelectedImage().setAll(msg.annotations())));

        // Handles when a new localizaion is added to the view. Typically after a user click
        // At this point the localization data has not been saved via the services
        rx.ofType(AddLocalizationEvent.class)
                .subscribe(this::addLocalization);

        rx.ofType(UpdatedLocalizationsEvent.class)
                .subscribe(this::updateLocalization);

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

        // When the selected concept is changed. Use it to update the labels of
        toolBox.data()
                .selectedConceptProperty()
                .addListener((obs, oldv, newv) -> {
                    if (newv != null) {
                        var selected = new ArrayList<>(toolBox.localizations().getSelectedLocalizations());
                        selected.forEach(loc -> loc.setLabel(newv));
                        toolBox.eventBus().publish(new UpdatedLocalizationsEvent(selected));
                    }
                });

    }

    private void addLocalization(AddLocalizationEvent<? extends Data, ? extends Shape> event) {
        var loc = event.localization();
        var selectedConcept = toolBox.data().getSelectedConcept();
        if (selectedConcept != null) {
            loc.setLabel(selectedConcept);
            // TODO create a new annotation and association via the service
        }
    }

    private void updateLocalization(UpdatedLocalizationsEvent event) {
        // TODO update the existing annotation/association via the service
    }

    private void reload() {
        // Reload services as they may have changed
        var serviceFactory = Initializer.newServiceFactory();
        var services = serviceFactory.newServices();
        toolBox.servicesProperty().set(services);
    }


    private void setSelectedConcept(String conceptName) {
        var services = toolBox.servicesProperty().get();
        services.namesService()
                .findConcept(conceptName)
                .thenAccept(concept -> {
                    if (concept.isEmpty()) {
                        log.atInfo().log("Unable to find concept with name of " + conceptName);
                        var d = toolBox.i18n().getString(KEY_CONCEPT_DEFAULT);
                        Platform.runLater(() -> toolBox.data().setSelectedConcept(d));
                    }
                    else {
                        Platform.runLater(() -> toolBox.data().setSelectedConcept(concept.get().primaryName()));
                    }
                });
    }


}
