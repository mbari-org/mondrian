package org.mbari.mondrian.msg.commands;

import org.mbari.mondrian.ToolBox;

/**
 * Encapsulates some bit of executable code. Apply is used to execute, unapply should undo
 * everything. These commands are pushed onto the {@link org.mbari.mondrian.etc.rxjava.EventBus}
 * and picked up and executed by the {@link CommandManager}.
 *
 * @author Brian Schlining
 * @since 2017-05-10T10:07:00
 */
public interface Command {

    void apply(ToolBox toolBox);

    void unapply(ToolBox toolBox);

    String getDescription();

}