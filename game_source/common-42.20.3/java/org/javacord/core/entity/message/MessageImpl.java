/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageActivity;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.MessageReference;
import org.javacord.api.entity.message.MessageType;
import org.javacord.api.entity.message.Reaction;
import org.javacord.api.entity.message.component.HighLevelComponent;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.sticker.StickerItem;
import org.javacord.api.entity.user.User;
import org.javacord.api.exception.MissingIntentException;
import org.javacord.api.interaction.MessageInteraction;
import org.javacord.api.util.DiscordRegexPattern;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.emoji.UnicodeEmojiImpl;
import org.javacord.core.entity.message.MessageActivityImpl;
import org.javacord.core.entity.message.MessageAttachmentImpl;
import org.javacord.core.entity.message.MessageAuthorImpl;
import org.javacord.core.entity.message.MessageReferenceImpl;
import org.javacord.core.entity.message.ReactionImpl;
import org.javacord.core.entity.message.component.ActionRowImpl;
import org.javacord.core.entity.message.embed.EmbedImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.sticker.StickerItemImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.interaction.MessageInteractionImpl;
import org.javacord.core.listener.message.InternalMessageAttachableListenerManager;
import org.javacord.core.util.cache.MessageCacheImpl;

public class MessageImpl
implements Message,
InternalMessageAttachableListenerManager {
    private final DiscordApiImpl api;
    private final TextChannel channel;
    private final long id;
    private volatile String content;
    private final List<HighLevelComponent> components = new ArrayList<HighLevelComponent>();
    private final MessageInteraction messageInteraction;
    private final MessageType type;
    private final EnumSet<MessageFlag> flags = EnumSet.noneOf(MessageFlag.class);
    private volatile boolean pinned;
    private volatile boolean tts;
    private volatile boolean mentionsEveryone;
    private volatile Instant lastEditTime;
    private MessageAuthor author;
    private final MessageActivityImpl activity;
    private final String nonce;
    private final Message referencedMessage;
    private final MessageReference messageReference;
    private volatile boolean cacheForever = false;
    private final List<Embed> embeds = new ArrayList<Embed>();
    private final List<Reaction> reactions = new ArrayList<Reaction>();
    private final List<MessageAttachment> attachments = new ArrayList<MessageAttachment>();
    private final List<User> mentions = new ArrayList<User>();
    private final List<Role> roleMentions = new ArrayList<Role>();
    private final Set<StickerItem> stickerItems = new HashSet<StickerItem>();
    private final Integer position;

    public MessageImpl(DiscordApiImpl api, TextChannel channel, JsonNode data) {
        this.api = api;
        this.channel = channel;
        this.id = data.get("id").asLong();
        this.type = MessageType.byType(data.get("type").asInt(), data.has("webhook_id"));
        Long webhookId = data.has("webhook_id") ? Long.valueOf(data.get("webhook_id").asLong()) : null;
        this.author = new MessageAuthorImpl(this, webhookId, data);
        this.activity = data.hasNonNull("activity") ? new MessageActivityImpl(this, data.get("activity")) : null;
        this.nonce = data.hasNonNull("nonce") ? data.path("nonce").asText() : null;
        this.referencedMessage = data.hasNonNull("referenced_message") ? api.getOrCreateMessage(channel, data.get("referenced_message")) : null;
        this.messageReference = data.hasNonNull("message_reference") ? new MessageReferenceImpl(api, this.referencedMessage, data.get("message_reference")) : null;
        this.messageInteraction = data.hasNonNull("interaction") ? new MessageInteractionImpl(this, data.get("interaction")) : null;
        this.position = data.hasNonNull("position") ? Integer.valueOf(data.get("position").asInt()) : null;
        this.setUpdatableFields(data);
        MessageCacheImpl cache = (MessageCacheImpl)channel.getMessageCache();
        cache.addMessage(this);
    }

    private MessageImpl(DiscordApiImpl api, TextChannel channel, long id, MessageType type, MessageAuthor author, MessageActivityImpl activity, String content, String nonce, Message referencedMessage, MessageReference messageReference, boolean pinned, boolean tts, boolean mentionsEveryone, Instant lastEditTime, List<HighLevelComponent> components, List<Embed> embeds, List<Reaction> reactions, List<MessageAttachment> attachments, List<User> mentions, List<Role> roleMentions, Set<StickerItem> stickerItems, MessageInteraction messageInteraction, Integer position) {
        this.api = api;
        this.channel = channel;
        this.id = id;
        this.type = type;
        this.author = author;
        this.activity = activity;
        this.content = content;
        this.nonce = nonce;
        this.referencedMessage = referencedMessage;
        this.messageReference = messageReference;
        this.messageInteraction = messageInteraction;
        this.pinned = pinned;
        this.tts = tts;
        this.mentionsEveryone = mentionsEveryone;
        this.lastEditTime = lastEditTime;
        this.components.addAll(components);
        this.embeds.addAll(embeds);
        this.reactions.addAll(reactions);
        this.attachments.addAll(attachments);
        this.mentions.addAll(mentions);
        this.roleMentions.addAll(roleMentions);
        this.stickerItems.addAll(stickerItems);
        this.position = position;
    }

    public MessageImpl copyMessage() {
        return new MessageImpl(this.api, this.channel, this.id, this.type, this.author, this.activity, this.content, this.nonce, this.referencedMessage, this.messageReference, this.pinned, this.tts, this.mentionsEveryone, this.lastEditTime, this.components, this.embeds, this.reactions, this.attachments, this.mentions, this.roleMentions, this.stickerItems, this.messageInteraction, this.position);
    }

    public void setUpdatableFields(JsonNode data) {
        if (data.has("content")) {
            this.content = data.get("content").asText("");
        }
        if (data.has("pinned")) {
            this.pinned = data.path("pinned").asBoolean(false);
        }
        if (data.has("tts")) {
            this.tts = data.path("tts").asBoolean(false);
        }
        if (data.has("mention_everyone")) {
            this.mentionsEveryone = data.path("mention_everyone").asBoolean(false);
        }
        if (data.has("edited_timestamp")) {
            this.lastEditTime = data.get("edited_timestamp").isNull() ? null : OffsetDateTime.parse(data.get("edited_timestamp").asText()).toInstant();
        }
        if (data.has("embeds")) {
            this.embeds.clear();
            for (JsonNode jsonNode : data.get("embeds")) {
                EmbedImpl embed = new EmbedImpl(jsonNode);
                this.embeds.add(embed);
            }
        }
        if (data.has("flags")) {
            this.flags.clear();
            int flag = data.get("flags").asInt();
            for (MessageFlag value : MessageFlag.values()) {
                if ((flag & value.getId()) != value.getId()) continue;
                this.flags.add(value);
            }
        }
        if (data.has("author")) {
            this.author = new MessageAuthorImpl(this, this.author.getWebhookId().orElse(null), data);
        }
        if (data.has("components")) {
            this.components.clear();
            for (JsonNode jsonNode : data.get("components")) {
                ActionRowImpl actionRow = new ActionRowImpl(jsonNode);
                this.components.add(actionRow);
            }
        }
        if (data.has("reactions")) {
            this.reactions.clear();
            for (JsonNode jsonNode : data.get("reactions")) {
                ReactionImpl reaction = new ReactionImpl(this, jsonNode);
                this.reactions.add(reaction);
            }
        }
        if (data.has("attachments")) {
            this.attachments.clear();
            for (JsonNode jsonNode : data.get("attachments")) {
                MessageAttachmentImpl attachment = new MessageAttachmentImpl(this, jsonNode);
                this.attachments.add(attachment);
            }
        }
        if (data.has("mentions")) {
            this.mentions.clear();
            for (JsonNode jsonNode : data.get("mentions")) {
                UserImpl user = new UserImpl(this.api, jsonNode, (MemberImpl)null, (ServerImpl)this.getServer().map(ServerImpl.class::cast).orElse(null));
                this.mentions.add(user);
            }
        }
        if (data.has("mention_roles")) {
            this.roleMentions.clear();
            this.getServer().ifPresent(server -> {
                for (JsonNode roleMentionJson : data.get("mention_roles")) {
                    server.getRoleById(roleMentionJson.asText()).ifPresent(this.roleMentions::add);
                }
            });
        }
        if (data.has("sticker_items")) {
            this.stickerItems.clear();
            for (JsonNode jsonNode : data.get("sticker_items")) {
                this.stickerItems.add(new StickerItemImpl(this.api, jsonNode));
            }
        }
    }

    public void addReaction(Emoji emoji, boolean you) {
        Optional<Reaction> reaction = this.reactions.stream().filter(r -> emoji.equalsEmoji(r.getEmoji())).findAny();
        reaction.ifPresent(r -> ((ReactionImpl)r).incrementCount(you));
        if (!reaction.isPresent()) {
            this.reactions.add(new ReactionImpl(this, emoji, 1, you));
        }
    }

    public void removeReaction(Emoji emoji, boolean you) {
        Optional<Reaction> reaction = this.reactions.stream().filter(r -> emoji.equalsEmoji(r.getEmoji())).findAny();
        reaction.ifPresent(r -> ((ReactionImpl)r).decrementCount(you));
        this.reactions.removeIf(r -> r.getCount() <= 0);
    }

    public void removeAllReactionsFromCache() {
        this.reactions.clear();
    }

    @Override
    public DiscordApi getApi() {
        return this.api;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public boolean canYouReadContent() {
        return this.api.getIntents().contains((Object)Intent.MESSAGE_CONTENT) || !this.content.isEmpty() || this.isPrivateMessage() || this.author.getId() == this.api.getYourself().getId() || this.getMentionedUsers().contains(this.api.getYourself());
    }

    @Override
    public String getContent() {
        if (this.canYouReadContent()) {
            return this.content;
        }
        throw new MissingIntentException("The MESSAGE_CONTENT intent is required to receive the content of a message. Please see https://javacord.org/wiki/basic-tutorials/gateway-intents.html on how to enable this privileged intent or check with the method Message#canYouReadContent if you have received the content in special cases like: DMs, bot mentions or if it's your own messages");
    }

    @Override
    public Optional<Instant> getLastEditTimestamp() {
        return Optional.ofNullable(this.lastEditTime);
    }

    @Override
    public List<MessageAttachment> getAttachments() {
        if (this.canYouReadContent()) {
            return Collections.unmodifiableList(this.attachments);
        }
        throw new MissingIntentException("The MESSAGE_CONTENT intent is required to receive attachments of a message. Please see https://javacord.org/wiki/basic-tutorials/gateway-intents.html on how to enable this privileged intent or check with the method Message#canYouReadContent if you have received the attachments in special cases like: DMs, bot mentions or if it's your own messages");
    }

    @Override
    public List<CustomEmoji> getCustomEmojis() {
        String content = this.getContent();
        ArrayList<CustomEmoji> emojis = new ArrayList<CustomEmoji>();
        Matcher customEmoji = DiscordRegexPattern.CUSTOM_EMOJI.matcher(content);
        while (customEmoji.find()) {
            long id = Long.parseLong(customEmoji.group("id"));
            String name = customEmoji.group("name");
            boolean animated = customEmoji.group(0).charAt(1) == 'a';
            CustomEmoji emoji = this.getApi().getKnownCustomEmojiOrCreateCustomEmoji(id, name, animated);
            emojis.add(emoji);
        }
        return Collections.unmodifiableList(emojis);
    }

    @Override
    public MessageType getType() {
        return this.type;
    }

    @Override
    public TextChannel getChannel() {
        return this.channel;
    }

    @Override
    public Optional<MessageActivity> getActivity() {
        return Optional.ofNullable(this.activity);
    }

    @Override
    public EnumSet<MessageFlag> getFlags() {
        return EnumSet.copyOf(this.flags);
    }

    @Override
    public boolean isPinned() {
        return this.pinned;
    }

    @Override
    public boolean isTts() {
        return this.tts;
    }

    @Override
    public boolean mentionsEveryone() {
        return this.mentionsEveryone;
    }

    @Override
    public List<Embed> getEmbeds() {
        if (this.canYouReadContent()) {
            return Collections.unmodifiableList(new ArrayList<Embed>(this.embeds));
        }
        throw new MissingIntentException("The MESSAGE_CONTENT intent is required to receive embeds of a message. Please see https://javacord.org/wiki/basic-tutorials/gateway-intents.html on how to enable this privileged intent or check with the method Message#canYouReadContent if you have received the embeds in special cases like: DMs, bot mentions or if it's your own messages");
    }

    @Override
    public MessageAuthor getAuthor() {
        return this.author;
    }

    @Override
    public Optional<User> getUserAuthor() {
        return this.author.asUser();
    }

    @Override
    public Optional<MessageReference> getMessageReference() {
        return Optional.ofNullable(this.messageReference);
    }

    @Override
    public Optional<Message> getReferencedMessage() {
        return Optional.ofNullable(this.referencedMessage);
    }

    @Override
    public boolean isCachedForever() {
        return this.cacheForever;
    }

    @Override
    public void setCachedForever(boolean cachedForever) {
        this.cacheForever = cachedForever;
        if (cachedForever) {
            ((MessageCacheImpl)this.channel.getMessageCache()).addMessage(this);
            ((MessageCacheImpl)this.channel.getMessageCache()).addCacheForeverMessage(this);
        } else {
            ((MessageCacheImpl)this.channel.getMessageCache()).removeCacheForeverMessage(this);
        }
    }

    @Override
    public List<Reaction> getReactions() {
        return Collections.unmodifiableList(new ArrayList<Reaction>(this.reactions));
    }

    @Override
    public Optional<MessageInteraction> getMessageInteraction() {
        return Optional.ofNullable(this.messageInteraction);
    }

    @Override
    public List<HighLevelComponent> getComponents() {
        if (this.canYouReadContent()) {
            return Collections.unmodifiableList(new ArrayList<HighLevelComponent>(this.components));
        }
        throw new MissingIntentException("The MESSAGE_CONTENT intent is required to receive the components of a message. Please see https://javacord.org/wiki/basic-tutorials/gateway-intents.html on how to enable this privileged intent or check with the method Message#canYouReadContent if you have received the components in special cases like: DMs, bot mentions or if it's your own messages");
    }

    @Override
    public List<User> getMentionedUsers() {
        return Collections.unmodifiableList(new ArrayList<User>(this.mentions));
    }

    @Override
    public List<Role> getMentionedRoles() {
        return Collections.unmodifiableList(new ArrayList<Role>(this.roleMentions));
    }

    @Override
    public Optional<String> getNonce() {
        return Optional.ofNullable(this.nonce);
    }

    @Override
    public Set<StickerItem> getStickerItems() {
        return Collections.unmodifiableSet(this.stickerItems);
    }

    @Override
    public CompletableFuture<Void> addReactions(String ... unicodeEmojis) {
        return this.addReactions((Emoji[])Arrays.stream(unicodeEmojis).map(UnicodeEmojiImpl::fromString).toArray(Emoji[]::new));
    }

    @Override
    public CompletableFuture<Void> removeReactionByEmoji(User user, String unicodeEmoji) {
        return this.removeReactionByEmoji(user, UnicodeEmojiImpl.fromString(unicodeEmoji));
    }

    @Override
    public CompletableFuture<Void> removeReactionByEmoji(String unicodeEmoji) {
        return this.removeReactionByEmoji(UnicodeEmojiImpl.fromString(unicodeEmoji));
    }

    @Override
    public CompletableFuture<Void> removeReactionsByEmoji(User user, String ... unicodeEmojis) {
        return this.removeReactionsByEmoji(user, (Emoji[])Arrays.stream(unicodeEmojis).map(UnicodeEmojiImpl::fromString).toArray(Emoji[]::new));
    }

    @Override
    public CompletableFuture<Void> removeReactionsByEmoji(String ... unicodeEmojis) {
        return this.removeReactionsByEmoji((Emoji[])Arrays.stream(unicodeEmojis).map(UnicodeEmojiImpl::fromString).toArray(Emoji[]::new));
    }

    @Override
    public CompletableFuture<Void> removeOwnReactionByEmoji(String unicodeEmoji) {
        return this.removeOwnReactionByEmoji(UnicodeEmojiImpl.fromString(unicodeEmoji));
    }

    @Override
    public CompletableFuture<Void> removeOwnReactionsByEmoji(String ... unicodeEmojis) {
        return this.removeOwnReactionsByEmoji((Emoji[])Arrays.stream(unicodeEmojis).map(UnicodeEmojiImpl::fromString).toArray(Emoji[]::new));
    }

    @Override
    public Optional<Integer> getPosition() {
        return Optional.ofNullable(this.position);
    }

    @Override
    public int compareTo(Message otherMessage) {
        return Long.compareUnsigned(this.getId(), otherMessage.getId());
    }

    public boolean equals(Object o) {
        return this == o || o != null && this.getClass() == o.getClass() && this.getId() == ((DiscordEntity)o).getId();
    }

    public int hashCode() {
        return Objects.hash(this.getId());
    }

    public String toString() {
        return String.format("Message (id: %s, content: %s)", this.getIdAsString(), this.getContent());
    }
}

