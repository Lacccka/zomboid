/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageActivity;
import org.javacord.api.entity.message.MessageActivityType;

public class MessageActivityImpl
implements MessageActivity {
    private final MessageActivityType type;
    private final String partyId;
    private final Message message;

    public MessageActivityImpl(Message message, JsonNode data) {
        this.type = MessageActivityType.getMessageActivityTypeById(data.get("type").asInt());
        this.partyId = data.has("party_id") ? data.get("party_id").asText() : null;
        this.message = message;
    }

    @Override
    public MessageActivityType getType() {
        return this.type;
    }

    @Override
    public Optional<String> getPartyId() {
        return Optional.ofNullable(this.partyId);
    }

    @Override
    public Message getMessage() {
        return this.message;
    }

    public String toString() {
        return String.format("MessageActivity (type: %s, partyId: %s, message: %s)", new Object[]{this.getType(), this.getPartyId().orElse("none"), this.getMessage()});
    }
}

