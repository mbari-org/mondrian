package org.mbari.mondrian;

import org.mbari.mondrian.services.AnnotationService;
import org.mbari.mondrian.services.AssociationService;
import org.mbari.mondrian.services.MediaService;
import org.mbari.mondrian.services.NamesService;

public record Services(NamesService namesService,
                       AnnotationService annotationService,
                       AssociationService associationService,
                       MediaService mediaService
                       ) {

}
