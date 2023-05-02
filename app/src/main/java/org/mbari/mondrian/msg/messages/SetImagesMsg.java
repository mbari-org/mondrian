package org.mbari.mondrian.msg.messages;

import org.mbari.vars.services.model.Image;

import java.util.Collection;

public record SetImagesMsg(Collection<Image> images) {
}
