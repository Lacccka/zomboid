/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.javacord.api.DiscordApi;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.core.DiscordApiImpl;

public class ListenerManagerImpl<T>
implements ListenerManager<T> {
    private final DiscordApiImpl api;
    private final T listener;
    private final Class<T> listenerClass;
    private final Class<?> assignedObjectClass;
    private final long objectId;
    private final List<Runnable> removeHandlers = new ArrayList<Runnable>();

    public ListenerManagerImpl(DiscordApi api, T listener, Class<T> listenerClass) {
        this(api, listener, listenerClass, null, -1L);
    }

    public ListenerManagerImpl(DiscordApi api, T listener, Class<T> listenerClass, Class<?> assignedObjectClass, long objectId) {
        this.api = (DiscordApiImpl)api;
        this.listener = listener;
        this.listenerClass = listenerClass;
        this.assignedObjectClass = assignedObjectClass;
        this.objectId = objectId;
    }

    public void removed() {
        this.removeHandlers.forEach(Runnable::run);
    }

    @Override
    public boolean isGlobalListener() {
        return this.assignedObjectClass == null;
    }

    @Override
    public Class<T> getListenerClass() {
        return this.listenerClass;
    }

    @Override
    public T getListener() {
        return this.listener;
    }

    @Override
    public Optional<Class<?>> getAssignedObjectClass() {
        return Optional.ofNullable(this.assignedObjectClass);
    }

    @Override
    public Optional<Long> getAssignedObjectId() {
        if (this.isGlobalListener()) {
            return Optional.empty();
        }
        return Optional.of(this.objectId);
    }

    @Override
    public ListenerManagerImpl<T> remove() {
        if (this.isGlobalListener()) {
            this.api.removeListener(this.listenerClass, (GloballyAttachableListener)this.listener);
        } else {
            this.api.removeObjectListener(this.assignedObjectClass, this.objectId, this.listenerClass, (ObjectAttachableListener)this.listener);
        }
        return this;
    }

    @Override
    public ListenerManagerImpl<T> removeAfter(long delay, TimeUnit timeUnit) {
        this.api.getThreadPool().getScheduler().schedule(() -> this.remove(), delay, timeUnit);
        return this;
    }

    @Override
    public void addRemoveHandler(Runnable removeHandler) {
        this.removeHandlers.add(removeHandler);
    }
}

