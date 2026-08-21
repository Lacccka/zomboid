/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.Icon;
import org.javacord.api.internal.AccountUpdaterDelegate;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.FileContainer;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class AccountUpdaterDelegateImpl
implements AccountUpdaterDelegate {
    private final DiscordApiImpl api;
    private String username = null;
    private FileContainer avatar = null;

    public AccountUpdaterDelegateImpl(DiscordApiImpl api) {
        this.api = api;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
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
    public CompletableFuture<Void> update() {
        boolean patchAccount = false;
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        if (this.username != null) {
            body.put("username", this.username);
            patchAccount = true;
        }
        if (this.avatar != null) {
            patchAccount = true;
        }
        if (patchAccount) {
            if (this.avatar != null) {
                return ((CompletableFuture)this.avatar.asByteArray(this.api).thenAccept(bytes -> {
                    String base64Avatar = "data:image/" + this.avatar.getFileType() + ";base64," + Base64.getEncoder().encodeToString((byte[])bytes);
                    body.put("avatar", base64Avatar);
                })).thenCompose(aVoid -> new RestRequest(this.api, RestMethod.PATCH, RestEndpoint.CURRENT_USER).setBody(body).execute(result -> null));
            }
            return new RestRequest(this.api, RestMethod.PATCH, RestEndpoint.CURRENT_USER).setBody(body).execute(result -> null);
        }
        return CompletableFuture.completedFuture(null);
    }
}

