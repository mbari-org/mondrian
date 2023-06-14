package org.mbari.mondrian;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Alert;
import org.mbari.imgfx.etc.rx.EventBus;
import org.mbari.mondrian.etc.jdk.Logging;
import org.mbari.mondrian.javafx.AnnotationColors;
import org.mbari.mondrian.javafx.Localizations;
import org.mbari.mondrian.javafx.dialogs.AlertContent;
import org.mbari.mondrian.javafx.settings.GeneralSettingsPaneController;
import org.mbari.mondrian.msg.messages.ShowAlertMsg;
import org.mbari.mondrian.services.ServiceFactory;
import org.mbari.mondrian.services.noop.NoopServiceFactory;
import org.mbari.mondrian.services.vars.VarsServiceFactory;
import org.mbari.vars.core.crypto.AES;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class Initializer {

    private static final Logging log = new Logging(Initializer.class);
    private static final EventBus eventBus = new EventBus();
    private static final ResourceBundle i18n = ResourceBundle.getBundle("i18n",
            Locale.getDefault());
    private static final List<String> stylesheets = List.of(
            "imgfx.css",
            Objects.requireNonNull(Initializer.class.getResource("/css/mondrian.css")).toExternalForm()
                );
    private static Path settingsDirectory;
    private static ToolBox toolBox;
    private static final Object lock = new Object(){};

    public static void reset() {
        synchronized (lock) {
            var services = loadServices();
            getToolBox().servicesProperty().set(services);
        }
    }

    public static EventBus getEventBus() {
        return eventBus;
    }

    public static ResourceBundle getI18n() {
        return i18n;
    }

    public static List<String> getStylesheets() {
        return stylesheets;
    }

    public static AES getAes() {
        return new AES("brian@mbari.org 1993-08-21");
    }

    /**
     * The settingsDirectory is scratch space for VARS
     *
     * @return The path to the settings directory. null is returned if the
     *  directory doesn't exist (or can't be created) or is not writable.
     */
    public static Path getSettingsDirectory() {
        if (settingsDirectory == null) {
            String home = System.getProperty("user.home");
            Path path = Paths.get(home, ".vars");
            settingsDirectory = createDirectory(path);
            if (settingsDirectory == null) {
                log.atWarn().log("Failed to create settings directory at " + path);
            }
        }
        return settingsDirectory;
    }

    public static Path createDirectory(Path path) {
        Path createdPath = path;
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
                if (!Files.isWritable(path)) {
                    createdPath = null;
                }
            }
            catch (IOException e) {
                String msg = "Unable to create a directory at " + path + ".";
                log.atError().withCause(e).log(msg);
                createdPath = null;
            }
        }
        return createdPath;
    }

    private static Services loadServices() {
        var services = new NoopServiceFactory().newServices();
        try {
            services = newServiceFactory().newServices();
        }
        catch (Exception e) {
            log.atWarn().withCause(e).log("Failed to initialize services. Defaulting to NOOP services");
            var alertContent = new AlertContent(i18n, "alert.initializer.services.failed", e);
            var msg = new ShowAlertMsg(Alert.AlertType.WARNING, alertContent, e);
            eventBus.publish(msg);
        }
        return services;
    }

    public static ServiceFactory newServiceFactory() {
        return new VarsServiceFactory();
    }

    public static ToolBox getToolBox() {
        if (toolBox == null) {
            synchronized (lock) {

                // Load settings from saved preferences
                var data = new Data();
                var generalSettings = GeneralSettingsPaneController.loadSettings();
                data.setPageSize(generalSettings.pageSize());

                var services = loadServices();

                toolBox = new ToolBox(eventBus,
                        i18n,
                        data,
                        new Localizations(eventBus),
                        new AnnotationColors(),
                        new SimpleObjectProperty<>(services),
                        stylesheets,
                        getAes(),
                        new SimpleObjectProperty<>());
                log.atInfo().log("ToolBox has been initialized");
            }
        }
        return toolBox;
    }
}
