/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server.sticker;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.sticker.StickerAttachableListener;
import org.javacord.api.listener.server.sticker.StickerChangeDescriptionListener;
import org.javacord.api.listener.server.sticker.StickerChangeNameListener;
import org.javacord.api.listener.server.sticker.StickerChangeTagsListener;
import org.javacord.api.listener.server.sticker.StickerDeleteListener;
import org.javacord.api.util.event.ListenerManager;

public interface StickerAttachableListenerManager {
    public ListenerManager<StickerChangeTagsListener> addStickerChangeTagsListener(StickerChangeTagsListener var1);

    public List<StickerChangeTagsListener> getStickerChangeTagsListeners();

    public ListenerManager<StickerChangeDescriptionListener> addStickerChangeDescriptionListener(StickerChangeDescriptionListener var1);

    public List<StickerChangeDescriptionListener> getStickerChangeDescriptionListeners();

    public ListenerManager<StickerChangeNameListener> addStickerChangeNameListener(StickerChangeNameListener var1);

    public List<StickerChangeNameListener> getStickerChangeNameListeners();

    public ListenerManager<StickerDeleteListener> addStickerDeleteListener(StickerDeleteListener var1);

    public List<StickerDeleteListener> getStickerDeleteListeners();

    public <T extends StickerAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addStickerAttachableListener(T var1);

    public <T extends StickerAttachableListener & ObjectAttachableListener> void removeStickerAttachableListener(T var1);

    public <T extends StickerAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getStickerAttachableListeners();

    public <T extends StickerAttachableListener & ObjectAttachableListener> void removeListener(Class<T> var1, T var2);
}

