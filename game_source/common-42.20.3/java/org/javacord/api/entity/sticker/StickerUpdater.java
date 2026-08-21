/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.sticker;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.sticker.Sticker;
import org.javacord.api.entity.sticker.internal.StickerUpdaterDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class StickerUpdater {
    private final StickerUpdaterDelegate delegate;

    public StickerUpdater(Server server, long id) {
        this.delegate = DelegateFactory.createStickerUpdaterDelegate(server, id);
    }

    public StickerUpdater setName(String name) {
        this.delegate.setName(name);
        return this;
    }

    public StickerUpdater setDescription(String description) {
        this.delegate.setDescription(description);
        return this;
    }

    public StickerUpdater setTags(String tags) {
        this.delegate.setTags(tags);
        return this;
    }

    public CompletableFuture<Sticker> update() {
        return this.delegate.update();
    }

    public CompletableFuture<Sticker> update(String reason) {
        return this.delegate.update(reason);
    }

    public StickerUpdaterDelegate getDelegate() {
        return this.delegate;
    }
}

