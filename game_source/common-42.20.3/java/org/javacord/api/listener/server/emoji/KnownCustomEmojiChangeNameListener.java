/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server.emoji;

import org.javacord.api.event.server.emoji.KnownCustomEmojiChangeNameEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;
import org.javacord.api.listener.server.emoji.KnownCustomEmojiAttachableListener;

@FunctionalInterface
public interface KnownCustomEmojiChangeNameListener
extends ServerAttachableListener,
KnownCustomEmojiAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onKnownCustomEmojiChangeName(KnownCustomEmojiChangeNameEvent var1);
}

