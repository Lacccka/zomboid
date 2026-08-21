/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.listener.channel.server;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.ChannelAttachableListener;
import org.javacord.api.listener.channel.server.ChannelCategoryAttachableListener;
import org.javacord.api.listener.channel.server.ChannelCategoryAttachableListenerManager;
import org.javacord.api.listener.channel.server.ServerChannelAttachableListener;
import org.javacord.api.listener.channel.server.ServerChannelChangeNsfwFlagListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.listener.channel.server.InternalServerChannelAttachableListenerManager;
import org.javacord.core.util.ClassHelper;

public interface InternalChannelCategoryAttachableListenerManager
extends ChannelCategoryAttachableListenerManager,
InternalServerChannelAttachableListenerManager {
    @Override
    public DiscordApi getApi();

    @Override
    public long getId();

    @Override
    default public ListenerManager<ServerChannelChangeNsfwFlagListener> addServerChannelChangeNsfwFlagListener(ServerChannelChangeNsfwFlagListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ChannelCategory.class, this.getId(), ServerChannelChangeNsfwFlagListener.class, listener);
    }

    @Override
    default public List<ServerChannelChangeNsfwFlagListener> getServerChannelChangeNsfwFlagListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ChannelCategory.class, this.getId(), ServerChannelChangeNsfwFlagListener.class);
    }

    @Override
    default public <T extends ChannelCategoryAttachableListener & ObjectAttachableListener> Collection<ListenerManager<? extends ChannelCategoryAttachableListener>> addChannelCategoryAttachableListener(T listener) {
        return ClassHelper.getInterfacesAsStream(listener.getClass()).filter(ChannelCategoryAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).flatMap(listenerClass -> {
            if (ChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                return this.addChannelAttachableListener((ChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ChannelAttachableListener)listener))))).stream();
            }
            if (ServerChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                return this.addServerChannelAttachableListener((ServerChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ServerChannelAttachableListener)listener))))).stream();
            }
            return Stream.of(((DiscordApiImpl)this.getApi()).addObjectListener(ChannelCategory.class, this.getId(), listenerClass, listener));
        }).collect(Collectors.toList());
    }

    @Override
    default public <T extends ChannelCategoryAttachableListener & ObjectAttachableListener> void removeChannelCategoryAttachableListener(T listener) {
        ClassHelper.getInterfacesAsStream(listener.getClass()).filter(ChannelCategoryAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).forEach(listenerClass -> {
            if (ChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                this.removeChannelAttachableListener((ChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ChannelAttachableListener)listener)))));
            } else if (ServerChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                this.removeServerChannelAttachableListener((ServerChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ServerChannelAttachableListener)listener)))));
            } else {
                ((DiscordApiImpl)this.getApi()).removeObjectListener(ChannelCategory.class, this.getId(), listenerClass, listener);
            }
        });
    }

    @Override
    default public <T extends ChannelCategoryAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getChannelCategoryAttachableListeners() {
        Map listeners = ((DiscordApiImpl)this.getApi()).getObjectListeners(ChannelCategory.class, this.getId());
        this.getServerChannelAttachableListeners().forEach((listener, listenerClasses) -> listeners.merge(listener, listenerClasses, (listenerClasses1, listenerClasses2) -> {
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
    default public <T extends ChannelCategoryAttachableListener & ObjectAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        ((DiscordApiImpl)this.getApi()).removeObjectListener(ChannelCategory.class, this.getId(), listenerClass, listener);
    }
}

