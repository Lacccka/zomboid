/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.sticker;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.Deletable;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Nameable;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.sticker.StickerBuilder;
import org.javacord.api.entity.sticker.StickerFormatType;
import org.javacord.api.entity.sticker.StickerType;
import org.javacord.api.entity.sticker.StickerUpdater;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.server.sticker.StickerAttachableListenerManager;

public interface Sticker
extends DiscordEntity,
Nameable,
Deletable,
StickerAttachableListenerManager {
    public Optional<Long> getPackId();

    public String getDescription();

    public String getTags();

    public StickerType getType();

    public StickerFormatType getFormatType();

    public Optional<Boolean> isAvailable();

    public Optional<Long> getServerId();

    public Optional<Server> getServer();

    public Optional<User> getUser();

    public Optional<Integer> getSortValue();

    default public CompletableFuture<Sticker> update(String name, String description, String tags) {
        return this.update(name, description, tags, null);
    }

    default public CompletableFuture<Sticker> update(String name, String description, String tags, String reason) {
        return this.createUpdater().setName(name).setDescription(description).setTags(tags).update(reason);
    }

    default public CompletableFuture<Sticker> updateName(String name) {
        return this.updateName(name, null);
    }

    default public CompletableFuture<Sticker> updateName(String name, String reason) {
        return this.createUpdater().setName(name).update(reason);
    }

    default public CompletableFuture<Sticker> updateDescription(String description) {
        return this.createUpdater().setDescription(description).update(null);
    }

    default public CompletableFuture<Sticker> updateDescription(String description, String reason) {
        return this.createUpdater().setDescription(description).update(reason);
    }

    default public CompletableFuture<Sticker> updateTags(String tags) {
        return this.updateTags(tags, null);
    }

    default public CompletableFuture<Sticker> updateTags(String tags, String reason) {
        return this.createUpdater().setTags(tags).update(reason);
    }

    public StickerUpdater createUpdater();

    public static CompletableFuture<Sticker> create(Server server, String name, String tags, File file) {
        return Sticker.create(server, name, "", tags, file);
    }

    public static CompletableFuture<Sticker> create(Server server, String name, String description, String tags, File file) {
        return Sticker.create(server, name, description, tags, file, null);
    }

    public static CompletableFuture<Sticker> create(Server server, String name, String description, String tags, File file, String reason) {
        return new StickerBuilder(server).setName(name).setDescription(description).setTags(tags).setFile(file).create(reason);
    }
}

