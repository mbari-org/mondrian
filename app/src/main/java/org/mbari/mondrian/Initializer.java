package org.mbari.mondrian;

import javafx.beans.property.SimpleObjectProperty;
import org.mbari.imgfx.Autoscale;
import org.mbari.imgfx.etc.rx.EventBus;
import org.mbari.mondrian.etc.jdk.Logging;
import org.mbari.mondrian.services.ServiceFactory;
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
    private static Path settingsDirectory;
    private static ToolBox toolBox;
    private static final Object lock = new Object(){};

    public static void reset() {
        synchronized (lock) {
            toolBox = null;
        }
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

    public static ServiceFactory newServiceFactory() {
        return new VarsServiceFactory();
    }

    public static ToolBox getToolBox() {
        if (toolBox == null) {
            synchronized (lock) {
                var eventBus = new EventBus();
                var i18n = ResourceBundle.getBundle("i18n",
                        Locale.getDefault());
                var data = new Data();
                var services = newServiceFactory().newServices();
                var stylesheets = List.of(
                        Objects.requireNonNull(Initializer.class.getResource("/css/mondrian.css")).toExternalForm()
                );
                toolBox = new ToolBox(eventBus,
                        i18n,
                        data,
                        new Localizations(eventBus),
                        new AnnotationColors(),
                        new SimpleObjectProperty<>(services),
                        stylesheets,
                        getAes());
            }
        }
        return toolBox;
    }
}
