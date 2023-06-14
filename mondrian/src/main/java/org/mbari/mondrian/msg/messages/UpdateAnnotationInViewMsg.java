package org.mbari.mondrian.msg.messages;

import org.mbari.vars.services.model.Annotation;

/**
 * Message to be sent after an annotation has been changed in the database and we need
 * to update all it's reference in the UI
 *
 * @param annotation The updated annotation
 */
public record UpdateAnnotationInViewMsg(Annotation annotation) implements Message {
}
