/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.stream.Stream;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Deletable;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.UpdatableFromCache;
import org.javacord.api.entity.channel.AutoArchiveDuration;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.MessageActivity;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.MessageReference;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.message.MessageType;
import org.javacord.api.entity.message.MessageUpdater;
import org.javacord.api.entity.message.Reaction;
import org.javacord.api.entity.message.WebhookMessageBuilder;
import org.javacord.api.entity.message.component.HighLevelComponent;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.sticker.StickerItem;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.MessageInteraction;
import org.javacord.api.listener.message.MessageAttachableListenerManager;
import org.javacord.api.util.DiscordRegexPattern;

public interface Message
extends DiscordEntity,
Deletable,
Comparable<Message>,
UpdatableFromCache<Message>,
MessageAttachableListenerManager {
    default public MessageBuilder toMessageBuilder() {
        return MessageBuilder.fromMessage(this);
    }

    default public WebhookMessageBuilder toWebhookMessageBuilder() {
        return WebhookMessageBuilder.fromMessage(this);
    }

    public static CompletableFuture<Message> crossPost(DiscordApi api, long channelId, long messageId) {
        return api.getUncachedMessageUtil().crossPost(channelId, messageId);
    }

    default public CompletableFuture<Message> crossPost() {
        return Message.crossPost(this.getApi(), this.getChannel().getId(), this.getId());
    }

    public static CompletableFuture<Void> delete(DiscordApi api, long channelId, long messageId) {
        return api.getUncachedMessageUtil().delete(channelId, messageId);
    }

    public static CompletableFuture<Void> delete(DiscordApi api, String channelId, String messageId) {
        return api.getUncachedMessageUtil().delete(channelId, messageId, (String)null);
    }

    public static CompletableFuture<Void> delete(DiscordApi api, long channelId, long messageId, String reason) {
        return api.getUncachedMessageUtil().delete(channelId, messageId, reason);
    }

    public static CompletableFuture<Void> delete(DiscordApi api, String channelId, String messageId, String reason) {
        return api.getUncachedMessageUtil().delete(channelId, messageId, reason);
    }

    @Override
    default public CompletableFuture<Void> delete(String reason) {
        return Message.delete(this.getApi(), this.getChannel().getId(), this.getId(), reason);
    }

    public static CompletableFuture<Void> delete(DiscordApi api, long channelId, long ... messageIds) {
        return api.getUncachedMessageUtil().delete(channelId, messageIds);
    }

    public static CompletableFuture<Void> delete(DiscordApi api, String channelId, String ... messageIds) {
        return api.getUncachedMessageUtil().delete(channelId, messageIds);
    }

    public static CompletableFuture<Void> delete(DiscordApi api, Message ... messages) {
        return api.getUncachedMessageUtil().delete(messages);
    }

    public static CompletableFuture<Void> delete(DiscordApi api, Iterable<Message> messages) {
        return api.getUncachedMessageUtil().delete(messages);
    }

    public static CompletableFuture<Message> edit(DiscordApi api, long channelId, long messageId, String content) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, content);
    }

    public static CompletableFuture<Message> edit(DiscordApi api, String channelId, String messageId, String content) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, content, true, Collections.emptyList(), false);
    }

    public static CompletableFuture<Message> edit(DiscordApi api, long channelId, long messageId, EmbedBuilder ... embeds) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, null, false, Arrays.asList(embeds), true);
    }

    public static CompletableFuture<Message> edit(DiscordApi api, long channelId, long messageId, List<EmbedBuilder> embeds) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, null, false, embeds, true);
    }

    public static CompletableFuture<Message> edit(DiscordApi api, String channelId, String messageId, EmbedBuilder ... embeds) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, null, false, Arrays.asList(embeds), true);
    }

    public static CompletableFuture<Message> edit(DiscordApi api, String channelId, String messageId, List<EmbedBuilder> embeds) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, null, false, embeds, true);
    }

    public static CompletableFuture<Message> edit(DiscordApi api, long channelId, long messageId, String content, EmbedBuilder ... embeds) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, content, true, Arrays.asList(embeds), true);
    }

    public static CompletableFuture<Message> edit(DiscordApi api, long channelId, long messageId, String content, List<EmbedBuilder> embeds) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, content, true, embeds, true);
    }

    public static CompletableFuture<Message> edit(DiscordApi api, String channelId, String messageId, String content, EmbedBuilder ... embeds) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, content, true, Arrays.asList(embeds), true);
    }

    public static CompletableFuture<Message> edit(DiscordApi api, String channelId, String messageId, String content, List<EmbedBuilder> embeds) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, content, true, embeds, true);
    }

    public static CompletableFuture<Message> edit(DiscordApi api, long channelId, long messageId, String content, boolean updateContent, List<EmbedBuilder> embeds, boolean updateEmbed) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, content, updateContent, embeds, updateEmbed);
    }

    public static CompletableFuture<Message> edit(DiscordApi api, String channelId, String messageId, String content, boolean updateContent, List<EmbedBuilder> embeds, boolean updateEmbed) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, content, updateContent, embeds, updateEmbed);
    }

    default public CompletableFuture<Message> edit(String content) {
        return ((MessageUpdater)new MessageUpdater(this).setContent(content)).applyChanges();
    }

    default public CompletableFuture<Message> edit(EmbedBuilder ... embeds) {
        return ((MessageUpdater)new MessageUpdater(this).addEmbeds((List)Arrays.asList(embeds))).applyChanges();
    }

    default public CompletableFuture<Message> edit(List<EmbedBuilder> embeds) {
        return ((MessageUpdater)new MessageUpdater(this).addEmbeds((List)embeds)).applyChanges();
    }

    default public CompletableFuture<Message> edit(String content, EmbedBuilder ... embeds) {
        return ((MessageUpdater)((MessageUpdater)new MessageUpdater(this).setContent(content)).addEmbeds(embeds)).applyChanges();
    }

    default public CompletableFuture<Message> edit(String content, List<EmbedBuilder> embeds) {
        return ((MessageUpdater)((MessageUpdater)new MessageUpdater(this).setContent(content)).addEmbeds((List)embeds)).applyChanges();
    }

    default public MessageUpdater createUpdater() {
        return new MessageUpdater(this);
    }

    public static CompletableFuture<Message> removeContent(DiscordApi api, long channelId, long messageId) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, "");
    }

    public static CompletableFuture<Message> removeContent(DiscordApi api, String channelId, String messageId) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, null, true, Collections.emptyList(), false);
    }

    default public CompletableFuture<Message> removeContent() {
        return ((MessageUpdater)new MessageUpdater(this).setContent("")).applyChanges();
    }

    public static CompletableFuture<Message> removeEmbed(DiscordApi api, long channelId, long messageId) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, null, false, Collections.emptyList(), true);
    }

    public static CompletableFuture<Message> removeEmbed(DiscordApi api, String channelId, String messageId) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, null, false, Collections.emptyList(), true);
    }

    default public CompletableFuture<Message> removeEmbed() {
        return ((MessageUpdater)new MessageUpdater(this).addEmbeds(Collections.emptyList())).applyChanges();
    }

    public static CompletableFuture<Message> removeContentAndEmbed(DiscordApi api, long channelId, long messageId) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, null, true, Collections.emptyList(), true);
    }

    public static CompletableFuture<Message> removeContentAndEmbed(DiscordApi api, String channelId, String messageId) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, null, true, Collections.emptyList(), true);
    }

    default public CompletableFuture<Message> removeContentAndEmbed() {
        return ((MessageUpdater)((MessageUpdater)new MessageUpdater(this).setContent("")).addEmbeds(Collections.emptyList())).applyChanges();
    }

    public static CompletableFuture<Void> addReaction(DiscordApi api, long channelId, long messageId, String unicodeEmoji) {
        return api.getUncachedMessageUtil().addReaction(channelId, messageId, unicodeEmoji);
    }

    public static CompletableFuture<Void> addReaction(DiscordApi api, String channelId, String messageId, String unicodeEmoji) {
        return api.getUncachedMessageUtil().addReaction(channelId, messageId, unicodeEmoji);
    }

    default public CompletableFuture<Void> addReaction(Emoji emoji) {
        return Message.addReaction(this.getApi(), this.getChannel().getId(), this.getId(), emoji);
    }

    public static CompletableFuture<Void> addReaction(DiscordApi api, long channelId, long messageId, Emoji emoji) {
        return api.getUncachedMessageUtil().addReaction(channelId, messageId, emoji);
    }

    default public CompletableFuture<Void> addReaction(String unicodeEmoji) {
        return Message.addReaction(this.getApi(), this.getChannel().getId(), this.getId(), unicodeEmoji);
    }

    public static CompletableFuture<Void> addReaction(DiscordApi api, String channelId, String messageId, Emoji emoji) {
        return api.getUncachedMessageUtil().addReaction(channelId, messageId, emoji);
    }

    public static CompletableFuture<Void> removeAllReactions(DiscordApi api, long channelId, long messageId) {
        return api.getUncachedMessageUtil().removeAllReactions(channelId, messageId);
    }

    public static CompletableFuture<Void> removeAllReactions(DiscordApi api, String channelId, String messageId) {
        return api.getUncachedMessageUtil().removeAllReactions(channelId, messageId);
    }

    default public CompletableFuture<Void> removeAllReactions() {
        return Message.removeAllReactions(this.getApi(), this.getChannel().getId(), this.getId());
    }

    public static CompletableFuture<Void> pin(DiscordApi api, long channelId, long messageId) {
        return api.getUncachedMessageUtil().pin(channelId, messageId);
    }

    public static CompletableFuture<Void> pin(DiscordApi api, String channelId, String messageId) {
        return api.getUncachedMessageUtil().pin(channelId, messageId);
    }

    default public CompletableFuture<Void> pin() {
        return Message.pin(this.getApi(), this.getChannel().getId(), this.getId());
    }

    public static CompletableFuture<Void> unpin(DiscordApi api, long channelId, long messageId) {
        return api.getUncachedMessageUtil().unpin(channelId, messageId);
    }

    public static CompletableFuture<Void> unpin(DiscordApi api, String channelId, String messageId) {
        return api.getUncachedMessageUtil().unpin(channelId, messageId);
    }

    default public CompletableFuture<Void> unpin() {
        return Message.unpin(this.getApi(), this.getChannel().getId(), this.getId());
    }

    public boolean canYouReadContent();

    public String getContent();

    public Optional<Instant> getLastEditTimestamp();

    public List<MessageAttachment> getAttachments();

    default public String getReadableContent() {
        return this.getApi().makeMentionsReadable(this.getContent(), this.getServer().orElse(null));
    }

    default public URL getLink() throws AssertionError {
        try {
            return new URL("https://discord.com/channels/" + this.getServer().map(DiscordEntity::getIdAsString).orElse("@me") + "/" + this.getChannel().getIdAsString() + "/" + this.getIdAsString());
        }
        catch (MalformedURLException e) {
            throw new AssertionError("Message link is malformed", e);
        }
    }

    public List<CustomEmoji> getCustomEmojis();

    public MessageType getType();

    public TextChannel getChannel();

    public Optional<MessageActivity> getActivity();

    public EnumSet<MessageFlag> getFlags();

    public boolean isPinned();

    public boolean isTts();

    public boolean mentionsEveryone();

    public List<Embed> getEmbeds();

    public Optional<User> getUserAuthor();

    public MessageAuthor getAuthor();

    public Optional<MessageReference> getMessageReference();

    public Optional<Message> getReferencedMessage();

    default public Optional<CompletableFuture<Message>> requestReferencedMessage() {
        return this.getReferencedMessage().map(message -> this.getReferencedMessage().map(CompletableFuture::completedFuture).orElseGet(() -> this.getApi().getMessageById(message.getId(), this.getChannel())));
    }

    public boolean isCachedForever();

    public void setCachedForever(boolean var1);

    public List<Reaction> getReactions();

    public Optional<MessageInteraction> getMessageInteraction();

    public List<HighLevelComponent> getComponents();

    public List<User> getMentionedUsers();

    public List<Role> getMentionedRoles();

    public Optional<String> getNonce();

    public Set<StickerItem> getStickerItems();

    public Optional<Integer> getPosition();

    default public List<ServerChannel> getMentionedChannels() {
        ArrayList mentionedChannels = new ArrayList();
        Matcher channelMention = DiscordRegexPattern.CHANNEL_MENTION.matcher(this.getContent());
        while (channelMention.find()) {
            String channelId = channelMention.group("id");
            this.getApi().getServerChannelById(channelId).filter(channel -> !mentionedChannels.contains(channel)).ifPresent(mentionedChannels::add);
        }
        return Collections.unmodifiableList(mentionedChannels);
    }

    default public boolean isPrivateMessage() {
        return this.getChannel().getType() == ChannelType.PRIVATE_CHANNEL;
    }

    default public boolean isServerMessage() {
        return this.getChannel().getType() == ChannelType.SERVER_TEXT_CHANNEL;
    }

    default public boolean isThreadMessage() {
        return this.getChannel().getType() == ChannelType.SERVER_PRIVATE_THREAD || this.getChannel().getType() == ChannelType.SERVER_PUBLIC_THREAD || this.getChannel().getType() == ChannelType.SERVER_NEWS_THREAD;
    }

    default public Optional<Reaction> getReactionByEmoji(Emoji emoji) {
        return this.getReactions().stream().filter(reaction -> reaction.getEmoji().equals(emoji)).findAny();
    }

    default public Optional<Reaction> getReactionByEmoji(String unicodeEmoji) {
        return this.getReactions().stream().filter(reaction -> unicodeEmoji.equals(reaction.getEmoji().asUnicodeEmoji().orElse(null))).findAny();
    }

    default public CompletableFuture<Void> addReactions(Emoji ... emojis) {
        return CompletableFuture.allOf((CompletableFuture[])Arrays.stream(emojis).map(this::addReaction).toArray(CompletableFuture[]::new));
    }

    public CompletableFuture<Void> addReactions(String ... var1);

    default public CompletableFuture<Void> removeReactionByEmoji(User user, Emoji emoji) {
        return Reaction.removeUser(this.getApi(), this.getChannel().getId(), this.getId(), emoji, user.getId());
    }

    public CompletableFuture<Void> removeReactionByEmoji(User var1, String var2);

    default public CompletableFuture<Void> removeReactionByEmoji(Emoji emoji) {
        return this.getReactionByEmoji(emoji).map(Reaction::remove).orElseGet(() -> CompletableFuture.completedFuture(null));
    }

    public CompletableFuture<Void> removeReactionByEmoji(String var1);

    default public CompletableFuture<Void> removeReactionsByEmoji(User user, Emoji ... emojis) {
        return CompletableFuture.allOf((CompletableFuture[])Arrays.stream(emojis).map(emoji -> this.removeReactionByEmoji(user, (Emoji)emoji)).toArray(CompletableFuture[]::new));
    }

    public CompletableFuture<Void> removeReactionsByEmoji(User var1, String ... var2);

    default public CompletableFuture<Void> removeReactionsByEmoji(Emoji ... emojis) {
        return CompletableFuture.allOf((CompletableFuture[])Arrays.stream(emojis).map(this::removeReactionByEmoji).toArray(CompletableFuture[]::new));
    }

    public CompletableFuture<Void> removeReactionsByEmoji(String ... var1);

    default public CompletableFuture<Void> removeOwnReactionByEmoji(Emoji emoji) {
        return this.removeReactionByEmoji(this.getApi().getYourself(), emoji);
    }

    public CompletableFuture<Void> removeOwnReactionByEmoji(String var1);

    default public CompletableFuture<Void> removeOwnReactionsByEmoji(Emoji ... emojis) {
        return this.removeReactionsByEmoji(this.getApi().getYourself(), emojis);
    }

    public CompletableFuture<Void> removeOwnReactionsByEmoji(String ... var1);

    default public Optional<ServerTextChannel> getServerTextChannel() {
        return this.getChannel().asServerTextChannel();
    }

    default public Optional<ServerVoiceChannel> getServerVoiceChannel() {
        return this.getChannel().asServerVoiceChannel();
    }

    default public Optional<ServerThreadChannel> getServerThreadChannel() {
        return this.getChannel().asServerThreadChannel();
    }

    default public Optional<PrivateChannel> getPrivateChannel() {
        return this.getChannel().asPrivateChannel();
    }

    default public Optional<Server> getServer() {
        return Optional.of(this.getChannel()).map(Channel::asServerChannel).orElseGet(() -> this.getServerThreadChannel().flatMap(Channel::asServerChannel)).map(ServerChannel::getServer);
    }

    default public CompletableFuture<MessageSet> getMessagesBefore(int limit) {
        return this.getChannel().getMessagesBefore(limit, this);
    }

    default public CompletableFuture<MessageSet> getMessagesBeforeUntil(Predicate<Message> condition) {
        return this.getChannel().getMessagesBeforeUntil(condition, this);
    }

    default public CompletableFuture<MessageSet> getMessagesBeforeWhile(Predicate<Message> condition) {
        return this.getChannel().getMessagesBeforeWhile(condition, this);
    }

    default public Stream<Message> getMessagesBeforeAsStream() {
        return this.getChannel().getMessagesBeforeAsStream(this);
    }

    default public CompletableFuture<MessageSet> getMessagesAfter(int limit) {
        return this.getChannel().getMessagesAfter(limit, this);
    }

    default public CompletableFuture<MessageSet> getMessagesAfterUntil(Predicate<Message> condition) {
        return this.getChannel().getMessagesAfterUntil(condition, this);
    }

    default public CompletableFuture<MessageSet> getMessagesAfterWhile(Predicate<Message> condition) {
        return this.getChannel().getMessagesAfterWhile(condition, this);
    }

    default public Stream<Message> getMessagesAfterAsStream() {
        return this.getChannel().getMessagesAfterAsStream(this);
    }

    default public CompletableFuture<MessageSet> getMessagesAround(int limit) {
        return this.getChannel().getMessagesAround(limit, this);
    }

    default public CompletableFuture<MessageSet> getMessagesAroundUntil(Predicate<Message> condition) {
        return this.getChannel().getMessagesAroundUntil(condition, this);
    }

    default public CompletableFuture<MessageSet> getMessagesAroundWhile(Predicate<Message> condition) {
        return this.getChannel().getMessagesAroundWhile(condition, this);
    }

    default public Stream<Message> getMessagesAroundAsStream() {
        return this.getChannel().getMessagesAroundAsStream(this);
    }

    default public CompletableFuture<MessageSet> getMessagesBetween(long other) {
        return this.getChannel().getMessagesBetween(this.getId(), other);
    }

    default public CompletableFuture<MessageSet> getMessagesBetween(Message other) {
        return this.getMessagesBetween(other.getId());
    }

    default public CompletableFuture<MessageSet> getMessagesBetweenUntil(long other, Predicate<Message> condition) {
        return this.getChannel().getMessagesBetweenUntil(condition, this.getId(), other);
    }

    default public CompletableFuture<MessageSet> getMessagesBetweenUntil(Message other, Predicate<Message> condition) {
        return this.getMessagesBetweenUntil(other.getId(), condition);
    }

    default public CompletableFuture<MessageSet> getMessagesBetweenWhile(long other, Predicate<Message> condition) {
        return this.getChannel().getMessagesBetweenWhile(condition, this.getId(), other);
    }

    default public CompletableFuture<MessageSet> getMessagesBetweenWhile(Message other, Predicate<Message> condition) {
        return this.getMessagesBetweenWhile(other.getId(), condition);
    }

    default public Stream<Message> getMessagesBetweenAsStream(long other) {
        return this.getChannel().getMessagesBetweenAsStream(this.getId(), other);
    }

    default public Stream<Message> getMessagesBetweenAsStream(Message other) {
        return this.getMessagesBetweenAsStream(other.getId());
    }

    default public boolean canAddNewReactions(User user) {
        Optional<ServerTextChannel> channel = this.getServerTextChannel();
        return !channel.isPresent() || channel.get().hasPermission(user, PermissionType.ADMINISTRATOR) || channel.get().hasPermissions(user, PermissionType.VIEW_CHANNEL, PermissionType.READ_MESSAGE_HISTORY, PermissionType.ADD_REACTIONS);
    }

    default public boolean canYouAddNewReactions() {
        return this.canAddNewReactions(this.getApi().getYourself());
    }

    default public boolean canDelete(User user) {
        if (!this.getChannel().canSee(user)) {
            return false;
        }
        if (this.getAuthor().asUser().orElse(null) == user) {
            return true;
        }
        return this.getServerTextChannel().map(channel -> channel.canManageMessages(user)).orElse(false);
    }

    default public CompletableFuture<Message> reply(String messageContent) {
        return this.reply(messageContent, true);
    }

    default public CompletableFuture<Message> reply(String messageContent, boolean assertReferenceExists) {
        return ((MessageBuilder)new MessageBuilder().replyTo(this.getId(), assertReferenceExists).setContent(messageContent)).send(this.getChannel());
    }

    default public CompletableFuture<Message> reply(EmbedBuilder embed) {
        return this.reply(embed, true);
    }

    default public CompletableFuture<Message> reply(EmbedBuilder embed, boolean assertReferenceExists) {
        return ((MessageBuilder)new MessageBuilder().replyTo(this.getId(), assertReferenceExists).setEmbed(embed)).send(this.getChannel());
    }

    default public boolean canYouDelete() {
        return this.canDelete(this.getApi().getYourself());
    }

    @Override
    default public Optional<Message> getCurrentCachedInstance() {
        return this.getApi().getCachedMessageById(this.getId());
    }

    @Override
    default public CompletableFuture<Message> getLatestInstance() {
        return this.getChannel().getMessageById(this.getId());
    }

    default public CompletableFuture<ServerThreadChannel> createThread(String name, AutoArchiveDuration autoArchiveDuration) {
        return this.getServerTextChannel().map(serverTextChannel -> serverTextChannel.createThreadForMessage(this, name, autoArchiveDuration)).orElseThrow(() -> new IllegalStateException("In order to create a thread the channel of this message must be a ServerTextChannel"));
    }

    default public CompletableFuture<ServerThreadChannel> createThread(String name, Integer autoArchiveDuration) {
        return this.getServerTextChannel().map(serverTextChannel -> serverTextChannel.createThreadForMessage(this, name, autoArchiveDuration)).orElseThrow(() -> new IllegalStateException("In order to create a thread the channel of this message must be a ServerTextChannel"));
    }
}

