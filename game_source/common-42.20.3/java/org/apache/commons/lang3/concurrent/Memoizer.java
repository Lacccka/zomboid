/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.concurrent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import org.apache.commons.lang3.concurrent.Computable;
import org.apache.commons.lang3.concurrent.FutureTasks;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class Memoizer<I, O>
implements Computable<I, O> {
    private final ConcurrentMap<I, Future<O>> cache = new ConcurrentHashMap<I, Future<O>>();
    private final Function<? super I, ? extends Future<O>> mappingFunction;
    private final boolean recalculate;

    public Memoizer(Computable<I, O> computable) {
        this(computable, false);
    }

    public Memoizer(Computable<I, O> computable, boolean recalculate) {
        this.recalculate = recalculate;
        this.mappingFunction = k -> FutureTasks.run(() -> computable.compute(k));
    }

    public Memoizer(Function<I, O> function) {
        this(function, false);
    }

    public Memoizer(Function<I, O> function, boolean recalculate) {
        this.recalculate = recalculate;
        this.mappingFunction = k -> FutureTasks.run(() -> function.apply(k));
    }

    @Override
    public O compute(I arg) throws InterruptedException {
        while (true) {
            Future<O> future = this.cache.computeIfAbsent(arg, this.mappingFunction);
            try {
                return future.get();
            }
            catch (CancellationException e) {
                this.cache.remove(arg, future);
                continue;
            }
            catch (ExecutionException e) {
                if (this.recalculate) {
                    this.cache.remove(arg, future);
                }
                throw this.launderException(e.getCause());
            }
            break;
        }
    }

    private RuntimeException launderException(Throwable throwable) {
        throw new IllegalStateException("Unchecked exception", ExceptionUtils.throwUnchecked(throwable));
    }
}

