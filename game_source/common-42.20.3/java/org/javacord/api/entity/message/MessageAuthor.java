/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message;

import java.awt.Color;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Nameable;
import org.javacord.api.entity.channel.Categorizable;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.Webhook;

public interface MessageAuthor
extends DiscordEntity,
Nameable {
    public Optional<Long> getWebhookId();

    public Message getMessage();

    default public String getDisplayName() {
        Optional<Server> server = this.getMessage().getServer();
        Optional<User> user = this.asUser();
        if (user.isPresent()) {
            return server.map(s -> ((User)user.get()).getDisplayName((Server)s)).orElseGet(() -> ((User)user.get()).getName());
        }
        return this.getName();
    }

    default public String getDiscriminatedName() {
        return this.getDiscriminator().map(discriminator -> this.getName() + "#" + discriminator).orElseGet(this::getName);
    }

    public Optional<String> getDiscriminator();

    public Icon getAvatar();

    public Icon getAvatar(int var1);

    default public Optional<ServerVoiceChannel> getConnectedVoiceChannel() {
        return this.getMessage().getServer().flatMap(server -> server.getConnectedVoiceChannel(this.getId()));
    }

    public boolean isUser();

    default public boolean isBotOwner() {
        return this.asUser().map(User::isBotOwner).orElse(false);
    }

    default public boolean isTeamMember() {
        return this.asUser().map(User::isTeamMember).orElse(false);
    }

    default public boolean isBotUser() {
        return this.asUser().map(User::isBot).orElse(false);
    }

    default public boolean isRegularUser() {
        return this.asUser().map(user -> !user.isBot()).orElse(false);
    }

    default public boolean canCreateChannelsOnServer() {
        return this.getMessage().getServer().flatMap(server -> this.asUser().map(server::canCreateChannels)).orElse(false);
    }

    default public boolean canViewAuditLogOfServer() {
        return this.getMessage().getServer().flatMap(server -> this.asUser().map(server::canViewAuditLog)).orElse(false);
    }

    default public boolean canChangeOwnNicknameOnServer() {
        return this.getMessage().getServer().flatMap(server -> this.asUser().map(server::canChangeOwnNickname)).orElse(false);
    }

    default public boolean canManageNicknamesOnServer() {
        return this.getMessage().getServer().flatMap(server -> this.asUser().map(server::canManageNicknames)).orElse(false);
    }

    default public boolean canMuteMembersOnServer() {
        return this.getMessage().getServer().flatMap(server -> this.asUser().map(server::canMuteMembers)).orElse(false);
    }

    default public boolean canDeafenMembersOnServer() {
        return this.getMessage().getServer().flatMap(server -> this.asUser().map(server::canDeafenMembers)).orElse(false);
    }

    default public boolean canMoveMembersOnServer() {
        return this.getMessage().getServer().flatMap(server -> this.asUser().map(server::canMoveMembers)).orElse(false);
    }

    default public boolean canManageEmojisOnServer() {
        return this.getMessage().getServer().flatMap(server -> this.asUser().map(server::canManageEmojis)).orElse(false);
    }

    default public boolean canManageRolesOnServer() {
        return this.getMessage().getServer().flatMap(server -> this.asUser().map(server::canManageRoles)).orElse(false);
    }

    default public boolean canManageServer() {
        return this.getMessage().getServer().flatMap(server -> this.asUser().map(server::canManage)).orElse(false);
    }

    default public boolean canKickUsersFromServer() {
        return this.getMessage().getServer().flatMap(server -> this.asUser().map(server::canKickUsers)).orElse(false);
    }

    default public boolean canKickUserFromServer(User userToKick) {
        return this.getMessage().getServer().flatMap(server -> this.asUser().map(user -> server.canKickUser((User)user, userToKick))).orElse(false);
    }

    default public boolean canBanUsersFromServer() {
        return this.getMessage().getServer().flatMap(server -> this.asUser().map(server::canBanUsers)).orElse(false);
    }

    default public boolean canBanUserFromServer(User userToBan) {
        return this.getMessage().getServer().flatMap(server -> this.asUser().map(user -> server.canBanUser((User)user, userToBan))).orElse(false);
    }

    default public boolean canSeeChannel() {
        return this.asUser().map(this.getMessage().getChannel()::canSee).orElse(false);
    }

    default public boolean canSeeAllChannelsInCategory() {
        return this.getMessage().getChannel().asCategorizable().flatMap(Categorizable::getCategory).map(channelCategory -> this.asUser().map(channelCategory::canSeeAll).orElse(false)).orElse(true);
    }

    default public boolean canCreateInstantInviteToTextChannel() {
        return this.getMessage().getChannel().asRegularServerChannel().flatMap(serverChannel -> this.asUser().map(serverChannel::canCreateInstantInvite)).orElse(false);
    }

    default public boolean canWriteInTextChannel() {
        return this.getMessage().getChannel().asTextChannel().map(textChannel -> this.asUser().map(textChannel::canWrite).orElse(false)).orElseThrow(AssertionError::new);
    }

    default public boolean canUseExternalEmojisInTextChannel() {
        return this.getMessage().getChannel().asTextChannel().map(textChannel -> this.asUser().map(textChannel::canUseExternalEmojis).orElse(false)).orElseThrow(AssertionError::new);
    }

    default public boolean canEmbedLinksInTextChannel() {
        return this.getMessage().getChannel().asTextChannel().map(textChannel -> this.asUser().map(textChannel::canEmbedLinks).orElse(false)).orElseThrow(AssertionError::new);
    }

    default public boolean canReadMessageHistoryOfTextChannel() {
        return this.getMessage().getChannel().asTextChannel().map(textChannel -> this.asUser().map(textChannel::canReadMessageHistory).orElse(false)).orElseThrow(AssertionError::new);
    }

    default public boolean canUseTtsInTextChannel() {
        return this.getMessage().getChannel().asTextChannel().map(textChannel -> this.asUser().map(textChannel::canUseTts).orElse(false)).orElseThrow(AssertionError::new);
    }

    default public boolean canAttachFilesToTextChannel() {
        return this.getMessage().getChannel().asTextChannel().map(textChannel -> this.asUser().map(textChannel::canAttachFiles).orElse(false)).orElseThrow(AssertionError::new);
    }

    default public boolean canAddNewReactionsInTextChannel() {
        return this.getMessage().getChannel().asTextChannel().map(textChannel -> this.asUser().map(textChannel::canAddNewReactions).orElse(false)).orElseThrow(AssertionError::new);
    }

    default public boolean canManageMessagesInTextChannel() {
        return this.getMessage().getChannel().asTextChannel().map(textChannel -> this.asUser().map(textChannel::canManageMessages).orElse(false)).orElseThrow(AssertionError::new);
    }

    default public boolean canRemoveReactionsOfOthersInTextChannel() {
        return this.getMessage().getChannel().asTextChannel().map(textChannel -> this.asUser().map(textChannel::canRemoveReactionsOfOthers).orElse(false)).orElseThrow(AssertionError::new);
    }

    default public boolean canMentionEveryoneInTextChannel() {
        return this.getMessage().getChannel().asTextChannel().map(textChannel -> this.asUser().map(textChannel::canMentionEveryone).orElse(false)).orElseThrow(AssertionError::new);
    }

    default public boolean canAddNewReactionsToMessage() {
        return this.asUser().map(this.getMessage()::canAddNewReactions).orElse(false);
    }

    default public boolean canDeleteMessage() {
        return this.asUser().map(this.getMessage()::canDelete).orElse(false);
    }

    default public Optional<Color> getRoleColor() {
        return this.getMessage().getServer().flatMap(server -> this.asUser().flatMap(server::getRoleColor));
    }

    default public boolean isServerAdmin() {
        return this.getMessage().getServer().flatMap(server -> this.asUser().map(server::isAdmin)).orElse(false);
    }

    public Optional<User> asUser();

    public boolean isWebhook();

    default public Optional<CompletableFuture<Webhook>> asWebhook() {
        if (this.isWebhook()) {
            return Optional.of(this.getApi().getWebhookById(this.getId()));
        }
        return Optional.empty();
    }

    default public boolean isYourself() {
        return this.asUser().map(User::isYourself).orElse(false);
    }
}

