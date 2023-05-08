package org.mbari.mondrian.util;

import org.junit.jupiter.api.Test;
import org.mbari.mondrian.domain.Counter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class FetchRangeTest {

    @Test
    public void fromLimitOffsetTest1() {
        var counts = List.of(10L, 10L, 10L, 10L, 10L);
        var limit = 5;
        var offset = 14;
        var fetchRanges = FetchRange.fromLimitOffset(counts, limit ,offset);
        assertEquals(1, fetchRanges.size());
        assertEquals(4, fetchRanges.get(0).offset());
        assertEquals(5, fetchRanges.get(0).limit());
    }

    @Test
    public void fromLimitOffsetTest2() {
        var counts = List.of(10L, 10L, 10L, 10L, 10L);
        var limit = 7;
        var offset = 14;
        var fetchRanges = FetchRange.fromLimitOffset(counts, limit ,offset);
        assertEquals(2, fetchRanges.size());
        assertEquals(4, fetchRanges.get(0).offset());
        assertEquals(6, fetchRanges.get(0).limit());
        assertEquals(0, fetchRanges.get(1).offset());
        assertEquals(1, fetchRanges.get(1).limit());
    }

    @Test
    public void fromLimitOffsetTest3() {
        var counts = List.of(10L, 10L, 10L, 10L, 10L);
        var limit = 21;
        var offset = 14;
        var fetchRanges = FetchRange.fromLimitOffset(counts, limit ,offset);
        assertEquals(3, fetchRanges.size());
        assertEquals(4, fetchRanges.get(0).offset());
        assertEquals(6, fetchRanges.get(0).limit());
        assertEquals(0, fetchRanges.get(1).offset());
        assertEquals(10, fetchRanges.get(1).limit());
        assertEquals(0, fetchRanges.get(2).offset());
        assertEquals(5, fetchRanges.get(2).limit());
    }

    @Test
    public void fromLimitOffsetTest4() {
        var counts = List.of(11L, 22L, 33L, 44L, 55L, 66L, 77L, 88L );
        var limit = 101;
        var offset = 38;
        var fetchRanges = FetchRange.fromLimitOffset(counts, limit ,offset);
        assertEquals(3, fetchRanges.size());
        assertEquals(5, fetchRanges.get(0).offset());
        assertEquals(28, fetchRanges.get(0).limit());
        assertEquals(0, fetchRanges.get(1).offset());
        assertEquals(44, fetchRanges.get(1).limit());
        assertEquals(0, fetchRanges.get(2).offset());
        assertEquals(29, fetchRanges.get(2).limit());
    }

    @Test
    public void fromPageTest4() {
        var counts = List.of(11L, 22L, 33L, 44L, 55L, 66L, 77L, 88L );
        var page = 1;
        var size = 38;
        var fetchRanges = FetchRange.fromPage(counts, size, page);
        assertEquals(2, fetchRanges.size());
        assertEquals(5, fetchRanges.get(0).offset());
        assertEquals(28, fetchRanges.get(0).limit());
        assertEquals(0, fetchRanges.get(1).offset());
        assertEquals(10, fetchRanges.get(1).limit());
    }

    @Test
    public void fromPageObjectsTest1() {
        var xs = List.of(new Counter(10), new Counter(20), new Counter(30));
        var page = 1;
        var size = 25;

        var fetchRanges = FetchRange.fromPage(xs, size, page, v -> v.count().longValue());
        assertEquals(2, fetchRanges.size());
        var a = fetchRanges.get(0);
        assertEquals(20, a.data().count());
        assertEquals(15, a.offset());
        assertEquals(5, a.limit());
        var b = fetchRanges.get(1);
        assertEquals(30, b.data().count());
        assertEquals(0, b.offset());
        assertEquals(20, b.limit());

        var fetchRangesEmpty = FetchRange.fromPage(xs, 1, 80, v -> v.count().longValue());
        assertEquals(0, fetchRangesEmpty.size());

    }


}
