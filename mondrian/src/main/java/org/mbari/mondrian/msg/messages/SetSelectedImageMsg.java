package org.mbari.mondrian.msg.messages;

import org.mbari.mondrian.domain.Selection;
import org.mbari.vars.annosaurus.sdk.r1.models.Image;

public record SetSelectedImageMsg(Selection<Image> selection) implements Message {

    public Image image() {
        return selection.selected();
    }

}
