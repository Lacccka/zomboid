/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.server.internal;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Region;
import org.javacord.api.entity.server.DefaultMessageNotificationLevel;
import org.javacord.api.entity.server.ExplicitContentFilterLevel;
import org.javacord.api.entity.server.VerificationLevel;

public interface ServerBuilderDelegate {
    public void setName(String var1);

    public void setRegion(Region var1);

    public void setExplicitContentFilterLevel(ExplicitContentFilterLevel var1);

    public void setVerificationLevel(VerificationLevel var1);

    public void setDefaultMessageNotificationLevel(DefaultMessageNotificationLevel var1);

    public void setAfkTimeoutInSeconds(int var1);

    public void setIcon(BufferedImage var1);

    public void setIcon(BufferedImage var1, String var2);

    public void setIcon(File var1);

    public void setIcon(Icon var1);

    public void setIcon(URL var1);

    public void setIcon(byte[] var1);

    public void setIcon(byte[] var1, String var2);

    public void setIcon(InputStream var1);

    public void setIcon(InputStream var1, String var2);

    public CompletableFuture<Long> create();
}

