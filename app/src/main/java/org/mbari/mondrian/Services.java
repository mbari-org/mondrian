package org.mbari.mondrian;

import org.mbari.mondrian.services.AnnotationService;
import org.mbari.mondrian.services.AssociationService;
import org.mbari.mondrian.services.NamesService;

public class Services {

    private final NamesService namesService;
    private final AnnotationService annotationService;
    private final AssociationService associationService;

    public Services(NamesService namesService, AnnotationService annotationService, AssociationService associationService) {
        this.namesService = namesService;
        this.annotationService = annotationService;
        this.associationService = associationService;
    }
}
