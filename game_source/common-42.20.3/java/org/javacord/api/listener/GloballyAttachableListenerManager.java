/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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
import org.javacord.api.util.event.ListenerManager;

public interface GloballyAttachableListenerManager {
    default public ListenerManager<InteractionCreateListener> addInteractionCreateListener(InteractionCreateListener listener) {
        return this.addListener(InteractionCreateListener.class, listener);
    }

    default public List<InteractionCreateListener> getInteractionCreateListeners() {
        return this.getListeners(InteractionCreateListener.class);
    }

    default public ListenerManager<SlashCommandCreateListener> addSlashCommandCreateListener(SlashCommandCreateListener listener) {
        return this.addListener(SlashCommandCreateListener.class, listener);
    }

    default public List<SlashCommandCreateListener> getSlashCommandCreateListeners() {
        return this.getListeners(SlashCommandCreateListener.class);
    }

    default public ListenerManager<AutocompleteCreateListener> addAutocompleteCreateListener(AutocompleteCreateListener listener) {
        return this.addListener(AutocompleteCreateListener.class, listener);
    }

    default public List<AutocompleteCreateListener> getAutocompleteCreateListeners() {
        return this.getListeners(AutocompleteCreateListener.class);
    }

    default public ListenerManager<ModalSubmitListener> addModalSubmitListener(ModalSubmitListener listener) {
        return this.addListener(ModalSubmitListener.class, listener);
    }

    default public List<ModalSubmitListener> getModalSubmitListeners() {
        return this.getListeners(ModalSubmitListener.class);
    }

    default public ListenerManager<MessageContextMenuCommandListener> addMessageContextMenuCommandListener(MessageContextMenuCommandListener listener) {
        return this.addListener(MessageContextMenuCommandListener.class, listener);
    }

    default public List<MessageContextMenuCommandListener> getMessageContextMenuCommandListeners() {
        return this.getListeners(MessageContextMenuCommandListener.class);
    }

    default public ListenerManager<MessageComponentCreateListener> addMessageComponentCreateListener(MessageComponentCreateListener listener) {
        return this.addListener(MessageComponentCreateListener.class, listener);
    }

    default public List<MessageComponentCreateListener> getMessageComponentCreateListeners() {
        return this.getListeners(MessageComponentCreateListener.class);
    }

    default public ListenerManager<UserContextMenuCommandListener> addUserContextMenuCommandListener(UserContextMenuCommandListener listener) {
        return this.addListener(UserContextMenuCommandListener.class, listener);
    }

    default public List<UserContextMenuCommandListener> getUserContextMenuCommandListeners() {
        return this.getListeners(UserContextMenuCommandListener.class);
    }

    default public ListenerManager<SelectMenuChooseListener> addSelectMenuChooseListener(SelectMenuChooseListener listener) {
        return this.addListener(SelectMenuChooseListener.class, listener);
    }

    default public List<SelectMenuChooseListener> getSelectMenuChooseListeners() {
        return this.getListeners(SelectMenuChooseListener.class);
    }

    default public ListenerManager<ButtonClickListener> addButtonClickListener(ButtonClickListener listener) {
        return this.addListener(ButtonClickListener.class, listener);
    }

    default public List<ButtonClickListener> getButtonClickListeners() {
        return this.getListeners(ButtonClickListener.class);
    }

    default public ListenerManager<ServerChangeIconListener> addServerChangeIconListener(ServerChangeIconListener listener) {
        return this.addListener(ServerChangeIconListener.class, listener);
    }

    default public List<ServerChangeIconListener> getServerChangeIconListeners() {
        return this.getListeners(ServerChangeIconListener.class);
    }

    default public ListenerManager<ServerChangeNameListener> addServerChangeNameListener(ServerChangeNameListener listener) {
        return this.addListener(ServerChangeNameListener.class, listener);
    }

    default public List<ServerChangeNameListener> getServerChangeNameListeners() {
        return this.getListeners(ServerChangeNameListener.class);
    }

    default public ListenerManager<ServerThreadChannelChangeLastMessageIdListener> addServerThreadChannelChangeLastMessageIdListener(ServerThreadChannelChangeLastMessageIdListener listener) {
        return this.addListener(ServerThreadChannelChangeLastMessageIdListener.class, listener);
    }

    default public List<ServerThreadChannelChangeLastMessageIdListener> getServerThreadChannelChangeLastMessageIdListeners() {
        return this.getListeners(ServerThreadChannelChangeLastMessageIdListener.class);
    }

    default public ListenerManager<ServerThreadChannelChangeArchivedListener> addServerThreadChannelChangeArchivedListener(ServerThreadChannelChangeArchivedListener listener) {
        return this.addListener(ServerThreadChannelChangeArchivedListener.class, listener);
    }

    default public List<ServerThreadChannelChangeArchivedListener> getServerThreadChannelChangeArchivedListeners() {
        return this.getListeners(ServerThreadChannelChangeArchivedListener.class);
    }

    default public ListenerManager<ServerThreadChannelChangeMemberCountListener> addServerThreadChannelChangeMemberCountListener(ServerThreadChannelChangeMemberCountListener listener) {
        return this.addListener(ServerThreadChannelChangeMemberCountListener.class, listener);
    }

    default public List<ServerThreadChannelChangeMemberCountListener> getServerThreadChannelChangeMemberCountListeners() {
        return this.getListeners(ServerThreadChannelChangeMemberCountListener.class);
    }

    default public ListenerManager<ServerPrivateThreadJoinListener> addServerPrivateThreadJoinListener(ServerPrivateThreadJoinListener listener) {
        return this.addListener(ServerPrivateThreadJoinListener.class, listener);
    }

    default public List<ServerPrivateThreadJoinListener> getServerPrivateThreadJoinListeners() {
        return this.getListeners(ServerPrivateThreadJoinListener.class);
    }

    default public ListenerManager<ServerThreadChannelChangeInvitableListener> addServerThreadChannelChangeInvitableListener(ServerThreadChannelChangeInvitableListener listener) {
        return this.addListener(ServerThreadChannelChangeInvitableListener.class, listener);
    }

    default public List<ServerThreadChannelChangeInvitableListener> getServerThreadChannelChangeInvitableListeners() {
        return this.getListeners(ServerThreadChannelChangeInvitableListener.class);
    }

    default public ListenerManager<ServerThreadChannelChangeAutoArchiveDurationListener> addServerThreadChannelChangeAutoArchiveDurationListener(ServerThreadChannelChangeAutoArchiveDurationListener listener) {
        return this.addListener(ServerThreadChannelChangeAutoArchiveDurationListener.class, listener);
    }

    default public List<ServerThreadChannelChangeAutoArchiveDurationListener> getServerThreadChannelChangeAutoArchiveDurationListeners() {
        return this.getListeners(ServerThreadChannelChangeAutoArchiveDurationListener.class);
    }

    default public ListenerManager<ServerThreadChannelChangeRateLimitPerUserListener> addServerThreadChannelChangeRateLimitPerUserListener(ServerThreadChannelChangeRateLimitPerUserListener listener) {
        return this.addListener(ServerThreadChannelChangeRateLimitPerUserListener.class, listener);
    }

    default public List<ServerThreadChannelChangeRateLimitPerUserListener> getServerThreadChannelChangeRateLimitPerUserListeners() {
        return this.getListeners(ServerThreadChannelChangeRateLimitPerUserListener.class);
    }

    default public ListenerManager<ServerThreadChannelChangeLockedListener> addServerThreadChannelChangeLockedListener(ServerThreadChannelChangeLockedListener listener) {
        return this.addListener(ServerThreadChannelChangeLockedListener.class, listener);
    }

    default public List<ServerThreadChannelChangeLockedListener> getServerThreadChannelChangeLockedListeners() {
        return this.getListeners(ServerThreadChannelChangeLockedListener.class);
    }

    default public ListenerManager<ServerThreadChannelChangeArchiveTimestampListener> addServerThreadChannelChangeArchiveTimestampListener(ServerThreadChannelChangeArchiveTimestampListener listener) {
        return this.addListener(ServerThreadChannelChangeArchiveTimestampListener.class, listener);
    }

    default public List<ServerThreadChannelChangeArchiveTimestampListener> getServerThreadChannelChangeArchiveTimestampListeners() {
        return this.getListeners(ServerThreadChannelChangeArchiveTimestampListener.class);
    }

    default public ListenerManager<ServerThreadChannelChangeTotalMessageSentListener> addServerThreadChannelChangeTotalMessageSentListener(ServerThreadChannelChangeTotalMessageSentListener listener) {
        return this.addListener(ServerThreadChannelChangeTotalMessageSentListener.class, listener);
    }

    default public List<ServerThreadChannelChangeTotalMessageSentListener> getServerThreadChannelChangeTotalMessageSentListeners() {
        return this.getListeners(ServerThreadChannelChangeTotalMessageSentListener.class);
    }

    default public ListenerManager<ServerThreadChannelChangeMessageCountListener> addServerThreadChannelChangeMessageCountListener(ServerThreadChannelChangeMessageCountListener listener) {
        return this.addListener(ServerThreadChannelChangeMessageCountListener.class, listener);
    }

    default public List<ServerThreadChannelChangeMessageCountListener> getServerThreadChannelChangeMessageCountListeners() {
        return this.getListeners(ServerThreadChannelChangeMessageCountListener.class);
    }

    default public ListenerManager<ServerChangeAfkTimeoutListener> addServerChangeAfkTimeoutListener(ServerChangeAfkTimeoutListener listener) {
        return this.addListener(ServerChangeAfkTimeoutListener.class, listener);
    }

    default public List<ServerChangeAfkTimeoutListener> getServerChangeAfkTimeoutListeners() {
        return this.getListeners(ServerChangeAfkTimeoutListener.class);
    }

    default public ListenerManager<StickerChangeTagsListener> addStickerChangeTagsListener(StickerChangeTagsListener listener) {
        return this.addListener(StickerChangeTagsListener.class, listener);
    }

    default public List<StickerChangeTagsListener> getStickerChangeTagsListeners() {
        return this.getListeners(StickerChangeTagsListener.class);
    }

    default public ListenerManager<StickerChangeDescriptionListener> addStickerChangeDescriptionListener(StickerChangeDescriptionListener listener) {
        return this.addListener(StickerChangeDescriptionListener.class, listener);
    }

    default public List<StickerChangeDescriptionListener> getStickerChangeDescriptionListeners() {
        return this.getListeners(StickerChangeDescriptionListener.class);
    }

    default public ListenerManager<StickerCreateListener> addStickerCreateListener(StickerCreateListener listener) {
        return this.addListener(StickerCreateListener.class, listener);
    }

    default public List<StickerCreateListener> getStickerCreateListeners() {
        return this.getListeners(StickerCreateListener.class);
    }

    default public ListenerManager<StickerChangeNameListener> addStickerChangeNameListener(StickerChangeNameListener listener) {
        return this.addListener(StickerChangeNameListener.class, listener);
    }

    default public List<StickerChangeNameListener> getStickerChangeNameListeners() {
        return this.getListeners(StickerChangeNameListener.class);
    }

    default public ListenerManager<StickerDeleteListener> addStickerDeleteListener(StickerDeleteListener listener) {
        return this.addListener(StickerDeleteListener.class, listener);
    }

    default public List<StickerDeleteListener> getStickerDeleteListeners() {
        return this.getListeners(StickerDeleteListener.class);
    }

    default public ListenerManager<ServerChangeSplashListener> addServerChangeSplashListener(ServerChangeSplashListener listener) {
        return this.addListener(ServerChangeSplashListener.class, listener);
    }

    default public List<ServerChangeSplashListener> getServerChangeSplashListeners() {
        return this.getListeners(ServerChangeSplashListener.class);
    }

    default public ListenerManager<ServerChangeAfkChannelListener> addServerChangeAfkChannelListener(ServerChangeAfkChannelListener listener) {
        return this.addListener(ServerChangeAfkChannelListener.class, listener);
    }

    default public List<ServerChangeAfkChannelListener> getServerChangeAfkChannelListeners() {
        return this.getListeners(ServerChangeAfkChannelListener.class);
    }

    default public ListenerManager<VoiceStateUpdateListener> addVoiceStateUpdateListener(VoiceStateUpdateListener listener) {
        return this.addListener(VoiceStateUpdateListener.class, listener);
    }

    default public List<VoiceStateUpdateListener> getVoiceStateUpdateListeners() {
        return this.getListeners(VoiceStateUpdateListener.class);
    }

    default public ListenerManager<ServerChangeVanityUrlCodeListener> addServerChangeVanityUrlCodeListener(ServerChangeVanityUrlCodeListener listener) {
        return this.addListener(ServerChangeVanityUrlCodeListener.class, listener);
    }

    default public List<ServerChangeVanityUrlCodeListener> getServerChangeVanityUrlCodeListeners() {
        return this.getListeners(ServerChangeVanityUrlCodeListener.class);
    }

    default public ListenerManager<ServerChangeDiscoverySplashListener> addServerChangeDiscoverySplashListener(ServerChangeDiscoverySplashListener listener) {
        return this.addListener(ServerChangeDiscoverySplashListener.class, listener);
    }

    default public List<ServerChangeDiscoverySplashListener> getServerChangeDiscoverySplashListeners() {
        return this.getListeners(ServerChangeDiscoverySplashListener.class);
    }

    default public ListenerManager<ServerJoinListener> addServerJoinListener(ServerJoinListener listener) {
        return this.addListener(ServerJoinListener.class, listener);
    }

    default public List<ServerJoinListener> getServerJoinListeners() {
        return this.getListeners(ServerJoinListener.class);
    }

    default public ListenerManager<ApplicationCommandPermissionsUpdateListener> addApplicationCommandPermissionsUpdateListener(ApplicationCommandPermissionsUpdateListener listener) {
        return this.addListener(ApplicationCommandPermissionsUpdateListener.class, listener);
    }

    default public List<ApplicationCommandPermissionsUpdateListener> getApplicationCommandPermissionsUpdateListeners() {
        return this.getListeners(ApplicationCommandPermissionsUpdateListener.class);
    }

    default public ListenerManager<ServerBecomesUnavailableListener> addServerBecomesUnavailableListener(ServerBecomesUnavailableListener listener) {
        return this.addListener(ServerBecomesUnavailableListener.class, listener);
    }

    default public List<ServerBecomesUnavailableListener> getServerBecomesUnavailableListeners() {
        return this.getListeners(ServerBecomesUnavailableListener.class);
    }

    default public ListenerManager<VoiceServerUpdateListener> addVoiceServerUpdateListener(VoiceServerUpdateListener listener) {
        return this.addListener(VoiceServerUpdateListener.class, listener);
    }

    default public List<VoiceServerUpdateListener> getVoiceServerUpdateListeners() {
        return this.getListeners(VoiceServerUpdateListener.class);
    }

    default public ListenerManager<ServerChangeDescriptionListener> addServerChangeDescriptionListener(ServerChangeDescriptionListener listener) {
        return this.addListener(ServerChangeDescriptionListener.class, listener);
    }

    default public List<ServerChangeDescriptionListener> getServerChangeDescriptionListeners() {
        return this.getListeners(ServerChangeDescriptionListener.class);
    }

    default public ListenerManager<ServerChangeVerificationLevelListener> addServerChangeVerificationLevelListener(ServerChangeVerificationLevelListener listener) {
        return this.addListener(ServerChangeVerificationLevelListener.class, listener);
    }

    default public List<ServerChangeVerificationLevelListener> getServerChangeVerificationLevelListeners() {
        return this.getListeners(ServerChangeVerificationLevelListener.class);
    }

    default public ListenerManager<ServerLeaveListener> addServerLeaveListener(ServerLeaveListener listener) {
        return this.addListener(ServerLeaveListener.class, listener);
    }

    default public List<ServerLeaveListener> getServerLeaveListeners() {
        return this.getListeners(ServerLeaveListener.class);
    }

    default public ListenerManager<ServerChangeBoostCountListener> addServerChangeBoostCountListener(ServerChangeBoostCountListener listener) {
        return this.addListener(ServerChangeBoostCountListener.class, listener);
    }

    default public List<ServerChangeBoostCountListener> getServerChangeBoostCountListeners() {
        return this.getListeners(ServerChangeBoostCountListener.class);
    }

    default public ListenerManager<ServerBecomesAvailableListener> addServerBecomesAvailableListener(ServerBecomesAvailableListener listener) {
        return this.addListener(ServerBecomesAvailableListener.class, listener);
    }

    default public List<ServerBecomesAvailableListener> getServerBecomesAvailableListeners() {
        return this.getListeners(ServerBecomesAvailableListener.class);
    }

    default public ListenerManager<ServerChangeDefaultMessageNotificationLevelListener> addServerChangeDefaultMessageNotificationLevelListener(ServerChangeDefaultMessageNotificationLevelListener listener) {
        return this.addListener(ServerChangeDefaultMessageNotificationLevelListener.class, listener);
    }

    default public List<ServerChangeDefaultMessageNotificationLevelListener> getServerChangeDefaultMessageNotificationLevelListeners() {
        return this.getListeners(ServerChangeDefaultMessageNotificationLevelListener.class);
    }

    default public ListenerManager<ServerChangeRegionListener> addServerChangeRegionListener(ServerChangeRegionListener listener) {
        return this.addListener(ServerChangeRegionListener.class, listener);
    }

    default public List<ServerChangeRegionListener> getServerChangeRegionListeners() {
        return this.getListeners(ServerChangeRegionListener.class);
    }

    default public ListenerManager<ServerMemberJoinListener> addServerMemberJoinListener(ServerMemberJoinListener listener) {
        return this.addListener(ServerMemberJoinListener.class, listener);
    }

    default public List<ServerMemberJoinListener> getServerMemberJoinListeners() {
        return this.getListeners(ServerMemberJoinListener.class);
    }

    default public ListenerManager<ServerMemberLeaveListener> addServerMemberLeaveListener(ServerMemberLeaveListener listener) {
        return this.addListener(ServerMemberLeaveListener.class, listener);
    }

    default public List<ServerMemberLeaveListener> getServerMemberLeaveListeners() {
        return this.getListeners(ServerMemberLeaveListener.class);
    }

    default public ListenerManager<ServerMemberBanListener> addServerMemberBanListener(ServerMemberBanListener listener) {
        return this.addListener(ServerMemberBanListener.class, listener);
    }

    default public List<ServerMemberBanListener> getServerMemberBanListeners() {
        return this.getListeners(ServerMemberBanListener.class);
    }

    default public ListenerManager<ServerMembersChunkListener> addServerMembersChunkListener(ServerMembersChunkListener listener) {
        return this.addListener(ServerMembersChunkListener.class, listener);
    }

    default public List<ServerMembersChunkListener> getServerMembersChunkListeners() {
        return this.getListeners(ServerMembersChunkListener.class);
    }

    default public ListenerManager<ServerMemberUnbanListener> addServerMemberUnbanListener(ServerMemberUnbanListener listener) {
        return this.addListener(ServerMemberUnbanListener.class, listener);
    }

    default public List<ServerMemberUnbanListener> getServerMemberUnbanListeners() {
        return this.getListeners(ServerMemberUnbanListener.class);
    }

    default public ListenerManager<KnownCustomEmojiChangeNameListener> addKnownCustomEmojiChangeNameListener(KnownCustomEmojiChangeNameListener listener) {
        return this.addListener(KnownCustomEmojiChangeNameListener.class, listener);
    }

    default public List<KnownCustomEmojiChangeNameListener> getKnownCustomEmojiChangeNameListeners() {
        return this.getListeners(KnownCustomEmojiChangeNameListener.class);
    }

    default public ListenerManager<KnownCustomEmojiDeleteListener> addKnownCustomEmojiDeleteListener(KnownCustomEmojiDeleteListener listener) {
        return this.addListener(KnownCustomEmojiDeleteListener.class, listener);
    }

    default public List<KnownCustomEmojiDeleteListener> getKnownCustomEmojiDeleteListeners() {
        return this.getListeners(KnownCustomEmojiDeleteListener.class);
    }

    default public ListenerManager<KnownCustomEmojiChangeWhitelistedRolesListener> addKnownCustomEmojiChangeWhitelistedRolesListener(KnownCustomEmojiChangeWhitelistedRolesListener listener) {
        return this.addListener(KnownCustomEmojiChangeWhitelistedRolesListener.class, listener);
    }

    default public List<KnownCustomEmojiChangeWhitelistedRolesListener> getKnownCustomEmojiChangeWhitelistedRolesListeners() {
        return this.getListeners(KnownCustomEmojiChangeWhitelistedRolesListener.class);
    }

    default public ListenerManager<KnownCustomEmojiCreateListener> addKnownCustomEmojiCreateListener(KnownCustomEmojiCreateListener listener) {
        return this.addListener(KnownCustomEmojiCreateListener.class, listener);
    }

    default public List<KnownCustomEmojiCreateListener> getKnownCustomEmojiCreateListeners() {
        return this.getListeners(KnownCustomEmojiCreateListener.class);
    }

    default public ListenerManager<ServerChangeSystemChannelListener> addServerChangeSystemChannelListener(ServerChangeSystemChannelListener listener) {
        return this.addListener(ServerChangeSystemChannelListener.class, listener);
    }

    default public List<ServerChangeSystemChannelListener> getServerChangeSystemChannelListeners() {
        return this.getListeners(ServerChangeSystemChannelListener.class);
    }

    default public ListenerManager<ServerChangePreferredLocaleListener> addServerChangePreferredLocaleListener(ServerChangePreferredLocaleListener listener) {
        return this.addListener(ServerChangePreferredLocaleListener.class, listener);
    }

    default public List<ServerChangePreferredLocaleListener> getServerChangePreferredLocaleListeners() {
        return this.getListeners(ServerChangePreferredLocaleListener.class);
    }

    default public ListenerManager<ServerChangeBoostLevelListener> addServerChangeBoostLevelListener(ServerChangeBoostLevelListener listener) {
        return this.addListener(ServerChangeBoostLevelListener.class, listener);
    }

    default public List<ServerChangeBoostLevelListener> getServerChangeBoostLevelListeners() {
        return this.getListeners(ServerChangeBoostLevelListener.class);
    }

    default public ListenerManager<ServerChangeRulesChannelListener> addServerChangeRulesChannelListener(ServerChangeRulesChannelListener listener) {
        return this.addListener(ServerChangeRulesChannelListener.class, listener);
    }

    default public List<ServerChangeRulesChannelListener> getServerChangeRulesChannelListeners() {
        return this.getListeners(ServerChangeRulesChannelListener.class);
    }

    default public ListenerManager<ServerChangeServerFeatureListener> addServerChangeServerFeatureListener(ServerChangeServerFeatureListener listener) {
        return this.addListener(ServerChangeServerFeatureListener.class, listener);
    }

    default public List<ServerChangeServerFeatureListener> getServerChangeServerFeatureListeners() {
        return this.getListeners(ServerChangeServerFeatureListener.class);
    }

    default public ListenerManager<ServerChangeOwnerListener> addServerChangeOwnerListener(ServerChangeOwnerListener listener) {
        return this.addListener(ServerChangeOwnerListener.class, listener);
    }

    default public List<ServerChangeOwnerListener> getServerChangeOwnerListeners() {
        return this.getListeners(ServerChangeOwnerListener.class);
    }

    default public ListenerManager<ServerChangeMultiFactorAuthenticationLevelListener> addServerChangeMultiFactorAuthenticationLevelListener(ServerChangeMultiFactorAuthenticationLevelListener listener) {
        return this.addListener(ServerChangeMultiFactorAuthenticationLevelListener.class, listener);
    }

    default public List<ServerChangeMultiFactorAuthenticationLevelListener> getServerChangeMultiFactorAuthenticationLevelListeners() {
        return this.getListeners(ServerChangeMultiFactorAuthenticationLevelListener.class);
    }

    default public ListenerManager<ServerChangeExplicitContentFilterLevelListener> addServerChangeExplicitContentFilterLevelListener(ServerChangeExplicitContentFilterLevelListener listener) {
        return this.addListener(ServerChangeExplicitContentFilterLevelListener.class, listener);
    }

    default public List<ServerChangeExplicitContentFilterLevelListener> getServerChangeExplicitContentFilterLevelListeners() {
        return this.getListeners(ServerChangeExplicitContentFilterLevelListener.class);
    }

    default public ListenerManager<RoleChangePositionListener> addRoleChangePositionListener(RoleChangePositionListener listener) {
        return this.addListener(RoleChangePositionListener.class, listener);
    }

    default public List<RoleChangePositionListener> getRoleChangePositionListeners() {
        return this.getListeners(RoleChangePositionListener.class);
    }

    default public ListenerManager<RoleChangeMentionableListener> addRoleChangeMentionableListener(RoleChangeMentionableListener listener) {
        return this.addListener(RoleChangeMentionableListener.class, listener);
    }

    default public List<RoleChangeMentionableListener> getRoleChangeMentionableListeners() {
        return this.getListeners(RoleChangeMentionableListener.class);
    }

    default public ListenerManager<RoleChangeColorListener> addRoleChangeColorListener(RoleChangeColorListener listener) {
        return this.addListener(RoleChangeColorListener.class, listener);
    }

    default public List<RoleChangeColorListener> getRoleChangeColorListeners() {
        return this.getListeners(RoleChangeColorListener.class);
    }

    default public ListenerManager<RoleChangeNameListener> addRoleChangeNameListener(RoleChangeNameListener listener) {
        return this.addListener(RoleChangeNameListener.class, listener);
    }

    default public List<RoleChangeNameListener> getRoleChangeNameListeners() {
        return this.getListeners(RoleChangeNameListener.class);
    }

    default public ListenerManager<RoleChangeHoistListener> addRoleChangeHoistListener(RoleChangeHoistListener listener) {
        return this.addListener(RoleChangeHoistListener.class, listener);
    }

    default public List<RoleChangeHoistListener> getRoleChangeHoistListeners() {
        return this.getListeners(RoleChangeHoistListener.class);
    }

    default public ListenerManager<RoleCreateListener> addRoleCreateListener(RoleCreateListener listener) {
        return this.addListener(RoleCreateListener.class, listener);
    }

    default public List<RoleCreateListener> getRoleCreateListeners() {
        return this.getListeners(RoleCreateListener.class);
    }

    default public ListenerManager<RoleChangePermissionsListener> addRoleChangePermissionsListener(RoleChangePermissionsListener listener) {
        return this.addListener(RoleChangePermissionsListener.class, listener);
    }

    default public List<RoleChangePermissionsListener> getRoleChangePermissionsListeners() {
        return this.getListeners(RoleChangePermissionsListener.class);
    }

    default public ListenerManager<UserRoleRemoveListener> addUserRoleRemoveListener(UserRoleRemoveListener listener) {
        return this.addListener(UserRoleRemoveListener.class, listener);
    }

    default public List<UserRoleRemoveListener> getUserRoleRemoveListeners() {
        return this.getListeners(UserRoleRemoveListener.class);
    }

    default public ListenerManager<UserRoleAddListener> addUserRoleAddListener(UserRoleAddListener listener) {
        return this.addListener(UserRoleAddListener.class, listener);
    }

    default public List<UserRoleAddListener> getUserRoleAddListeners() {
        return this.getListeners(UserRoleAddListener.class);
    }

    default public ListenerManager<RoleDeleteListener> addRoleDeleteListener(RoleDeleteListener listener) {
        return this.addListener(RoleDeleteListener.class, listener);
    }

    default public List<RoleDeleteListener> getRoleDeleteListeners() {
        return this.getListeners(RoleDeleteListener.class);
    }

    default public ListenerManager<ServerChangeModeratorsOnlyChannelListener> addServerChangeModeratorsOnlyChannelListener(ServerChangeModeratorsOnlyChannelListener listener) {
        return this.addListener(ServerChangeModeratorsOnlyChannelListener.class, listener);
    }

    default public List<ServerChangeModeratorsOnlyChannelListener> getServerChangeModeratorsOnlyChannelListeners() {
        return this.getListeners(ServerChangeModeratorsOnlyChannelListener.class);
    }

    default public ListenerManager<ServerChangeNsfwLevelListener> addServerChangeNsfwLevelListener(ServerChangeNsfwLevelListener listener) {
        return this.addListener(ServerChangeNsfwLevelListener.class, listener);
    }

    default public List<ServerChangeNsfwLevelListener> getServerChangeNsfwLevelListeners() {
        return this.getListeners(ServerChangeNsfwLevelListener.class);
    }

    default public ListenerManager<ServerChannelChangePositionListener> addServerChannelChangePositionListener(ServerChannelChangePositionListener listener) {
        return this.addListener(ServerChannelChangePositionListener.class, listener);
    }

    default public List<ServerChannelChangePositionListener> getServerChannelChangePositionListeners() {
        return this.getListeners(ServerChannelChangePositionListener.class);
    }

    default public ListenerManager<ServerThreadListSyncListener> addServerThreadListSyncListener(ServerThreadListSyncListener listener) {
        return this.addListener(ServerThreadListSyncListener.class, listener);
    }

    default public List<ServerThreadListSyncListener> getServerThreadListSyncListeners() {
        return this.getListeners(ServerThreadListSyncListener.class);
    }

    default public ListenerManager<ServerThreadChannelUpdateListener> addServerThreadChannelUpdateListener(ServerThreadChannelUpdateListener listener) {
        return this.addListener(ServerThreadChannelUpdateListener.class, listener);
    }

    default public List<ServerThreadChannelUpdateListener> getServerThreadChannelUpdateListeners() {
        return this.getListeners(ServerThreadChannelUpdateListener.class);
    }

    default public ListenerManager<ServerThreadChannelMembersUpdateListener> addServerThreadChannelMembersUpdateListener(ServerThreadChannelMembersUpdateListener listener) {
        return this.addListener(ServerThreadChannelMembersUpdateListener.class, listener);
    }

    default public List<ServerThreadChannelMembersUpdateListener> getServerThreadChannelMembersUpdateListeners() {
        return this.getListeners(ServerThreadChannelMembersUpdateListener.class);
    }

    default public ListenerManager<ServerThreadChannelCreateListener> addServerThreadChannelCreateListener(ServerThreadChannelCreateListener listener) {
        return this.addListener(ServerThreadChannelCreateListener.class, listener);
    }

    default public List<ServerThreadChannelCreateListener> getServerThreadChannelCreateListeners() {
        return this.getListeners(ServerThreadChannelCreateListener.class);
    }

    default public ListenerManager<ServerThreadChannelDeleteListener> addServerThreadChannelDeleteListener(ServerThreadChannelDeleteListener listener) {
        return this.addListener(ServerThreadChannelDeleteListener.class, listener);
    }

    default public List<ServerThreadChannelDeleteListener> getServerThreadChannelDeleteListeners() {
        return this.getListeners(ServerThreadChannelDeleteListener.class);
    }

    default public ListenerManager<WebhooksUpdateListener> addWebhooksUpdateListener(WebhooksUpdateListener listener) {
        return this.addListener(WebhooksUpdateListener.class, listener);
    }

    default public List<WebhooksUpdateListener> getWebhooksUpdateListeners() {
        return this.getListeners(WebhooksUpdateListener.class);
    }

    default public ListenerManager<ServerTextChannelChangeDefaultAutoArchiveDurationListener> addServerTextChannelChangeDefaultAutoArchiveDurationListener(ServerTextChannelChangeDefaultAutoArchiveDurationListener listener) {
        return this.addListener(ServerTextChannelChangeDefaultAutoArchiveDurationListener.class, listener);
    }

    default public List<ServerTextChannelChangeDefaultAutoArchiveDurationListener> getServerTextChannelChangeDefaultAutoArchiveDurationListeners() {
        return this.getListeners(ServerTextChannelChangeDefaultAutoArchiveDurationListener.class);
    }

    default public ListenerManager<ServerTextChannelChangeSlowmodeListener> addServerTextChannelChangeSlowmodeListener(ServerTextChannelChangeSlowmodeListener listener) {
        return this.addListener(ServerTextChannelChangeSlowmodeListener.class, listener);
    }

    default public List<ServerTextChannelChangeSlowmodeListener> getServerTextChannelChangeSlowmodeListeners() {
        return this.getListeners(ServerTextChannelChangeSlowmodeListener.class);
    }

    default public ListenerManager<ServerTextChannelChangeTopicListener> addServerTextChannelChangeTopicListener(ServerTextChannelChangeTopicListener listener) {
        return this.addListener(ServerTextChannelChangeTopicListener.class, listener);
    }

    default public List<ServerTextChannelChangeTopicListener> getServerTextChannelChangeTopicListeners() {
        return this.getListeners(ServerTextChannelChangeTopicListener.class);
    }

    default public ListenerManager<ServerChannelChangeOverwrittenPermissionsListener> addServerChannelChangeOverwrittenPermissionsListener(ServerChannelChangeOverwrittenPermissionsListener listener) {
        return this.addListener(ServerChannelChangeOverwrittenPermissionsListener.class, listener);
    }

    default public List<ServerChannelChangeOverwrittenPermissionsListener> getServerChannelChangeOverwrittenPermissionsListeners() {
        return this.getListeners(ServerChannelChangeOverwrittenPermissionsListener.class);
    }

    default public ListenerManager<ServerChannelInviteDeleteListener> addServerChannelInviteDeleteListener(ServerChannelInviteDeleteListener listener) {
        return this.addListener(ServerChannelInviteDeleteListener.class, listener);
    }

    default public List<ServerChannelInviteDeleteListener> getServerChannelInviteDeleteListeners() {
        return this.getListeners(ServerChannelInviteDeleteListener.class);
    }

    default public ListenerManager<ServerChannelInviteCreateListener> addServerChannelInviteCreateListener(ServerChannelInviteCreateListener listener) {
        return this.addListener(ServerChannelInviteCreateListener.class, listener);
    }

    default public List<ServerChannelInviteCreateListener> getServerChannelInviteCreateListeners() {
        return this.getListeners(ServerChannelInviteCreateListener.class);
    }

    default public ListenerManager<ServerChannelChangeNsfwFlagListener> addServerChannelChangeNsfwFlagListener(ServerChannelChangeNsfwFlagListener listener) {
        return this.addListener(ServerChannelChangeNsfwFlagListener.class, listener);
    }

    default public List<ServerChannelChangeNsfwFlagListener> getServerChannelChangeNsfwFlagListeners() {
        return this.getListeners(ServerChannelChangeNsfwFlagListener.class);
    }

    default public ListenerManager<ServerChannelDeleteListener> addServerChannelDeleteListener(ServerChannelDeleteListener listener) {
        return this.addListener(ServerChannelDeleteListener.class, listener);
    }

    default public List<ServerChannelDeleteListener> getServerChannelDeleteListeners() {
        return this.getListeners(ServerChannelDeleteListener.class);
    }

    default public ListenerManager<ServerChannelCreateListener> addServerChannelCreateListener(ServerChannelCreateListener listener) {
        return this.addListener(ServerChannelCreateListener.class, listener);
    }

    default public List<ServerChannelCreateListener> getServerChannelCreateListeners() {
        return this.getListeners(ServerChannelCreateListener.class);
    }

    default public ListenerManager<ServerStageVoiceChannelChangeTopicListener> addServerStageVoiceChannelChangeTopicListener(ServerStageVoiceChannelChangeTopicListener listener) {
        return this.addListener(ServerStageVoiceChannelChangeTopicListener.class, listener);
    }

    default public List<ServerStageVoiceChannelChangeTopicListener> getServerStageVoiceChannelChangeTopicListeners() {
        return this.getListeners(ServerStageVoiceChannelChangeTopicListener.class);
    }

    default public ListenerManager<ServerVoiceChannelChangeBitrateListener> addServerVoiceChannelChangeBitrateListener(ServerVoiceChannelChangeBitrateListener listener) {
        return this.addListener(ServerVoiceChannelChangeBitrateListener.class, listener);
    }

    default public List<ServerVoiceChannelChangeBitrateListener> getServerVoiceChannelChangeBitrateListeners() {
        return this.getListeners(ServerVoiceChannelChangeBitrateListener.class);
    }

    default public ListenerManager<ServerVoiceChannelChangeUserLimitListener> addServerVoiceChannelChangeUserLimitListener(ServerVoiceChannelChangeUserLimitListener listener) {
        return this.addListener(ServerVoiceChannelChangeUserLimitListener.class, listener);
    }

    default public List<ServerVoiceChannelChangeUserLimitListener> getServerVoiceChannelChangeUserLimitListeners() {
        return this.getListeners(ServerVoiceChannelChangeUserLimitListener.class);
    }

    default public ListenerManager<ServerVoiceChannelMemberLeaveListener> addServerVoiceChannelMemberLeaveListener(ServerVoiceChannelMemberLeaveListener listener) {
        return this.addListener(ServerVoiceChannelMemberLeaveListener.class, listener);
    }

    default public List<ServerVoiceChannelMemberLeaveListener> getServerVoiceChannelMemberLeaveListeners() {
        return this.getListeners(ServerVoiceChannelMemberLeaveListener.class);
    }

    default public ListenerManager<ServerVoiceChannelChangeNsfwListener> addServerVoiceChannelChangeNsfwListener(ServerVoiceChannelChangeNsfwListener listener) {
        return this.addListener(ServerVoiceChannelChangeNsfwListener.class, listener);
    }

    default public List<ServerVoiceChannelChangeNsfwListener> getServerVoiceChannelChangeNsfwListeners() {
        return this.getListeners(ServerVoiceChannelChangeNsfwListener.class);
    }

    default public ListenerManager<ServerVoiceChannelMemberJoinListener> addServerVoiceChannelMemberJoinListener(ServerVoiceChannelMemberJoinListener listener) {
        return this.addListener(ServerVoiceChannelMemberJoinListener.class, listener);
    }

    default public List<ServerVoiceChannelMemberJoinListener> getServerVoiceChannelMemberJoinListeners() {
        return this.getListeners(ServerVoiceChannelMemberJoinListener.class);
    }

    default public ListenerManager<ServerChannelChangeNameListener> addServerChannelChangeNameListener(ServerChannelChangeNameListener listener) {
        return this.addListener(ServerChannelChangeNameListener.class, listener);
    }

    default public List<ServerChannelChangeNameListener> getServerChannelChangeNameListeners() {
        return this.getListeners(ServerChannelChangeNameListener.class);
    }

    default public ListenerManager<PrivateChannelDeleteListener> addPrivateChannelDeleteListener(PrivateChannelDeleteListener listener) {
        return this.addListener(PrivateChannelDeleteListener.class, listener);
    }

    default public List<PrivateChannelDeleteListener> getPrivateChannelDeleteListeners() {
        return this.getListeners(PrivateChannelDeleteListener.class);
    }

    default public ListenerManager<PrivateChannelCreateListener> addPrivateChannelCreateListener(PrivateChannelCreateListener listener) {
        return this.addListener(PrivateChannelCreateListener.class, listener);
    }

    default public List<PrivateChannelCreateListener> getPrivateChannelCreateListeners() {
        return this.getListeners(PrivateChannelCreateListener.class);
    }

    default public ListenerManager<AudioSourceFinishedListener> addAudioSourceFinishedListener(AudioSourceFinishedListener listener) {
        return this.addListener(AudioSourceFinishedListener.class, listener);
    }

    default public List<AudioSourceFinishedListener> getAudioSourceFinishedListeners() {
        return this.getListeners(AudioSourceFinishedListener.class);
    }

    default public ListenerManager<UserChangeDeafenedListener> addUserChangeDeafenedListener(UserChangeDeafenedListener listener) {
        return this.addListener(UserChangeDeafenedListener.class, listener);
    }

    default public List<UserChangeDeafenedListener> getUserChangeDeafenedListeners() {
        return this.getListeners(UserChangeDeafenedListener.class);
    }

    default public ListenerManager<UserChangeNicknameListener> addUserChangeNicknameListener(UserChangeNicknameListener listener) {
        return this.addListener(UserChangeNicknameListener.class, listener);
    }

    default public List<UserChangeNicknameListener> getUserChangeNicknameListeners() {
        return this.getListeners(UserChangeNicknameListener.class);
    }

    default public ListenerManager<UserChangePendingListener> addUserChangePendingListener(UserChangePendingListener listener) {
        return this.addListener(UserChangePendingListener.class, listener);
    }

    default public List<UserChangePendingListener> getUserChangePendingListeners() {
        return this.getListeners(UserChangePendingListener.class);
    }

    default public ListenerManager<UserStartTypingListener> addUserStartTypingListener(UserStartTypingListener listener) {
        return this.addListener(UserStartTypingListener.class, listener);
    }

    default public List<UserStartTypingListener> getUserStartTypingListeners() {
        return this.getListeners(UserStartTypingListener.class);
    }

    default public ListenerManager<UserChangeDiscriminatorListener> addUserChangeDiscriminatorListener(UserChangeDiscriminatorListener listener) {
        return this.addListener(UserChangeDiscriminatorListener.class, listener);
    }

    default public List<UserChangeDiscriminatorListener> getUserChangeDiscriminatorListeners() {
        return this.getListeners(UserChangeDiscriminatorListener.class);
    }

    default public ListenerManager<UserChangeStatusListener> addUserChangeStatusListener(UserChangeStatusListener listener) {
        return this.addListener(UserChangeStatusListener.class, listener);
    }

    default public List<UserChangeStatusListener> getUserChangeStatusListeners() {
        return this.getListeners(UserChangeStatusListener.class);
    }

    default public ListenerManager<UserChangeServerAvatarListener> addUserChangeServerAvatarListener(UserChangeServerAvatarListener listener) {
        return this.addListener(UserChangeServerAvatarListener.class, listener);
    }

    default public List<UserChangeServerAvatarListener> getUserChangeServerAvatarListeners() {
        return this.getListeners(UserChangeServerAvatarListener.class);
    }

    default public ListenerManager<UserChangeSelfMutedListener> addUserChangeSelfMutedListener(UserChangeSelfMutedListener listener) {
        return this.addListener(UserChangeSelfMutedListener.class, listener);
    }

    default public List<UserChangeSelfMutedListener> getUserChangeSelfMutedListeners() {
        return this.getListeners(UserChangeSelfMutedListener.class);
    }

    default public ListenerManager<UserChangeNameListener> addUserChangeNameListener(UserChangeNameListener listener) {
        return this.addListener(UserChangeNameListener.class, listener);
    }

    default public List<UserChangeNameListener> getUserChangeNameListeners() {
        return this.getListeners(UserChangeNameListener.class);
    }

    default public ListenerManager<UserChangeTimeoutListener> addUserChangeTimeoutListener(UserChangeTimeoutListener listener) {
        return this.addListener(UserChangeTimeoutListener.class, listener);
    }

    default public List<UserChangeTimeoutListener> getUserChangeTimeoutListeners() {
        return this.getListeners(UserChangeTimeoutListener.class);
    }

    default public ListenerManager<UserChangeAvatarListener> addUserChangeAvatarListener(UserChangeAvatarListener listener) {
        return this.addListener(UserChangeAvatarListener.class, listener);
    }

    default public List<UserChangeAvatarListener> getUserChangeAvatarListeners() {
        return this.getListeners(UserChangeAvatarListener.class);
    }

    default public ListenerManager<UserChangeSelfDeafenedListener> addUserChangeSelfDeafenedListener(UserChangeSelfDeafenedListener listener) {
        return this.addListener(UserChangeSelfDeafenedListener.class, listener);
    }

    default public List<UserChangeSelfDeafenedListener> getUserChangeSelfDeafenedListeners() {
        return this.getListeners(UserChangeSelfDeafenedListener.class);
    }

    default public ListenerManager<UserChangeMutedListener> addUserChangeMutedListener(UserChangeMutedListener listener) {
        return this.addListener(UserChangeMutedListener.class, listener);
    }

    default public List<UserChangeMutedListener> getUserChangeMutedListeners() {
        return this.getListeners(UserChangeMutedListener.class);
    }

    default public ListenerManager<UserChangeActivityListener> addUserChangeActivityListener(UserChangeActivityListener listener) {
        return this.addListener(UserChangeActivityListener.class, listener);
    }

    default public List<UserChangeActivityListener> getUserChangeActivityListeners() {
        return this.getListeners(UserChangeActivityListener.class);
    }

    default public ListenerManager<MessageEditListener> addMessageEditListener(MessageEditListener listener) {
        return this.addListener(MessageEditListener.class, listener);
    }

    default public List<MessageEditListener> getMessageEditListeners() {
        return this.getListeners(MessageEditListener.class);
    }

    default public ListenerManager<ChannelPinsUpdateListener> addChannelPinsUpdateListener(ChannelPinsUpdateListener listener) {
        return this.addListener(ChannelPinsUpdateListener.class, listener);
    }

    default public List<ChannelPinsUpdateListener> getChannelPinsUpdateListeners() {
        return this.getListeners(ChannelPinsUpdateListener.class);
    }

    default public ListenerManager<ReactionRemoveListener> addReactionRemoveListener(ReactionRemoveListener listener) {
        return this.addListener(ReactionRemoveListener.class, listener);
    }

    default public List<ReactionRemoveListener> getReactionRemoveListeners() {
        return this.getListeners(ReactionRemoveListener.class);
    }

    default public ListenerManager<ReactionAddListener> addReactionAddListener(ReactionAddListener listener) {
        return this.addListener(ReactionAddListener.class, listener);
    }

    default public List<ReactionAddListener> getReactionAddListeners() {
        return this.getListeners(ReactionAddListener.class);
    }

    default public ListenerManager<ReactionRemoveAllListener> addReactionRemoveAllListener(ReactionRemoveAllListener listener) {
        return this.addListener(ReactionRemoveAllListener.class, listener);
    }

    default public List<ReactionRemoveAllListener> getReactionRemoveAllListeners() {
        return this.getListeners(ReactionRemoveAllListener.class);
    }

    default public ListenerManager<MessageCreateListener> addMessageCreateListener(MessageCreateListener listener) {
        return this.addListener(MessageCreateListener.class, listener);
    }

    default public List<MessageCreateListener> getMessageCreateListeners() {
        return this.getListeners(MessageCreateListener.class);
    }

    default public ListenerManager<CachedMessageUnpinListener> addCachedMessageUnpinListener(CachedMessageUnpinListener listener) {
        return this.addListener(CachedMessageUnpinListener.class, listener);
    }

    default public List<CachedMessageUnpinListener> getCachedMessageUnpinListeners() {
        return this.getListeners(CachedMessageUnpinListener.class);
    }

    default public ListenerManager<CachedMessagePinListener> addCachedMessagePinListener(CachedMessagePinListener listener) {
        return this.addListener(CachedMessagePinListener.class, listener);
    }

    default public List<CachedMessagePinListener> getCachedMessagePinListeners() {
        return this.getListeners(CachedMessagePinListener.class);
    }

    default public ListenerManager<MessageReplyListener> addMessageReplyListener(MessageReplyListener listener) {
        return this.addListener(MessageReplyListener.class, listener);
    }

    default public List<MessageReplyListener> getMessageReplyListeners() {
        return this.getListeners(MessageReplyListener.class);
    }

    default public ListenerManager<MessageDeleteListener> addMessageDeleteListener(MessageDeleteListener listener) {
        return this.addListener(MessageDeleteListener.class, listener);
    }

    default public List<MessageDeleteListener> getMessageDeleteListeners() {
        return this.getListeners(MessageDeleteListener.class);
    }

    default public ListenerManager<ResumeListener> addResumeListener(ResumeListener listener) {
        return this.addListener(ResumeListener.class, listener);
    }

    default public List<ResumeListener> getResumeListeners() {
        return this.getListeners(ResumeListener.class);
    }

    default public ListenerManager<LostConnectionListener> addLostConnectionListener(LostConnectionListener listener) {
        return this.addListener(LostConnectionListener.class, listener);
    }

    default public List<LostConnectionListener> getLostConnectionListeners() {
        return this.getListeners(LostConnectionListener.class);
    }

    default public ListenerManager<ReconnectListener> addReconnectListener(ReconnectListener listener) {
        return this.addListener(ReconnectListener.class, listener);
    }

    default public List<ReconnectListener> getReconnectListeners() {
        return this.getListeners(ReconnectListener.class);
    }

    public <T extends GloballyAttachableListener> ListenerManager<T> addListener(Class<T> var1, T var2);

    public Collection<ListenerManager<? extends GloballyAttachableListener>> addListener(GloballyAttachableListener var1);

    public void removeListener(GloballyAttachableListener var1);

    public <T extends GloballyAttachableListener> void removeListener(Class<T> var1, T var2);

    public <T extends GloballyAttachableListener> Map<T, List<Class<T>>> getListeners();

    public <T extends GloballyAttachableListener> List<T> getListeners(Class<T> var1);
}

