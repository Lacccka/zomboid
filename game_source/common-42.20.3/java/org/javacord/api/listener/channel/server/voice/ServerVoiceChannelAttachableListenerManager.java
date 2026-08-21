/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel.server.voice;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.VoiceChannelAttachableListenerManager;
import org.javacord.api.listener.channel.server.ServerChannelAttachableListenerManager;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelAttachableListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelChangeBitrateListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelChangeNsfwListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelChangeUserLimitListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberJoinListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberLeaveListener;
import org.javacord.api.util.event.ListenerManager;

public interface ServerVoiceChannelAttachableListenerManager
extends VoiceChannelAttachableListenerManager,
ServerChannelAttachableListenerManager {
    public ListenerManager<ServerVoiceChannelChangeBitrateListener> addServerVoiceChannelChangeBitrateListener(ServerVoiceChannelChangeBitrateListener var1);

    public List<ServerVoiceChannelChangeBitrateListener> getServerVoiceChannelChangeBitrateListeners();

    public ListenerManager<ServerVoiceChannelChangeUserLimitListener> addServerVoiceChannelChangeUserLimitListener(ServerVoiceChannelChangeUserLimitListener var1);

    public List<ServerVoiceChannelChangeUserLimitListener> getServerVoiceChannelChangeUserLimitListeners();

    public ListenerManager<ServerVoiceChannelMemberLeaveListener> addServerVoiceChannelMemberLeaveListener(ServerVoiceChannelMemberLeaveListener var1);

    public List<ServerVoiceChannelMemberLeaveListener> getServerVoiceChannelMemberLeaveListeners();

    public ListenerManager<ServerVoiceChannelChangeNsfwListener> addServerVoiceChannelChangeNsfwListener(ServerVoiceChannelChangeNsfwListener var1);

    public List<ServerVoiceChannelChangeNsfwListener> getServerVoiceChannelChangeNsfwListeners();

    public ListenerManager<ServerVoiceChannelMemberJoinListener> addServerVoiceChannelMemberJoinListener(ServerVoiceChannelMemberJoinListener var1);

    public List<ServerVoiceChannelMemberJoinListener> getServerVoiceChannelMemberJoinListeners();

    public <T extends ServerVoiceChannelAttachableListener & ObjectAttachableListener> Collection<ListenerManager<? extends ServerVoiceChannelAttachableListener>> addServerVoiceChannelAttachableListener(T var1);

    public <T extends ServerVoiceChannelAttachableListener & ObjectAttachableListener> void removeServerVoiceChannelAttachableListener(T var1);

    public <T extends ServerVoiceChannelAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getServerVoiceChannelAttachableListeners();

    @Override
    public <T extends ServerVoiceChannelAttachableListener & ObjectAttachableListener> void removeListener(Class<T> var1, T var2);
}

