/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel.server.voice;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.server.voice.ServerStageVoiceChannelAttachableListener;
import org.javacord.api.listener.channel.server.voice.ServerStageVoiceChannelChangeTopicListener;
import org.javacord.api.util.event.ListenerManager;

public interface ServerStageVoiceChannelAttachableListenerManager {
    public ListenerManager<ServerStageVoiceChannelChangeTopicListener> addServerStageVoiceChannelChangeTopicListener(ServerStageVoiceChannelChangeTopicListener var1);

    public List<ServerStageVoiceChannelChangeTopicListener> getServerStageVoiceChannelChangeTopicListeners();

    public <T extends ServerStageVoiceChannelAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addServerStageVoiceChannelAttachableListener(T var1);

    public <T extends ServerStageVoiceChannelAttachableListener & ObjectAttachableListener> void removeServerStageVoiceChannelAttachableListener(T var1);

    public <T extends ServerStageVoiceChannelAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getServerStageVoiceChannelAttachableListeners();

    public <T extends ServerStageVoiceChannelAttachableListener & ObjectAttachableListener> void removeListener(Class<T> var1, T var2);
}

