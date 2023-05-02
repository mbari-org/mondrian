package org.mbari.mondrian.msg.messages;

import org.mbari.vars.services.model.Annotation;

import java.util.Collection;

public record SetAnnotationsForSelectedImageMsg(Object source, Collection<Annotation> annotations) implements Message {
}
