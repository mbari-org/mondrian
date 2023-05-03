package org.mbari.mondrian.msg.messages;

import org.mbari.mondrian.domain.Selection;
import org.mbari.vars.services.model.Image;

public record SetSelectedImageMsg(Selection<Image> selection) implements Message {

    public Image image() {
        return selection.selected();
    }

}
