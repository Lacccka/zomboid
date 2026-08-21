/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.sticker;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.sticker.Sticker;
import org.javacord.api.entity.sticker.internal.StickerBuilderDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class StickerBuilder {
    private final StickerBuilderDelegate delegate;

    public StickerBuilder(Server server) {
        this.delegate = DelegateFactory.createStickerBuilderDelegate(server);
    }

    public StickerBuilder setName(String name) {
        this.delegate.setName(name);
        return this;
    }

    public StickerBuilder setDescription(String description) {
        this.delegate.setDescription(description);
        return this;
    }

    public StickerBuilder setTags(String tags) {
        this.delegate.setTags(tags);
        return this;
    }

    public StickerBuilder setFile(File file) {
        this.delegate.setFile(file);
        return this;
    }

    public CompletableFuture<Sticker> create() {
        return this.delegate.create();
    }

    public CompletableFuture<Sticker> create(String reason) {
        return this.delegate.create(reason);
    }

    public StickerBuilderDelegate getDelegate() {
        return this.delegate;
    }
}

