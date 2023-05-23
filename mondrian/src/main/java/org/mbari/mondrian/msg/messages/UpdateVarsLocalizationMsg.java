package org.mbari.mondrian.msg.messages;

import org.mbari.mondrian.domain.VarsLocalization;

import java.util.List;

public record UpdateVarsLocalizationMsg(VarsLocalization varsLocalization) implements Message {


}
