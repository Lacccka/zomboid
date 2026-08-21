/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.sticker.internal;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.sticker.Sticker;

public interface StickerBuilderDelegate {
    public void copy(Sticker var1);

    public void setName(String var1);

    public void setDescription(String var1);

    public void setTags(String var1);

    public void setFile(File var1);

    public CompletableFuture<Sticker> create();

    public CompletableFuture<Sticker> create(String var1);
}

