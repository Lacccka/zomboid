/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.webhook;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.channel.TextableRegularServerChannel;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.entity.webhook.internal.WebhookBuilderDelegate;
import org.javacord.core.entity.webhook.IncomingWebhookImpl;
import org.javacord.core.util.FileContainer;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class WebhookBuilderDelegateImpl
implements WebhookBuilderDelegate {
    protected final TextableRegularServerChannel channel;
    private String reason = null;
    protected String name = null;
    private FileContainer avatar = null;

    public WebhookBuilderDelegateImpl(TextableRegularServerChannel channel) {
        this.channel = channel;
    }

    @Override
    public void setAuditLogReason(String reason) {
        this.reason = reason;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setAvatar(BufferedImage avatar) {
        this.avatar = avatar == null ? null : new FileContainer(avatar, "png");
    }

    @Override
    public void setAvatar(BufferedImage avatar, String fileType) {
        this.avatar = avatar == null ? null : new FileContainer(avatar, fileType);
    }

    @Override
    public void setAvatar(File avatar) {
        this.avatar = avatar == null ? null : new FileContainer(avatar);
    }

    @Override
    public void setAvatar(Icon avatar) {
        this.avatar = avatar == null ? null : new FileContainer(avatar);
    }

    @Override
    public void setAvatar(URL avatar) {
        this.avatar = avatar == null ? null : new FileContainer(avatar);
    }

    @Override
    public void setAvatar(byte[] avatar) {
        this.avatar = avatar == null ? null : new FileContainer(avatar, "png");
    }

    @Override
    public void setAvatar(byte[] avatar, String fileType) {
        this.avatar = avatar == null ? null : new FileContainer(avatar, fileType);
    }

    @Override
    public void setAvatar(InputStream avatar) {
        this.avatar = avatar == null ? null : new FileContainer(avatar, "png");
    }

    @Override
    public void setAvatar(InputStream avatar, String fileType) {
        this.avatar = avatar == null ? null : new FileContainer(avatar, fileType);
    }

    @Override
    public CompletableFuture<IncomingWebhook> create() {
        if (this.name == null) {
            throw new IllegalStateException("Name is no optional parameter!");
        }
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("name", this.name);
        if (this.avatar != null) {
            return ((CompletableFuture)this.avatar.asByteArray(this.channel.getApi()).thenAccept(bytes -> {
                String base64Avatar = "data:image/" + this.avatar.getFileType() + ";base64," + Base64.getEncoder().encodeToString((byte[])bytes);
                body.put("avatar", base64Avatar);
            })).thenCompose(aVoid -> new RestRequest(this.channel.getApi(), RestMethod.POST, RestEndpoint.CHANNEL_WEBHOOK).setUrlParameters(this.channel.getIdAsString()).setBody(body).setAuditLogReason(this.reason).execute(result -> new IncomingWebhookImpl(this.channel.getApi(), result.getJsonBody())));
        }
        return new RestRequest(this.channel.getApi(), RestMethod.POST, RestEndpoint.CHANNEL_WEBHOOK).setUrlParameters(this.channel.getIdAsString()).setBody(body).setAuditLogReason(this.reason).execute(result -> new IncomingWebhookImpl(this.channel.getApi(), result.getJsonBody()));
    }
}

