/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.emoji;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.emoji.internal.CustomEmojiBuilderDelegate;
import org.javacord.api.entity.permission.Role;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.util.FileContainer;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class CustomEmojiBuilderDelegateImpl
implements CustomEmojiBuilderDelegate {
    private final ServerImpl server;
    private String reason = null;
    private String name = null;
    private FileContainer image = null;
    private Collection<Role> whitelist = null;

    public CustomEmojiBuilderDelegateImpl(ServerImpl server) {
        this.server = server;
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
    public void setImage(Icon image) {
        this.image = image == null ? null : new FileContainer(image);
    }

    @Override
    public void setImage(URL image) {
        this.image = image == null ? null : new FileContainer(image);
    }

    @Override
    public void setImage(File image) {
        this.image = image == null ? null : new FileContainer(image);
    }

    @Override
    public void setImage(BufferedImage image) {
        this.image = image == null ? null : new FileContainer(image, "png");
    }

    @Override
    public void setImage(BufferedImage image, String type) {
        this.image = image == null ? null : new FileContainer(image, type);
    }

    @Override
    public void setImage(byte[] image) {
        this.image = image == null ? null : new FileContainer(image, "png");
    }

    @Override
    public void setImage(byte[] image, String type) {
        this.image = image == null ? null : new FileContainer(image, type);
    }

    @Override
    public void setImage(InputStream image) {
        this.image = image == null ? null : new FileContainer(image, "png");
    }

    @Override
    public void setImage(InputStream image, String type) {
        this.image = image == null ? null : new FileContainer(image, type);
    }

    @Override
    public void addRoleToWhitelist(Role role) {
        if (this.whitelist == null) {
            this.whitelist = new HashSet<Role>();
        }
        this.whitelist.add(role);
    }

    @Override
    public void setWhitelist(Collection<Role> roles) {
        this.whitelist = roles == null ? null : new HashSet<Role>(roles);
    }

    @Override
    public void setWhitelist(Role ... roles) {
        this.setWhitelist(roles == null ? null : Arrays.asList(roles));
    }

    @Override
    public CompletableFuture<KnownCustomEmoji> create() {
        if (this.name == null) {
            throw new IllegalStateException("The name is no optional parameter!");
        }
        if (this.image == null) {
            throw new IllegalStateException("The image is no optional parameter!");
        }
        ObjectNode body = JsonNodeFactory.instance.objectNode().put("name", this.name);
        if (this.whitelist != null) {
            ArrayNode jsonRoles = body.putArray("roles");
            this.whitelist.stream().map(DiscordEntity::getIdAsString).forEach(jsonRoles::add);
        }
        return ((CompletableFuture)this.image.asByteArray(this.server.getApi()).thenAccept(bytes -> {
            String base64Icon = "data:image/" + this.image.getFileType() + ";base64," + Base64.getEncoder().encodeToString((byte[])bytes);
            body.put("image", base64Icon);
        })).thenCompose(aVoid -> new RestRequest(this.server.getApi(), RestMethod.POST, RestEndpoint.CUSTOM_EMOJI).setUrlParameters(this.server.getIdAsString()).setBody(body).setAuditLogReason(this.reason).execute(result -> ((DiscordApiImpl)this.server.getApi()).getOrCreateKnownCustomEmoji(this.server, result.getJsonBody())));
    }
}

