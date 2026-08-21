/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.server.internal;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Region;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.DefaultMessageNotificationLevel;
import org.javacord.api.entity.server.ExplicitContentFilterLevel;
import org.javacord.api.entity.server.VerificationLevel;
import org.javacord.api.entity.user.User;

public interface ServerUpdaterDelegate {
    public void setAuditLogReason(String var1);

    public void setName(String var1);

    public void setRegion(Region var1);

    public void setExplicitContentFilterLevel(ExplicitContentFilterLevel var1);

    public void setVerificationLevel(VerificationLevel var1);

    public void setDefaultMessageNotificationLevel(DefaultMessageNotificationLevel var1);

    public void setAfkChannel(ServerVoiceChannel var1);

    public void removeAfkChannel();

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

    public void removeIcon();

    public void setOwner(User var1);

    public void setSplash(BufferedImage var1);

    public void setSplash(BufferedImage var1, String var2);

    public void setSplash(File var1);

    public void setSplash(Icon var1);

    public void setSplash(URL var1);

    public void setSplash(byte[] var1);

    public void setSplash(byte[] var1, String var2);

    public void setSplash(InputStream var1);

    public void setSplash(InputStream var1, String var2);

    public void removeSplash();

    public void setBanner(BufferedImage var1);

    public void setBanner(BufferedImage var1, String var2);

    public void setBanner(File var1);

    public void setBanner(Icon var1);

    public void setBanner(URL var1);

    public void setBanner(byte[] var1);

    public void setBanner(byte[] var1, String var2);

    public void setBanner(InputStream var1);

    public void setBanner(InputStream var1, String var2);

    public void removeBanner();

    public void setRulesChannel(ServerTextChannel var1);

    public void removeRulesChannel();

    public void setModeratorsOnlyChannel(ServerTextChannel var1);

    public void removeModeratorsOnlyChannel();

    public void setPreferredLocale(Locale var1);

    public void setSystemChannel(ServerTextChannel var1);

    public void removeSystemChannel();

    public void setNickname(User var1, String var2);

    public void setUserTimeout(User var1, Instant var2);

    public void setMuted(User var1, boolean var2);

    public void setDeafened(User var1, boolean var2);

    public void setVoiceChannel(User var1, ServerVoiceChannel var2);

    public void reorderRoles(List<Role> var1);

    public void addRoleToUser(User var1, Role var2);

    public void addRolesToUser(User var1, Collection<Role> var2);

    public void removeRoleFromUser(User var1, Role var2);

    public void removeRolesFromUser(User var1, Collection<Role> var2);

    public void removeAllRolesFromUser(User var1);

    public CompletableFuture<Void> update();
}

