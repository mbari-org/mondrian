package org.mbari.mondrian.etc.gson;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class Json {
    public static Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

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
