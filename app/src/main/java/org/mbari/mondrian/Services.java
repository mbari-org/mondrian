package org.mbari.mondrian;

import org.mbari.mondrian.domain.Page;
import org.mbari.mondrian.services.*;
import org.mbari.vars.services.model.Image;

public record Services(NamesService namesService,
                       AnnotationService annotationService,
                       AssociationService associationService,
                       ImageService imageService,
                       MediaService mediaService
                       ) {


}
