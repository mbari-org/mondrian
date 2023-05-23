package org.mbari.mondrian.msg.commands;

import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import org.mbari.imgfx.roi.Localization;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.domain.Selection;
import org.mbari.mondrian.domain.VarsLocalization;
import org.mbari.mondrian.etc.jdk.Logging;
import org.mbari.mondrian.javafx.dialogs.AlertContent;
import org.mbari.mondrian.javafx.roi.Datum;
import org.mbari.mondrian.javafx.roi.Datums;
import org.mbari.mondrian.javafx.roi.RoiTranslators;
import org.mbari.mondrian.msg.messages.RemoveVarsLocalizationMsg;
import org.mbari.mondrian.msg.messages.ShowAlertMsg;
import org.mbari.mondrian.util.SupportUtil;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Image;

import java.util.List;

public class CreateAnnotationWithLocalizationCmd implements Command {

    private static final Logging log = new Logging(CreateAnnotationWithLocalizationCmd.class);
    private final String observer;
    private final Image image;
    private final String concept;
    private final Localization<?, ImageView> localization;
    private final String comment;
    private final Datum<?> datum;
    private VarsLocalization varsLocalization;


    public CreateAnnotationWithLocalizationCmd(String observer,
                                               Image image,
                                               String concept,
                                               Localization<?, ImageView> localization,
                                               String comment) {
        this.observer = observer;
        this.image = image;
        this.concept = concept;
        this.localization = localization;
        this.comment = comment;
        var opt = Datums.from(localization.getDataView().getData());
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Unable to extract valid datum from " + localization);
        }
        this.datum = opt.get();
    }

    @Override
    public void apply(ToolBox toolBox) {
        // NOTE that  New localizations already have random UUID's assigned and these are used
        // as the associations UUID.
        if (datum != null) {
            var opt = RoiTranslators.fromLocalization(localization,
                    image.getImageReferenceUuid(),
                    comment);
            if (opt.isPresent()) {
                var association = opt.get();
                var annotation = new Annotation(concept, observer, image.getVideoIndex(), image.getVideoReferenceUuid());
                annotation.setAssociations(List.of(association));
                toolBox.servicesProperty()
                        .get()
                        .annotationService()
                        .create(List.of(annotation))
                        .handle((xs, ex) -> {
                            if (ex != null) {
                                var alertContent = new AlertContent(toolBox.i18n(), "alert.command.create.annotation.apply", ex);
                                log.atInfo().log(alertContent.content());
                                toolBox.eventBus().publish(new ShowAlertMsg(Alert.AlertType.INFORMATION, alertContent));
                                return null;
                            }
                            else {
                                for (var a: xs) {
                                    for (var ass : a.getAssociations()) {
                                        if (ass.getUuid().toString().equals(localization.getUuid().toString())) {
                                            varsLocalization = new VarsLocalization(a, ass, localization);
                                            SupportUtil.publishVarsLocalization(varsLocalization, false, toolBox.eventBus(), CreateAnnotationWithLocalizationCmd.this);
//                                            AddLocalizationEvents.from(varsLocalization.getLocalization(), false)
//                                                    .ifPresent(evt -> {
//                                                        toolBox.eventBus().publish(evt);
//                                                        toolBox.eventBus().publish(new AddVarsLocalizationMsg(new Selection<>(CreateAnnotationWithLocalizationCmd.this, varsLocalization)));
//                                                    });
                                            break;
                                        }
                                    }
                                }
                                return null;
                            }
                        });
            }
            else {
                var alertContent = new AlertContent(toolBox.i18n(), "alert.command.create.annotation.apply");
                log.atInfo().log(alertContent.content());
                toolBox.eventBus().publish(new ShowAlertMsg(Alert.AlertType.INFORMATION, alertContent));
            }
        }
    }

    @Override
    public void unapply(ToolBox toolBox) {
        if (datum != null && varsLocalization != null) {
            toolBox.servicesProperty()
                    .get()
                    .annotationService()
                    .deleteAll(List.of(varsLocalization.getAnnotation().getObservationUuid()))
                    .thenAccept(ok -> {
                        if (ok) {
                            var msg = new RemoveVarsLocalizationMsg(new Selection<>(CreateAnnotationWithLocalizationCmd.this, varsLocalization));
                            toolBox.eventBus().publish(msg);
                        }
                        else {
                            var alertContent = new AlertContent(toolBox.i18n(), "alert.command.create.annotation.unapply");
                            var alertContent2 = alertContent.copy(alertContent.content() + ": " + getDescription());
                            log.atInfo().log(alertContent2.content());
                            toolBox.eventBus().publish(new ShowAlertMsg(Alert.AlertType.INFORMATION, alertContent2));
                        }
                    });
        }
        else {
            var content = toolBox.i18n().getString("alert.command.create.annotation.unapply.content2");
            log.atInfo().log(content);
        }
    }

    @Override
    public String getDescription() {
        return "Create annotation of " + concept + " with " + localization;
    }
}
