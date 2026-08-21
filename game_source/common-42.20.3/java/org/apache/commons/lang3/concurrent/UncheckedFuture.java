/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.concurrent;

import java.util.Collection;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.concurrent.UncheckedFutureImpl;

public interface UncheckedFuture<V>
extends Future<V> {
    public static <T> Stream<UncheckedFuture<T>> map(Collection<Future<T>> futures) {
        return futures.stream().map(UncheckedFuture::on);
    }

    public static <T> Collection<UncheckedFuture<T>> on(Collection<Future<T>> futures) {
        return UncheckedFuture.map(futures).collect(Collectors.toList());
    }

    public static <T> UncheckedFuture<T> on(Future<T> future) {
        return new UncheckedFutureImpl<T>(future);
    }

    @Override
    public V get();

    @Override
    public V get(long var1, TimeUnit var3);
}

