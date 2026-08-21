/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.audio;

import java.util.AbstractCollection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.audio.SpeakingFlag;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.listener.audio.InternalAudioConnectionAttachableListenerManager;
import org.javacord.core.util.concurrent.BlockingReference;
import org.javacord.core.util.gateway.AudioWebSocketAdapter;
import org.javacord.core.util.logging.LoggerUtil;

public class AudioConnectionImpl
implements AudioConnection,
InternalAudioConnectionAttachableListenerManager {
    private static final AtomicInteger idCounter = new AtomicInteger(0);
    private static final Logger logger = LoggerUtil.getLogger(AudioConnectionImpl.class);
    private final DiscordApiImpl api;
    private volatile ServerVoiceChannel channel;
    private final CompletableFuture<AudioConnection> readyFuture;
    private CompletableFuture<Void> movingFuture;
    private CompletableFuture<Void> disconnectFuture;
    private final BlockingReference<AudioSource> currentSource = new BlockingReference();
    private final long id;
    private volatile AudioWebSocketAdapter websocketAdapter;
    private volatile boolean connectingOrConnected = false;
    private volatile String sessionId;
    private volatile String token;
    private volatile String endpoint;
    private volatile EnumSet<SpeakingFlag> speakingFlags = EnumSet.noneOf(SpeakingFlag.class);
    private volatile boolean muted;
    private volatile boolean deafened;

    public AudioConnectionImpl(ServerVoiceChannel channel, CompletableFuture<AudioConnection> readyFuture, boolean muted, boolean deafened) {
        this.channel = channel;
        this.readyFuture = readyFuture;
        this.id = idCounter.getAndIncrement();
        this.api = (DiscordApiImpl)channel.getApi();
        this.api.getWebSocketAdapter().sendVoiceStateUpdate(channel.getServer(), this.getChannel(), muted, deafened);
    }

    public CompletableFuture<AudioConnection> getReadyFuture() {
        return this.readyFuture;
    }

    public CompletableFuture<Void> getDisconnectFuture() {
        return this.disconnectFuture;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public String getToken() {
        return this.token;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public void setChannel(ServerVoiceChannel channel) {
        this.channel = channel;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setSpeaking(boolean speaking) {
        Object newSpeakingFlags = this.speakingFlags.clone();
        if (speaking) {
            ((AbstractCollection)newSpeakingFlags).add(SpeakingFlag.SPEAKING);
        } else {
            ((AbstractCollection)newSpeakingFlags).remove((Object)SpeakingFlag.SPEAKING);
        }
        this.setSpeakingFlags((EnumSet<SpeakingFlag>)newSpeakingFlags);
    }

    @Override
    public boolean isPrioritySpeaking() {
        return this.speakingFlags.contains((Object)SpeakingFlag.PRIORITY_SPEAKER);
    }

    @Override
    public void setPrioritySpeaking(boolean prioritySpeaking) {
        Object newSpeakingFlags = this.speakingFlags.clone();
        if (prioritySpeaking) {
            ((AbstractCollection)newSpeakingFlags).add(SpeakingFlag.PRIORITY_SPEAKER);
        } else {
            ((AbstractCollection)newSpeakingFlags).remove((Object)SpeakingFlag.PRIORITY_SPEAKER);
        }
        this.setSpeakingFlags((EnumSet<SpeakingFlag>)newSpeakingFlags);
    }

    @Override
    public Set<SpeakingFlag> getSpeakingFlags() {
        return Collections.unmodifiableSet(this.speakingFlags);
    }

    public void setSpeakingFlags(EnumSet<SpeakingFlag> speakingFlags) {
        if (!speakingFlags.equals(this.speakingFlags)) {
            this.speakingFlags = speakingFlags;
            this.websocketAdapter.sendSpeaking();
        }
    }

    public synchronized boolean tryConnect() {
        if (this.movingFuture != null && !this.movingFuture.isDone()) {
            this.movingFuture.complete(null);
            return true;
        }
        if (this.connectingOrConnected || this.sessionId == null || this.token == null || this.endpoint == null) {
            return false;
        }
        this.connectingOrConnected = true;
        logger.debug("Received all information required to connect to voice channel {}", (Object)this.getChannel());
        this.websocketAdapter = new AudioWebSocketAdapter(this);
        this.channel = this.channel.getCurrentCachedInstance().flatMap(Channel::asServerVoiceChannel).orElse(this.channel);
        return true;
    }

    public void reconnect() {
        this.websocketAdapter = null;
        this.sessionId = null;
        this.token = null;
        this.endpoint = null;
        this.connectingOrConnected = false;
        this.api.getWebSocketAdapter().sendVoiceStateUpdate(this.getChannel().getServer(), this.getChannel(), this.isSelfMuted(), this.isSelfDeafened());
    }

    public AudioSource getCurrentAudioSourceBlocking() throws InterruptedException {
        return this.currentSource.get();
    }

    public AudioSource getCurrentAudioSourceBlocking(long timeout2, TimeUnit unit) throws InterruptedException {
        return this.currentSource.get(timeout2, unit);
    }

    @Override
    public DiscordApi getApi() {
        return this.getChannel().getApi();
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public CompletableFuture<Void> moveTo(ServerVoiceChannel destChannel) {
        return this.moveTo(destChannel, this.muted, this.deafened);
    }

    @Override
    public CompletableFuture<Void> moveTo(ServerVoiceChannel destChannel, boolean selfMute, boolean selfDeafen) {
        this.movingFuture = new CompletableFuture();
        if (!destChannel.getServer().equals(this.getChannel().getServer())) {
            this.movingFuture.completeExceptionally(new IllegalArgumentException("Cannot move to a voice channel not in the same server!"));
            return this.movingFuture;
        }
        if (destChannel.equals(this.getChannel())) {
            this.movingFuture.complete(null);
            return this.movingFuture;
        }
        this.api.getWebSocketAdapter().sendVoiceStateUpdate(this.getChannel().getServer(), destChannel, selfMute, selfDeafen);
        return this.movingFuture.thenRun(() -> this.setChannel(destChannel));
    }

    @Override
    public CompletableFuture<Void> close() {
        this.disconnectFuture = new CompletableFuture();
        this.websocketAdapter.disconnect();
        this.api.getWebSocketAdapter().sendVoiceStateUpdate(this.getChannel().getServer(), null, this.muted, this.deafened);
        ((ServerImpl)this.getChannel().getServer()).removeAudioConnection(this);
        return this.disconnectFuture;
    }

    @Override
    public Optional<AudioSource> getAudioSource() {
        try {
            return Optional.ofNullable(this.currentSource.get(0L, TimeUnit.MILLISECONDS));
        }
        catch (InterruptedException e) {
            return Optional.empty();
        }
    }

    @Override
    public void setAudioSource(AudioSource source2) {
        this.currentSource.set(source2);
    }

    @Override
    public void removeAudioSource() {
        this.currentSource.set(null);
    }

    @Override
    public ServerVoiceChannel getChannel() {
        return this.channel.getCurrentCachedInstance().flatMap(Channel::asServerVoiceChannel).orElse(this.channel);
    }

    @Override
    public boolean isSelfMuted() {
        return this.muted;
    }

    @Override
    public void setSelfMuted(boolean muted) {
        this.muted = muted;
        this.api.getWebSocketAdapter().sendVoiceStateUpdate(this.getChannel().getServer(), this.getChannel(), muted, this.deafened);
    }

    @Override
    public boolean isSelfDeafened() {
        return this.deafened;
    }

    @Override
    public void setSelfDeafened(boolean deafened) {
        this.deafened = deafened;
        this.api.getWebSocketAdapter().sendVoiceStateUpdate(this.getChannel().getServer(), this.getChannel(), this.muted, deafened);
    }

    public String toString() {
        return String.format("AudioConnection (channel: %#s, sessionId: %s, endpoint: %s)", this.getChannel(), this.sessionId, this.endpoint);
    }
}

