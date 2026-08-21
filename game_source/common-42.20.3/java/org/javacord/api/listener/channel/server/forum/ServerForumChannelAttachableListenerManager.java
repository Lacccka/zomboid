/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel.server.forum;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.server.forum.ServerForumChannelAttachableListener;
import org.javacord.api.util.event.ListenerManager;

public interface ServerForumChannelAttachableListenerManager {
    public <T extends ServerForumChannelAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addServerForumChannelAttachableListener(T var1);

    public <T extends ServerForumChannelAttachableListener & ObjectAttachableListener> void removeServerForumChannelAttachableListener(T var1);

    public <T extends ServerForumChannelAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getServerForumChannelAttachableListeners();

    public <T extends ServerForumChannelAttachableListener & ObjectAttachableListener> void removeListener(Class<T> var1, T var2);
}

