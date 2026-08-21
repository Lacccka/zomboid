/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.event;

import java.util.ArrayList;
import java.util.Collection;
import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerStageVoiceChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.sticker.Sticker;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.Webhook;
import org.javacord.api.event.audio.AudioSourceFinishedEvent;
import org.javacord.api.event.channel.server.ServerChannelChangeNameEvent;
import org.javacord.api.event.channel.server.ServerChannelChangeNsfwFlagEvent;
import org.javacord.api.event.channel.server.ServerChannelChangeOverwrittenPermissionsEvent;
import org.javacord.api.event.channel.server.ServerChannelChangePositionEvent;
import org.javacord.api.event.channel.server.ServerChannelCreateEvent;
import org.javacord.api.event.channel.server.ServerChannelDeleteEvent;
import org.javacord.api.event.channel.server.invite.ServerChannelInviteCreateEvent;
import org.javacord.api.event.channel.server.invite.ServerChannelInviteDeleteEvent;
import org.javacord.api.event.channel.server.text.ServerTextChannelChangeDefaultAutoArchiveDurationEvent;
import org.javacord.api.event.channel.server.text.ServerTextChannelChangeSlowmodeEvent;
import org.javacord.api.event.channel.server.text.ServerTextChannelChangeTopicEvent;
import org.javacord.api.event.channel.server.text.WebhooksUpdateEvent;
import org.javacord.api.event.channel.server.thread.ServerPrivateThreadJoinEvent;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeArchiveTimestampEvent;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeArchivedEvent;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeAutoArchiveDurationEvent;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeInvitableEvent;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeLastMessageIdEvent;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeLockedEvent;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeMemberCountEvent;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeMessageCountEvent;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeRateLimitPerUserEvent;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeTotalMessageSentEvent;
import org.javacord.api.event.channel.server.voice.ServerStageVoiceChannelChangeTopicEvent;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelChangeBitrateEvent;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelChangeNsfwEvent;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelChangeUserLimitEvent;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberJoinEvent;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberLeaveEvent;
import org.javacord.api.event.channel.thread.ThreadCreateEvent;
import org.javacord.api.event.channel.thread.ThreadDeleteEvent;
import org.javacord.api.event.channel.thread.ThreadListSyncEvent;
import org.javacord.api.event.channel.thread.ThreadMembersUpdateEvent;
import org.javacord.api.event.channel.thread.ThreadUpdateEvent;
import org.javacord.api.event.channel.user.PrivateChannelCreateEvent;
import org.javacord.api.event.channel.user.PrivateChannelDeleteEvent;
import org.javacord.api.event.connection.LostConnectionEvent;
import org.javacord.api.event.connection.ReconnectEvent;
import org.javacord.api.event.connection.ResumeEvent;
import org.javacord.api.event.interaction.AutocompleteCreateEvent;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.event.interaction.InteractionCreateEvent;
import org.javacord.api.event.interaction.MessageComponentCreateEvent;
import org.javacord.api.event.interaction.MessageContextMenuCommandEvent;
import org.javacord.api.event.interaction.ModalSubmitEvent;
import org.javacord.api.event.interaction.SelectMenuChooseEvent;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.interaction.UserContextMenuCommandEvent;
import org.javacord.api.event.message.CachedMessagePinEvent;
import org.javacord.api.event.message.CachedMessageUnpinEvent;
import org.javacord.api.event.message.ChannelPinsUpdateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.MessageDeleteEvent;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.api.event.message.MessageReplyEvent;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveAllEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;
import org.javacord.api.event.server.ApplicationCommandPermissionsUpdateEvent;
import org.javacord.api.event.server.ServerBecomesAvailableEvent;
import org.javacord.api.event.server.ServerBecomesUnavailableEvent;
import org.javacord.api.event.server.ServerChangeAfkChannelEvent;
import org.javacord.api.event.server.ServerChangeAfkTimeoutEvent;
import org.javacord.api.event.server.ServerChangeBoostCountEvent;
import org.javacord.api.event.server.ServerChangeBoostLevelEvent;
import org.javacord.api.event.server.ServerChangeDefaultMessageNotificationLevelEvent;
import org.javacord.api.event.server.ServerChangeDescriptionEvent;
import org.javacord.api.event.server.ServerChangeDiscoverySplashEvent;
import org.javacord.api.event.server.ServerChangeExplicitContentFilterLevelEvent;
import org.javacord.api.event.server.ServerChangeIconEvent;
import org.javacord.api.event.server.ServerChangeModeratorsOnlyChannelEvent;
import org.javacord.api.event.server.ServerChangeMultiFactorAuthenticationLevelEvent;
import org.javacord.api.event.server.ServerChangeNameEvent;
import org.javacord.api.event.server.ServerChangeNsfwLevelEvent;
import org.javacord.api.event.server.ServerChangeOwnerEvent;
import org.javacord.api.event.server.ServerChangePreferredLocaleEvent;
import org.javacord.api.event.server.ServerChangeRegionEvent;
import org.javacord.api.event.server.ServerChangeRulesChannelEvent;
import org.javacord.api.event.server.ServerChangeServerFeaturesEvent;
import org.javacord.api.event.server.ServerChangeSplashEvent;
import org.javacord.api.event.server.ServerChangeSystemChannelEvent;
import org.javacord.api.event.server.ServerChangeVanityUrlCodeEvent;
import org.javacord.api.event.server.ServerChangeVerificationLevelEvent;
import org.javacord.api.event.server.ServerJoinEvent;
import org.javacord.api.event.server.ServerLeaveEvent;
import org.javacord.api.event.server.VoiceServerUpdateEvent;
import org.javacord.api.event.server.VoiceStateUpdateEvent;
import org.javacord.api.event.server.emoji.KnownCustomEmojiChangeNameEvent;
import org.javacord.api.event.server.emoji.KnownCustomEmojiChangeWhitelistedRolesEvent;
import org.javacord.api.event.server.emoji.KnownCustomEmojiCreateEvent;
import org.javacord.api.event.server.emoji.KnownCustomEmojiDeleteEvent;
import org.javacord.api.event.server.member.ServerMemberBanEvent;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.event.server.member.ServerMemberLeaveEvent;
import org.javacord.api.event.server.member.ServerMemberUnbanEvent;
import org.javacord.api.event.server.member.ServerMembersChunkEvent;
import org.javacord.api.event.server.role.RoleChangeColorEvent;
import org.javacord.api.event.server.role.RoleChangeHoistEvent;
import org.javacord.api.event.server.role.RoleChangeMentionableEvent;
import org.javacord.api.event.server.role.RoleChangeNameEvent;
import org.javacord.api.event.server.role.RoleChangePermissionsEvent;
import org.javacord.api.event.server.role.RoleChangePositionEvent;
import org.javacord.api.event.server.role.RoleCreateEvent;
import org.javacord.api.event.server.role.RoleDeleteEvent;
import org.javacord.api.event.server.role.UserRoleAddEvent;
import org.javacord.api.event.server.role.UserRoleRemoveEvent;
import org.javacord.api.event.server.sticker.StickerChangeDescriptionEvent;
import org.javacord.api.event.server.sticker.StickerChangeNameEvent;
import org.javacord.api.event.server.sticker.StickerChangeTagsEvent;
import org.javacord.api.event.server.sticker.StickerCreateEvent;
import org.javacord.api.event.server.sticker.StickerDeleteEvent;
import org.javacord.api.event.user.UserChangeActivityEvent;
import org.javacord.api.event.user.UserChangeAvatarEvent;
import org.javacord.api.event.user.UserChangeDeafenedEvent;
import org.javacord.api.event.user.UserChangeDiscriminatorEvent;
import org.javacord.api.event.user.UserChangeMutedEvent;
import org.javacord.api.event.user.UserChangeNameEvent;
import org.javacord.api.event.user.UserChangeNicknameEvent;
import org.javacord.api.event.user.UserChangePendingEvent;
import org.javacord.api.event.user.UserChangeSelfDeafenedEvent;
import org.javacord.api.event.user.UserChangeSelfMutedEvent;
import org.javacord.api.event.user.UserChangeServerAvatarEvent;
import org.javacord.api.event.user.UserChangeStatusEvent;
import org.javacord.api.event.user.UserChangeTimeoutEvent;
import org.javacord.api.event.user.UserStartTypingEvent;
import org.javacord.api.listener.audio.AudioConnectionAttachableListenerManager;
import org.javacord.api.listener.audio.AudioSourceAttachableListenerManager;
import org.javacord.api.listener.audio.AudioSourceFinishedListener;
import org.javacord.api.listener.channel.ServerThreadChannelAttachableListenerManager;
import org.javacord.api.listener.channel.TextChannelAttachableListenerManager;
import org.javacord.api.listener.channel.server.ChannelCategoryAttachableListenerManager;
import org.javacord.api.listener.channel.server.ServerChannelAttachableListenerManager;
import org.javacord.api.listener.channel.server.ServerChannelChangeNameListener;
import org.javacord.api.listener.channel.server.ServerChannelChangeNsfwFlagListener;
import org.javacord.api.listener.channel.server.ServerChannelChangeOverwrittenPermissionsListener;
import org.javacord.api.listener.channel.server.ServerChannelChangePositionListener;
import org.javacord.api.listener.channel.server.ServerChannelCreateListener;
import org.javacord.api.listener.channel.server.ServerChannelDeleteListener;
import org.javacord.api.listener.channel.server.invite.ServerChannelInviteCreateListener;
import org.javacord.api.listener.channel.server.invite.ServerChannelInviteDeleteListener;
import org.javacord.api.listener.channel.server.text.ServerTextChannelAttachableListenerManager;
import org.javacord.api.listener.channel.server.text.ServerTextChannelChangeDefaultAutoArchiveDurationListener;
import org.javacord.api.listener.channel.server.text.ServerTextChannelChangeSlowmodeListener;
import org.javacord.api.listener.channel.server.text.ServerTextChannelChangeTopicListener;
import org.javacord.api.listener.channel.server.text.WebhooksUpdateListener;
import org.javacord.api.listener.channel.server.thread.ServerThreadChannelCreateListener;
import org.javacord.api.listener.channel.server.thread.ServerThreadChannelDeleteListener;
import org.javacord.api.listener.channel.server.thread.ServerThreadChannelMembersUpdateListener;
import org.javacord.api.listener.channel.server.thread.ServerThreadChannelUpdateListener;
import org.javacord.api.listener.channel.server.thread.ServerThreadListSyncListener;
import org.javacord.api.listener.channel.server.voice.ServerStageVoiceChannelAttachableListenerManager;
import org.javacord.api.listener.channel.server.voice.ServerStageVoiceChannelChangeTopicListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelAttachableListenerManager;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelChangeBitrateListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelChangeNsfwListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelChangeUserLimitListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberJoinListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberLeaveListener;
import org.javacord.api.listener.channel.user.PrivateChannelAttachableListenerManager;
import org.javacord.api.listener.channel.user.PrivateChannelCreateListener;
import org.javacord.api.listener.channel.user.PrivateChannelDeleteListener;
import org.javacord.api.listener.connection.LostConnectionListener;
import org.javacord.api.listener.connection.ReconnectListener;
import org.javacord.api.listener.connection.ResumeListener;
import org.javacord.api.listener.interaction.AutocompleteCreateListener;
import org.javacord.api.listener.interaction.ButtonClickListener;
import org.javacord.api.listener.interaction.InteractionCreateListener;
import org.javacord.api.listener.interaction.MessageComponentCreateListener;
import org.javacord.api.listener.interaction.MessageContextMenuCommandListener;
import org.javacord.api.listener.interaction.ModalSubmitListener;
import org.javacord.api.listener.interaction.SelectMenuChooseListener;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import org.javacord.api.listener.interaction.UserContextMenuCommandListener;
import org.javacord.api.listener.message.CachedMessagePinListener;
import org.javacord.api.listener.message.CachedMessageUnpinListener;
import org.javacord.api.listener.message.ChannelPinsUpdateListener;
import org.javacord.api.listener.message.MessageAttachableListenerManager;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.MessageDeleteListener;
import org.javacord.api.listener.message.MessageEditListener;
import org.javacord.api.listener.message.MessageReplyListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveAllListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.javacord.api.listener.server.ApplicationCommandPermissionsUpdateListener;
import org.javacord.api.listener.server.ServerAttachableListenerManager;
import org.javacord.api.listener.server.ServerBecomesAvailableListener;
import org.javacord.api.listener.server.ServerBecomesUnavailableListener;
import org.javacord.api.listener.server.ServerChangeAfkChannelListener;
import org.javacord.api.listener.server.ServerChangeAfkTimeoutListener;
import org.javacord.api.listener.server.ServerChangeBoostCountListener;
import org.javacord.api.listener.server.ServerChangeBoostLevelListener;
import org.javacord.api.listener.server.ServerChangeDefaultMessageNotificationLevelListener;
import org.javacord.api.listener.server.ServerChangeDescriptionListener;
import org.javacord.api.listener.server.ServerChangeDiscoverySplashListener;
import org.javacord.api.listener.server.ServerChangeExplicitContentFilterLevelListener;
import org.javacord.api.listener.server.ServerChangeIconListener;
import org.javacord.api.listener.server.ServerChangeModeratorsOnlyChannelListener;
import org.javacord.api.listener.server.ServerChangeMultiFactorAuthenticationLevelListener;
import org.javacord.api.listener.server.ServerChangeNameListener;
import org.javacord.api.listener.server.ServerChangeNsfwLevelListener;
import org.javacord.api.listener.server.ServerChangeOwnerListener;
import org.javacord.api.listener.server.ServerChangePreferredLocaleListener;
import org.javacord.api.listener.server.ServerChangeRegionListener;
import org.javacord.api.listener.server.ServerChangeRulesChannelListener;
import org.javacord.api.listener.server.ServerChangeServerFeatureListener;
import org.javacord.api.listener.server.ServerChangeSplashListener;
import org.javacord.api.listener.server.ServerChangeSystemChannelListener;
import org.javacord.api.listener.server.ServerChangeVanityUrlCodeListener;
import org.javacord.api.listener.server.ServerChangeVerificationLevelListener;
import org.javacord.api.listener.server.ServerJoinListener;
import org.javacord.api.listener.server.ServerLeaveListener;
import org.javacord.api.listener.server.VoiceServerUpdateListener;
import org.javacord.api.listener.server.VoiceStateUpdateListener;
import org.javacord.api.listener.server.emoji.KnownCustomEmojiAttachableListenerManager;
import org.javacord.api.listener.server.emoji.KnownCustomEmojiChangeNameListener;
import org.javacord.api.listener.server.emoji.KnownCustomEmojiChangeWhitelistedRolesListener;
import org.javacord.api.listener.server.emoji.KnownCustomEmojiCreateListener;
import org.javacord.api.listener.server.emoji.KnownCustomEmojiDeleteListener;
import org.javacord.api.listener.server.member.ServerMemberBanListener;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import org.javacord.api.listener.server.member.ServerMemberLeaveListener;
import org.javacord.api.listener.server.member.ServerMemberUnbanListener;
import org.javacord.api.listener.server.member.ServerMembersChunkListener;
import org.javacord.api.listener.server.role.RoleAttachableListenerManager;
import org.javacord.api.listener.server.role.RoleChangeColorListener;
import org.javacord.api.listener.server.role.RoleChangeHoistListener;
import org.javacord.api.listener.server.role.RoleChangeMentionableListener;
import org.javacord.api.listener.server.role.RoleChangeNameListener;
import org.javacord.api.listener.server.role.RoleChangePermissionsListener;
import org.javacord.api.listener.server.role.RoleChangePositionListener;
import org.javacord.api.listener.server.role.RoleCreateListener;
import org.javacord.api.listener.server.role.RoleDeleteListener;
import org.javacord.api.listener.server.role.UserRoleAddListener;
import org.javacord.api.listener.server.role.UserRoleRemoveListener;
import org.javacord.api.listener.server.sticker.StickerAttachableListenerManager;
import org.javacord.api.listener.server.sticker.StickerChangeDescriptionListener;
import org.javacord.api.listener.server.sticker.StickerChangeNameListener;
import org.javacord.api.listener.server.sticker.StickerChangeTagsListener;
import org.javacord.api.listener.server.sticker.StickerCreateListener;
import org.javacord.api.listener.server.sticker.StickerDeleteListener;
import org.javacord.api.listener.server.thread.ServerPrivateThreadJoinListener;
import org.javacord.api.listener.server.thread.ServerThreadChannelChangeArchiveTimestampListener;
import org.javacord.api.listener.server.thread.ServerThreadChannelChangeArchivedListener;
import org.javacord.api.listener.server.thread.ServerThreadChannelChangeAutoArchiveDurationListener;
import org.javacord.api.listener.server.thread.ServerThreadChannelChangeInvitableListener;
import org.javacord.api.listener.server.thread.ServerThreadChannelChangeLastMessageIdListener;
import org.javacord.api.listener.server.thread.ServerThreadChannelChangeLockedListener;
import org.javacord.api.listener.server.thread.ServerThreadChannelChangeMemberCountListener;
import org.javacord.api.listener.server.thread.ServerThreadChannelChangeMessageCountListener;
import org.javacord.api.listener.server.thread.ServerThreadChannelChangeRateLimitPerUserListener;
import org.javacord.api.listener.server.thread.ServerThreadChannelChangeTotalMessageSentListener;
import org.javacord.api.listener.user.UserAttachableListenerManager;
import org.javacord.api.listener.user.UserChangeActivityListener;
import org.javacord.api.listener.user.UserChangeAvatarListener;
import org.javacord.api.listener.user.UserChangeDeafenedListener;
import org.javacord.api.listener.user.UserChangeDiscriminatorListener;
import org.javacord.api.listener.user.UserChangeMutedListener;
import org.javacord.api.listener.user.UserChangeNameListener;
import org.javacord.api.listener.user.UserChangeNicknameListener;
import org.javacord.api.listener.user.UserChangePendingListener;
import org.javacord.api.listener.user.UserChangeSelfDeafenedListener;
import org.javacord.api.listener.user.UserChangeSelfMutedListener;
import org.javacord.api.listener.user.UserChangeServerAvatarListener;
import org.javacord.api.listener.user.UserChangeStatusListener;
import org.javacord.api.listener.user.UserChangeTimeoutListener;
import org.javacord.api.listener.user.UserStartTypingListener;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.event.EventDispatcherBase;

public class EventDispatcher
extends EventDispatcherBase {
    public EventDispatcher(DiscordApiImpl api) {
        super(api);
    }

    public void dispatchInteractionCreateEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<TextChannel> textChannels, Collection<User> users, InteractionCreateEvent event) {
        ArrayList<InteractionCreateListener> listeners = new ArrayList<InteractionCreateListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getInteractionCreateListeners).forEach(listeners::addAll);
        }
        if (textChannels != null) {
            textChannels.stream().map(TextChannelAttachableListenerManager::getInteractionCreateListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getInteractionCreateListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getInteractionCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onInteractionCreate(event));
    }

    public void dispatchInteractionCreateEvent(DispatchQueueSelector queueSelector, Server server, TextChannel textChannel, User user, InteractionCreateEvent event) {
        ArrayList<InteractionCreateListener> listeners = new ArrayList<InteractionCreateListener>();
        if (server != null) {
            listeners.addAll(server.getInteractionCreateListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getInteractionCreateListeners());
        }
        if (user != null) {
            listeners.addAll(user.getInteractionCreateListeners());
        }
        listeners.addAll(this.getApi().getInteractionCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onInteractionCreate(event));
    }

    public void dispatchInteractionCreateEvent(DispatchQueueSelector queueSelector, Server server, TextChannel textChannel, long userId, InteractionCreateEvent event) {
        ArrayList<InteractionCreateListener> listeners = new ArrayList<InteractionCreateListener>();
        if (server != null) {
            listeners.addAll(server.getInteractionCreateListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getInteractionCreateListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, InteractionCreateListener.class));
        listeners.addAll(this.getApi().getInteractionCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onInteractionCreate(event));
    }

    public void dispatchSlashCommandCreateEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<TextChannel> textChannels, Collection<User> users, SlashCommandCreateEvent event) {
        ArrayList<SlashCommandCreateListener> listeners = new ArrayList<SlashCommandCreateListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getSlashCommandCreateListeners).forEach(listeners::addAll);
        }
        if (textChannels != null) {
            textChannels.stream().map(TextChannelAttachableListenerManager::getSlashCommandCreateListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getSlashCommandCreateListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getSlashCommandCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onSlashCommandCreate(event));
    }

    public void dispatchSlashCommandCreateEvent(DispatchQueueSelector queueSelector, Server server, TextChannel textChannel, User user, SlashCommandCreateEvent event) {
        ArrayList<SlashCommandCreateListener> listeners = new ArrayList<SlashCommandCreateListener>();
        if (server != null) {
            listeners.addAll(server.getSlashCommandCreateListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getSlashCommandCreateListeners());
        }
        if (user != null) {
            listeners.addAll(user.getSlashCommandCreateListeners());
        }
        listeners.addAll(this.getApi().getSlashCommandCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onSlashCommandCreate(event));
    }

    public void dispatchSlashCommandCreateEvent(DispatchQueueSelector queueSelector, Server server, TextChannel textChannel, long userId, SlashCommandCreateEvent event) {
        ArrayList<SlashCommandCreateListener> listeners = new ArrayList<SlashCommandCreateListener>();
        if (server != null) {
            listeners.addAll(server.getSlashCommandCreateListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getSlashCommandCreateListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, SlashCommandCreateListener.class));
        listeners.addAll(this.getApi().getSlashCommandCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onSlashCommandCreate(event));
    }

    public void dispatchAutocompleteCreateEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<TextChannel> textChannels, Collection<User> users, AutocompleteCreateEvent event) {
        ArrayList<AutocompleteCreateListener> listeners = new ArrayList<AutocompleteCreateListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getAutocompleteCreateListeners).forEach(listeners::addAll);
        }
        if (textChannels != null) {
            textChannels.stream().map(TextChannelAttachableListenerManager::getAutocompleteCreateListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getAutocompleteCreateListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getAutocompleteCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onAutocompleteCreate(event));
    }

    public void dispatchAutocompleteCreateEvent(DispatchQueueSelector queueSelector, Server server, TextChannel textChannel, User user, AutocompleteCreateEvent event) {
        ArrayList<AutocompleteCreateListener> listeners = new ArrayList<AutocompleteCreateListener>();
        if (server != null) {
            listeners.addAll(server.getAutocompleteCreateListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getAutocompleteCreateListeners());
        }
        if (user != null) {
            listeners.addAll(user.getAutocompleteCreateListeners());
        }
        listeners.addAll(this.getApi().getAutocompleteCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onAutocompleteCreate(event));
    }

    public void dispatchAutocompleteCreateEvent(DispatchQueueSelector queueSelector, Server server, TextChannel textChannel, long userId, AutocompleteCreateEvent event) {
        ArrayList<AutocompleteCreateListener> listeners = new ArrayList<AutocompleteCreateListener>();
        if (server != null) {
            listeners.addAll(server.getAutocompleteCreateListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getAutocompleteCreateListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, AutocompleteCreateListener.class));
        listeners.addAll(this.getApi().getAutocompleteCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onAutocompleteCreate(event));
    }

    public void dispatchModalSubmitEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<TextChannel> textChannels, Collection<User> users, ModalSubmitEvent event) {
        ArrayList<ModalSubmitListener> listeners = new ArrayList<ModalSubmitListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getModalSubmitListeners).forEach(listeners::addAll);
        }
        if (textChannels != null) {
            textChannels.stream().map(TextChannelAttachableListenerManager::getModalSubmitListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getModalSubmitListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getModalSubmitListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onModalSubmit(event));
    }

    public void dispatchModalSubmitEvent(DispatchQueueSelector queueSelector, Server server, TextChannel textChannel, User user, ModalSubmitEvent event) {
        ArrayList<ModalSubmitListener> listeners = new ArrayList<ModalSubmitListener>();
        if (server != null) {
            listeners.addAll(server.getModalSubmitListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getModalSubmitListeners());
        }
        if (user != null) {
            listeners.addAll(user.getModalSubmitListeners());
        }
        listeners.addAll(this.getApi().getModalSubmitListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onModalSubmit(event));
    }

    public void dispatchModalSubmitEvent(DispatchQueueSelector queueSelector, Server server, TextChannel textChannel, long userId, ModalSubmitEvent event) {
        ArrayList<ModalSubmitListener> listeners = new ArrayList<ModalSubmitListener>();
        if (server != null) {
            listeners.addAll(server.getModalSubmitListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getModalSubmitListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, ModalSubmitListener.class));
        listeners.addAll(this.getApi().getModalSubmitListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onModalSubmit(event));
    }

    public void dispatchMessageContextMenuCommandEvent(DispatchQueueSelector queueSelector, Collection<Message> messages, Collection<Server> servers, Collection<TextChannel> textChannels, Collection<User> users, MessageContextMenuCommandEvent event) {
        ArrayList<MessageContextMenuCommandListener> listeners = new ArrayList<MessageContextMenuCommandListener>();
        if (messages != null) {
            messages.stream().map(MessageAttachableListenerManager::getMessageContextMenuCommandListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getMessageContextMenuCommandListeners).forEach(listeners::addAll);
        }
        if (textChannels != null) {
            textChannels.stream().map(TextChannelAttachableListenerManager::getMessageContextMenuCommandListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getMessageContextMenuCommandListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getMessageContextMenuCommandListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onMessageContextMenuCommand(event));
    }

    public void dispatchMessageContextMenuCommandEvent(DispatchQueueSelector queueSelector, long messageId, Server server, TextChannel textChannel, User user, MessageContextMenuCommandEvent event) {
        ArrayList<MessageContextMenuCommandListener> listeners = new ArrayList<MessageContextMenuCommandListener>();
        listeners.addAll(MessageAttachableListenerManager.getMessageContextMenuCommandListeners((DiscordApi)this.getApi(), messageId));
        if (server != null) {
            listeners.addAll(server.getMessageContextMenuCommandListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getMessageContextMenuCommandListeners());
        }
        if (user != null) {
            listeners.addAll(user.getMessageContextMenuCommandListeners());
        }
        listeners.addAll(this.getApi().getMessageContextMenuCommandListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onMessageContextMenuCommand(event));
    }

    public void dispatchMessageContextMenuCommandEvent(DispatchQueueSelector queueSelector, long messageId, Server server, TextChannel textChannel, long userId, MessageContextMenuCommandEvent event) {
        ArrayList<MessageContextMenuCommandListener> listeners = new ArrayList<MessageContextMenuCommandListener>();
        listeners.addAll(MessageAttachableListenerManager.getMessageContextMenuCommandListeners((DiscordApi)this.getApi(), messageId));
        if (server != null) {
            listeners.addAll(server.getMessageContextMenuCommandListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getMessageContextMenuCommandListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, MessageContextMenuCommandListener.class));
        listeners.addAll(this.getApi().getMessageContextMenuCommandListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onMessageContextMenuCommand(event));
    }

    public void dispatchMessageComponentCreateEvent(DispatchQueueSelector queueSelector, Collection<Message> messages, Collection<Server> servers, Collection<TextChannel> textChannels, Collection<User> users, MessageComponentCreateEvent event) {
        ArrayList<MessageComponentCreateListener> listeners = new ArrayList<MessageComponentCreateListener>();
        if (messages != null) {
            messages.stream().map(MessageAttachableListenerManager::getMessageComponentCreateListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getMessageComponentCreateListeners).forEach(listeners::addAll);
        }
        if (textChannels != null) {
            textChannels.stream().map(TextChannelAttachableListenerManager::getMessageComponentCreateListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getMessageComponentCreateListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getMessageComponentCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onComponentCreate(event));
    }

    public void dispatchMessageComponentCreateEvent(DispatchQueueSelector queueSelector, long messageId, Server server, TextChannel textChannel, User user, MessageComponentCreateEvent event) {
        ArrayList<MessageComponentCreateListener> listeners = new ArrayList<MessageComponentCreateListener>();
        listeners.addAll(MessageAttachableListenerManager.getMessageComponentCreateListeners((DiscordApi)this.getApi(), messageId));
        if (server != null) {
            listeners.addAll(server.getMessageComponentCreateListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getMessageComponentCreateListeners());
        }
        if (user != null) {
            listeners.addAll(user.getMessageComponentCreateListeners());
        }
        listeners.addAll(this.getApi().getMessageComponentCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onComponentCreate(event));
    }

    public void dispatchMessageComponentCreateEvent(DispatchQueueSelector queueSelector, long messageId, Server server, TextChannel textChannel, long userId, MessageComponentCreateEvent event) {
        ArrayList<MessageComponentCreateListener> listeners = new ArrayList<MessageComponentCreateListener>();
        listeners.addAll(MessageAttachableListenerManager.getMessageComponentCreateListeners((DiscordApi)this.getApi(), messageId));
        if (server != null) {
            listeners.addAll(server.getMessageComponentCreateListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getMessageComponentCreateListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, MessageComponentCreateListener.class));
        listeners.addAll(this.getApi().getMessageComponentCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onComponentCreate(event));
    }

    public void dispatchUserContextMenuCommandEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<TextChannel> textChannels, Collection<User> users, UserContextMenuCommandEvent event) {
        ArrayList<UserContextMenuCommandListener> listeners = new ArrayList<UserContextMenuCommandListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getUserContextMenuCommandListeners).forEach(listeners::addAll);
        }
        if (textChannels != null) {
            textChannels.stream().map(TextChannelAttachableListenerManager::getUserContextMenuCommandListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getUserContextMenuCommandListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getUserContextMenuCommandListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserContextMenuCommand(event));
    }

    public void dispatchUserContextMenuCommandEvent(DispatchQueueSelector queueSelector, Server server, TextChannel textChannel, User user, UserContextMenuCommandEvent event) {
        ArrayList<UserContextMenuCommandListener> listeners = new ArrayList<UserContextMenuCommandListener>();
        if (server != null) {
            listeners.addAll(server.getUserContextMenuCommandListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getUserContextMenuCommandListeners());
        }
        if (user != null) {
            listeners.addAll(user.getUserContextMenuCommandListeners());
        }
        listeners.addAll(this.getApi().getUserContextMenuCommandListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserContextMenuCommand(event));
    }

    public void dispatchUserContextMenuCommandEvent(DispatchQueueSelector queueSelector, Server server, TextChannel textChannel, long userId, UserContextMenuCommandEvent event) {
        ArrayList<UserContextMenuCommandListener> listeners = new ArrayList<UserContextMenuCommandListener>();
        if (server != null) {
            listeners.addAll(server.getUserContextMenuCommandListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getUserContextMenuCommandListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, UserContextMenuCommandListener.class));
        listeners.addAll(this.getApi().getUserContextMenuCommandListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserContextMenuCommand(event));
    }

    public void dispatchSelectMenuChooseEvent(DispatchQueueSelector queueSelector, Collection<Message> messages, Collection<Server> servers, Collection<TextChannel> textChannels, Collection<User> users, SelectMenuChooseEvent event) {
        ArrayList<SelectMenuChooseListener> listeners = new ArrayList<SelectMenuChooseListener>();
        if (messages != null) {
            messages.stream().map(MessageAttachableListenerManager::getSelectMenuChooseListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getSelectMenuChooseListeners).forEach(listeners::addAll);
        }
        if (textChannels != null) {
            textChannels.stream().map(TextChannelAttachableListenerManager::getSelectMenuChooseListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getSelectMenuChooseListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getSelectMenuChooseListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onSelectMenuChoose(event));
    }

    public void dispatchSelectMenuChooseEvent(DispatchQueueSelector queueSelector, long messageId, Server server, TextChannel textChannel, User user, SelectMenuChooseEvent event) {
        ArrayList<SelectMenuChooseListener> listeners = new ArrayList<SelectMenuChooseListener>();
        listeners.addAll(MessageAttachableListenerManager.getSelectMenuChooseListeners((DiscordApi)this.getApi(), messageId));
        if (server != null) {
            listeners.addAll(server.getSelectMenuChooseListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getSelectMenuChooseListeners());
        }
        if (user != null) {
            listeners.addAll(user.getSelectMenuChooseListeners());
        }
        listeners.addAll(this.getApi().getSelectMenuChooseListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onSelectMenuChoose(event));
    }

    public void dispatchSelectMenuChooseEvent(DispatchQueueSelector queueSelector, long messageId, Server server, TextChannel textChannel, long userId, SelectMenuChooseEvent event) {
        ArrayList<SelectMenuChooseListener> listeners = new ArrayList<SelectMenuChooseListener>();
        listeners.addAll(MessageAttachableListenerManager.getSelectMenuChooseListeners((DiscordApi)this.getApi(), messageId));
        if (server != null) {
            listeners.addAll(server.getSelectMenuChooseListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getSelectMenuChooseListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, SelectMenuChooseListener.class));
        listeners.addAll(this.getApi().getSelectMenuChooseListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onSelectMenuChoose(event));
    }

    public void dispatchButtonClickEvent(DispatchQueueSelector queueSelector, Collection<Message> messages, Collection<Server> servers, Collection<TextChannel> textChannels, Collection<User> users, ButtonClickEvent event) {
        ArrayList<ButtonClickListener> listeners = new ArrayList<ButtonClickListener>();
        if (messages != null) {
            messages.stream().map(MessageAttachableListenerManager::getButtonClickListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getButtonClickListeners).forEach(listeners::addAll);
        }
        if (textChannels != null) {
            textChannels.stream().map(TextChannelAttachableListenerManager::getButtonClickListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getButtonClickListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getButtonClickListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onButtonClick(event));
    }

    public void dispatchButtonClickEvent(DispatchQueueSelector queueSelector, long messageId, Server server, TextChannel textChannel, User user, ButtonClickEvent event) {
        ArrayList<ButtonClickListener> listeners = new ArrayList<ButtonClickListener>();
        listeners.addAll(MessageAttachableListenerManager.getButtonClickListeners((DiscordApi)this.getApi(), messageId));
        if (server != null) {
            listeners.addAll(server.getButtonClickListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getButtonClickListeners());
        }
        if (user != null) {
            listeners.addAll(user.getButtonClickListeners());
        }
        listeners.addAll(this.getApi().getButtonClickListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onButtonClick(event));
    }

    public void dispatchButtonClickEvent(DispatchQueueSelector queueSelector, long messageId, Server server, TextChannel textChannel, long userId, ButtonClickEvent event) {
        ArrayList<ButtonClickListener> listeners = new ArrayList<ButtonClickListener>();
        listeners.addAll(MessageAttachableListenerManager.getButtonClickListeners((DiscordApi)this.getApi(), messageId));
        if (server != null) {
            listeners.addAll(server.getButtonClickListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getButtonClickListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, ButtonClickListener.class));
        listeners.addAll(this.getApi().getButtonClickListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onButtonClick(event));
    }

    public void dispatchServerChangeIconEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChangeIconEvent event) {
        ArrayList<ServerChangeIconListener> listeners = new ArrayList<ServerChangeIconListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChangeIconListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChangeIconListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeIcon(event));
    }

    public void dispatchServerChangeIconEvent(DispatchQueueSelector queueSelector, Server server, ServerChangeIconEvent event) {
        ArrayList<ServerChangeIconListener> listeners = new ArrayList<ServerChangeIconListener>();
        if (server != null) {
            listeners.addAll(server.getServerChangeIconListeners());
        }
        listeners.addAll(this.getApi().getServerChangeIconListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeIcon(event));
    }

    public void dispatchServerChangeNameEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChangeNameEvent event) {
        ArrayList<ServerChangeNameListener> listeners = new ArrayList<ServerChangeNameListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChangeNameListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChangeNameListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeName(event));
    }

    public void dispatchServerChangeNameEvent(DispatchQueueSelector queueSelector, Server server, ServerChangeNameEvent event) {
        ArrayList<ServerChangeNameListener> listeners = new ArrayList<ServerChangeNameListener>();
        if (server != null) {
            listeners.addAll(server.getServerChangeNameListeners());
        }
        listeners.addAll(this.getApi().getServerChangeNameListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeName(event));
    }

    public void dispatchServerThreadChannelChangeLastMessageIdEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerThreadChannel> serverThreadChannels, ServerThreadChannelChangeLastMessageIdEvent event) {
        ArrayList<ServerThreadChannelChangeLastMessageIdListener> listeners = new ArrayList<ServerThreadChannelChangeLastMessageIdListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerThreadChannelChangeLastMessageIdListeners).forEach(listeners::addAll);
        }
        if (serverThreadChannels != null) {
            serverThreadChannels.stream().map(ServerThreadChannelAttachableListenerManager::getServerThreadChannelChangeLastMessageIdListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerThreadChannelChangeLastMessageIdListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerThreadChannelChangeLastMessageId(event));
    }

    public void dispatchServerThreadChannelChangeLastMessageIdEvent(DispatchQueueSelector queueSelector, Server server, ServerThreadChannel serverThreadChannel, ServerThreadChannelChangeLastMessageIdEvent event) {
        ArrayList<ServerThreadChannelChangeLastMessageIdListener> listeners = new ArrayList<ServerThreadChannelChangeLastMessageIdListener>();
        if (server != null) {
            listeners.addAll(server.getServerThreadChannelChangeLastMessageIdListeners());
        }
        if (serverThreadChannel != null) {
            listeners.addAll(serverThreadChannel.getServerThreadChannelChangeLastMessageIdListeners());
        }
        listeners.addAll(this.getApi().getServerThreadChannelChangeLastMessageIdListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerThreadChannelChangeLastMessageId(event));
    }

    public void dispatchServerThreadChannelChangeArchivedEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerThreadChannel> serverThreadChannels, ServerThreadChannelChangeArchivedEvent event) {
        ArrayList<ServerThreadChannelChangeArchivedListener> listeners = new ArrayList<ServerThreadChannelChangeArchivedListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerThreadChannelChangeArchivedListeners).forEach(listeners::addAll);
        }
        if (serverThreadChannels != null) {
            serverThreadChannels.stream().map(ServerThreadChannelAttachableListenerManager::getServerThreadChannelChangeArchivedListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerThreadChannelChangeArchivedListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerThreadChannelChangeArchived(event));
    }

    public void dispatchServerThreadChannelChangeArchivedEvent(DispatchQueueSelector queueSelector, Server server, ServerThreadChannel serverThreadChannel, ServerThreadChannelChangeArchivedEvent event) {
        ArrayList<ServerThreadChannelChangeArchivedListener> listeners = new ArrayList<ServerThreadChannelChangeArchivedListener>();
        if (server != null) {
            listeners.addAll(server.getServerThreadChannelChangeArchivedListeners());
        }
        if (serverThreadChannel != null) {
            listeners.addAll(serverThreadChannel.getServerThreadChannelChangeArchivedListeners());
        }
        listeners.addAll(this.getApi().getServerThreadChannelChangeArchivedListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerThreadChannelChangeArchived(event));
    }

    public void dispatchServerThreadChannelChangeMemberCountEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerThreadChannel> serverThreadChannels, ServerThreadChannelChangeMemberCountEvent event) {
        ArrayList<ServerThreadChannelChangeMemberCountListener> listeners = new ArrayList<ServerThreadChannelChangeMemberCountListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerThreadChannelChangeMemberCountListeners).forEach(listeners::addAll);
        }
        if (serverThreadChannels != null) {
            serverThreadChannels.stream().map(ServerThreadChannelAttachableListenerManager::getServerThreadChannelChangeMemberCountListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerThreadChannelChangeMemberCountListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerThreadChannelChangeMemberCount(event));
    }

    public void dispatchServerThreadChannelChangeMemberCountEvent(DispatchQueueSelector queueSelector, Server server, ServerThreadChannel serverThreadChannel, ServerThreadChannelChangeMemberCountEvent event) {
        ArrayList<ServerThreadChannelChangeMemberCountListener> listeners = new ArrayList<ServerThreadChannelChangeMemberCountListener>();
        if (server != null) {
            listeners.addAll(server.getServerThreadChannelChangeMemberCountListeners());
        }
        if (serverThreadChannel != null) {
            listeners.addAll(serverThreadChannel.getServerThreadChannelChangeMemberCountListeners());
        }
        listeners.addAll(this.getApi().getServerThreadChannelChangeMemberCountListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerThreadChannelChangeMemberCount(event));
    }

    public void dispatchServerPrivateThreadJoinEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerThreadChannel> serverThreadChannels, ServerPrivateThreadJoinEvent event) {
        ArrayList<ServerPrivateThreadJoinListener> listeners = new ArrayList<ServerPrivateThreadJoinListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerPrivateThreadJoinListeners).forEach(listeners::addAll);
        }
        if (serverThreadChannels != null) {
            serverThreadChannels.stream().map(ServerThreadChannelAttachableListenerManager::getServerPrivateThreadJoinListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerPrivateThreadJoinListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerPrivateThreadJoin(event));
    }

    public void dispatchServerPrivateThreadJoinEvent(DispatchQueueSelector queueSelector, Server server, ServerThreadChannel serverThreadChannel, ServerPrivateThreadJoinEvent event) {
        ArrayList<ServerPrivateThreadJoinListener> listeners = new ArrayList<ServerPrivateThreadJoinListener>();
        if (server != null) {
            listeners.addAll(server.getServerPrivateThreadJoinListeners());
        }
        if (serverThreadChannel != null) {
            listeners.addAll(serverThreadChannel.getServerPrivateThreadJoinListeners());
        }
        listeners.addAll(this.getApi().getServerPrivateThreadJoinListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerPrivateThreadJoin(event));
    }

    public void dispatchServerThreadChannelChangeInvitableEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerThreadChannel> serverThreadChannels, ServerThreadChannelChangeInvitableEvent event) {
        ArrayList<ServerThreadChannelChangeInvitableListener> listeners = new ArrayList<ServerThreadChannelChangeInvitableListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerThreadChannelChangeInvitableListeners).forEach(listeners::addAll);
        }
        if (serverThreadChannels != null) {
            serverThreadChannels.stream().map(ServerThreadChannelAttachableListenerManager::getServerThreadChannelChangeInvitableListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerThreadChannelChangeInvitableListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerThreadChannelChangeInvitable(event));
    }

    public void dispatchServerThreadChannelChangeInvitableEvent(DispatchQueueSelector queueSelector, Server server, ServerThreadChannel serverThreadChannel, ServerThreadChannelChangeInvitableEvent event) {
        ArrayList<ServerThreadChannelChangeInvitableListener> listeners = new ArrayList<ServerThreadChannelChangeInvitableListener>();
        if (server != null) {
            listeners.addAll(server.getServerThreadChannelChangeInvitableListeners());
        }
        if (serverThreadChannel != null) {
            listeners.addAll(serverThreadChannel.getServerThreadChannelChangeInvitableListeners());
        }
        listeners.addAll(this.getApi().getServerThreadChannelChangeInvitableListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerThreadChannelChangeInvitable(event));
    }

    public void dispatchServerThreadChannelChangeAutoArchiveDurationEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerThreadChannel> serverThreadChannels, ServerThreadChannelChangeAutoArchiveDurationEvent event) {
        ArrayList<ServerThreadChannelChangeAutoArchiveDurationListener> listeners = new ArrayList<ServerThreadChannelChangeAutoArchiveDurationListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerThreadChannelChangeAutoArchiveDurationListeners).forEach(listeners::addAll);
        }
        if (serverThreadChannels != null) {
            serverThreadChannels.stream().map(ServerThreadChannelAttachableListenerManager::getServerThreadChannelChangeAutoArchiveDurationListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerThreadChannelChangeAutoArchiveDurationListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerThreadChannelChangeAutoArchiveDuration(event));
    }

    public void dispatchServerThreadChannelChangeAutoArchiveDurationEvent(DispatchQueueSelector queueSelector, Server server, ServerThreadChannel serverThreadChannel, ServerThreadChannelChangeAutoArchiveDurationEvent event) {
        ArrayList<ServerThreadChannelChangeAutoArchiveDurationListener> listeners = new ArrayList<ServerThreadChannelChangeAutoArchiveDurationListener>();
        if (server != null) {
            listeners.addAll(server.getServerThreadChannelChangeAutoArchiveDurationListeners());
        }
        if (serverThreadChannel != null) {
            listeners.addAll(serverThreadChannel.getServerThreadChannelChangeAutoArchiveDurationListeners());
        }
        listeners.addAll(this.getApi().getServerThreadChannelChangeAutoArchiveDurationListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerThreadChannelChangeAutoArchiveDuration(event));
    }

    public void dispatchServerThreadChannelChangeRateLimitPerUserEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerThreadChannel> serverThreadChannels, ServerThreadChannelChangeRateLimitPerUserEvent event) {
        ArrayList<ServerThreadChannelChangeRateLimitPerUserListener> listeners = new ArrayList<ServerThreadChannelChangeRateLimitPerUserListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerThreadChannelChangeRateLimitPerUserListeners).forEach(listeners::addAll);
        }
        if (serverThreadChannels != null) {
            serverThreadChannels.stream().map(ServerThreadChannelAttachableListenerManager::getServerThreadChannelChangeRateLimitPerUserListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerThreadChannelChangeRateLimitPerUserListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerThreadChannelChangeRateLimitPerUser(event));
    }

    public void dispatchServerThreadChannelChangeRateLimitPerUserEvent(DispatchQueueSelector queueSelector, Server server, ServerThreadChannel serverThreadChannel, ServerThreadChannelChangeRateLimitPerUserEvent event) {
        ArrayList<ServerThreadChannelChangeRateLimitPerUserListener> listeners = new ArrayList<ServerThreadChannelChangeRateLimitPerUserListener>();
        if (server != null) {
            listeners.addAll(server.getServerThreadChannelChangeRateLimitPerUserListeners());
        }
        if (serverThreadChannel != null) {
            listeners.addAll(serverThreadChannel.getServerThreadChannelChangeRateLimitPerUserListeners());
        }
        listeners.addAll(this.getApi().getServerThreadChannelChangeRateLimitPerUserListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerThreadChannelChangeRateLimitPerUser(event));
    }

    public void dispatchServerThreadChannelChangeLockedEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerThreadChannel> serverThreadChannels, ServerThreadChannelChangeLockedEvent event) {
        ArrayList<ServerThreadChannelChangeLockedListener> listeners = new ArrayList<ServerThreadChannelChangeLockedListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerThreadChannelChangeLockedListeners).forEach(listeners::addAll);
        }
        if (serverThreadChannels != null) {
            serverThreadChannels.stream().map(ServerThreadChannelAttachableListenerManager::getServerThreadChannelChangeLockedListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerThreadChannelChangeLockedListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerThreadChannelChangeLocked(event));
    }

    public void dispatchServerThreadChannelChangeLockedEvent(DispatchQueueSelector queueSelector, Server server, ServerThreadChannel serverThreadChannel, ServerThreadChannelChangeLockedEvent event) {
        ArrayList<ServerThreadChannelChangeLockedListener> listeners = new ArrayList<ServerThreadChannelChangeLockedListener>();
        if (server != null) {
            listeners.addAll(server.getServerThreadChannelChangeLockedListeners());
        }
        if (serverThreadChannel != null) {
            listeners.addAll(serverThreadChannel.getServerThreadChannelChangeLockedListeners());
        }
        listeners.addAll(this.getApi().getServerThreadChannelChangeLockedListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerThreadChannelChangeLocked(event));
    }

    public void dispatchServerThreadChannelChangeArchiveTimestampEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerThreadChannel> serverThreadChannels, ServerThreadChannelChangeArchiveTimestampEvent event) {
        ArrayList<ServerThreadChannelChangeArchiveTimestampListener> listeners = new ArrayList<ServerThreadChannelChangeArchiveTimestampListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerThreadChannelChangeArchiveTimestampListeners).forEach(listeners::addAll);
        }
        if (serverThreadChannels != null) {
            serverThreadChannels.stream().map(ServerThreadChannelAttachableListenerManager::getServerThreadChannelChangeArchiveTimestampListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerThreadChannelChangeArchiveTimestampListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerThreadChannelChangeArchiveTimestamp(event));
    }

    public void dispatchServerThreadChannelChangeArchiveTimestampEvent(DispatchQueueSelector queueSelector, Server server, ServerThreadChannel serverThreadChannel, ServerThreadChannelChangeArchiveTimestampEvent event) {
        ArrayList<ServerThreadChannelChangeArchiveTimestampListener> listeners = new ArrayList<ServerThreadChannelChangeArchiveTimestampListener>();
        if (server != null) {
            listeners.addAll(server.getServerThreadChannelChangeArchiveTimestampListeners());
        }
        if (serverThreadChannel != null) {
            listeners.addAll(serverThreadChannel.getServerThreadChannelChangeArchiveTimestampListeners());
        }
        listeners.addAll(this.getApi().getServerThreadChannelChangeArchiveTimestampListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerThreadChannelChangeArchiveTimestamp(event));
    }

    public void dispatchServerThreadChannelChangeTotalMessageSentEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerThreadChannel> serverThreadChannels, ServerThreadChannelChangeTotalMessageSentEvent event) {
        ArrayList<ServerThreadChannelChangeTotalMessageSentListener> listeners = new ArrayList<ServerThreadChannelChangeTotalMessageSentListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerThreadChannelChangeTotalMessageSentListeners).forEach(listeners::addAll);
        }
        if (serverThreadChannels != null) {
            serverThreadChannels.stream().map(ServerThreadChannelAttachableListenerManager::getServerThreadChannelChangeTotalMessageSentListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerThreadChannelChangeTotalMessageSentListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerThreadChannelChangeTotalMessageSent(event));
    }

    public void dispatchServerThreadChannelChangeTotalMessageSentEvent(DispatchQueueSelector queueSelector, Server server, ServerThreadChannel serverThreadChannel, ServerThreadChannelChangeTotalMessageSentEvent event) {
        ArrayList<ServerThreadChannelChangeTotalMessageSentListener> listeners = new ArrayList<ServerThreadChannelChangeTotalMessageSentListener>();
        if (server != null) {
            listeners.addAll(server.getServerThreadChannelChangeTotalMessageSentListeners());
        }
        if (serverThreadChannel != null) {
            listeners.addAll(serverThreadChannel.getServerThreadChannelChangeTotalMessageSentListeners());
        }
        listeners.addAll(this.getApi().getServerThreadChannelChangeTotalMessageSentListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerThreadChannelChangeTotalMessageSent(event));
    }

    public void dispatchServerThreadChannelChangeMessageCountEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerThreadChannel> serverThreadChannels, ServerThreadChannelChangeMessageCountEvent event) {
        ArrayList<ServerThreadChannelChangeMessageCountListener> listeners = new ArrayList<ServerThreadChannelChangeMessageCountListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerThreadChannelChangeMessageCountListeners).forEach(listeners::addAll);
        }
        if (serverThreadChannels != null) {
            serverThreadChannels.stream().map(ServerThreadChannelAttachableListenerManager::getServerThreadChannelChangeMessageCountListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerThreadChannelChangeMessageCountListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerThreadChannelChangeMessageCount(event));
    }

    public void dispatchServerThreadChannelChangeMessageCountEvent(DispatchQueueSelector queueSelector, Server server, ServerThreadChannel serverThreadChannel, ServerThreadChannelChangeMessageCountEvent event) {
        ArrayList<ServerThreadChannelChangeMessageCountListener> listeners = new ArrayList<ServerThreadChannelChangeMessageCountListener>();
        if (server != null) {
            listeners.addAll(server.getServerThreadChannelChangeMessageCountListeners());
        }
        if (serverThreadChannel != null) {
            listeners.addAll(serverThreadChannel.getServerThreadChannelChangeMessageCountListeners());
        }
        listeners.addAll(this.getApi().getServerThreadChannelChangeMessageCountListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerThreadChannelChangeMessageCount(event));
    }

    public void dispatchServerChangeAfkTimeoutEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChangeAfkTimeoutEvent event) {
        ArrayList<ServerChangeAfkTimeoutListener> listeners = new ArrayList<ServerChangeAfkTimeoutListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChangeAfkTimeoutListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChangeAfkTimeoutListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeAfkTimeout(event));
    }

    public void dispatchServerChangeAfkTimeoutEvent(DispatchQueueSelector queueSelector, Server server, ServerChangeAfkTimeoutEvent event) {
        ArrayList<ServerChangeAfkTimeoutListener> listeners = new ArrayList<ServerChangeAfkTimeoutListener>();
        if (server != null) {
            listeners.addAll(server.getServerChangeAfkTimeoutListeners());
        }
        listeners.addAll(this.getApi().getServerChangeAfkTimeoutListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeAfkTimeout(event));
    }

    public void dispatchStickerChangeTagsEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<Sticker> stickers, StickerChangeTagsEvent event) {
        ArrayList<StickerChangeTagsListener> listeners = new ArrayList<StickerChangeTagsListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getStickerChangeTagsListeners).forEach(listeners::addAll);
        }
        if (stickers != null) {
            stickers.stream().map(StickerAttachableListenerManager::getStickerChangeTagsListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getStickerChangeTagsListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onStickerChangeTags(event));
    }

    public void dispatchStickerChangeTagsEvent(DispatchQueueSelector queueSelector, Server server, Sticker sticker, StickerChangeTagsEvent event) {
        ArrayList<StickerChangeTagsListener> listeners = new ArrayList<StickerChangeTagsListener>();
        if (server != null) {
            listeners.addAll(server.getStickerChangeTagsListeners());
        }
        if (sticker != null) {
            listeners.addAll(sticker.getStickerChangeTagsListeners());
        }
        listeners.addAll(this.getApi().getStickerChangeTagsListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onStickerChangeTags(event));
    }

    public void dispatchStickerChangeDescriptionEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<Sticker> stickers, StickerChangeDescriptionEvent event) {
        ArrayList<StickerChangeDescriptionListener> listeners = new ArrayList<StickerChangeDescriptionListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getStickerChangeDescriptionListeners).forEach(listeners::addAll);
        }
        if (stickers != null) {
            stickers.stream().map(StickerAttachableListenerManager::getStickerChangeDescriptionListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getStickerChangeDescriptionListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onStickerChangeDescription(event));
    }

    public void dispatchStickerChangeDescriptionEvent(DispatchQueueSelector queueSelector, Server server, Sticker sticker, StickerChangeDescriptionEvent event) {
        ArrayList<StickerChangeDescriptionListener> listeners = new ArrayList<StickerChangeDescriptionListener>();
        if (server != null) {
            listeners.addAll(server.getStickerChangeDescriptionListeners());
        }
        if (sticker != null) {
            listeners.addAll(sticker.getStickerChangeDescriptionListeners());
        }
        listeners.addAll(this.getApi().getStickerChangeDescriptionListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onStickerChangeDescription(event));
    }

    public void dispatchStickerCreateEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, StickerCreateEvent event) {
        ArrayList<StickerCreateListener> listeners = new ArrayList<StickerCreateListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getStickerCreateListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getStickerCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onStickerCreate(event));
    }

    public void dispatchStickerCreateEvent(DispatchQueueSelector queueSelector, Server server, StickerCreateEvent event) {
        ArrayList<StickerCreateListener> listeners = new ArrayList<StickerCreateListener>();
        if (server != null) {
            listeners.addAll(server.getStickerCreateListeners());
        }
        listeners.addAll(this.getApi().getStickerCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onStickerCreate(event));
    }

    public void dispatchStickerChangeNameEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<Sticker> stickers, StickerChangeNameEvent event) {
        ArrayList<StickerChangeNameListener> listeners = new ArrayList<StickerChangeNameListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getStickerChangeNameListeners).forEach(listeners::addAll);
        }
        if (stickers != null) {
            stickers.stream().map(StickerAttachableListenerManager::getStickerChangeNameListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getStickerChangeNameListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onStickerChangeName(event));
    }

    public void dispatchStickerChangeNameEvent(DispatchQueueSelector queueSelector, Server server, Sticker sticker, StickerChangeNameEvent event) {
        ArrayList<StickerChangeNameListener> listeners = new ArrayList<StickerChangeNameListener>();
        if (server != null) {
            listeners.addAll(server.getStickerChangeNameListeners());
        }
        if (sticker != null) {
            listeners.addAll(sticker.getStickerChangeNameListeners());
        }
        listeners.addAll(this.getApi().getStickerChangeNameListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onStickerChangeName(event));
    }

    public void dispatchStickerDeleteEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<Sticker> stickers, StickerDeleteEvent event) {
        ArrayList<StickerDeleteListener> listeners = new ArrayList<StickerDeleteListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getStickerDeleteListeners).forEach(listeners::addAll);
        }
        if (stickers != null) {
            stickers.stream().map(StickerAttachableListenerManager::getStickerDeleteListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getStickerDeleteListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onStickerDelete(event));
    }

    public void dispatchStickerDeleteEvent(DispatchQueueSelector queueSelector, Server server, Sticker sticker, StickerDeleteEvent event) {
        ArrayList<StickerDeleteListener> listeners = new ArrayList<StickerDeleteListener>();
        if (server != null) {
            listeners.addAll(server.getStickerDeleteListeners());
        }
        if (sticker != null) {
            listeners.addAll(sticker.getStickerDeleteListeners());
        }
        listeners.addAll(this.getApi().getStickerDeleteListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onStickerDelete(event));
    }

    public void dispatchServerChangeSplashEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChangeSplashEvent event) {
        ArrayList<ServerChangeSplashListener> listeners = new ArrayList<ServerChangeSplashListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChangeSplashListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChangeSplashListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeSplash(event));
    }

    public void dispatchServerChangeSplashEvent(DispatchQueueSelector queueSelector, Server server, ServerChangeSplashEvent event) {
        ArrayList<ServerChangeSplashListener> listeners = new ArrayList<ServerChangeSplashListener>();
        if (server != null) {
            listeners.addAll(server.getServerChangeSplashListeners());
        }
        listeners.addAll(this.getApi().getServerChangeSplashListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeSplash(event));
    }

    public void dispatchServerChangeAfkChannelEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChangeAfkChannelEvent event) {
        ArrayList<ServerChangeAfkChannelListener> listeners = new ArrayList<ServerChangeAfkChannelListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChangeAfkChannelListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChangeAfkChannelListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeAfkChannel(event));
    }

    public void dispatchServerChangeAfkChannelEvent(DispatchQueueSelector queueSelector, Server server, ServerChangeAfkChannelEvent event) {
        ArrayList<ServerChangeAfkChannelListener> listeners = new ArrayList<ServerChangeAfkChannelListener>();
        if (server != null) {
            listeners.addAll(server.getServerChangeAfkChannelListeners());
        }
        listeners.addAll(this.getApi().getServerChangeAfkChannelListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeAfkChannel(event));
    }

    public void dispatchVoiceStateUpdateEvent(DispatchQueueSelector queueSelector, Collection<ServerChannel> serverChannels, VoiceStateUpdateEvent event) {
        ArrayList<VoiceStateUpdateListener> listeners = new ArrayList<VoiceStateUpdateListener>();
        if (serverChannels != null) {
            serverChannels.stream().map(ServerChannelAttachableListenerManager::getVoiceStateUpdateListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getVoiceStateUpdateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onVoiceStateUpdate(event));
    }

    public void dispatchVoiceStateUpdateEvent(DispatchQueueSelector queueSelector, ServerChannel serverChannel, VoiceStateUpdateEvent event) {
        ArrayList<VoiceStateUpdateListener> listeners = new ArrayList<VoiceStateUpdateListener>();
        if (serverChannel != null) {
            listeners.addAll(serverChannel.getVoiceStateUpdateListeners());
        }
        listeners.addAll(this.getApi().getVoiceStateUpdateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onVoiceStateUpdate(event));
    }

    public void dispatchServerChangeVanityUrlCodeEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChangeVanityUrlCodeEvent event) {
        ArrayList<ServerChangeVanityUrlCodeListener> listeners = new ArrayList<ServerChangeVanityUrlCodeListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChangeVanityUrlCodeListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChangeVanityUrlCodeListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeVanityUrlCode(event));
    }

    public void dispatchServerChangeVanityUrlCodeEvent(DispatchQueueSelector queueSelector, Server server, ServerChangeVanityUrlCodeEvent event) {
        ArrayList<ServerChangeVanityUrlCodeListener> listeners = new ArrayList<ServerChangeVanityUrlCodeListener>();
        if (server != null) {
            listeners.addAll(server.getServerChangeVanityUrlCodeListeners());
        }
        listeners.addAll(this.getApi().getServerChangeVanityUrlCodeListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeVanityUrlCode(event));
    }

    public void dispatchServerChangeDiscoverySplashEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChangeDiscoverySplashEvent event) {
        ArrayList<ServerChangeDiscoverySplashListener> listeners = new ArrayList<ServerChangeDiscoverySplashListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChangeDiscoverySplashListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChangeDiscoverySplashListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeDiscoverySplash(event));
    }

    public void dispatchServerChangeDiscoverySplashEvent(DispatchQueueSelector queueSelector, Server server, ServerChangeDiscoverySplashEvent event) {
        ArrayList<ServerChangeDiscoverySplashListener> listeners = new ArrayList<ServerChangeDiscoverySplashListener>();
        if (server != null) {
            listeners.addAll(server.getServerChangeDiscoverySplashListeners());
        }
        listeners.addAll(this.getApi().getServerChangeDiscoverySplashListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeDiscoverySplash(event));
    }

    public void dispatchServerJoinEvent(DispatchQueueSelector queueSelector, ServerJoinEvent event) {
        ArrayList<ServerJoinListener> listeners = new ArrayList<ServerJoinListener>();
        listeners.addAll(this.getApi().getServerJoinListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerJoin(event));
    }

    public void dispatchApplicationCommandPermissionsUpdateEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ApplicationCommandPermissionsUpdateEvent event) {
        ArrayList<ApplicationCommandPermissionsUpdateListener> listeners = new ArrayList<ApplicationCommandPermissionsUpdateListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getApplicationCommandPermissionsUpdateListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getApplicationCommandPermissionsUpdateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onApplicationCommandPermissionsUpdate(event));
    }

    public void dispatchApplicationCommandPermissionsUpdateEvent(DispatchQueueSelector queueSelector, Server server, ApplicationCommandPermissionsUpdateEvent event) {
        ArrayList<ApplicationCommandPermissionsUpdateListener> listeners = new ArrayList<ApplicationCommandPermissionsUpdateListener>();
        if (server != null) {
            listeners.addAll(server.getApplicationCommandPermissionsUpdateListeners());
        }
        listeners.addAll(this.getApi().getApplicationCommandPermissionsUpdateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onApplicationCommandPermissionsUpdate(event));
    }

    public void dispatchServerBecomesUnavailableEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerBecomesUnavailableEvent event) {
        ArrayList<ServerBecomesUnavailableListener> listeners = new ArrayList<ServerBecomesUnavailableListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerBecomesUnavailableListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerBecomesUnavailableListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerBecomesUnavailable(event));
    }

    public void dispatchServerBecomesUnavailableEvent(DispatchQueueSelector queueSelector, Server server, ServerBecomesUnavailableEvent event) {
        ArrayList<ServerBecomesUnavailableListener> listeners = new ArrayList<ServerBecomesUnavailableListener>();
        if (server != null) {
            listeners.addAll(server.getServerBecomesUnavailableListeners());
        }
        listeners.addAll(this.getApi().getServerBecomesUnavailableListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerBecomesUnavailable(event));
    }

    public void dispatchVoiceServerUpdateEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, VoiceServerUpdateEvent event) {
        ArrayList<VoiceServerUpdateListener> listeners = new ArrayList<VoiceServerUpdateListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getVoiceServerUpdateListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getVoiceServerUpdateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onVoiceServerUpdate(event));
    }

    public void dispatchVoiceServerUpdateEvent(DispatchQueueSelector queueSelector, Server server, VoiceServerUpdateEvent event) {
        ArrayList<VoiceServerUpdateListener> listeners = new ArrayList<VoiceServerUpdateListener>();
        if (server != null) {
            listeners.addAll(server.getVoiceServerUpdateListeners());
        }
        listeners.addAll(this.getApi().getVoiceServerUpdateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onVoiceServerUpdate(event));
    }

    public void dispatchServerChangeDescriptionEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChangeDescriptionEvent event) {
        ArrayList<ServerChangeDescriptionListener> listeners = new ArrayList<ServerChangeDescriptionListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChangeDescriptionListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChangeDescriptionListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeDescription(event));
    }

    public void dispatchServerChangeDescriptionEvent(DispatchQueueSelector queueSelector, Server server, ServerChangeDescriptionEvent event) {
        ArrayList<ServerChangeDescriptionListener> listeners = new ArrayList<ServerChangeDescriptionListener>();
        if (server != null) {
            listeners.addAll(server.getServerChangeDescriptionListeners());
        }
        listeners.addAll(this.getApi().getServerChangeDescriptionListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeDescription(event));
    }

    public void dispatchServerChangeVerificationLevelEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChangeVerificationLevelEvent event) {
        ArrayList<ServerChangeVerificationLevelListener> listeners = new ArrayList<ServerChangeVerificationLevelListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChangeVerificationLevelListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChangeVerificationLevelListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeVerificationLevel(event));
    }

    public void dispatchServerChangeVerificationLevelEvent(DispatchQueueSelector queueSelector, Server server, ServerChangeVerificationLevelEvent event) {
        ArrayList<ServerChangeVerificationLevelListener> listeners = new ArrayList<ServerChangeVerificationLevelListener>();
        if (server != null) {
            listeners.addAll(server.getServerChangeVerificationLevelListeners());
        }
        listeners.addAll(this.getApi().getServerChangeVerificationLevelListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeVerificationLevel(event));
    }

    public void dispatchServerLeaveEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerLeaveEvent event) {
        ArrayList<ServerLeaveListener> listeners = new ArrayList<ServerLeaveListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerLeaveListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerLeaveListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerLeave(event));
    }

    public void dispatchServerLeaveEvent(DispatchQueueSelector queueSelector, Server server, ServerLeaveEvent event) {
        ArrayList<ServerLeaveListener> listeners = new ArrayList<ServerLeaveListener>();
        if (server != null) {
            listeners.addAll(server.getServerLeaveListeners());
        }
        listeners.addAll(this.getApi().getServerLeaveListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerLeave(event));
    }

    public void dispatchServerChangeBoostCountEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChangeBoostCountEvent event) {
        ArrayList<ServerChangeBoostCountListener> listeners = new ArrayList<ServerChangeBoostCountListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChangeBoostCountListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChangeBoostCountListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeBoostCount(event));
    }

    public void dispatchServerChangeBoostCountEvent(DispatchQueueSelector queueSelector, Server server, ServerChangeBoostCountEvent event) {
        ArrayList<ServerChangeBoostCountListener> listeners = new ArrayList<ServerChangeBoostCountListener>();
        if (server != null) {
            listeners.addAll(server.getServerChangeBoostCountListeners());
        }
        listeners.addAll(this.getApi().getServerChangeBoostCountListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeBoostCount(event));
    }

    public void dispatchServerBecomesAvailableEvent(DispatchQueueSelector queueSelector, ServerBecomesAvailableEvent event) {
        ArrayList<ServerBecomesAvailableListener> listeners = new ArrayList<ServerBecomesAvailableListener>();
        listeners.addAll(this.getApi().getServerBecomesAvailableListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerBecomesAvailable(event));
    }

    public void dispatchServerChangeDefaultMessageNotificationLevelEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChangeDefaultMessageNotificationLevelEvent event) {
        ArrayList<ServerChangeDefaultMessageNotificationLevelListener> listeners = new ArrayList<ServerChangeDefaultMessageNotificationLevelListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChangeDefaultMessageNotificationLevelListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChangeDefaultMessageNotificationLevelListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeDefaultMessageNotificationLevel(event));
    }

    public void dispatchServerChangeDefaultMessageNotificationLevelEvent(DispatchQueueSelector queueSelector, Server server, ServerChangeDefaultMessageNotificationLevelEvent event) {
        ArrayList<ServerChangeDefaultMessageNotificationLevelListener> listeners = new ArrayList<ServerChangeDefaultMessageNotificationLevelListener>();
        if (server != null) {
            listeners.addAll(server.getServerChangeDefaultMessageNotificationLevelListeners());
        }
        listeners.addAll(this.getApi().getServerChangeDefaultMessageNotificationLevelListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeDefaultMessageNotificationLevel(event));
    }

    public void dispatchServerChangeRegionEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChangeRegionEvent event) {
        ArrayList<ServerChangeRegionListener> listeners = new ArrayList<ServerChangeRegionListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChangeRegionListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChangeRegionListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeRegion(event));
    }

    public void dispatchServerChangeRegionEvent(DispatchQueueSelector queueSelector, Server server, ServerChangeRegionEvent event) {
        ArrayList<ServerChangeRegionListener> listeners = new ArrayList<ServerChangeRegionListener>();
        if (server != null) {
            listeners.addAll(server.getServerChangeRegionListeners());
        }
        listeners.addAll(this.getApi().getServerChangeRegionListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeRegion(event));
    }

    public void dispatchServerMemberJoinEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<User> users, ServerMemberJoinEvent event) {
        ArrayList<ServerMemberJoinListener> listeners = new ArrayList<ServerMemberJoinListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerMemberJoinListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getServerMemberJoinListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerMemberJoinListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerMemberJoin(event));
    }

    public void dispatchServerMemberJoinEvent(DispatchQueueSelector queueSelector, Server server, User user, ServerMemberJoinEvent event) {
        ArrayList<ServerMemberJoinListener> listeners = new ArrayList<ServerMemberJoinListener>();
        if (server != null) {
            listeners.addAll(server.getServerMemberJoinListeners());
        }
        if (user != null) {
            listeners.addAll(user.getServerMemberJoinListeners());
        }
        listeners.addAll(this.getApi().getServerMemberJoinListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerMemberJoin(event));
    }

    public void dispatchServerMemberJoinEvent(DispatchQueueSelector queueSelector, Server server, long userId, ServerMemberJoinEvent event) {
        ArrayList<ServerMemberJoinListener> listeners = new ArrayList<ServerMemberJoinListener>();
        if (server != null) {
            listeners.addAll(server.getServerMemberJoinListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, ServerMemberJoinListener.class));
        listeners.addAll(this.getApi().getServerMemberJoinListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerMemberJoin(event));
    }

    public void dispatchServerMemberLeaveEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<User> users, ServerMemberLeaveEvent event) {
        ArrayList<ServerMemberLeaveListener> listeners = new ArrayList<ServerMemberLeaveListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerMemberLeaveListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getServerMemberLeaveListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerMemberLeaveListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerMemberLeave(event));
    }

    public void dispatchServerMemberLeaveEvent(DispatchQueueSelector queueSelector, Server server, User user, ServerMemberLeaveEvent event) {
        ArrayList<ServerMemberLeaveListener> listeners = new ArrayList<ServerMemberLeaveListener>();
        if (server != null) {
            listeners.addAll(server.getServerMemberLeaveListeners());
        }
        if (user != null) {
            listeners.addAll(user.getServerMemberLeaveListeners());
        }
        listeners.addAll(this.getApi().getServerMemberLeaveListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerMemberLeave(event));
    }

    public void dispatchServerMemberLeaveEvent(DispatchQueueSelector queueSelector, Server server, long userId, ServerMemberLeaveEvent event) {
        ArrayList<ServerMemberLeaveListener> listeners = new ArrayList<ServerMemberLeaveListener>();
        if (server != null) {
            listeners.addAll(server.getServerMemberLeaveListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, ServerMemberLeaveListener.class));
        listeners.addAll(this.getApi().getServerMemberLeaveListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerMemberLeave(event));
    }

    public void dispatchServerMemberBanEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<User> users, ServerMemberBanEvent event) {
        ArrayList<ServerMemberBanListener> listeners = new ArrayList<ServerMemberBanListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerMemberBanListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getServerMemberBanListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerMemberBanListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerMemberBan(event));
    }

    public void dispatchServerMemberBanEvent(DispatchQueueSelector queueSelector, Server server, User user, ServerMemberBanEvent event) {
        ArrayList<ServerMemberBanListener> listeners = new ArrayList<ServerMemberBanListener>();
        if (server != null) {
            listeners.addAll(server.getServerMemberBanListeners());
        }
        if (user != null) {
            listeners.addAll(user.getServerMemberBanListeners());
        }
        listeners.addAll(this.getApi().getServerMemberBanListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerMemberBan(event));
    }

    public void dispatchServerMemberBanEvent(DispatchQueueSelector queueSelector, Server server, long userId, ServerMemberBanEvent event) {
        ArrayList<ServerMemberBanListener> listeners = new ArrayList<ServerMemberBanListener>();
        if (server != null) {
            listeners.addAll(server.getServerMemberBanListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, ServerMemberBanListener.class));
        listeners.addAll(this.getApi().getServerMemberBanListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerMemberBan(event));
    }

    public void dispatchServerMembersChunkEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerMembersChunkEvent event) {
        ArrayList<ServerMembersChunkListener> listeners = new ArrayList<ServerMembersChunkListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerMembersChunkListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerMembersChunkListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerMembersChunk(event));
    }

    public void dispatchServerMembersChunkEvent(DispatchQueueSelector queueSelector, Server server, ServerMembersChunkEvent event) {
        ArrayList<ServerMembersChunkListener> listeners = new ArrayList<ServerMembersChunkListener>();
        if (server != null) {
            listeners.addAll(server.getServerMembersChunkListeners());
        }
        listeners.addAll(this.getApi().getServerMembersChunkListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerMembersChunk(event));
    }

    public void dispatchServerMemberUnbanEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<User> users, ServerMemberUnbanEvent event) {
        ArrayList<ServerMemberUnbanListener> listeners = new ArrayList<ServerMemberUnbanListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerMemberUnbanListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getServerMemberUnbanListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerMemberUnbanListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerMemberUnban(event));
    }

    public void dispatchServerMemberUnbanEvent(DispatchQueueSelector queueSelector, Server server, User user, ServerMemberUnbanEvent event) {
        ArrayList<ServerMemberUnbanListener> listeners = new ArrayList<ServerMemberUnbanListener>();
        if (server != null) {
            listeners.addAll(server.getServerMemberUnbanListeners());
        }
        if (user != null) {
            listeners.addAll(user.getServerMemberUnbanListeners());
        }
        listeners.addAll(this.getApi().getServerMemberUnbanListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerMemberUnban(event));
    }

    public void dispatchServerMemberUnbanEvent(DispatchQueueSelector queueSelector, Server server, long userId, ServerMemberUnbanEvent event) {
        ArrayList<ServerMemberUnbanListener> listeners = new ArrayList<ServerMemberUnbanListener>();
        if (server != null) {
            listeners.addAll(server.getServerMemberUnbanListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, ServerMemberUnbanListener.class));
        listeners.addAll(this.getApi().getServerMemberUnbanListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerMemberUnban(event));
    }

    public void dispatchKnownCustomEmojiChangeNameEvent(DispatchQueueSelector queueSelector, Collection<KnownCustomEmoji> knownCustomEmojis, Collection<Server> servers, KnownCustomEmojiChangeNameEvent event) {
        ArrayList<KnownCustomEmojiChangeNameListener> listeners = new ArrayList<KnownCustomEmojiChangeNameListener>();
        if (knownCustomEmojis != null) {
            knownCustomEmojis.stream().map(KnownCustomEmojiAttachableListenerManager::getKnownCustomEmojiChangeNameListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getKnownCustomEmojiChangeNameListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getKnownCustomEmojiChangeNameListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onKnownCustomEmojiChangeName(event));
    }

    public void dispatchKnownCustomEmojiChangeNameEvent(DispatchQueueSelector queueSelector, KnownCustomEmoji knownCustomEmoji, Server server, KnownCustomEmojiChangeNameEvent event) {
        ArrayList<KnownCustomEmojiChangeNameListener> listeners = new ArrayList<KnownCustomEmojiChangeNameListener>();
        if (knownCustomEmoji != null) {
            listeners.addAll(knownCustomEmoji.getKnownCustomEmojiChangeNameListeners());
        }
        if (server != null) {
            listeners.addAll(server.getKnownCustomEmojiChangeNameListeners());
        }
        listeners.addAll(this.getApi().getKnownCustomEmojiChangeNameListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onKnownCustomEmojiChangeName(event));
    }

    public void dispatchKnownCustomEmojiDeleteEvent(DispatchQueueSelector queueSelector, Collection<KnownCustomEmoji> knownCustomEmojis, Collection<Server> servers, KnownCustomEmojiDeleteEvent event) {
        ArrayList<KnownCustomEmojiDeleteListener> listeners = new ArrayList<KnownCustomEmojiDeleteListener>();
        if (knownCustomEmojis != null) {
            knownCustomEmojis.stream().map(KnownCustomEmojiAttachableListenerManager::getKnownCustomEmojiDeleteListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getKnownCustomEmojiDeleteListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getKnownCustomEmojiDeleteListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onKnownCustomEmojiDelete(event));
    }

    public void dispatchKnownCustomEmojiDeleteEvent(DispatchQueueSelector queueSelector, KnownCustomEmoji knownCustomEmoji, Server server, KnownCustomEmojiDeleteEvent event) {
        ArrayList<KnownCustomEmojiDeleteListener> listeners = new ArrayList<KnownCustomEmojiDeleteListener>();
        if (knownCustomEmoji != null) {
            listeners.addAll(knownCustomEmoji.getKnownCustomEmojiDeleteListeners());
        }
        if (server != null) {
            listeners.addAll(server.getKnownCustomEmojiDeleteListeners());
        }
        listeners.addAll(this.getApi().getKnownCustomEmojiDeleteListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onKnownCustomEmojiDelete(event));
    }

    public void dispatchKnownCustomEmojiChangeWhitelistedRolesEvent(DispatchQueueSelector queueSelector, Collection<KnownCustomEmoji> knownCustomEmojis, Collection<Server> servers, KnownCustomEmojiChangeWhitelistedRolesEvent event) {
        ArrayList<KnownCustomEmojiChangeWhitelistedRolesListener> listeners = new ArrayList<KnownCustomEmojiChangeWhitelistedRolesListener>();
        if (knownCustomEmojis != null) {
            knownCustomEmojis.stream().map(KnownCustomEmojiAttachableListenerManager::getKnownCustomEmojiChangeWhitelistedRolesListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getKnownCustomEmojiChangeWhitelistedRolesListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getKnownCustomEmojiChangeWhitelistedRolesListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onKnownCustomEmojiChangeWhitelistedRoles(event));
    }

    public void dispatchKnownCustomEmojiChangeWhitelistedRolesEvent(DispatchQueueSelector queueSelector, KnownCustomEmoji knownCustomEmoji, Server server, KnownCustomEmojiChangeWhitelistedRolesEvent event) {
        ArrayList<KnownCustomEmojiChangeWhitelistedRolesListener> listeners = new ArrayList<KnownCustomEmojiChangeWhitelistedRolesListener>();
        if (knownCustomEmoji != null) {
            listeners.addAll(knownCustomEmoji.getKnownCustomEmojiChangeWhitelistedRolesListeners());
        }
        if (server != null) {
            listeners.addAll(server.getKnownCustomEmojiChangeWhitelistedRolesListeners());
        }
        listeners.addAll(this.getApi().getKnownCustomEmojiChangeWhitelistedRolesListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onKnownCustomEmojiChangeWhitelistedRoles(event));
    }

    public void dispatchKnownCustomEmojiCreateEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, KnownCustomEmojiCreateEvent event) {
        ArrayList<KnownCustomEmojiCreateListener> listeners = new ArrayList<KnownCustomEmojiCreateListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getKnownCustomEmojiCreateListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getKnownCustomEmojiCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onKnownCustomEmojiCreate(event));
    }

    public void dispatchKnownCustomEmojiCreateEvent(DispatchQueueSelector queueSelector, Server server, KnownCustomEmojiCreateEvent event) {
        ArrayList<KnownCustomEmojiCreateListener> listeners = new ArrayList<KnownCustomEmojiCreateListener>();
        if (server != null) {
            listeners.addAll(server.getKnownCustomEmojiCreateListeners());
        }
        listeners.addAll(this.getApi().getKnownCustomEmojiCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onKnownCustomEmojiCreate(event));
    }

    public void dispatchServerChangeSystemChannelEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChangeSystemChannelEvent event) {
        ArrayList<ServerChangeSystemChannelListener> listeners = new ArrayList<ServerChangeSystemChannelListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChangeSystemChannelListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChangeSystemChannelListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeSystemChannel(event));
    }

    public void dispatchServerChangeSystemChannelEvent(DispatchQueueSelector queueSelector, Server server, ServerChangeSystemChannelEvent event) {
        ArrayList<ServerChangeSystemChannelListener> listeners = new ArrayList<ServerChangeSystemChannelListener>();
        if (server != null) {
            listeners.addAll(server.getServerChangeSystemChannelListeners());
        }
        listeners.addAll(this.getApi().getServerChangeSystemChannelListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeSystemChannel(event));
    }

    public void dispatchServerChangePreferredLocaleEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChangePreferredLocaleEvent event) {
        ArrayList<ServerChangePreferredLocaleListener> listeners = new ArrayList<ServerChangePreferredLocaleListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChangePreferredLocaleListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChangePreferredLocaleListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangePreferredLocale(event));
    }

    public void dispatchServerChangePreferredLocaleEvent(DispatchQueueSelector queueSelector, Server server, ServerChangePreferredLocaleEvent event) {
        ArrayList<ServerChangePreferredLocaleListener> listeners = new ArrayList<ServerChangePreferredLocaleListener>();
        if (server != null) {
            listeners.addAll(server.getServerChangePreferredLocaleListeners());
        }
        listeners.addAll(this.getApi().getServerChangePreferredLocaleListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangePreferredLocale(event));
    }

    public void dispatchServerChangeBoostLevelEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChangeBoostLevelEvent event) {
        ArrayList<ServerChangeBoostLevelListener> listeners = new ArrayList<ServerChangeBoostLevelListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChangeBoostLevelListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChangeBoostLevelListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeBoostLevel(event));
    }

    public void dispatchServerChangeBoostLevelEvent(DispatchQueueSelector queueSelector, Server server, ServerChangeBoostLevelEvent event) {
        ArrayList<ServerChangeBoostLevelListener> listeners = new ArrayList<ServerChangeBoostLevelListener>();
        if (server != null) {
            listeners.addAll(server.getServerChangeBoostLevelListeners());
        }
        listeners.addAll(this.getApi().getServerChangeBoostLevelListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeBoostLevel(event));
    }

    public void dispatchServerChangeRulesChannelEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChangeRulesChannelEvent event) {
        ArrayList<ServerChangeRulesChannelListener> listeners = new ArrayList<ServerChangeRulesChannelListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChangeRulesChannelListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChangeRulesChannelListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeRulesChannel(event));
    }

    public void dispatchServerChangeRulesChannelEvent(DispatchQueueSelector queueSelector, Server server, ServerChangeRulesChannelEvent event) {
        ArrayList<ServerChangeRulesChannelListener> listeners = new ArrayList<ServerChangeRulesChannelListener>();
        if (server != null) {
            listeners.addAll(server.getServerChangeRulesChannelListeners());
        }
        listeners.addAll(this.getApi().getServerChangeRulesChannelListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeRulesChannel(event));
    }

    public void dispatchServerChangeServerFeaturesEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChangeServerFeaturesEvent event) {
        ArrayList<ServerChangeServerFeatureListener> listeners = new ArrayList<ServerChangeServerFeatureListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChangeServerFeatureListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChangeServerFeatureListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeServerFeature(event));
    }

    public void dispatchServerChangeServerFeaturesEvent(DispatchQueueSelector queueSelector, Server server, ServerChangeServerFeaturesEvent event) {
        ArrayList<ServerChangeServerFeatureListener> listeners = new ArrayList<ServerChangeServerFeatureListener>();
        if (server != null) {
            listeners.addAll(server.getServerChangeServerFeatureListeners());
        }
        listeners.addAll(this.getApi().getServerChangeServerFeatureListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeServerFeature(event));
    }

    public void dispatchServerChangeOwnerEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChangeOwnerEvent event) {
        ArrayList<ServerChangeOwnerListener> listeners = new ArrayList<ServerChangeOwnerListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChangeOwnerListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChangeOwnerListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeOwner(event));
    }

    public void dispatchServerChangeOwnerEvent(DispatchQueueSelector queueSelector, Server server, ServerChangeOwnerEvent event) {
        ArrayList<ServerChangeOwnerListener> listeners = new ArrayList<ServerChangeOwnerListener>();
        if (server != null) {
            listeners.addAll(server.getServerChangeOwnerListeners());
        }
        listeners.addAll(this.getApi().getServerChangeOwnerListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeOwner(event));
    }

    public void dispatchServerChangeMultiFactorAuthenticationLevelEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChangeMultiFactorAuthenticationLevelEvent event) {
        ArrayList<ServerChangeMultiFactorAuthenticationLevelListener> listeners = new ArrayList<ServerChangeMultiFactorAuthenticationLevelListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChangeMultiFactorAuthenticationLevelListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChangeMultiFactorAuthenticationLevelListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeMultiFactorAuthenticationLevel(event));
    }

    public void dispatchServerChangeMultiFactorAuthenticationLevelEvent(DispatchQueueSelector queueSelector, Server server, ServerChangeMultiFactorAuthenticationLevelEvent event) {
        ArrayList<ServerChangeMultiFactorAuthenticationLevelListener> listeners = new ArrayList<ServerChangeMultiFactorAuthenticationLevelListener>();
        if (server != null) {
            listeners.addAll(server.getServerChangeMultiFactorAuthenticationLevelListeners());
        }
        listeners.addAll(this.getApi().getServerChangeMultiFactorAuthenticationLevelListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeMultiFactorAuthenticationLevel(event));
    }

    public void dispatchServerChangeExplicitContentFilterLevelEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChangeExplicitContentFilterLevelEvent event) {
        ArrayList<ServerChangeExplicitContentFilterLevelListener> listeners = new ArrayList<ServerChangeExplicitContentFilterLevelListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChangeExplicitContentFilterLevelListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChangeExplicitContentFilterLevelListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeExplicitContentFilterLevel(event));
    }

    public void dispatchServerChangeExplicitContentFilterLevelEvent(DispatchQueueSelector queueSelector, Server server, ServerChangeExplicitContentFilterLevelEvent event) {
        ArrayList<ServerChangeExplicitContentFilterLevelListener> listeners = new ArrayList<ServerChangeExplicitContentFilterLevelListener>();
        if (server != null) {
            listeners.addAll(server.getServerChangeExplicitContentFilterLevelListeners());
        }
        listeners.addAll(this.getApi().getServerChangeExplicitContentFilterLevelListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeExplicitContentFilterLevel(event));
    }

    public void dispatchRoleChangePositionEvent(DispatchQueueSelector queueSelector, Collection<Role> roles, Collection<Server> servers, RoleChangePositionEvent event) {
        ArrayList<RoleChangePositionListener> listeners = new ArrayList<RoleChangePositionListener>();
        if (roles != null) {
            roles.stream().map(RoleAttachableListenerManager::getRoleChangePositionListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getRoleChangePositionListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getRoleChangePositionListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onRoleChangePosition(event));
    }

    public void dispatchRoleChangePositionEvent(DispatchQueueSelector queueSelector, Role role, Server server, RoleChangePositionEvent event) {
        ArrayList<RoleChangePositionListener> listeners = new ArrayList<RoleChangePositionListener>();
        if (role != null) {
            listeners.addAll(role.getRoleChangePositionListeners());
        }
        if (server != null) {
            listeners.addAll(server.getRoleChangePositionListeners());
        }
        listeners.addAll(this.getApi().getRoleChangePositionListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onRoleChangePosition(event));
    }

    public void dispatchRoleChangeMentionableEvent(DispatchQueueSelector queueSelector, Collection<Role> roles, Collection<Server> servers, RoleChangeMentionableEvent event) {
        ArrayList<RoleChangeMentionableListener> listeners = new ArrayList<RoleChangeMentionableListener>();
        if (roles != null) {
            roles.stream().map(RoleAttachableListenerManager::getRoleChangeMentionableListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getRoleChangeMentionableListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getRoleChangeMentionableListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onRoleChangeMentionable(event));
    }

    public void dispatchRoleChangeMentionableEvent(DispatchQueueSelector queueSelector, Role role, Server server, RoleChangeMentionableEvent event) {
        ArrayList<RoleChangeMentionableListener> listeners = new ArrayList<RoleChangeMentionableListener>();
        if (role != null) {
            listeners.addAll(role.getRoleChangeMentionableListeners());
        }
        if (server != null) {
            listeners.addAll(server.getRoleChangeMentionableListeners());
        }
        listeners.addAll(this.getApi().getRoleChangeMentionableListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onRoleChangeMentionable(event));
    }

    public void dispatchRoleChangeColorEvent(DispatchQueueSelector queueSelector, Collection<Role> roles, Collection<Server> servers, RoleChangeColorEvent event) {
        ArrayList<RoleChangeColorListener> listeners = new ArrayList<RoleChangeColorListener>();
        if (roles != null) {
            roles.stream().map(RoleAttachableListenerManager::getRoleChangeColorListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getRoleChangeColorListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getRoleChangeColorListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onRoleChangeColor(event));
    }

    public void dispatchRoleChangeColorEvent(DispatchQueueSelector queueSelector, Role role, Server server, RoleChangeColorEvent event) {
        ArrayList<RoleChangeColorListener> listeners = new ArrayList<RoleChangeColorListener>();
        if (role != null) {
            listeners.addAll(role.getRoleChangeColorListeners());
        }
        if (server != null) {
            listeners.addAll(server.getRoleChangeColorListeners());
        }
        listeners.addAll(this.getApi().getRoleChangeColorListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onRoleChangeColor(event));
    }

    public void dispatchRoleChangeNameEvent(DispatchQueueSelector queueSelector, Collection<Role> roles, Collection<Server> servers, RoleChangeNameEvent event) {
        ArrayList<RoleChangeNameListener> listeners = new ArrayList<RoleChangeNameListener>();
        if (roles != null) {
            roles.stream().map(RoleAttachableListenerManager::getRoleChangeNameListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getRoleChangeNameListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getRoleChangeNameListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onRoleChangeName(event));
    }

    public void dispatchRoleChangeNameEvent(DispatchQueueSelector queueSelector, Role role, Server server, RoleChangeNameEvent event) {
        ArrayList<RoleChangeNameListener> listeners = new ArrayList<RoleChangeNameListener>();
        if (role != null) {
            listeners.addAll(role.getRoleChangeNameListeners());
        }
        if (server != null) {
            listeners.addAll(server.getRoleChangeNameListeners());
        }
        listeners.addAll(this.getApi().getRoleChangeNameListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onRoleChangeName(event));
    }

    public void dispatchRoleChangeHoistEvent(DispatchQueueSelector queueSelector, Collection<Role> roles, Collection<Server> servers, RoleChangeHoistEvent event) {
        ArrayList<RoleChangeHoistListener> listeners = new ArrayList<RoleChangeHoistListener>();
        if (roles != null) {
            roles.stream().map(RoleAttachableListenerManager::getRoleChangeHoistListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getRoleChangeHoistListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getRoleChangeHoistListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onRoleChangeHoist(event));
    }

    public void dispatchRoleChangeHoistEvent(DispatchQueueSelector queueSelector, Role role, Server server, RoleChangeHoistEvent event) {
        ArrayList<RoleChangeHoistListener> listeners = new ArrayList<RoleChangeHoistListener>();
        if (role != null) {
            listeners.addAll(role.getRoleChangeHoistListeners());
        }
        if (server != null) {
            listeners.addAll(server.getRoleChangeHoistListeners());
        }
        listeners.addAll(this.getApi().getRoleChangeHoistListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onRoleChangeHoist(event));
    }

    public void dispatchRoleCreateEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, RoleCreateEvent event) {
        ArrayList<RoleCreateListener> listeners = new ArrayList<RoleCreateListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getRoleCreateListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getRoleCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onRoleCreate(event));
    }

    public void dispatchRoleCreateEvent(DispatchQueueSelector queueSelector, Server server, RoleCreateEvent event) {
        ArrayList<RoleCreateListener> listeners = new ArrayList<RoleCreateListener>();
        if (server != null) {
            listeners.addAll(server.getRoleCreateListeners());
        }
        listeners.addAll(this.getApi().getRoleCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onRoleCreate(event));
    }

    public void dispatchRoleChangePermissionsEvent(DispatchQueueSelector queueSelector, Collection<Role> roles, Collection<Server> servers, RoleChangePermissionsEvent event) {
        ArrayList<RoleChangePermissionsListener> listeners = new ArrayList<RoleChangePermissionsListener>();
        if (roles != null) {
            roles.stream().map(RoleAttachableListenerManager::getRoleChangePermissionsListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getRoleChangePermissionsListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getRoleChangePermissionsListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onRoleChangePermissions(event));
    }

    public void dispatchRoleChangePermissionsEvent(DispatchQueueSelector queueSelector, Role role, Server server, RoleChangePermissionsEvent event) {
        ArrayList<RoleChangePermissionsListener> listeners = new ArrayList<RoleChangePermissionsListener>();
        if (role != null) {
            listeners.addAll(role.getRoleChangePermissionsListeners());
        }
        if (server != null) {
            listeners.addAll(server.getRoleChangePermissionsListeners());
        }
        listeners.addAll(this.getApi().getRoleChangePermissionsListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onRoleChangePermissions(event));
    }

    public void dispatchUserRoleRemoveEvent(DispatchQueueSelector queueSelector, Collection<Role> roles, Collection<Server> servers, Collection<User> users, UserRoleRemoveEvent event) {
        ArrayList<UserRoleRemoveListener> listeners = new ArrayList<UserRoleRemoveListener>();
        if (roles != null) {
            roles.stream().map(RoleAttachableListenerManager::getUserRoleRemoveListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getUserRoleRemoveListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getUserRoleRemoveListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getUserRoleRemoveListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserRoleRemove(event));
    }

    public void dispatchUserRoleRemoveEvent(DispatchQueueSelector queueSelector, Role role, Server server, User user, UserRoleRemoveEvent event) {
        ArrayList<UserRoleRemoveListener> listeners = new ArrayList<UserRoleRemoveListener>();
        if (role != null) {
            listeners.addAll(role.getUserRoleRemoveListeners());
        }
        if (server != null) {
            listeners.addAll(server.getUserRoleRemoveListeners());
        }
        if (user != null) {
            listeners.addAll(user.getUserRoleRemoveListeners());
        }
        listeners.addAll(this.getApi().getUserRoleRemoveListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserRoleRemove(event));
    }

    public void dispatchUserRoleRemoveEvent(DispatchQueueSelector queueSelector, Role role, Server server, long userId, UserRoleRemoveEvent event) {
        ArrayList<UserRoleRemoveListener> listeners = new ArrayList<UserRoleRemoveListener>();
        if (role != null) {
            listeners.addAll(role.getUserRoleRemoveListeners());
        }
        if (server != null) {
            listeners.addAll(server.getUserRoleRemoveListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, UserRoleRemoveListener.class));
        listeners.addAll(this.getApi().getUserRoleRemoveListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserRoleRemove(event));
    }

    public void dispatchUserRoleAddEvent(DispatchQueueSelector queueSelector, Collection<Role> roles, Collection<Server> servers, Collection<User> users, UserRoleAddEvent event) {
        ArrayList<UserRoleAddListener> listeners = new ArrayList<UserRoleAddListener>();
        if (roles != null) {
            roles.stream().map(RoleAttachableListenerManager::getUserRoleAddListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getUserRoleAddListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getUserRoleAddListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getUserRoleAddListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserRoleAdd(event));
    }

    public void dispatchUserRoleAddEvent(DispatchQueueSelector queueSelector, Role role, Server server, User user, UserRoleAddEvent event) {
        ArrayList<UserRoleAddListener> listeners = new ArrayList<UserRoleAddListener>();
        if (role != null) {
            listeners.addAll(role.getUserRoleAddListeners());
        }
        if (server != null) {
            listeners.addAll(server.getUserRoleAddListeners());
        }
        if (user != null) {
            listeners.addAll(user.getUserRoleAddListeners());
        }
        listeners.addAll(this.getApi().getUserRoleAddListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserRoleAdd(event));
    }

    public void dispatchUserRoleAddEvent(DispatchQueueSelector queueSelector, Role role, Server server, long userId, UserRoleAddEvent event) {
        ArrayList<UserRoleAddListener> listeners = new ArrayList<UserRoleAddListener>();
        if (role != null) {
            listeners.addAll(role.getUserRoleAddListeners());
        }
        if (server != null) {
            listeners.addAll(server.getUserRoleAddListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, UserRoleAddListener.class));
        listeners.addAll(this.getApi().getUserRoleAddListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserRoleAdd(event));
    }

    public void dispatchRoleDeleteEvent(DispatchQueueSelector queueSelector, Collection<Role> roles, Collection<Server> servers, RoleDeleteEvent event) {
        ArrayList<RoleDeleteListener> listeners = new ArrayList<RoleDeleteListener>();
        if (roles != null) {
            roles.stream().map(RoleAttachableListenerManager::getRoleDeleteListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getRoleDeleteListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getRoleDeleteListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onRoleDelete(event));
    }

    public void dispatchRoleDeleteEvent(DispatchQueueSelector queueSelector, Role role, Server server, RoleDeleteEvent event) {
        ArrayList<RoleDeleteListener> listeners = new ArrayList<RoleDeleteListener>();
        if (role != null) {
            listeners.addAll(role.getRoleDeleteListeners());
        }
        if (server != null) {
            listeners.addAll(server.getRoleDeleteListeners());
        }
        listeners.addAll(this.getApi().getRoleDeleteListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onRoleDelete(event));
    }

    public void dispatchServerChangeModeratorsOnlyChannelEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChangeModeratorsOnlyChannelEvent event) {
        ArrayList<ServerChangeModeratorsOnlyChannelListener> listeners = new ArrayList<ServerChangeModeratorsOnlyChannelListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChangeModeratorsOnlyChannelListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChangeModeratorsOnlyChannelListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeModeratorsOnlyChannel(event));
    }

    public void dispatchServerChangeModeratorsOnlyChannelEvent(DispatchQueueSelector queueSelector, Server server, ServerChangeModeratorsOnlyChannelEvent event) {
        ArrayList<ServerChangeModeratorsOnlyChannelListener> listeners = new ArrayList<ServerChangeModeratorsOnlyChannelListener>();
        if (server != null) {
            listeners.addAll(server.getServerChangeModeratorsOnlyChannelListeners());
        }
        listeners.addAll(this.getApi().getServerChangeModeratorsOnlyChannelListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeModeratorsOnlyChannel(event));
    }

    public void dispatchServerChangeNsfwLevelEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChangeNsfwLevelEvent event) {
        ArrayList<ServerChangeNsfwLevelListener> listeners = new ArrayList<ServerChangeNsfwLevelListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChangeNsfwLevelListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChangeNsfwLevelListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeNsfwLevel(event));
    }

    public void dispatchServerChangeNsfwLevelEvent(DispatchQueueSelector queueSelector, Server server, ServerChangeNsfwLevelEvent event) {
        ArrayList<ServerChangeNsfwLevelListener> listeners = new ArrayList<ServerChangeNsfwLevelListener>();
        if (server != null) {
            listeners.addAll(server.getServerChangeNsfwLevelListeners());
        }
        listeners.addAll(this.getApi().getServerChangeNsfwLevelListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChangeNsfwLevel(event));
    }

    public void dispatchServerChannelChangePositionEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerChannel> serverChannels, ServerChannelChangePositionEvent event) {
        ArrayList<ServerChannelChangePositionListener> listeners = new ArrayList<ServerChannelChangePositionListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChannelChangePositionListeners).forEach(listeners::addAll);
        }
        if (serverChannels != null) {
            serverChannels.stream().map(ServerChannelAttachableListenerManager::getServerChannelChangePositionListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChannelChangePositionListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChannelChangePosition(event));
    }

    public void dispatchServerChannelChangePositionEvent(DispatchQueueSelector queueSelector, Server server, ServerChannel serverChannel, ServerChannelChangePositionEvent event) {
        ArrayList<ServerChannelChangePositionListener> listeners = new ArrayList<ServerChannelChangePositionListener>();
        if (server != null) {
            listeners.addAll(server.getServerChannelChangePositionListeners());
        }
        if (serverChannel != null) {
            listeners.addAll(serverChannel.getServerChannelChangePositionListeners());
        }
        listeners.addAll(this.getApi().getServerChannelChangePositionListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChannelChangePosition(event));
    }

    public void dispatchThreadListSyncEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ThreadListSyncEvent event) {
        ArrayList<ServerThreadListSyncListener> listeners = new ArrayList<ServerThreadListSyncListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerThreadListSyncListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerThreadListSyncListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onThreadListSync(event));
    }

    public void dispatchThreadListSyncEvent(DispatchQueueSelector queueSelector, Server server, ThreadListSyncEvent event) {
        ArrayList<ServerThreadListSyncListener> listeners = new ArrayList<ServerThreadListSyncListener>();
        if (server != null) {
            listeners.addAll(server.getServerThreadListSyncListeners());
        }
        listeners.addAll(this.getApi().getServerThreadListSyncListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onThreadListSync(event));
    }

    public void dispatchThreadUpdateEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerThreadChannel> serverThreadChannels, ThreadUpdateEvent event) {
        ArrayList<ServerThreadChannelUpdateListener> listeners = new ArrayList<ServerThreadChannelUpdateListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerThreadChannelUpdateListeners).forEach(listeners::addAll);
        }
        if (serverThreadChannels != null) {
            serverThreadChannels.stream().map(ServerThreadChannelAttachableListenerManager::getServerThreadChannelUpdateListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerThreadChannelUpdateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onThreadUpdate(event));
    }

    public void dispatchThreadUpdateEvent(DispatchQueueSelector queueSelector, Server server, ServerThreadChannel serverThreadChannel, ThreadUpdateEvent event) {
        ArrayList<ServerThreadChannelUpdateListener> listeners = new ArrayList<ServerThreadChannelUpdateListener>();
        if (server != null) {
            listeners.addAll(server.getServerThreadChannelUpdateListeners());
        }
        if (serverThreadChannel != null) {
            listeners.addAll(serverThreadChannel.getServerThreadChannelUpdateListeners());
        }
        listeners.addAll(this.getApi().getServerThreadChannelUpdateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onThreadUpdate(event));
    }

    public void dispatchThreadMembersUpdateEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerThreadChannel> serverThreadChannels, ThreadMembersUpdateEvent event) {
        ArrayList<ServerThreadChannelMembersUpdateListener> listeners = new ArrayList<ServerThreadChannelMembersUpdateListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerThreadChannelMembersUpdateListeners).forEach(listeners::addAll);
        }
        if (serverThreadChannels != null) {
            serverThreadChannels.stream().map(ServerThreadChannelAttachableListenerManager::getServerThreadChannelMembersUpdateListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerThreadChannelMembersUpdateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onThreadMembersUpdate(event));
    }

    public void dispatchThreadMembersUpdateEvent(DispatchQueueSelector queueSelector, Server server, ServerThreadChannel serverThreadChannel, ThreadMembersUpdateEvent event) {
        ArrayList<ServerThreadChannelMembersUpdateListener> listeners = new ArrayList<ServerThreadChannelMembersUpdateListener>();
        if (server != null) {
            listeners.addAll(server.getServerThreadChannelMembersUpdateListeners());
        }
        if (serverThreadChannel != null) {
            listeners.addAll(serverThreadChannel.getServerThreadChannelMembersUpdateListeners());
        }
        listeners.addAll(this.getApi().getServerThreadChannelMembersUpdateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onThreadMembersUpdate(event));
    }

    public void dispatchThreadCreateEvent(DispatchQueueSelector queueSelector, Collection<ServerThreadChannel> serverThreadChannels, ThreadCreateEvent event) {
        ArrayList<ServerThreadChannelCreateListener> listeners = new ArrayList<ServerThreadChannelCreateListener>();
        if (serverThreadChannels != null) {
            serverThreadChannels.stream().map(ServerThreadChannelAttachableListenerManager::getServerThreadChannelCreateListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerThreadChannelCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onThreadCreate(event));
    }

    public void dispatchThreadCreateEvent(DispatchQueueSelector queueSelector, ServerThreadChannel serverThreadChannel, ThreadCreateEvent event) {
        ArrayList<ServerThreadChannelCreateListener> listeners = new ArrayList<ServerThreadChannelCreateListener>();
        if (serverThreadChannel != null) {
            listeners.addAll(serverThreadChannel.getServerThreadChannelCreateListeners());
        }
        listeners.addAll(this.getApi().getServerThreadChannelCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onThreadCreate(event));
    }

    public void dispatchThreadDeleteEvent(DispatchQueueSelector queueSelector, Collection<ServerThreadChannel> serverThreadChannels, ThreadDeleteEvent event) {
        ArrayList<ServerThreadChannelDeleteListener> listeners = new ArrayList<ServerThreadChannelDeleteListener>();
        if (serverThreadChannels != null) {
            serverThreadChannels.stream().map(ServerThreadChannelAttachableListenerManager::getServerThreadChannelDeleteListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerThreadChannelDeleteListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onThreadDelete(event));
    }

    public void dispatchThreadDeleteEvent(DispatchQueueSelector queueSelector, ServerThreadChannel serverThreadChannel, ThreadDeleteEvent event) {
        ArrayList<ServerThreadChannelDeleteListener> listeners = new ArrayList<ServerThreadChannelDeleteListener>();
        if (serverThreadChannel != null) {
            listeners.addAll(serverThreadChannel.getServerThreadChannelDeleteListeners());
        }
        listeners.addAll(this.getApi().getServerThreadChannelDeleteListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onThreadDelete(event));
    }

    public void dispatchWebhooksUpdateEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerTextChannel> serverTextChannels, WebhooksUpdateEvent event) {
        ArrayList<WebhooksUpdateListener> listeners = new ArrayList<WebhooksUpdateListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getWebhooksUpdateListeners).forEach(listeners::addAll);
        }
        if (serverTextChannels != null) {
            serverTextChannels.stream().map(ServerTextChannelAttachableListenerManager::getWebhooksUpdateListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getWebhooksUpdateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onWebhooksUpdate(event));
    }

    public void dispatchWebhooksUpdateEvent(DispatchQueueSelector queueSelector, Server server, ServerTextChannel serverTextChannel, WebhooksUpdateEvent event) {
        ArrayList<WebhooksUpdateListener> listeners = new ArrayList<WebhooksUpdateListener>();
        if (server != null) {
            listeners.addAll(server.getWebhooksUpdateListeners());
        }
        if (serverTextChannel != null) {
            listeners.addAll(serverTextChannel.getWebhooksUpdateListeners());
        }
        listeners.addAll(this.getApi().getWebhooksUpdateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onWebhooksUpdate(event));
    }

    public void dispatchServerTextChannelChangeDefaultAutoArchiveDurationEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerTextChannel> serverTextChannels, ServerTextChannelChangeDefaultAutoArchiveDurationEvent event) {
        ArrayList<ServerTextChannelChangeDefaultAutoArchiveDurationListener> listeners = new ArrayList<ServerTextChannelChangeDefaultAutoArchiveDurationListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerTextChannelChangeDefaultAutoArchiveDurationListeners).forEach(listeners::addAll);
        }
        if (serverTextChannels != null) {
            serverTextChannels.stream().map(ServerTextChannelAttachableListenerManager::getServerTextChannelChangeDefaultAutoArchiveDurationListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerTextChannelChangeDefaultAutoArchiveDurationListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerTextChannelChangeDefaultAutoArchiveDuration(event));
    }

    public void dispatchServerTextChannelChangeDefaultAutoArchiveDurationEvent(DispatchQueueSelector queueSelector, Server server, ServerTextChannel serverTextChannel, ServerTextChannelChangeDefaultAutoArchiveDurationEvent event) {
        ArrayList<ServerTextChannelChangeDefaultAutoArchiveDurationListener> listeners = new ArrayList<ServerTextChannelChangeDefaultAutoArchiveDurationListener>();
        if (server != null) {
            listeners.addAll(server.getServerTextChannelChangeDefaultAutoArchiveDurationListeners());
        }
        if (serverTextChannel != null) {
            listeners.addAll(serverTextChannel.getServerTextChannelChangeDefaultAutoArchiveDurationListeners());
        }
        listeners.addAll(this.getApi().getServerTextChannelChangeDefaultAutoArchiveDurationListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerTextChannelChangeDefaultAutoArchiveDuration(event));
    }

    public void dispatchServerTextChannelChangeSlowmodeEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerTextChannel> serverTextChannels, ServerTextChannelChangeSlowmodeEvent event) {
        ArrayList<ServerTextChannelChangeSlowmodeListener> listeners = new ArrayList<ServerTextChannelChangeSlowmodeListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerTextChannelChangeSlowmodeListeners).forEach(listeners::addAll);
        }
        if (serverTextChannels != null) {
            serverTextChannels.stream().map(ServerTextChannelAttachableListenerManager::getServerTextChannelChangeSlowmodeListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerTextChannelChangeSlowmodeListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerTextChannelChangeSlowmodeDelay(event));
    }

    public void dispatchServerTextChannelChangeSlowmodeEvent(DispatchQueueSelector queueSelector, Server server, ServerTextChannel serverTextChannel, ServerTextChannelChangeSlowmodeEvent event) {
        ArrayList<ServerTextChannelChangeSlowmodeListener> listeners = new ArrayList<ServerTextChannelChangeSlowmodeListener>();
        if (server != null) {
            listeners.addAll(server.getServerTextChannelChangeSlowmodeListeners());
        }
        if (serverTextChannel != null) {
            listeners.addAll(serverTextChannel.getServerTextChannelChangeSlowmodeListeners());
        }
        listeners.addAll(this.getApi().getServerTextChannelChangeSlowmodeListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerTextChannelChangeSlowmodeDelay(event));
    }

    public void dispatchServerTextChannelChangeTopicEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerTextChannel> serverTextChannels, ServerTextChannelChangeTopicEvent event) {
        ArrayList<ServerTextChannelChangeTopicListener> listeners = new ArrayList<ServerTextChannelChangeTopicListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerTextChannelChangeTopicListeners).forEach(listeners::addAll);
        }
        if (serverTextChannels != null) {
            serverTextChannels.stream().map(ServerTextChannelAttachableListenerManager::getServerTextChannelChangeTopicListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerTextChannelChangeTopicListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerTextChannelChangeTopic(event));
    }

    public void dispatchServerTextChannelChangeTopicEvent(DispatchQueueSelector queueSelector, Server server, ServerTextChannel serverTextChannel, ServerTextChannelChangeTopicEvent event) {
        ArrayList<ServerTextChannelChangeTopicListener> listeners = new ArrayList<ServerTextChannelChangeTopicListener>();
        if (server != null) {
            listeners.addAll(server.getServerTextChannelChangeTopicListeners());
        }
        if (serverTextChannel != null) {
            listeners.addAll(serverTextChannel.getServerTextChannelChangeTopicListeners());
        }
        listeners.addAll(this.getApi().getServerTextChannelChangeTopicListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerTextChannelChangeTopic(event));
    }

    public void dispatchServerChannelChangeOverwrittenPermissionsEvent(DispatchQueueSelector queueSelector, Collection<Role> roles, Collection<Server> servers, Collection<ServerChannel> serverChannels, Collection<User> users, ServerChannelChangeOverwrittenPermissionsEvent event) {
        ArrayList<ServerChannelChangeOverwrittenPermissionsListener> listeners = new ArrayList<ServerChannelChangeOverwrittenPermissionsListener>();
        if (roles != null) {
            roles.stream().map(RoleAttachableListenerManager::getServerChannelChangeOverwrittenPermissionsListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChannelChangeOverwrittenPermissionsListeners).forEach(listeners::addAll);
        }
        if (serverChannels != null) {
            serverChannels.stream().map(ServerChannelAttachableListenerManager::getServerChannelChangeOverwrittenPermissionsListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getServerChannelChangeOverwrittenPermissionsListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChannelChangeOverwrittenPermissionsListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChannelChangeOverwrittenPermissions(event));
    }

    public void dispatchServerChannelChangeOverwrittenPermissionsEvent(DispatchQueueSelector queueSelector, Role role, Server server, ServerChannel serverChannel, User user, ServerChannelChangeOverwrittenPermissionsEvent event) {
        ArrayList<ServerChannelChangeOverwrittenPermissionsListener> listeners = new ArrayList<ServerChannelChangeOverwrittenPermissionsListener>();
        if (role != null) {
            listeners.addAll(role.getServerChannelChangeOverwrittenPermissionsListeners());
        }
        if (server != null) {
            listeners.addAll(server.getServerChannelChangeOverwrittenPermissionsListeners());
        }
        if (serverChannel != null) {
            listeners.addAll(serverChannel.getServerChannelChangeOverwrittenPermissionsListeners());
        }
        if (user != null) {
            listeners.addAll(user.getServerChannelChangeOverwrittenPermissionsListeners());
        }
        listeners.addAll(this.getApi().getServerChannelChangeOverwrittenPermissionsListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChannelChangeOverwrittenPermissions(event));
    }

    public void dispatchServerChannelChangeOverwrittenPermissionsEvent(DispatchQueueSelector queueSelector, Role role, Server server, ServerChannel serverChannel, long userId, ServerChannelChangeOverwrittenPermissionsEvent event) {
        ArrayList<ServerChannelChangeOverwrittenPermissionsListener> listeners = new ArrayList<ServerChannelChangeOverwrittenPermissionsListener>();
        if (role != null) {
            listeners.addAll(role.getServerChannelChangeOverwrittenPermissionsListeners());
        }
        if (server != null) {
            listeners.addAll(server.getServerChannelChangeOverwrittenPermissionsListeners());
        }
        if (serverChannel != null) {
            listeners.addAll(serverChannel.getServerChannelChangeOverwrittenPermissionsListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, ServerChannelChangeOverwrittenPermissionsListener.class));
        listeners.addAll(this.getApi().getServerChannelChangeOverwrittenPermissionsListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChannelChangeOverwrittenPermissions(event));
    }

    public void dispatchServerChannelInviteDeleteEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChannelInviteDeleteEvent event) {
        ArrayList<ServerChannelInviteDeleteListener> listeners = new ArrayList<ServerChannelInviteDeleteListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChannelInviteDeleteListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChannelInviteDeleteListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChannelInviteDelete(event));
    }

    public void dispatchServerChannelInviteDeleteEvent(DispatchQueueSelector queueSelector, Server server, ServerChannelInviteDeleteEvent event) {
        ArrayList<ServerChannelInviteDeleteListener> listeners = new ArrayList<ServerChannelInviteDeleteListener>();
        if (server != null) {
            listeners.addAll(server.getServerChannelInviteDeleteListeners());
        }
        listeners.addAll(this.getApi().getServerChannelInviteDeleteListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChannelInviteDelete(event));
    }

    public void dispatchServerChannelInviteCreateEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChannelInviteCreateEvent event) {
        ArrayList<ServerChannelInviteCreateListener> listeners = new ArrayList<ServerChannelInviteCreateListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChannelInviteCreateListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChannelInviteCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChannelInviteCreate(event));
    }

    public void dispatchServerChannelInviteCreateEvent(DispatchQueueSelector queueSelector, Server server, ServerChannelInviteCreateEvent event) {
        ArrayList<ServerChannelInviteCreateListener> listeners = new ArrayList<ServerChannelInviteCreateListener>();
        if (server != null) {
            listeners.addAll(server.getServerChannelInviteCreateListeners());
        }
        listeners.addAll(this.getApi().getServerChannelInviteCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChannelInviteCreate(event));
    }

    public void dispatchServerChannelChangeNsfwFlagEvent(DispatchQueueSelector queueSelector, Collection<ChannelCategory> channelCategorys, Collection<Server> servers, Collection<ServerTextChannel> serverTextChannels, ServerChannelChangeNsfwFlagEvent event) {
        ArrayList<ServerChannelChangeNsfwFlagListener> listeners = new ArrayList<ServerChannelChangeNsfwFlagListener>();
        if (channelCategorys != null) {
            channelCategorys.stream().map(ChannelCategoryAttachableListenerManager::getServerChannelChangeNsfwFlagListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChannelChangeNsfwFlagListeners).forEach(listeners::addAll);
        }
        if (serverTextChannels != null) {
            serverTextChannels.stream().map(ServerTextChannelAttachableListenerManager::getServerChannelChangeNsfwFlagListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChannelChangeNsfwFlagListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChannelChangeNsfwFlag(event));
    }

    public void dispatchServerChannelChangeNsfwFlagEvent(DispatchQueueSelector queueSelector, ChannelCategory channelCategory, Server server, ServerTextChannel serverTextChannel, ServerChannelChangeNsfwFlagEvent event) {
        ArrayList<ServerChannelChangeNsfwFlagListener> listeners = new ArrayList<ServerChannelChangeNsfwFlagListener>();
        if (channelCategory != null) {
            listeners.addAll(channelCategory.getServerChannelChangeNsfwFlagListeners());
        }
        if (server != null) {
            listeners.addAll(server.getServerChannelChangeNsfwFlagListeners());
        }
        if (serverTextChannel != null) {
            listeners.addAll(serverTextChannel.getServerChannelChangeNsfwFlagListeners());
        }
        listeners.addAll(this.getApi().getServerChannelChangeNsfwFlagListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChannelChangeNsfwFlag(event));
    }

    public void dispatchServerChannelDeleteEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerChannel> serverChannels, ServerChannelDeleteEvent event) {
        ArrayList<ServerChannelDeleteListener> listeners = new ArrayList<ServerChannelDeleteListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChannelDeleteListeners).forEach(listeners::addAll);
        }
        if (serverChannels != null) {
            serverChannels.stream().map(ServerChannelAttachableListenerManager::getServerChannelDeleteListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChannelDeleteListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChannelDelete(event));
    }

    public void dispatchServerChannelDeleteEvent(DispatchQueueSelector queueSelector, Server server, ServerChannel serverChannel, ServerChannelDeleteEvent event) {
        ArrayList<ServerChannelDeleteListener> listeners = new ArrayList<ServerChannelDeleteListener>();
        if (server != null) {
            listeners.addAll(server.getServerChannelDeleteListeners());
        }
        if (serverChannel != null) {
            listeners.addAll(serverChannel.getServerChannelDeleteListeners());
        }
        listeners.addAll(this.getApi().getServerChannelDeleteListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChannelDelete(event));
    }

    public void dispatchServerChannelCreateEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, ServerChannelCreateEvent event) {
        ArrayList<ServerChannelCreateListener> listeners = new ArrayList<ServerChannelCreateListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChannelCreateListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChannelCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChannelCreate(event));
    }

    public void dispatchServerChannelCreateEvent(DispatchQueueSelector queueSelector, Server server, ServerChannelCreateEvent event) {
        ArrayList<ServerChannelCreateListener> listeners = new ArrayList<ServerChannelCreateListener>();
        if (server != null) {
            listeners.addAll(server.getServerChannelCreateListeners());
        }
        listeners.addAll(this.getApi().getServerChannelCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChannelCreate(event));
    }

    public void dispatchServerStageVoiceChannelChangeTopicEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerStageVoiceChannel> serverStageVoiceChannels, ServerStageVoiceChannelChangeTopicEvent event) {
        ArrayList<ServerStageVoiceChannelChangeTopicListener> listeners = new ArrayList<ServerStageVoiceChannelChangeTopicListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerStageVoiceChannelChangeTopicListeners).forEach(listeners::addAll);
        }
        if (serverStageVoiceChannels != null) {
            serverStageVoiceChannels.stream().map(ServerStageVoiceChannelAttachableListenerManager::getServerStageVoiceChannelChangeTopicListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerStageVoiceChannelChangeTopicListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerStageVoiceChannelChangeTopic(event));
    }

    public void dispatchServerStageVoiceChannelChangeTopicEvent(DispatchQueueSelector queueSelector, Server server, ServerStageVoiceChannel serverStageVoiceChannel, ServerStageVoiceChannelChangeTopicEvent event) {
        ArrayList<ServerStageVoiceChannelChangeTopicListener> listeners = new ArrayList<ServerStageVoiceChannelChangeTopicListener>();
        if (server != null) {
            listeners.addAll(server.getServerStageVoiceChannelChangeTopicListeners());
        }
        if (serverStageVoiceChannel != null) {
            listeners.addAll(serverStageVoiceChannel.getServerStageVoiceChannelChangeTopicListeners());
        }
        listeners.addAll(this.getApi().getServerStageVoiceChannelChangeTopicListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerStageVoiceChannelChangeTopic(event));
    }

    public void dispatchServerVoiceChannelChangeBitrateEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerVoiceChannel> serverVoiceChannels, ServerVoiceChannelChangeBitrateEvent event) {
        ArrayList<ServerVoiceChannelChangeBitrateListener> listeners = new ArrayList<ServerVoiceChannelChangeBitrateListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerVoiceChannelChangeBitrateListeners).forEach(listeners::addAll);
        }
        if (serverVoiceChannels != null) {
            serverVoiceChannels.stream().map(ServerVoiceChannelAttachableListenerManager::getServerVoiceChannelChangeBitrateListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerVoiceChannelChangeBitrateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerVoiceChannelChangeBitrate(event));
    }

    public void dispatchServerVoiceChannelChangeBitrateEvent(DispatchQueueSelector queueSelector, Server server, ServerVoiceChannel serverVoiceChannel, ServerVoiceChannelChangeBitrateEvent event) {
        ArrayList<ServerVoiceChannelChangeBitrateListener> listeners = new ArrayList<ServerVoiceChannelChangeBitrateListener>();
        if (server != null) {
            listeners.addAll(server.getServerVoiceChannelChangeBitrateListeners());
        }
        if (serverVoiceChannel != null) {
            listeners.addAll(serverVoiceChannel.getServerVoiceChannelChangeBitrateListeners());
        }
        listeners.addAll(this.getApi().getServerVoiceChannelChangeBitrateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerVoiceChannelChangeBitrate(event));
    }

    public void dispatchServerVoiceChannelChangeUserLimitEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerVoiceChannel> serverVoiceChannels, ServerVoiceChannelChangeUserLimitEvent event) {
        ArrayList<ServerVoiceChannelChangeUserLimitListener> listeners = new ArrayList<ServerVoiceChannelChangeUserLimitListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerVoiceChannelChangeUserLimitListeners).forEach(listeners::addAll);
        }
        if (serverVoiceChannels != null) {
            serverVoiceChannels.stream().map(ServerVoiceChannelAttachableListenerManager::getServerVoiceChannelChangeUserLimitListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerVoiceChannelChangeUserLimitListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerVoiceChannelChangeUserLimit(event));
    }

    public void dispatchServerVoiceChannelChangeUserLimitEvent(DispatchQueueSelector queueSelector, Server server, ServerVoiceChannel serverVoiceChannel, ServerVoiceChannelChangeUserLimitEvent event) {
        ArrayList<ServerVoiceChannelChangeUserLimitListener> listeners = new ArrayList<ServerVoiceChannelChangeUserLimitListener>();
        if (server != null) {
            listeners.addAll(server.getServerVoiceChannelChangeUserLimitListeners());
        }
        if (serverVoiceChannel != null) {
            listeners.addAll(serverVoiceChannel.getServerVoiceChannelChangeUserLimitListeners());
        }
        listeners.addAll(this.getApi().getServerVoiceChannelChangeUserLimitListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerVoiceChannelChangeUserLimit(event));
    }

    public void dispatchServerVoiceChannelMemberLeaveEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerVoiceChannel> serverVoiceChannels, Collection<User> users, ServerVoiceChannelMemberLeaveEvent event) {
        ArrayList<ServerVoiceChannelMemberLeaveListener> listeners = new ArrayList<ServerVoiceChannelMemberLeaveListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerVoiceChannelMemberLeaveListeners).forEach(listeners::addAll);
        }
        if (serverVoiceChannels != null) {
            serverVoiceChannels.stream().map(ServerVoiceChannelAttachableListenerManager::getServerVoiceChannelMemberLeaveListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getServerVoiceChannelMemberLeaveListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerVoiceChannelMemberLeaveListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerVoiceChannelMemberLeave(event));
    }

    public void dispatchServerVoiceChannelMemberLeaveEvent(DispatchQueueSelector queueSelector, Server server, ServerVoiceChannel serverVoiceChannel, User user, ServerVoiceChannelMemberLeaveEvent event) {
        ArrayList<ServerVoiceChannelMemberLeaveListener> listeners = new ArrayList<ServerVoiceChannelMemberLeaveListener>();
        if (server != null) {
            listeners.addAll(server.getServerVoiceChannelMemberLeaveListeners());
        }
        if (serverVoiceChannel != null) {
            listeners.addAll(serverVoiceChannel.getServerVoiceChannelMemberLeaveListeners());
        }
        if (user != null) {
            listeners.addAll(user.getServerVoiceChannelMemberLeaveListeners());
        }
        listeners.addAll(this.getApi().getServerVoiceChannelMemberLeaveListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerVoiceChannelMemberLeave(event));
    }

    public void dispatchServerVoiceChannelMemberLeaveEvent(DispatchQueueSelector queueSelector, Server server, ServerVoiceChannel serverVoiceChannel, long userId, ServerVoiceChannelMemberLeaveEvent event) {
        ArrayList<ServerVoiceChannelMemberLeaveListener> listeners = new ArrayList<ServerVoiceChannelMemberLeaveListener>();
        if (server != null) {
            listeners.addAll(server.getServerVoiceChannelMemberLeaveListeners());
        }
        if (serverVoiceChannel != null) {
            listeners.addAll(serverVoiceChannel.getServerVoiceChannelMemberLeaveListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, ServerVoiceChannelMemberLeaveListener.class));
        listeners.addAll(this.getApi().getServerVoiceChannelMemberLeaveListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerVoiceChannelMemberLeave(event));
    }

    public void dispatchServerVoiceChannelChangeNsfwEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerVoiceChannel> serverVoiceChannels, ServerVoiceChannelChangeNsfwEvent event) {
        ArrayList<ServerVoiceChannelChangeNsfwListener> listeners = new ArrayList<ServerVoiceChannelChangeNsfwListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerVoiceChannelChangeNsfwListeners).forEach(listeners::addAll);
        }
        if (serverVoiceChannels != null) {
            serverVoiceChannels.stream().map(ServerVoiceChannelAttachableListenerManager::getServerVoiceChannelChangeNsfwListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerVoiceChannelChangeNsfwListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerVoiceChannelChangeNsfw(event));
    }

    public void dispatchServerVoiceChannelChangeNsfwEvent(DispatchQueueSelector queueSelector, Server server, ServerVoiceChannel serverVoiceChannel, ServerVoiceChannelChangeNsfwEvent event) {
        ArrayList<ServerVoiceChannelChangeNsfwListener> listeners = new ArrayList<ServerVoiceChannelChangeNsfwListener>();
        if (server != null) {
            listeners.addAll(server.getServerVoiceChannelChangeNsfwListeners());
        }
        if (serverVoiceChannel != null) {
            listeners.addAll(serverVoiceChannel.getServerVoiceChannelChangeNsfwListeners());
        }
        listeners.addAll(this.getApi().getServerVoiceChannelChangeNsfwListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerVoiceChannelChangeNsfw(event));
    }

    public void dispatchServerVoiceChannelMemberJoinEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerVoiceChannel> serverVoiceChannels, Collection<User> users, ServerVoiceChannelMemberJoinEvent event) {
        ArrayList<ServerVoiceChannelMemberJoinListener> listeners = new ArrayList<ServerVoiceChannelMemberJoinListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerVoiceChannelMemberJoinListeners).forEach(listeners::addAll);
        }
        if (serverVoiceChannels != null) {
            serverVoiceChannels.stream().map(ServerVoiceChannelAttachableListenerManager::getServerVoiceChannelMemberJoinListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getServerVoiceChannelMemberJoinListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerVoiceChannelMemberJoinListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerVoiceChannelMemberJoin(event));
    }

    public void dispatchServerVoiceChannelMemberJoinEvent(DispatchQueueSelector queueSelector, Server server, ServerVoiceChannel serverVoiceChannel, User user, ServerVoiceChannelMemberJoinEvent event) {
        ArrayList<ServerVoiceChannelMemberJoinListener> listeners = new ArrayList<ServerVoiceChannelMemberJoinListener>();
        if (server != null) {
            listeners.addAll(server.getServerVoiceChannelMemberJoinListeners());
        }
        if (serverVoiceChannel != null) {
            listeners.addAll(serverVoiceChannel.getServerVoiceChannelMemberJoinListeners());
        }
        if (user != null) {
            listeners.addAll(user.getServerVoiceChannelMemberJoinListeners());
        }
        listeners.addAll(this.getApi().getServerVoiceChannelMemberJoinListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerVoiceChannelMemberJoin(event));
    }

    public void dispatchServerVoiceChannelMemberJoinEvent(DispatchQueueSelector queueSelector, Server server, ServerVoiceChannel serverVoiceChannel, long userId, ServerVoiceChannelMemberJoinEvent event) {
        ArrayList<ServerVoiceChannelMemberJoinListener> listeners = new ArrayList<ServerVoiceChannelMemberJoinListener>();
        if (server != null) {
            listeners.addAll(server.getServerVoiceChannelMemberJoinListeners());
        }
        if (serverVoiceChannel != null) {
            listeners.addAll(serverVoiceChannel.getServerVoiceChannelMemberJoinListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, ServerVoiceChannelMemberJoinListener.class));
        listeners.addAll(this.getApi().getServerVoiceChannelMemberJoinListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerVoiceChannelMemberJoin(event));
    }

    public void dispatchServerChannelChangeNameEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<ServerChannel> serverChannels, ServerChannelChangeNameEvent event) {
        ArrayList<ServerChannelChangeNameListener> listeners = new ArrayList<ServerChannelChangeNameListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getServerChannelChangeNameListeners).forEach(listeners::addAll);
        }
        if (serverChannels != null) {
            serverChannels.stream().map(ServerChannelAttachableListenerManager::getServerChannelChangeNameListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getServerChannelChangeNameListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChannelChangeName(event));
    }

    public void dispatchServerChannelChangeNameEvent(DispatchQueueSelector queueSelector, Server server, ServerChannel serverChannel, ServerChannelChangeNameEvent event) {
        ArrayList<ServerChannelChangeNameListener> listeners = new ArrayList<ServerChannelChangeNameListener>();
        if (server != null) {
            listeners.addAll(server.getServerChannelChangeNameListeners());
        }
        if (serverChannel != null) {
            listeners.addAll(serverChannel.getServerChannelChangeNameListeners());
        }
        listeners.addAll(this.getApi().getServerChannelChangeNameListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerChannelChangeName(event));
    }

    public void dispatchPrivateChannelDeleteEvent(DispatchQueueSelector queueSelector, Collection<PrivateChannel> privateChannels, Collection<User> users, PrivateChannelDeleteEvent event) {
        ArrayList<PrivateChannelDeleteListener> listeners = new ArrayList<PrivateChannelDeleteListener>();
        if (privateChannels != null) {
            privateChannels.stream().map(PrivateChannelAttachableListenerManager::getPrivateChannelDeleteListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getPrivateChannelDeleteListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getPrivateChannelDeleteListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onPrivateChannelDelete(event));
    }

    public void dispatchPrivateChannelDeleteEvent(DispatchQueueSelector queueSelector, PrivateChannel privateChannel, User user, PrivateChannelDeleteEvent event) {
        ArrayList<PrivateChannelDeleteListener> listeners = new ArrayList<PrivateChannelDeleteListener>();
        if (privateChannel != null) {
            listeners.addAll(privateChannel.getPrivateChannelDeleteListeners());
        }
        if (user != null) {
            listeners.addAll(user.getPrivateChannelDeleteListeners());
        }
        listeners.addAll(this.getApi().getPrivateChannelDeleteListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onPrivateChannelDelete(event));
    }

    public void dispatchPrivateChannelDeleteEvent(DispatchQueueSelector queueSelector, PrivateChannel privateChannel, long userId, PrivateChannelDeleteEvent event) {
        ArrayList<PrivateChannelDeleteListener> listeners = new ArrayList<PrivateChannelDeleteListener>();
        if (privateChannel != null) {
            listeners.addAll(privateChannel.getPrivateChannelDeleteListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, PrivateChannelDeleteListener.class));
        listeners.addAll(this.getApi().getPrivateChannelDeleteListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onPrivateChannelDelete(event));
    }

    public void dispatchPrivateChannelCreateEvent(DispatchQueueSelector queueSelector, Collection<User> users, PrivateChannelCreateEvent event) {
        ArrayList<PrivateChannelCreateListener> listeners = new ArrayList<PrivateChannelCreateListener>();
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getPrivateChannelCreateListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getPrivateChannelCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onPrivateChannelCreate(event));
    }

    public void dispatchPrivateChannelCreateEvent(DispatchQueueSelector queueSelector, User user, PrivateChannelCreateEvent event) {
        ArrayList<PrivateChannelCreateListener> listeners = new ArrayList<PrivateChannelCreateListener>();
        if (user != null) {
            listeners.addAll(user.getPrivateChannelCreateListeners());
        }
        listeners.addAll(this.getApi().getPrivateChannelCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onPrivateChannelCreate(event));
    }

    public void dispatchPrivateChannelCreateEvent(DispatchQueueSelector queueSelector, long userId, PrivateChannelCreateEvent event) {
        ArrayList<PrivateChannelCreateListener> listeners = new ArrayList<PrivateChannelCreateListener>();
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, PrivateChannelCreateListener.class));
        listeners.addAll(this.getApi().getPrivateChannelCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onPrivateChannelCreate(event));
    }

    public void dispatchAudioSourceFinishedEvent(DispatchQueueSelector queueSelector, Collection<AudioConnection> audioConnections, Collection<AudioSource> audioSources, AudioSourceFinishedEvent event) {
        ArrayList<AudioSourceFinishedListener> listeners = new ArrayList<AudioSourceFinishedListener>();
        if (audioConnections != null) {
            audioConnections.stream().map(AudioConnectionAttachableListenerManager::getAudioSourceFinishedListeners).forEach(listeners::addAll);
        }
        if (audioSources != null) {
            audioSources.stream().map(AudioSourceAttachableListenerManager::getAudioSourceFinishedListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getAudioSourceFinishedListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onAudioSourceFinished(event));
    }

    public void dispatchAudioSourceFinishedEvent(DispatchQueueSelector queueSelector, AudioConnection audioConnection, AudioSource audioSource, AudioSourceFinishedEvent event) {
        ArrayList<AudioSourceFinishedListener> listeners = new ArrayList<AudioSourceFinishedListener>();
        if (audioConnection != null) {
            listeners.addAll(audioConnection.getAudioSourceFinishedListeners());
        }
        if (audioSource != null) {
            listeners.addAll(audioSource.getAudioSourceFinishedListeners());
        }
        listeners.addAll(this.getApi().getAudioSourceFinishedListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onAudioSourceFinished(event));
    }

    public void dispatchUserChangeDeafenedEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<User> users, UserChangeDeafenedEvent event) {
        ArrayList<UserChangeDeafenedListener> listeners = new ArrayList<UserChangeDeafenedListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getUserChangeDeafenedListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getUserChangeDeafenedListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getUserChangeDeafenedListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeDeafened(event));
    }

    public void dispatchUserChangeDeafenedEvent(DispatchQueueSelector queueSelector, Server server, User user, UserChangeDeafenedEvent event) {
        ArrayList<UserChangeDeafenedListener> listeners = new ArrayList<UserChangeDeafenedListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeDeafenedListeners());
        }
        if (user != null) {
            listeners.addAll(user.getUserChangeDeafenedListeners());
        }
        listeners.addAll(this.getApi().getUserChangeDeafenedListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeDeafened(event));
    }

    public void dispatchUserChangeDeafenedEvent(DispatchQueueSelector queueSelector, Server server, long userId, UserChangeDeafenedEvent event) {
        ArrayList<UserChangeDeafenedListener> listeners = new ArrayList<UserChangeDeafenedListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeDeafenedListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, UserChangeDeafenedListener.class));
        listeners.addAll(this.getApi().getUserChangeDeafenedListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeDeafened(event));
    }

    public void dispatchUserChangeNicknameEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<User> users, UserChangeNicknameEvent event) {
        ArrayList<UserChangeNicknameListener> listeners = new ArrayList<UserChangeNicknameListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getUserChangeNicknameListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getUserChangeNicknameListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getUserChangeNicknameListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeNickname(event));
    }

    public void dispatchUserChangeNicknameEvent(DispatchQueueSelector queueSelector, Server server, User user, UserChangeNicknameEvent event) {
        ArrayList<UserChangeNicknameListener> listeners = new ArrayList<UserChangeNicknameListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeNicknameListeners());
        }
        if (user != null) {
            listeners.addAll(user.getUserChangeNicknameListeners());
        }
        listeners.addAll(this.getApi().getUserChangeNicknameListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeNickname(event));
    }

    public void dispatchUserChangeNicknameEvent(DispatchQueueSelector queueSelector, Server server, long userId, UserChangeNicknameEvent event) {
        ArrayList<UserChangeNicknameListener> listeners = new ArrayList<UserChangeNicknameListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeNicknameListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, UserChangeNicknameListener.class));
        listeners.addAll(this.getApi().getUserChangeNicknameListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeNickname(event));
    }

    public void dispatchUserChangePendingEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<User> users, UserChangePendingEvent event) {
        ArrayList<UserChangePendingListener> listeners = new ArrayList<UserChangePendingListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getUserChangePendingListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getUserChangePendingListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getUserChangePendingListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerMemberChangePending(event));
    }

    public void dispatchUserChangePendingEvent(DispatchQueueSelector queueSelector, Server server, User user, UserChangePendingEvent event) {
        ArrayList<UserChangePendingListener> listeners = new ArrayList<UserChangePendingListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangePendingListeners());
        }
        if (user != null) {
            listeners.addAll(user.getUserChangePendingListeners());
        }
        listeners.addAll(this.getApi().getUserChangePendingListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerMemberChangePending(event));
    }

    public void dispatchUserChangePendingEvent(DispatchQueueSelector queueSelector, Server server, long userId, UserChangePendingEvent event) {
        ArrayList<UserChangePendingListener> listeners = new ArrayList<UserChangePendingListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangePendingListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, UserChangePendingListener.class));
        listeners.addAll(this.getApi().getUserChangePendingListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onServerMemberChangePending(event));
    }

    public void dispatchUserStartTypingEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<TextChannel> textChannels, Collection<User> users, UserStartTypingEvent event) {
        ArrayList<UserStartTypingListener> listeners = new ArrayList<UserStartTypingListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getUserStartTypingListeners).forEach(listeners::addAll);
        }
        if (textChannels != null) {
            textChannels.stream().map(TextChannelAttachableListenerManager::getUserStartTypingListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getUserStartTypingListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getUserStartTypingListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserStartTyping(event));
    }

    public void dispatchUserStartTypingEvent(DispatchQueueSelector queueSelector, Server server, TextChannel textChannel, User user, UserStartTypingEvent event) {
        ArrayList<UserStartTypingListener> listeners = new ArrayList<UserStartTypingListener>();
        if (server != null) {
            listeners.addAll(server.getUserStartTypingListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getUserStartTypingListeners());
        }
        if (user != null) {
            listeners.addAll(user.getUserStartTypingListeners());
        }
        listeners.addAll(this.getApi().getUserStartTypingListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserStartTyping(event));
    }

    public void dispatchUserStartTypingEvent(DispatchQueueSelector queueSelector, Server server, TextChannel textChannel, long userId, UserStartTypingEvent event) {
        ArrayList<UserStartTypingListener> listeners = new ArrayList<UserStartTypingListener>();
        if (server != null) {
            listeners.addAll(server.getUserStartTypingListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getUserStartTypingListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, UserStartTypingListener.class));
        listeners.addAll(this.getApi().getUserStartTypingListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserStartTyping(event));
    }

    public void dispatchUserChangeDiscriminatorEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<User> users, UserChangeDiscriminatorEvent event) {
        ArrayList<UserChangeDiscriminatorListener> listeners = new ArrayList<UserChangeDiscriminatorListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getUserChangeDiscriminatorListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getUserChangeDiscriminatorListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getUserChangeDiscriminatorListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeDiscriminator(event));
    }

    public void dispatchUserChangeDiscriminatorEvent(DispatchQueueSelector queueSelector, Server server, User user, UserChangeDiscriminatorEvent event) {
        ArrayList<UserChangeDiscriminatorListener> listeners = new ArrayList<UserChangeDiscriminatorListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeDiscriminatorListeners());
        }
        if (user != null) {
            listeners.addAll(user.getUserChangeDiscriminatorListeners());
        }
        listeners.addAll(this.getApi().getUserChangeDiscriminatorListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeDiscriminator(event));
    }

    public void dispatchUserChangeDiscriminatorEvent(DispatchQueueSelector queueSelector, Server server, long userId, UserChangeDiscriminatorEvent event) {
        ArrayList<UserChangeDiscriminatorListener> listeners = new ArrayList<UserChangeDiscriminatorListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeDiscriminatorListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, UserChangeDiscriminatorListener.class));
        listeners.addAll(this.getApi().getUserChangeDiscriminatorListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeDiscriminator(event));
    }

    public void dispatchUserChangeStatusEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<User> users, UserChangeStatusEvent event) {
        ArrayList<UserChangeStatusListener> listeners = new ArrayList<UserChangeStatusListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getUserChangeStatusListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getUserChangeStatusListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getUserChangeStatusListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeStatus(event));
    }

    public void dispatchUserChangeStatusEvent(DispatchQueueSelector queueSelector, Server server, User user, UserChangeStatusEvent event) {
        ArrayList<UserChangeStatusListener> listeners = new ArrayList<UserChangeStatusListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeStatusListeners());
        }
        if (user != null) {
            listeners.addAll(user.getUserChangeStatusListeners());
        }
        listeners.addAll(this.getApi().getUserChangeStatusListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeStatus(event));
    }

    public void dispatchUserChangeStatusEvent(DispatchQueueSelector queueSelector, Server server, long userId, UserChangeStatusEvent event) {
        ArrayList<UserChangeStatusListener> listeners = new ArrayList<UserChangeStatusListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeStatusListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, UserChangeStatusListener.class));
        listeners.addAll(this.getApi().getUserChangeStatusListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeStatus(event));
    }

    public void dispatchUserChangeServerAvatarEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<User> users, UserChangeServerAvatarEvent event) {
        ArrayList<UserChangeServerAvatarListener> listeners = new ArrayList<UserChangeServerAvatarListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getUserChangeServerAvatarListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getUserChangeServerAvatarListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getUserChangeServerAvatarListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeServerAvatar(event));
    }

    public void dispatchUserChangeServerAvatarEvent(DispatchQueueSelector queueSelector, Server server, User user, UserChangeServerAvatarEvent event) {
        ArrayList<UserChangeServerAvatarListener> listeners = new ArrayList<UserChangeServerAvatarListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeServerAvatarListeners());
        }
        if (user != null) {
            listeners.addAll(user.getUserChangeServerAvatarListeners());
        }
        listeners.addAll(this.getApi().getUserChangeServerAvatarListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeServerAvatar(event));
    }

    public void dispatchUserChangeServerAvatarEvent(DispatchQueueSelector queueSelector, Server server, long userId, UserChangeServerAvatarEvent event) {
        ArrayList<UserChangeServerAvatarListener> listeners = new ArrayList<UserChangeServerAvatarListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeServerAvatarListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, UserChangeServerAvatarListener.class));
        listeners.addAll(this.getApi().getUserChangeServerAvatarListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeServerAvatar(event));
    }

    public void dispatchUserChangeSelfMutedEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<User> users, UserChangeSelfMutedEvent event) {
        ArrayList<UserChangeSelfMutedListener> listeners = new ArrayList<UserChangeSelfMutedListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getUserChangeSelfMutedListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getUserChangeSelfMutedListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getUserChangeSelfMutedListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeSelfMuted(event));
    }

    public void dispatchUserChangeSelfMutedEvent(DispatchQueueSelector queueSelector, Server server, User user, UserChangeSelfMutedEvent event) {
        ArrayList<UserChangeSelfMutedListener> listeners = new ArrayList<UserChangeSelfMutedListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeSelfMutedListeners());
        }
        if (user != null) {
            listeners.addAll(user.getUserChangeSelfMutedListeners());
        }
        listeners.addAll(this.getApi().getUserChangeSelfMutedListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeSelfMuted(event));
    }

    public void dispatchUserChangeSelfMutedEvent(DispatchQueueSelector queueSelector, Server server, long userId, UserChangeSelfMutedEvent event) {
        ArrayList<UserChangeSelfMutedListener> listeners = new ArrayList<UserChangeSelfMutedListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeSelfMutedListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, UserChangeSelfMutedListener.class));
        listeners.addAll(this.getApi().getUserChangeSelfMutedListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeSelfMuted(event));
    }

    public void dispatchUserChangeNameEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<User> users, UserChangeNameEvent event) {
        ArrayList<UserChangeNameListener> listeners = new ArrayList<UserChangeNameListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getUserChangeNameListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getUserChangeNameListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getUserChangeNameListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeName(event));
    }

    public void dispatchUserChangeNameEvent(DispatchQueueSelector queueSelector, Server server, User user, UserChangeNameEvent event) {
        ArrayList<UserChangeNameListener> listeners = new ArrayList<UserChangeNameListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeNameListeners());
        }
        if (user != null) {
            listeners.addAll(user.getUserChangeNameListeners());
        }
        listeners.addAll(this.getApi().getUserChangeNameListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeName(event));
    }

    public void dispatchUserChangeNameEvent(DispatchQueueSelector queueSelector, Server server, long userId, UserChangeNameEvent event) {
        ArrayList<UserChangeNameListener> listeners = new ArrayList<UserChangeNameListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeNameListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, UserChangeNameListener.class));
        listeners.addAll(this.getApi().getUserChangeNameListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeName(event));
    }

    public void dispatchUserChangeTimeoutEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<User> users, UserChangeTimeoutEvent event) {
        ArrayList<UserChangeTimeoutListener> listeners = new ArrayList<UserChangeTimeoutListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getUserChangeTimeoutListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getUserChangeTimeoutListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getUserChangeTimeoutListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeTimeout(event));
    }

    public void dispatchUserChangeTimeoutEvent(DispatchQueueSelector queueSelector, Server server, User user, UserChangeTimeoutEvent event) {
        ArrayList<UserChangeTimeoutListener> listeners = new ArrayList<UserChangeTimeoutListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeTimeoutListeners());
        }
        if (user != null) {
            listeners.addAll(user.getUserChangeTimeoutListeners());
        }
        listeners.addAll(this.getApi().getUserChangeTimeoutListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeTimeout(event));
    }

    public void dispatchUserChangeTimeoutEvent(DispatchQueueSelector queueSelector, Server server, long userId, UserChangeTimeoutEvent event) {
        ArrayList<UserChangeTimeoutListener> listeners = new ArrayList<UserChangeTimeoutListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeTimeoutListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, UserChangeTimeoutListener.class));
        listeners.addAll(this.getApi().getUserChangeTimeoutListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeTimeout(event));
    }

    public void dispatchUserChangeAvatarEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<User> users, UserChangeAvatarEvent event) {
        ArrayList<UserChangeAvatarListener> listeners = new ArrayList<UserChangeAvatarListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getUserChangeAvatarListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getUserChangeAvatarListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getUserChangeAvatarListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeAvatar(event));
    }

    public void dispatchUserChangeAvatarEvent(DispatchQueueSelector queueSelector, Server server, User user, UserChangeAvatarEvent event) {
        ArrayList<UserChangeAvatarListener> listeners = new ArrayList<UserChangeAvatarListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeAvatarListeners());
        }
        if (user != null) {
            listeners.addAll(user.getUserChangeAvatarListeners());
        }
        listeners.addAll(this.getApi().getUserChangeAvatarListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeAvatar(event));
    }

    public void dispatchUserChangeAvatarEvent(DispatchQueueSelector queueSelector, Server server, long userId, UserChangeAvatarEvent event) {
        ArrayList<UserChangeAvatarListener> listeners = new ArrayList<UserChangeAvatarListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeAvatarListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, UserChangeAvatarListener.class));
        listeners.addAll(this.getApi().getUserChangeAvatarListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeAvatar(event));
    }

    public void dispatchUserChangeSelfDeafenedEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<User> users, UserChangeSelfDeafenedEvent event) {
        ArrayList<UserChangeSelfDeafenedListener> listeners = new ArrayList<UserChangeSelfDeafenedListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getUserChangeSelfDeafenedListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getUserChangeSelfDeafenedListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getUserChangeSelfDeafenedListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeSelfDeafened(event));
    }

    public void dispatchUserChangeSelfDeafenedEvent(DispatchQueueSelector queueSelector, Server server, User user, UserChangeSelfDeafenedEvent event) {
        ArrayList<UserChangeSelfDeafenedListener> listeners = new ArrayList<UserChangeSelfDeafenedListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeSelfDeafenedListeners());
        }
        if (user != null) {
            listeners.addAll(user.getUserChangeSelfDeafenedListeners());
        }
        listeners.addAll(this.getApi().getUserChangeSelfDeafenedListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeSelfDeafened(event));
    }

    public void dispatchUserChangeSelfDeafenedEvent(DispatchQueueSelector queueSelector, Server server, long userId, UserChangeSelfDeafenedEvent event) {
        ArrayList<UserChangeSelfDeafenedListener> listeners = new ArrayList<UserChangeSelfDeafenedListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeSelfDeafenedListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, UserChangeSelfDeafenedListener.class));
        listeners.addAll(this.getApi().getUserChangeSelfDeafenedListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeSelfDeafened(event));
    }

    public void dispatchUserChangeMutedEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<User> users, UserChangeMutedEvent event) {
        ArrayList<UserChangeMutedListener> listeners = new ArrayList<UserChangeMutedListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getUserChangeMutedListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getUserChangeMutedListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getUserChangeMutedListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeMuted(event));
    }

    public void dispatchUserChangeMutedEvent(DispatchQueueSelector queueSelector, Server server, User user, UserChangeMutedEvent event) {
        ArrayList<UserChangeMutedListener> listeners = new ArrayList<UserChangeMutedListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeMutedListeners());
        }
        if (user != null) {
            listeners.addAll(user.getUserChangeMutedListeners());
        }
        listeners.addAll(this.getApi().getUserChangeMutedListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeMuted(event));
    }

    public void dispatchUserChangeMutedEvent(DispatchQueueSelector queueSelector, Server server, long userId, UserChangeMutedEvent event) {
        ArrayList<UserChangeMutedListener> listeners = new ArrayList<UserChangeMutedListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeMutedListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, UserChangeMutedListener.class));
        listeners.addAll(this.getApi().getUserChangeMutedListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeMuted(event));
    }

    public void dispatchUserChangeActivityEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<User> users, UserChangeActivityEvent event) {
        ArrayList<UserChangeActivityListener> listeners = new ArrayList<UserChangeActivityListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getUserChangeActivityListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getUserChangeActivityListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getUserChangeActivityListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeActivity(event));
    }

    public void dispatchUserChangeActivityEvent(DispatchQueueSelector queueSelector, Server server, User user, UserChangeActivityEvent event) {
        ArrayList<UserChangeActivityListener> listeners = new ArrayList<UserChangeActivityListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeActivityListeners());
        }
        if (user != null) {
            listeners.addAll(user.getUserChangeActivityListeners());
        }
        listeners.addAll(this.getApi().getUserChangeActivityListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeActivity(event));
    }

    public void dispatchUserChangeActivityEvent(DispatchQueueSelector queueSelector, Server server, long userId, UserChangeActivityEvent event) {
        ArrayList<UserChangeActivityListener> listeners = new ArrayList<UserChangeActivityListener>();
        if (server != null) {
            listeners.addAll(server.getUserChangeActivityListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, UserChangeActivityListener.class));
        listeners.addAll(this.getApi().getUserChangeActivityListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onUserChangeActivity(event));
    }

    public void dispatchMessageEditEvent(DispatchQueueSelector queueSelector, Collection<Message> messages, Collection<Server> servers, Collection<TextChannel> textChannels, MessageEditEvent event) {
        ArrayList<MessageEditListener> listeners = new ArrayList<MessageEditListener>();
        if (messages != null) {
            messages.stream().map(MessageAttachableListenerManager::getMessageEditListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getMessageEditListeners).forEach(listeners::addAll);
        }
        if (textChannels != null) {
            textChannels.stream().map(TextChannelAttachableListenerManager::getMessageEditListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getMessageEditListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onMessageEdit(event));
    }

    public void dispatchMessageEditEvent(DispatchQueueSelector queueSelector, long messageId, Server server, TextChannel textChannel, MessageEditEvent event) {
        ArrayList<MessageEditListener> listeners = new ArrayList<MessageEditListener>();
        listeners.addAll(MessageAttachableListenerManager.getMessageEditListeners((DiscordApi)this.getApi(), messageId));
        if (server != null) {
            listeners.addAll(server.getMessageEditListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getMessageEditListeners());
        }
        listeners.addAll(this.getApi().getMessageEditListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onMessageEdit(event));
    }

    public void dispatchChannelPinsUpdateEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<TextChannel> textChannels, ChannelPinsUpdateEvent event) {
        ArrayList<ChannelPinsUpdateListener> listeners = new ArrayList<ChannelPinsUpdateListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getChannelPinsUpdateListeners).forEach(listeners::addAll);
        }
        if (textChannels != null) {
            textChannels.stream().map(TextChannelAttachableListenerManager::getChannelPinsUpdateListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getChannelPinsUpdateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onChannelPinsUpdate(event));
    }

    public void dispatchChannelPinsUpdateEvent(DispatchQueueSelector queueSelector, Server server, TextChannel textChannel, ChannelPinsUpdateEvent event) {
        ArrayList<ChannelPinsUpdateListener> listeners = new ArrayList<ChannelPinsUpdateListener>();
        if (server != null) {
            listeners.addAll(server.getChannelPinsUpdateListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getChannelPinsUpdateListeners());
        }
        listeners.addAll(this.getApi().getChannelPinsUpdateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onChannelPinsUpdate(event));
    }

    public void dispatchReactionRemoveEvent(DispatchQueueSelector queueSelector, Collection<Message> messages, Collection<Server> servers, Collection<TextChannel> textChannels, Collection<User> users, ReactionRemoveEvent event) {
        ArrayList<ReactionRemoveListener> listeners = new ArrayList<ReactionRemoveListener>();
        if (messages != null) {
            messages.stream().map(MessageAttachableListenerManager::getReactionRemoveListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getReactionRemoveListeners).forEach(listeners::addAll);
        }
        if (textChannels != null) {
            textChannels.stream().map(TextChannelAttachableListenerManager::getReactionRemoveListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getReactionRemoveListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getReactionRemoveListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onReactionRemove(event));
    }

    public void dispatchReactionRemoveEvent(DispatchQueueSelector queueSelector, long messageId, Server server, TextChannel textChannel, User user, ReactionRemoveEvent event) {
        ArrayList<ReactionRemoveListener> listeners = new ArrayList<ReactionRemoveListener>();
        listeners.addAll(MessageAttachableListenerManager.getReactionRemoveListeners((DiscordApi)this.getApi(), messageId));
        if (server != null) {
            listeners.addAll(server.getReactionRemoveListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getReactionRemoveListeners());
        }
        if (user != null) {
            listeners.addAll(user.getReactionRemoveListeners());
        }
        listeners.addAll(this.getApi().getReactionRemoveListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onReactionRemove(event));
    }

    public void dispatchReactionRemoveEvent(DispatchQueueSelector queueSelector, long messageId, Server server, TextChannel textChannel, long userId, ReactionRemoveEvent event) {
        ArrayList<ReactionRemoveListener> listeners = new ArrayList<ReactionRemoveListener>();
        listeners.addAll(MessageAttachableListenerManager.getReactionRemoveListeners((DiscordApi)this.getApi(), messageId));
        if (server != null) {
            listeners.addAll(server.getReactionRemoveListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getReactionRemoveListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, ReactionRemoveListener.class));
        listeners.addAll(this.getApi().getReactionRemoveListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onReactionRemove(event));
    }

    public void dispatchReactionAddEvent(DispatchQueueSelector queueSelector, Collection<Message> messages, Collection<Server> servers, Collection<TextChannel> textChannels, Collection<User> users, ReactionAddEvent event) {
        ArrayList<ReactionAddListener> listeners = new ArrayList<ReactionAddListener>();
        if (messages != null) {
            messages.stream().map(MessageAttachableListenerManager::getReactionAddListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getReactionAddListeners).forEach(listeners::addAll);
        }
        if (textChannels != null) {
            textChannels.stream().map(TextChannelAttachableListenerManager::getReactionAddListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getReactionAddListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getReactionAddListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onReactionAdd(event));
    }

    public void dispatchReactionAddEvent(DispatchQueueSelector queueSelector, long messageId, Server server, TextChannel textChannel, User user, ReactionAddEvent event) {
        ArrayList<ReactionAddListener> listeners = new ArrayList<ReactionAddListener>();
        listeners.addAll(MessageAttachableListenerManager.getReactionAddListeners((DiscordApi)this.getApi(), messageId));
        if (server != null) {
            listeners.addAll(server.getReactionAddListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getReactionAddListeners());
        }
        if (user != null) {
            listeners.addAll(user.getReactionAddListeners());
        }
        listeners.addAll(this.getApi().getReactionAddListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onReactionAdd(event));
    }

    public void dispatchReactionAddEvent(DispatchQueueSelector queueSelector, long messageId, Server server, TextChannel textChannel, long userId, ReactionAddEvent event) {
        ArrayList<ReactionAddListener> listeners = new ArrayList<ReactionAddListener>();
        listeners.addAll(MessageAttachableListenerManager.getReactionAddListeners((DiscordApi)this.getApi(), messageId));
        if (server != null) {
            listeners.addAll(server.getReactionAddListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getReactionAddListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, ReactionAddListener.class));
        listeners.addAll(this.getApi().getReactionAddListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onReactionAdd(event));
    }

    public void dispatchReactionRemoveAllEvent(DispatchQueueSelector queueSelector, Collection<Message> messages, Collection<Server> servers, Collection<TextChannel> textChannels, ReactionRemoveAllEvent event) {
        ArrayList<ReactionRemoveAllListener> listeners = new ArrayList<ReactionRemoveAllListener>();
        if (messages != null) {
            messages.stream().map(MessageAttachableListenerManager::getReactionRemoveAllListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getReactionRemoveAllListeners).forEach(listeners::addAll);
        }
        if (textChannels != null) {
            textChannels.stream().map(TextChannelAttachableListenerManager::getReactionRemoveAllListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getReactionRemoveAllListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onReactionRemoveAll(event));
    }

    public void dispatchReactionRemoveAllEvent(DispatchQueueSelector queueSelector, long messageId, Server server, TextChannel textChannel, ReactionRemoveAllEvent event) {
        ArrayList<ReactionRemoveAllListener> listeners = new ArrayList<ReactionRemoveAllListener>();
        listeners.addAll(MessageAttachableListenerManager.getReactionRemoveAllListeners((DiscordApi)this.getApi(), messageId));
        if (server != null) {
            listeners.addAll(server.getReactionRemoveAllListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getReactionRemoveAllListeners());
        }
        listeners.addAll(this.getApi().getReactionRemoveAllListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onReactionRemoveAll(event));
    }

    public void dispatchMessageCreateEvent(DispatchQueueSelector queueSelector, Collection<Server> servers, Collection<TextChannel> textChannels, Collection<User> users, Collection<Long> webhookIds, MessageCreateEvent event) {
        ArrayList<MessageCreateListener> listeners = new ArrayList<MessageCreateListener>();
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getMessageCreateListeners).forEach(listeners::addAll);
        }
        if (textChannels != null) {
            textChannels.stream().map(TextChannelAttachableListenerManager::getMessageCreateListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getMessageCreateListeners).forEach(listeners::addAll);
        }
        if (webhookIds != null) {
            webhookIds.stream().map(webhookId -> this.getApi().getObjectListeners(Webhook.class, (long)webhookId, MessageCreateListener.class)).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getMessageCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onMessageCreate(event));
    }

    public void dispatchMessageCreateEvent(DispatchQueueSelector queueSelector, Server server, TextChannel textChannel, User user, Long webhookId, MessageCreateEvent event) {
        ArrayList<MessageCreateListener> listeners = new ArrayList<MessageCreateListener>();
        if (server != null) {
            listeners.addAll(server.getMessageCreateListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getMessageCreateListeners());
        }
        if (user != null) {
            listeners.addAll(user.getMessageCreateListeners());
        }
        if (webhookId != null) {
            listeners.addAll(this.getApi().getObjectListeners(Webhook.class, webhookId, MessageCreateListener.class));
        }
        listeners.addAll(this.getApi().getMessageCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onMessageCreate(event));
    }

    public void dispatchMessageCreateEvent(DispatchQueueSelector queueSelector, Server server, TextChannel textChannel, long userId, Long webhookId, MessageCreateEvent event) {
        ArrayList<MessageCreateListener> listeners = new ArrayList<MessageCreateListener>();
        if (server != null) {
            listeners.addAll(server.getMessageCreateListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getMessageCreateListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, MessageCreateListener.class));
        if (webhookId != null) {
            listeners.addAll(this.getApi().getObjectListeners(Webhook.class, webhookId, MessageCreateListener.class));
        }
        listeners.addAll(this.getApi().getMessageCreateListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onMessageCreate(event));
    }

    public void dispatchCachedMessageUnpinEvent(DispatchQueueSelector queueSelector, Collection<Message> messages, Collection<Server> servers, Collection<TextChannel> textChannels, CachedMessageUnpinEvent event) {
        ArrayList<CachedMessageUnpinListener> listeners = new ArrayList<CachedMessageUnpinListener>();
        if (messages != null) {
            messages.stream().map(MessageAttachableListenerManager::getCachedMessageUnpinListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getCachedMessageUnpinListeners).forEach(listeners::addAll);
        }
        if (textChannels != null) {
            textChannels.stream().map(TextChannelAttachableListenerManager::getCachedMessageUnpinListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getCachedMessageUnpinListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onCachedMessageUnpin(event));
    }

    public void dispatchCachedMessageUnpinEvent(DispatchQueueSelector queueSelector, Message message, Server server, TextChannel textChannel, CachedMessageUnpinEvent event) {
        ArrayList<CachedMessageUnpinListener> listeners = new ArrayList<CachedMessageUnpinListener>();
        if (message != null) {
            listeners.addAll(message.getCachedMessageUnpinListeners());
        }
        if (server != null) {
            listeners.addAll(server.getCachedMessageUnpinListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getCachedMessageUnpinListeners());
        }
        listeners.addAll(this.getApi().getCachedMessageUnpinListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onCachedMessageUnpin(event));
    }

    public void dispatchCachedMessagePinEvent(DispatchQueueSelector queueSelector, Collection<Message> messages, Collection<Server> servers, Collection<TextChannel> textChannels, CachedMessagePinEvent event) {
        ArrayList<CachedMessagePinListener> listeners = new ArrayList<CachedMessagePinListener>();
        if (messages != null) {
            messages.stream().map(MessageAttachableListenerManager::getCachedMessagePinListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getCachedMessagePinListeners).forEach(listeners::addAll);
        }
        if (textChannels != null) {
            textChannels.stream().map(TextChannelAttachableListenerManager::getCachedMessagePinListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getCachedMessagePinListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onCachedMessagePin(event));
    }

    public void dispatchCachedMessagePinEvent(DispatchQueueSelector queueSelector, Message message, Server server, TextChannel textChannel, CachedMessagePinEvent event) {
        ArrayList<CachedMessagePinListener> listeners = new ArrayList<CachedMessagePinListener>();
        if (message != null) {
            listeners.addAll(message.getCachedMessagePinListeners());
        }
        if (server != null) {
            listeners.addAll(server.getCachedMessagePinListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getCachedMessagePinListeners());
        }
        listeners.addAll(this.getApi().getCachedMessagePinListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onCachedMessagePin(event));
    }

    public void dispatchMessageReplyEvent(DispatchQueueSelector queueSelector, Collection<Message> messages, Collection<Server> servers, Collection<TextChannel> textChannels, Collection<User> users, Collection<Long> webhookIds, MessageReplyEvent event) {
        ArrayList<MessageReplyListener> listeners = new ArrayList<MessageReplyListener>();
        if (messages != null) {
            messages.stream().map(MessageAttachableListenerManager::getMessageReplyListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getMessageReplyListeners).forEach(listeners::addAll);
        }
        if (textChannels != null) {
            textChannels.stream().map(TextChannelAttachableListenerManager::getMessageReplyListeners).forEach(listeners::addAll);
        }
        if (users != null) {
            users.stream().map(UserAttachableListenerManager::getMessageReplyListeners).forEach(listeners::addAll);
        }
        if (webhookIds != null) {
            webhookIds.stream().map(webhookId -> this.getApi().getObjectListeners(Webhook.class, (long)webhookId, MessageReplyListener.class)).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getMessageReplyListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onMessageReply(event));
    }

    public void dispatchMessageReplyEvent(DispatchQueueSelector queueSelector, long messageId, Server server, TextChannel textChannel, User user, Long webhookId, MessageReplyEvent event) {
        ArrayList<MessageReplyListener> listeners = new ArrayList<MessageReplyListener>();
        listeners.addAll(MessageAttachableListenerManager.getMessageReplyListeners((DiscordApi)this.getApi(), messageId));
        if (server != null) {
            listeners.addAll(server.getMessageReplyListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getMessageReplyListeners());
        }
        if (user != null) {
            listeners.addAll(user.getMessageReplyListeners());
        }
        if (webhookId != null) {
            listeners.addAll(this.getApi().getObjectListeners(Webhook.class, webhookId, MessageReplyListener.class));
        }
        listeners.addAll(this.getApi().getMessageReplyListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onMessageReply(event));
    }

    public void dispatchMessageReplyEvent(DispatchQueueSelector queueSelector, long messageId, Server server, TextChannel textChannel, long userId, Long webhookId, MessageReplyEvent event) {
        ArrayList<MessageReplyListener> listeners = new ArrayList<MessageReplyListener>();
        listeners.addAll(MessageAttachableListenerManager.getMessageReplyListeners((DiscordApi)this.getApi(), messageId));
        if (server != null) {
            listeners.addAll(server.getMessageReplyListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getMessageReplyListeners());
        }
        listeners.addAll(this.getApi().getObjectListeners(User.class, userId, MessageReplyListener.class));
        if (webhookId != null) {
            listeners.addAll(this.getApi().getObjectListeners(Webhook.class, webhookId, MessageReplyListener.class));
        }
        listeners.addAll(this.getApi().getMessageReplyListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onMessageReply(event));
    }

    public void dispatchMessageDeleteEvent(DispatchQueueSelector queueSelector, Collection<Message> messages, Collection<Server> servers, Collection<TextChannel> textChannels, MessageDeleteEvent event) {
        ArrayList<MessageDeleteListener> listeners = new ArrayList<MessageDeleteListener>();
        if (messages != null) {
            messages.stream().map(MessageAttachableListenerManager::getMessageDeleteListeners).forEach(listeners::addAll);
        }
        if (servers != null) {
            servers.stream().map(ServerAttachableListenerManager::getMessageDeleteListeners).forEach(listeners::addAll);
        }
        if (textChannels != null) {
            textChannels.stream().map(TextChannelAttachableListenerManager::getMessageDeleteListeners).forEach(listeners::addAll);
        }
        listeners.addAll(this.getApi().getMessageDeleteListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onMessageDelete(event));
    }

    public void dispatchMessageDeleteEvent(DispatchQueueSelector queueSelector, long messageId, Server server, TextChannel textChannel, MessageDeleteEvent event) {
        ArrayList<MessageDeleteListener> listeners = new ArrayList<MessageDeleteListener>();
        listeners.addAll(MessageAttachableListenerManager.getMessageDeleteListeners((DiscordApi)this.getApi(), messageId));
        if (server != null) {
            listeners.addAll(server.getMessageDeleteListeners());
        }
        if (textChannel != null) {
            listeners.addAll(textChannel.getMessageDeleteListeners());
        }
        listeners.addAll(this.getApi().getMessageDeleteListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onMessageDelete(event));
    }

    public void dispatchResumeEvent(DispatchQueueSelector queueSelector, ResumeEvent event) {
        ArrayList<ResumeListener> listeners = new ArrayList<ResumeListener>();
        listeners.addAll(this.getApi().getResumeListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onResume(event));
    }

    public void dispatchLostConnectionEvent(DispatchQueueSelector queueSelector, LostConnectionEvent event) {
        ArrayList<LostConnectionListener> listeners = new ArrayList<LostConnectionListener>();
        listeners.addAll(this.getApi().getLostConnectionListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onLostConnection(event));
    }

    public void dispatchReconnectEvent(DispatchQueueSelector queueSelector, ReconnectEvent event) {
        ArrayList<ReconnectListener> listeners = new ArrayList<ReconnectListener>();
        listeners.addAll(this.getApi().getReconnectListeners());
        this.dispatchEvent(queueSelector, listeners, listener -> listener.onReconnect(event));
    }
}

