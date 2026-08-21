/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.listener.channel.server.text;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.ChannelAttachableListener;
import org.javacord.api.listener.channel.ServerThreadChannelAttachableListener;
import org.javacord.api.listener.channel.TextChannelAttachableListener;
import org.javacord.api.listener.channel.server.ServerChannelAttachableListener;
import org.javacord.api.listener.channel.server.ServerChannelChangeNsfwFlagListener;
import org.javacord.api.listener.channel.server.text.ServerTextChannelAttachableListener;
import org.javacord.api.listener.channel.server.text.ServerTextChannelAttachableListenerManager;
import org.javacord.api.listener.channel.server.text.ServerTextChannelChangeDefaultAutoArchiveDurationListener;
import org.javacord.api.listener.channel.server.text.ServerTextChannelChangeSlowmodeListener;
import org.javacord.api.listener.channel.server.text.ServerTextChannelChangeTopicListener;
import org.javacord.api.listener.channel.server.text.WebhooksUpdateListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.listener.channel.InternalTextChannelAttachableListenerManager;
import org.javacord.core.listener.channel.server.InternalServerChannelAttachableListenerManager;
import org.javacord.core.util.ClassHelper;

public interface InternalServerTextChannelAttachableListenerManager
extends ServerTextChannelAttachableListenerManager,
InternalTextChannelAttachableListenerManager,
InternalServerChannelAttachableListenerManager {
    @Override
    public DiscordApi getApi();

    @Override
    public long getId();

    @Override
    default public ListenerManager<WebhooksUpdateListener> addWebhooksUpdateListener(WebhooksUpdateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerTextChannel.class, this.getId(), WebhooksUpdateListener.class, listener);
    }

    @Override
    default public List<WebhooksUpdateListener> getWebhooksUpdateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerTextChannel.class, this.getId(), WebhooksUpdateListener.class);
    }

    @Override
    default public ListenerManager<ServerTextChannelChangeDefaultAutoArchiveDurationListener> addServerTextChannelChangeDefaultAutoArchiveDurationListener(ServerTextChannelChangeDefaultAutoArchiveDurationListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerTextChannel.class, this.getId(), ServerTextChannelChangeDefaultAutoArchiveDurationListener.class, listener);
    }

    @Override
    default public List<ServerTextChannelChangeDefaultAutoArchiveDurationListener> getServerTextChannelChangeDefaultAutoArchiveDurationListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerTextChannel.class, this.getId(), ServerTextChannelChangeDefaultAutoArchiveDurationListener.class);
    }

    @Override
    default public ListenerManager<ServerTextChannelChangeSlowmodeListener> addServerTextChannelChangeSlowmodeListener(ServerTextChannelChangeSlowmodeListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerTextChannel.class, this.getId(), ServerTextChannelChangeSlowmodeListener.class, listener);
    }

    @Override
    default public List<ServerTextChannelChangeSlowmodeListener> getServerTextChannelChangeSlowmodeListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerTextChannel.class, this.getId(), ServerTextChannelChangeSlowmodeListener.class);
    }

    @Override
    default public ListenerManager<ServerTextChannelChangeTopicListener> addServerTextChannelChangeTopicListener(ServerTextChannelChangeTopicListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerTextChannel.class, this.getId(), ServerTextChannelChangeTopicListener.class, listener);
    }

    @Override
    default public List<ServerTextChannelChangeTopicListener> getServerTextChannelChangeTopicListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerTextChannel.class, this.getId(), ServerTextChannelChangeTopicListener.class);
    }

    @Override
    default public ListenerManager<ServerChannelChangeNsfwFlagListener> addServerChannelChangeNsfwFlagListener(ServerChannelChangeNsfwFlagListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerTextChannel.class, this.getId(), ServerChannelChangeNsfwFlagListener.class, listener);
    }

    @Override
    default public List<ServerChannelChangeNsfwFlagListener> getServerChannelChangeNsfwFlagListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerTextChannel.class, this.getId(), ServerChannelChangeNsfwFlagListener.class);
    }

    @Override
    default public <T extends ServerTextChannelAttachableListener & ObjectAttachableListener> Collection<ListenerManager<? extends ServerTextChannelAttachableListener>> addServerTextChannelAttachableListener(T listener) {
        return ClassHelper.getInterfacesAsStream(listener.getClass()).filter(ServerTextChannelAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).flatMap(listenerClass -> {
            if (ServerThreadChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                return this.addServerThreadChannelAttachableListener((ServerThreadChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ServerThreadChannelAttachableListener)listener))))).stream();
            }
            if (ChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                return this.addChannelAttachableListener((ChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ChannelAttachableListener)listener))))).stream();
            }
            if (TextChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                return this.addTextChannelAttachableListener((TextChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((TextChannelAttachableListener)listener))))).stream();
            }
            if (ServerChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                return this.addServerChannelAttachableListener((ServerChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ServerChannelAttachableListener)listener))))).stream();
            }
            return Stream.of(((DiscordApiImpl)this.getApi()).addObjectListener(ServerTextChannel.class, this.getId(), listenerClass, listener));
        }).collect(Collectors.toList());
    }

    @Override
    default public <T extends ServerTextChannelAttachableListener & ObjectAttachableListener> void removeServerTextChannelAttachableListener(T listener) {
        ClassHelper.getInterfacesAsStream(listener.getClass()).filter(ServerTextChannelAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).forEach(listenerClass -> {
            if (ServerThreadChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                this.removeServerThreadChannelAttachableListener((ServerThreadChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ServerThreadChannelAttachableListener)listener)))));
            } else if (ChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                this.removeChannelAttachableListener((ChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ChannelAttachableListener)listener)))));
            } else if (TextChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                this.removeTextChannelAttachableListener((TextChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((TextChannelAttachableListener)listener)))));
            } else if (ServerChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                this.removeServerChannelAttachableListener((ServerChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ServerChannelAttachableListener)listener)))));
            } else {
                ((DiscordApiImpl)this.getApi()).removeObjectListener(ServerTextChannel.class, this.getId(), listenerClass, listener);
            }
        });
    }

    @Override
    default public <T extends ServerTextChannelAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getServerTextChannelAttachableListeners() {
        Map listeners = ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerTextChannel.class, this.getId());
        this.getServerChannelAttachableListeners().forEach((listener, listenerClasses) -> listeners.merge(listener, listenerClasses, (listenerClasses1, listenerClasses2) -> {
            listenerClasses1.addAll(listenerClasses2);
            return listenerClasses1;
        }));
        this.getTextChannelAttachableListeners().forEach((listener, listenerClasses) -> listeners.merge(listener, listenerClasses, (listenerClasses1, listenerClasses2) -> {
            listenerClasses1.addAll(listenerClasses2);
            return listenerClasses1;
        }));
        this.getChannelAttachableListeners().forEach((listener, listenerClasses) -> listeners.merge(listener, listenerClasses, (listenerClasses1, listenerClasses2) -> {
            listenerClasses1.addAll(listenerClasses2);
            return listenerClasses1;
        }));
        this.getServerThreadChannelAttachableListeners().forEach((listener, listenerClasses) -> listeners.merge(listener, listenerClasses, (listenerClasses1, listenerClasses2) -> {
            listenerClasses1.addAll(listenerClasses2);
            return listenerClasses1;
        }));
        return listeners;
    }

    @Override
    default public <T extends ServerTextChannelAttachableListener & ObjectAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        ((DiscordApiImpl)this.getApi()).removeObjectListener(ServerTextChannel.class, this.getId(), listenerClass, listener);
    }
}

