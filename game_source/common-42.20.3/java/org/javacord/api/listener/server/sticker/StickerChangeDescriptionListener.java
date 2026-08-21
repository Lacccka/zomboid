/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server.sticker;

import org.javacord.api.event.server.sticker.StickerChangeDescriptionEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;
import org.javacord.api.listener.server.sticker.StickerAttachableListener;

@FunctionalInterface
public interface StickerChangeDescriptionListener
extends ServerAttachableListener,
ObjectAttachableListener,
GloballyAttachableListener,
StickerAttachableListener {
    public void onStickerChangeDescription(StickerChangeDescriptionEvent var1);
}

