package org.mbari.mondrian.msg.messages;

import org.mbari.vars.services.model.User;

public record SetUserMsg(User user) implements Message {
}
