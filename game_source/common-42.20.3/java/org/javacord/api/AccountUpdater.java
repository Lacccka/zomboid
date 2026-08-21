/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Icon;
import org.javacord.api.internal.AccountUpdaterDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class AccountUpdater {
    private final AccountUpdaterDelegate delegate;

    public AccountUpdater(DiscordApi api) {
        this.delegate = DelegateFactory.createAccountUpdaterDelegate(api);
    }

    public AccountUpdater setUsername(String username) {
        this.delegate.setUsername(username);
        return this;
    }

    public AccountUpdater setAvatar(BufferedImage avatar) {
        this.delegate.setAvatar(avatar);
        return this;
    }

    public AccountUpdater setAvatar(BufferedImage avatar, String fileType) {
        this.delegate.setAvatar(avatar, fileType);
        return this;
    }

    public AccountUpdater setAvatar(File avatar) {
        this.delegate.setAvatar(avatar);
        return this;
    }

    public AccountUpdater setAvatar(Icon avatar) {
        this.delegate.setAvatar(avatar);
        return this;
    }

    public AccountUpdater setAvatar(URL avatar) {
        this.delegate.setAvatar(avatar);
        return this;
    }

    public AccountUpdater setAvatar(byte[] avatar) {
        this.delegate.setAvatar(avatar);
        return this;
    }

    public AccountUpdater setAvatar(byte[] avatar, String fileType) {
        this.delegate.setAvatar(avatar, fileType);
        return this;
    }

    public AccountUpdater setAvatar(InputStream avatar) {
        this.delegate.setAvatar(avatar);
        return this;
    }

    public AccountUpdater setAvatar(InputStream avatar, String fileType) {
        this.delegate.setAvatar(avatar, fileType);
        return this;
    }

    public CompletableFuture<Void> update() {
        return this.delegate.update();
    }
}

