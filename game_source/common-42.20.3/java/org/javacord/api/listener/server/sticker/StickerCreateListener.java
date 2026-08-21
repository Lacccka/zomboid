/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server.sticker;

import org.javacord.api.event.server.sticker.StickerCreateEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;

@FunctionalInterface
public interface StickerCreateListener
extends ServerAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onStickerCreate(StickerCreateEvent var1);
}

