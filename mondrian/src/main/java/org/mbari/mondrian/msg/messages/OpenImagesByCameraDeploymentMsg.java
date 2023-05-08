package org.mbari.mondrian.msg.messages;

public record OpenImagesByCameraDeploymentMsg(Object source,
                                              String cameraDeployment,
                                              int size,
                                              int page) implements Message, Paging<OpenImagesByCameraDeploymentMsg> {

    @Override
    public OpenImagesByCameraDeploymentMsg nextPage() {
        return new OpenImagesByCameraDeploymentMsg(source, cameraDeployment, size, page + 1);
    }

    @Override
    public OpenImagesByCameraDeploymentMsg previousPage() {
        var p = Math.max(page - 1, 0);
        return new OpenImagesByCameraDeploymentMsg(source, cameraDeployment, size, p);
    }

    @Override
    public OpenImagesByCameraDeploymentMsg withPageSize(int newSize) {
        return new OpenImagesByCameraDeploymentMsg(source, cameraDeployment, newSize, 0);
    }
}
