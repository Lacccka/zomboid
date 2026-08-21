/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel.server.text;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.TextChannelAttachableListenerManager;
import org.javacord.api.listener.channel.server.ServerChannelAttachableListenerManager;
import org.javacord.api.listener.channel.server.ServerChannelChangeNsfwFlagListener;
import org.javacord.api.listener.channel.server.text.ServerTextChannelAttachableListener;
import org.javacord.api.listener.channel.server.text.ServerTextChannelChangeDefaultAutoArchiveDurationListener;
import org.javacord.api.listener.channel.server.text.ServerTextChannelChangeSlowmodeListener;
import org.javacord.api.listener.channel.server.text.ServerTextChannelChangeTopicListener;
import org.javacord.api.listener.channel.server.text.WebhooksUpdateListener;
import org.javacord.api.util.event.ListenerManager;

public interface ServerTextChannelAttachableListenerManager
extends TextChannelAttachableListenerManager,
ServerChannelAttachableListenerManager {
    public ListenerManager<WebhooksUpdateListener> addWebhooksUpdateListener(WebhooksUpdateListener var1);

    public List<WebhooksUpdateListener> getWebhooksUpdateListeners();

    public ListenerManager<ServerTextChannelChangeDefaultAutoArchiveDurationListener> addServerTextChannelChangeDefaultAutoArchiveDurationListener(ServerTextChannelChangeDefaultAutoArchiveDurationListener var1);

    public List<ServerTextChannelChangeDefaultAutoArchiveDurationListener> getServerTextChannelChangeDefaultAutoArchiveDurationListeners();

    public ListenerManager<ServerTextChannelChangeSlowmodeListener> addServerTextChannelChangeSlowmodeListener(ServerTextChannelChangeSlowmodeListener var1);

    public List<ServerTextChannelChangeSlowmodeListener> getServerTextChannelChangeSlowmodeListeners();

    public ListenerManager<ServerTextChannelChangeTopicListener> addServerTextChannelChangeTopicListener(ServerTextChannelChangeTopicListener var1);

    public List<ServerTextChannelChangeTopicListener> getServerTextChannelChangeTopicListeners();

    public ListenerManager<ServerChannelChangeNsfwFlagListener> addServerChannelChangeNsfwFlagListener(ServerChannelChangeNsfwFlagListener var1);

    public List<ServerChannelChangeNsfwFlagListener> getServerChannelChangeNsfwFlagListeners();

    public <T extends ServerTextChannelAttachableListener & ObjectAttachableListener> Collection<ListenerManager<? extends ServerTextChannelAttachableListener>> addServerTextChannelAttachableListener(T var1);

    public <T extends ServerTextChannelAttachableListener & ObjectAttachableListener> void removeServerTextChannelAttachableListener(T var1);

    public <T extends ServerTextChannelAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getServerTextChannelAttachableListeners();

    @Override
    public <T extends ServerTextChannelAttachableListener & ObjectAttachableListener> void removeListener(Class<T> var1, T var2);
}

