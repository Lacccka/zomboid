/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.listener.server.emoji;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.emoji.KnownCustomEmojiAttachableListener;
import org.javacord.api.listener.server.emoji.KnownCustomEmojiAttachableListenerManager;
import org.javacord.api.listener.server.emoji.KnownCustomEmojiChangeNameListener;
import org.javacord.api.listener.server.emoji.KnownCustomEmojiChangeWhitelistedRolesListener;
import org.javacord.api.listener.server.emoji.KnownCustomEmojiDeleteListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.ClassHelper;

public interface InternalKnownCustomEmojiAttachableListenerManager
extends KnownCustomEmojiAttachableListenerManager {
    public DiscordApi getApi();

    public long getId();

    @Override
    default public ListenerManager<KnownCustomEmojiChangeNameListener> addKnownCustomEmojiChangeNameListener(KnownCustomEmojiChangeNameListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(KnownCustomEmoji.class, this.getId(), KnownCustomEmojiChangeNameListener.class, listener);
    }

    @Override
    default public List<KnownCustomEmojiChangeNameListener> getKnownCustomEmojiChangeNameListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(KnownCustomEmoji.class, this.getId(), KnownCustomEmojiChangeNameListener.class);
    }

    @Override
    default public ListenerManager<KnownCustomEmojiDeleteListener> addKnownCustomEmojiDeleteListener(KnownCustomEmojiDeleteListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(KnownCustomEmoji.class, this.getId(), KnownCustomEmojiDeleteListener.class, listener);
    }

    @Override
    default public List<KnownCustomEmojiDeleteListener> getKnownCustomEmojiDeleteListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(KnownCustomEmoji.class, this.getId(), KnownCustomEmojiDeleteListener.class);
    }

    @Override
    default public ListenerManager<KnownCustomEmojiChangeWhitelistedRolesListener> addKnownCustomEmojiChangeWhitelistedRolesListener(KnownCustomEmojiChangeWhitelistedRolesListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(KnownCustomEmoji.class, this.getId(), KnownCustomEmojiChangeWhitelistedRolesListener.class, listener);
    }

    @Override
    default public List<KnownCustomEmojiChangeWhitelistedRolesListener> getKnownCustomEmojiChangeWhitelistedRolesListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(KnownCustomEmoji.class, this.getId(), KnownCustomEmojiChangeWhitelistedRolesListener.class);
    }

    @Override
    default public <T extends KnownCustomEmojiAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addKnownCustomEmojiAttachableListener(T listener) {
        return ClassHelper.getInterfacesAsStream(listener.getClass()).filter(KnownCustomEmojiAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).map(listenerClass -> ((DiscordApiImpl)this.getApi()).addObjectListener(KnownCustomEmoji.class, this.getId(), listenerClass, listener)).collect(Collectors.toList());
    }

    @Override
    default public <T extends KnownCustomEmojiAttachableListener & ObjectAttachableListener> void removeKnownCustomEmojiAttachableListener(T listener) {
        ClassHelper.getInterfacesAsStream(listener.getClass()).filter(KnownCustomEmojiAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).forEach(listenerClass -> ((DiscordApiImpl)this.getApi()).removeObjectListener(KnownCustomEmoji.class, this.getId(), listenerClass, listener));
    }

    @Override
    default public <T extends KnownCustomEmojiAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getKnownCustomEmojiAttachableListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(KnownCustomEmoji.class, this.getId());
    }

    @Override
    default public <T extends KnownCustomEmojiAttachableListener & ObjectAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        ((DiscordApiImpl)this.getApi()).removeObjectListener(KnownCustomEmoji.class, this.getId(), listenerClass, listener);
    }
}

