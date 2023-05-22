package org.mbari.mondrian.util;

import org.mbari.imgfx.etc.rx.EventBus;
import org.mbari.mondrian.domain.Selection;
import org.mbari.mondrian.domain.VarsLocalization;
import org.mbari.mondrian.msg.events.AddLocalizationEvents;
import org.mbari.mondrian.msg.messages.AddVarsLocalizationMsg;

public class SupportUtil {
    private SupportUtil() {
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
}
