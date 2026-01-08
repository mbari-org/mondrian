package org.mbari.mondrian.services.vars;

import org.mbari.mondrian.Services;
import org.mbari.mondrian.etc.jdk.Logging;
import org.mbari.mondrian.etc.okhttp3.ClientSupport;
import org.mbari.mondrian.services.MediaService;
import org.mbari.mondrian.services.ServiceFactory;
import org.mbari.vars.oni.sdk.r1.UserService;


import java.util.List;
import java.util.concurrent.TimeUnit;

public class VarsServiceFactory implements ServiceFactory {

    private static final Logging log = new Logging(VarsServiceFactory.class);


    @Override
    public Services newServices() {
        // Fetch info from Raziel and build services
        var serviceBuilder = new ServiceBuilder(true);
        var annosaurusClient = serviceBuilder.getAnnotationService();
        var oniClient = serviceBuilder.getConceptService();
        var vampireSquidClient = serviceBuilder.getMediaService();

        //
        var nameService = new KbNamesService(oniClient);
        var annotationService = new AnnosaurusAnnotationService(annosaurusClient, vampireSquidClient);
        var associationService = new AnnosaurusAssociationService(annosaurusClient);
        var mediaService = new VampireSquidService(vampireSquidClient);
        var usersService = new VarsUserService((UserService) oniClient);
        var imageService = new AnnosaurusImageService(annosaurusClient, vampireSquidClient);

        return new Services(nameService, annotationService, associationService, imageService, mediaService, usersService);

    }

}
