/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import org.javacord.api.entity.channel.internal.ServerChannelBuilderDelegate;

public class ServerChannelBuilder<T> {
    protected final Class<T> myClass;
    protected final ServerChannelBuilderDelegate delegate;

    protected ServerChannelBuilder(Class<T> myClass, ServerChannelBuilderDelegate delegate) {
        this.myClass = myClass;
        this.delegate = delegate;
    }

    public T setAuditLogReason(String reason) {
        this.delegate.setAuditLogReason(reason);
        return this.myClass.cast(this);
    }

    public T setName(String name) {
        this.delegate.setName(name);
        return this.myClass.cast(this);
    }
}

