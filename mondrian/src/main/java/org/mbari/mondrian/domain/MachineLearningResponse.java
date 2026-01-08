package org.mbari.mondrian.domain;

import org.mbari.mondrian.etc.gson.Json;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;
import org.mbari.vars.annosaurus.sdk.r1.models.Image;


import java.util.List;

public record MachineLearningResponse(Image image, List<MachineLearningLocalization> mlLocalizations) {


    public List<Annotation> toAnnotation(String observer, String group, String activity) {

        return mlLocalizations.stream()
                .map(mll -> {
                    var json = Json.stringify(mll.boundingBox());
                    final var association = new Association(BoundingBox.LINK_NAME, Association.VALUE_SELF, json, "application/json");
                    final var annotation = new Annotation(mll.concept(), observer, image.getVideoIndex(), image.getVideoReferenceUuid());
                    annotation.setAssociations(List.of(association));
                    annotation.setGroup(group);
                    annotation.setActivity(activity);
                    return annotation;
                })
                .toList();


    }

}
