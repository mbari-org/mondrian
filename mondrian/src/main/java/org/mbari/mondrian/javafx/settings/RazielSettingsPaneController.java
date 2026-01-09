package org.mbari.mondrian.javafx.settings;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.mbari.mondrian.Initializer;
import org.mbari.mondrian.etc.jdk.Logging;
import org.mbari.mondrian.msg.messages.ReloadMsg;
import org.mbari.mondrian.services.vars.Raziel;
import org.mbari.mondrian.util.FXMLUtils;
import org.mbari.mondrian.util.JFXUtils;
import org.mbari.vars.raziel.sdk.r1.RazielKiotaClient;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Comparator;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class RazielSettingsPaneController implements SettingsPane {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private VBox endpointStatusPane;

    @FXML
    private PasswordField passwordTextfield;

    @FXML
    private GridPane root;

    @FXML
    private Button testButton;

    @FXML
    private TextField urlTextfield;

    @FXML
    private TextField usernameTextfield;

    @FXML
    private Label msgLabel;

    private String name;
    private final Logging log = new Logging(getClass());

    @FXML
    void initialize() {

        name = resources.getString("settings.raziel.name");

        // Enable/disable test button
        usernameTextfield.textProperty().addListener((obs, oldv, newv) -> checkEnable());
        urlTextfield.textProperty().addListener((obs, oldv, newv) -> checkEnable());
        passwordTextfield.textProperty().addListener((obs, oldv, newv) -> checkEnable());

        testButton.setOnAction(event -> test());

        JFXUtils.attractAttention(testButton);
    }

    private Optional<Raziel.ConnectionParams> parseRazielConnectionParams() {
        try {
            var urlText = urlTextfield.getText();
            var userText = usernameTextfield.getText();
            var pwdText = passwordTextfield.getText();
            var ok = urlText != null && userText != null && pwdText != null &&
                    urlText.length() > 0 && userText.length() > 0 && pwdText.length() > 0;
            if (ok) {
//                if (!urlText.startsWith("http://")) {
//                    urlText = "http://" + urlText;
//                }
                URL url = URI.create(urlText).toURL();
                var rcp = new Raziel.ConnectionParams(url, userText, pwdText);
                return Optional.of(rcp);
            }
        }
        catch (Exception e) {
            log.atDebug().withCause(e).log(() -> "Failed to parse connection params from the UI fields");
            // Do nothing
        }
        return Optional.empty();
    }

    private void checkEnable() {
        var opt = parseRazielConnectionParams();
        testButton.setDisable(opt.isEmpty());
    }

    private void test() {
        endpointStatusPane.getChildren().clear();
        msgLabel.setText(resources.getString("settings.raziel.pane.msg.starting"));
        var opt = parseRazielConnectionParams();
        if (opt.isEmpty()) {
            var msg = resources.getString("settings.raziel.pane.msg.invalidparams");
            log.atDebug().log("Invalid raziel connection params");
            Platform.runLater(() -> msgLabel.setText(msg));
            return;
        }
        var rcp = opt.get();
        try {
            var service = new RazielKiotaClient(rcp.url().toURI());
            service.checkStatus(rcp.username(), rcp.password())
                    .handle((statuses, ex) -> {
                        if (ex != null) {
                            var s = resources.getString("settings.raziel.pane.msg.authfailed");
                            Platform.runLater(() -> msgLabel.setText(s));
                            log.atDebug()
                                    .withCause(ex)
                                    .log("An exception occurred while running text against Raziel at" + rcp.url());
                        } else {
                            var sortedStatuses = statuses.stream()
                                    .sorted(Comparator.comparing(es -> es.endpointConfig().name()))
                                    .collect(Collectors.toList());
                            var panes = EndpointStatusPaneController.from(sortedStatuses)
                                    .stream()
                                    .map(EndpointStatusPaneController::getRoot)
                                    .toList();
                            Platform.runLater(() -> {
                                msgLabel.setText(null);
                                endpointStatusPane.getChildren().addAll(panes);
                            });

                        }
                        return null;
                    });
        }
        catch (Exception e) {
            var s = resources.getString("settings.raziel.pane.msg.authfailed");
            Platform.runLater(() -> msgLabel.setText(s));
            log.atDebug().withCause(e).log(() -> "Failed to test raziel connection params");
        }

    }


    @Override
    public void load() {
        Raziel.ConnectionParams
                .load()
                .ifPresent(rcp -> {
                    urlTextfield.setText(rcp.url().toExternalForm());
                    usernameTextfield.setText(rcp.username());
                    passwordTextfield.setText(rcp.password());
                    checkEnable();
                });
    }

    @Override
    public void save() {
        parseRazielConnectionParams().ifPresent(rcp -> {
            var toolbox = Initializer.getToolBox();
            var path = Raziel.ConnectionParams.path();
            var aes = Initializer.getToolBox().aes();
            try {
                rcp.write(path, aes);
                // The AppController will listen for the ReloadMsg and
                // will read the raziel params and refresh the services.
                toolbox.eventBus().publish(new ReloadMsg());
            } catch (IOException e) {
                Platform.runLater(() -> msgLabel.setText("Failed to save connection params"));
                log.atWarn()
                        .withCause(e)
                        .log("Failed to save raziel connection parameters");
            }
        });
        endpointStatusPane.getChildren().clear();

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Pane getPane() {
        return root;
    }

    public static RazielSettingsPaneController newInstance() {
        var i18n = Initializer.getToolBox().i18n();
        return FXMLUtils.newInstance(RazielSettingsPaneController.class,
                "/fxml/RazielSettingsPane.fxml",
                i18n);

    }
}

