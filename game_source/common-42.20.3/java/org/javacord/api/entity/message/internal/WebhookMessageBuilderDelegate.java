/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.internal;

import java.net.URL;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.internal.MessageBuilderBaseDelegate;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.IncomingWebhook;

public interface WebhookMessageBuilderDelegate
extends MessageBuilderBaseDelegate {
    public void setDisplayName(String var1);

    public void setDisplayAvatar(URL var1);

    public void setDisplayAvatar(Icon var1);

    default public void setDisplayAuthor(MessageAuthor author) {
        this.setDisplayAvatar(author.getAvatar());
        this.setDisplayName(author.getDisplayName());
    }

    default public void setDisplayAuthor(User author) {
        this.setDisplayAvatar(author.getAvatar());
        this.setDisplayName(author.getName());
    }

    public CompletableFuture<Message> send(DiscordApi var1, String var2, String var3);

    public CompletableFuture<Void> sendSilently(IncomingWebhook var1);

    public CompletableFuture<Void> sendSilently(DiscordApi var1, String var2, String var3);
}

