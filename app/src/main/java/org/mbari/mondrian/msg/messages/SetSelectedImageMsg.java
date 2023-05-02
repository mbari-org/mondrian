package org.mbari.mondrian.msg.messages;

import org.mbari.vars.services.model.Image;

public record SetSelectedImageMsg(Image image) implements Message {
}
