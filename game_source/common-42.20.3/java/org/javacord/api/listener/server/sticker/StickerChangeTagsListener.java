/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server.sticker;

import org.javacord.api.event.server.sticker.StickerChangeTagsEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;
import org.javacord.api.listener.server.sticker.StickerAttachableListener;

@FunctionalInterface
public interface StickerChangeTagsListener
extends ServerAttachableListener,
ObjectAttachableListener,
GloballyAttachableListener,
StickerAttachableListener {
    public void onStickerChangeTags(StickerChangeTagsEvent var1);
}

