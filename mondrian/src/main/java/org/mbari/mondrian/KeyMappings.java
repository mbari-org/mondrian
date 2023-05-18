package org.mbari.mondrian;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.mbari.mondrian.domain.Selection;
import org.mbari.mondrian.msg.messages.SetSelectedAnnotationsMsg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyMappings {

    private final ToolBox toolBox;
    private final Scene scene;

    public KeyMappings(ToolBox toolBox, Scene scene) {
        this.toolBox = toolBox;
        this.scene = scene;
        init();
    }

    private void init() {
        Map<KeyCodeCombination, Runnable> map = buildKeyMap();
        for (Map.Entry<KeyCodeCombination, Runnable> e : map.entrySet()) {
            scene.getAccelerators().put(e.getKey(), e.getValue());
        }
    }

    private Map<KeyCodeCombination, Runnable> buildKeyMap() {
        Map<KeyCodeCombination, Runnable> map = buildBaseKeyMap();
        return map;
    }

    private Map<KeyCodeCombination, Runnable> buildBaseKeyMap() {

        Map<KeyCodeCombination, Runnable> map = new HashMap<>();

        map.put(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
            var msg = new SetSelectedAnnotationsMsg(new Selection<>(KeyMappings.this, List.of()));
            toolBox.eventBus().publish(msg);
        });

        return map;
    }
}
