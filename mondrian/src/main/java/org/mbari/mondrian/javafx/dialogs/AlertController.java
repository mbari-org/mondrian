package org.mbari.mondrian.javafx.dialogs;


import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.mbari.mondrian.ToolBox;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-06-12T11:42:00
 */
public class AlertController {

    private final Alert alert ;
    private GridPane content;
    private TextArea textArea;
    private final ResourceBundle i18n;

    public AlertController(Alert.AlertType alertType,
                           ResourceBundle i18n,
                           List<String> stylesheets) {
        this.alert = new Alert(alertType);
        this.i18n = i18n;
        alert.getDialogPane().getStylesheets().addAll(stylesheets);
    }

    public void showAndWait(String title, String headerText, String content) {
        showAndWait(title, headerText, content, null);
    }

    public void showAndWait(String title, String headerText, String contentText, Exception ex) {
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        if (ex != null) {
            alert.getDialogPane().setExpandableContent(getContent(ex));
        }
        else {
            alert.getDialogPane().setExpandableContent(null);
        }
        alert.showAndWait();
    }

    private GridPane getContent(Exception ex) {
        if (content == null) {
            // TODO i18n
            Label label = new Label(i18n.getString("alert.stacktrace.msg"));

            textArea = new TextArea();
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            content = new GridPane();
            content.setMaxWidth(Double.MAX_VALUE);
            content.add(label, 0, 0);
            content.add(textArea, 0 , 1);

        }
        // Make pretty stack trace string
        textArea.setText(null);
        if (ex != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            String exceptionText = sw.toString();
            textArea.setText(exceptionText);
        }
        return content;
    }



}

