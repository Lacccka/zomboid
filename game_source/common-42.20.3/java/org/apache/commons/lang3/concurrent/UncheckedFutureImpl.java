/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.lang3.concurrent.AbstractFutureProxy;
import org.apache.commons.lang3.concurrent.UncheckedExecutionException;
import org.apache.commons.lang3.concurrent.UncheckedFuture;
import org.apache.commons.lang3.concurrent.UncheckedTimeoutException;
import org.apache.commons.lang3.exception.UncheckedInterruptedException;

final class UncheckedFutureImpl<V>
extends AbstractFutureProxy<V>
implements UncheckedFuture<V> {
    UncheckedFutureImpl(Future<V> future) {
        super(future);
    }

    @Override
    public V get() {
        try {
            return super.get();
        }
        catch (InterruptedException e) {
            throw new UncheckedInterruptedException(e);
        }
        catch (ExecutionException e) {
            throw new UncheckedExecutionException(e);
        }
    }

    @Override
    public V get(long timeout2, TimeUnit unit) {
        try {
            return super.get(timeout2, unit);
        }
        catch (InterruptedException e) {
            throw new UncheckedInterruptedException(e);
        }
        catch (ExecutionException e) {
            throw new UncheckedExecutionException(e);
        }
        catch (TimeoutException e) {
            throw new UncheckedTimeoutException(e);
        }
    }
}

