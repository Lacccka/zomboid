/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.InteractionType;
import org.javacord.api.interaction.MessageInteraction;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;

public class MessageInteractionImpl
implements MessageInteraction {
    private final Message message;
    private final long id;
    private final InteractionType type;
    private final String name;
    private final UserImpl user;

    public MessageInteractionImpl(Message message, JsonNode jsonData) {
        this.message = message;
        this.id = jsonData.get("id").asLong();
        this.type = InteractionType.fromValue(jsonData.get("type").asInt());
        this.name = jsonData.get("name").asText();
        UserImpl userTemp = new UserImpl((DiscordApiImpl)message.getApi(), jsonData.get("user"), (MemberImpl)null, null);
        if (jsonData.hasNonNull("member")) {
            MemberImpl member = new MemberImpl((DiscordApiImpl)message.getApi(), (ServerImpl)message.getServer().orElseThrow(AssertionError::new), jsonData.get("member"), userTemp);
            userTemp = (UserImpl)member.getUser();
        }
        this.user = userTemp;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public Message getMessage() {
        return this.message;
    }

    @Override
    public InteractionType getType() {
        return this.type;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public User getUser() {
        return this.user;
    }
}

