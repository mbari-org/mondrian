package org.mbari.mondrian.services.vars;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.mbari.mondrian.Initializer;
import org.mbari.vars.core.crypto.AES;
import org.mbari.vars.core.util.StringUtils;
import org.mbari.vars.services.RemoteAuthException;
import org.mbari.vars.services.ConfigurationService;
import org.mbari.vars.services.RemoteRequestException;
import org.mbari.vars.services.gson.DurationConverter;
import org.mbari.vars.services.model.Authorization;
import org.mbari.vars.services.model.EndpointConfig;
import org.mbari.vars.services.model.HealthStatusCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class Raziel implements ConfigurationService {

    public record ConnectionParams(URL url, String username, String password) {

        public void write(Path file, AES aes) throws IOException {
            var s = url.toExternalForm() + "\n" + aes.encrypt(username) + "\n" + aes.encrypt(password);
            Files.writeString(file, s, StandardCharsets.UTF_8);
        }

        public static Optional<ConnectionParams> read(Path file, AES aes) {
            if (Files.exists(file)) {
                try {
                    var lines = Files.readAllLines(file);
                    var url = new URL(lines.get(0));
                    var username  = aes.decrypt(lines.get(1));
                    var password = aes.decrypt(lines.get(2));
                    return Optional.of(new ConnectionParams(url, username, password));
                }
                catch (Exception e) {
                    LoggerFactory
                            .getLogger(ConnectionParams.class)
                            .atWarn()
                            .log(() -> "The file at " + file + " does not contain valid connection info");
                    return Optional.empty();
                }
            }
            return Optional.empty();
        }

        public static Path path() {
            var settingsDirectory = Initializer.getSettingsDirectory();
            return settingsDirectory.resolve("raziel.txt");
        }

        public static Optional<ConnectionParams> load() {
            var path = path();
            if (Files.exists(path)) {
                return ConnectionParams.read(path, Initializer.getAes());
            }
            return Optional.empty();
        }
    }

    private final OkHttpClient client;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationConverter())
            .create();
    private final Logger log = LoggerFactory.getLogger(getClass());

    public Raziel() {
        var logger = new HttpLoggingInterceptor(log::debug);
        logger.setLevel(HttpLoggingInterceptor.Level.BODY);
        client = new OkHttpClient.Builder()
                .addInterceptor(logger)
                .build();
    }

    @Override
    public CompletableFuture<Authorization> authenticate(URL baseUrl, String username, String password) {
        var url = StringUtils.asUrl(baseUrl.toExternalForm() + "/auth")
                .orElseThrow(() -> new RemoteAuthException("Invalid base url: " + baseUrl));
        var request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", Credentials.basic(username, password))
                .addHeader("Accept", "application/json")
                .post(RequestBody.create(MediaType.parse("text/plain"), ""))
                .build();
        Function<String, Authorization> fn = (body) -> gson.fromJson(body, Authorization.class);
        return execRequest(request, fn).handle((auth, ex) -> {
            if (ex != null) {
                // Map all exception to RemoteAuthException
                throw new RemoteAuthException(ex);
            }
            else {
                return auth;
            }
        });
    }


    @Override
    public CompletableFuture<List<EndpointConfig>> endpoints(URL baseUrl, String jwt) {
        var url = StringUtils.asUrl(baseUrl.toExternalForm() + "/endpoints")
                .orElseThrow(() -> new RemoteRequestException("Invalid base url: " + baseUrl));
        var request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + jwt)
                .get()
                .build();
        Function<String, List<EndpointConfig>> fn = (body) -> {
            var array = gson.fromJson(body, EndpointConfig[].class);
            return Arrays.asList(array);
        };
        return execRequest(request, fn);
    }

    @Override
    public CompletableFuture<List<HealthStatusCheck>> healthStatus(URL baseUrl) {
        var url = StringUtils.asUrl(baseUrl.toExternalForm() + "/health/status")
                .orElseThrow(() -> new RemoteRequestException("Invalid base url: " + baseUrl));
        var request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .get()
                .build();
        Function<String, List<HealthStatusCheck>> fn = (body) -> {
            var array = gson.fromJson(body, HealthStatusCheck[].class);
            return Arrays.asList(array);
        };
        return execRequest(request, fn);
    }

    private <T> CompletableFuture<T> execRequest(Request request, Function<String, T> fn) {
        return CompletableFuture.supplyAsync(() -> {
            var responseCode = 200;
            T returnValue = null;
            try (var response = client.newCall(request).execute()) {
                responseCode = response.code();
                if (response.isSuccessful()) {
                    returnValue = fn.apply(response.body().string());
                }
            }
            catch (Exception e) {
                throw new RemoteRequestException(e);
            }
            if (returnValue == null) {
                var msg = String.format("No body was returned from %s. Response code %d",
                        request.url(), responseCode);
                throw new RemoteRequestException(msg);
            }
            return returnValue;
        });
    }

}
