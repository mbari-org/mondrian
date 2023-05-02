package org.mbari.mondrian.javafx;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.mbari.imgfx.Autoscale;

public class ImageViewZoomController {

    private ImageView imageView;
    private final Autoscale<ImageView> originalAutoscale;
    private volatile double zoom = 8.0;

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
        originalAutoscale.getView().addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
            var image = imageView.getImage();
            if (image != null) {
                var x = event.getSceneX();
                var y = event.getSceneY();
                var xy = new Point2D(x, y);
                var imageXy = originalAutoscale.sceneToUnscaled(xy);
                var portWidth = image.getWidth() / zoom;
                var portHeight = image.getHeight() / zoom;
                var magX = imageXy.getX() - portWidth / 2D;
                var magY = imageXy.getY() - portHeight / 2D;
                var viewPort = new Rectangle2D(magX, magY, portWidth, portHeight);
                imageView.setViewport(viewPort);
            }
        });
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }
}
