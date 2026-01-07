package org.mbari.mondrian.util;

import org.mbari.imgfx.etc.rx.EventBus;
import org.mbari.mondrian.domain.Selection;
import org.mbari.mondrian.domain.VarsLocalization;
import org.mbari.mondrian.msg.events.AddLocalizationEvents;
import org.mbari.mondrian.msg.messages.AddVarsLocalizationMsg;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;

import java.util.ArrayList;
import java.util.List;

public class SupportUtils {
    private SupportUtils() {
        // No instantiation allowed
    }

    /**
     * Publish a localization that has not been added to the UI components. This typically done
     * when a localization is built from an annotation.
     * @param varsLocalization The VarsLocalization to add
     * @param isNew true if this was created by the UI, false if parsed from an existing annotation. In most cases, thsi will be false
     * @param eventBus The eventbus to publish on
     * @param source The `source` for the Selection events
     */
    public static void publishVarsLocalization(VarsLocalization varsLocalization,
                                               boolean isNew,
                                               EventBus eventBus,
                                               Object source) {
        AddLocalizationEvents.from(varsLocalization.getLocalization(), isNew)
                .ifPresent(evt -> {
                    // Add the localization to the UI components
                    eventBus.publish(evt);
                    // Add the localization data to be managed.
                    eventBus.publish(new AddVarsLocalizationMsg(new Selection<>(source, varsLocalization)));
                });
    }

    public static boolean replaceIn(VarsLocalization varsLocalization, List<VarsLocalization> xs) {
        var isUpdated = false;
        var associationUuid = varsLocalization.getAssociation().getUuid();
        for (int i = 0; i < xs.size(); i++) {
            var x = xs.get(i);
            var uuid = x.getAssociation().getUuid();
            if (uuid.equals(associationUuid)) {
                xs.set(i, varsLocalization);
                isUpdated = true;
                break;
            }
        }
        return isUpdated;
    }

    public static Annotation replaceIn(Association association, Annotation annotation) {
        var newAnno = new Annotation(annotation);
        var associations = new ArrayList<>(annotation.getAssociations());
        for (int i = 0; i < associations.size(); i++) {
            var ass = associations.get(i);
            if (association.getUuid().equals(ass.getUuid())) {
                associations.set(i, association);
                break;
            }
        }
        newAnno.setAssociations(associations);
        return newAnno;
    }


}
