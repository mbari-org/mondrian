package org.mbari.mondrian.etc.gson;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mbari.jcommons.util.IOUtil;
import org.mbari.vars.services.model.Image;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class JsonTest {

    @Test
    public void testImageDecode() {
        var url = getClass().getResource("/data/json/image_sample.json");
        try(var in = url.openStream()) {
            var bytes = IOUtil.toByteArray(in);
            var s = new String(bytes, StandardCharsets.UTF_8);
            var images = Json.decodeArray(s, Image.class);
            assertEquals(10, images.size());
        } catch (IOException e) {
            fail(e);
        }

    }
}
