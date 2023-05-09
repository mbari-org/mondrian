package org.mbari.mondrian.javafx.controls;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import org.mbari.imgfx.etc.rx.EventBus;
import org.mbari.mondrian.domain.Page;
import org.mbari.mondrian.msg.messages.ImageSet;
import org.mbari.mondrian.msg.messages.SetImagesMsg;

public class PageLabelController {
    private Label label;
    private final EventBus eventBus;
    private StringProperty imageSetDescription = new SimpleStringProperty();
    private StringProperty pageText = new SimpleStringProperty();

    public PageLabelController(EventBus eventBus) {
        this.eventBus = eventBus;
        init();
    }

    private void init() {
        var rx = eventBus.toObserverable();
        rx.ofType(ImageSet.class)
                .subscribe(msg -> Platform.runLater(() -> imageSetDescription.setValue(msg.description())));
        rx.ofType(SetImagesMsg.class)
                .subscribe(msg -> Platform.runLater(() -> pageText.setValue(pageToText(msg.selection().selected()))));

        var binding = Bindings.createStringBinding(() -> imageSetDescription.getValueSafe() + " - " + pageText.getValueSafe(),
                imageSetDescription, pageText);
        label = new Label();
        label.textProperty().bind(binding);
    }

    private String pageToText(Page<?> page) {
        var totalPages = (int) Math.ceil(page.totalSize() / (double) page.size());
        return "page " + page.page() + " of " + totalPages + " (" + page.size() + " images shown)";
    }

    public Label getLabel() {
        return label;
    }
}
