/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel.server;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.server.ChannelCategoryAttachableListener;
import org.javacord.api.listener.channel.server.ServerChannelAttachableListenerManager;
import org.javacord.api.listener.channel.server.ServerChannelChangeNsfwFlagListener;
import org.javacord.api.util.event.ListenerManager;

public interface ChannelCategoryAttachableListenerManager
extends ServerChannelAttachableListenerManager {
    public ListenerManager<ServerChannelChangeNsfwFlagListener> addServerChannelChangeNsfwFlagListener(ServerChannelChangeNsfwFlagListener var1);

    public List<ServerChannelChangeNsfwFlagListener> getServerChannelChangeNsfwFlagListeners();

    public <T extends ChannelCategoryAttachableListener & ObjectAttachableListener> Collection<ListenerManager<? extends ChannelCategoryAttachableListener>> addChannelCategoryAttachableListener(T var1);

    public <T extends ChannelCategoryAttachableListener & ObjectAttachableListener> void removeChannelCategoryAttachableListener(T var1);

    public <T extends ChannelCategoryAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getChannelCategoryAttachableListeners();

    @Override
    public <T extends ChannelCategoryAttachableListener & ObjectAttachableListener> void removeListener(Class<T> var1, T var2);
}

