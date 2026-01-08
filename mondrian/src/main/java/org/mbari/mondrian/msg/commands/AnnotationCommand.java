package org.mbari.mondrian.msg.commands;

import javafx.scene.control.Alert;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.msg.messages.ShowAlertMsg;
import org.mbari.mondrian.msg.messages.UpdateAnnotationInViewMsg;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;


public interface AnnotationCommand extends Command {

    default void handleUpdatedAnnotation(ToolBox toolBox, Annotation annotation, String errorMsg) {
        if (annotation == null) {
            var msg = new ShowAlertMsg(Alert.AlertType.WARNING, "Mondrian",
                    "Oops",
                    errorMsg);
            toolBox.eventBus().publish(msg);
        }
        else {
            var msg = new UpdateAnnotationInViewMsg(annotation);
            toolBox.eventBus().publish(msg);
        }
    }
}
