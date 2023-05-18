package org.mbari.mondrian.javafx.dialogs;

import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public record AlertContent(String title, String header, String content) {

    public AlertContent(ResourceBundle i18n, String key, Throwable ex) {
        this(i18n.getString(key + ".title"),
                i18n.getString(key + ".header"),
                contentFromException(ex));
    }

    public AlertContent(ResourceBundle i18n, String key) {
        this(i18n.getString(key + ".title"),
                i18n.getString(key + ".header"),
                i18n.getString(key + ".content"));
    }

    private static String contentFromException(Throwable ex) {
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

    public AlertContent copy(String newContent) {
        return new AlertContent(title, header, newContent);
    }
}
