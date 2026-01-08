package org.mbari.mondrian.msg.messages;

import org.mbari.mondrian.domain.Selection;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;

import java.util.Collection;

public record SetAnnotationsForSelectedImageMsg(Selection<Collection<Annotation>> selection) implements Message {

    public Collection<Annotation> annotations() {
        return selection.selected();
    }
}
