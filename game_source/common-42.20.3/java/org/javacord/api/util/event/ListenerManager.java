/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.util.event;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public interface ListenerManager<T> {
    public boolean isGlobalListener();

    public Class<T> getListenerClass();

    public T getListener();

    public Optional<Class<?>> getAssignedObjectClass();

    public Optional<Long> getAssignedObjectId();

    public ListenerManager<T> remove();

    public ListenerManager<T> removeAfter(long var1, TimeUnit var3);

    public void addRemoveHandler(Runnable var1);
}

