/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.sticker;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.sticker.Sticker;
import org.javacord.api.entity.sticker.StickerFormatType;
import org.javacord.api.entity.sticker.StickerType;
import org.javacord.api.entity.sticker.StickerUpdater;
import org.javacord.api.entity.user.User;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.listener.server.sticker.InternalStickerAttachableListenerManager;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class StickerImpl
implements Sticker,
InternalStickerAttachableListenerManager {
    private final DiscordApiImpl api;
    private final long id;
    private final Long packId;
    private final StickerType type;
    private final StickerFormatType formatType;
    private final Boolean available;
    private final Long serverId;
    private final User user;
    private final Integer sortValue;
    private String name;
    private String description;
    private String tags;

    public StickerImpl(DiscordApiImpl api, JsonNode data) {
        this.api = api;
        this.id = data.get("id").asLong();
        this.packId = data.has("pack_id") ? Long.valueOf(data.get("pack_id").asLong()) : null;
        this.name = data.get("name").asText();
        this.description = data.hasNonNull("description") ? data.get("description").asText() : "";
        this.tags = data.get("tags").asText();
        this.type = StickerType.fromId(data.get("type").asInt());
        this.formatType = StickerFormatType.fromId(data.get("format_type").asInt());
        this.available = data.has("available") ? Boolean.valueOf(data.get("available").asBoolean()) : null;
        this.serverId = data.has("guild_id") ? Long.valueOf(data.get("guild_id").asLong()) : null;
        this.user = data.has("user") ? new UserImpl(api, data.get("user"), (MemberImpl)null, (ServerImpl)this.getServer().get()) : null;
        this.sortValue = data.has("sort_value") ? Integer.valueOf(data.get("sort_value").asInt()) : null;
    }

    @Override
    public DiscordApi getApi() {
        return this.api;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Optional<Long> getPackId() {
        return Optional.ofNullable(this.packId);
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String getTags() {
        return this.tags;
    }

    @Override
    public StickerType getType() {
        return this.type;
    }

    @Override
    public StickerFormatType getFormatType() {
        return this.formatType;
    }

    @Override
    public Optional<Boolean> isAvailable() {
        return Optional.ofNullable(this.available);
    }

    @Override
    public Optional<Long> getServerId() {
        return Optional.ofNullable(this.serverId);
    }

    @Override
    public Optional<Server> getServer() {
        return this.getServerId().flatMap(this.api::getServerById);
    }

    @Override
    public Optional<User> getUser() {
        return Optional.ofNullable(this.user);
    }

    @Override
    public Optional<Integer> getSortValue() {
        return Optional.ofNullable(this.sortValue);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    @Override
    public CompletableFuture<Void> delete() {
        if (this.getServer().isPresent()) {
            return this.delete(null);
        }
        throw new IllegalArgumentException("The server is not present.");
    }

    @Override
    public CompletableFuture<Void> delete(String reason) {
        if (this.getServer().isPresent()) {
            return new RestRequest(this.api, RestMethod.DELETE, RestEndpoint.SERVER_STICKER).setUrlParameters(this.getServer().get().getIdAsString(), String.valueOf(this.id)).setAuditLogReason(reason).execute(result -> null);
        }
        throw new IllegalArgumentException("The server is not present.");
    }

    @Override
    public StickerUpdater createUpdater() {
        if (this.getServer().isPresent()) {
            return new StickerUpdater(this.getServer().get(), this.id);
        }
        throw new IllegalArgumentException("The server is not present.");
    }
}

