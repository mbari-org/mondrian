package org.mbari.mondrian.services.pythia;

import javafx.scene.image.ImageView;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.roi.Localization;
import org.mbari.imgfx.roi.RectangleView;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.domain.BoundingBox;
import org.mbari.mondrian.domain.MachineLearningLocalization;
import org.mbari.mondrian.domain.MachineLearningResponse;
import org.mbari.mondrian.etc.gson.Json;
import org.mbari.mondrian.javafx.settings.MLSettingsPaneController;
import org.mbari.mondrian.msg.commands.BulkCreateAnnotations;
import org.mbari.mondrian.util.ImageUtils;
import org.mbari.vars.core.util.Preconditions;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.services.model.Image;
import static org.mbari.mondrian.util.MathUtil.doubleToInt;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public class MLUtils {

    public static void analyze(ToolBox toolBox, Image image) {
        var mlRemoteUrlOpt = MLSettingsPaneController.getRemoteUrl();
        Preconditions.checkArgument(mlRemoteUrlOpt.isPresent(), "The URL for the machine learning web service was not set");
        var mlService = new OkHttpMegalodonService(mlRemoteUrlOpt.get());
        var data = toolBox.data();
        try {
            var jpegBytes = readJpegBytes(image.getUrl());
            var mlLocs = mlService.predict(jpegBytes);
            var mlResponse = new MachineLearningResponse(image, mlLocs);
            var annotations = mlResponse.toAnnotation(data.getUser().getUsername(),
                    data.getGroup(), data.getActivity());
            var msg = new BulkCreateAnnotations(annotations);
            toolBox.eventBus().publish(msg);
        }
        catch (Exception e) {

        }

    }


    private static byte[] readJpegBytes(URL url) throws IOException {
        var bufferedImage = ImageIO.read(url);
        return ImageUtils.toJpegByteArray(bufferedImage);
    }

    public static Optional<Localization<RectangleView, ImageView>> toLocalization(MachineLearningLocalization ml,
                                                                                  AutoscalePaneController<ImageView> autoscale) {
        var b = ml.boundingBox();
        var view = RectangleView.fromImageCoords((double) b.getX(),
                (double) b.getY(),
                (double) b.getWidth(),
                (double) b.getHeight(),
                autoscale.getAutoscale());
        return view.map(v -> new Localization<>(v, autoscale, ml.concept()));
    }

    public static Optional<Annotation> toAnnotation(String observer,
                                                    String group,
                                                    String activity,
                                                    Image image,
                                                    Localization<RectangleView, ImageView> localization) {

        if (localization.isVisible()) {
            // Build association that defines bounding box
            final var data = localization.getDataView().getData();
            final var x = doubleToInt(data.getX());
            final var y = doubleToInt(data.getY());
            final var width = doubleToInt(data.getWidth());
            final var height = doubleToInt(data.getHeight());
            final var boundingBox = new BoundingBox(x, y, width, height, image.getImageReferenceUuid());
            final var json = Json.stringify(boundingBox);
            final var association = new Association(BoundingBox.LINK_NAME, Association.VALUE_SELF, json, "application/json");

            // Build annotation
            final var concept = localization.labelProperty().get();
            final var annotation = new Annotation(concept, observer, image.getVideoIndex(), image.getVideoReferenceUuid());
            annotation.setAssociations(List.of(association));
            annotation.setGroup(group);
            annotation.setActivity(activity);
            return Optional.of(annotation);
        }
        return Optional.empty();

    }
}
