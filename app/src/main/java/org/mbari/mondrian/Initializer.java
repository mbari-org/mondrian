package org.mbari.mondrian;

import org.mbari.mondrian.etc.jdk.Logging;
import org.mbari.mondrian.services.vars.Raziel;
import org.mbari.vars.core.crypto.AES;
import org.mbari.vars.services.Services;
import org.mbari.vars.services.ServicesBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class Initializer {

    private static final Logging log = new Logging(Initializer.class);
    private static Path settingsDirectory;

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

    public static Services loadServices() {
        var opt = Raziel.ConnectionParams.load();
        if (opt.isPresent()) {
            var rcp = opt.get();
            final var service = new Raziel();
            var future = service.authenticate(rcp.url(), rcp.username(), rcp.password())
                    .thenCompose(auth -> service.endpoints(rcp.url(), auth.getAccessToken()))
                    .thenApply(ServicesBuilder::buildForUI)
                    .handle((services, ex) -> {
                        if (ex != null) {
                            log.atWarn().withCause(ex).log(() -> "Failed to retrieve server configurations from Raziel at " + rcp.url());
                            return ServicesBuilder.noop();
                        }
                        return services;
                    });

            try {
                return future.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.atWarn().withCause(e).log(() -> "Failed to retrieve server configurations from Raziel at " + rcp.url());
                return ServicesBuilder.noop();
            }

        }
        return ServicesBuilder.noop();
    }
}
