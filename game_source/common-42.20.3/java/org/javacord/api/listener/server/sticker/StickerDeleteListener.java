/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server.sticker;

import org.javacord.api.event.server.sticker.StickerDeleteEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;
import org.javacord.api.listener.server.sticker.StickerAttachableListener;

@FunctionalInterface
public interface StickerDeleteListener
extends ServerAttachableListener,
StickerAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onStickerDelete(StickerDeleteEvent var1);
}

