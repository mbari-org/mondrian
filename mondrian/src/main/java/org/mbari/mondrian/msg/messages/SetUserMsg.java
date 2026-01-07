package org.mbari.mondrian.msg.messages;


import org.mbari.vars.oni.sdk.r1.models.User;

public record SetUserMsg(User user) implements Message {
}
