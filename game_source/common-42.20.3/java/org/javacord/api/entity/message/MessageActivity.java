/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message;

import java.util.Optional;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageActivityType;

public interface MessageActivity {
    public MessageActivityType getType();

    public Optional<String> getPartyId();

    public Message getMessage();
}

