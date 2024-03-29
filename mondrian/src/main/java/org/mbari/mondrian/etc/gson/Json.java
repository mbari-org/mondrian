package org.mbari.mondrian.etc.gson;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.mbari.vars.services.gson.*;
import org.mbari.vars.services.model.ImagedMoment;
import org.mbari.vcr4j.time.Timecode;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class Json {
    public static Gson GSON = newGson();

    private static Gson newGson() {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .registerTypeAdapter(ImagedMoment.class, new AnnotationCreator())
                .registerTypeAdapter(Duration.class, new DurationConverter())
                .registerTypeAdapter(Timecode.class, new TimecodeConverter())
                .registerTypeAdapter(Instant.class, new InstantConverter())
                .registerTypeAdapter(byte[].class, new ByteArrayConverter());

        return gsonBuilder.create();

    }



    public static <T>  String stringify(T obj) {
        return GSON.toJson(obj);
    }

    public static <T> T decode(String s, Class<T> clazz) {
        return  GSON.fromJson(s, clazz);
    }

    public static <T> List<T> decodeArray(String s, Class<T> clazz) {
        var typeOf = TypeToken.getParameterized(List.class, clazz).getType();
        return GSON.fromJson(s, typeOf);
    }
}
