/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener;

import java.util.function.Function;
import java.util.function.Supplier;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.audio.AudioSourceFinishedListener;
import org.javacord.api.listener.channel.server.ServerChannelChangeNameListener;
import org.javacord.api.listener.channel.server.ServerChannelChangeNsfwFlagListener;
import org.javacord.api.listener.channel.server.ServerChannelChangeOverwrittenPermissionsListener;
import org.javacord.api.listener.channel.server.ServerChannelChangePositionListener;
import org.javacord.api.listener.channel.server.ServerChannelCreateListener;
import org.javacord.api.listener.channel.server.ServerChannelDeleteListener;
import org.javacord.api.listener.channel.server.invite.ServerChannelInviteCreateListener;
import org.javacord.api.listener.channel.server.invite.ServerChannelInviteDeleteListener;
import org.javacord.api.listener.channel.server.text.ServerTextChannelChangeDefaultAutoArchiveDurationListener;
import org.javacord.api.listener.channel.server.text.ServerTextChannelChangeSlowmodeListener;
import org.javacord.api.listener.channel.server.text.ServerTextChannelChangeTopicListener;
import org.javacord.api.listener.channel.server.text.WebhooksUpdateListener;
import org.javacord.api.listener.channel.server.thread.ServerThreadChannelCreateListener;
import org.javacord.api.listener.channel.server.thread.ServerThreadChannelDeleteListener;
import org.javacord.api.listener.channel.server.thread.ServerThreadChannelMembersUpdateListener;
import org.javacord.api.listener.channel.server.thread.ServerThreadChannelUpdateListener;
import org.javacord.api.listener.channel.server.thread.ServerThreadListSyncListener;
import org.javacord.api.listener.channel.server.voice.ServerStageVoiceChannelChangeTopicListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelChangeBitrateListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelChangeNsfwListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelChangeUserLimitListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberJoinListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberLeaveListener;
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
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.MessageDeleteListener;
import org.javacord.api.listener.message.MessageEditListener;
import org.javacord.api.listener.message.MessageReplyListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveAllListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.javacord.api.listener.server.ApplicationCommandPermissionsUpdateListener;
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
import org.javacord.api.listener.server.emoji.KnownCustomEmojiChangeNameListener;
import org.javacord.api.listener.server.emoji.KnownCustomEmojiChangeWhitelistedRolesListener;
import org.javacord.api.listener.server.emoji.KnownCustomEmojiCreateListener;
import org.javacord.api.listener.server.emoji.KnownCustomEmojiDeleteListener;
import org.javacord.api.listener.server.member.ServerMemberBanListener;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import org.javacord.api.listener.server.member.ServerMemberLeaveListener;
import org.javacord.api.listener.server.member.ServerMemberUnbanListener;
import org.javacord.api.listener.server.member.ServerMembersChunkListener;
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

public interface ChainableGloballyAttachableListenerManager {
    default public DiscordApiBuilder addInteractionCreateListener(InteractionCreateListener listener) {
        return this.addListener(InteractionCreateListener.class, listener);
    }

    default public DiscordApiBuilder addInteractionCreateListener(Supplier<InteractionCreateListener> listenerSupplier) {
        return this.addListener(InteractionCreateListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addInteractionCreateListener(Function<DiscordApi, InteractionCreateListener> listenerFunction) {
        return this.addListener(InteractionCreateListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addSlashCommandCreateListener(SlashCommandCreateListener listener) {
        return this.addListener(SlashCommandCreateListener.class, listener);
    }

    default public DiscordApiBuilder addSlashCommandCreateListener(Supplier<SlashCommandCreateListener> listenerSupplier) {
        return this.addListener(SlashCommandCreateListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addSlashCommandCreateListener(Function<DiscordApi, SlashCommandCreateListener> listenerFunction) {
        return this.addListener(SlashCommandCreateListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addAutocompleteCreateListener(AutocompleteCreateListener listener) {
        return this.addListener(AutocompleteCreateListener.class, listener);
    }

    default public DiscordApiBuilder addAutocompleteCreateListener(Supplier<AutocompleteCreateListener> listenerSupplier) {
        return this.addListener(AutocompleteCreateListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addAutocompleteCreateListener(Function<DiscordApi, AutocompleteCreateListener> listenerFunction) {
        return this.addListener(AutocompleteCreateListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addModalSubmitListener(ModalSubmitListener listener) {
        return this.addListener(ModalSubmitListener.class, listener);
    }

    default public DiscordApiBuilder addModalSubmitListener(Supplier<ModalSubmitListener> listenerSupplier) {
        return this.addListener(ModalSubmitListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addModalSubmitListener(Function<DiscordApi, ModalSubmitListener> listenerFunction) {
        return this.addListener(ModalSubmitListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addMessageContextMenuCommandListener(MessageContextMenuCommandListener listener) {
        return this.addListener(MessageContextMenuCommandListener.class, listener);
    }

    default public DiscordApiBuilder addMessageContextMenuCommandListener(Supplier<MessageContextMenuCommandListener> listenerSupplier) {
        return this.addListener(MessageContextMenuCommandListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addMessageContextMenuCommandListener(Function<DiscordApi, MessageContextMenuCommandListener> listenerFunction) {
        return this.addListener(MessageContextMenuCommandListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addMessageComponentCreateListener(MessageComponentCreateListener listener) {
        return this.addListener(MessageComponentCreateListener.class, listener);
    }

    default public DiscordApiBuilder addMessageComponentCreateListener(Supplier<MessageComponentCreateListener> listenerSupplier) {
        return this.addListener(MessageComponentCreateListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addMessageComponentCreateListener(Function<DiscordApi, MessageComponentCreateListener> listenerFunction) {
        return this.addListener(MessageComponentCreateListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addUserContextMenuCommandListener(UserContextMenuCommandListener listener) {
        return this.addListener(UserContextMenuCommandListener.class, listener);
    }

    default public DiscordApiBuilder addUserContextMenuCommandListener(Supplier<UserContextMenuCommandListener> listenerSupplier) {
        return this.addListener(UserContextMenuCommandListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addUserContextMenuCommandListener(Function<DiscordApi, UserContextMenuCommandListener> listenerFunction) {
        return this.addListener(UserContextMenuCommandListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addSelectMenuChooseListener(SelectMenuChooseListener listener) {
        return this.addListener(SelectMenuChooseListener.class, listener);
    }

    default public DiscordApiBuilder addSelectMenuChooseListener(Supplier<SelectMenuChooseListener> listenerSupplier) {
        return this.addListener(SelectMenuChooseListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addSelectMenuChooseListener(Function<DiscordApi, SelectMenuChooseListener> listenerFunction) {
        return this.addListener(SelectMenuChooseListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addButtonClickListener(ButtonClickListener listener) {
        return this.addListener(ButtonClickListener.class, listener);
    }

    default public DiscordApiBuilder addButtonClickListener(Supplier<ButtonClickListener> listenerSupplier) {
        return this.addListener(ButtonClickListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addButtonClickListener(Function<DiscordApi, ButtonClickListener> listenerFunction) {
        return this.addListener(ButtonClickListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChangeIconListener(ServerChangeIconListener listener) {
        return this.addListener(ServerChangeIconListener.class, listener);
    }

    default public DiscordApiBuilder addServerChangeIconListener(Supplier<ServerChangeIconListener> listenerSupplier) {
        return this.addListener(ServerChangeIconListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChangeIconListener(Function<DiscordApi, ServerChangeIconListener> listenerFunction) {
        return this.addListener(ServerChangeIconListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChangeNameListener(ServerChangeNameListener listener) {
        return this.addListener(ServerChangeNameListener.class, listener);
    }

    default public DiscordApiBuilder addServerChangeNameListener(Supplier<ServerChangeNameListener> listenerSupplier) {
        return this.addListener(ServerChangeNameListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChangeNameListener(Function<DiscordApi, ServerChangeNameListener> listenerFunction) {
        return this.addListener(ServerChangeNameListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeLastMessageIdListener(ServerThreadChannelChangeLastMessageIdListener listener) {
        return this.addListener(ServerThreadChannelChangeLastMessageIdListener.class, listener);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeLastMessageIdListener(Supplier<ServerThreadChannelChangeLastMessageIdListener> listenerSupplier) {
        return this.addListener(ServerThreadChannelChangeLastMessageIdListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeLastMessageIdListener(Function<DiscordApi, ServerThreadChannelChangeLastMessageIdListener> listenerFunction) {
        return this.addListener(ServerThreadChannelChangeLastMessageIdListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeArchivedListener(ServerThreadChannelChangeArchivedListener listener) {
        return this.addListener(ServerThreadChannelChangeArchivedListener.class, listener);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeArchivedListener(Supplier<ServerThreadChannelChangeArchivedListener> listenerSupplier) {
        return this.addListener(ServerThreadChannelChangeArchivedListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeArchivedListener(Function<DiscordApi, ServerThreadChannelChangeArchivedListener> listenerFunction) {
        return this.addListener(ServerThreadChannelChangeArchivedListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeMemberCountListener(ServerThreadChannelChangeMemberCountListener listener) {
        return this.addListener(ServerThreadChannelChangeMemberCountListener.class, listener);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeMemberCountListener(Supplier<ServerThreadChannelChangeMemberCountListener> listenerSupplier) {
        return this.addListener(ServerThreadChannelChangeMemberCountListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeMemberCountListener(Function<DiscordApi, ServerThreadChannelChangeMemberCountListener> listenerFunction) {
        return this.addListener(ServerThreadChannelChangeMemberCountListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerPrivateThreadJoinListener(ServerPrivateThreadJoinListener listener) {
        return this.addListener(ServerPrivateThreadJoinListener.class, listener);
    }

    default public DiscordApiBuilder addServerPrivateThreadJoinListener(Supplier<ServerPrivateThreadJoinListener> listenerSupplier) {
        return this.addListener(ServerPrivateThreadJoinListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerPrivateThreadJoinListener(Function<DiscordApi, ServerPrivateThreadJoinListener> listenerFunction) {
        return this.addListener(ServerPrivateThreadJoinListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeInvitableListener(ServerThreadChannelChangeInvitableListener listener) {
        return this.addListener(ServerThreadChannelChangeInvitableListener.class, listener);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeInvitableListener(Supplier<ServerThreadChannelChangeInvitableListener> listenerSupplier) {
        return this.addListener(ServerThreadChannelChangeInvitableListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeInvitableListener(Function<DiscordApi, ServerThreadChannelChangeInvitableListener> listenerFunction) {
        return this.addListener(ServerThreadChannelChangeInvitableListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeAutoArchiveDurationListener(ServerThreadChannelChangeAutoArchiveDurationListener listener) {
        return this.addListener(ServerThreadChannelChangeAutoArchiveDurationListener.class, listener);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeAutoArchiveDurationListener(Supplier<ServerThreadChannelChangeAutoArchiveDurationListener> listenerSupplier) {
        return this.addListener(ServerThreadChannelChangeAutoArchiveDurationListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeAutoArchiveDurationListener(Function<DiscordApi, ServerThreadChannelChangeAutoArchiveDurationListener> listenerFunction) {
        return this.addListener(ServerThreadChannelChangeAutoArchiveDurationListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeRateLimitPerUserListener(ServerThreadChannelChangeRateLimitPerUserListener listener) {
        return this.addListener(ServerThreadChannelChangeRateLimitPerUserListener.class, listener);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeRateLimitPerUserListener(Supplier<ServerThreadChannelChangeRateLimitPerUserListener> listenerSupplier) {
        return this.addListener(ServerThreadChannelChangeRateLimitPerUserListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeRateLimitPerUserListener(Function<DiscordApi, ServerThreadChannelChangeRateLimitPerUserListener> listenerFunction) {
        return this.addListener(ServerThreadChannelChangeRateLimitPerUserListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeLockedListener(ServerThreadChannelChangeLockedListener listener) {
        return this.addListener(ServerThreadChannelChangeLockedListener.class, listener);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeLockedListener(Supplier<ServerThreadChannelChangeLockedListener> listenerSupplier) {
        return this.addListener(ServerThreadChannelChangeLockedListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeLockedListener(Function<DiscordApi, ServerThreadChannelChangeLockedListener> listenerFunction) {
        return this.addListener(ServerThreadChannelChangeLockedListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeArchiveTimestampListener(ServerThreadChannelChangeArchiveTimestampListener listener) {
        return this.addListener(ServerThreadChannelChangeArchiveTimestampListener.class, listener);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeArchiveTimestampListener(Supplier<ServerThreadChannelChangeArchiveTimestampListener> listenerSupplier) {
        return this.addListener(ServerThreadChannelChangeArchiveTimestampListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeArchiveTimestampListener(Function<DiscordApi, ServerThreadChannelChangeArchiveTimestampListener> listenerFunction) {
        return this.addListener(ServerThreadChannelChangeArchiveTimestampListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeTotalMessageSentListener(ServerThreadChannelChangeTotalMessageSentListener listener) {
        return this.addListener(ServerThreadChannelChangeTotalMessageSentListener.class, listener);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeTotalMessageSentListener(Supplier<ServerThreadChannelChangeTotalMessageSentListener> listenerSupplier) {
        return this.addListener(ServerThreadChannelChangeTotalMessageSentListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeTotalMessageSentListener(Function<DiscordApi, ServerThreadChannelChangeTotalMessageSentListener> listenerFunction) {
        return this.addListener(ServerThreadChannelChangeTotalMessageSentListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeMessageCountListener(ServerThreadChannelChangeMessageCountListener listener) {
        return this.addListener(ServerThreadChannelChangeMessageCountListener.class, listener);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeMessageCountListener(Supplier<ServerThreadChannelChangeMessageCountListener> listenerSupplier) {
        return this.addListener(ServerThreadChannelChangeMessageCountListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerThreadChannelChangeMessageCountListener(Function<DiscordApi, ServerThreadChannelChangeMessageCountListener> listenerFunction) {
        return this.addListener(ServerThreadChannelChangeMessageCountListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChangeAfkTimeoutListener(ServerChangeAfkTimeoutListener listener) {
        return this.addListener(ServerChangeAfkTimeoutListener.class, listener);
    }

    default public DiscordApiBuilder addServerChangeAfkTimeoutListener(Supplier<ServerChangeAfkTimeoutListener> listenerSupplier) {
        return this.addListener(ServerChangeAfkTimeoutListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChangeAfkTimeoutListener(Function<DiscordApi, ServerChangeAfkTimeoutListener> listenerFunction) {
        return this.addListener(ServerChangeAfkTimeoutListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addStickerChangeTagsListener(StickerChangeTagsListener listener) {
        return this.addListener(StickerChangeTagsListener.class, listener);
    }

    default public DiscordApiBuilder addStickerChangeTagsListener(Supplier<StickerChangeTagsListener> listenerSupplier) {
        return this.addListener(StickerChangeTagsListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addStickerChangeTagsListener(Function<DiscordApi, StickerChangeTagsListener> listenerFunction) {
        return this.addListener(StickerChangeTagsListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addStickerChangeDescriptionListener(StickerChangeDescriptionListener listener) {
        return this.addListener(StickerChangeDescriptionListener.class, listener);
    }

    default public DiscordApiBuilder addStickerChangeDescriptionListener(Supplier<StickerChangeDescriptionListener> listenerSupplier) {
        return this.addListener(StickerChangeDescriptionListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addStickerChangeDescriptionListener(Function<DiscordApi, StickerChangeDescriptionListener> listenerFunction) {
        return this.addListener(StickerChangeDescriptionListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addStickerCreateListener(StickerCreateListener listener) {
        return this.addListener(StickerCreateListener.class, listener);
    }

    default public DiscordApiBuilder addStickerCreateListener(Supplier<StickerCreateListener> listenerSupplier) {
        return this.addListener(StickerCreateListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addStickerCreateListener(Function<DiscordApi, StickerCreateListener> listenerFunction) {
        return this.addListener(StickerCreateListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addStickerChangeNameListener(StickerChangeNameListener listener) {
        return this.addListener(StickerChangeNameListener.class, listener);
    }

    default public DiscordApiBuilder addStickerChangeNameListener(Supplier<StickerChangeNameListener> listenerSupplier) {
        return this.addListener(StickerChangeNameListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addStickerChangeNameListener(Function<DiscordApi, StickerChangeNameListener> listenerFunction) {
        return this.addListener(StickerChangeNameListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addStickerDeleteListener(StickerDeleteListener listener) {
        return this.addListener(StickerDeleteListener.class, listener);
    }

    default public DiscordApiBuilder addStickerDeleteListener(Supplier<StickerDeleteListener> listenerSupplier) {
        return this.addListener(StickerDeleteListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addStickerDeleteListener(Function<DiscordApi, StickerDeleteListener> listenerFunction) {
        return this.addListener(StickerDeleteListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChangeSplashListener(ServerChangeSplashListener listener) {
        return this.addListener(ServerChangeSplashListener.class, listener);
    }

    default public DiscordApiBuilder addServerChangeSplashListener(Supplier<ServerChangeSplashListener> listenerSupplier) {
        return this.addListener(ServerChangeSplashListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChangeSplashListener(Function<DiscordApi, ServerChangeSplashListener> listenerFunction) {
        return this.addListener(ServerChangeSplashListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChangeAfkChannelListener(ServerChangeAfkChannelListener listener) {
        return this.addListener(ServerChangeAfkChannelListener.class, listener);
    }

    default public DiscordApiBuilder addServerChangeAfkChannelListener(Supplier<ServerChangeAfkChannelListener> listenerSupplier) {
        return this.addListener(ServerChangeAfkChannelListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChangeAfkChannelListener(Function<DiscordApi, ServerChangeAfkChannelListener> listenerFunction) {
        return this.addListener(ServerChangeAfkChannelListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addVoiceStateUpdateListener(VoiceStateUpdateListener listener) {
        return this.addListener(VoiceStateUpdateListener.class, listener);
    }

    default public DiscordApiBuilder addVoiceStateUpdateListener(Supplier<VoiceStateUpdateListener> listenerSupplier) {
        return this.addListener(VoiceStateUpdateListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addVoiceStateUpdateListener(Function<DiscordApi, VoiceStateUpdateListener> listenerFunction) {
        return this.addListener(VoiceStateUpdateListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChangeVanityUrlCodeListener(ServerChangeVanityUrlCodeListener listener) {
        return this.addListener(ServerChangeVanityUrlCodeListener.class, listener);
    }

    default public DiscordApiBuilder addServerChangeVanityUrlCodeListener(Supplier<ServerChangeVanityUrlCodeListener> listenerSupplier) {
        return this.addListener(ServerChangeVanityUrlCodeListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChangeVanityUrlCodeListener(Function<DiscordApi, ServerChangeVanityUrlCodeListener> listenerFunction) {
        return this.addListener(ServerChangeVanityUrlCodeListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChangeDiscoverySplashListener(ServerChangeDiscoverySplashListener listener) {
        return this.addListener(ServerChangeDiscoverySplashListener.class, listener);
    }

    default public DiscordApiBuilder addServerChangeDiscoverySplashListener(Supplier<ServerChangeDiscoverySplashListener> listenerSupplier) {
        return this.addListener(ServerChangeDiscoverySplashListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChangeDiscoverySplashListener(Function<DiscordApi, ServerChangeDiscoverySplashListener> listenerFunction) {
        return this.addListener(ServerChangeDiscoverySplashListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerJoinListener(ServerJoinListener listener) {
        return this.addListener(ServerJoinListener.class, listener);
    }

    default public DiscordApiBuilder addServerJoinListener(Supplier<ServerJoinListener> listenerSupplier) {
        return this.addListener(ServerJoinListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerJoinListener(Function<DiscordApi, ServerJoinListener> listenerFunction) {
        return this.addListener(ServerJoinListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addApplicationCommandPermissionsUpdateListener(ApplicationCommandPermissionsUpdateListener listener) {
        return this.addListener(ApplicationCommandPermissionsUpdateListener.class, listener);
    }

    default public DiscordApiBuilder addApplicationCommandPermissionsUpdateListener(Supplier<ApplicationCommandPermissionsUpdateListener> listenerSupplier) {
        return this.addListener(ApplicationCommandPermissionsUpdateListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addApplicationCommandPermissionsUpdateListener(Function<DiscordApi, ApplicationCommandPermissionsUpdateListener> listenerFunction) {
        return this.addListener(ApplicationCommandPermissionsUpdateListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerBecomesUnavailableListener(ServerBecomesUnavailableListener listener) {
        return this.addListener(ServerBecomesUnavailableListener.class, listener);
    }

    default public DiscordApiBuilder addServerBecomesUnavailableListener(Supplier<ServerBecomesUnavailableListener> listenerSupplier) {
        return this.addListener(ServerBecomesUnavailableListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerBecomesUnavailableListener(Function<DiscordApi, ServerBecomesUnavailableListener> listenerFunction) {
        return this.addListener(ServerBecomesUnavailableListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addVoiceServerUpdateListener(VoiceServerUpdateListener listener) {
        return this.addListener(VoiceServerUpdateListener.class, listener);
    }

    default public DiscordApiBuilder addVoiceServerUpdateListener(Supplier<VoiceServerUpdateListener> listenerSupplier) {
        return this.addListener(VoiceServerUpdateListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addVoiceServerUpdateListener(Function<DiscordApi, VoiceServerUpdateListener> listenerFunction) {
        return this.addListener(VoiceServerUpdateListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChangeDescriptionListener(ServerChangeDescriptionListener listener) {
        return this.addListener(ServerChangeDescriptionListener.class, listener);
    }

    default public DiscordApiBuilder addServerChangeDescriptionListener(Supplier<ServerChangeDescriptionListener> listenerSupplier) {
        return this.addListener(ServerChangeDescriptionListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChangeDescriptionListener(Function<DiscordApi, ServerChangeDescriptionListener> listenerFunction) {
        return this.addListener(ServerChangeDescriptionListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChangeVerificationLevelListener(ServerChangeVerificationLevelListener listener) {
        return this.addListener(ServerChangeVerificationLevelListener.class, listener);
    }

    default public DiscordApiBuilder addServerChangeVerificationLevelListener(Supplier<ServerChangeVerificationLevelListener> listenerSupplier) {
        return this.addListener(ServerChangeVerificationLevelListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChangeVerificationLevelListener(Function<DiscordApi, ServerChangeVerificationLevelListener> listenerFunction) {
        return this.addListener(ServerChangeVerificationLevelListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerLeaveListener(ServerLeaveListener listener) {
        return this.addListener(ServerLeaveListener.class, listener);
    }

    default public DiscordApiBuilder addServerLeaveListener(Supplier<ServerLeaveListener> listenerSupplier) {
        return this.addListener(ServerLeaveListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerLeaveListener(Function<DiscordApi, ServerLeaveListener> listenerFunction) {
        return this.addListener(ServerLeaveListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChangeBoostCountListener(ServerChangeBoostCountListener listener) {
        return this.addListener(ServerChangeBoostCountListener.class, listener);
    }

    default public DiscordApiBuilder addServerChangeBoostCountListener(Supplier<ServerChangeBoostCountListener> listenerSupplier) {
        return this.addListener(ServerChangeBoostCountListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChangeBoostCountListener(Function<DiscordApi, ServerChangeBoostCountListener> listenerFunction) {
        return this.addListener(ServerChangeBoostCountListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerBecomesAvailableListener(ServerBecomesAvailableListener listener) {
        return this.addListener(ServerBecomesAvailableListener.class, listener);
    }

    default public DiscordApiBuilder addServerBecomesAvailableListener(Supplier<ServerBecomesAvailableListener> listenerSupplier) {
        return this.addListener(ServerBecomesAvailableListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerBecomesAvailableListener(Function<DiscordApi, ServerBecomesAvailableListener> listenerFunction) {
        return this.addListener(ServerBecomesAvailableListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChangeDefaultMessageNotificationLevelListener(ServerChangeDefaultMessageNotificationLevelListener listener) {
        return this.addListener(ServerChangeDefaultMessageNotificationLevelListener.class, listener);
    }

    default public DiscordApiBuilder addServerChangeDefaultMessageNotificationLevelListener(Supplier<ServerChangeDefaultMessageNotificationLevelListener> listenerSupplier) {
        return this.addListener(ServerChangeDefaultMessageNotificationLevelListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChangeDefaultMessageNotificationLevelListener(Function<DiscordApi, ServerChangeDefaultMessageNotificationLevelListener> listenerFunction) {
        return this.addListener(ServerChangeDefaultMessageNotificationLevelListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChangeRegionListener(ServerChangeRegionListener listener) {
        return this.addListener(ServerChangeRegionListener.class, listener);
    }

    default public DiscordApiBuilder addServerChangeRegionListener(Supplier<ServerChangeRegionListener> listenerSupplier) {
        return this.addListener(ServerChangeRegionListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChangeRegionListener(Function<DiscordApi, ServerChangeRegionListener> listenerFunction) {
        return this.addListener(ServerChangeRegionListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerMemberJoinListener(ServerMemberJoinListener listener) {
        return this.addListener(ServerMemberJoinListener.class, listener);
    }

    default public DiscordApiBuilder addServerMemberJoinListener(Supplier<ServerMemberJoinListener> listenerSupplier) {
        return this.addListener(ServerMemberJoinListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerMemberJoinListener(Function<DiscordApi, ServerMemberJoinListener> listenerFunction) {
        return this.addListener(ServerMemberJoinListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerMemberLeaveListener(ServerMemberLeaveListener listener) {
        return this.addListener(ServerMemberLeaveListener.class, listener);
    }

    default public DiscordApiBuilder addServerMemberLeaveListener(Supplier<ServerMemberLeaveListener> listenerSupplier) {
        return this.addListener(ServerMemberLeaveListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerMemberLeaveListener(Function<DiscordApi, ServerMemberLeaveListener> listenerFunction) {
        return this.addListener(ServerMemberLeaveListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerMemberBanListener(ServerMemberBanListener listener) {
        return this.addListener(ServerMemberBanListener.class, listener);
    }

    default public DiscordApiBuilder addServerMemberBanListener(Supplier<ServerMemberBanListener> listenerSupplier) {
        return this.addListener(ServerMemberBanListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerMemberBanListener(Function<DiscordApi, ServerMemberBanListener> listenerFunction) {
        return this.addListener(ServerMemberBanListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerMembersChunkListener(ServerMembersChunkListener listener) {
        return this.addListener(ServerMembersChunkListener.class, listener);
    }

    default public DiscordApiBuilder addServerMembersChunkListener(Supplier<ServerMembersChunkListener> listenerSupplier) {
        return this.addListener(ServerMembersChunkListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerMembersChunkListener(Function<DiscordApi, ServerMembersChunkListener> listenerFunction) {
        return this.addListener(ServerMembersChunkListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerMemberUnbanListener(ServerMemberUnbanListener listener) {
        return this.addListener(ServerMemberUnbanListener.class, listener);
    }

    default public DiscordApiBuilder addServerMemberUnbanListener(Supplier<ServerMemberUnbanListener> listenerSupplier) {
        return this.addListener(ServerMemberUnbanListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerMemberUnbanListener(Function<DiscordApi, ServerMemberUnbanListener> listenerFunction) {
        return this.addListener(ServerMemberUnbanListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addKnownCustomEmojiChangeNameListener(KnownCustomEmojiChangeNameListener listener) {
        return this.addListener(KnownCustomEmojiChangeNameListener.class, listener);
    }

    default public DiscordApiBuilder addKnownCustomEmojiChangeNameListener(Supplier<KnownCustomEmojiChangeNameListener> listenerSupplier) {
        return this.addListener(KnownCustomEmojiChangeNameListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addKnownCustomEmojiChangeNameListener(Function<DiscordApi, KnownCustomEmojiChangeNameListener> listenerFunction) {
        return this.addListener(KnownCustomEmojiChangeNameListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addKnownCustomEmojiDeleteListener(KnownCustomEmojiDeleteListener listener) {
        return this.addListener(KnownCustomEmojiDeleteListener.class, listener);
    }

    default public DiscordApiBuilder addKnownCustomEmojiDeleteListener(Supplier<KnownCustomEmojiDeleteListener> listenerSupplier) {
        return this.addListener(KnownCustomEmojiDeleteListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addKnownCustomEmojiDeleteListener(Function<DiscordApi, KnownCustomEmojiDeleteListener> listenerFunction) {
        return this.addListener(KnownCustomEmojiDeleteListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addKnownCustomEmojiChangeWhitelistedRolesListener(KnownCustomEmojiChangeWhitelistedRolesListener listener) {
        return this.addListener(KnownCustomEmojiChangeWhitelistedRolesListener.class, listener);
    }

    default public DiscordApiBuilder addKnownCustomEmojiChangeWhitelistedRolesListener(Supplier<KnownCustomEmojiChangeWhitelistedRolesListener> listenerSupplier) {
        return this.addListener(KnownCustomEmojiChangeWhitelistedRolesListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addKnownCustomEmojiChangeWhitelistedRolesListener(Function<DiscordApi, KnownCustomEmojiChangeWhitelistedRolesListener> listenerFunction) {
        return this.addListener(KnownCustomEmojiChangeWhitelistedRolesListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addKnownCustomEmojiCreateListener(KnownCustomEmojiCreateListener listener) {
        return this.addListener(KnownCustomEmojiCreateListener.class, listener);
    }

    default public DiscordApiBuilder addKnownCustomEmojiCreateListener(Supplier<KnownCustomEmojiCreateListener> listenerSupplier) {
        return this.addListener(KnownCustomEmojiCreateListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addKnownCustomEmojiCreateListener(Function<DiscordApi, KnownCustomEmojiCreateListener> listenerFunction) {
        return this.addListener(KnownCustomEmojiCreateListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChangeSystemChannelListener(ServerChangeSystemChannelListener listener) {
        return this.addListener(ServerChangeSystemChannelListener.class, listener);
    }

    default public DiscordApiBuilder addServerChangeSystemChannelListener(Supplier<ServerChangeSystemChannelListener> listenerSupplier) {
        return this.addListener(ServerChangeSystemChannelListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChangeSystemChannelListener(Function<DiscordApi, ServerChangeSystemChannelListener> listenerFunction) {
        return this.addListener(ServerChangeSystemChannelListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChangePreferredLocaleListener(ServerChangePreferredLocaleListener listener) {
        return this.addListener(ServerChangePreferredLocaleListener.class, listener);
    }

    default public DiscordApiBuilder addServerChangePreferredLocaleListener(Supplier<ServerChangePreferredLocaleListener> listenerSupplier) {
        return this.addListener(ServerChangePreferredLocaleListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChangePreferredLocaleListener(Function<DiscordApi, ServerChangePreferredLocaleListener> listenerFunction) {
        return this.addListener(ServerChangePreferredLocaleListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChangeBoostLevelListener(ServerChangeBoostLevelListener listener) {
        return this.addListener(ServerChangeBoostLevelListener.class, listener);
    }

    default public DiscordApiBuilder addServerChangeBoostLevelListener(Supplier<ServerChangeBoostLevelListener> listenerSupplier) {
        return this.addListener(ServerChangeBoostLevelListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChangeBoostLevelListener(Function<DiscordApi, ServerChangeBoostLevelListener> listenerFunction) {
        return this.addListener(ServerChangeBoostLevelListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChangeRulesChannelListener(ServerChangeRulesChannelListener listener) {
        return this.addListener(ServerChangeRulesChannelListener.class, listener);
    }

    default public DiscordApiBuilder addServerChangeRulesChannelListener(Supplier<ServerChangeRulesChannelListener> listenerSupplier) {
        return this.addListener(ServerChangeRulesChannelListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChangeRulesChannelListener(Function<DiscordApi, ServerChangeRulesChannelListener> listenerFunction) {
        return this.addListener(ServerChangeRulesChannelListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChangeServerFeatureListener(ServerChangeServerFeatureListener listener) {
        return this.addListener(ServerChangeServerFeatureListener.class, listener);
    }

    default public DiscordApiBuilder addServerChangeServerFeatureListener(Supplier<ServerChangeServerFeatureListener> listenerSupplier) {
        return this.addListener(ServerChangeServerFeatureListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChangeServerFeatureListener(Function<DiscordApi, ServerChangeServerFeatureListener> listenerFunction) {
        return this.addListener(ServerChangeServerFeatureListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChangeOwnerListener(ServerChangeOwnerListener listener) {
        return this.addListener(ServerChangeOwnerListener.class, listener);
    }

    default public DiscordApiBuilder addServerChangeOwnerListener(Supplier<ServerChangeOwnerListener> listenerSupplier) {
        return this.addListener(ServerChangeOwnerListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChangeOwnerListener(Function<DiscordApi, ServerChangeOwnerListener> listenerFunction) {
        return this.addListener(ServerChangeOwnerListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChangeMultiFactorAuthenticationLevelListener(ServerChangeMultiFactorAuthenticationLevelListener listener) {
        return this.addListener(ServerChangeMultiFactorAuthenticationLevelListener.class, listener);
    }

    default public DiscordApiBuilder addServerChangeMultiFactorAuthenticationLevelListener(Supplier<ServerChangeMultiFactorAuthenticationLevelListener> listenerSupplier) {
        return this.addListener(ServerChangeMultiFactorAuthenticationLevelListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChangeMultiFactorAuthenticationLevelListener(Function<DiscordApi, ServerChangeMultiFactorAuthenticationLevelListener> listenerFunction) {
        return this.addListener(ServerChangeMultiFactorAuthenticationLevelListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChangeExplicitContentFilterLevelListener(ServerChangeExplicitContentFilterLevelListener listener) {
        return this.addListener(ServerChangeExplicitContentFilterLevelListener.class, listener);
    }

    default public DiscordApiBuilder addServerChangeExplicitContentFilterLevelListener(Supplier<ServerChangeExplicitContentFilterLevelListener> listenerSupplier) {
        return this.addListener(ServerChangeExplicitContentFilterLevelListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChangeExplicitContentFilterLevelListener(Function<DiscordApi, ServerChangeExplicitContentFilterLevelListener> listenerFunction) {
        return this.addListener(ServerChangeExplicitContentFilterLevelListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addRoleChangePositionListener(RoleChangePositionListener listener) {
        return this.addListener(RoleChangePositionListener.class, listener);
    }

    default public DiscordApiBuilder addRoleChangePositionListener(Supplier<RoleChangePositionListener> listenerSupplier) {
        return this.addListener(RoleChangePositionListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addRoleChangePositionListener(Function<DiscordApi, RoleChangePositionListener> listenerFunction) {
        return this.addListener(RoleChangePositionListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addRoleChangeMentionableListener(RoleChangeMentionableListener listener) {
        return this.addListener(RoleChangeMentionableListener.class, listener);
    }

    default public DiscordApiBuilder addRoleChangeMentionableListener(Supplier<RoleChangeMentionableListener> listenerSupplier) {
        return this.addListener(RoleChangeMentionableListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addRoleChangeMentionableListener(Function<DiscordApi, RoleChangeMentionableListener> listenerFunction) {
        return this.addListener(RoleChangeMentionableListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addRoleChangeColorListener(RoleChangeColorListener listener) {
        return this.addListener(RoleChangeColorListener.class, listener);
    }

    default public DiscordApiBuilder addRoleChangeColorListener(Supplier<RoleChangeColorListener> listenerSupplier) {
        return this.addListener(RoleChangeColorListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addRoleChangeColorListener(Function<DiscordApi, RoleChangeColorListener> listenerFunction) {
        return this.addListener(RoleChangeColorListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addRoleChangeNameListener(RoleChangeNameListener listener) {
        return this.addListener(RoleChangeNameListener.class, listener);
    }

    default public DiscordApiBuilder addRoleChangeNameListener(Supplier<RoleChangeNameListener> listenerSupplier) {
        return this.addListener(RoleChangeNameListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addRoleChangeNameListener(Function<DiscordApi, RoleChangeNameListener> listenerFunction) {
        return this.addListener(RoleChangeNameListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addRoleChangeHoistListener(RoleChangeHoistListener listener) {
        return this.addListener(RoleChangeHoistListener.class, listener);
    }

    default public DiscordApiBuilder addRoleChangeHoistListener(Supplier<RoleChangeHoistListener> listenerSupplier) {
        return this.addListener(RoleChangeHoistListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addRoleChangeHoistListener(Function<DiscordApi, RoleChangeHoistListener> listenerFunction) {
        return this.addListener(RoleChangeHoistListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addRoleCreateListener(RoleCreateListener listener) {
        return this.addListener(RoleCreateListener.class, listener);
    }

    default public DiscordApiBuilder addRoleCreateListener(Supplier<RoleCreateListener> listenerSupplier) {
        return this.addListener(RoleCreateListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addRoleCreateListener(Function<DiscordApi, RoleCreateListener> listenerFunction) {
        return this.addListener(RoleCreateListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addRoleChangePermissionsListener(RoleChangePermissionsListener listener) {
        return this.addListener(RoleChangePermissionsListener.class, listener);
    }

    default public DiscordApiBuilder addRoleChangePermissionsListener(Supplier<RoleChangePermissionsListener> listenerSupplier) {
        return this.addListener(RoleChangePermissionsListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addRoleChangePermissionsListener(Function<DiscordApi, RoleChangePermissionsListener> listenerFunction) {
        return this.addListener(RoleChangePermissionsListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addUserRoleRemoveListener(UserRoleRemoveListener listener) {
        return this.addListener(UserRoleRemoveListener.class, listener);
    }

    default public DiscordApiBuilder addUserRoleRemoveListener(Supplier<UserRoleRemoveListener> listenerSupplier) {
        return this.addListener(UserRoleRemoveListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addUserRoleRemoveListener(Function<DiscordApi, UserRoleRemoveListener> listenerFunction) {
        return this.addListener(UserRoleRemoveListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addUserRoleAddListener(UserRoleAddListener listener) {
        return this.addListener(UserRoleAddListener.class, listener);
    }

    default public DiscordApiBuilder addUserRoleAddListener(Supplier<UserRoleAddListener> listenerSupplier) {
        return this.addListener(UserRoleAddListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addUserRoleAddListener(Function<DiscordApi, UserRoleAddListener> listenerFunction) {
        return this.addListener(UserRoleAddListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addRoleDeleteListener(RoleDeleteListener listener) {
        return this.addListener(RoleDeleteListener.class, listener);
    }

    default public DiscordApiBuilder addRoleDeleteListener(Supplier<RoleDeleteListener> listenerSupplier) {
        return this.addListener(RoleDeleteListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addRoleDeleteListener(Function<DiscordApi, RoleDeleteListener> listenerFunction) {
        return this.addListener(RoleDeleteListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChangeModeratorsOnlyChannelListener(ServerChangeModeratorsOnlyChannelListener listener) {
        return this.addListener(ServerChangeModeratorsOnlyChannelListener.class, listener);
    }

    default public DiscordApiBuilder addServerChangeModeratorsOnlyChannelListener(Supplier<ServerChangeModeratorsOnlyChannelListener> listenerSupplier) {
        return this.addListener(ServerChangeModeratorsOnlyChannelListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChangeModeratorsOnlyChannelListener(Function<DiscordApi, ServerChangeModeratorsOnlyChannelListener> listenerFunction) {
        return this.addListener(ServerChangeModeratorsOnlyChannelListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChangeNsfwLevelListener(ServerChangeNsfwLevelListener listener) {
        return this.addListener(ServerChangeNsfwLevelListener.class, listener);
    }

    default public DiscordApiBuilder addServerChangeNsfwLevelListener(Supplier<ServerChangeNsfwLevelListener> listenerSupplier) {
        return this.addListener(ServerChangeNsfwLevelListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChangeNsfwLevelListener(Function<DiscordApi, ServerChangeNsfwLevelListener> listenerFunction) {
        return this.addListener(ServerChangeNsfwLevelListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChannelChangePositionListener(ServerChannelChangePositionListener listener) {
        return this.addListener(ServerChannelChangePositionListener.class, listener);
    }

    default public DiscordApiBuilder addServerChannelChangePositionListener(Supplier<ServerChannelChangePositionListener> listenerSupplier) {
        return this.addListener(ServerChannelChangePositionListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChannelChangePositionListener(Function<DiscordApi, ServerChannelChangePositionListener> listenerFunction) {
        return this.addListener(ServerChannelChangePositionListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerThreadListSyncListener(ServerThreadListSyncListener listener) {
        return this.addListener(ServerThreadListSyncListener.class, listener);
    }

    default public DiscordApiBuilder addServerThreadListSyncListener(Supplier<ServerThreadListSyncListener> listenerSupplier) {
        return this.addListener(ServerThreadListSyncListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerThreadListSyncListener(Function<DiscordApi, ServerThreadListSyncListener> listenerFunction) {
        return this.addListener(ServerThreadListSyncListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerThreadChannelUpdateListener(ServerThreadChannelUpdateListener listener) {
        return this.addListener(ServerThreadChannelUpdateListener.class, listener);
    }

    default public DiscordApiBuilder addServerThreadChannelUpdateListener(Supplier<ServerThreadChannelUpdateListener> listenerSupplier) {
        return this.addListener(ServerThreadChannelUpdateListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerThreadChannelUpdateListener(Function<DiscordApi, ServerThreadChannelUpdateListener> listenerFunction) {
        return this.addListener(ServerThreadChannelUpdateListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerThreadChannelMembersUpdateListener(ServerThreadChannelMembersUpdateListener listener) {
        return this.addListener(ServerThreadChannelMembersUpdateListener.class, listener);
    }

    default public DiscordApiBuilder addServerThreadChannelMembersUpdateListener(Supplier<ServerThreadChannelMembersUpdateListener> listenerSupplier) {
        return this.addListener(ServerThreadChannelMembersUpdateListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerThreadChannelMembersUpdateListener(Function<DiscordApi, ServerThreadChannelMembersUpdateListener> listenerFunction) {
        return this.addListener(ServerThreadChannelMembersUpdateListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerThreadChannelCreateListener(ServerThreadChannelCreateListener listener) {
        return this.addListener(ServerThreadChannelCreateListener.class, listener);
    }

    default public DiscordApiBuilder addServerThreadChannelCreateListener(Supplier<ServerThreadChannelCreateListener> listenerSupplier) {
        return this.addListener(ServerThreadChannelCreateListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerThreadChannelCreateListener(Function<DiscordApi, ServerThreadChannelCreateListener> listenerFunction) {
        return this.addListener(ServerThreadChannelCreateListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerThreadChannelDeleteListener(ServerThreadChannelDeleteListener listener) {
        return this.addListener(ServerThreadChannelDeleteListener.class, listener);
    }

    default public DiscordApiBuilder addServerThreadChannelDeleteListener(Supplier<ServerThreadChannelDeleteListener> listenerSupplier) {
        return this.addListener(ServerThreadChannelDeleteListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerThreadChannelDeleteListener(Function<DiscordApi, ServerThreadChannelDeleteListener> listenerFunction) {
        return this.addListener(ServerThreadChannelDeleteListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addWebhooksUpdateListener(WebhooksUpdateListener listener) {
        return this.addListener(WebhooksUpdateListener.class, listener);
    }

    default public DiscordApiBuilder addWebhooksUpdateListener(Supplier<WebhooksUpdateListener> listenerSupplier) {
        return this.addListener(WebhooksUpdateListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addWebhooksUpdateListener(Function<DiscordApi, WebhooksUpdateListener> listenerFunction) {
        return this.addListener(WebhooksUpdateListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerTextChannelChangeDefaultAutoArchiveDurationListener(ServerTextChannelChangeDefaultAutoArchiveDurationListener listener) {
        return this.addListener(ServerTextChannelChangeDefaultAutoArchiveDurationListener.class, listener);
    }

    default public DiscordApiBuilder addServerTextChannelChangeDefaultAutoArchiveDurationListener(Supplier<ServerTextChannelChangeDefaultAutoArchiveDurationListener> listenerSupplier) {
        return this.addListener(ServerTextChannelChangeDefaultAutoArchiveDurationListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerTextChannelChangeDefaultAutoArchiveDurationListener(Function<DiscordApi, ServerTextChannelChangeDefaultAutoArchiveDurationListener> listenerFunction) {
        return this.addListener(ServerTextChannelChangeDefaultAutoArchiveDurationListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerTextChannelChangeSlowmodeListener(ServerTextChannelChangeSlowmodeListener listener) {
        return this.addListener(ServerTextChannelChangeSlowmodeListener.class, listener);
    }

    default public DiscordApiBuilder addServerTextChannelChangeSlowmodeListener(Supplier<ServerTextChannelChangeSlowmodeListener> listenerSupplier) {
        return this.addListener(ServerTextChannelChangeSlowmodeListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerTextChannelChangeSlowmodeListener(Function<DiscordApi, ServerTextChannelChangeSlowmodeListener> listenerFunction) {
        return this.addListener(ServerTextChannelChangeSlowmodeListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerTextChannelChangeTopicListener(ServerTextChannelChangeTopicListener listener) {
        return this.addListener(ServerTextChannelChangeTopicListener.class, listener);
    }

    default public DiscordApiBuilder addServerTextChannelChangeTopicListener(Supplier<ServerTextChannelChangeTopicListener> listenerSupplier) {
        return this.addListener(ServerTextChannelChangeTopicListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerTextChannelChangeTopicListener(Function<DiscordApi, ServerTextChannelChangeTopicListener> listenerFunction) {
        return this.addListener(ServerTextChannelChangeTopicListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChannelChangeOverwrittenPermissionsListener(ServerChannelChangeOverwrittenPermissionsListener listener) {
        return this.addListener(ServerChannelChangeOverwrittenPermissionsListener.class, listener);
    }

    default public DiscordApiBuilder addServerChannelChangeOverwrittenPermissionsListener(Supplier<ServerChannelChangeOverwrittenPermissionsListener> listenerSupplier) {
        return this.addListener(ServerChannelChangeOverwrittenPermissionsListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChannelChangeOverwrittenPermissionsListener(Function<DiscordApi, ServerChannelChangeOverwrittenPermissionsListener> listenerFunction) {
        return this.addListener(ServerChannelChangeOverwrittenPermissionsListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChannelInviteDeleteListener(ServerChannelInviteDeleteListener listener) {
        return this.addListener(ServerChannelInviteDeleteListener.class, listener);
    }

    default public DiscordApiBuilder addServerChannelInviteDeleteListener(Supplier<ServerChannelInviteDeleteListener> listenerSupplier) {
        return this.addListener(ServerChannelInviteDeleteListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChannelInviteDeleteListener(Function<DiscordApi, ServerChannelInviteDeleteListener> listenerFunction) {
        return this.addListener(ServerChannelInviteDeleteListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChannelInviteCreateListener(ServerChannelInviteCreateListener listener) {
        return this.addListener(ServerChannelInviteCreateListener.class, listener);
    }

    default public DiscordApiBuilder addServerChannelInviteCreateListener(Supplier<ServerChannelInviteCreateListener> listenerSupplier) {
        return this.addListener(ServerChannelInviteCreateListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChannelInviteCreateListener(Function<DiscordApi, ServerChannelInviteCreateListener> listenerFunction) {
        return this.addListener(ServerChannelInviteCreateListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChannelChangeNsfwFlagListener(ServerChannelChangeNsfwFlagListener listener) {
        return this.addListener(ServerChannelChangeNsfwFlagListener.class, listener);
    }

    default public DiscordApiBuilder addServerChannelChangeNsfwFlagListener(Supplier<ServerChannelChangeNsfwFlagListener> listenerSupplier) {
        return this.addListener(ServerChannelChangeNsfwFlagListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChannelChangeNsfwFlagListener(Function<DiscordApi, ServerChannelChangeNsfwFlagListener> listenerFunction) {
        return this.addListener(ServerChannelChangeNsfwFlagListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChannelDeleteListener(ServerChannelDeleteListener listener) {
        return this.addListener(ServerChannelDeleteListener.class, listener);
    }

    default public DiscordApiBuilder addServerChannelDeleteListener(Supplier<ServerChannelDeleteListener> listenerSupplier) {
        return this.addListener(ServerChannelDeleteListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChannelDeleteListener(Function<DiscordApi, ServerChannelDeleteListener> listenerFunction) {
        return this.addListener(ServerChannelDeleteListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChannelCreateListener(ServerChannelCreateListener listener) {
        return this.addListener(ServerChannelCreateListener.class, listener);
    }

    default public DiscordApiBuilder addServerChannelCreateListener(Supplier<ServerChannelCreateListener> listenerSupplier) {
        return this.addListener(ServerChannelCreateListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChannelCreateListener(Function<DiscordApi, ServerChannelCreateListener> listenerFunction) {
        return this.addListener(ServerChannelCreateListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerStageVoiceChannelChangeTopicListener(ServerStageVoiceChannelChangeTopicListener listener) {
        return this.addListener(ServerStageVoiceChannelChangeTopicListener.class, listener);
    }

    default public DiscordApiBuilder addServerStageVoiceChannelChangeTopicListener(Supplier<ServerStageVoiceChannelChangeTopicListener> listenerSupplier) {
        return this.addListener(ServerStageVoiceChannelChangeTopicListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerStageVoiceChannelChangeTopicListener(Function<DiscordApi, ServerStageVoiceChannelChangeTopicListener> listenerFunction) {
        return this.addListener(ServerStageVoiceChannelChangeTopicListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerVoiceChannelChangeBitrateListener(ServerVoiceChannelChangeBitrateListener listener) {
        return this.addListener(ServerVoiceChannelChangeBitrateListener.class, listener);
    }

    default public DiscordApiBuilder addServerVoiceChannelChangeBitrateListener(Supplier<ServerVoiceChannelChangeBitrateListener> listenerSupplier) {
        return this.addListener(ServerVoiceChannelChangeBitrateListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerVoiceChannelChangeBitrateListener(Function<DiscordApi, ServerVoiceChannelChangeBitrateListener> listenerFunction) {
        return this.addListener(ServerVoiceChannelChangeBitrateListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerVoiceChannelChangeUserLimitListener(ServerVoiceChannelChangeUserLimitListener listener) {
        return this.addListener(ServerVoiceChannelChangeUserLimitListener.class, listener);
    }

    default public DiscordApiBuilder addServerVoiceChannelChangeUserLimitListener(Supplier<ServerVoiceChannelChangeUserLimitListener> listenerSupplier) {
        return this.addListener(ServerVoiceChannelChangeUserLimitListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerVoiceChannelChangeUserLimitListener(Function<DiscordApi, ServerVoiceChannelChangeUserLimitListener> listenerFunction) {
        return this.addListener(ServerVoiceChannelChangeUserLimitListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerVoiceChannelMemberLeaveListener(ServerVoiceChannelMemberLeaveListener listener) {
        return this.addListener(ServerVoiceChannelMemberLeaveListener.class, listener);
    }

    default public DiscordApiBuilder addServerVoiceChannelMemberLeaveListener(Supplier<ServerVoiceChannelMemberLeaveListener> listenerSupplier) {
        return this.addListener(ServerVoiceChannelMemberLeaveListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerVoiceChannelMemberLeaveListener(Function<DiscordApi, ServerVoiceChannelMemberLeaveListener> listenerFunction) {
        return this.addListener(ServerVoiceChannelMemberLeaveListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerVoiceChannelChangeNsfwListener(ServerVoiceChannelChangeNsfwListener listener) {
        return this.addListener(ServerVoiceChannelChangeNsfwListener.class, listener);
    }

    default public DiscordApiBuilder addServerVoiceChannelChangeNsfwListener(Supplier<ServerVoiceChannelChangeNsfwListener> listenerSupplier) {
        return this.addListener(ServerVoiceChannelChangeNsfwListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerVoiceChannelChangeNsfwListener(Function<DiscordApi, ServerVoiceChannelChangeNsfwListener> listenerFunction) {
        return this.addListener(ServerVoiceChannelChangeNsfwListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerVoiceChannelMemberJoinListener(ServerVoiceChannelMemberJoinListener listener) {
        return this.addListener(ServerVoiceChannelMemberJoinListener.class, listener);
    }

    default public DiscordApiBuilder addServerVoiceChannelMemberJoinListener(Supplier<ServerVoiceChannelMemberJoinListener> listenerSupplier) {
        return this.addListener(ServerVoiceChannelMemberJoinListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerVoiceChannelMemberJoinListener(Function<DiscordApi, ServerVoiceChannelMemberJoinListener> listenerFunction) {
        return this.addListener(ServerVoiceChannelMemberJoinListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addServerChannelChangeNameListener(ServerChannelChangeNameListener listener) {
        return this.addListener(ServerChannelChangeNameListener.class, listener);
    }

    default public DiscordApiBuilder addServerChannelChangeNameListener(Supplier<ServerChannelChangeNameListener> listenerSupplier) {
        return this.addListener(ServerChannelChangeNameListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addServerChannelChangeNameListener(Function<DiscordApi, ServerChannelChangeNameListener> listenerFunction) {
        return this.addListener(ServerChannelChangeNameListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addPrivateChannelDeleteListener(PrivateChannelDeleteListener listener) {
        return this.addListener(PrivateChannelDeleteListener.class, listener);
    }

    default public DiscordApiBuilder addPrivateChannelDeleteListener(Supplier<PrivateChannelDeleteListener> listenerSupplier) {
        return this.addListener(PrivateChannelDeleteListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addPrivateChannelDeleteListener(Function<DiscordApi, PrivateChannelDeleteListener> listenerFunction) {
        return this.addListener(PrivateChannelDeleteListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addPrivateChannelCreateListener(PrivateChannelCreateListener listener) {
        return this.addListener(PrivateChannelCreateListener.class, listener);
    }

    default public DiscordApiBuilder addPrivateChannelCreateListener(Supplier<PrivateChannelCreateListener> listenerSupplier) {
        return this.addListener(PrivateChannelCreateListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addPrivateChannelCreateListener(Function<DiscordApi, PrivateChannelCreateListener> listenerFunction) {
        return this.addListener(PrivateChannelCreateListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addAudioSourceFinishedListener(AudioSourceFinishedListener listener) {
        return this.addListener(AudioSourceFinishedListener.class, listener);
    }

    default public DiscordApiBuilder addAudioSourceFinishedListener(Supplier<AudioSourceFinishedListener> listenerSupplier) {
        return this.addListener(AudioSourceFinishedListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addAudioSourceFinishedListener(Function<DiscordApi, AudioSourceFinishedListener> listenerFunction) {
        return this.addListener(AudioSourceFinishedListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addUserChangeDeafenedListener(UserChangeDeafenedListener listener) {
        return this.addListener(UserChangeDeafenedListener.class, listener);
    }

    default public DiscordApiBuilder addUserChangeDeafenedListener(Supplier<UserChangeDeafenedListener> listenerSupplier) {
        return this.addListener(UserChangeDeafenedListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addUserChangeDeafenedListener(Function<DiscordApi, UserChangeDeafenedListener> listenerFunction) {
        return this.addListener(UserChangeDeafenedListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addUserChangeNicknameListener(UserChangeNicknameListener listener) {
        return this.addListener(UserChangeNicknameListener.class, listener);
    }

    default public DiscordApiBuilder addUserChangeNicknameListener(Supplier<UserChangeNicknameListener> listenerSupplier) {
        return this.addListener(UserChangeNicknameListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addUserChangeNicknameListener(Function<DiscordApi, UserChangeNicknameListener> listenerFunction) {
        return this.addListener(UserChangeNicknameListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addUserChangePendingListener(UserChangePendingListener listener) {
        return this.addListener(UserChangePendingListener.class, listener);
    }

    default public DiscordApiBuilder addUserChangePendingListener(Supplier<UserChangePendingListener> listenerSupplier) {
        return this.addListener(UserChangePendingListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addUserChangePendingListener(Function<DiscordApi, UserChangePendingListener> listenerFunction) {
        return this.addListener(UserChangePendingListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addUserStartTypingListener(UserStartTypingListener listener) {
        return this.addListener(UserStartTypingListener.class, listener);
    }

    default public DiscordApiBuilder addUserStartTypingListener(Supplier<UserStartTypingListener> listenerSupplier) {
        return this.addListener(UserStartTypingListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addUserStartTypingListener(Function<DiscordApi, UserStartTypingListener> listenerFunction) {
        return this.addListener(UserStartTypingListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addUserChangeDiscriminatorListener(UserChangeDiscriminatorListener listener) {
        return this.addListener(UserChangeDiscriminatorListener.class, listener);
    }

    default public DiscordApiBuilder addUserChangeDiscriminatorListener(Supplier<UserChangeDiscriminatorListener> listenerSupplier) {
        return this.addListener(UserChangeDiscriminatorListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addUserChangeDiscriminatorListener(Function<DiscordApi, UserChangeDiscriminatorListener> listenerFunction) {
        return this.addListener(UserChangeDiscriminatorListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addUserChangeStatusListener(UserChangeStatusListener listener) {
        return this.addListener(UserChangeStatusListener.class, listener);
    }

    default public DiscordApiBuilder addUserChangeStatusListener(Supplier<UserChangeStatusListener> listenerSupplier) {
        return this.addListener(UserChangeStatusListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addUserChangeStatusListener(Function<DiscordApi, UserChangeStatusListener> listenerFunction) {
        return this.addListener(UserChangeStatusListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addUserChangeServerAvatarListener(UserChangeServerAvatarListener listener) {
        return this.addListener(UserChangeServerAvatarListener.class, listener);
    }

    default public DiscordApiBuilder addUserChangeServerAvatarListener(Supplier<UserChangeServerAvatarListener> listenerSupplier) {
        return this.addListener(UserChangeServerAvatarListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addUserChangeServerAvatarListener(Function<DiscordApi, UserChangeServerAvatarListener> listenerFunction) {
        return this.addListener(UserChangeServerAvatarListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addUserChangeSelfMutedListener(UserChangeSelfMutedListener listener) {
        return this.addListener(UserChangeSelfMutedListener.class, listener);
    }

    default public DiscordApiBuilder addUserChangeSelfMutedListener(Supplier<UserChangeSelfMutedListener> listenerSupplier) {
        return this.addListener(UserChangeSelfMutedListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addUserChangeSelfMutedListener(Function<DiscordApi, UserChangeSelfMutedListener> listenerFunction) {
        return this.addListener(UserChangeSelfMutedListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addUserChangeNameListener(UserChangeNameListener listener) {
        return this.addListener(UserChangeNameListener.class, listener);
    }

    default public DiscordApiBuilder addUserChangeNameListener(Supplier<UserChangeNameListener> listenerSupplier) {
        return this.addListener(UserChangeNameListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addUserChangeNameListener(Function<DiscordApi, UserChangeNameListener> listenerFunction) {
        return this.addListener(UserChangeNameListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addUserChangeTimeoutListener(UserChangeTimeoutListener listener) {
        return this.addListener(UserChangeTimeoutListener.class, listener);
    }

    default public DiscordApiBuilder addUserChangeTimeoutListener(Supplier<UserChangeTimeoutListener> listenerSupplier) {
        return this.addListener(UserChangeTimeoutListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addUserChangeTimeoutListener(Function<DiscordApi, UserChangeTimeoutListener> listenerFunction) {
        return this.addListener(UserChangeTimeoutListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addUserChangeAvatarListener(UserChangeAvatarListener listener) {
        return this.addListener(UserChangeAvatarListener.class, listener);
    }

    default public DiscordApiBuilder addUserChangeAvatarListener(Supplier<UserChangeAvatarListener> listenerSupplier) {
        return this.addListener(UserChangeAvatarListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addUserChangeAvatarListener(Function<DiscordApi, UserChangeAvatarListener> listenerFunction) {
        return this.addListener(UserChangeAvatarListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addUserChangeSelfDeafenedListener(UserChangeSelfDeafenedListener listener) {
        return this.addListener(UserChangeSelfDeafenedListener.class, listener);
    }

    default public DiscordApiBuilder addUserChangeSelfDeafenedListener(Supplier<UserChangeSelfDeafenedListener> listenerSupplier) {
        return this.addListener(UserChangeSelfDeafenedListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addUserChangeSelfDeafenedListener(Function<DiscordApi, UserChangeSelfDeafenedListener> listenerFunction) {
        return this.addListener(UserChangeSelfDeafenedListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addUserChangeMutedListener(UserChangeMutedListener listener) {
        return this.addListener(UserChangeMutedListener.class, listener);
    }

    default public DiscordApiBuilder addUserChangeMutedListener(Supplier<UserChangeMutedListener> listenerSupplier) {
        return this.addListener(UserChangeMutedListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addUserChangeMutedListener(Function<DiscordApi, UserChangeMutedListener> listenerFunction) {
        return this.addListener(UserChangeMutedListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addUserChangeActivityListener(UserChangeActivityListener listener) {
        return this.addListener(UserChangeActivityListener.class, listener);
    }

    default public DiscordApiBuilder addUserChangeActivityListener(Supplier<UserChangeActivityListener> listenerSupplier) {
        return this.addListener(UserChangeActivityListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addUserChangeActivityListener(Function<DiscordApi, UserChangeActivityListener> listenerFunction) {
        return this.addListener(UserChangeActivityListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addMessageEditListener(MessageEditListener listener) {
        return this.addListener(MessageEditListener.class, listener);
    }

    default public DiscordApiBuilder addMessageEditListener(Supplier<MessageEditListener> listenerSupplier) {
        return this.addListener(MessageEditListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addMessageEditListener(Function<DiscordApi, MessageEditListener> listenerFunction) {
        return this.addListener(MessageEditListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addChannelPinsUpdateListener(ChannelPinsUpdateListener listener) {
        return this.addListener(ChannelPinsUpdateListener.class, listener);
    }

    default public DiscordApiBuilder addChannelPinsUpdateListener(Supplier<ChannelPinsUpdateListener> listenerSupplier) {
        return this.addListener(ChannelPinsUpdateListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addChannelPinsUpdateListener(Function<DiscordApi, ChannelPinsUpdateListener> listenerFunction) {
        return this.addListener(ChannelPinsUpdateListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addReactionRemoveListener(ReactionRemoveListener listener) {
        return this.addListener(ReactionRemoveListener.class, listener);
    }

    default public DiscordApiBuilder addReactionRemoveListener(Supplier<ReactionRemoveListener> listenerSupplier) {
        return this.addListener(ReactionRemoveListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addReactionRemoveListener(Function<DiscordApi, ReactionRemoveListener> listenerFunction) {
        return this.addListener(ReactionRemoveListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addReactionAddListener(ReactionAddListener listener) {
        return this.addListener(ReactionAddListener.class, listener);
    }

    default public DiscordApiBuilder addReactionAddListener(Supplier<ReactionAddListener> listenerSupplier) {
        return this.addListener(ReactionAddListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addReactionAddListener(Function<DiscordApi, ReactionAddListener> listenerFunction) {
        return this.addListener(ReactionAddListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addReactionRemoveAllListener(ReactionRemoveAllListener listener) {
        return this.addListener(ReactionRemoveAllListener.class, listener);
    }

    default public DiscordApiBuilder addReactionRemoveAllListener(Supplier<ReactionRemoveAllListener> listenerSupplier) {
        return this.addListener(ReactionRemoveAllListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addReactionRemoveAllListener(Function<DiscordApi, ReactionRemoveAllListener> listenerFunction) {
        return this.addListener(ReactionRemoveAllListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addMessageCreateListener(MessageCreateListener listener) {
        return this.addListener(MessageCreateListener.class, listener);
    }

    default public DiscordApiBuilder addMessageCreateListener(Supplier<MessageCreateListener> listenerSupplier) {
        return this.addListener(MessageCreateListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addMessageCreateListener(Function<DiscordApi, MessageCreateListener> listenerFunction) {
        return this.addListener(MessageCreateListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addCachedMessageUnpinListener(CachedMessageUnpinListener listener) {
        return this.addListener(CachedMessageUnpinListener.class, listener);
    }

    default public DiscordApiBuilder addCachedMessageUnpinListener(Supplier<CachedMessageUnpinListener> listenerSupplier) {
        return this.addListener(CachedMessageUnpinListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addCachedMessageUnpinListener(Function<DiscordApi, CachedMessageUnpinListener> listenerFunction) {
        return this.addListener(CachedMessageUnpinListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addCachedMessagePinListener(CachedMessagePinListener listener) {
        return this.addListener(CachedMessagePinListener.class, listener);
    }

    default public DiscordApiBuilder addCachedMessagePinListener(Supplier<CachedMessagePinListener> listenerSupplier) {
        return this.addListener(CachedMessagePinListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addCachedMessagePinListener(Function<DiscordApi, CachedMessagePinListener> listenerFunction) {
        return this.addListener(CachedMessagePinListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addMessageReplyListener(MessageReplyListener listener) {
        return this.addListener(MessageReplyListener.class, listener);
    }

    default public DiscordApiBuilder addMessageReplyListener(Supplier<MessageReplyListener> listenerSupplier) {
        return this.addListener(MessageReplyListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addMessageReplyListener(Function<DiscordApi, MessageReplyListener> listenerFunction) {
        return this.addListener(MessageReplyListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addMessageDeleteListener(MessageDeleteListener listener) {
        return this.addListener(MessageDeleteListener.class, listener);
    }

    default public DiscordApiBuilder addMessageDeleteListener(Supplier<MessageDeleteListener> listenerSupplier) {
        return this.addListener(MessageDeleteListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addMessageDeleteListener(Function<DiscordApi, MessageDeleteListener> listenerFunction) {
        return this.addListener(MessageDeleteListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addResumeListener(ResumeListener listener) {
        return this.addListener(ResumeListener.class, listener);
    }

    default public DiscordApiBuilder addResumeListener(Supplier<ResumeListener> listenerSupplier) {
        return this.addListener(ResumeListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addResumeListener(Function<DiscordApi, ResumeListener> listenerFunction) {
        return this.addListener(ResumeListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addLostConnectionListener(LostConnectionListener listener) {
        return this.addListener(LostConnectionListener.class, listener);
    }

    default public DiscordApiBuilder addLostConnectionListener(Supplier<LostConnectionListener> listenerSupplier) {
        return this.addListener(LostConnectionListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addLostConnectionListener(Function<DiscordApi, LostConnectionListener> listenerFunction) {
        return this.addListener(LostConnectionListener.class, listenerFunction);
    }

    default public DiscordApiBuilder addReconnectListener(ReconnectListener listener) {
        return this.addListener(ReconnectListener.class, listener);
    }

    default public DiscordApiBuilder addReconnectListener(Supplier<ReconnectListener> listenerSupplier) {
        return this.addListener(ReconnectListener.class, listenerSupplier);
    }

    default public DiscordApiBuilder addReconnectListener(Function<DiscordApi, ReconnectListener> listenerFunction) {
        return this.addListener(ReconnectListener.class, listenerFunction);
    }

    public <T extends GloballyAttachableListener> DiscordApiBuilder addListener(Class<T> var1, T var2);

    public DiscordApiBuilder addListener(GloballyAttachableListener var1);

    public <T extends GloballyAttachableListener> DiscordApiBuilder addListener(Class<T> var1, Supplier<T> var2);

    public DiscordApiBuilder addListener(Supplier<GloballyAttachableListener> var1);

    public <T extends GloballyAttachableListener> DiscordApiBuilder addListener(Class<T> var1, Function<DiscordApi, T> var2);

    public DiscordApiBuilder addListener(Function<DiscordApi, GloballyAttachableListener> var1);

    public DiscordApiBuilder removeListener(GloballyAttachableListener var1);

    public <T extends GloballyAttachableListener> DiscordApiBuilder removeListener(Class<T> var1, T var2);

    public DiscordApiBuilder removeListenerSupplier(Supplier<GloballyAttachableListener> var1);

    public <T extends GloballyAttachableListener> DiscordApiBuilder removeListenerSupplier(Class<T> var1, Supplier<T> var2);

    public DiscordApiBuilder removeListenerFunction(Function<DiscordApi, GloballyAttachableListener> var1);

    public <T extends GloballyAttachableListener> DiscordApiBuilder removeListenerFunction(Class<T> var1, Function<DiscordApi, T> var2);
}

