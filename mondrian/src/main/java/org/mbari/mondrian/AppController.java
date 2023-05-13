package org.mbari.mondrian;

import javafx.application.Platform;

import javafx.scene.shape.Shape;
import org.mbari.imgfx.etc.rx.events.AddLocalizationEvent;
import org.mbari.imgfx.etc.rx.events.UpdatedLocalizationsEvent;
import org.mbari.mondrian.domain.Page;
import org.mbari.mondrian.domain.Selection;
import org.mbari.mondrian.domain.VarsLocalization;
import org.mbari.mondrian.etc.jdk.Logging;
import org.mbari.mondrian.msg.messages.*;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Image;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

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

        rx.ofType(OpenImagesByCameraDeploymentMsg.class)
                .subscribe(msg -> {
                    toolBox.data().setOpenUsingPaging(msg);
                    loadImagesByVideoSequenceName(msg.source(),
                            msg.cameraDeployment(), msg.size(), msg.page());
                });

        rx.ofType(OpenImagesByConceptMsg.class)
                .subscribe(msg -> {
                    toolBox.data().setOpenUsingPaging(msg);
                    loadImagesByConcept(msg.source(),
                            msg.concept(), msg.includeDescendants(),
                            msg.size(), msg.page());
                });

        rx.ofType(OpenNextPageMsg.class)
                .subscribe(msg -> {
                    var paging = toolBox.data().getOpenUsingPaging();
                    if (paging != null) {
                        toolBox.eventBus().publish(paging.nextPage());
                    }
                });

        rx.ofType(OpenPreviousPageMsg.class)
                .subscribe(msg -> {
                    var paging = toolBox.data().getOpenUsingPaging();
                    if (paging != null) {
                        toolBox.eventBus().publish(paging.previousPage());
                    }
                });

        // Reload/refresh data from services
        rx.ofType(ReloadMsg.class)
                .subscribe(
                        msg -> reload(),
                        throwable -> log.atError()
                                .withCause(throwable)
                                .log("An error occurred while handling a ReloadMsg"));

        rx.ofType(SetPageSizeMsg.class)
                .subscribe(msg -> {
                    if (msg.pageSize() > 0) {
                        toolBox.data().setPageSize(msg.pageSize());
                        var paging = toolBox.data().getOpenUsingPaging();
                        if (paging != null) {
                            toolBox.eventBus().publish(paging.withPageSize(msg.pageSize()));
                        }
                    }
                });

        // Sets the concept use for new annotations
        rx.ofType(SetSelectedConceptMsg.class)
                .subscribe(msg -> setSelectedConcept(msg.concept()));

        rx.ofType(SetSelectedImageMsg.class)
                .subscribe(msg -> setSelectedImage(msg.image()));

        rx.ofType(SetImagesMsg.class)
                .subscribe(msg -> Platform.runLater(() -> {
                    // Clear any previous selected image.
                    toolBox.eventBus().publish(new SetSelectedImageMsg(new Selection<>(AppController.this, null)));
                    toolBox.data().setCurrentImagePage(msg.selection().selected());
                }));

        rx.ofType(SetUserMsg.class)
                .subscribe(msg -> Platform.runLater(() -> toolBox.data().setUser(msg.user())));

        rx.ofType(SetAnnotationsForSelectedImageMsg.class)
                .subscribe(msg -> setAnnotationsForSelectedImage(msg.annotations()));

        // Handles when a new localizaion is added to the view. Typically after a user click
        // At this point the localization data has not been saved via the services
        rx.ofType(AddLocalizationEvent.class)
                .subscribe(this::addLocalization);

        rx.ofType(UpdatedLocalizationsEvent.class)
                .subscribe(this::updateLocalization);

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

    private void setSelectedImage(Image selectedImage) {
        toolBox.eventBus()
                .publish(new SetAnnotationsForSelectedImageMsg(new Selection<>(AppController.this, List.of())));
        Platform.runLater(() -> toolBox.data().setSelectedImage(selectedImage));
        if (selectedImage != null && selectedImage.getImageReferenceUuid() != null) {
            toolBox.servicesProperty()
                    .get()
                    .annotationService()
                    .findByImageUuid(selectedImage.getImageReferenceUuid())
                    .thenAccept(annotations -> toolBox.eventBus()
                            .publish(new SetAnnotationsForSelectedImageMsg(new Selection<>(AppController.this, annotations))));
        }
    }

    private void setAnnotationsForSelectedImage(Collection<Annotation> annotations) {
        //TODO save an dirty localizations in VARS
        toolBox.data()
                .getVarsLocalizations()
                .forEach(a -> a.getLocalization().setVisible(false));
        Platform.runLater(() -> toolBox.data().getAnnotationsForSelectedImage().setAll(annotations));
        // TODO build localizations. ONce build calling setVisible will ad them to the pane

    }


    private void addLocalization(AddLocalizationEvent<? extends Data, ? extends Shape> event) {
        var loc = event.localization();
        // Sheck events isNew. If false don't update the concept use the one
        // already associated with it.
        if (event.isNew()) {
            var selectedConcept = toolBox.data().getSelectedConcept();
            if (selectedConcept != null) {
                loc.setLabel(selectedConcept);
            }
            // TODO if isNew create a new annotation and association via the service
        }

    }

    private void updateLocalization(UpdatedLocalizationsEvent event) {
        // TODO update the existing annotation/association via the service
    }

    private void reload() {
        // Reload services as they may have changed
        Initializer.reset();
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

    public void loadImagesByVideoSequenceName(Object source,
                                              String videoSequenceName,
                                              int size,
                                              int pageNumber) {
        toolBox.servicesProperty()
                .get()
                .imageService()
                .findByVideoSequenceName(videoSequenceName, size, pageNumber)
                .handle((page, ex) -> {
                    if (ex != null) {
                        log.atWarn().withCause(ex).log("Failed to get images");
                    }
                    else {
                        log.atInfo().log(page + "");
                        var images = page.content()
                                .stream()
                                .sorted(Comparator.comparing(org.mbari.vars.services.model.Image::getRecordedTimestamp))
                                .distinct()
                                .toList();
                        var newPage = new Page<>(images, size, pageNumber, page.totalSize());
                        var selection = new Selection<>(source, newPage);
                        toolBox.eventBus().publish(new SetImagesMsg(selection));
                    }
                    return null;
                });
    }

    public void loadImagesByConcept(Object source,
                                    String concept,
                                    Boolean includeDescendants,
                                    int size,
                                    int pageNumber) {
        toolBox.servicesProperty()
                .get()
                .imageService()
                .findByConceptName(concept, size, pageNumber, includeDescendants)
                .handle((page, ex) -> {
                    if (ex != null) {
                        log.atWarn().withCause(ex).log("Failed to get images");
                    }
                    else {
                        var images = page.content()
                                .stream()
                                .sorted(Comparator.comparing(org.mbari.vars.services.model.Image::getRecordedTimestamp))
                                .distinct()
                                .toList();
                        var newPage = new Page<>(images, size, pageNumber, page.totalSize());
                        var selection = new Selection<>(source, newPage);
                        toolBox.eventBus().publish(new SetImagesMsg(selection));
                    }
                    return null;
                });
    }



}
