/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.util.internal;

import java.util.Iterator;
import java.util.ServiceLoader;
import org.javacord.api.DiscordApi;
import org.javacord.api.audio.internal.AudioSourceBaseDelegate;
import org.javacord.api.entity.channel.RegularServerChannel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerForumChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextableRegularServerChannel;
import org.javacord.api.entity.channel.internal.ChannelCategoryBuilderDelegate;
import org.javacord.api.entity.channel.internal.RegularServerChannelUpdaterDelegate;
import org.javacord.api.entity.channel.internal.ServerChannelUpdaterDelegate;
import org.javacord.api.entity.channel.internal.ServerForumChannelBuilderDelegate;
import org.javacord.api.entity.channel.internal.ServerForumChannelUpdaterDelegate;
import org.javacord.api.entity.channel.internal.ServerTextChannelBuilderDelegate;
import org.javacord.api.entity.channel.internal.ServerTextChannelUpdaterDelegate;
import org.javacord.api.entity.channel.internal.ServerThreadChannelBuilderDelegate;
import org.javacord.api.entity.channel.internal.ServerThreadChannelUpdaterDelegate;
import org.javacord.api.entity.channel.internal.ServerVoiceChannelBuilderDelegate;
import org.javacord.api.entity.channel.internal.ServerVoiceChannelUpdaterDelegate;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.emoji.internal.CustomEmojiBuilderDelegate;
import org.javacord.api.entity.emoji.internal.CustomEmojiUpdaterDelegate;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.component.internal.ActionRowBuilderDelegate;
import org.javacord.api.entity.message.component.internal.ButtonBuilderDelegate;
import org.javacord.api.entity.message.component.internal.SelectMenuBuilderDelegate;
import org.javacord.api.entity.message.component.internal.SelectMenuOptionBuilderDelegate;
import org.javacord.api.entity.message.component.internal.TextInputBuilderDelegate;
import org.javacord.api.entity.message.embed.internal.EmbedBuilderDelegate;
import org.javacord.api.entity.message.internal.InteractionMessageBuilderDelegate;
import org.javacord.api.entity.message.internal.MessageBuilderBaseDelegate;
import org.javacord.api.entity.message.internal.WebhookMessageBuilderDelegate;
import org.javacord.api.entity.message.mention.internal.AllowedMentionsBuilderDelegate;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.permission.internal.PermissionsBuilderDelegate;
import org.javacord.api.entity.permission.internal.RoleBuilderDelegate;
import org.javacord.api.entity.permission.internal.RoleUpdaterDelegate;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.internal.ServerBuilderDelegate;
import org.javacord.api.entity.server.internal.ServerUpdaterDelegate;
import org.javacord.api.entity.server.invite.internal.InviteBuilderDelegate;
import org.javacord.api.entity.sticker.internal.StickerBuilderDelegate;
import org.javacord.api.entity.sticker.internal.StickerUpdaterDelegate;
import org.javacord.api.entity.webhook.Webhook;
import org.javacord.api.entity.webhook.internal.WebhookBuilderDelegate;
import org.javacord.api.entity.webhook.internal.WebhookUpdaterDelegate;
import org.javacord.api.interaction.internal.MessageContextMenuBuilderDelegate;
import org.javacord.api.interaction.internal.MessageContextMenuUpdaterDelegate;
import org.javacord.api.interaction.internal.SlashCommandBuilderDelegate;
import org.javacord.api.interaction.internal.SlashCommandOptionBuilderDelegate;
import org.javacord.api.interaction.internal.SlashCommandOptionChoiceBuilderDelegate;
import org.javacord.api.interaction.internal.SlashCommandUpdaterDelegate;
import org.javacord.api.interaction.internal.UserContextMenuBuilderDelegate;
import org.javacord.api.interaction.internal.UserContextMenuUpdaterDelegate;
import org.javacord.api.internal.AccountUpdaterDelegate;
import org.javacord.api.internal.DiscordApiBuilderDelegate;
import org.javacord.api.util.exception.DiscordExceptionValidator;
import org.javacord.api.util.internal.DelegateFactoryDelegate;
import org.javacord.api.util.logging.internal.ExceptionLoggerDelegate;

public class DelegateFactory {
    private static final DelegateFactoryDelegate delegateFactoryDelegate;
    private static final ExceptionLoggerDelegate exceptionLoggerDelegate;
    private static final DiscordExceptionValidator discordExceptionValidator;

    private DelegateFactory() {
        throw new UnsupportedOperationException();
    }

    public static DiscordApiBuilderDelegate createDiscordApiBuilderDelegate() {
        return delegateFactoryDelegate.createDiscordApiBuilderDelegate();
    }

    public static EmbedBuilderDelegate createEmbedBuilderDelegate() {
        return delegateFactoryDelegate.createEmbedBuilderDelegate();
    }

    public static ActionRowBuilderDelegate createActionRowBuilderDelegate() {
        return delegateFactoryDelegate.createActionRowBuilderDelegate();
    }

    public static ButtonBuilderDelegate createButtonBuilderDelegate() {
        return delegateFactoryDelegate.createButtonBuilderDelegate();
    }

    public static TextInputBuilderDelegate createTextInputBuilderDelegate() {
        return delegateFactoryDelegate.createTextInputBuilderDelegate();
    }

    public static SelectMenuBuilderDelegate createSelectMenuBuilderDelegate() {
        return delegateFactoryDelegate.createSelectMenuBuilderDelegate();
    }

    public static SelectMenuOptionBuilderDelegate createSelectMenuOptionBuilderDelegate() {
        return delegateFactoryDelegate.createSelectMenuOptionBuilderDelegate();
    }

    public static AllowedMentionsBuilderDelegate createAllowedMentionsBuilderDelegate() {
        return delegateFactoryDelegate.createAllowedMentionsBuilderDelegate();
    }

    public static MessageBuilderBaseDelegate createMessageBuilderBaseDelegate() {
        return delegateFactoryDelegate.createMessageBuilderDelegate();
    }

    public static InteractionMessageBuilderDelegate createInteractionMessageBuilderDelegate() {
        return delegateFactoryDelegate.createInteractionMessageBuilderDelegate();
    }

    public static WebhookMessageBuilderDelegate createWebhookMessageBuilderDelegate() {
        return delegateFactoryDelegate.createWebhookMessageBuilderDelegate();
    }

    public static PermissionsBuilderDelegate createPermissionsBuilderDelegate() {
        return delegateFactoryDelegate.createPermissionsBuilderDelegate();
    }

    public static PermissionsBuilderDelegate createPermissionsBuilderDelegate(Permissions permissions) {
        return delegateFactoryDelegate.createPermissionsBuilderDelegate(permissions);
    }

    public static ChannelCategoryBuilderDelegate createChannelCategoryBuilderDelegate(Server server) {
        return delegateFactoryDelegate.createChannelCategoryBuilderDelegate(server);
    }

    public static ServerTextChannelBuilderDelegate createServerTextChannelBuilderDelegate(Server server) {
        return delegateFactoryDelegate.createServerTextChannelBuilderDelegate(server);
    }

    public static ServerForumChannelBuilderDelegate createServerForumChannelBuilderDelegate(Server server) {
        return delegateFactoryDelegate.createServerForumChannelBuilderDelegate(server);
    }

    public static ServerThreadChannelBuilderDelegate createServerThreadChannelBuilderDelegate(ServerTextChannel serverTextChannel) {
        return delegateFactoryDelegate.createServerThreadChannelBuilderDelegate(serverTextChannel);
    }

    public static ServerThreadChannelBuilderDelegate createServerThreadChannelBuilderDelegate(Message message) {
        return delegateFactoryDelegate.createServerThreadChannelBuilderDelegate(message);
    }

    public static ServerVoiceChannelBuilderDelegate createServerVoiceChannelBuilderDelegate(Server server) {
        return delegateFactoryDelegate.createServerVoiceChannelBuilderDelegate(server);
    }

    public static CustomEmojiBuilderDelegate createCustomEmojiBuilderDelegate(Server server) {
        return delegateFactoryDelegate.createCustomEmojiBuilderDelegate(server);
    }

    public static WebhookBuilderDelegate createWebhookBuilderDelegate(TextableRegularServerChannel channel) {
        return delegateFactoryDelegate.createWebhookBuilderDelegate(channel);
    }

    public static ServerBuilderDelegate createServerBuilderDelegate(DiscordApi api) {
        return delegateFactoryDelegate.createServerBuilderDelegate(api);
    }

    public static RoleBuilderDelegate createRoleBuilderDelegate(Server server) {
        return delegateFactoryDelegate.createRoleBuilderDelegate(server);
    }

    public static InviteBuilderDelegate createInviteBuilderDelegate(ServerChannel channel) {
        return delegateFactoryDelegate.createInviteBuilderDelegate(channel);
    }

    public static AccountUpdaterDelegate createAccountUpdaterDelegate(DiscordApi api) {
        return delegateFactoryDelegate.createAccountUpdaterDelegate(api);
    }

    public static SlashCommandUpdaterDelegate createSlashCommandUpdaterDelegate(long commandId) {
        return delegateFactoryDelegate.createSlashCommandUpdaterDelegate(commandId);
    }

    public static UserContextMenuUpdaterDelegate createUserContextMenuUpdaterDelegate(long commandId) {
        return delegateFactoryDelegate.createUserContextMenuUpdaterDelegate(commandId);
    }

    public static MessageContextMenuUpdaterDelegate createMessageContextMenuUpdaterDelegate(long commandId) {
        return delegateFactoryDelegate.createMessageContextMenuUpdaterDelegate(commandId);
    }

    public static ServerChannelUpdaterDelegate createServerChannelUpdaterDelegate(ServerChannel channel) {
        return delegateFactoryDelegate.createServerChannelUpdaterDelegate(channel);
    }

    public static RegularServerChannelUpdaterDelegate createRegularServerChannelUpdaterDelegate(RegularServerChannel channel) {
        return delegateFactoryDelegate.createRegularServerChannelUpdaterDelegate(channel);
    }

    public static ServerTextChannelUpdaterDelegate createServerTextChannelUpdaterDelegate(ServerTextChannel channel) {
        return delegateFactoryDelegate.createServerTextChannelUpdaterDelegate(channel);
    }

    public static ServerForumChannelUpdaterDelegate createServerForumChannelUpdaterDelegate(ServerForumChannel channel) {
        return delegateFactoryDelegate.createServerForumChannelUpdaterDelegate(channel);
    }

    public static ServerVoiceChannelUpdaterDelegate createServerVoiceChannelUpdaterDelegate(ServerVoiceChannel channel) {
        return delegateFactoryDelegate.createServerVoiceChannelUpdaterDelegate(channel);
    }

    public static ServerThreadChannelUpdaterDelegate createServerThreadChannelUpdaterDelegate(ServerThreadChannel thread2) {
        return delegateFactoryDelegate.createServerThreadChannelUpdaterDelegate(thread2);
    }

    public static CustomEmojiUpdaterDelegate createCustomEmojiUpdaterDelegate(KnownCustomEmoji emoji) {
        return delegateFactoryDelegate.createCustomEmojiUpdaterDelegate(emoji);
    }

    public static RoleUpdaterDelegate createRoleUpdaterDelegate(Role role) {
        return delegateFactoryDelegate.createRoleUpdaterDelegate(role);
    }

    public static ServerUpdaterDelegate createServerUpdaterDelegate(Server server) {
        return delegateFactoryDelegate.createServerUpdaterDelegate(server);
    }

    public static WebhookUpdaterDelegate createWebhookUpdaterDelegate(Webhook webhook) {
        return delegateFactoryDelegate.createWebhookUpdaterDelegate(webhook);
    }

    public static AudioSourceBaseDelegate createAudioSourceBaseDelegate(DiscordApi api) {
        return delegateFactoryDelegate.createAudioSourceBaseDelegate(api);
    }

    public static SlashCommandBuilderDelegate createSlashCommandBuilderDelegate() {
        return delegateFactoryDelegate.createSlashCommandBuilderDelegate();
    }

    public static UserContextMenuBuilderDelegate createUserContextMenuBuilderDelegate() {
        return delegateFactoryDelegate.createUserContextMenuBuilderDelegate();
    }

    public static MessageContextMenuBuilderDelegate createMessageContextMenuBuilderDelegate() {
        return delegateFactoryDelegate.createMessageContextMenuBuilderDelegate();
    }

    public static SlashCommandOptionBuilderDelegate createSlashCommandOptionBuilderDelegate() {
        return delegateFactoryDelegate.createSlashCommandOptionBuilderDelegate();
    }

    public static SlashCommandOptionChoiceBuilderDelegate createSlashCommandOptionChoiceBuilderDelegate() {
        return delegateFactoryDelegate.createSlashCommandOptionChoiceBuilderDelegate();
    }

    public static StickerBuilderDelegate createStickerBuilderDelegate(Server server) {
        return delegateFactoryDelegate.createStickerBuilderDelegate(server);
    }

    public static StickerUpdaterDelegate createStickerUpdaterDelegate(Server server, long id) {
        return delegateFactoryDelegate.createStickerUpdaterDelegate(server, id);
    }

    public static ExceptionLoggerDelegate getExceptionLoggerDelegate() {
        return exceptionLoggerDelegate;
    }

    public static DiscordExceptionValidator getDiscordExceptionValidator() {
        return discordExceptionValidator;
    }

    static {
        ServiceLoader<DelegateFactoryDelegate> delegateServiceLoader = ServiceLoader.load(DelegateFactoryDelegate.class, DelegateFactory.class.getClassLoader());
        Iterator<DelegateFactoryDelegate> delegateIterator = delegateServiceLoader.iterator();
        if (delegateIterator.hasNext()) {
            delegateFactoryDelegate = delegateIterator.next();
            if (delegateIterator.hasNext()) {
                throw new IllegalStateException("Found more than one DelegateFactoryDelegate implementation!");
            }
        } else {
            throw new IllegalStateException("No DelegateFactoryDelegate implementation was found!");
        }
        exceptionLoggerDelegate = delegateFactoryDelegate.createExceptionLoggerDelegate();
        discordExceptionValidator = delegateFactoryDelegate.createDiscordExceptionValidator();
    }
}

