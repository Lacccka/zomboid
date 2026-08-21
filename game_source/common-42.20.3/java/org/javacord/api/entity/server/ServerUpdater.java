/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
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
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.VerificationLevel;
import org.javacord.api.entity.server.internal.ServerUpdaterDelegate;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.internal.DelegateFactory;

public class ServerUpdater {
    private final ServerUpdaterDelegate delegate;

    public ServerUpdater(Server server) {
        this.delegate = DelegateFactory.createServerUpdaterDelegate(server);
    }

    public ServerUpdater setAuditLogReason(String reason) {
        this.delegate.setAuditLogReason(reason);
        return this;
    }

    public ServerUpdater setName(String name) {
        this.delegate.setName(name);
        return this;
    }

    public ServerUpdater setRegion(Region region) {
        this.delegate.setRegion(region);
        return this;
    }

    public ServerUpdater setExplicitContentFilterLevel(ExplicitContentFilterLevel explicitContentFilterLevel) {
        this.delegate.setExplicitContentFilterLevel(explicitContentFilterLevel);
        return this;
    }

    public ServerUpdater setVerificationLevel(VerificationLevel verificationLevel) {
        this.delegate.setVerificationLevel(verificationLevel);
        return this;
    }

    public ServerUpdater setDefaultMessageNotificationLevel(DefaultMessageNotificationLevel defaultMessageNotificationLevel) {
        this.delegate.setDefaultMessageNotificationLevel(defaultMessageNotificationLevel);
        return this;
    }

    public ServerUpdater setAfkChannel(ServerVoiceChannel afkChannel) {
        this.delegate.setAfkChannel(afkChannel);
        return this;
    }

    public ServerUpdater removeAfkChannel() {
        this.delegate.removeAfkChannel();
        return this;
    }

    public ServerUpdater setAfkTimeoutInSeconds(int afkTimeout) {
        this.delegate.setAfkTimeoutInSeconds(afkTimeout);
        return this;
    }

    public ServerUpdater setIcon(BufferedImage icon) {
        this.delegate.setIcon(icon);
        return this;
    }

    public ServerUpdater setIcon(BufferedImage icon, String fileType) {
        this.delegate.setIcon(icon, fileType);
        return this;
    }

    public ServerUpdater setIcon(File icon) {
        this.delegate.setIcon(icon);
        return this;
    }

    public ServerUpdater setIcon(Icon icon) {
        this.delegate.setIcon(icon);
        return this;
    }

    public ServerUpdater setIcon(URL icon) {
        this.delegate.setIcon(icon);
        return this;
    }

    public ServerUpdater setIcon(byte[] icon) {
        this.delegate.setIcon(icon);
        return this;
    }

    public ServerUpdater setIcon(byte[] icon, String fileType) {
        this.delegate.setIcon(icon, fileType);
        return this;
    }

    public ServerUpdater setIcon(InputStream icon) {
        this.delegate.setIcon(icon);
        return this;
    }

    public ServerUpdater setIcon(InputStream icon, String fileType) {
        this.delegate.setIcon(icon, fileType);
        return this;
    }

    public ServerUpdater removeIcon() {
        this.delegate.removeIcon();
        return this;
    }

    public ServerUpdater setOwner(User owner) {
        this.delegate.setOwner(owner);
        return this;
    }

    public ServerUpdater setSplash(BufferedImage splash) {
        this.delegate.setSplash(splash);
        return this;
    }

    public ServerUpdater setSplash(BufferedImage splash, String fileType) {
        this.delegate.setSplash(splash, fileType);
        return this;
    }

    public ServerUpdater setSplash(File splash) {
        this.delegate.setSplash(splash);
        return this;
    }

    public ServerUpdater setSplash(Icon splash) {
        this.delegate.setSplash(splash);
        return this;
    }

    public ServerUpdater setSplash(URL splash) {
        this.delegate.setSplash(splash);
        return this;
    }

    public ServerUpdater setSplash(byte[] splash) {
        this.delegate.setSplash(splash);
        return this;
    }

    public ServerUpdater setSplash(byte[] splash, String fileType) {
        this.delegate.setSplash(splash, fileType);
        return this;
    }

    public ServerUpdater setSplash(InputStream splash) {
        this.delegate.setSplash(splash);
        return this;
    }

    public ServerUpdater setSplash(InputStream splash, String fileType) {
        this.delegate.setSplash(splash, fileType);
        return this;
    }

    public ServerUpdater removeSplash() {
        this.delegate.removeSplash();
        return this;
    }

    public ServerUpdater setBanner(BufferedImage banner) {
        this.delegate.setBanner(banner);
        return this;
    }

    public ServerUpdater setBanner(BufferedImage banner, String fileType) {
        this.delegate.setBanner(banner, fileType);
        return this;
    }

    public ServerUpdater setBanner(File banner) {
        this.delegate.setBanner(banner);
        return this;
    }

    public ServerUpdater setBanner(Icon banner) {
        this.delegate.setBanner(banner);
        return this;
    }

    public ServerUpdater setBanner(URL banner) {
        this.delegate.setBanner(banner);
        return this;
    }

    public ServerUpdater setBanner(byte[] banner) {
        this.delegate.setBanner(banner);
        return this;
    }

    public ServerUpdater setBanner(byte[] banner, String fileType) {
        this.delegate.setBanner(banner, fileType);
        return this;
    }

    public ServerUpdater setBanner(InputStream banner) {
        this.delegate.setBanner(banner);
        return this;
    }

    public ServerUpdater setBanner(InputStream banner, String fileType) {
        this.delegate.setBanner(banner, fileType);
        return this;
    }

    public ServerUpdater removeBanner() {
        this.delegate.removeBanner();
        return this;
    }

    public ServerUpdater setRulesChannel(ServerTextChannel rulesChannel) {
        this.delegate.setRulesChannel(rulesChannel);
        return this;
    }

    public ServerUpdater removeRulesChannel() {
        this.delegate.removeRulesChannel();
        return this;
    }

    public ServerUpdater setModeratorsOnlyChannel(ServerTextChannel moderatorsOnlyChannel) {
        this.delegate.setModeratorsOnlyChannel(moderatorsOnlyChannel);
        return this;
    }

    public ServerUpdater removeModeratorsOnlyChannel() {
        this.delegate.removeModeratorsOnlyChannel();
        return this;
    }

    public ServerUpdater setPreferredLocale(Locale locale) {
        this.delegate.setPreferredLocale(locale);
        return this;
    }

    public ServerUpdater setSystemChannel(ServerTextChannel systemChannel) {
        this.delegate.setSystemChannel(systemChannel);
        return this;
    }

    public ServerUpdater removeSystemChannel() {
        this.delegate.removeSystemChannel();
        return this;
    }

    public ServerUpdater setNickname(User user, String nickname) {
        this.delegate.setNickname(user, nickname);
        return this;
    }

    public ServerUpdater setUserTimeout(User user, Instant timeout2) {
        this.delegate.setUserTimeout(user, timeout2);
        return this;
    }

    public ServerUpdater setUserTimeout(User user, Duration duration) {
        return this.setUserTimeout(user, Instant.now().plus(duration));
    }

    public ServerUpdater removeUserTimeout(User user) {
        this.delegate.setUserTimeout(user, Instant.MIN);
        return this;
    }

    public ServerUpdater setMuted(User user, boolean muted) {
        this.delegate.setMuted(user, muted);
        return this;
    }

    public ServerUpdater setDeafened(User user, boolean deafened) {
        this.delegate.setDeafened(user, deafened);
        return this;
    }

    public ServerUpdater setVoiceChannel(User user, ServerVoiceChannel channel) {
        this.delegate.setVoiceChannel(user, channel);
        return this;
    }

    public ServerUpdater reorderRoles(List<Role> roles) {
        this.delegate.reorderRoles(roles);
        return this;
    }

    public ServerUpdater addRoleToUser(User user, Role role) {
        this.delegate.addRoleToUser(user, role);
        return this;
    }

    public ServerUpdater addRolesToUser(User user, Collection<Role> roles) {
        this.delegate.addRolesToUser(user, roles);
        return this;
    }

    public ServerUpdater removeRoleFromUser(User user, Role role) {
        this.delegate.removeRoleFromUser(user, role);
        return this;
    }

    public ServerUpdater removeRolesFromUser(User user, Collection<Role> roles) {
        this.delegate.removeRolesFromUser(user, roles);
        return this;
    }

    public ServerUpdater removeAllRolesFromUser(User user) {
        this.delegate.removeAllRolesFromUser(user);
        return this;
    }

    public CompletableFuture<Void> update() {
        return this.delegate.update();
    }
}

