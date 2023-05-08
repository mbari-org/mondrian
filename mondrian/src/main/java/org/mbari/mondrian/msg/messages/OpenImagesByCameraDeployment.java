package org.mbari.mondrian.msg.messages;

public record OpenImagesByCameraDeployment(Object source,
                                           String cameraDeployment,
                                           int size,
                                           int page) implements Message, Paging<OpenImagesByCameraDeployment> {

    @Override
    public OpenImagesByCameraDeployment nextPage() {
        return new OpenImagesByCameraDeployment(source, cameraDeployment, size, page + 1);
    }

    @Override
    public OpenImagesByCameraDeployment previousPage() {
        var p = Math.max(page - 1, 0);
        return new OpenImagesByCameraDeployment(source, cameraDeployment, size, p);
    }
}
