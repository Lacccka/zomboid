/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.audio;

import org.javacord.api.audio.AudioConnection;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.Event;

public interface AudioSourceEvent
extends Event {
    public AudioSource getSource();

    public AudioConnection getConnection();

    default public ServerVoiceChannel getChannel() {
        return this.getConnection().getChannel();
    }

    default public Server getServer() {
        return this.getConnection().getServer();
    }
}

