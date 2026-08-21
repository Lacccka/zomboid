/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.listener.channel.user;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.ChannelAttachableListener;
import org.javacord.api.listener.channel.ServerThreadChannelAttachableListener;
import org.javacord.api.listener.channel.TextChannelAttachableListener;
import org.javacord.api.listener.channel.VoiceChannelAttachableListener;
import org.javacord.api.listener.channel.user.PrivateChannelAttachableListener;
import org.javacord.api.listener.channel.user.PrivateChannelAttachableListenerManager;
import org.javacord.api.listener.channel.user.PrivateChannelDeleteListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.listener.channel.InternalTextChannelAttachableListenerManager;
import org.javacord.core.listener.channel.InternalVoiceChannelAttachableListenerManager;
import org.javacord.core.util.ClassHelper;

public interface InternalPrivateChannelAttachableListenerManager
extends PrivateChannelAttachableListenerManager,
InternalVoiceChannelAttachableListenerManager,
InternalTextChannelAttachableListenerManager {
    @Override
    public DiscordApi getApi();

    @Override
    public long getId();

    @Override
    default public ListenerManager<PrivateChannelDeleteListener> addPrivateChannelDeleteListener(PrivateChannelDeleteListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(PrivateChannel.class, this.getId(), PrivateChannelDeleteListener.class, listener);
    }

    @Override
    default public List<PrivateChannelDeleteListener> getPrivateChannelDeleteListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(PrivateChannel.class, this.getId(), PrivateChannelDeleteListener.class);
    }

    @Override
    default public <T extends PrivateChannelAttachableListener & ObjectAttachableListener> Collection<ListenerManager<? extends PrivateChannelAttachableListener>> addPrivateChannelAttachableListener(T listener) {
        return ClassHelper.getInterfacesAsStream(listener.getClass()).filter(PrivateChannelAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).flatMap(listenerClass -> {
            if (ChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                return this.addChannelAttachableListener((ChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ChannelAttachableListener)listener))))).stream();
            }
            if (ServerThreadChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                return this.addServerThreadChannelAttachableListener((ServerThreadChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ServerThreadChannelAttachableListener)listener))))).stream();
            }
            if (VoiceChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                return this.addVoiceChannelAttachableListener((VoiceChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((VoiceChannelAttachableListener)listener))))).stream();
            }
            if (TextChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                return this.addTextChannelAttachableListener((TextChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((TextChannelAttachableListener)listener))))).stream();
            }
            return Stream.of(((DiscordApiImpl)this.getApi()).addObjectListener(PrivateChannel.class, this.getId(), listenerClass, listener));
        }).collect(Collectors.toList());
    }

    @Override
    default public <T extends PrivateChannelAttachableListener & ObjectAttachableListener> void removePrivateChannelAttachableListener(T listener) {
        ClassHelper.getInterfacesAsStream(listener.getClass()).filter(PrivateChannelAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).forEach(listenerClass -> {
            if (ChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                this.removeChannelAttachableListener((ChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ChannelAttachableListener)listener)))));
            } else if (ServerThreadChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                this.removeServerThreadChannelAttachableListener((ServerThreadChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ServerThreadChannelAttachableListener)listener)))));
            } else if (VoiceChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                this.removeVoiceChannelAttachableListener((VoiceChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((VoiceChannelAttachableListener)listener)))));
            } else if (TextChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                this.removeTextChannelAttachableListener((TextChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((TextChannelAttachableListener)listener)))));
            } else {
                ((DiscordApiImpl)this.getApi()).removeObjectListener(PrivateChannel.class, this.getId(), listenerClass, listener);
            }
        });
    }

    @Override
    default public <T extends PrivateChannelAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getPrivateChannelAttachableListeners() {
        Map listeners = ((DiscordApiImpl)this.getApi()).getObjectListeners(PrivateChannel.class, this.getId());
        this.getTextChannelAttachableListeners().forEach((listener, listenerClasses) -> listeners.merge(listener, listenerClasses, (listenerClasses1, listenerClasses2) -> {
            listenerClasses1.addAll(listenerClasses2);
            return listenerClasses1;
        }));
        this.getVoiceChannelAttachableListeners().forEach((listener, listenerClasses) -> listeners.merge(listener, listenerClasses, (listenerClasses1, listenerClasses2) -> {
            listenerClasses1.addAll(listenerClasses2);
            return listenerClasses1;
        }));
        this.getServerThreadChannelAttachableListeners().forEach((listener, listenerClasses) -> listeners.merge(listener, listenerClasses, (listenerClasses1, listenerClasses2) -> {
            listenerClasses1.addAll(listenerClasses2);
            return listenerClasses1;
        }));
        this.getChannelAttachableListeners().forEach((listener, listenerClasses) -> listeners.merge(listener, listenerClasses, (listenerClasses1, listenerClasses2) -> {
            listenerClasses1.addAll(listenerClasses2);
            return listenerClasses1;
        }));
        return listeners;
    }

    @Override
    default public <T extends PrivateChannelAttachableListener & ObjectAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        ((DiscordApiImpl)this.getApi()).removeObjectListener(PrivateChannel.class, this.getId(), listenerClass, listener);
    }
}

