/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.emoji;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.emoji.internal.CustomEmojiBuilderDelegate;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.util.internal.DelegateFactory;

public class CustomEmojiBuilder {
    private final CustomEmojiBuilderDelegate delegate;

    public CustomEmojiBuilder(Server server) {
        this.delegate = DelegateFactory.createCustomEmojiBuilderDelegate(server);
    }

    public CustomEmojiBuilder setAuditLogReason(String reason) {
        this.delegate.setAuditLogReason(reason);
        return this;
    }

    public CustomEmojiBuilder setName(String name) {
        this.delegate.setName(name);
        return this;
    }

    public CustomEmojiBuilder setImage(Icon image) {
        this.delegate.setImage(image);
        return this;
    }

    public CustomEmojiBuilder setImage(URL image) {
        this.delegate.setImage(image);
        return this;
    }

    public CustomEmojiBuilder setImage(File image) {
        this.delegate.setImage(image);
        return this;
    }

    public CustomEmojiBuilder setImage(BufferedImage image) {
        this.delegate.setImage(image);
        return this;
    }

    public CustomEmojiBuilder setImage(BufferedImage image, String type) {
        this.delegate.setImage(image, type);
        return this;
    }

    public CustomEmojiBuilder setImage(byte[] image) {
        this.delegate.setImage(image);
        return this;
    }

    public CustomEmojiBuilder setImage(byte[] image, String type) {
        this.delegate.setImage(image, type);
        return this;
    }

    public CustomEmojiBuilder setImage(InputStream image) {
        this.delegate.setImage(image);
        return this;
    }

    public CustomEmojiBuilder setImage(InputStream image, String type) {
        this.delegate.setImage(image, type);
        return this;
    }

    public CustomEmojiBuilder addRoleToWhitelist(Role role) {
        this.delegate.addRoleToWhitelist(role);
        return this;
    }

    public CustomEmojiBuilder setWhitelist(Collection<Role> roles) {
        this.delegate.setWhitelist(roles);
        return this;
    }

    public CustomEmojiBuilder setWhitelist(Role ... roles) {
        this.delegate.setWhitelist(roles);
        return this;
    }

    public CompletableFuture<KnownCustomEmoji> create() {
        return this.delegate.create();
    }
}

