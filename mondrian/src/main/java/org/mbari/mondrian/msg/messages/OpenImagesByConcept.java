package org.mbari.mondrian.msg.messages;

public record OpenImagesByConcept(Object source,
                                  String concept,
                                  boolean includeDescendants,
                                  int size,
                                  int page) implements Message, Paging<OpenImagesByConcept> {

    @Override
    public OpenImagesByConcept nextPage() {
        return new OpenImagesByConcept(source, concept, includeDescendants, size, page + 1);
    }

    @Override
    public OpenImagesByConcept previousPage() {
        var p = Math.max(page - 1, 0);
        return new OpenImagesByConcept(source, concept, includeDescendants, size, p);
    }
}
