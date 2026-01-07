package org.mbari.mondrian;

import org.mbari.mondrian.services.*;

public record Services(NamesService namesService,
                       AnnotationService annotationService,
                       AssociationService associationService,
                       ImageService imageService,
                       MediaService mediaService,
                       UsersService usersService
                       ) {


}
