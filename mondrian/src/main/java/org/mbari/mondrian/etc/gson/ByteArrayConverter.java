package org.mbari.mondrian.etc.gson;

import com.google.gson.*;
import org.mbari.mondrian.util.HexUtils;

import java.lang.reflect.Type;
import java.util.Base64;

/**
 * @author Brian Schlining
 * @since 2017-05-15T11:00:00
 */
public class ByteArrayConverter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {

    /**
     *
     * @param s
     * @return
     */
    public static byte[] decode(String s) {
        return HexUtils.parseHexBinary(s);
    }

    @Override
    public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        return decode(json.getAsString());
    }

    /**
     *
     * @param bs
     * @return
     */
    public static String encode(byte[] bs) {
        return HexUtils.printHexBinary(bs);
    }

    @Override
    public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(encode(src));
    }
}
