package org.mbari.mondrian.msg.messages;

public record OpenImagesByConceptMsg(Object source,
                                     String concept,
                                     boolean includeDescendants,
                                     int size,
                                     int page) implements Message, Paging<OpenImagesByConceptMsg> {

    @Override
    public OpenImagesByConceptMsg nextPage() {
        return new OpenImagesByConceptMsg(source, concept, includeDescendants, size, page + 1);
    }

    @Override
    public OpenImagesByConceptMsg previousPage() {
        var p = Math.max(page - 1, 0);
        return new OpenImagesByConceptMsg(source, concept, includeDescendants, size, p);
    }

    @Override
    public OpenImagesByConceptMsg withPageSize(int pageSize) {
        return new OpenImagesByConceptMsg(source, concept, includeDescendants, pageSize, 0);
    }
}
