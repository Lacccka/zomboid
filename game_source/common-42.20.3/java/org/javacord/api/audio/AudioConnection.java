/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.audio;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.audio.SpeakingFlag;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.listener.audio.AudioConnectionAttachableListenerManager;

public interface AudioConnection
extends AudioConnectionAttachableListenerManager {
    public CompletableFuture<Void> moveTo(ServerVoiceChannel var1);

    public CompletableFuture<Void> moveTo(ServerVoiceChannel var1, boolean var2, boolean var3);

    public CompletableFuture<Void> close();

    public Optional<AudioSource> getAudioSource();

    public void setAudioSource(AudioSource var1);

    public void removeAudioSource();

    public ServerVoiceChannel getChannel();

    public boolean isSelfMuted();

    public void setSelfMuted(boolean var1);

    public boolean isSelfDeafened();

    public void setSelfDeafened(boolean var1);

    public boolean isPrioritySpeaking();

    public void setPrioritySpeaking(boolean var1);

    public Set<SpeakingFlag> getSpeakingFlags();

    default public Server getServer() {
        return this.getChannel().getServer();
    }
}

