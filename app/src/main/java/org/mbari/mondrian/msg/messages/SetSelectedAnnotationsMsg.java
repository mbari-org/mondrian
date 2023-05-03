package org.mbari.mondrian.msg.messages;

import org.mbari.mondrian.domain.Selection;
import org.mbari.vars.services.model.Annotation;

import java.util.Collection;

public record SetSelectedAnnotationsMsg(Selection<Collection<Annotation>> selection) implements Message {

    public Collection<Annotation> annotations() {
        return selection.selected();
    }
}
