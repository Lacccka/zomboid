/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server.sticker;

import org.javacord.api.event.server.sticker.StickerChangeNameEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;
import org.javacord.api.listener.server.sticker.StickerAttachableListener;

@FunctionalInterface
public interface StickerChangeNameListener
extends ServerAttachableListener,
ObjectAttachableListener,
GloballyAttachableListener,
StickerAttachableListener {
    public void onStickerChangeName(StickerChangeNameEvent var1);
}

