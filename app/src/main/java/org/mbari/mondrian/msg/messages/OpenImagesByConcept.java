package org.mbari.mondrian.msg.messages;

public record OpenImagesByConcept(Object source,
                                  String concept,
                                  boolean includeDescendants,
                                  int size,
                                  int page) {
}
