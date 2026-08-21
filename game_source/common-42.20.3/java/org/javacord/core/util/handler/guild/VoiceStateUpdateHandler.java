/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.guild;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.VoiceChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberJoinEvent;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberLeaveEvent;
import org.javacord.api.event.server.VoiceStateUpdateEvent;
import org.javacord.api.event.user.UserChangeDeafenedEvent;
import org.javacord.api.event.user.UserChangeMutedEvent;
import org.javacord.api.event.user.UserChangeSelfDeafenedEvent;
import org.javacord.api.event.user.UserChangeSelfMutedEvent;
import org.javacord.core.audio.AudioConnectionImpl;
import org.javacord.core.entity.channel.PrivateChannelImpl;
import org.javacord.core.entity.channel.ServerVoiceChannelImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.Member;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.event.channel.server.voice.ServerVoiceChannelMemberJoinEventImpl;
import org.javacord.core.event.channel.server.voice.ServerVoiceChannelMemberLeaveEventImpl;
import org.javacord.core.event.server.VoiceStateUpdateEventImpl;
import org.javacord.core.event.user.ServerUserEventImpl;
import org.javacord.core.event.user.UserChangeDeafenedEventImpl;
import org.javacord.core.event.user.UserChangeMutedEventImpl;
import org.javacord.core.event.user.UserChangeSelfDeafenedEventImpl;
import org.javacord.core.event.user.UserChangeSelfMutedEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

public class VoiceStateUpdateHandler
extends PacketHandler {
    private static final Logger logger = LoggerUtil.getLogger(VoiceStateUpdateHandler.class);

    public VoiceStateUpdateHandler(DiscordApi api) {
        super(api, true, "VOICE_STATE_UPDATE");
    }

    @Override
    public void handle(JsonNode packet) {
        if (!packet.hasNonNull("user_id")) {
            return;
        }
        long userId = packet.get("user_id").asLong();
        if (packet.hasNonNull("guild_id")) {
            this.handleServerVoiceChannel(packet, userId);
        } else if (packet.hasNonNull("channel_id")) {
            long channelId = packet.get("channel_id").asLong();
            Optional<VoiceChannel> optionalChannel = this.api.getVoiceChannelById(channelId);
            if (optionalChannel.isPresent()) {
                VoiceChannel voiceChannel = optionalChannel.get();
                if (voiceChannel instanceof PrivateChannel) {
                    this.handlePrivateChannel(userId, (PrivateChannelImpl)voiceChannel);
                } else if (voiceChannel.getType() == ChannelType.GROUP_CHANNEL) {
                    logger.info("Received a VOICE_STATE_UPDATE packet for a group channel. This should be impossible.");
                }
            } else {
                LoggerUtil.logMissingChannel(logger, channelId);
            }
        }
        if (this.api.getYourself().getId() == userId) {
            this.handleSelf(packet);
        }
    }

    private void handleSelf(JsonNode packet) {
        String sessionId = packet.get("session_id").asText();
        long channelId = packet.get("channel_id").asLong();
        Optional<ServerVoiceChannel> optionalChannel = this.api.getServerVoiceChannelById(channelId);
        if (optionalChannel.isPresent()) {
            ServerVoiceChannel channel = optionalChannel.get();
            this.dispatchVoiceStateUpdateEvent(channel, channel.getServer(), packet.get("session_id").asText());
            AudioConnectionImpl pendingAudioConnection = this.api.getPendingAudioConnectionByServerId(channel.getServer().getId());
            if (pendingAudioConnection != null) {
                pendingAudioConnection.setSessionId(sessionId);
                pendingAudioConnection.tryConnect();
            }
            channel.getServer().getAudioConnection().ifPresent(connection -> {
                ((AudioConnectionImpl)connection).setSessionId(sessionId);
                ((AudioConnectionImpl)connection).tryConnect();
            });
        } else {
            LoggerUtil.logMissingChannel(logger, channelId);
        }
    }

    private void handleServerVoiceChannel(JsonNode packet, long userId) {
        this.api.getPossiblyUnreadyServerById(packet.get("guild_id").asLong()).map(ServerImpl.class::cast).ifPresent(server -> {
            ServerUserEventImpl event;
            boolean newSelfMuted = packet.get("self_mute").asBoolean();
            boolean oldSelfMuted = server.isSelfMuted(userId);
            boolean newSelfDeafened = packet.get("self_deaf").asBoolean();
            boolean oldSelfDeafened = server.isSelfDeafened(userId);
            boolean newMuted = packet.get("mute").asBoolean();
            boolean oldMuted = server.isMuted(userId);
            boolean newDeafened = packet.get("deaf").asBoolean();
            boolean oldDeafened = server.isDeafened(userId);
            MemberImpl oldMember = server.getRealMemberById(userId).orElse(null);
            if (oldMember == null && !packet.hasNonNull("member")) {
                logger.warn("Received VOICE_STATE_UPDATE packet without member field or member in the cache: {}", (Object)packet);
                return;
            }
            MemberImpl newMember = (oldMember != null ? oldMember : new MemberImpl(this.api, (ServerImpl)server, packet.get("member"), null)).setMuted(newMuted).setDeafened(newDeafened).setSelfMuted(newSelfMuted).setSelfDeafened(newSelfDeafened);
            this.api.addMemberToCacheOrReplaceExisting(newMember);
            Optional<ServerVoiceChannelImpl> oldChannel = server.getConnectedVoiceChannel(userId).map(ServerVoiceChannelImpl.class::cast);
            Optional<Object> newChannel = packet.hasNonNull("channel_id") ? server.getVoiceChannelById(packet.get("channel_id").asLong()).map(ServerVoiceChannelImpl.class::cast) : Optional.empty();
            if (!newChannel.equals(oldChannel)) {
                if (userId == this.api.getYourself().getId()) {
                    if (oldChannel.isPresent() && newChannel.isPresent()) {
                        oldChannel.get().getServer().getAudioConnection().ifPresent(audioConnection -> audioConnection.moveTo((ServerVoiceChannel)newChannel.get()));
                    } else if (oldChannel.isPresent()) {
                        oldChannel.get().getServer().getAudioConnection().ifPresent(audioConnection -> {
                            CompletableFuture<Void> future = ((AudioConnectionImpl)audioConnection).getDisconnectFuture();
                            if (future == null || !future.isDone()) {
                                audioConnection.close();
                            }
                        });
                    }
                }
                oldChannel.ifPresent(channel -> {
                    channel.removeConnectedUser(userId);
                    this.dispatchServerVoiceChannelMemberLeaveEvent(newMember, newChannel.orElse(null), (ServerVoiceChannel)channel, (Server)server);
                });
                newChannel.ifPresent(channel -> {
                    channel.addConnectedUser(userId);
                    this.dispatchServerVoiceChannelMemberJoinEvent(newMember, (ServerVoiceChannel)channel, oldChannel.orElse(null), (Server)server);
                });
            }
            if (newSelfMuted != oldSelfMuted) {
                event = new UserChangeSelfMutedEventImpl(newMember, oldMember);
                this.api.getEventDispatcher().dispatchUserChangeSelfMutedEvent((DispatchQueueSelector)server, (Server)server, newMember.getUser(), (UserChangeSelfMutedEvent)((Object)event));
            }
            if (newSelfDeafened != oldSelfDeafened) {
                event = new UserChangeSelfDeafenedEventImpl(newMember, oldMember);
                this.api.getEventDispatcher().dispatchUserChangeSelfDeafenedEvent((DispatchQueueSelector)server, (Server)server, newMember.getUser(), (UserChangeSelfDeafenedEvent)((Object)event));
            }
            if (newMuted != oldMuted) {
                event = new UserChangeMutedEventImpl(newMember, oldMember);
                this.api.getEventDispatcher().dispatchUserChangeMutedEvent((DispatchQueueSelector)server, (Server)server, newMember.getUser(), (UserChangeMutedEvent)((Object)event));
            }
            if (newDeafened != oldDeafened) {
                event = new UserChangeDeafenedEventImpl(newMember, oldMember);
                this.api.getEventDispatcher().dispatchUserChangeDeafenedEvent((DispatchQueueSelector)server, (Server)server, newMember.getUser(), (UserChangeDeafenedEvent)((Object)event));
            }
        });
    }

    private void handlePrivateChannel(long userId, PrivateChannelImpl channel) {
    }

    private void dispatchVoiceStateUpdateEvent(ServerVoiceChannel newChannel, Server server, String sessionId) {
        VoiceStateUpdateEventImpl event = new VoiceStateUpdateEventImpl(newChannel, sessionId);
        this.api.getEventDispatcher().dispatchVoiceStateUpdateEvent((DispatchQueueSelector)((Object)server), newChannel, (VoiceStateUpdateEvent)event);
    }

    private void dispatchServerVoiceChannelMemberJoinEvent(Member member, ServerVoiceChannel newChannel, ServerVoiceChannel oldChannel, Server server) {
        ServerVoiceChannelMemberJoinEventImpl event = new ServerVoiceChannelMemberJoinEventImpl(member, newChannel, oldChannel);
        this.api.getEventDispatcher().dispatchServerVoiceChannelMemberJoinEvent((DispatchQueueSelector)((Object)server), server, newChannel, member.getUser(), (ServerVoiceChannelMemberJoinEvent)event);
    }

    private void dispatchServerVoiceChannelMemberLeaveEvent(Member member, ServerVoiceChannel newChannel, ServerVoiceChannel oldChannel, Server server) {
        ServerVoiceChannelMemberLeaveEventImpl event = new ServerVoiceChannelMemberLeaveEventImpl(member, newChannel, oldChannel);
        this.api.getEventDispatcher().dispatchServerVoiceChannelMemberLeaveEvent((DispatchQueueSelector)((Object)server), server, oldChannel, member.getUser(), (ServerVoiceChannelMemberLeaveEvent)event);
    }
}

