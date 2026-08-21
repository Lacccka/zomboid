/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.util.internal;

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
import org.javacord.api.util.logging.internal.ExceptionLoggerDelegate;

public interface DelegateFactoryDelegate {
    public DiscordApiBuilderDelegate createDiscordApiBuilderDelegate();

    public EmbedBuilderDelegate createEmbedBuilderDelegate();

    public AllowedMentionsBuilderDelegate createAllowedMentionsBuilderDelegate();

    public MessageBuilderBaseDelegate createMessageBuilderDelegate();

    public InteractionMessageBuilderDelegate createInteractionMessageBuilderDelegate();

    public WebhookMessageBuilderDelegate createWebhookMessageBuilderDelegate();

    public PermissionsBuilderDelegate createPermissionsBuilderDelegate();

    public PermissionsBuilderDelegate createPermissionsBuilderDelegate(Permissions var1);

    public ChannelCategoryBuilderDelegate createChannelCategoryBuilderDelegate(Server var1);

    public ServerTextChannelBuilderDelegate createServerTextChannelBuilderDelegate(Server var1);

    public ServerForumChannelBuilderDelegate createServerForumChannelBuilderDelegate(Server var1);

    public ServerThreadChannelBuilderDelegate createServerThreadChannelBuilderDelegate(ServerTextChannel var1);

    public ServerThreadChannelBuilderDelegate createServerThreadChannelBuilderDelegate(Message var1);

    public ServerVoiceChannelBuilderDelegate createServerVoiceChannelBuilderDelegate(Server var1);

    public CustomEmojiBuilderDelegate createCustomEmojiBuilderDelegate(Server var1);

    public WebhookBuilderDelegate createWebhookBuilderDelegate(TextableRegularServerChannel var1);

    public ServerBuilderDelegate createServerBuilderDelegate(DiscordApi var1);

    public RoleBuilderDelegate createRoleBuilderDelegate(Server var1);

    public InviteBuilderDelegate createInviteBuilderDelegate(ServerChannel var1);

    public AccountUpdaterDelegate createAccountUpdaterDelegate(DiscordApi var1);

    public SlashCommandUpdaterDelegate createSlashCommandUpdaterDelegate(long var1);

    public UserContextMenuUpdaterDelegate createUserContextMenuUpdaterDelegate(long var1);

    public MessageContextMenuUpdaterDelegate createMessageContextMenuUpdaterDelegate(long var1);

    public ServerChannelUpdaterDelegate createServerChannelUpdaterDelegate(ServerChannel var1);

    public RegularServerChannelUpdaterDelegate createRegularServerChannelUpdaterDelegate(RegularServerChannel var1);

    public ServerTextChannelUpdaterDelegate createServerTextChannelUpdaterDelegate(ServerTextChannel var1);

    public ServerForumChannelUpdaterDelegate createServerForumChannelUpdaterDelegate(ServerForumChannel var1);

    public ServerVoiceChannelUpdaterDelegate createServerVoiceChannelUpdaterDelegate(ServerVoiceChannel var1);

    public ServerThreadChannelUpdaterDelegate createServerThreadChannelUpdaterDelegate(ServerThreadChannel var1);

    public CustomEmojiUpdaterDelegate createCustomEmojiUpdaterDelegate(KnownCustomEmoji var1);

    public RoleUpdaterDelegate createRoleUpdaterDelegate(Role var1);

    public ServerUpdaterDelegate createServerUpdaterDelegate(Server var1);

    public WebhookUpdaterDelegate createWebhookUpdaterDelegate(Webhook var1);

    public AudioSourceBaseDelegate createAudioSourceBaseDelegate(DiscordApi var1);

    public ExceptionLoggerDelegate createExceptionLoggerDelegate();

    public DiscordExceptionValidator createDiscordExceptionValidator();

    public SlashCommandBuilderDelegate createSlashCommandBuilderDelegate();

    public UserContextMenuBuilderDelegate createUserContextMenuBuilderDelegate();

    public MessageContextMenuBuilderDelegate createMessageContextMenuBuilderDelegate();

    public SlashCommandOptionBuilderDelegate createSlashCommandOptionBuilderDelegate();

    public SlashCommandOptionChoiceBuilderDelegate createSlashCommandOptionChoiceBuilderDelegate();

    public ActionRowBuilderDelegate createActionRowBuilderDelegate();

    public ButtonBuilderDelegate createButtonBuilderDelegate();

    public TextInputBuilderDelegate createTextInputBuilderDelegate();

    public SelectMenuBuilderDelegate createSelectMenuBuilderDelegate();

    public SelectMenuOptionBuilderDelegate createSelectMenuOptionBuilderDelegate();

    public StickerBuilderDelegate createStickerBuilderDelegate(Server var1);

    public StickerUpdaterDelegate createStickerUpdaterDelegate(Server var1, long var2);
}

