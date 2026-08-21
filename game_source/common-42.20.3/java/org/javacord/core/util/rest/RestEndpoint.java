/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.rest;

import java.util.Optional;
import okhttp3.HttpUrl;

public enum RestEndpoint {
    GATEWAY("/gateway"),
    GATEWAY_BOT("/gateway/bot"),
    MESSAGE("/channels/%s/messages", 0),
    MESSAGE_DELETE("/channels/%s/messages", 0),
    MESSAGES_BULK_DELETE("/channels/%s/messages/bulk-delete", 0),
    CHANNEL_TYPING("/channels/%s/typing", 0),
    CHANNEL_INVITE("/channels/%s/invites", 0),
    USER("/users/%s"),
    USER_CHANNEL("/users/@me/channels"),
    CHANNEL("/channels/%s", 0),
    ROLE("/guilds/%s/roles", 0),
    SERVER("/guilds"),
    SERVER_PRUNE("/guilds/%s/prune", 0),
    SERVER_SELF("/users/@me/guilds/%s", 0),
    SERVER_CHANNEL("/guilds/%s/channels", 0),
    REACTION("/channels/%s/messages/%s/reactions", 0),
    PINS("/channels/%s/pins", 0),
    SERVER_MEMBER("/guilds/%s/members/%s", 0),
    SERVER_MEMBER_ROLE("/guilds/%s/members/%s/roles/%s", 0),
    OWN_NICKNAME("/guilds/%s/members/@me/nick", 0),
    SELF_INFO("/oauth2/applications/@me"),
    CHANNEL_WEBHOOK("/channels/%s/webhooks", 0),
    SERVER_WEBHOOK("/guilds/%s/webhooks", 0),
    SERVER_INVITE("/guilds/%s/invites", 0),
    WEBHOOK("/webhooks/%s", 0),
    WEBHOOK_SEND("/webhooks/%s/%s", 0),
    WEBHOOK_MESSAGE("/webhooks/%s/%s/messages/%s", 0),
    INVITE("/invites/%s"),
    BAN("/guilds/%s/bans", 0),
    CURRENT_USER("/users/@me"),
    AUDIT_LOG("/guilds/%s/audit-logs", 0),
    CUSTOM_EMOJI("/guilds/%s/emojis", 0),
    STICKER("/stickers"),
    STICKER_PACK("/sticker-packs"),
    SERVER_STICKER("/guilds/%s/stickers", 0),
    INTERACTION_RESPONSE("/interactions/%s/%s/callback"),
    ORIGINAL_INTERACTION_RESPONSE("/webhooks/%s/%s/messages/@original"),
    APPLICATION_COMMANDS("/applications/%s/commands"),
    SERVER_APPLICATION_COMMANDS("/applications/%s/guilds/%s/commands", 0),
    SERVER_APPLICATION_COMMAND_PERMISSIONS("/applications/%s/guilds/%s/commands/permissions", 0),
    APPLICATION_COMMAND_PERMISSIONS("/applications/%s/guilds/%s/commands/%s/permissions", 0),
    START_THREAD_WITH_MESSAGE("/channels/%s/messages/%s/threads", 0),
    START_THREAD_WITHOUT_MESSAGE("/channels/%s/threads", 0),
    JOIN_LEAVE_THREAD("/channels/%s/thread-members/@me", 0),
    ADD_REMOVE_THREAD_MEMBER("/channels/%s/thread-members/%s", 0),
    LIST_THREAD_MEMBERS("/channels/%s/thread-members", 0),
    LIST_ACTIVE_THREADS("/guilds/%s/threads/active", 0),
    LIST_PUBLIC_ARCHIVED_THREADS("/channels/%s/threads/archived/public", 0),
    LIST_PRIVATE_ARCHIVED_THREADS("/channels/%s/threads/archived/private", 0),
    LIST_JOINED_PRIVATE_ARCHIVED_THREADS("/channels/%s/users/@me/threads/archived/private", 0),
    THREAD_MEMBER("/channels/%s/thread-members/%s", 0);

    private final String endpointUrl;
    private final int majorParameterPosition;

    private RestEndpoint(String endpointUrl) {
        this(endpointUrl, -1);
    }

    private RestEndpoint(String endpointUrl, int majorParameterPosition) {
        this.endpointUrl = endpointUrl;
        this.majorParameterPosition = majorParameterPosition;
    }

    public Optional<Integer> getMajorParameterPosition() {
        if (this.majorParameterPosition >= 0) {
            return Optional.of(this.majorParameterPosition);
        }
        return Optional.empty();
    }

    public String getEndpointUrl() {
        return this.endpointUrl;
    }

    public String getFullUrl(String ... parameters) {
        StringBuilder url = new StringBuilder("https://discord.com/api/v10" + this.getEndpointUrl());
        url = new StringBuilder(String.format(url.toString(), parameters));
        int parameterAmount = this.getEndpointUrl().split("%s").length - (this.getEndpointUrl().endsWith("%s") ? 0 : 1);
        if (parameters.length > parameterAmount) {
            for (int i = parameterAmount; i < parameters.length; ++i) {
                url.append("/").append(parameters[i]);
            }
        }
        return url.toString();
    }

    public HttpUrl getOkHttpUrl(String ... parameters) {
        return HttpUrl.parse(this.getFullUrl(parameters));
    }
}

