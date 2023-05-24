package org.mbari.mondrian.javafx.settings;


import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.util.IPrefs;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Brian Schlining
 * @since 2017-08-08T16:25:00
 */
public class SettingsPaneController implements IPrefs {

    private TabPane root;
    private final ToolBox toolBox;
    private final List<IPrefs> prefs = new ArrayList<>();

    public SettingsPaneController(ToolBox toolBox) {
        this.toolBox = toolBox;
    }

    public TabPane getRoot() {
        if (root == null) {
            root = new TabPane();
            root.setPrefSize(600, 600);
            loadPanes();
        }
        return root;
    }

    private void loadPanes() {
        // This is the list of Settings panes to display
        var settingsPanes = List.of(RazielSettingsPaneController.newInstance(),
                GeneralSettingsPaneController.newInstance(), MLSettingsPaneController.newInstance());
        settingsPanes.forEach(controller -> {
            var tab = new Tab(controller.getName());
            tab.setClosable(false);
            tab.setContent(controller.getPane());
            root.getTabs().add(tab);
            prefs.add(controller);
        });
    }



    /**
     * Loads prefs for each tab
     */
    @Override
    public void load() {
        getRoot(); // Loads preferences panes
        prefs.forEach(IPrefs::load);
    }

    /**
     * Saves prefs for each time
     */
    @Override
    public void save() {
        getRoot(); // Loads preferences panes
        prefs.forEach(IPrefs::save);
    }

}

