/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server.emoji;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.emoji.KnownCustomEmojiAttachableListener;
import org.javacord.api.listener.server.emoji.KnownCustomEmojiChangeNameListener;
import org.javacord.api.listener.server.emoji.KnownCustomEmojiChangeWhitelistedRolesListener;
import org.javacord.api.listener.server.emoji.KnownCustomEmojiDeleteListener;
import org.javacord.api.util.event.ListenerManager;

public interface KnownCustomEmojiAttachableListenerManager {
    public ListenerManager<KnownCustomEmojiChangeNameListener> addKnownCustomEmojiChangeNameListener(KnownCustomEmojiChangeNameListener var1);

    public List<KnownCustomEmojiChangeNameListener> getKnownCustomEmojiChangeNameListeners();

    public ListenerManager<KnownCustomEmojiDeleteListener> addKnownCustomEmojiDeleteListener(KnownCustomEmojiDeleteListener var1);

    public List<KnownCustomEmojiDeleteListener> getKnownCustomEmojiDeleteListeners();

    public ListenerManager<KnownCustomEmojiChangeWhitelistedRolesListener> addKnownCustomEmojiChangeWhitelistedRolesListener(KnownCustomEmojiChangeWhitelistedRolesListener var1);

    public List<KnownCustomEmojiChangeWhitelistedRolesListener> getKnownCustomEmojiChangeWhitelistedRolesListeners();

    public <T extends KnownCustomEmojiAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addKnownCustomEmojiAttachableListener(T var1);

    public <T extends KnownCustomEmojiAttachableListener & ObjectAttachableListener> void removeKnownCustomEmojiAttachableListener(T var1);

    public <T extends KnownCustomEmojiAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getKnownCustomEmojiAttachableListeners();

    public <T extends KnownCustomEmojiAttachableListener & ObjectAttachableListener> void removeListener(Class<T> var1, T var2);
}

