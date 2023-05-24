module org.mbari.mondrian {
    requires com.google.gson;
    requires io.reactivex.rxjava3;
    requires java.desktop;
    requires java.prefs;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.material2;
    requires org.mbari.imgfx;
    requires org.mbari.jcommons;
    requires org.mbari.vars.core;
    requires org.mbari.vars.services;
    requires vcr4j.core;

    opens org.mbari.mondrian to javafx.graphics;
    opens org.mbari.mondrian.domain to com.google.gson;
    opens org.mbari.mondrian.javafx.settings to javafx.fxml;

}