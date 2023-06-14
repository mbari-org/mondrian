package org.mbari.mondrian.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Function;

public class CollectionUtils {
    private CollectionUtils() {
        // no instantiation
    }

    public static <T, R> boolean isSame(Collection<T> a, Collection<T> b, Function<T, R> transform) {
        if (a.size() != b.size()) {
            return false;
        }
        var at = a.stream().map(transform).toList();
        var bt = b.stream().map(transform).toList();
        return new HashSet<>(at).equals(new HashSet<>(bt));
    }

    public static <A, B, C> Collection<A> intersection(Collection<A> a,
                                                            Collection<B> b,
                                                            Function<A, C> ta,
                                                       Function<B, C> tb) {
        var xs = new ArrayList<A>();
        for (var ai: a) {
            var i = ta.apply(ai);
            b.stream()
                    .map(tb)
                    .filter(i::equals)
                    .findFirst()
                    .ifPresent(bi -> xs.add(ai));
        }
        return xs;
    }

    public static <A> A head(Collection<A> items) {
        return items.stream().findFirst().orElse(null);
    }
}
