package org.mbari.mondrian.msg.messages;

public record OpenImagesByCameraDeployment(Object source,
                                           String cameraDeployment,
                                           int size,
                                           int page) {
}
