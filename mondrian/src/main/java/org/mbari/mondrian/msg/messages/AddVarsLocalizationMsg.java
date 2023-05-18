package org.mbari.mondrian.msg.messages;

import org.mbari.mondrian.domain.Selection;
import org.mbari.mondrian.domain.VarsLocalization;

/**
 * After a Localization has been created and it's corresponding annotation/association has
 * been added to the database, send this message to the UI can update itself.
 * @param selection
 */
public record AddVarsLocalizationMsg(Selection<VarsLocalization> selection) {
    public VarsLocalization varsLocalization() {
        return selection.selected();
    }
}
