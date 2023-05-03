package org.mbari.mondrian.msg.messages;

import org.mbari.mondrian.domain.Selection;

public record SetSelectedConceptMsg(Selection<String> selection) implements Message {

    public String concept() {
        return selection.selected();
    }
}
