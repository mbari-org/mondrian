package org.mbari.mondrian.services.vars;

import org.mbari.mondrian.Services;
import org.mbari.mondrian.etc.jdk.Logging;
import org.mbari.mondrian.etc.okhttp3.ClientSupport;
import org.mbari.mondrian.services.ServiceFactory;
import org.mbari.vars.services.ServicesBuilder;

import java.util.concurrent.TimeUnit;

public class VarsServiceFactory implements ServiceFactory {

    private static final Logging log = new Logging(VarsServiceFactory.class);

    @Override
    public Services newServices() {
        var vars = loadServices();
        var nameService = new KbNamesService(vars.getConceptService());
        var annotationService = new AnnosaurusAnnotationService(vars.getAnnotationService(),
                vars.getMediaService());
        var associationService = new AnnosaurusAssociationService(vars.getAnnotationService());
        var mediaService = new VampireSquidService(vars.getMediaService());
        return new Services(nameService, annotationService, associationService, mediaService);
    }

    public static org.mbari.vars.services.Services loadServices() {
        var opt = Raziel.ConnectionParams.load();
        if (opt.isPresent()) {
            var rcp = opt.get();
            final var service = new Raziel(new ClientSupport());
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
