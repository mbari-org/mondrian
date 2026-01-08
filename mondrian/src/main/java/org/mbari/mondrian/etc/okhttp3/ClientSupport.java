package org.mbari.mondrian.etc.okhttp3;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import org.mbari.mondrian.etc.jdk.Logging;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ClientSupport {
    private final OkHttpClient client;
    private static final Logging log = new Logging(ClientSupport.class);

    public ClientSupport() {
        var debugLog = log.atDebug();
        var logger = new HttpLoggingInterceptor(debugLog::log);
        logger.setLevel(HttpLoggingInterceptor.Level.BODY);
        client = new OkHttpClient.Builder()
                .addInterceptor(logger)
                .build();
    }

    public OkHttpClient getClient() {
        return client;
    }

    public <T> CompletableFuture<T> execRequest(Request request, Function<String, T> fn) {
        return CompletableFuture.supplyAsync(() -> {
            var responseCode = 200;
            T returnValue = null;
            try (var response = client.newCall(request).execute()) {
                responseCode = response.code();
                if (response.isSuccessful()) {
                    var body = response.body();
                    if (body != null) {
                        returnValue = fn.apply(body.string());
                    }
                }
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (returnValue == null) {
                var msg = String.format("No body was returned from %s. Response code %d",
                        request.url(), responseCode);
                throw new RuntimeException(msg);
            }
            return returnValue;
        });
    }
}
