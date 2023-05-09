package org.mbari.mondrian.javafx.dialogs;

import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public record AlertContent(String title, String header, String content) {

    public AlertContent(ResourceBundle i18n, String key, Exception ex) {
        this(i18n.getString(key + ".title"),
                i18n.getString(key + ".header"),
                contentFromException(ex));
    }

    private static String contentFromException(Exception ex) {
        var causes = new ArrayList<Throwable>();
        causes.add(ex);
        Throwable cause = ex.getCause();
        while (cause != null) {
            causes.add(cause);
            cause = cause.getCause();
        }
        String cs = causes.stream()
                .map(t -> t.getClass().getName())
                .collect(Collectors.joining(" -> "));
        return ex.getMessage() + " (" + cs + ")";
    }
}
