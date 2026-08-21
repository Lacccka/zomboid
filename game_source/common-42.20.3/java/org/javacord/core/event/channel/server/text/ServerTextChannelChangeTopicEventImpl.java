/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.text;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.event.channel.server.text.ServerTextChannelChangeTopicEvent;
import org.javacord.core.event.channel.server.text.ServerTextChannelEventImpl;

public class ServerTextChannelChangeTopicEventImpl
extends ServerTextChannelEventImpl
implements ServerTextChannelChangeTopicEvent {
    private final String newTopic;
    private final String oldTopic;

    public ServerTextChannelChangeTopicEventImpl(ServerTextChannel channel, String newTopic, String oldTopic) {
        super(channel);
        this.newTopic = newTopic;
        this.oldTopic = oldTopic;
    }

    @Override
    public String getNewTopic() {
        return this.newTopic;
    }

    @Override
    public String getOldTopic() {
        return this.oldTopic;
    }
}

