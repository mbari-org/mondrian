package org.mbari.mondrian.msg.messages;

import javafx.scene.Node;
import javafx.scene.shape.Shape;
import org.mbari.imgfx.roi.Data;
import org.mbari.imgfx.roi.DataView;
import org.mbari.imgfx.roi.Localization;
import org.mbari.mondrian.domain.Selection;

import java.util.Collection;

public record SetSelectedLocalizationsMsg(Selection<Collection<Localization<? extends DataView<? extends Data, ? extends Shape>, ? extends Node>>> selection) {

    public Collection<Localization<? extends DataView<? extends Data, ? extends Shape>, ? extends Node>> localizations() {
        return selection.selected();
    }
}
