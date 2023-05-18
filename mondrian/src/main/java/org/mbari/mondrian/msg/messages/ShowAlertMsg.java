package org.mbari.mondrian.msg.messages;

import javafx.scene.control.Alert;
import org.mbari.mondrian.javafx.dialogs.AlertContent;

public record ShowAlertMsg(Alert.AlertType alertType, String title, String header, String content, Throwable exception) {

    public ShowAlertMsg(Alert.AlertType alertType, String title, String header, String content) {
        this(alertType, title, header, content, null);
    }

    public ShowAlertMsg(Alert.AlertType alertType, AlertContent alertContent) {
        this(alertType, alertContent.title(), alertContent.header(), alertContent.content());
    }

    public ShowAlertMsg(Alert.AlertType alertType, AlertContent alertContent, Throwable ex) {
        this(alertType, alertContent.title(), alertContent.header(), alertContent.content(), ex);
    }

}
