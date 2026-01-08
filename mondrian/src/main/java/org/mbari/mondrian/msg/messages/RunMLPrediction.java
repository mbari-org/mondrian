package org.mbari.mondrian.msg.messages;


import org.mbari.vars.annosaurus.sdk.r1.models.Image;

public record RunMLPrediction(Image image, byte[] jpegBytes) implements Message {

}
