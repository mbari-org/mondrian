package org.mbari.mondrian.msg.messages;

import org.mbari.mondrian.domain.Selection;
import org.mbari.mondrian.domain.VarsLocalization;

public record RemoveVarsLocalizationMsg(Selection<VarsLocalization> selection) implements Message {
    public VarsLocalization varsLocalization() {
        return selection.selected();
    }
}