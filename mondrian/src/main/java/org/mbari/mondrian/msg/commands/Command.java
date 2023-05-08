package org.mbari.mondrian.msg.commands;

import org.mbari.mondrian.ToolBox;
import org.mbari.vars.core.EventBus;

/**
 * Encapsulates some bit of executable code. Apply is used to execute, unapply should undo
 * everything. These commands are pushed onto the {@link EventBus}
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