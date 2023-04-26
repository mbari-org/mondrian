package org.mbari.mondrian.util;

import org.mbari.jcommons.math.Matlib;
import org.mbari.vars.core.util.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record FetchRange<T>(T data, long limit, long offset) {

    public static List<FetchRange<Long>> fromPage(List<Long> counts, int size, int page) {
        return fromLimitOffset(counts, size, (long) page * size);
    }

    public static <T> List<FetchRange<T>> fromPage(List<T> data, int size, int page, Function<T, Long> transform) {
        return fromLimitOffset(data, size, (long) page * size, transform);
    }

    public static <T> List<FetchRange<T>> fromLimitOffset(List<T> data, long limit, long offset, Function<T, Long> transform) {
        Preconditions.checkArgument(limit > 0, "Limit must be > 0");
        Preconditions.checkArgument(offset >= 0, "Offset mut be >= 0");
        var xs = data.stream()
                .map(transform)
                .mapToDouble(Long::doubleValue)
                .toArray();
        var cum = Matlib.cumsum(xs);
        var startInc = offset;          // inclusive start index relative to all data
        var endExc = startInc + limit;  // exclusive end index relative to all data
//        System.out.println(startInc + " --> " + endExc);
        var fetchRanges = new ArrayList<FetchRange<T>>();
        for (int i = 0; i < xs.length; i++) {
            var end = cum[i];
            var start = end - xs[i];
//            System.out.println(start + " --> " + end);
            if ((startInc >= start && startInc < end)
                    || (endExc > start && endExc <= end)
                    || (startInc <= start && endExc > end)) {
//                System.out.println("Found " + i);
                var n = xs[i];
                double fOffset = 0D;
                if (start < startInc) {
                    fOffset = startInc - start;
                }

                double fLimit = n - fOffset;
                if (fLimit > n) {
                    fLimit = n;
                }
                if (start + fOffset + fLimit > endExc) {
                    fLimit = endExc - start - fOffset;

                }
                var fr = new FetchRange<T>(data.get(i), (long) fLimit, (long) fOffset);
                fetchRanges.add(fr);
            }
        }
        return fetchRanges;
    }


    public static List<FetchRange<Long>> fromLimitOffset(List<Long> counts, long limit, long offset) {
        return fromLimitOffset(counts, limit, offset, v -> v);
    }
}
