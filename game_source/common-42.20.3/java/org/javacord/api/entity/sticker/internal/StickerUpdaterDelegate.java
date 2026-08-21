/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.sticker.internal;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.sticker.Sticker;

public interface StickerUpdaterDelegate {
    public void setName(String var1);

    public void setDescription(String var1);

    public void setTags(String var1);

    public CompletableFuture<Sticker> update();

    public CompletableFuture<Sticker> update(String var1);
}

