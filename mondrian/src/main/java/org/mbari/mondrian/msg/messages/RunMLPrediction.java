package org.mbari.mondrian.msg.messages;

import org.mbari.vars.services.model.Image;


public record RunMLPrediction(Image image, byte[] jpegBytes) implements Message {

}
