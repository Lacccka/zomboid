/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel.user;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.TextChannelAttachableListenerManager;
import org.javacord.api.listener.channel.VoiceChannelAttachableListenerManager;
import org.javacord.api.listener.channel.user.PrivateChannelAttachableListener;
import org.javacord.api.listener.channel.user.PrivateChannelDeleteListener;
import org.javacord.api.util.event.ListenerManager;

public interface PrivateChannelAttachableListenerManager
extends VoiceChannelAttachableListenerManager,
TextChannelAttachableListenerManager {
    public ListenerManager<PrivateChannelDeleteListener> addPrivateChannelDeleteListener(PrivateChannelDeleteListener var1);

    public List<PrivateChannelDeleteListener> getPrivateChannelDeleteListeners();

    public <T extends PrivateChannelAttachableListener & ObjectAttachableListener> Collection<ListenerManager<? extends PrivateChannelAttachableListener>> addPrivateChannelAttachableListener(T var1);

    public <T extends PrivateChannelAttachableListener & ObjectAttachableListener> void removePrivateChannelAttachableListener(T var1);

    public <T extends PrivateChannelAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getPrivateChannelAttachableListeners();

    @Override
    public <T extends PrivateChannelAttachableListener & ObjectAttachableListener> void removeListener(Class<T> var1, T var2);
}

