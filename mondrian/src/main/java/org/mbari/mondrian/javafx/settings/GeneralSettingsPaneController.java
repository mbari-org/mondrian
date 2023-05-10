package org.mbari.mondrian.javafx.settings;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.util.converter.IntegerStringConverter;
import org.mbari.mondrian.Initializer;
import org.mbari.mondrian.msg.messages.SetPageSizeMsg;
import org.mbari.mondrian.util.FXMLUtils;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.prefs.Preferences;

public class GeneralSettingsPaneController implements SettingsPane {

    public record Settings(int pageSize) {}

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField pageSizeTextField;

    @FXML
    private GridPane pane;

    private String name;
    public static final String KEY_PAGE_SIZE = "pageSize";
    private static final int DEFAULT_PAGE_SIZE = 200;

    @FXML
    void initialize() {
        name = resources.getString("settings.general.name");

        // Only allow digits
        // https://stackoverflow.com/questions/40472668/numeric-textfield-for-integers-in-javafx-8-with-textformatter-and-or-unaryoperat
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("-?([1-9][0-9]*)?")) {
                return change;
            }
            return null;
        };
        pageSizeTextField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0, integerFilter));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Pane getPane() {
        return pane;
    }

    public static Settings loadSettings() {
        var prefs = Preferences.userNodeForPackage(GeneralSettingsPaneController.class);
        var pageSize = prefs.getInt(KEY_PAGE_SIZE, DEFAULT_PAGE_SIZE);
        return new Settings(pageSize);
    }

    @Override
    public void load() {
        var settings = loadSettings();
        pageSizeTextField.setText(String.valueOf(settings.pageSize()));
    }

    @Override
    public void save() {
        var prefs = Preferences.userNodeForPackage(getClass());
        var pageSize = DEFAULT_PAGE_SIZE;
        try {
            pageSize = Integer.parseInt(pageSizeTextField.getText());
        }
        catch (Exception e) {
            // TODO log exception
        }
        prefs.putInt(KEY_PAGE_SIZE, pageSize);
        Initializer.getToolBox()
                .eventBus()
                .publish(new SetPageSizeMsg(pageSize));
    }

    public static GeneralSettingsPaneController newInstance() {
        var i18n = Initializer.getToolBox().i18n();
        return FXMLUtils.newInstance(GeneralSettingsPaneController.class,
                "/fxml/GeneralSettingsPane.fxml",
                i18n);

    }
}
