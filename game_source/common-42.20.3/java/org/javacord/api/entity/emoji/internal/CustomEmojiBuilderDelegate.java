/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.emoji.internal;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.permission.Role;

public interface CustomEmojiBuilderDelegate {
    public void setAuditLogReason(String var1);

    public void setName(String var1);

    public void setImage(Icon var1);

    public void setImage(URL var1);

    public void setImage(File var1);

    public void setImage(BufferedImage var1);

    public void setImage(BufferedImage var1, String var2);

    public void setImage(byte[] var1);

    public void setImage(byte[] var1, String var2);

    public void setImage(InputStream var1);

    public void setImage(InputStream var1, String var2);

    public void addRoleToWhitelist(Role var1);

    public void setWhitelist(Collection<Role> var1);

    public void setWhitelist(Role ... var1);

    public CompletableFuture<KnownCustomEmoji> create();
}

