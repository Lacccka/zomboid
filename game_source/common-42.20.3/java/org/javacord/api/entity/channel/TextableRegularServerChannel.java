/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.Categorizable;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.WebhookBuilder;

public interface TextableRegularServerChannel
extends TextChannel,
Mentionable,
Categorizable {
    @Override
    default public boolean canWrite(User user) {
        return this.asRegularServerChannel().map(regularServerChannel -> regularServerChannel.hasPermission(user, PermissionType.ADMINISTRATOR) || regularServerChannel.hasPermissions(user, PermissionType.VIEW_CHANNEL, PermissionType.SEND_MESSAGES)).orElse(false);
    }

    @Override
    default public boolean canUseExternalEmojis(User user) {
        if (!this.canWrite(user)) {
            return false;
        }
        return this.asRegularServerChannel().map(regularServerChannel -> regularServerChannel.hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.USE_EXTERNAL_EMOJIS)).orElse(false);
    }

    @Override
    default public boolean canEmbedLinks(User user) {
        if (!this.canWrite(user)) {
            return false;
        }
        return this.asRegularServerChannel().map(regularServerChannel -> regularServerChannel.hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.EMBED_LINKS)).orElse(false);
    }

    @Override
    default public boolean canReadMessageHistory(User user) {
        if (!this.canSee(user)) {
            return false;
        }
        return this.asRegularServerChannel().map(regularServerChannel -> regularServerChannel.hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.READ_MESSAGE_HISTORY)).orElse(false);
    }

    @Override
    default public boolean canUseTts(User user) {
        if (!this.canWrite(user)) {
            return false;
        }
        return this.asRegularServerChannel().map(regularServerChannel -> regularServerChannel.hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.READ_MESSAGE_HISTORY)).orElse(false);
    }

    @Override
    default public boolean canAttachFiles(User user) {
        return this.asRegularServerChannel().map(regularServerChannel -> regularServerChannel.hasPermission(user, PermissionType.ADMINISTRATOR) || regularServerChannel.hasPermission(user, PermissionType.ATTACH_FILE) && this.canWrite(user)).orElse(false);
    }

    @Override
    default public boolean canAddNewReactions(User user) {
        return this.asRegularServerChannel().map(regularServerChannel -> regularServerChannel.hasPermission(user, PermissionType.ADMINISTRATOR) || regularServerChannel.hasPermissions(user, PermissionType.VIEW_CHANNEL, PermissionType.READ_MESSAGE_HISTORY, PermissionType.ADD_REACTIONS)).orElse(false);
    }

    @Override
    default public boolean canManageMessages(User user) {
        if (!this.canSee(user)) {
            return false;
        }
        return this.asRegularServerChannel().map(regularServerChannel -> regularServerChannel.hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.MANAGE_MESSAGES)).orElse(false);
    }

    @Override
    default public boolean canMentionEveryone(User user) {
        if (!this.canSee(user)) {
            return false;
        }
        return this.asRegularServerChannel().map(regularServerChannel -> regularServerChannel.hasPermission(user, PermissionType.ADMINISTRATOR) || regularServerChannel.hasPermission(user, PermissionType.MENTION_EVERYONE) && this.canWrite(user)).orElse(false);
    }

    @Override
    default public String getMentionTag() {
        return "<#" + this.getIdAsString() + ">";
    }

    public boolean isNsfw();

    default public WebhookBuilder createWebhookBuilder() {
        return new WebhookBuilder(this);
    }
}

