package org.mbari.mondrian.javafx.controls;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import org.mbari.imgfx.etc.rx.EventBus;
import org.mbari.mondrian.javafx.Icons;
import org.mbari.mondrian.msg.messages.OpenNextPageMsg;
import org.mbari.mondrian.msg.messages.OpenPreviousPageMsg;

public class PagerPaneController {

    private final EventBus eventBus;
    private HBox hBox;

    public PagerPaneController(EventBus eventBus) {
        this.eventBus = eventBus;
        init();
    }

    private void init() {
        var previousIcon = Icons.NAVIGATE_BEFORE.standardSize();
        var previousButton = new Button(null, previousIcon);
        previousButton.setOnAction(evt -> eventBus.publish(new OpenPreviousPageMsg()));

        var nextIcon = Icons.NAVIGATE_NEXT.standardSize();
        var nextButton = new Button(null, nextIcon);
        nextButton.setOnAction(evt -> eventBus.publish(new OpenNextPageMsg()));
        hBox = new HBox(previousButton, nextButton);
    }

    public HBox getPane() {
        return hBox;
    }
}
