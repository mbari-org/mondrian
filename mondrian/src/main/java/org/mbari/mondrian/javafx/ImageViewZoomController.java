package org.mbari.mondrian.javafx;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.mbari.imgfx.Autoscale;
import org.mbari.mondrian.util.JFXUtilities;

/**
 * <pre>
 *     <code>
 *         Autoscale&lt;ImageView&gt; autoscale = ....
 *         var controller = new ImageViewZoomController(autoscale)
 *         var controller.setZoom(10.0);
 *     </code>
 * </pre>
 */
public class ImageViewZoomController {

    private ImageView imageView;
    private final Autoscale<ImageView> originalAutoscale;
    private DoubleProperty zoom = new SimpleDoubleProperty(8.0);

    public ImageViewZoomController(Autoscale<ImageView> originalAutoscale) {
        this.originalAutoscale = originalAutoscale;
        init();
    }

    public ImageView getImageView() {
        return imageView;
    }

    private void init() {
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
//        originalAutoscale.getView().addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
        EventHandler<? super MouseEvent> eventHandler = event -> {
            var image = imageView.getImage();
            if (image != null) {
                var x = event.getSceneX();
                var y = event.getSceneY();
                var xy = new Point2D(x, y);
                var imageXy = originalAutoscale.sceneToUnscaled(xy);
                if (imageXy.getX() > -1 && imageXy.getX() < image.getWidth() && imageXy.getY() > -1 && imageXy.getY() < image.getHeight()) {
                    var portWidth = image.getWidth() / zoom.get();
                    var portHeight = image.getHeight() / zoom.get();
                    var dim = Math.max(portWidth, portHeight);
                    var magX = imageXy.getX() - portWidth / 2D;
                    var magY = imageXy.getY() - portHeight / 2D;
                    var viewPort = new Rectangle2D(magX, magY, dim, dim);
                    imageView.setViewport(viewPort);
                }
            }
        };
        var view = originalAutoscale.getView();

        view.sceneProperty()
                .addListener((obs, oldv, newv) -> {
            if (oldv != null) {
                oldv.removeEventHandler(MouseEvent.MOUSE_MOVED, eventHandler);
            }
            if (newv != null) {
                newv.addEventHandler(MouseEvent.MOUSE_MOVED, eventHandler);
            }
        });

//        originalAutoscale.getView()
//                .getScene()
//                .addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
//            var image = imageView.getImage();
//            if (image != null) {
//                var x = event.getSceneX();
//                var y = event.getSceneY();
//                var xy = new Point2D(x, y);
//                var imageXy = originalAutoscale.sceneToUnscaled(xy);
//                if (imageXy.getX() > -1 && imageXy.getX() < image.getWidth() && imageXy.getY() > -1 && imageXy.getY() < image.getHeight()) {
//                    var portWidth = image.getWidth() / zoom.get();
//                    var portHeight = image.getHeight() / zoom.get();
//                    var dim = Math.max(portWidth, portHeight);
//                    var magX = imageXy.getX() - portWidth / 2D;
//                    var magY = imageXy.getY() - portHeight / 2D;
//                    var viewPort = new Rectangle2D(magX, magY, dim, dim);
//                    imageView.setViewport(viewPort);
//                }
//            }
//        });
    }

    public double getZoom() {
        return zoom.get();
    }

    public DoubleProperty zoomProperty() {
        return zoom;
    }

    public void setZoom(double zoom) {
        this.zoom.set(zoom);
    }
}
