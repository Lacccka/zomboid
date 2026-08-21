/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.javacord.api.listener.ObjectAttachableListener;
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
import org.javacord.api.listener.channel.server.thread.ServerThreadChannelMembersUpdateListener;
import org.javacord.api.listener.channel.server.thread.ServerThreadChannelUpdateListener;
import org.javacord.api.listener.channel.server.thread.ServerThreadListSyncListener;
import org.javacord.api.listener.channel.server.voice.ServerStageVoiceChannelChangeTopicListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelChangeBitrateListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelChangeNsfwListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelChangeUserLimitListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberJoinListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberLeaveListener;
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
import org.javacord.api.listener.server.ServerAttachableListener;
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
import org.javacord.api.listener.server.ServerLeaveListener;
import org.javacord.api.listener.server.VoiceServerUpdateListener;
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

public interface ServerAttachableListenerManager {
    public ListenerManager<InteractionCreateListener> addInteractionCreateListener(InteractionCreateListener var1);

    public List<InteractionCreateListener> getInteractionCreateListeners();

    public ListenerManager<SlashCommandCreateListener> addSlashCommandCreateListener(SlashCommandCreateListener var1);

    public List<SlashCommandCreateListener> getSlashCommandCreateListeners();

    public ListenerManager<AutocompleteCreateListener> addAutocompleteCreateListener(AutocompleteCreateListener var1);

    public List<AutocompleteCreateListener> getAutocompleteCreateListeners();

    public ListenerManager<ModalSubmitListener> addModalSubmitListener(ModalSubmitListener var1);

    public List<ModalSubmitListener> getModalSubmitListeners();

    public ListenerManager<MessageContextMenuCommandListener> addMessageContextMenuCommandListener(MessageContextMenuCommandListener var1);

    public List<MessageContextMenuCommandListener> getMessageContextMenuCommandListeners();

    public ListenerManager<MessageComponentCreateListener> addMessageComponentCreateListener(MessageComponentCreateListener var1);

    public List<MessageComponentCreateListener> getMessageComponentCreateListeners();

    public ListenerManager<UserContextMenuCommandListener> addUserContextMenuCommandListener(UserContextMenuCommandListener var1);

    public List<UserContextMenuCommandListener> getUserContextMenuCommandListeners();

    public ListenerManager<SelectMenuChooseListener> addSelectMenuChooseListener(SelectMenuChooseListener var1);

    public List<SelectMenuChooseListener> getSelectMenuChooseListeners();

    public ListenerManager<ButtonClickListener> addButtonClickListener(ButtonClickListener var1);

    public List<ButtonClickListener> getButtonClickListeners();

    public ListenerManager<ServerChangeIconListener> addServerChangeIconListener(ServerChangeIconListener var1);

    public List<ServerChangeIconListener> getServerChangeIconListeners();

    public ListenerManager<ServerChangeNameListener> addServerChangeNameListener(ServerChangeNameListener var1);

    public List<ServerChangeNameListener> getServerChangeNameListeners();

    public ListenerManager<ServerThreadChannelChangeLastMessageIdListener> addServerThreadChannelChangeLastMessageIdListener(ServerThreadChannelChangeLastMessageIdListener var1);

    public List<ServerThreadChannelChangeLastMessageIdListener> getServerThreadChannelChangeLastMessageIdListeners();

    public ListenerManager<ServerThreadChannelChangeArchivedListener> addServerThreadChannelChangeArchivedListener(ServerThreadChannelChangeArchivedListener var1);

    public List<ServerThreadChannelChangeArchivedListener> getServerThreadChannelChangeArchivedListeners();

    public ListenerManager<ServerThreadChannelChangeMemberCountListener> addServerThreadChannelChangeMemberCountListener(ServerThreadChannelChangeMemberCountListener var1);

    public List<ServerThreadChannelChangeMemberCountListener> getServerThreadChannelChangeMemberCountListeners();

    public ListenerManager<ServerPrivateThreadJoinListener> addServerPrivateThreadJoinListener(ServerPrivateThreadJoinListener var1);

    public List<ServerPrivateThreadJoinListener> getServerPrivateThreadJoinListeners();

    public ListenerManager<ServerThreadChannelChangeInvitableListener> addServerThreadChannelChangeInvitableListener(ServerThreadChannelChangeInvitableListener var1);

    public List<ServerThreadChannelChangeInvitableListener> getServerThreadChannelChangeInvitableListeners();

    public ListenerManager<ServerThreadChannelChangeAutoArchiveDurationListener> addServerThreadChannelChangeAutoArchiveDurationListener(ServerThreadChannelChangeAutoArchiveDurationListener var1);

    public List<ServerThreadChannelChangeAutoArchiveDurationListener> getServerThreadChannelChangeAutoArchiveDurationListeners();

    public ListenerManager<ServerThreadChannelChangeRateLimitPerUserListener> addServerThreadChannelChangeRateLimitPerUserListener(ServerThreadChannelChangeRateLimitPerUserListener var1);

    public List<ServerThreadChannelChangeRateLimitPerUserListener> getServerThreadChannelChangeRateLimitPerUserListeners();

    public ListenerManager<ServerThreadChannelChangeLockedListener> addServerThreadChannelChangeLockedListener(ServerThreadChannelChangeLockedListener var1);

    public List<ServerThreadChannelChangeLockedListener> getServerThreadChannelChangeLockedListeners();

    public ListenerManager<ServerThreadChannelChangeArchiveTimestampListener> addServerThreadChannelChangeArchiveTimestampListener(ServerThreadChannelChangeArchiveTimestampListener var1);

    public List<ServerThreadChannelChangeArchiveTimestampListener> getServerThreadChannelChangeArchiveTimestampListeners();

    public ListenerManager<ServerThreadChannelChangeTotalMessageSentListener> addServerThreadChannelChangeTotalMessageSentListener(ServerThreadChannelChangeTotalMessageSentListener var1);

    public List<ServerThreadChannelChangeTotalMessageSentListener> getServerThreadChannelChangeTotalMessageSentListeners();

    public ListenerManager<ServerThreadChannelChangeMessageCountListener> addServerThreadChannelChangeMessageCountListener(ServerThreadChannelChangeMessageCountListener var1);

    public List<ServerThreadChannelChangeMessageCountListener> getServerThreadChannelChangeMessageCountListeners();

    public ListenerManager<ServerChangeAfkTimeoutListener> addServerChangeAfkTimeoutListener(ServerChangeAfkTimeoutListener var1);

    public List<ServerChangeAfkTimeoutListener> getServerChangeAfkTimeoutListeners();

    public ListenerManager<StickerChangeTagsListener> addStickerChangeTagsListener(StickerChangeTagsListener var1);

    public List<StickerChangeTagsListener> getStickerChangeTagsListeners();

    public ListenerManager<StickerChangeDescriptionListener> addStickerChangeDescriptionListener(StickerChangeDescriptionListener var1);

    public List<StickerChangeDescriptionListener> getStickerChangeDescriptionListeners();

    public ListenerManager<StickerCreateListener> addStickerCreateListener(StickerCreateListener var1);

    public List<StickerCreateListener> getStickerCreateListeners();

    public ListenerManager<StickerChangeNameListener> addStickerChangeNameListener(StickerChangeNameListener var1);

    public List<StickerChangeNameListener> getStickerChangeNameListeners();

    public ListenerManager<StickerDeleteListener> addStickerDeleteListener(StickerDeleteListener var1);

    public List<StickerDeleteListener> getStickerDeleteListeners();

    public ListenerManager<ServerChangeSplashListener> addServerChangeSplashListener(ServerChangeSplashListener var1);

    public List<ServerChangeSplashListener> getServerChangeSplashListeners();

    public ListenerManager<ServerChangeAfkChannelListener> addServerChangeAfkChannelListener(ServerChangeAfkChannelListener var1);

    public List<ServerChangeAfkChannelListener> getServerChangeAfkChannelListeners();

    public ListenerManager<ServerChangeVanityUrlCodeListener> addServerChangeVanityUrlCodeListener(ServerChangeVanityUrlCodeListener var1);

    public List<ServerChangeVanityUrlCodeListener> getServerChangeVanityUrlCodeListeners();

    public ListenerManager<ServerChangeDiscoverySplashListener> addServerChangeDiscoverySplashListener(ServerChangeDiscoverySplashListener var1);

    public List<ServerChangeDiscoverySplashListener> getServerChangeDiscoverySplashListeners();

    public ListenerManager<ApplicationCommandPermissionsUpdateListener> addApplicationCommandPermissionsUpdateListener(ApplicationCommandPermissionsUpdateListener var1);

    public List<ApplicationCommandPermissionsUpdateListener> getApplicationCommandPermissionsUpdateListeners();

    public ListenerManager<ServerBecomesUnavailableListener> addServerBecomesUnavailableListener(ServerBecomesUnavailableListener var1);

    public List<ServerBecomesUnavailableListener> getServerBecomesUnavailableListeners();

    public ListenerManager<VoiceServerUpdateListener> addVoiceServerUpdateListener(VoiceServerUpdateListener var1);

    public List<VoiceServerUpdateListener> getVoiceServerUpdateListeners();

    public ListenerManager<ServerChangeDescriptionListener> addServerChangeDescriptionListener(ServerChangeDescriptionListener var1);

    public List<ServerChangeDescriptionListener> getServerChangeDescriptionListeners();

    public ListenerManager<ServerChangeVerificationLevelListener> addServerChangeVerificationLevelListener(ServerChangeVerificationLevelListener var1);

    public List<ServerChangeVerificationLevelListener> getServerChangeVerificationLevelListeners();

    public ListenerManager<ServerLeaveListener> addServerLeaveListener(ServerLeaveListener var1);

    public List<ServerLeaveListener> getServerLeaveListeners();

    public ListenerManager<ServerChangeBoostCountListener> addServerChangeBoostCountListener(ServerChangeBoostCountListener var1);

    public List<ServerChangeBoostCountListener> getServerChangeBoostCountListeners();

    public ListenerManager<ServerChangeDefaultMessageNotificationLevelListener> addServerChangeDefaultMessageNotificationLevelListener(ServerChangeDefaultMessageNotificationLevelListener var1);

    public List<ServerChangeDefaultMessageNotificationLevelListener> getServerChangeDefaultMessageNotificationLevelListeners();

    public ListenerManager<ServerChangeRegionListener> addServerChangeRegionListener(ServerChangeRegionListener var1);

    public List<ServerChangeRegionListener> getServerChangeRegionListeners();

    public ListenerManager<ServerMemberJoinListener> addServerMemberJoinListener(ServerMemberJoinListener var1);

    public List<ServerMemberJoinListener> getServerMemberJoinListeners();

    public ListenerManager<ServerMemberLeaveListener> addServerMemberLeaveListener(ServerMemberLeaveListener var1);

    public List<ServerMemberLeaveListener> getServerMemberLeaveListeners();

    public ListenerManager<ServerMemberBanListener> addServerMemberBanListener(ServerMemberBanListener var1);

    public List<ServerMemberBanListener> getServerMemberBanListeners();

    public ListenerManager<ServerMembersChunkListener> addServerMembersChunkListener(ServerMembersChunkListener var1);

    public List<ServerMembersChunkListener> getServerMembersChunkListeners();

    public ListenerManager<ServerMemberUnbanListener> addServerMemberUnbanListener(ServerMemberUnbanListener var1);

    public List<ServerMemberUnbanListener> getServerMemberUnbanListeners();

    public ListenerManager<KnownCustomEmojiChangeNameListener> addKnownCustomEmojiChangeNameListener(KnownCustomEmojiChangeNameListener var1);

    public List<KnownCustomEmojiChangeNameListener> getKnownCustomEmojiChangeNameListeners();

    public ListenerManager<KnownCustomEmojiDeleteListener> addKnownCustomEmojiDeleteListener(KnownCustomEmojiDeleteListener var1);

    public List<KnownCustomEmojiDeleteListener> getKnownCustomEmojiDeleteListeners();

    public ListenerManager<KnownCustomEmojiChangeWhitelistedRolesListener> addKnownCustomEmojiChangeWhitelistedRolesListener(KnownCustomEmojiChangeWhitelistedRolesListener var1);

    public List<KnownCustomEmojiChangeWhitelistedRolesListener> getKnownCustomEmojiChangeWhitelistedRolesListeners();

    public ListenerManager<KnownCustomEmojiCreateListener> addKnownCustomEmojiCreateListener(KnownCustomEmojiCreateListener var1);

    public List<KnownCustomEmojiCreateListener> getKnownCustomEmojiCreateListeners();

    public ListenerManager<ServerChangeSystemChannelListener> addServerChangeSystemChannelListener(ServerChangeSystemChannelListener var1);

    public List<ServerChangeSystemChannelListener> getServerChangeSystemChannelListeners();

    public ListenerManager<ServerChangePreferredLocaleListener> addServerChangePreferredLocaleListener(ServerChangePreferredLocaleListener var1);

    public List<ServerChangePreferredLocaleListener> getServerChangePreferredLocaleListeners();

    public ListenerManager<ServerChangeBoostLevelListener> addServerChangeBoostLevelListener(ServerChangeBoostLevelListener var1);

    public List<ServerChangeBoostLevelListener> getServerChangeBoostLevelListeners();

    public ListenerManager<ServerChangeRulesChannelListener> addServerChangeRulesChannelListener(ServerChangeRulesChannelListener var1);

    public List<ServerChangeRulesChannelListener> getServerChangeRulesChannelListeners();

    public ListenerManager<ServerChangeServerFeatureListener> addServerChangeServerFeatureListener(ServerChangeServerFeatureListener var1);

    public List<ServerChangeServerFeatureListener> getServerChangeServerFeatureListeners();

    public ListenerManager<ServerChangeOwnerListener> addServerChangeOwnerListener(ServerChangeOwnerListener var1);

    public List<ServerChangeOwnerListener> getServerChangeOwnerListeners();

    public ListenerManager<ServerChangeMultiFactorAuthenticationLevelListener> addServerChangeMultiFactorAuthenticationLevelListener(ServerChangeMultiFactorAuthenticationLevelListener var1);

    public List<ServerChangeMultiFactorAuthenticationLevelListener> getServerChangeMultiFactorAuthenticationLevelListeners();

    public ListenerManager<ServerChangeExplicitContentFilterLevelListener> addServerChangeExplicitContentFilterLevelListener(ServerChangeExplicitContentFilterLevelListener var1);

    public List<ServerChangeExplicitContentFilterLevelListener> getServerChangeExplicitContentFilterLevelListeners();

    public ListenerManager<RoleChangePositionListener> addRoleChangePositionListener(RoleChangePositionListener var1);

    public List<RoleChangePositionListener> getRoleChangePositionListeners();

    public ListenerManager<RoleChangeMentionableListener> addRoleChangeMentionableListener(RoleChangeMentionableListener var1);

    public List<RoleChangeMentionableListener> getRoleChangeMentionableListeners();

    public ListenerManager<RoleChangeColorListener> addRoleChangeColorListener(RoleChangeColorListener var1);

    public List<RoleChangeColorListener> getRoleChangeColorListeners();

    public ListenerManager<RoleChangeNameListener> addRoleChangeNameListener(RoleChangeNameListener var1);

    public List<RoleChangeNameListener> getRoleChangeNameListeners();

    public ListenerManager<RoleChangeHoistListener> addRoleChangeHoistListener(RoleChangeHoistListener var1);

    public List<RoleChangeHoistListener> getRoleChangeHoistListeners();

    public ListenerManager<RoleCreateListener> addRoleCreateListener(RoleCreateListener var1);

    public List<RoleCreateListener> getRoleCreateListeners();

    public ListenerManager<RoleChangePermissionsListener> addRoleChangePermissionsListener(RoleChangePermissionsListener var1);

    public List<RoleChangePermissionsListener> getRoleChangePermissionsListeners();

    public ListenerManager<UserRoleRemoveListener> addUserRoleRemoveListener(UserRoleRemoveListener var1);

    public List<UserRoleRemoveListener> getUserRoleRemoveListeners();

    public ListenerManager<UserRoleAddListener> addUserRoleAddListener(UserRoleAddListener var1);

    public List<UserRoleAddListener> getUserRoleAddListeners();

    public ListenerManager<RoleDeleteListener> addRoleDeleteListener(RoleDeleteListener var1);

    public List<RoleDeleteListener> getRoleDeleteListeners();

    public ListenerManager<ServerChangeModeratorsOnlyChannelListener> addServerChangeModeratorsOnlyChannelListener(ServerChangeModeratorsOnlyChannelListener var1);

    public List<ServerChangeModeratorsOnlyChannelListener> getServerChangeModeratorsOnlyChannelListeners();

    public ListenerManager<ServerChangeNsfwLevelListener> addServerChangeNsfwLevelListener(ServerChangeNsfwLevelListener var1);

    public List<ServerChangeNsfwLevelListener> getServerChangeNsfwLevelListeners();

    public ListenerManager<ServerChannelChangePositionListener> addServerChannelChangePositionListener(ServerChannelChangePositionListener var1);

    public List<ServerChannelChangePositionListener> getServerChannelChangePositionListeners();

    public ListenerManager<ServerThreadListSyncListener> addServerThreadListSyncListener(ServerThreadListSyncListener var1);

    public List<ServerThreadListSyncListener> getServerThreadListSyncListeners();

    public ListenerManager<ServerThreadChannelUpdateListener> addServerThreadChannelUpdateListener(ServerThreadChannelUpdateListener var1);

    public List<ServerThreadChannelUpdateListener> getServerThreadChannelUpdateListeners();

    public ListenerManager<ServerThreadChannelMembersUpdateListener> addServerThreadChannelMembersUpdateListener(ServerThreadChannelMembersUpdateListener var1);

    public List<ServerThreadChannelMembersUpdateListener> getServerThreadChannelMembersUpdateListeners();

    public ListenerManager<WebhooksUpdateListener> addWebhooksUpdateListener(WebhooksUpdateListener var1);

    public List<WebhooksUpdateListener> getWebhooksUpdateListeners();

    public ListenerManager<ServerTextChannelChangeDefaultAutoArchiveDurationListener> addServerTextChannelChangeDefaultAutoArchiveDurationListener(ServerTextChannelChangeDefaultAutoArchiveDurationListener var1);

    public List<ServerTextChannelChangeDefaultAutoArchiveDurationListener> getServerTextChannelChangeDefaultAutoArchiveDurationListeners();

    public ListenerManager<ServerTextChannelChangeSlowmodeListener> addServerTextChannelChangeSlowmodeListener(ServerTextChannelChangeSlowmodeListener var1);

    public List<ServerTextChannelChangeSlowmodeListener> getServerTextChannelChangeSlowmodeListeners();

    public ListenerManager<ServerTextChannelChangeTopicListener> addServerTextChannelChangeTopicListener(ServerTextChannelChangeTopicListener var1);

    public List<ServerTextChannelChangeTopicListener> getServerTextChannelChangeTopicListeners();

    public ListenerManager<ServerChannelChangeOverwrittenPermissionsListener> addServerChannelChangeOverwrittenPermissionsListener(ServerChannelChangeOverwrittenPermissionsListener var1);

    public List<ServerChannelChangeOverwrittenPermissionsListener> getServerChannelChangeOverwrittenPermissionsListeners();

    public ListenerManager<ServerChannelInviteDeleteListener> addServerChannelInviteDeleteListener(ServerChannelInviteDeleteListener var1);

    public List<ServerChannelInviteDeleteListener> getServerChannelInviteDeleteListeners();

    public ListenerManager<ServerChannelInviteCreateListener> addServerChannelInviteCreateListener(ServerChannelInviteCreateListener var1);

    public List<ServerChannelInviteCreateListener> getServerChannelInviteCreateListeners();

    public ListenerManager<ServerChannelChangeNsfwFlagListener> addServerChannelChangeNsfwFlagListener(ServerChannelChangeNsfwFlagListener var1);

    public List<ServerChannelChangeNsfwFlagListener> getServerChannelChangeNsfwFlagListeners();

    public ListenerManager<ServerChannelDeleteListener> addServerChannelDeleteListener(ServerChannelDeleteListener var1);

    public List<ServerChannelDeleteListener> getServerChannelDeleteListeners();

    public ListenerManager<ServerChannelCreateListener> addServerChannelCreateListener(ServerChannelCreateListener var1);

    public List<ServerChannelCreateListener> getServerChannelCreateListeners();

    public ListenerManager<ServerStageVoiceChannelChangeTopicListener> addServerStageVoiceChannelChangeTopicListener(ServerStageVoiceChannelChangeTopicListener var1);

    public List<ServerStageVoiceChannelChangeTopicListener> getServerStageVoiceChannelChangeTopicListeners();

    public ListenerManager<ServerVoiceChannelChangeBitrateListener> addServerVoiceChannelChangeBitrateListener(ServerVoiceChannelChangeBitrateListener var1);

    public List<ServerVoiceChannelChangeBitrateListener> getServerVoiceChannelChangeBitrateListeners();

    public ListenerManager<ServerVoiceChannelChangeUserLimitListener> addServerVoiceChannelChangeUserLimitListener(ServerVoiceChannelChangeUserLimitListener var1);

    public List<ServerVoiceChannelChangeUserLimitListener> getServerVoiceChannelChangeUserLimitListeners();

    public ListenerManager<ServerVoiceChannelMemberLeaveListener> addServerVoiceChannelMemberLeaveListener(ServerVoiceChannelMemberLeaveListener var1);

    public List<ServerVoiceChannelMemberLeaveListener> getServerVoiceChannelMemberLeaveListeners();

    public ListenerManager<ServerVoiceChannelChangeNsfwListener> addServerVoiceChannelChangeNsfwListener(ServerVoiceChannelChangeNsfwListener var1);

    public List<ServerVoiceChannelChangeNsfwListener> getServerVoiceChannelChangeNsfwListeners();

    public ListenerManager<ServerVoiceChannelMemberJoinListener> addServerVoiceChannelMemberJoinListener(ServerVoiceChannelMemberJoinListener var1);

    public List<ServerVoiceChannelMemberJoinListener> getServerVoiceChannelMemberJoinListeners();

    public ListenerManager<ServerChannelChangeNameListener> addServerChannelChangeNameListener(ServerChannelChangeNameListener var1);

    public List<ServerChannelChangeNameListener> getServerChannelChangeNameListeners();

    public ListenerManager<UserChangeDeafenedListener> addUserChangeDeafenedListener(UserChangeDeafenedListener var1);

    public List<UserChangeDeafenedListener> getUserChangeDeafenedListeners();

    public ListenerManager<UserChangeNicknameListener> addUserChangeNicknameListener(UserChangeNicknameListener var1);

    public List<UserChangeNicknameListener> getUserChangeNicknameListeners();

    public ListenerManager<UserChangePendingListener> addUserChangePendingListener(UserChangePendingListener var1);

    public List<UserChangePendingListener> getUserChangePendingListeners();

    public ListenerManager<UserStartTypingListener> addUserStartTypingListener(UserStartTypingListener var1);

    public List<UserStartTypingListener> getUserStartTypingListeners();

    public ListenerManager<UserChangeDiscriminatorListener> addUserChangeDiscriminatorListener(UserChangeDiscriminatorListener var1);

    public List<UserChangeDiscriminatorListener> getUserChangeDiscriminatorListeners();

    public ListenerManager<UserChangeStatusListener> addUserChangeStatusListener(UserChangeStatusListener var1);

    public List<UserChangeStatusListener> getUserChangeStatusListeners();

    public ListenerManager<UserChangeServerAvatarListener> addUserChangeServerAvatarListener(UserChangeServerAvatarListener var1);

    public List<UserChangeServerAvatarListener> getUserChangeServerAvatarListeners();

    public ListenerManager<UserChangeSelfMutedListener> addUserChangeSelfMutedListener(UserChangeSelfMutedListener var1);

    public List<UserChangeSelfMutedListener> getUserChangeSelfMutedListeners();

    public ListenerManager<UserChangeNameListener> addUserChangeNameListener(UserChangeNameListener var1);

    public List<UserChangeNameListener> getUserChangeNameListeners();

    public ListenerManager<UserChangeTimeoutListener> addUserChangeTimeoutListener(UserChangeTimeoutListener var1);

    public List<UserChangeTimeoutListener> getUserChangeTimeoutListeners();

    public ListenerManager<UserChangeAvatarListener> addUserChangeAvatarListener(UserChangeAvatarListener var1);

    public List<UserChangeAvatarListener> getUserChangeAvatarListeners();

    public ListenerManager<UserChangeSelfDeafenedListener> addUserChangeSelfDeafenedListener(UserChangeSelfDeafenedListener var1);

    public List<UserChangeSelfDeafenedListener> getUserChangeSelfDeafenedListeners();

    public ListenerManager<UserChangeMutedListener> addUserChangeMutedListener(UserChangeMutedListener var1);

    public List<UserChangeMutedListener> getUserChangeMutedListeners();

    public ListenerManager<UserChangeActivityListener> addUserChangeActivityListener(UserChangeActivityListener var1);

    public List<UserChangeActivityListener> getUserChangeActivityListeners();

    public ListenerManager<MessageEditListener> addMessageEditListener(MessageEditListener var1);

    public List<MessageEditListener> getMessageEditListeners();

    public ListenerManager<ChannelPinsUpdateListener> addChannelPinsUpdateListener(ChannelPinsUpdateListener var1);

    public List<ChannelPinsUpdateListener> getChannelPinsUpdateListeners();

    public ListenerManager<ReactionRemoveListener> addReactionRemoveListener(ReactionRemoveListener var1);

    public List<ReactionRemoveListener> getReactionRemoveListeners();

    public ListenerManager<ReactionAddListener> addReactionAddListener(ReactionAddListener var1);

    public List<ReactionAddListener> getReactionAddListeners();

    public ListenerManager<ReactionRemoveAllListener> addReactionRemoveAllListener(ReactionRemoveAllListener var1);

    public List<ReactionRemoveAllListener> getReactionRemoveAllListeners();

    public ListenerManager<MessageCreateListener> addMessageCreateListener(MessageCreateListener var1);

    public List<MessageCreateListener> getMessageCreateListeners();

    public ListenerManager<CachedMessageUnpinListener> addCachedMessageUnpinListener(CachedMessageUnpinListener var1);

    public List<CachedMessageUnpinListener> getCachedMessageUnpinListeners();

    public ListenerManager<CachedMessagePinListener> addCachedMessagePinListener(CachedMessagePinListener var1);

    public List<CachedMessagePinListener> getCachedMessagePinListeners();

    public ListenerManager<MessageReplyListener> addMessageReplyListener(MessageReplyListener var1);

    public List<MessageReplyListener> getMessageReplyListeners();

    public ListenerManager<MessageDeleteListener> addMessageDeleteListener(MessageDeleteListener var1);

    public List<MessageDeleteListener> getMessageDeleteListeners();

    public <T extends ServerAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addServerAttachableListener(T var1);

    public <T extends ServerAttachableListener & ObjectAttachableListener> void removeServerAttachableListener(T var1);

    public <T extends ServerAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getServerAttachableListeners();

    public <T extends ServerAttachableListener & ObjectAttachableListener> void removeListener(Class<T> var1, T var2);
}

