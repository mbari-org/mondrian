package org.mbari.mondrian;

import javafx.application.Platform;
import org.mbari.mondrian.etc.jdk.Logging;
import org.mbari.mondrian.msg.messages.ReloadMsg;
import org.mbari.mondrian.msg.messages.SetSelectedConcept;

public class AppController {

    private final ToolBox toolBox;
    private final Logging log = new Logging(getClass());
    private static final String KEY_CONCEPT_DEFAULT = "data.concept.default";

    public AppController(ToolBox toolBox) {
        this.toolBox = toolBox;
        init();
    }

    private void init() {
        var eventBus = toolBox.eventBus();

        eventBus.toObserverable()
                .ofType(ReloadMsg.class)
                .subscribe(
                        msg -> reload(),
                        throwable -> log.atError()
                                .withCause(throwable)
                                .log("An error occurred while handling a ReloadMsg"));

        eventBus.toObserverable()
                .ofType(SetSelectedConcept.class)
                .subscribe(msg -> setSelectedConcept(msg.concept()));

        // Run some stuff off main thread
        new Thread(() -> {
            reloadConcepts();
        }).start();
    }

    private void reload() {

        // Reload services as they may have changed
        var serviceFactory = Initializer.newServiceFactory();
        var services = serviceFactory.newServices();
        toolBox.servicesProperty().set(services);
        reloadConcepts();

    }

    private void reloadConcepts() {
        log.atInfo().log("Loading concepts");
        // Reload concepts for the new services
        // TODO we're only loading 10,000 names here. Need to page these to use worms
        toolBox.servicesProperty()
                .get()
                .namesService()
                .listNames(10000, 0)
                .thenAccept(page -> {
                    log.atInfo().log(page.content().size() + " concepts loaded");
                    Platform.runLater(() -> toolBox.data().getConcepts().setAll(page.content()));
                });
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
