package org.mbari.mondrian.domain;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.roi.Data;
import org.mbari.imgfx.roi.DataView;
import org.mbari.imgfx.roi.Localization;
import org.mbari.imgfx.roi.RectangleView;
import org.mbari.mondrian.etc.gson.Json;
import org.mbari.mondrian.etc.jdk.Logging;
import org.mbari.mondrian.javafx.roi.RoiTranslators;
import org.mbari.mondrian.util.SupportUtils;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class VarsLocalization {

    private final Annotation annotation;
    private final Association association;
    private final Localization<? extends DataView<? extends Data, ? extends Shape>, ? extends Node> localization;
    private final BooleanProperty dirtyConcept = new SimpleBooleanProperty(false);
    private final BooleanProperty dirtyLocalization = new SimpleBooleanProperty(false);
    private static final Logging log = new Logging(VarsLocalization.class);


    public VarsLocalization(Annotation annotation,
                            Association association,
                            Localization<? extends DataView<? extends Data, ? extends Shape>, ? extends Node> localization) {
        this.annotation = annotation;
        this.association = association;
        this.localization = localization;
        init();
    }

    private void init() {
        localization.labelProperty().addListener((obs, oldv, newv) -> dirtyConcept.set(true));
        if (localization.getDataView() instanceof RectangleView view) {
            view.getData().xProperty().addListener((obs, oldv, newv) -> dirtyLocalization.set(true));
            view.getData().yProperty().addListener((obs, oldv, newv) -> dirtyLocalization.set(true));
            view.getData().widthProperty().addListener((obs, oldv, newv) -> dirtyLocalization.set(true));
            view.getData().heightProperty().addListener((obs, oldv, newv) -> dirtyLocalization.set(true));
        }
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public Association getAssociation() {
        return association;
    }

    public Localization<? extends DataView<? extends Data, ? extends Shape>, ? extends Node> getLocalization() {
        return localization;
    }

    public boolean getDirtyConcept() {
        return dirtyConcept.get();
    }

    public BooleanProperty dirtyConceptProperty() {
        return dirtyConcept;
    }

    public void setDirtyConcept(boolean dirtyConcept) {
        this.dirtyConcept.set(dirtyConcept);
    }

    public boolean isDirtyLocalization() {
        return dirtyLocalization.get();
    }

    public BooleanProperty dirtyLocalizationProperty() {
        return dirtyLocalization;
    }

    public void setDirtyLocalization(boolean dirtyLocalization) {
        this.dirtyLocalization.set(dirtyLocalization);
    }

    public static Optional<VarsLocalization> from(Annotation annotation,
                                                  Association association,
                                                  AutoscalePaneController<ImageView> autoscalePaneController,
                                                  ObjectProperty<Color> editedColor) {
        var opt = RoiTranslators.findByLinkName(association.getLinkName());
        if (opt.isPresent()) {
            log.atDebug().log("Found ROI in " + association);
            final var roiTranslator = opt.get();
            log.atDebug().log("Building ROI using " + association + " and " + roiTranslator);
            return roiTranslator.fromAssociation(annotation.getConcept(),
                            association,
                            autoscalePaneController,
                            editedColor)
                    .map(localization -> {
                        // IMPORTANT: the localizations and it's association have the same UUID!!!
                        localization.setUuid(association.getUuid());
                        return new VarsLocalization(annotation, association, localization);
                    });

        }
        return Optional.empty();
    }

    public static List<VarsLocalization> from(Annotation annotation,
                                              AutoscalePaneController<ImageView> autoscalePaneController,
                                              ObjectProperty<Color> editedColor) {
        return annotation.getAssociations()
                .stream()
                .flatMap(a -> from(annotation, a, autoscalePaneController, editedColor).stream())
                .toList();
    }

    public static List<VarsLocalization> from(Collection<Annotation> annotations,
                                              AutoscalePaneController<ImageView> autoscalePaneController,
                                              ObjectProperty<Color> editedColor) {
        return annotations.stream()
                .flatMap(a -> from(a, autoscalePaneController, editedColor).stream())
                .toList();
    }

    public static Collection<VarsLocalization> intersection(Collection<VarsLocalization> locs,
                                                            Collection<Annotation> annos) {
        return annos.stream()
                .flatMap(a -> a.getAssociations().stream())
                .filter(a -> RoiTranslators.findByLinkName(a.getLinkName()).isPresent())
                .flatMap(a -> locs.stream()
                        .filter(l -> l.getLocalization().getUuid().equals(a.getUuid()))
                        .findFirst()
                        .stream())
                .toList();
    }

    public static Collection<VarsLocalization> localizationIntersection(Collection<VarsLocalization> vlocs,
                                                                        Collection<Localization<? extends DataView<? extends Data, ? extends Shape>, ? extends Node>> locs) {
        return vlocs.stream()
                .filter(v -> locs.stream()
                        .anyMatch(l -> l.getUuid().equals(v.localization.getUuid())))
                .toList();
    }

    public VarsLocalization updateUsingLocalizationData() {
        var json = getAssociation().getLinkValue();
        var auxInfo = Json.decode(json, AuxInfo.class);
        return RoiTranslators.fromLocalization(localization,
                        auxInfo.getImageReferenceUuuid(),
                        auxInfo.getComment())
                .map(newAssociation -> {
                    var anno = SupportUtils.replaceIn(newAssociation, annotation);
                    return new VarsLocalization(anno, newAssociation, localization);
                })
                .orElseThrow(); // HARD get here. In theory this should never fail
    }



}
