/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.webhook.internal;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.webhook.IncomingWebhook;

public interface WebhookBuilderDelegate {
    public void setAuditLogReason(String var1);

    public void setName(String var1);

    public void setAvatar(BufferedImage var1);

    public void setAvatar(BufferedImage var1, String var2);

    public void setAvatar(File var1);

    public void setAvatar(Icon var1);

    public void setAvatar(URL var1);

    public void setAvatar(byte[] var1);

    public void setAvatar(byte[] var1, String var2);

    public void setAvatar(InputStream var1);

    public void setAvatar(InputStream var1, String var2);

    public CompletableFuture<IncomingWebhook> create();
}

