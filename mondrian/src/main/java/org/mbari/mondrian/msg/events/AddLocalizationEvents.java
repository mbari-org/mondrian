package org.mbari.mondrian.msg.events;

import javafx.scene.image.ImageView;
import javafx.scene.shape.Shape;
import org.mbari.imgfx.etc.rx.events.*;
import org.mbari.imgfx.roi.*;

import java.util.Optional;

public class AddLocalizationEvents {

    public static Optional<AddLocalizationEvent<? extends DataView<? extends Data, ? extends Shape>, ImageView>> from(Localization<? , ImageView> localization, boolean isNew) {
        var dataView = localization.getDataView();
        if (dataView instanceof MarkerView) {
            return Optional.of(new AddMarkerEvent<>((Localization<MarkerView, ImageView>) localization, isNew));
        }
        else if (dataView instanceof LineView) {
            return Optional.of(new AddLineEvent<>((Localization<LineView, ImageView>) localization));
        }
        else if (dataView instanceof RectangleView) {
            return Optional.of(new AddRectangleEvent<>((Localization<RectangleView, ImageView>) localization));
        }
        else if (dataView instanceof PolygonView) {
            return Optional.of(new AddPolygonEvent<>((Localization<PolygonView, ImageView>) localization));
        }
        else {
            return Optional.empty();
        }
    }


}
