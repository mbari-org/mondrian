package org.mbari.mondrian.javafx;

import javafx.scene.shape.Shape;
import org.mbari.imgfx.roi.Data;
import org.mbari.imgfx.roi.DataView;

import java.util.Map;
import java.util.Optional;

public class RoiTranslatorFactory {

    private RoiTranslatorFactory() {
        // No instantiation.
    }

    private static final Map<String, RoiTranslator<? extends DataView<? extends Data, ? extends Shape>>> LINK_VALUES_TO_ROI_MAP = Map.of(
            RoiTranslatorBoundingBox.LINK_NAME, new RoiTranslatorBoundingBox(),
            RoiTranslatorLine.LINK_NAME, new RoiTranslatorLine(),
            RoiTranslatorMarker.LINK_NAME, new RoiTranslatorMarker(),
            RoiTranslatorPolygon.LINK_NAME, new RoiTranslatorPolygon()
    );

    public static Optional<RoiTranslator<? extends DataView<? extends Data, ? extends Shape>>> translatorFor(String linkName) {
        if (LINK_VALUES_TO_ROI_MAP.containsKey(linkName)) {
            return Optional.of(LINK_VALUES_TO_ROI_MAP.get(linkName));
        }
        else {
            return Optional.empty();
        }
    }


}
