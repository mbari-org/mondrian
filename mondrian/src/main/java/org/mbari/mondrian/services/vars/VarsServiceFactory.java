package org.mbari.mondrian.services.vars;

import org.mbari.mondrian.Services;
import org.mbari.mondrian.etc.jdk.Logging;
import org.mbari.mondrian.etc.okhttp3.ClientSupport;
import org.mbari.mondrian.services.ServiceFactory;
import org.mbari.vars.services.ServicesBuilder;
import org.mbari.vars.services.model.EndpointConfig;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class VarsServiceFactory implements ServiceFactory {

    private static final Logging log = new Logging(VarsServiceFactory.class);

    private record ServiceData(org.mbari.vars.services.Services services, List<EndpointConfig> endpoints) {}

    @Override
    public Services newServices() {
        var data = loadServices();
        var vars = data.services();
        var nameService = new KbNamesService(vars.getConceptService());
        var annotationService = new AnnosaurusAnnotationService(vars.getAnnotationService(),
                vars.getMediaService());
        var associationService = new AnnosaurusAssociationService(vars.getAnnotationService());
        var mediaService = new VampireSquidService(vars.getMediaService());
        var usersService = new VarsUserService(vars.getUserService());

        var annoEndpoint = data.endpoints().stream().filter(e -> e.getName().equals("annosaurus")).findFirst();
        if (annoEndpoint.isEmpty()) {
            throw new RuntimeException("Unable to load annosaurus configuration from Raziel");
        }
        var imageService = new AnnosaurusImageService(vars.getAnnotationService(),
                vars.getMediaService(), new ClientSupport(), annoEndpoint.get());
        return new Services(nameService, annotationService, associationService, imageService, mediaService, usersService);
    }

    public static ServiceData loadServices() {
        var opt = Raziel.ConnectionParams.load();
        var emptyServiceData = new ServiceData(ServicesBuilder.noop(), List.of());
        if (opt.isPresent()) {
            var rcp = opt.get();
            final var service = new Raziel(new ClientSupport());
            var future = service.authenticate(rcp.url(), rcp.username(), rcp.password())
                    .thenCompose(auth -> service.endpoints(rcp.url(), auth.getAccessToken()))
                    .thenApply(endpoints -> {
                        var services = ServicesBuilder.buildForUI(endpoints);
                        return new ServiceData(services, endpoints);
                    })
                    .handle((services, ex) -> {
                        if (ex != null) {
                            log.atWarn().withCause(ex).log(() -> "Failed to retrieve server configurations from Raziel at " + rcp.url());
                            return emptyServiceData;
                        }
                        return services;
                    });

            try {
                return future.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.atWarn().withCause(e).log(() -> "Failed to retrieve server configurations from Raziel at " + rcp.url());
                return emptyServiceData;
            }

        }
        return emptyServiceData;
    }
}
