package org.mbari.mondrian.domain;

import java.util.UUID;

public class AuxInfo {
    private UUID imageReferenceUuuid;
    private String comment;

    public AuxInfo() {
    }

    public AuxInfo(UUID imageReferenceUuuid, String comment) {
        this.imageReferenceUuuid = imageReferenceUuuid;
        this.comment = comment;
    }

    public UUID getImageReferenceUuuid() {
        return imageReferenceUuuid;
    }

    public void setImageReferenceUuuid(UUID imageReferenceUuuid) {
        this.imageReferenceUuuid = imageReferenceUuuid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
