/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.listener.server.sticker;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.sticker.Sticker;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.sticker.StickerAttachableListener;
import org.javacord.api.listener.server.sticker.StickerAttachableListenerManager;
import org.javacord.api.listener.server.sticker.StickerChangeDescriptionListener;
import org.javacord.api.listener.server.sticker.StickerChangeNameListener;
import org.javacord.api.listener.server.sticker.StickerChangeTagsListener;
import org.javacord.api.listener.server.sticker.StickerDeleteListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.ClassHelper;

public interface InternalStickerAttachableListenerManager
extends StickerAttachableListenerManager {
    public DiscordApi getApi();

    public long getId();

    @Override
    default public ListenerManager<StickerChangeTagsListener> addStickerChangeTagsListener(StickerChangeTagsListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Sticker.class, this.getId(), StickerChangeTagsListener.class, listener);
    }

    @Override
    default public List<StickerChangeTagsListener> getStickerChangeTagsListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Sticker.class, this.getId(), StickerChangeTagsListener.class);
    }

    @Override
    default public ListenerManager<StickerChangeDescriptionListener> addStickerChangeDescriptionListener(StickerChangeDescriptionListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Sticker.class, this.getId(), StickerChangeDescriptionListener.class, listener);
    }

    @Override
    default public List<StickerChangeDescriptionListener> getStickerChangeDescriptionListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Sticker.class, this.getId(), StickerChangeDescriptionListener.class);
    }

    @Override
    default public ListenerManager<StickerChangeNameListener> addStickerChangeNameListener(StickerChangeNameListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Sticker.class, this.getId(), StickerChangeNameListener.class, listener);
    }

    @Override
    default public List<StickerChangeNameListener> getStickerChangeNameListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Sticker.class, this.getId(), StickerChangeNameListener.class);
    }

    @Override
    default public ListenerManager<StickerDeleteListener> addStickerDeleteListener(StickerDeleteListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Sticker.class, this.getId(), StickerDeleteListener.class, listener);
    }

    @Override
    default public List<StickerDeleteListener> getStickerDeleteListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Sticker.class, this.getId(), StickerDeleteListener.class);
    }

    @Override
    default public <T extends StickerAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addStickerAttachableListener(T listener) {
        return ClassHelper.getInterfacesAsStream(listener.getClass()).filter(StickerAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).map(listenerClass -> ((DiscordApiImpl)this.getApi()).addObjectListener(Sticker.class, this.getId(), listenerClass, listener)).collect(Collectors.toList());
    }

    @Override
    default public <T extends StickerAttachableListener & ObjectAttachableListener> void removeStickerAttachableListener(T listener) {
        ClassHelper.getInterfacesAsStream(listener.getClass()).filter(StickerAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).forEach(listenerClass -> ((DiscordApiImpl)this.getApi()).removeObjectListener(Sticker.class, this.getId(), listenerClass, listener));
    }

    @Override
    default public <T extends StickerAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getStickerAttachableListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Sticker.class, this.getId());
    }

    @Override
    default public <T extends StickerAttachableListener & ObjectAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        ((DiscordApiImpl)this.getApi()).removeObjectListener(Sticker.class, this.getId(), listenerClass, listener);
    }
}

