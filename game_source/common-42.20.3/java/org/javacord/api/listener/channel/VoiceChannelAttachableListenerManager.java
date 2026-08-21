/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.ChannelAttachableListenerManager;
import org.javacord.api.listener.channel.VoiceChannelAttachableListener;
import org.javacord.api.util.event.ListenerManager;

public interface VoiceChannelAttachableListenerManager
extends ChannelAttachableListenerManager {
    public <T extends VoiceChannelAttachableListener & ObjectAttachableListener> Collection<ListenerManager<? extends VoiceChannelAttachableListener>> addVoiceChannelAttachableListener(T var1);

    public <T extends VoiceChannelAttachableListener & ObjectAttachableListener> void removeVoiceChannelAttachableListener(T var1);

    public <T extends VoiceChannelAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getVoiceChannelAttachableListeners();

    @Override
    public <T extends VoiceChannelAttachableListener & ObjectAttachableListener> void removeListener(Class<T> var1, T var2);
}

