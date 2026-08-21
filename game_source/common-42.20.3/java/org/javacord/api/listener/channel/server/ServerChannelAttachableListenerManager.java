/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel.server;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.ChannelAttachableListenerManager;
import org.javacord.api.listener.channel.server.ServerChannelAttachableListener;
import org.javacord.api.listener.channel.server.ServerChannelChangeNameListener;
import org.javacord.api.listener.channel.server.ServerChannelChangeOverwrittenPermissionsListener;
import org.javacord.api.listener.channel.server.ServerChannelChangePositionListener;
import org.javacord.api.listener.channel.server.ServerChannelDeleteListener;
import org.javacord.api.listener.server.VoiceStateUpdateListener;
import org.javacord.api.util.event.ListenerManager;

public interface ServerChannelAttachableListenerManager
extends ChannelAttachableListenerManager {
    public ListenerManager<VoiceStateUpdateListener> addVoiceStateUpdateListener(VoiceStateUpdateListener var1);

    public List<VoiceStateUpdateListener> getVoiceStateUpdateListeners();

    public ListenerManager<ServerChannelChangePositionListener> addServerChannelChangePositionListener(ServerChannelChangePositionListener var1);

    public List<ServerChannelChangePositionListener> getServerChannelChangePositionListeners();

    public ListenerManager<ServerChannelChangeOverwrittenPermissionsListener> addServerChannelChangeOverwrittenPermissionsListener(ServerChannelChangeOverwrittenPermissionsListener var1);

    public List<ServerChannelChangeOverwrittenPermissionsListener> getServerChannelChangeOverwrittenPermissionsListeners();

    public ListenerManager<ServerChannelDeleteListener> addServerChannelDeleteListener(ServerChannelDeleteListener var1);

    public List<ServerChannelDeleteListener> getServerChannelDeleteListeners();

    public ListenerManager<ServerChannelChangeNameListener> addServerChannelChangeNameListener(ServerChannelChangeNameListener var1);

    public List<ServerChannelChangeNameListener> getServerChannelChangeNameListeners();

    public <T extends ServerChannelAttachableListener & ObjectAttachableListener> Collection<ListenerManager<? extends ServerChannelAttachableListener>> addServerChannelAttachableListener(T var1);

    public <T extends ServerChannelAttachableListener & ObjectAttachableListener> void removeServerChannelAttachableListener(T var1);

    public <T extends ServerChannelAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getServerChannelAttachableListeners();

    @Override
    public <T extends ServerChannelAttachableListener & ObjectAttachableListener> void removeListener(Class<T> var1, T var2);
}

