package org.mbari.mondrian.services.pythia;

import org.junit.jupiter.api.Test;
import org.mbari.mondrian.domain.MachineLearningLocalization;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OkHttpMegalodonServiceTest {

    private static final String ENDPOINT = "http://perceptron.shore.mbari.org:8080/predictor";

    @Test
    void testPredict() throws IOException {
        // Load test image from resources
        var imageStream = getClass().getResourceAsStream("/images/MCC-00012.jpg");
        assertNotNull(imageStream, "Test image not found in resources");

        byte[] jpegBytes = imageStream.readAllBytes();
        assertTrue(jpegBytes.length > 0, "Test image should not be empty");

        // Create service with longer timeout for network call
        var service = new OkHttpMegalodonService(ENDPOINT, Duration.ofSeconds(60));

        // Call predict
        List<MachineLearningLocalization> predictions = service.predict(jpegBytes);

        // Verify we got results
        assertNotNull(predictions, "Predictions should not be null");

        // Log results for debugging
        System.out.println("Received " + predictions.size() + " predictions:");
        for (var prediction : predictions) {
            System.out.println("  - " + prediction.concept() + ": " + prediction.boundingBox());
        }
    }
}
