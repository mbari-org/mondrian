package org.mbari.mondrian.services.noop;

import org.mbari.mondrian.Services;
import org.mbari.mondrian.services.ServiceFactory;

public class NoopServiceFactory implements ServiceFactory {
    @Override
    public Services newServices() {
        return new Services(new NoopNamesService(), new NoopAnnotationService(), new NoopAssociationService(), new NoopImageService(), new NoopMediaService(), new NoopUsersServices());
    }
}
