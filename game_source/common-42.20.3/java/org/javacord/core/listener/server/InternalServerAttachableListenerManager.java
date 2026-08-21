/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.listener.server;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
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
import org.javacord.api.listener.server.ServerAttachableListenerManager;
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
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.ClassHelper;

public interface InternalServerAttachableListenerManager
extends ServerAttachableListenerManager {
    public DiscordApi getApi();

    public long getId();

    @Override
    default public ListenerManager<InteractionCreateListener> addInteractionCreateListener(InteractionCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), InteractionCreateListener.class, listener);
    }

    @Override
    default public List<InteractionCreateListener> getInteractionCreateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), InteractionCreateListener.class);
    }

    @Override
    default public ListenerManager<SlashCommandCreateListener> addSlashCommandCreateListener(SlashCommandCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), SlashCommandCreateListener.class, listener);
    }

    @Override
    default public List<SlashCommandCreateListener> getSlashCommandCreateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), SlashCommandCreateListener.class);
    }

    @Override
    default public ListenerManager<AutocompleteCreateListener> addAutocompleteCreateListener(AutocompleteCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), AutocompleteCreateListener.class, listener);
    }

    @Override
    default public List<AutocompleteCreateListener> getAutocompleteCreateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), AutocompleteCreateListener.class);
    }

    @Override
    default public ListenerManager<ModalSubmitListener> addModalSubmitListener(ModalSubmitListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ModalSubmitListener.class, listener);
    }

    @Override
    default public List<ModalSubmitListener> getModalSubmitListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ModalSubmitListener.class);
    }

    @Override
    default public ListenerManager<MessageContextMenuCommandListener> addMessageContextMenuCommandListener(MessageContextMenuCommandListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), MessageContextMenuCommandListener.class, listener);
    }

    @Override
    default public List<MessageContextMenuCommandListener> getMessageContextMenuCommandListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), MessageContextMenuCommandListener.class);
    }

    @Override
    default public ListenerManager<MessageComponentCreateListener> addMessageComponentCreateListener(MessageComponentCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), MessageComponentCreateListener.class, listener);
    }

    @Override
    default public List<MessageComponentCreateListener> getMessageComponentCreateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), MessageComponentCreateListener.class);
    }

    @Override
    default public ListenerManager<UserContextMenuCommandListener> addUserContextMenuCommandListener(UserContextMenuCommandListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), UserContextMenuCommandListener.class, listener);
    }

    @Override
    default public List<UserContextMenuCommandListener> getUserContextMenuCommandListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), UserContextMenuCommandListener.class);
    }

    @Override
    default public ListenerManager<SelectMenuChooseListener> addSelectMenuChooseListener(SelectMenuChooseListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), SelectMenuChooseListener.class, listener);
    }

    @Override
    default public List<SelectMenuChooseListener> getSelectMenuChooseListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), SelectMenuChooseListener.class);
    }

    @Override
    default public ListenerManager<ButtonClickListener> addButtonClickListener(ButtonClickListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ButtonClickListener.class, listener);
    }

    @Override
    default public List<ButtonClickListener> getButtonClickListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ButtonClickListener.class);
    }

    @Override
    default public ListenerManager<ServerChangeIconListener> addServerChangeIconListener(ServerChangeIconListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChangeIconListener.class, listener);
    }

    @Override
    default public List<ServerChangeIconListener> getServerChangeIconListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChangeIconListener.class);
    }

    @Override
    default public ListenerManager<ServerChangeNameListener> addServerChangeNameListener(ServerChangeNameListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChangeNameListener.class, listener);
    }

    @Override
    default public List<ServerChangeNameListener> getServerChangeNameListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChangeNameListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelChangeLastMessageIdListener> addServerThreadChannelChangeLastMessageIdListener(ServerThreadChannelChangeLastMessageIdListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerThreadChannelChangeLastMessageIdListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelChangeLastMessageIdListener> getServerThreadChannelChangeLastMessageIdListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerThreadChannelChangeLastMessageIdListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelChangeArchivedListener> addServerThreadChannelChangeArchivedListener(ServerThreadChannelChangeArchivedListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerThreadChannelChangeArchivedListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelChangeArchivedListener> getServerThreadChannelChangeArchivedListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerThreadChannelChangeArchivedListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelChangeMemberCountListener> addServerThreadChannelChangeMemberCountListener(ServerThreadChannelChangeMemberCountListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerThreadChannelChangeMemberCountListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelChangeMemberCountListener> getServerThreadChannelChangeMemberCountListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerThreadChannelChangeMemberCountListener.class);
    }

    @Override
    default public ListenerManager<ServerPrivateThreadJoinListener> addServerPrivateThreadJoinListener(ServerPrivateThreadJoinListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerPrivateThreadJoinListener.class, listener);
    }

    @Override
    default public List<ServerPrivateThreadJoinListener> getServerPrivateThreadJoinListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerPrivateThreadJoinListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelChangeInvitableListener> addServerThreadChannelChangeInvitableListener(ServerThreadChannelChangeInvitableListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerThreadChannelChangeInvitableListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelChangeInvitableListener> getServerThreadChannelChangeInvitableListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerThreadChannelChangeInvitableListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelChangeAutoArchiveDurationListener> addServerThreadChannelChangeAutoArchiveDurationListener(ServerThreadChannelChangeAutoArchiveDurationListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerThreadChannelChangeAutoArchiveDurationListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelChangeAutoArchiveDurationListener> getServerThreadChannelChangeAutoArchiveDurationListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerThreadChannelChangeAutoArchiveDurationListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelChangeRateLimitPerUserListener> addServerThreadChannelChangeRateLimitPerUserListener(ServerThreadChannelChangeRateLimitPerUserListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerThreadChannelChangeRateLimitPerUserListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelChangeRateLimitPerUserListener> getServerThreadChannelChangeRateLimitPerUserListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerThreadChannelChangeRateLimitPerUserListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelChangeLockedListener> addServerThreadChannelChangeLockedListener(ServerThreadChannelChangeLockedListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerThreadChannelChangeLockedListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelChangeLockedListener> getServerThreadChannelChangeLockedListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerThreadChannelChangeLockedListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelChangeArchiveTimestampListener> addServerThreadChannelChangeArchiveTimestampListener(ServerThreadChannelChangeArchiveTimestampListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerThreadChannelChangeArchiveTimestampListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelChangeArchiveTimestampListener> getServerThreadChannelChangeArchiveTimestampListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerThreadChannelChangeArchiveTimestampListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelChangeTotalMessageSentListener> addServerThreadChannelChangeTotalMessageSentListener(ServerThreadChannelChangeTotalMessageSentListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerThreadChannelChangeTotalMessageSentListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelChangeTotalMessageSentListener> getServerThreadChannelChangeTotalMessageSentListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerThreadChannelChangeTotalMessageSentListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelChangeMessageCountListener> addServerThreadChannelChangeMessageCountListener(ServerThreadChannelChangeMessageCountListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerThreadChannelChangeMessageCountListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelChangeMessageCountListener> getServerThreadChannelChangeMessageCountListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerThreadChannelChangeMessageCountListener.class);
    }

    @Override
    default public ListenerManager<ServerChangeAfkTimeoutListener> addServerChangeAfkTimeoutListener(ServerChangeAfkTimeoutListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChangeAfkTimeoutListener.class, listener);
    }

    @Override
    default public List<ServerChangeAfkTimeoutListener> getServerChangeAfkTimeoutListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChangeAfkTimeoutListener.class);
    }

    @Override
    default public ListenerManager<StickerChangeTagsListener> addStickerChangeTagsListener(StickerChangeTagsListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), StickerChangeTagsListener.class, listener);
    }

    @Override
    default public List<StickerChangeTagsListener> getStickerChangeTagsListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), StickerChangeTagsListener.class);
    }

    @Override
    default public ListenerManager<StickerChangeDescriptionListener> addStickerChangeDescriptionListener(StickerChangeDescriptionListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), StickerChangeDescriptionListener.class, listener);
    }

    @Override
    default public List<StickerChangeDescriptionListener> getStickerChangeDescriptionListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), StickerChangeDescriptionListener.class);
    }

    @Override
    default public ListenerManager<StickerCreateListener> addStickerCreateListener(StickerCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), StickerCreateListener.class, listener);
    }

    @Override
    default public List<StickerCreateListener> getStickerCreateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), StickerCreateListener.class);
    }

    @Override
    default public ListenerManager<StickerChangeNameListener> addStickerChangeNameListener(StickerChangeNameListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), StickerChangeNameListener.class, listener);
    }

    @Override
    default public List<StickerChangeNameListener> getStickerChangeNameListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), StickerChangeNameListener.class);
    }

    @Override
    default public ListenerManager<StickerDeleteListener> addStickerDeleteListener(StickerDeleteListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), StickerDeleteListener.class, listener);
    }

    @Override
    default public List<StickerDeleteListener> getStickerDeleteListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), StickerDeleteListener.class);
    }

    @Override
    default public ListenerManager<ServerChangeSplashListener> addServerChangeSplashListener(ServerChangeSplashListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChangeSplashListener.class, listener);
    }

    @Override
    default public List<ServerChangeSplashListener> getServerChangeSplashListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChangeSplashListener.class);
    }

    @Override
    default public ListenerManager<ServerChangeAfkChannelListener> addServerChangeAfkChannelListener(ServerChangeAfkChannelListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChangeAfkChannelListener.class, listener);
    }

    @Override
    default public List<ServerChangeAfkChannelListener> getServerChangeAfkChannelListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChangeAfkChannelListener.class);
    }

    @Override
    default public ListenerManager<ServerChangeVanityUrlCodeListener> addServerChangeVanityUrlCodeListener(ServerChangeVanityUrlCodeListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChangeVanityUrlCodeListener.class, listener);
    }

    @Override
    default public List<ServerChangeVanityUrlCodeListener> getServerChangeVanityUrlCodeListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChangeVanityUrlCodeListener.class);
    }

    @Override
    default public ListenerManager<ServerChangeDiscoverySplashListener> addServerChangeDiscoverySplashListener(ServerChangeDiscoverySplashListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChangeDiscoverySplashListener.class, listener);
    }

    @Override
    default public List<ServerChangeDiscoverySplashListener> getServerChangeDiscoverySplashListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChangeDiscoverySplashListener.class);
    }

    @Override
    default public ListenerManager<ApplicationCommandPermissionsUpdateListener> addApplicationCommandPermissionsUpdateListener(ApplicationCommandPermissionsUpdateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ApplicationCommandPermissionsUpdateListener.class, listener);
    }

    @Override
    default public List<ApplicationCommandPermissionsUpdateListener> getApplicationCommandPermissionsUpdateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ApplicationCommandPermissionsUpdateListener.class);
    }

    @Override
    default public ListenerManager<ServerBecomesUnavailableListener> addServerBecomesUnavailableListener(ServerBecomesUnavailableListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerBecomesUnavailableListener.class, listener);
    }

    @Override
    default public List<ServerBecomesUnavailableListener> getServerBecomesUnavailableListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerBecomesUnavailableListener.class);
    }

    @Override
    default public ListenerManager<VoiceServerUpdateListener> addVoiceServerUpdateListener(VoiceServerUpdateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), VoiceServerUpdateListener.class, listener);
    }

    @Override
    default public List<VoiceServerUpdateListener> getVoiceServerUpdateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), VoiceServerUpdateListener.class);
    }

    @Override
    default public ListenerManager<ServerChangeDescriptionListener> addServerChangeDescriptionListener(ServerChangeDescriptionListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChangeDescriptionListener.class, listener);
    }

    @Override
    default public List<ServerChangeDescriptionListener> getServerChangeDescriptionListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChangeDescriptionListener.class);
    }

    @Override
    default public ListenerManager<ServerChangeVerificationLevelListener> addServerChangeVerificationLevelListener(ServerChangeVerificationLevelListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChangeVerificationLevelListener.class, listener);
    }

    @Override
    default public List<ServerChangeVerificationLevelListener> getServerChangeVerificationLevelListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChangeVerificationLevelListener.class);
    }

    @Override
    default public ListenerManager<ServerLeaveListener> addServerLeaveListener(ServerLeaveListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerLeaveListener.class, listener);
    }

    @Override
    default public List<ServerLeaveListener> getServerLeaveListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerLeaveListener.class);
    }

    @Override
    default public ListenerManager<ServerChangeBoostCountListener> addServerChangeBoostCountListener(ServerChangeBoostCountListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChangeBoostCountListener.class, listener);
    }

    @Override
    default public List<ServerChangeBoostCountListener> getServerChangeBoostCountListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChangeBoostCountListener.class);
    }

    @Override
    default public ListenerManager<ServerChangeDefaultMessageNotificationLevelListener> addServerChangeDefaultMessageNotificationLevelListener(ServerChangeDefaultMessageNotificationLevelListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChangeDefaultMessageNotificationLevelListener.class, listener);
    }

    @Override
    default public List<ServerChangeDefaultMessageNotificationLevelListener> getServerChangeDefaultMessageNotificationLevelListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChangeDefaultMessageNotificationLevelListener.class);
    }

    @Override
    default public ListenerManager<ServerChangeRegionListener> addServerChangeRegionListener(ServerChangeRegionListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChangeRegionListener.class, listener);
    }

    @Override
    default public List<ServerChangeRegionListener> getServerChangeRegionListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChangeRegionListener.class);
    }

    @Override
    default public ListenerManager<ServerMemberJoinListener> addServerMemberJoinListener(ServerMemberJoinListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerMemberJoinListener.class, listener);
    }

    @Override
    default public List<ServerMemberJoinListener> getServerMemberJoinListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerMemberJoinListener.class);
    }

    @Override
    default public ListenerManager<ServerMemberLeaveListener> addServerMemberLeaveListener(ServerMemberLeaveListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerMemberLeaveListener.class, listener);
    }

    @Override
    default public List<ServerMemberLeaveListener> getServerMemberLeaveListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerMemberLeaveListener.class);
    }

    @Override
    default public ListenerManager<ServerMemberBanListener> addServerMemberBanListener(ServerMemberBanListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerMemberBanListener.class, listener);
    }

    @Override
    default public List<ServerMemberBanListener> getServerMemberBanListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerMemberBanListener.class);
    }

    @Override
    default public ListenerManager<ServerMembersChunkListener> addServerMembersChunkListener(ServerMembersChunkListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerMembersChunkListener.class, listener);
    }

    @Override
    default public List<ServerMembersChunkListener> getServerMembersChunkListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerMembersChunkListener.class);
    }

    @Override
    default public ListenerManager<ServerMemberUnbanListener> addServerMemberUnbanListener(ServerMemberUnbanListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerMemberUnbanListener.class, listener);
    }

    @Override
    default public List<ServerMemberUnbanListener> getServerMemberUnbanListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerMemberUnbanListener.class);
    }

    @Override
    default public ListenerManager<KnownCustomEmojiChangeNameListener> addKnownCustomEmojiChangeNameListener(KnownCustomEmojiChangeNameListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), KnownCustomEmojiChangeNameListener.class, listener);
    }

    @Override
    default public List<KnownCustomEmojiChangeNameListener> getKnownCustomEmojiChangeNameListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), KnownCustomEmojiChangeNameListener.class);
    }

    @Override
    default public ListenerManager<KnownCustomEmojiDeleteListener> addKnownCustomEmojiDeleteListener(KnownCustomEmojiDeleteListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), KnownCustomEmojiDeleteListener.class, listener);
    }

    @Override
    default public List<KnownCustomEmojiDeleteListener> getKnownCustomEmojiDeleteListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), KnownCustomEmojiDeleteListener.class);
    }

    @Override
    default public ListenerManager<KnownCustomEmojiChangeWhitelistedRolesListener> addKnownCustomEmojiChangeWhitelistedRolesListener(KnownCustomEmojiChangeWhitelistedRolesListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), KnownCustomEmojiChangeWhitelistedRolesListener.class, listener);
    }

    @Override
    default public List<KnownCustomEmojiChangeWhitelistedRolesListener> getKnownCustomEmojiChangeWhitelistedRolesListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), KnownCustomEmojiChangeWhitelistedRolesListener.class);
    }

    @Override
    default public ListenerManager<KnownCustomEmojiCreateListener> addKnownCustomEmojiCreateListener(KnownCustomEmojiCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), KnownCustomEmojiCreateListener.class, listener);
    }

    @Override
    default public List<KnownCustomEmojiCreateListener> getKnownCustomEmojiCreateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), KnownCustomEmojiCreateListener.class);
    }

    @Override
    default public ListenerManager<ServerChangeSystemChannelListener> addServerChangeSystemChannelListener(ServerChangeSystemChannelListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChangeSystemChannelListener.class, listener);
    }

    @Override
    default public List<ServerChangeSystemChannelListener> getServerChangeSystemChannelListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChangeSystemChannelListener.class);
    }

    @Override
    default public ListenerManager<ServerChangePreferredLocaleListener> addServerChangePreferredLocaleListener(ServerChangePreferredLocaleListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChangePreferredLocaleListener.class, listener);
    }

    @Override
    default public List<ServerChangePreferredLocaleListener> getServerChangePreferredLocaleListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChangePreferredLocaleListener.class);
    }

    @Override
    default public ListenerManager<ServerChangeBoostLevelListener> addServerChangeBoostLevelListener(ServerChangeBoostLevelListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChangeBoostLevelListener.class, listener);
    }

    @Override
    default public List<ServerChangeBoostLevelListener> getServerChangeBoostLevelListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChangeBoostLevelListener.class);
    }

    @Override
    default public ListenerManager<ServerChangeRulesChannelListener> addServerChangeRulesChannelListener(ServerChangeRulesChannelListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChangeRulesChannelListener.class, listener);
    }

    @Override
    default public List<ServerChangeRulesChannelListener> getServerChangeRulesChannelListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChangeRulesChannelListener.class);
    }

    @Override
    default public ListenerManager<ServerChangeServerFeatureListener> addServerChangeServerFeatureListener(ServerChangeServerFeatureListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChangeServerFeatureListener.class, listener);
    }

    @Override
    default public List<ServerChangeServerFeatureListener> getServerChangeServerFeatureListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChangeServerFeatureListener.class);
    }

    @Override
    default public ListenerManager<ServerChangeOwnerListener> addServerChangeOwnerListener(ServerChangeOwnerListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChangeOwnerListener.class, listener);
    }

    @Override
    default public List<ServerChangeOwnerListener> getServerChangeOwnerListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChangeOwnerListener.class);
    }

    @Override
    default public ListenerManager<ServerChangeMultiFactorAuthenticationLevelListener> addServerChangeMultiFactorAuthenticationLevelListener(ServerChangeMultiFactorAuthenticationLevelListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChangeMultiFactorAuthenticationLevelListener.class, listener);
    }

    @Override
    default public List<ServerChangeMultiFactorAuthenticationLevelListener> getServerChangeMultiFactorAuthenticationLevelListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChangeMultiFactorAuthenticationLevelListener.class);
    }

    @Override
    default public ListenerManager<ServerChangeExplicitContentFilterLevelListener> addServerChangeExplicitContentFilterLevelListener(ServerChangeExplicitContentFilterLevelListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChangeExplicitContentFilterLevelListener.class, listener);
    }

    @Override
    default public List<ServerChangeExplicitContentFilterLevelListener> getServerChangeExplicitContentFilterLevelListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChangeExplicitContentFilterLevelListener.class);
    }

    @Override
    default public ListenerManager<RoleChangePositionListener> addRoleChangePositionListener(RoleChangePositionListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), RoleChangePositionListener.class, listener);
    }

    @Override
    default public List<RoleChangePositionListener> getRoleChangePositionListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), RoleChangePositionListener.class);
    }

    @Override
    default public ListenerManager<RoleChangeMentionableListener> addRoleChangeMentionableListener(RoleChangeMentionableListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), RoleChangeMentionableListener.class, listener);
    }

    @Override
    default public List<RoleChangeMentionableListener> getRoleChangeMentionableListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), RoleChangeMentionableListener.class);
    }

    @Override
    default public ListenerManager<RoleChangeColorListener> addRoleChangeColorListener(RoleChangeColorListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), RoleChangeColorListener.class, listener);
    }

    @Override
    default public List<RoleChangeColorListener> getRoleChangeColorListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), RoleChangeColorListener.class);
    }

    @Override
    default public ListenerManager<RoleChangeNameListener> addRoleChangeNameListener(RoleChangeNameListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), RoleChangeNameListener.class, listener);
    }

    @Override
    default public List<RoleChangeNameListener> getRoleChangeNameListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), RoleChangeNameListener.class);
    }

    @Override
    default public ListenerManager<RoleChangeHoistListener> addRoleChangeHoistListener(RoleChangeHoistListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), RoleChangeHoistListener.class, listener);
    }

    @Override
    default public List<RoleChangeHoistListener> getRoleChangeHoistListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), RoleChangeHoistListener.class);
    }

    @Override
    default public ListenerManager<RoleCreateListener> addRoleCreateListener(RoleCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), RoleCreateListener.class, listener);
    }

    @Override
    default public List<RoleCreateListener> getRoleCreateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), RoleCreateListener.class);
    }

    @Override
    default public ListenerManager<RoleChangePermissionsListener> addRoleChangePermissionsListener(RoleChangePermissionsListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), RoleChangePermissionsListener.class, listener);
    }

    @Override
    default public List<RoleChangePermissionsListener> getRoleChangePermissionsListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), RoleChangePermissionsListener.class);
    }

    @Override
    default public ListenerManager<UserRoleRemoveListener> addUserRoleRemoveListener(UserRoleRemoveListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), UserRoleRemoveListener.class, listener);
    }

    @Override
    default public List<UserRoleRemoveListener> getUserRoleRemoveListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), UserRoleRemoveListener.class);
    }

    @Override
    default public ListenerManager<UserRoleAddListener> addUserRoleAddListener(UserRoleAddListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), UserRoleAddListener.class, listener);
    }

    @Override
    default public List<UserRoleAddListener> getUserRoleAddListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), UserRoleAddListener.class);
    }

    @Override
    default public ListenerManager<RoleDeleteListener> addRoleDeleteListener(RoleDeleteListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), RoleDeleteListener.class, listener);
    }

    @Override
    default public List<RoleDeleteListener> getRoleDeleteListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), RoleDeleteListener.class);
    }

    @Override
    default public ListenerManager<ServerChangeModeratorsOnlyChannelListener> addServerChangeModeratorsOnlyChannelListener(ServerChangeModeratorsOnlyChannelListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChangeModeratorsOnlyChannelListener.class, listener);
    }

    @Override
    default public List<ServerChangeModeratorsOnlyChannelListener> getServerChangeModeratorsOnlyChannelListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChangeModeratorsOnlyChannelListener.class);
    }

    @Override
    default public ListenerManager<ServerChangeNsfwLevelListener> addServerChangeNsfwLevelListener(ServerChangeNsfwLevelListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChangeNsfwLevelListener.class, listener);
    }

    @Override
    default public List<ServerChangeNsfwLevelListener> getServerChangeNsfwLevelListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChangeNsfwLevelListener.class);
    }

    @Override
    default public ListenerManager<ServerChannelChangePositionListener> addServerChannelChangePositionListener(ServerChannelChangePositionListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChannelChangePositionListener.class, listener);
    }

    @Override
    default public List<ServerChannelChangePositionListener> getServerChannelChangePositionListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChannelChangePositionListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadListSyncListener> addServerThreadListSyncListener(ServerThreadListSyncListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerThreadListSyncListener.class, listener);
    }

    @Override
    default public List<ServerThreadListSyncListener> getServerThreadListSyncListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerThreadListSyncListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelUpdateListener> addServerThreadChannelUpdateListener(ServerThreadChannelUpdateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerThreadChannelUpdateListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelUpdateListener> getServerThreadChannelUpdateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerThreadChannelUpdateListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelMembersUpdateListener> addServerThreadChannelMembersUpdateListener(ServerThreadChannelMembersUpdateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerThreadChannelMembersUpdateListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelMembersUpdateListener> getServerThreadChannelMembersUpdateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerThreadChannelMembersUpdateListener.class);
    }

    @Override
    default public ListenerManager<WebhooksUpdateListener> addWebhooksUpdateListener(WebhooksUpdateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), WebhooksUpdateListener.class, listener);
    }

    @Override
    default public List<WebhooksUpdateListener> getWebhooksUpdateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), WebhooksUpdateListener.class);
    }

    @Override
    default public ListenerManager<ServerTextChannelChangeDefaultAutoArchiveDurationListener> addServerTextChannelChangeDefaultAutoArchiveDurationListener(ServerTextChannelChangeDefaultAutoArchiveDurationListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerTextChannelChangeDefaultAutoArchiveDurationListener.class, listener);
    }

    @Override
    default public List<ServerTextChannelChangeDefaultAutoArchiveDurationListener> getServerTextChannelChangeDefaultAutoArchiveDurationListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerTextChannelChangeDefaultAutoArchiveDurationListener.class);
    }

    @Override
    default public ListenerManager<ServerTextChannelChangeSlowmodeListener> addServerTextChannelChangeSlowmodeListener(ServerTextChannelChangeSlowmodeListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerTextChannelChangeSlowmodeListener.class, listener);
    }

    @Override
    default public List<ServerTextChannelChangeSlowmodeListener> getServerTextChannelChangeSlowmodeListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerTextChannelChangeSlowmodeListener.class);
    }

    @Override
    default public ListenerManager<ServerTextChannelChangeTopicListener> addServerTextChannelChangeTopicListener(ServerTextChannelChangeTopicListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerTextChannelChangeTopicListener.class, listener);
    }

    @Override
    default public List<ServerTextChannelChangeTopicListener> getServerTextChannelChangeTopicListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerTextChannelChangeTopicListener.class);
    }

    @Override
    default public ListenerManager<ServerChannelChangeOverwrittenPermissionsListener> addServerChannelChangeOverwrittenPermissionsListener(ServerChannelChangeOverwrittenPermissionsListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChannelChangeOverwrittenPermissionsListener.class, listener);
    }

    @Override
    default public List<ServerChannelChangeOverwrittenPermissionsListener> getServerChannelChangeOverwrittenPermissionsListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChannelChangeOverwrittenPermissionsListener.class);
    }

    @Override
    default public ListenerManager<ServerChannelInviteDeleteListener> addServerChannelInviteDeleteListener(ServerChannelInviteDeleteListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChannelInviteDeleteListener.class, listener);
    }

    @Override
    default public List<ServerChannelInviteDeleteListener> getServerChannelInviteDeleteListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChannelInviteDeleteListener.class);
    }

    @Override
    default public ListenerManager<ServerChannelInviteCreateListener> addServerChannelInviteCreateListener(ServerChannelInviteCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChannelInviteCreateListener.class, listener);
    }

    @Override
    default public List<ServerChannelInviteCreateListener> getServerChannelInviteCreateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChannelInviteCreateListener.class);
    }

    @Override
    default public ListenerManager<ServerChannelChangeNsfwFlagListener> addServerChannelChangeNsfwFlagListener(ServerChannelChangeNsfwFlagListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChannelChangeNsfwFlagListener.class, listener);
    }

    @Override
    default public List<ServerChannelChangeNsfwFlagListener> getServerChannelChangeNsfwFlagListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChannelChangeNsfwFlagListener.class);
    }

    @Override
    default public ListenerManager<ServerChannelDeleteListener> addServerChannelDeleteListener(ServerChannelDeleteListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChannelDeleteListener.class, listener);
    }

    @Override
    default public List<ServerChannelDeleteListener> getServerChannelDeleteListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChannelDeleteListener.class);
    }

    @Override
    default public ListenerManager<ServerChannelCreateListener> addServerChannelCreateListener(ServerChannelCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChannelCreateListener.class, listener);
    }

    @Override
    default public List<ServerChannelCreateListener> getServerChannelCreateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChannelCreateListener.class);
    }

    @Override
    default public ListenerManager<ServerStageVoiceChannelChangeTopicListener> addServerStageVoiceChannelChangeTopicListener(ServerStageVoiceChannelChangeTopicListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerStageVoiceChannelChangeTopicListener.class, listener);
    }

    @Override
    default public List<ServerStageVoiceChannelChangeTopicListener> getServerStageVoiceChannelChangeTopicListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerStageVoiceChannelChangeTopicListener.class);
    }

    @Override
    default public ListenerManager<ServerVoiceChannelChangeBitrateListener> addServerVoiceChannelChangeBitrateListener(ServerVoiceChannelChangeBitrateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerVoiceChannelChangeBitrateListener.class, listener);
    }

    @Override
    default public List<ServerVoiceChannelChangeBitrateListener> getServerVoiceChannelChangeBitrateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerVoiceChannelChangeBitrateListener.class);
    }

    @Override
    default public ListenerManager<ServerVoiceChannelChangeUserLimitListener> addServerVoiceChannelChangeUserLimitListener(ServerVoiceChannelChangeUserLimitListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerVoiceChannelChangeUserLimitListener.class, listener);
    }

    @Override
    default public List<ServerVoiceChannelChangeUserLimitListener> getServerVoiceChannelChangeUserLimitListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerVoiceChannelChangeUserLimitListener.class);
    }

    @Override
    default public ListenerManager<ServerVoiceChannelMemberLeaveListener> addServerVoiceChannelMemberLeaveListener(ServerVoiceChannelMemberLeaveListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerVoiceChannelMemberLeaveListener.class, listener);
    }

    @Override
    default public List<ServerVoiceChannelMemberLeaveListener> getServerVoiceChannelMemberLeaveListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerVoiceChannelMemberLeaveListener.class);
    }

    @Override
    default public ListenerManager<ServerVoiceChannelChangeNsfwListener> addServerVoiceChannelChangeNsfwListener(ServerVoiceChannelChangeNsfwListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerVoiceChannelChangeNsfwListener.class, listener);
    }

    @Override
    default public List<ServerVoiceChannelChangeNsfwListener> getServerVoiceChannelChangeNsfwListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerVoiceChannelChangeNsfwListener.class);
    }

    @Override
    default public ListenerManager<ServerVoiceChannelMemberJoinListener> addServerVoiceChannelMemberJoinListener(ServerVoiceChannelMemberJoinListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerVoiceChannelMemberJoinListener.class, listener);
    }

    @Override
    default public List<ServerVoiceChannelMemberJoinListener> getServerVoiceChannelMemberJoinListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerVoiceChannelMemberJoinListener.class);
    }

    @Override
    default public ListenerManager<ServerChannelChangeNameListener> addServerChannelChangeNameListener(ServerChannelChangeNameListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ServerChannelChangeNameListener.class, listener);
    }

    @Override
    default public List<ServerChannelChangeNameListener> getServerChannelChangeNameListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ServerChannelChangeNameListener.class);
    }

    @Override
    default public ListenerManager<UserChangeDeafenedListener> addUserChangeDeafenedListener(UserChangeDeafenedListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), UserChangeDeafenedListener.class, listener);
    }

    @Override
    default public List<UserChangeDeafenedListener> getUserChangeDeafenedListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), UserChangeDeafenedListener.class);
    }

    @Override
    default public ListenerManager<UserChangeNicknameListener> addUserChangeNicknameListener(UserChangeNicknameListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), UserChangeNicknameListener.class, listener);
    }

    @Override
    default public List<UserChangeNicknameListener> getUserChangeNicknameListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), UserChangeNicknameListener.class);
    }

    @Override
    default public ListenerManager<UserChangePendingListener> addUserChangePendingListener(UserChangePendingListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), UserChangePendingListener.class, listener);
    }

    @Override
    default public List<UserChangePendingListener> getUserChangePendingListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), UserChangePendingListener.class);
    }

    @Override
    default public ListenerManager<UserStartTypingListener> addUserStartTypingListener(UserStartTypingListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), UserStartTypingListener.class, listener);
    }

    @Override
    default public List<UserStartTypingListener> getUserStartTypingListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), UserStartTypingListener.class);
    }

    @Override
    default public ListenerManager<UserChangeDiscriminatorListener> addUserChangeDiscriminatorListener(UserChangeDiscriminatorListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), UserChangeDiscriminatorListener.class, listener);
    }

    @Override
    default public List<UserChangeDiscriminatorListener> getUserChangeDiscriminatorListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), UserChangeDiscriminatorListener.class);
    }

    @Override
    default public ListenerManager<UserChangeStatusListener> addUserChangeStatusListener(UserChangeStatusListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), UserChangeStatusListener.class, listener);
    }

    @Override
    default public List<UserChangeStatusListener> getUserChangeStatusListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), UserChangeStatusListener.class);
    }

    @Override
    default public ListenerManager<UserChangeServerAvatarListener> addUserChangeServerAvatarListener(UserChangeServerAvatarListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), UserChangeServerAvatarListener.class, listener);
    }

    @Override
    default public List<UserChangeServerAvatarListener> getUserChangeServerAvatarListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), UserChangeServerAvatarListener.class);
    }

    @Override
    default public ListenerManager<UserChangeSelfMutedListener> addUserChangeSelfMutedListener(UserChangeSelfMutedListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), UserChangeSelfMutedListener.class, listener);
    }

    @Override
    default public List<UserChangeSelfMutedListener> getUserChangeSelfMutedListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), UserChangeSelfMutedListener.class);
    }

    @Override
    default public ListenerManager<UserChangeNameListener> addUserChangeNameListener(UserChangeNameListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), UserChangeNameListener.class, listener);
    }

    @Override
    default public List<UserChangeNameListener> getUserChangeNameListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), UserChangeNameListener.class);
    }

    @Override
    default public ListenerManager<UserChangeTimeoutListener> addUserChangeTimeoutListener(UserChangeTimeoutListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), UserChangeTimeoutListener.class, listener);
    }

    @Override
    default public List<UserChangeTimeoutListener> getUserChangeTimeoutListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), UserChangeTimeoutListener.class);
    }

    @Override
    default public ListenerManager<UserChangeAvatarListener> addUserChangeAvatarListener(UserChangeAvatarListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), UserChangeAvatarListener.class, listener);
    }

    @Override
    default public List<UserChangeAvatarListener> getUserChangeAvatarListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), UserChangeAvatarListener.class);
    }

    @Override
    default public ListenerManager<UserChangeSelfDeafenedListener> addUserChangeSelfDeafenedListener(UserChangeSelfDeafenedListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), UserChangeSelfDeafenedListener.class, listener);
    }

    @Override
    default public List<UserChangeSelfDeafenedListener> getUserChangeSelfDeafenedListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), UserChangeSelfDeafenedListener.class);
    }

    @Override
    default public ListenerManager<UserChangeMutedListener> addUserChangeMutedListener(UserChangeMutedListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), UserChangeMutedListener.class, listener);
    }

    @Override
    default public List<UserChangeMutedListener> getUserChangeMutedListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), UserChangeMutedListener.class);
    }

    @Override
    default public ListenerManager<UserChangeActivityListener> addUserChangeActivityListener(UserChangeActivityListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), UserChangeActivityListener.class, listener);
    }

    @Override
    default public List<UserChangeActivityListener> getUserChangeActivityListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), UserChangeActivityListener.class);
    }

    @Override
    default public ListenerManager<MessageEditListener> addMessageEditListener(MessageEditListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), MessageEditListener.class, listener);
    }

    @Override
    default public List<MessageEditListener> getMessageEditListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), MessageEditListener.class);
    }

    @Override
    default public ListenerManager<ChannelPinsUpdateListener> addChannelPinsUpdateListener(ChannelPinsUpdateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ChannelPinsUpdateListener.class, listener);
    }

    @Override
    default public List<ChannelPinsUpdateListener> getChannelPinsUpdateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ChannelPinsUpdateListener.class);
    }

    @Override
    default public ListenerManager<ReactionRemoveListener> addReactionRemoveListener(ReactionRemoveListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ReactionRemoveListener.class, listener);
    }

    @Override
    default public List<ReactionRemoveListener> getReactionRemoveListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ReactionRemoveListener.class);
    }

    @Override
    default public ListenerManager<ReactionAddListener> addReactionAddListener(ReactionAddListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ReactionAddListener.class, listener);
    }

    @Override
    default public List<ReactionAddListener> getReactionAddListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ReactionAddListener.class);
    }

    @Override
    default public ListenerManager<ReactionRemoveAllListener> addReactionRemoveAllListener(ReactionRemoveAllListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), ReactionRemoveAllListener.class, listener);
    }

    @Override
    default public List<ReactionRemoveAllListener> getReactionRemoveAllListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), ReactionRemoveAllListener.class);
    }

    @Override
    default public ListenerManager<MessageCreateListener> addMessageCreateListener(MessageCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), MessageCreateListener.class, listener);
    }

    @Override
    default public List<MessageCreateListener> getMessageCreateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), MessageCreateListener.class);
    }

    @Override
    default public ListenerManager<CachedMessageUnpinListener> addCachedMessageUnpinListener(CachedMessageUnpinListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), CachedMessageUnpinListener.class, listener);
    }

    @Override
    default public List<CachedMessageUnpinListener> getCachedMessageUnpinListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), CachedMessageUnpinListener.class);
    }

    @Override
    default public ListenerManager<CachedMessagePinListener> addCachedMessagePinListener(CachedMessagePinListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), CachedMessagePinListener.class, listener);
    }

    @Override
    default public List<CachedMessagePinListener> getCachedMessagePinListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), CachedMessagePinListener.class);
    }

    @Override
    default public ListenerManager<MessageReplyListener> addMessageReplyListener(MessageReplyListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), MessageReplyListener.class, listener);
    }

    @Override
    default public List<MessageReplyListener> getMessageReplyListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), MessageReplyListener.class);
    }

    @Override
    default public ListenerManager<MessageDeleteListener> addMessageDeleteListener(MessageDeleteListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), MessageDeleteListener.class, listener);
    }

    @Override
    default public List<MessageDeleteListener> getMessageDeleteListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId(), MessageDeleteListener.class);
    }

    @Override
    default public <T extends ServerAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addServerAttachableListener(T listener) {
        return ClassHelper.getInterfacesAsStream(listener.getClass()).filter(ServerAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).map(listenerClass -> ((DiscordApiImpl)this.getApi()).addObjectListener(Server.class, this.getId(), listenerClass, listener)).collect(Collectors.toList());
    }

    @Override
    default public <T extends ServerAttachableListener & ObjectAttachableListener> void removeServerAttachableListener(T listener) {
        ClassHelper.getInterfacesAsStream(listener.getClass()).filter(ServerAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).forEach(listenerClass -> ((DiscordApiImpl)this.getApi()).removeObjectListener(Server.class, this.getId(), listenerClass, listener));
    }

    @Override
    default public <T extends ServerAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getServerAttachableListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Server.class, this.getId());
    }

    @Override
    default public <T extends ServerAttachableListener & ObjectAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        ((DiscordApiImpl)this.getApi()).removeObjectListener(Server.class, this.getId(), listenerClass, listener);
    }
}

