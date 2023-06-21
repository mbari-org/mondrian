package org.mbari.mondrian;

import javafx.application.Platform;

import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Shape;
import org.mbari.imgfx.etc.rx.events.AddLocalizationEvent;
import org.mbari.imgfx.etc.rx.events.ClearLocalizations;
import org.mbari.imgfx.etc.rx.events.RemoveLocalizationEvent;
import org.mbari.imgfx.etc.rx.events.UpdatedLocalizationsEvent;
import org.mbari.imgfx.roi.Data;
import org.mbari.imgfx.roi.DataView;
import org.mbari.imgfx.roi.Localization;
import org.mbari.mondrian.domain.Page;
import org.mbari.mondrian.domain.Selection;
import org.mbari.mondrian.domain.VarsLocalization;
import org.mbari.mondrian.etc.jdk.Logging;
import org.mbari.mondrian.msg.commands.CreateVarsLocalizationCmd;
import org.mbari.mondrian.msg.commands.DeleteVarsLocalizationsCmd;
import org.mbari.mondrian.msg.commands.UpdateAnnotationsConceptCmd;
import org.mbari.mondrian.msg.commands.UpdateLocalizationCmd;
import org.mbari.mondrian.msg.messages.*;
import org.mbari.mondrian.util.CollectionUtils;
import org.mbari.mondrian.util.SupportUtils;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Image;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

        // Handles when a new localizaion is added to the view. Typically after a user click
        // At this point the localization data has not been saved via the services
        rx.ofType(AddLocalizationEvent.class)
                .subscribe(this::addLocalization);

        rx.ofType(RemoveLocalizationEvent.class)
                .window(200, TimeUnit.MILLISECONDS)
                .subscribe(obs -> obs.toList()
                        .subscribe(events -> {
                            var localizations = events.stream()
                                    .map(RemoveLocalizationEvent::localization)
                                    .toList();
                            removeLocalizations(localizations);
                        }));

        rx.ofType(AddVarsLocalizationMsg.class)
                        .subscribe(msg -> addVarsLocalization(msg.varsLocalization()));

        rx.ofType(RemoveVarsLocalizationMsg.class)
                .subscribe(msg -> removeVarsLocalization(msg.varsLocalization()));

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

        rx.ofType(SetSelectedAnnotationsMsg.class)
                        .subscribe(msg -> setSelectedAnnotations(msg.annotations()));

//        rx.ofType(SetSelectedLocalizationsMsg.class)
//                        .subscribe(msg -> toolBox.localizations().setSelectedLocalizations(msg.localizations()));

        rx.ofType(SetImagesMsg.class)
                .subscribe(msg -> Platform.runLater(() -> {
                    // Clear any previous selected image.
                    toolBox.eventBus().publish(new SetSelectedImageMsg(new Selection<>(AppController.this, null)));
                    toolBox.data().setCurrentImagePage(msg.selection().selected());
                }));

        rx.ofType(SetUserMsg.class)
                .subscribe(msg -> Platform.runLater(() -> toolBox.data().setUser(msg.user())));

        // TODO - implement me
        rx.ofType(UpdateAnnotationInViewMsg.class)
                .subscribe(this::updateAnnotation);

        rx.ofType(UpdateVarsLocalizationMsg.class)
                .subscribe(msg -> SupportUtils.replaceIn(msg.varsLocalization(), toolBox.data().getVarsLocalizations()));

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
        var eventBus = toolBox.eventBus();
        eventBus.publish(new ClearCommandManagerMsg());
        eventBus.publish(new ClearLocalizations());
        eventBus.publish(new SetAnnotationsForSelectedImageMsg(new Selection<>(AppController.this, List.of())));
        Platform.runLater(() -> toolBox.data().setSelectedImage(selectedImage));
        if (selectedImage != null && selectedImage.getImageReferenceUuid() != null) {
            toolBox.servicesProperty()
                    .get()
                    .annotationService()
                    .findByImageUuid(selectedImage.getImageReferenceUuid())
                    .thenAccept(annotations -> eventBus.publish(new SetAnnotationsForSelectedImageMsg(new Selection<>(AppController.this, annotations))));
        }
    }



    private void addLocalization(AddLocalizationEvent<? extends DataView<? extends Data, ? extends Shape>, ImageView> event) {
        // NOTE: The Localizations class is where Add/Remove Localization Events trigger add/remove
        // to the scene graph
        var loc = event.localization();
        // Check events isNew. If false don't update the concept use the one
        // already associated with it.
        if (event.isNew()) {
            var selectedConcept = toolBox.data().getSelectedConcept();
            if (selectedConcept != null) {
                loc.setLabel(selectedConcept);
            }
            // TODO if isNew create a new annotation and association via the service
            var data = toolBox.data();
            var command = new CreateVarsLocalizationCmd(data.getUser().getUsername(),
                    data.getSelectedImage(),
                    data.getSelectedConcept(),
                    loc,
                    null);
            toolBox.eventBus().publish(command);
        }

    }

    public void removeLocalizations(Collection<Localization<? extends DataView<? extends Data, ? extends Shape>, ? extends Node>> localizations) {
        var vlocs = VarsLocalization.localizationIntersection(toolBox.data().getVarsLocalizations(), localizations);
        if (!vlocs.isEmpty()) {
            var msg = new DeleteVarsLocalizationsCmd(vlocs);
            toolBox.eventBus().publish(msg);
        }
    }

    private void updateLocalization(UpdatedLocalizationsEvent event) {
        // TODO update the existing annotation/association via the service
    }

    private void updateAnnotation(UpdateAnnotationInViewMsg msg) {
        var loc = VarsLocalization.intersection(toolBox.data().getVarsLocalizations(), List.of(msg.annotation()));
        if (!loc.isEmpty()) {
            var head = CollectionUtils.head(loc);
            var newMsg = new UpdateVarsLocalizationMsg(head);
            toolBox.eventBus().publish(newMsg);
        }
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
                        var primaryName = concept.get().primaryName();
                        Platform.runLater(() -> toolBox.data().setSelectedConcept(primaryName));
                        // Update them in database
                        if (toolBox.data().getUser() != null) {
                            var selected = new ArrayList<>(toolBox.localizations().getSelectedLocalizations());
                            if (!selected.isEmpty()) {
                                var vlocs = VarsLocalization.localizationIntersection(toolBox.data().getVarsLocalizations(), selected);
                                var cmd = new UpdateAnnotationsConceptCmd(vlocs, primaryName, true);
                                toolBox.eventBus().publish(cmd);
                            }
                        }
                    }
                });
    }

    private void setSelectedAnnotations(Collection<Annotation> selectedAnnotations) {
        // Find matching varsLocalizations and select them
        var localizations = List.copyOf(toolBox.data().getVarsLocalizations());
        var selectedLocalizations = VarsLocalization.intersection(localizations, selectedAnnotations)
                        .stream()
                        .map(VarsLocalization::getLocalization)
                        .toList();
//        var msg = new SetSelectedLocalizationsMsg(new Selection<>(AppController.this, selectedLocalizations));
//        toolBox.eventBus().publish(msg);
        toolBox.localizations().setSelectedLocalizations(selectedLocalizations);
        toolBox.localizations().setEditedLocalization(null);


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

    public void addVarsLocalization(VarsLocalization varsLocalization) {
        toolBox.data().getVarsLocalizations().add(varsLocalization);

        // Listen for when a localization's location has been edited
        varsLocalization.getLocalization()
                .getDataView()
                .editingProperty()
                .addListener((obs, oldv, newv) -> {
                    if (!newv && varsLocalization.isDirtyLocalization()) {
                        varsLocalization.setDirtyLocalization(false);
                        var msg = new UpdateLocalizationCmd(varsLocalization);
                        toolBox.eventBus().publish(msg);
//                        log.atInfo().log(varsLocalization + " should be updated in the database");
                    }
                });

    }

    public void removeVarsLocalization(VarsLocalization varsLocalization) {
        toolBox.data().getVarsLocalizations().remove(varsLocalization);
        var nextMsg = new RemoveLocalizationEvent(varsLocalization.getLocalization());
        toolBox.eventBus().publish(nextMsg);
    }


}
