/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.UnknownRegularServerChannel;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.channel.RegularServerChannelImpl;
import org.javacord.core.entity.server.ServerImpl;

public class UnknownRegularServerChannelImpl
extends RegularServerChannelImpl
implements UnknownRegularServerChannel {
    public UnknownRegularServerChannelImpl(DiscordApiImpl api, ServerImpl server, JsonNode data) {
        super(api, server, data);
    }

    @Override
    public String getMentionTag() {
        return "<#" + this.getIdAsString() + ">";
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o != null && this.getClass() == o.getClass() && this.getId() == ((DiscordEntity)o).getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }

    @Override
    public String toString() {
        return String.format("UnknownRegularServerChannel (id: %s, name: %s)", this.getIdAsString(), this.getName());
    }
}

