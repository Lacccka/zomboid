/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Attachment;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.ActionRowBuilder;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.entity.message.component.HighLevelComponent;
import org.javacord.api.entity.message.component.LowLevelComponent;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.internal.MessageBuilderBaseDelegate;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.AttachmentImpl;
import org.javacord.core.entity.message.component.ComponentImpl;
import org.javacord.core.entity.message.embed.EmbedBuilderDelegateImpl;
import org.javacord.core.entity.message.mention.AllowedMentionsImpl;
import org.javacord.core.entity.user.Member;
import org.javacord.core.util.FileContainer;
import org.javacord.core.util.logging.LoggerUtil;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class MessageBuilderBaseDelegateImpl
implements MessageBuilderBaseDelegate {
    private static final Logger logger = LoggerUtil.getLogger(MessageBuilderBaseDelegateImpl.class);
    protected final StringBuilder strBuilder = new StringBuilder();
    protected boolean contentChanged = false;
    protected List<EmbedBuilder> embeds = new ArrayList<EmbedBuilder>();
    protected boolean embedsChanged = false;
    protected boolean tts = false;
    protected String nonce = null;
    protected final List<FileContainer> newAttachments = new ArrayList<FileContainer>();
    protected boolean removeAllAttachments = false;
    protected final List<Attachment> attachmentsToRemove = new ArrayList<Attachment>();
    protected boolean attachmentsChanged = false;
    protected final List<HighLevelComponent> components = new ArrayList<HighLevelComponent>();
    protected boolean componentsChanged = false;
    protected AllowedMentions allowedMentions = null;
    protected Long replyingTo = null;
    private boolean assertReferenceExists;
    protected Set<Long> stickerIds = new HashSet<Long>();

    @Override
    public void addComponents(HighLevelComponent ... highLevelComponents) {
        this.components.addAll(Arrays.asList(highLevelComponents));
        this.componentsChanged = true;
    }

    @Override
    public void addActionRow(LowLevelComponent ... lowLevelComponents) {
        this.addComponents(ActionRow.of(lowLevelComponents));
        this.componentsChanged = true;
    }

    @Override
    public void appendCode(String language, String code) {
        this.strBuilder.append("\n").append(MessageDecoration.CODE_LONG.getPrefix()).append(language).append("\n").append(code).append(MessageDecoration.CODE_LONG.getSuffix());
        this.contentChanged = true;
    }

    @Override
    public void append(String message, MessageDecoration ... decorations) {
        for (MessageDecoration decoration : decorations) {
            this.strBuilder.append(decoration.getPrefix());
        }
        this.strBuilder.append(message);
        for (int i = decorations.length - 1; i >= 0; --i) {
            this.strBuilder.append(decorations[i].getSuffix());
        }
        this.contentChanged = true;
    }

    @Override
    public void append(Mentionable entity) {
        this.strBuilder.append(entity.getMentionTag());
        this.contentChanged = true;
    }

    @Override
    public void append(Object object) {
        this.strBuilder.append(object);
        this.contentChanged = true;
    }

    @Override
    public void appendNamedLink(String name, String url) {
        this.strBuilder.append("[").append(name).append("]").append("(").append(url).append(")");
        this.contentChanged = true;
    }

    @Override
    public void appendNewLine() {
        this.strBuilder.append("\n");
        this.contentChanged = true;
    }

    @Override
    public void setContent(String content) {
        this.strBuilder.setLength(0);
        this.strBuilder.append(content);
        this.contentChanged = true;
    }

    @Override
    public void removeExistingAttachment(Attachment attachment) {
        this.attachmentsToRemove.add(attachment);
    }

    @Override
    public void removeExistingAttachments() {
        this.removeAllAttachments = true;
    }

    @Override
    public void removeExistingAttachments(Collection<Attachment> attachments) {
        this.attachmentsToRemove.addAll(attachments);
    }

    @Override
    public void addEmbed(EmbedBuilder embed) {
        if (embed != null) {
            this.embeds.add(embed);
            this.embedsChanged = true;
        }
    }

    @Override
    public void copy(Message message) {
        this.getStringBuilder().append(message.getContent());
        message.getEmbeds().forEach(embed -> this.addEmbed(embed.toBuilder()));
        for (MessageAttachment attachment : message.getAttachments()) {
            this.addAttachment(attachment.getUrl(), (String)attachment.getDescription().orElse(null));
        }
        for (HighLevelComponent component : message.getComponents()) {
            if (component.getType() != ComponentType.ACTION_ROW) continue;
            ActionRowBuilder builder = new ActionRowBuilder();
            builder.copy((ActionRow)component);
            this.addComponents(builder.build());
        }
        this.contentChanged = false;
        this.componentsChanged = false;
        this.attachmentsChanged = false;
        this.embedsChanged = false;
    }

    @Override
    public void removeAllEmbeds() {
        this.embeds.clear();
        this.embedsChanged = true;
    }

    @Override
    public void addEmbeds(List<EmbedBuilder> embeds) {
        embeds.forEach(this::addEmbed);
    }

    @Override
    public void removeEmbed(EmbedBuilder embed) {
        this.embeds.remove(embed);
        this.embedsChanged = true;
    }

    @Override
    public void removeEmbeds(EmbedBuilder ... embeds) {
        this.embeds.removeAll(Arrays.asList(embeds));
        this.embedsChanged = true;
    }

    @Override
    public void removeComponent(int index) {
        this.components.remove(index);
        this.componentsChanged = true;
    }

    @Override
    public void removeComponent(HighLevelComponent component) {
        this.components.remove(component);
        this.componentsChanged = true;
    }

    @Override
    public void removeAllComponents() {
        this.components.clear();
        this.componentsChanged = true;
    }

    @Override
    public void setTts(boolean tts) {
        this.tts = tts;
    }

    @Override
    public void addAttachment(BufferedImage image, String fileName, String description) {
        if (image == null || fileName == null) {
            throw new IllegalArgumentException("image and fileName cannot be null!");
        }
        this.newAttachments.add(new FileContainer(image, fileName, description));
        this.attachmentsChanged = true;
    }

    @Override
    public void addAttachment(File file, String description) {
        if (file == null) {
            throw new IllegalArgumentException("file cannot be null!");
        }
        this.newAttachments.add(new FileContainer(file, description));
        this.attachmentsChanged = true;
    }

    @Override
    public void addAttachment(Icon icon, String description) {
        if (icon == null) {
            throw new IllegalArgumentException("icon cannot be null!");
        }
        this.newAttachments.add(new FileContainer(icon, description));
        this.attachmentsChanged = true;
    }

    @Override
    public void addAttachment(URL url, String description) {
        if (url == null) {
            throw new IllegalArgumentException("url cannot be null!");
        }
        this.newAttachments.add(new FileContainer(url, description));
        this.attachmentsChanged = true;
    }

    @Override
    public void addAttachment(byte[] bytes, String fileName, String description) {
        if (bytes == null || fileName == null) {
            throw new IllegalArgumentException("bytes and fileName cannot be null!");
        }
        this.newAttachments.add(new FileContainer(bytes, fileName, description));
        this.attachmentsChanged = true;
    }

    @Override
    public void addAttachment(InputStream stream, String fileName, String description) {
        if (stream == null || fileName == null) {
            throw new IllegalArgumentException("stream and fileName cannot be null!");
        }
        this.newAttachments.add(new FileContainer(stream, fileName, description));
        this.attachmentsChanged = true;
    }

    @Override
    public void addAttachmentAsSpoiler(File file, String description) {
        if (file == null) {
            throw new IllegalArgumentException("file cannot be null!");
        }
        this.newAttachments.add(new FileContainer(file, true, description));
        this.attachmentsChanged = true;
    }

    @Override
    public void addAttachmentAsSpoiler(Icon icon, String description) {
        if (icon == null) {
            throw new IllegalArgumentException("icon cannot be null!");
        }
        this.newAttachments.add(new FileContainer(icon, true, description));
        this.attachmentsChanged = true;
    }

    @Override
    public void addAttachmentAsSpoiler(URL url, String description) {
        if (url == null) {
            throw new IllegalArgumentException("url cannot be null!");
        }
        this.newAttachments.add(new FileContainer(url, true, description));
        this.attachmentsChanged = true;
    }

    @Override
    public void setAllowedMentions(AllowedMentions allowedMentions) {
        if (allowedMentions == null) {
            throw new IllegalArgumentException("mention cannot be null!");
        }
        this.allowedMentions = allowedMentions;
    }

    @Override
    public void replyTo(long messageId, boolean assertReferenceExists) {
        this.replyingTo = messageId;
        this.assertReferenceExists = assertReferenceExists;
    }

    @Override
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    @Override
    public void addSticker(long stickerId) {
        this.stickerIds.add(stickerId);
    }

    @Override
    public void addStickers(Collection<Long> stickerIds) {
        this.stickerIds.addAll(stickerIds);
    }

    @Override
    public StringBuilder getStringBuilder() {
        return this.strBuilder;
    }

    @Override
    public CompletableFuture<Message> send(User user) {
        return this.send((Messageable)user);
    }

    @Override
    public CompletableFuture<Message> send(Messageable messageable) {
        if (messageable == null) {
            throw new IllegalStateException("Cannot send message without knowing the receiver");
        }
        if (messageable instanceof TextChannel) {
            return this.send((TextChannel)messageable);
        }
        if (messageable instanceof User) {
            return ((User)messageable).openPrivateChannel().thenCompose(this::send);
        }
        if (messageable instanceof Member) {
            return this.send(((Member)messageable).getUser());
        }
        if (messageable instanceof IncomingWebhook) {
            return this.send((IncomingWebhook)messageable);
        }
        throw new IllegalStateException("Messageable of unknown type");
    }

    @Override
    public CompletableFuture<Message> send(TextChannel channel) {
        ObjectNode body = JsonNodeFactory.instance.objectNode().put("content", this.toString() == null ? "" : this.toString()).put("tts", this.tts);
        body.putArray("mentions");
        this.prepareAllowedMentions(body);
        this.prepareEmbeds(body, false);
        this.prepareComponents(body, false);
        if (this.nonce != null) {
            body.put("nonce", this.nonce);
        }
        if (!this.stickerIds.isEmpty()) {
            ArrayNode stickersNode = JsonNodeFactory.instance.objectNode().arrayNode();
            for (long stickerId : this.stickerIds) {
                stickersNode.add(stickerId);
            }
            body.set("sticker_ids", stickersNode);
        }
        if (this.replyingTo != null) {
            body.putObject("message_reference").put("message_id", this.replyingTo).put("fail_if_not_exists", this.assertReferenceExists);
        }
        RestRequest<Message> request = new RestRequest(channel.getApi(), RestMethod.POST, RestEndpoint.MESSAGE).setUrlParameters(channel.getIdAsString());
        return this.checkForAttachmentsAndExecuteRequest(channel, body, request, false);
    }

    @Override
    public CompletableFuture<Message> send(IncomingWebhook webhook) {
        return this.send(webhook.getIdAsString(), webhook.getToken(), null, null, true, webhook.getApi());
    }

    protected CompletableFuture<Message> send(String webhookId, String webhookToken, String displayName, URL avatarUrl, boolean wait, DiscordApi api) {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        this.prepareCommonWebhookMessageBodyParts(body);
        if (displayName != null) {
            body.put("username", displayName);
        }
        if (avatarUrl != null) {
            body.put("avatar_url", avatarUrl.toExternalForm());
        }
        this.prepareComponents(body);
        if (this.strBuilder.length() != 0) {
            body.put("content", this.strBuilder.toString());
        }
        RestRequest<Message> request = new RestRequest(api, RestMethod.POST, RestEndpoint.WEBHOOK_SEND).addQueryParameter("wait", Boolean.toString(wait)).setUrlParameters(webhookId, webhookToken).consumeGlobalRatelimit(false).includeAuthorizationHeader(false);
        CompletableFuture<Message> future = new CompletableFuture<Message>();
        if (!this.newAttachments.isEmpty() || this.embeds.stream().anyMatch(EmbedBuilder::requiresAttachments)) {
            api.getThreadPool().getExecutorService().submit(() -> {
                try {
                    ArrayList<FileContainer> tempAttachments = new ArrayList<FileContainer>(this.newAttachments);
                    for (EmbedBuilder embed : this.embeds) {
                        tempAttachments.addAll(((EmbedBuilderDelegateImpl)embed.getDelegate()).getRequiredAttachments());
                    }
                    this.addMultipartBodyToRequest(request, body, tempAttachments, api);
                    MessageBuilderBaseDelegateImpl.executeWebhookRest(request, wait, future, api);
                }
                catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            });
        } else {
            request.setBody(body);
            MessageBuilderBaseDelegateImpl.executeWebhookRest(request, wait, future, api);
        }
        return future;
    }

    @Override
    public CompletableFuture<Message> sendWithWebhook(DiscordApi api, String webhookId, String webhookToken) {
        return this.send(webhookId, webhookToken, null, null, true, api);
    }

    private static void executeWebhookRest(RestRequest<Message> request, boolean wait, CompletableFuture<Message> future, DiscordApi api) {
        CompletableFuture<Message> tmpFuture = wait ? request.execute(result -> {
            JsonNode body = result.getJsonBody();
            TextChannel channel = api.getTextChannelById(body.get("channel_id").asText()).orElseThrow(() -> new IllegalStateException("Cannot return a message when the channel isn't cached!"));
            return ((DiscordApiImpl)api).getOrCreateMessage(channel, body);
        }) : request.execute(result -> null);
        tmpFuture.whenComplete((message, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally((Throwable)throwable);
            } else {
                future.complete((Message)message);
            }
        });
    }

    @Override
    public CompletableFuture<Message> edit(Message message, boolean updateAll) {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        if (updateAll || this.contentChanged) {
            body.put("content", this.strBuilder.toString());
        }
        this.prepareAllowedMentions(body);
        this.prepareEmbeds(body, updateAll || this.embedsChanged);
        this.prepareComponents(body, updateAll || this.componentsChanged);
        this.prepareAttachments(message.getAttachments(), body, updateAll);
        RestRequest<Message> request = new RestRequest(message.getApi(), RestMethod.PATCH, RestEndpoint.MESSAGE).setUrlParameters(Long.toUnsignedString(message.getChannel().getId()), Long.toUnsignedString(message.getId()));
        if (updateAll || this.attachmentsChanged) {
            return this.checkForAttachmentsAndExecuteRequest(message.getChannel(), body, request, true);
        }
        return this.executeRequestWithoutNewAttachments(message.getChannel(), body, request);
    }

    private CompletableFuture<Message> checkForAttachmentsAndExecuteRequest(TextChannel channel, ObjectNode body, RestRequest<Message> request, boolean clearAttachmentsIfAppropriate) {
        if (this.newAttachments.isEmpty() && this.embeds.stream().noneMatch(EmbedBuilder::requiresAttachments)) {
            return this.executeRequestWithoutNewAttachments(channel, body, request);
        }
        CompletableFuture<Message> future = new CompletableFuture<Message>();
        channel.getApi().getThreadPool().getExecutorService().submit(() -> {
            try {
                ArrayList<FileContainer> tempAttachments = new ArrayList<FileContainer>(this.newAttachments);
                for (EmbedBuilder embed : this.embeds) {
                    tempAttachments.addAll(((EmbedBuilderDelegateImpl)embed.getDelegate()).getRequiredAttachments());
                }
                this.addMultipartBodyToRequest(request, body, tempAttachments, channel.getApi());
                request.execute(result -> ((DiscordApiImpl)channel.getApi()).getOrCreateMessage(channel, result.getJsonBody())).whenComplete((newMessage, throwable) -> {
                    if (throwable != null) {
                        future.completeExceptionally((Throwable)throwable);
                    } else {
                        future.complete((Message)newMessage);
                    }
                });
            }
            catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    private CompletableFuture<Message> executeRequestWithoutNewAttachments(TextChannel channel, ObjectNode body, RestRequest<Message> request) {
        request.setBody(body);
        return request.execute(result -> ((DiscordApiImpl)channel.getApi()).getOrCreateMessage(channel, result.getJsonBody()));
    }

    private void prepareAllowedMentions(ObjectNode body) {
        if (this.allowedMentions != null) {
            ((AllowedMentionsImpl)this.allowedMentions).toJsonNode(body.putObject("allowed_mentions"));
        }
    }

    private void prepareAttachments(List<MessageAttachment> attachmentsList, ObjectNode body, boolean updateAll) {
        ArrayNode attachments = body.putArray("attachments");
        if (this.removeAllAttachments) {
            return;
        }
        if (!this.attachmentsToRemove.isEmpty()) {
            for (Attachment attachment : attachmentsList) {
                if (this.attachmentsToRemove.contains(attachment)) continue;
                attachments.add(((AttachmentImpl)attachment).toJsonNode());
            }
        } else {
            if (updateAll) {
                return;
            }
            for (Attachment attachment : attachmentsList) {
                attachments.add(((AttachmentImpl)attachment).toJsonNode());
            }
        }
    }

    protected void addMultipartBodyToRequest(RestRequest<?> request, ObjectNode body, List<FileContainer> attachments, DiscordApi api) {
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        Collections.reverse(attachments);
        for (int i = 0; i < attachments.size(); ++i) {
            FileContainer fileContainer = attachments.get(i);
            byte[] bytes = fileContainer.asByteArray(api).join();
            String mediaType = URLConnection.guessContentTypeFromName(fileContainer.getFileTypeOrName());
            if (mediaType == null) {
                mediaType = "application/octet-stream";
            }
            multipartBodyBuilder.addFormDataPart("files[" + i + "]", fileContainer.getFileTypeOrName(), RequestBody.create(bytes, MediaType.parse(mediaType)));
            if (fileContainer.getDescription() == null) continue;
            ArrayNode attachmentJson = body.withArray("attachments");
            ObjectNode newFileAttachment = JsonNodeFactory.instance.objectNode();
            newFileAttachment.put("id", i);
            newFileAttachment.put("description", fileContainer.getDescription());
            attachmentJson.add(newFileAttachment);
        }
        multipartBodyBuilder.addFormDataPart("payload_json", body.toString());
        request.setMultipartBody(multipartBodyBuilder.build());
    }

    protected void prepareCommonWebhookMessageBodyParts(ObjectNode body) {
        body.put("tts", this.tts);
        if (this.strBuilder.length() != 0) {
            body.put("content", this.strBuilder.toString());
        }
        this.prepareAllowedMentions(body);
        this.prepareEmbeds(body, false);
    }

    private void prepareEmbeds(ObjectNode body, boolean evenIfEmpty) {
        if (!this.embeds.isEmpty() || evenIfEmpty) {
            ArrayNode embedsNode = JsonNodeFactory.instance.objectNode().arrayNode();
            for (EmbedBuilder embed : this.embeds) {
                embedsNode.add(((EmbedBuilderDelegateImpl)embed.getDelegate()).toJsonNode());
            }
            body.set("embeds", embedsNode);
        }
    }

    public String toString() {
        return this.strBuilder.toString();
    }

    protected void prepareComponents(ObjectNode body) {
        this.prepareComponents(body, false);
    }

    protected void prepareComponents(ObjectNode body, boolean evenIfEmpty) {
        if (evenIfEmpty || !this.components.isEmpty()) {
            ArrayNode componentsNode = JsonNodeFactory.instance.objectNode().arrayNode();
            this.components.forEach(highLevelComponent -> componentsNode.add(((ComponentImpl)((Object)highLevelComponent)).toJsonNode()));
            body.set("components", componentsNode);
        }
    }
}

