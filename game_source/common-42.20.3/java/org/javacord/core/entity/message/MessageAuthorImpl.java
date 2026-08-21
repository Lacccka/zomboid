/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;
import java.util.Optional;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.user.User;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;

public class MessageAuthorImpl
implements MessageAuthor {
    private final Message message;
    private final UserImpl user;
    private final MemberImpl member;
    private final Long webhookId;
    private final long id;
    private final String name;
    private final String discriminator;
    private final String avatarId;

    public MessageAuthorImpl(Message message, Long webhookId, JsonNode messageJson) {
        this.message = message;
        JsonNode authorJson = messageJson.get("author");
        this.id = authorJson.get("id").asLong();
        this.name = authorJson.get("username").asText();
        this.discriminator = authorJson.get("discriminator").asText();
        String string = this.avatarId = authorJson.has("avatar") && !authorJson.get("avatar").isNull() ? authorJson.get("avatar").asText() : null;
        if (webhookId == null) {
            if (messageJson.hasNonNull("member")) {
                this.user = new UserImpl((DiscordApiImpl)this.getApi(), authorJson, messageJson.get("member"), (ServerImpl)message.getServer().orElseThrow(AssertionError::new));
                this.member = this.user.getMember().orElseThrow(AssertionError::new);
            } else {
                this.user = new UserImpl((DiscordApiImpl)this.getApi(), authorJson, (MemberImpl)null, null);
                this.member = null;
            }
        } else {
            this.user = null;
            this.member = null;
        }
        this.webhookId = webhookId;
    }

    @Override
    public DiscordApi getApi() {
        return this.message.getApi();
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public Optional<Long> getWebhookId() {
        return Optional.ofNullable(this.webhookId);
    }

    @Override
    public Message getMessage() {
        return this.message;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Optional<String> getDiscriminator() {
        return this.isUser() ? Optional.of(this.discriminator) : Optional.empty();
    }

    @Override
    public Icon getAvatar() {
        return UserImpl.getAvatar(this.message.getApi(), this.avatarId, this.discriminator, this.id);
    }

    @Override
    public Icon getAvatar(int size) {
        return UserImpl.getAvatar(this.message.getApi(), this.avatarId, this.discriminator, this.id, size);
    }

    @Override
    public boolean isUser() {
        return this.webhookId == null;
    }

    @Override
    public Optional<User> asUser() {
        return Optional.ofNullable(this.user);
    }

    @Override
    public boolean isWebhook() {
        return this.webhookId != null;
    }

    public boolean equals(Object o) {
        return this == o || o != null && this.getClass() == o.getClass() && this.getId() == ((DiscordEntity)o).getId();
    }

    public int hashCode() {
        return Objects.hash(this.getId());
    }

    public String toString() {
        return String.format("MessageAuthor (id: %s, name: %s)", this.getIdAsString(), this.getName());
    }
}

