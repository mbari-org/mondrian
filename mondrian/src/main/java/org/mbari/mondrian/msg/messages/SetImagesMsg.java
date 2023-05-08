package org.mbari.mondrian.msg.messages;

import org.mbari.mondrian.domain.Selection;
import org.mbari.vars.services.model.Image;

import java.util.Collection;

public record SetImagesMsg(Selection<Collection<Image>> selection) {

    public Collection<Image> images() {
        return selection.selected();
    }
}
