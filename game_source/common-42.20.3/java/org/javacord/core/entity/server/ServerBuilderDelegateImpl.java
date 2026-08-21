/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.server;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Region;
import org.javacord.api.entity.server.DefaultMessageNotificationLevel;
import org.javacord.api.entity.server.ExplicitContentFilterLevel;
import org.javacord.api.entity.server.VerificationLevel;
import org.javacord.api.entity.server.internal.ServerBuilderDelegate;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.FileContainer;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class ServerBuilderDelegateImpl
implements ServerBuilderDelegate {
    private final DiscordApiImpl api;
    private String name = null;
    private Region region = null;
    private ExplicitContentFilterLevel explicitContentFilterLevel = null;
    private VerificationLevel verificationLevel = null;
    private DefaultMessageNotificationLevel defaultMessageNotificationLevel = null;
    private Integer afkTimeout = null;
    private FileContainer icon = null;

    public ServerBuilderDelegateImpl(DiscordApiImpl api) {
        this.api = api;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setRegion(Region region) {
        this.region = region;
    }

    @Override
    public void setExplicitContentFilterLevel(ExplicitContentFilterLevel explicitContentFilterLevel) {
        this.explicitContentFilterLevel = explicitContentFilterLevel;
    }

    @Override
    public void setVerificationLevel(VerificationLevel verificationLevel) {
        this.verificationLevel = verificationLevel;
    }

    @Override
    public void setDefaultMessageNotificationLevel(DefaultMessageNotificationLevel defaultMessageNotificationLevel) {
        this.defaultMessageNotificationLevel = defaultMessageNotificationLevel;
    }

    @Override
    public void setAfkTimeoutInSeconds(int afkTimeout) {
        this.afkTimeout = afkTimeout;
    }

    @Override
    public void setIcon(BufferedImage icon) {
        this.icon = icon == null ? null : new FileContainer(icon, "png");
    }

    @Override
    public void setIcon(BufferedImage icon, String fileType) {
        this.icon = icon == null ? null : new FileContainer(icon, fileType);
    }

    @Override
    public void setIcon(File icon) {
        this.icon = icon == null ? null : new FileContainer(icon);
    }

    @Override
    public void setIcon(Icon icon) {
        this.icon = icon == null ? null : new FileContainer(icon);
    }

    @Override
    public void setIcon(URL icon) {
        this.icon = icon == null ? null : new FileContainer(icon);
    }

    @Override
    public void setIcon(byte[] icon) {
        this.icon = icon == null ? null : new FileContainer(icon, "png");
    }

    @Override
    public void setIcon(byte[] icon, String fileType) {
        this.icon = icon == null ? null : new FileContainer(icon, fileType);
    }

    @Override
    public void setIcon(InputStream icon) {
        this.icon = icon == null ? null : new FileContainer(icon, "png");
    }

    @Override
    public void setIcon(InputStream icon, String fileType) {
        this.icon = icon == null ? null : new FileContainer(icon, fileType);
    }

    @Override
    public CompletableFuture<Long> create() {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        if (this.name != null) {
            body.put("name", this.name);
        }
        if (this.region != null) {
            body.put("region", this.region.getKey());
        }
        if (this.explicitContentFilterLevel != null) {
            body.put("explicit_content_filter", this.explicitContentFilterLevel.getId());
        }
        if (this.verificationLevel != null) {
            body.put("verification_level", this.verificationLevel.getId());
        }
        if (this.defaultMessageNotificationLevel != null) {
            body.put("default_message_notifications", this.defaultMessageNotificationLevel.getId());
        }
        if (this.afkTimeout != null) {
            body.put("afk_timeout", (int)this.afkTimeout);
        }
        if (this.icon != null) {
            return ((CompletableFuture)this.icon.asByteArray(this.api).thenAccept(bytes -> {
                String base64Icon = "data:image/" + this.icon.getFileType() + ";base64," + Base64.getEncoder().encodeToString((byte[])bytes);
                body.put("icon", base64Icon);
            })).thenCompose(aVoid -> new RestRequest(this.api, RestMethod.POST, RestEndpoint.SERVER).setBody(body).execute(result -> result.getJsonBody().get("id").asLong()));
        }
        return new RestRequest(this.api, RestMethod.POST, RestEndpoint.SERVER).setBody(body).execute(result -> result.getJsonBody().get("id").asLong());
    }
}

