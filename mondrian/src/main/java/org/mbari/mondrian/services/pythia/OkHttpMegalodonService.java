package org.mbari.mondrian.services.pythia;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.mbari.mondrian.domain.MachineLearningLocalization;
import org.mbari.mondrian.domain.MachineLearningResponse1;
import org.mbari.mondrian.etc.jdk.Logging;
import org.mbari.mondrian.services.MLPredictionService;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class OkHttpMegalodonService implements MLPredictionService {

    private final String endpoint;
    private final OkHttpClient client;
    private final MediaType jpegType = MediaType.parse("image/jpg");
    private static final Logging log = new Logging(OkHttpMegalodonService.class);
    private static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public OkHttpMegalodonService(String endpoint) {
        this(endpoint, Duration.ofSeconds(30));
    }

    public OkHttpMegalodonService(String endpoint, Duration timeout) {
        this.endpoint = endpoint;
        client = new OkHttpClient()
                .newBuilder()
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .writeTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .addInterceptor(new HttpLoggingInterceptor())
                .build();
    }

    @Override
    public List<MachineLearningLocalization> predict(byte[] jpegBytes) {
        var requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model_type", "image_queue_yolov5")
                .addFormDataPart("file", UUID.randomUUID() + ".jpg",
                        RequestBody.create(jpegType, jpegBytes))
                .build();

        var request = new Request.Builder()
                .header("Accept", "application/json")
                .url(endpoint)
                .post(requestBody)
                .build();

        try (var response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            var body = response.body().string();
            var prediction = gson.fromJson(body, MachineLearningResponse1.class);
            return prediction.toMLStandard();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
