/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.ChannelAttachableListener;
import org.javacord.api.util.event.ListenerManager;

public interface ChannelAttachableListenerManager {
    public <T extends ChannelAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addChannelAttachableListener(T var1);

    public <T extends ChannelAttachableListener & ObjectAttachableListener> void removeChannelAttachableListener(T var1);

    public <T extends ChannelAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getChannelAttachableListeners();

    public <T extends ChannelAttachableListener & ObjectAttachableListener> void removeListener(Class<T> var1, T var2);
}

