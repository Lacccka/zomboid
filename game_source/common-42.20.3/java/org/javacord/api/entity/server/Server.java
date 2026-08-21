/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.server;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.Deletable;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Nameable;
import org.javacord.api.entity.Region;
import org.javacord.api.entity.UpdatableFromCache;
import org.javacord.api.entity.VanityUrlCode;
import org.javacord.api.entity.auditlog.AuditLog;
import org.javacord.api.entity.auditlog.AuditLogActionType;
import org.javacord.api.entity.auditlog.AuditLogEntry;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ChannelCategoryBuilder;
import org.javacord.api.entity.channel.RegularServerChannel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerForumChannel;
import org.javacord.api.entity.channel.ServerStageVoiceChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerTextChannelBuilder;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.ServerVoiceChannelBuilder;
import org.javacord.api.entity.channel.UnknownRegularServerChannel;
import org.javacord.api.entity.channel.UnknownServerChannel;
import org.javacord.api.entity.emoji.CustomEmojiBuilder;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.permission.RoleBuilder;
import org.javacord.api.entity.server.ActiveThreads;
import org.javacord.api.entity.server.Ban;
import org.javacord.api.entity.server.BoostLevel;
import org.javacord.api.entity.server.DefaultMessageNotificationLevel;
import org.javacord.api.entity.server.ExplicitContentFilterLevel;
import org.javacord.api.entity.server.MultiFactorAuthenticationLevel;
import org.javacord.api.entity.server.NsfwLevel;
import org.javacord.api.entity.server.ServerFeature;
import org.javacord.api.entity.server.ServerUpdater;
import org.javacord.api.entity.server.SystemChannelFlag;
import org.javacord.api.entity.server.VerificationLevel;
import org.javacord.api.entity.server.invite.RichInvite;
import org.javacord.api.entity.server.invite.WelcomeScreen;
import org.javacord.api.entity.sticker.Sticker;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.entity.webhook.Webhook;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.listener.server.ServerAttachableListenerManager;

public interface Server
extends DiscordEntity,
Nameable,
Deletable,
UpdatableFromCache<Server>,
ServerAttachableListenerManager {
    public Optional<AudioConnection> getAudioConnection();

    public Set<ServerFeature> getFeatures();

    public BoostLevel getBoostLevel();

    public int getBoostCount();

    public Optional<ServerTextChannel> getRulesChannel();

    public Optional<String> getDescription();

    public NsfwLevel getNsfwLevel();

    public Optional<ServerTextChannel> getModeratorsOnlyChannel();

    public Optional<VanityUrlCode> getVanityUrlCode();

    public Optional<Icon> getDiscoverySplash();

    public Locale getPreferredLocale();

    public Region getRegion();

    public Optional<String> getNickname(User var1);

    public Optional<Instant> getServerBoostingSinceTimestamp(User var1);

    public Optional<Instant> getTimeout(User var1);

    default public Optional<Instant> getActiveTimeout(User user) {
        return this.getTimeout(user).filter(Instant.now()::isBefore);
    }

    public Optional<String> getUserServerAvatarHash(User var1);

    public Optional<Icon> getUserServerAvatar(User var1);

    public Optional<Icon> getUserServerAvatar(User var1, int var2);

    public boolean isPending(long var1);

    default public boolean areYouSelfMuted() {
        return this.isSelfMuted(this.getApi().getYourself());
    }

    public boolean isSelfMuted(long var1);

    default public boolean isSelfMuted(User user) {
        return this.isSelfMuted(user.getId());
    }

    default public boolean areYouSelfDeafened() {
        return this.isSelfDeafened(this.getApi().getYourself());
    }

    public boolean isSelfDeafened(long var1);

    default public boolean isSelfDeafened(User user) {
        return this.isSelfDeafened(user.getId());
    }

    default public boolean areYouMuted() {
        return this.isMuted(this.getApi().getYourself());
    }

    public boolean isMuted(long var1);

    default public boolean isMuted(User user) {
        return this.isMuted(user.getId());
    }

    default public boolean areYouDeafened() {
        return this.isDeafened(this.getApi().getYourself());
    }

    public boolean isDeafened(long var1);

    default public boolean isDeafened(User user) {
        return this.isDeafened(user.getId());
    }

    default public String getDisplayName(User user) {
        return user.getDisplayName(this);
    }

    public Optional<Instant> getJoinedAtTimestamp(User var1);

    public boolean isLarge();

    public int getMemberCount();

    public Optional<User> getOwner();

    default public CompletableFuture<User> requestOwner() {
        return this.getApi().getUserById(this.getOwnerId());
    }

    public long getOwnerId();

    public Optional<Long> getApplicationId();

    public VerificationLevel getVerificationLevel();

    public ExplicitContentFilterLevel getExplicitContentFilterLevel();

    public DefaultMessageNotificationLevel getDefaultMessageNotificationLevel();

    public MultiFactorAuthenticationLevel getMultiFactorAuthenticationLevel();

    public Optional<Icon> getIcon();

    public Optional<Icon> getSplash();

    public Optional<ServerTextChannel> getSystemChannel();

    public Optional<ServerVoiceChannel> getAfkChannel();

    public int getAfkTimeoutInSeconds();

    public CompletableFuture<Integer> getPruneCount(int var1);

    default public CompletableFuture<Integer> pruneMembers(int days) {
        return this.pruneMembers(days, null);
    }

    public CompletableFuture<Integer> pruneMembers(int var1, String var2);

    public CompletableFuture<Set<RichInvite>> getInvites();

    public boolean hasAllMembersInCache();

    public void requestMembersChunks();

    public Set<User> getMembers();

    public Optional<User> getMemberById(long var1);

    default public Optional<User> getMemberById(String id) {
        try {
            return this.getMemberById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public Optional<User> getMemberByDiscriminatedName(String discriminatedName) {
        String[] nameAndDiscriminator = discriminatedName.split("#", 2);
        return nameAndDiscriminator.length > 1 ? this.getMemberByNameAndDiscriminator(nameAndDiscriminator[0], nameAndDiscriminator[1]) : Optional.empty();
    }

    default public Optional<User> getMemberByDiscriminatedNameIgnoreCase(String discriminatedName) {
        String[] nameAndDiscriminator = discriminatedName.split("#", 2);
        return nameAndDiscriminator.length > 1 ? this.getMemberByNameAndDiscriminatorIgnoreCase(nameAndDiscriminator[0], nameAndDiscriminator[1]) : Optional.empty();
    }

    default public Optional<User> getMemberByNameAndDiscriminator(String name, String discriminator) {
        return this.getMembersByName(name).stream().filter(user -> user.getDiscriminator().equals(discriminator)).findAny();
    }

    default public Optional<User> getMemberByNameAndDiscriminatorIgnoreCase(String name, String discriminator) {
        return this.getMembersByNameIgnoreCase(name).stream().filter(user -> user.getDiscriminator().equalsIgnoreCase(discriminator)).findAny();
    }

    default public Set<User> getMembersByName(String name) {
        return Collections.unmodifiableSet(this.getMembers().stream().filter(user -> user.getName().equals(name)).collect(Collectors.toSet()));
    }

    default public Set<User> getMembersByNameIgnoreCase(String name) {
        return Collections.unmodifiableSet(this.getMembers().stream().filter(user -> user.getName().equalsIgnoreCase(name)).collect(Collectors.toSet()));
    }

    default public Set<User> getMembersByNickname(String nickname) {
        return Collections.unmodifiableSet(this.getMembers().stream().filter(user -> user.getNickname(this).map(nickname::equals).orElse(false)).collect(Collectors.toSet()));
    }

    default public Set<User> getMembersByNicknameIgnoreCase(String nickname) {
        return Collections.unmodifiableSet(this.getMembers().stream().filter(user -> user.getNickname(this).map(nickname::equalsIgnoreCase).orElse(false)).collect(Collectors.toSet()));
    }

    default public Set<User> getMembersByDisplayName(String displayName) {
        return Collections.unmodifiableSet(this.getMembers().stream().filter(user -> user.getDisplayName(this).equals(displayName)).collect(Collectors.toSet()));
    }

    default public Set<User> getMembersByDisplayNameIgnoreCase(String displayName) {
        return Collections.unmodifiableSet(this.getMembers().stream().filter(user -> user.getDisplayName(this).equalsIgnoreCase(displayName)).collect(Collectors.toSet()));
    }

    public boolean isMember(User var1);

    public List<Role> getRoles();

    public List<Role> getRoles(User var1);

    public Optional<Role> getRoleById(long var1);

    default public Optional<Role> getRoleById(String id) {
        try {
            return this.getRoleById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public Role getEveryoneRole() {
        return this.getRoleById(this.getId()).orElseThrow(AssertionError::new);
    }

    default public List<Role> getRolesByName(String name) {
        return Collections.unmodifiableList(this.getRoles().stream().filter(role -> role.getName().equals(name)).collect(Collectors.toList()));
    }

    default public List<Role> getRolesByNameIgnoreCase(String name) {
        return Collections.unmodifiableList(this.getRoles().stream().filter(role -> role.getName().equalsIgnoreCase(name)).collect(Collectors.toList()));
    }

    default public Optional<Color> getRoleColor(User user) {
        return user.getRoles(this).stream().filter(role -> role.getColor().isPresent()).max(Comparator.comparingInt(Role::getRawPosition)).flatMap(Role::getColor);
    }

    default public Permissions getPermissions(User user) {
        PermissionsBuilder builder = new PermissionsBuilder();
        this.getAllowedPermissions(user).forEach(type -> builder.setState((PermissionType)((Object)type), PermissionState.ALLOWED));
        return builder.build();
    }

    default public Set<PermissionType> getAllowedPermissions(User user) {
        HashSet<PermissionType> allowed = new HashSet<PermissionType>();
        if (this.isOwner(user)) {
            allowed.addAll(Arrays.asList(PermissionType.values()));
        } else {
            this.getRoles(user).forEach(role -> allowed.addAll(role.getAllowedPermissions()));
        }
        return Collections.unmodifiableSet(allowed);
    }

    default public Set<PermissionType> getUnsetPermissions(User user) {
        if (this.isOwner(user)) {
            return Collections.emptySet();
        }
        HashSet unset = new HashSet();
        this.getRoles(user).forEach(role -> unset.addAll(role.getUnsetPermissions()));
        return Collections.unmodifiableSet(unset);
    }

    default public boolean hasPermissions(User user, PermissionType ... type) {
        return this.getAllowedPermissions(user).containsAll(Arrays.asList(type));
    }

    default public boolean hasAnyPermission(User user, PermissionType ... type) {
        return this.getAllowedPermissions(user).stream().anyMatch(allowedPermissionType -> Arrays.stream(type).anyMatch(allowedPermissionType::equals));
    }

    default public CustomEmojiBuilder createCustomEmojiBuilder() {
        return new CustomEmojiBuilder(this);
    }

    default public ServerUpdater createUpdater() {
        return new ServerUpdater(this);
    }

    default public CompletableFuture<Void> updateName(String name) {
        return this.createUpdater().setName(name).update();
    }

    default public CompletableFuture<Void> updateRegion(Region region) {
        return this.createUpdater().setRegion(region).update();
    }

    default public CompletableFuture<Void> updateExplicitContentFilterLevel(ExplicitContentFilterLevel explicitContentFilterLevel) {
        return this.createUpdater().setExplicitContentFilterLevel(explicitContentFilterLevel).update();
    }

    default public CompletableFuture<Void> updateVerificationLevel(VerificationLevel verificationLevel) {
        return this.createUpdater().setVerificationLevel(verificationLevel).update();
    }

    default public CompletableFuture<Void> updateDefaultMessageNotificationLevel(DefaultMessageNotificationLevel defaultMessageNotificationLevel) {
        return this.createUpdater().setDefaultMessageNotificationLevel(defaultMessageNotificationLevel).update();
    }

    default public CompletableFuture<Void> updateAfkChannel(ServerVoiceChannel afkChannel) {
        return this.createUpdater().setAfkChannel(afkChannel).update();
    }

    default public CompletableFuture<Void> removeAfkChannel() {
        return this.createUpdater().removeAfkChannel().update();
    }

    default public CompletableFuture<Void> updateAfkTimeoutInSeconds(int afkTimeout) {
        return this.createUpdater().setAfkTimeoutInSeconds(afkTimeout).update();
    }

    default public CompletableFuture<Void> updateIcon(BufferedImage icon) {
        return this.createUpdater().setIcon(icon).update();
    }

    default public CompletableFuture<Void> updateIcon(BufferedImage icon, String fileType) {
        return this.createUpdater().setIcon(icon, fileType).update();
    }

    default public CompletableFuture<Void> updateIcon(File icon) {
        return this.createUpdater().setIcon(icon).update();
    }

    default public CompletableFuture<Void> updateIcon(Icon icon) {
        return this.createUpdater().setIcon(icon).update();
    }

    default public CompletableFuture<Void> updateIcon(URL icon) {
        return this.createUpdater().setIcon(icon).update();
    }

    default public CompletableFuture<Void> updateIcon(byte[] icon) {
        return this.createUpdater().setIcon(icon).update();
    }

    default public CompletableFuture<Void> updateIcon(byte[] icon, String fileType) {
        return this.createUpdater().setIcon(icon, fileType).update();
    }

    default public CompletableFuture<Void> updateIcon(InputStream icon) {
        return this.createUpdater().setIcon(icon).update();
    }

    default public CompletableFuture<Void> updateIcon(InputStream icon, String fileType) {
        return this.createUpdater().setIcon(icon, fileType).update();
    }

    default public CompletableFuture<Void> removeIcon() {
        return this.createUpdater().removeIcon().update();
    }

    default public CompletableFuture<Void> updateOwner(User owner) {
        return this.createUpdater().setOwner(owner).update();
    }

    default public CompletableFuture<Void> updateSplash(BufferedImage splash) {
        return this.createUpdater().setSplash(splash).update();
    }

    default public CompletableFuture<Void> updateSplash(BufferedImage splash, String fileType) {
        return this.createUpdater().setSplash(splash, fileType).update();
    }

    default public CompletableFuture<Void> updateSplash(File splash) {
        return this.createUpdater().setSplash(splash).update();
    }

    default public CompletableFuture<Void> updateSplash(Icon splash) {
        return this.createUpdater().setSplash(splash).update();
    }

    default public CompletableFuture<Void> updateSplash(URL splash) {
        return this.createUpdater().setSplash(splash).update();
    }

    default public CompletableFuture<Void> updateSplash(byte[] splash) {
        return this.createUpdater().setSplash(splash).update();
    }

    default public CompletableFuture<Void> updateSplash(byte[] splash, String fileType) {
        return this.createUpdater().setSplash(splash, fileType).update();
    }

    default public CompletableFuture<Void> updateSplash(InputStream splash) {
        return this.createUpdater().setSplash(splash).update();
    }

    default public CompletableFuture<Void> updateSplash(InputStream splash, String fileType) {
        return this.createUpdater().setSplash(splash, fileType).update();
    }

    default public CompletableFuture<Void> removeSplash() {
        return this.createUpdater().removeSplash().update();
    }

    default public CompletableFuture<Void> updateBanner(BufferedImage banner) {
        return this.createUpdater().setBanner(banner).update();
    }

    default public CompletableFuture<Void> updateBanner(BufferedImage banner, String fileType) {
        return this.createUpdater().setBanner(banner, fileType).update();
    }

    default public CompletableFuture<Void> updateBanner(File banner) {
        return this.createUpdater().setBanner(banner).update();
    }

    default public CompletableFuture<Void> updateBanner(Icon banner) {
        return this.createUpdater().setBanner(banner).update();
    }

    default public CompletableFuture<Void> updateBanner(URL banner) {
        return this.createUpdater().setBanner(banner).update();
    }

    default public CompletableFuture<Void> updateBanner(byte[] banner) {
        return this.createUpdater().setBanner(banner).update();
    }

    default public CompletableFuture<Void> updateBanner(byte[] banner, String fileType) {
        return this.createUpdater().setBanner(banner, fileType).update();
    }

    default public CompletableFuture<Void> updateBanner(InputStream banner) {
        return this.createUpdater().setBanner(banner).update();
    }

    default public CompletableFuture<Void> updateBanner(InputStream banner, String fileType) {
        return this.createUpdater().setBanner(banner, fileType).update();
    }

    default public CompletableFuture<Void> removeBanner() {
        return this.createUpdater().removeBanner().update();
    }

    default public CompletableFuture<Void> setRulesChannel(ServerTextChannel rulesChannel) {
        return this.createUpdater().setRulesChannel(rulesChannel).update();
    }

    default public CompletableFuture<Void> removeRulesChannel() {
        return this.createUpdater().removeRulesChannel().update();
    }

    default public CompletableFuture<Void> setModeratorsOnlyChannel(ServerTextChannel moderatorsOnlyChannel) {
        return this.createUpdater().setModeratorsOnlyChannel(moderatorsOnlyChannel).update();
    }

    default public CompletableFuture<Void> removeModeratorsOnlyChannel() {
        return this.createUpdater().removeModeratorsOnlyChannel().update();
    }

    default public CompletableFuture<Void> updatePreferredLocale(Locale locale) {
        return this.createUpdater().setPreferredLocale(locale).update();
    }

    default public CompletableFuture<Void> setSystemChannel(ServerTextChannel systemChannel) {
        return this.createUpdater().setSystemChannel(systemChannel).update();
    }

    default public CompletableFuture<Void> removeSystemChannel() {
        return this.createUpdater().removeSystemChannel().update();
    }

    default public CompletableFuture<Void> updateNickname(User user, String nickname) {
        return this.createUpdater().setNickname(user, nickname).update();
    }

    default public CompletableFuture<Void> updateNickname(User user, String nickname, String reason) {
        return this.createUpdater().setNickname(user, nickname).setAuditLogReason(reason).update();
    }

    default public CompletableFuture<Void> resetNickname(User user) {
        return this.createUpdater().setNickname(user, null).update();
    }

    default public CompletableFuture<Void> resetNickname(User user, String reason) {
        return this.createUpdater().setNickname(user, null).setAuditLogReason(reason).update();
    }

    default public CompletableFuture<Void> timeoutUser(User user, Instant timeout2) {
        return this.createUpdater().setUserTimeout(user, timeout2).update();
    }

    default public CompletableFuture<Void> timeoutUser(User user, Instant timeout2, String reason) {
        return this.createUpdater().setUserTimeout(user, timeout2).setAuditLogReason(reason).update();
    }

    default public CompletableFuture<Void> timeoutUser(User user, Duration duration) {
        return this.createUpdater().setUserTimeout(user, Instant.now().plus(duration)).update();
    }

    default public CompletableFuture<Void> timeoutUser(User user, Duration duration, String reason) {
        return this.createUpdater().setUserTimeout(user, Instant.now().plus(duration)).setAuditLogReason(reason).update();
    }

    default public CompletableFuture<Void> removeUserTimeout(User user) {
        return this.createUpdater().setUserTimeout(user, Instant.MIN).update();
    }

    default public CompletableFuture<Void> removeUserTimeout(User user, String reason) {
        return this.createUpdater().setUserTimeout(user, Instant.MIN).setAuditLogReason(reason).update();
    }

    public CompletableFuture<Void> leave();

    default public CompletableFuture<Void> addRoleToUser(User user, Role role) {
        return this.addRoleToUser(user, role, null);
    }

    public CompletableFuture<Void> addRoleToUser(User var1, Role var2, String var3);

    default public CompletableFuture<Void> removeRoleFromUser(User user, Role role) {
        return this.removeRoleFromUser(user, role, null);
    }

    public CompletableFuture<Void> removeRoleFromUser(User var1, Role var2, String var3);

    default public CompletableFuture<Void> updateRoles(User user, Collection<Role> roles) {
        return this.createUpdater().removeAllRolesFromUser(user).addRolesToUser(user, roles).update();
    }

    default public CompletableFuture<Void> updateRoles(User user, Collection<Role> roles, String reason) {
        return this.createUpdater().removeAllRolesFromUser(user).addRolesToUser(user, roles).setAuditLogReason(reason).update();
    }

    default public CompletableFuture<Void> reorderRoles(List<Role> roles) {
        return this.reorderRoles(roles, null);
    }

    public CompletableFuture<Void> reorderRoles(List<Role> var1, String var2);

    default public CompletableFuture<Void> moveYourself(ServerVoiceChannel channel) {
        return this.moveUser(this.getApi().getYourself(), channel);
    }

    default public CompletableFuture<Void> moveUser(User user, ServerVoiceChannel channel) {
        return this.createUpdater().setVoiceChannel(user, channel).update();
    }

    default public CompletableFuture<Void> kickUserFromVoiceChannel(User user) {
        return this.createUpdater().setVoiceChannel(user, null).update();
    }

    public void selfMute();

    public void selfUnmute();

    default public CompletableFuture<Void> muteYourself() {
        return this.muteUser(this.getApi().getYourself());
    }

    default public CompletableFuture<Void> unmuteYourself() {
        return this.unmuteUser(this.getApi().getYourself());
    }

    default public CompletableFuture<Void> muteUser(User user) {
        return this.createUpdater().setMuted(user, true).update();
    }

    default public CompletableFuture<Void> muteUser(User user, String reason) {
        return this.createUpdater().setMuted(user, true).setAuditLogReason(reason).update();
    }

    default public CompletableFuture<Void> unmuteUser(User user) {
        return this.createUpdater().setMuted(user, false).update();
    }

    default public CompletableFuture<Void> unmuteUser(User user, String reason) {
        return this.createUpdater().setMuted(user, false).setAuditLogReason(reason).update();
    }

    public void selfDeafen();

    public void selfUndeafen();

    default public CompletableFuture<Void> deafenYourself() {
        return this.deafenUser(this.getApi().getYourself());
    }

    default public CompletableFuture<Void> undeafenYourself() {
        return this.undeafenUser(this.getApi().getYourself());
    }

    default public CompletableFuture<Void> deafenUser(User user) {
        return this.createUpdater().setDeafened(user, true).update();
    }

    default public CompletableFuture<Void> deafenUser(User user, String reason) {
        return this.createUpdater().setDeafened(user, true).setAuditLogReason(reason).update();
    }

    default public CompletableFuture<User> requestMember(User user) {
        return this.requestMember(user.getId());
    }

    public CompletableFuture<User> requestMember(long var1);

    default public CompletableFuture<Void> undeafenUser(User user) {
        return this.createUpdater().setDeafened(user, false).update();
    }

    default public CompletableFuture<Void> undeafenUser(User user, String reason) {
        return this.createUpdater().setDeafened(user, false).setAuditLogReason(reason).update();
    }

    default public CompletableFuture<Void> kickUser(User user) {
        return this.kickUser(user, null);
    }

    public CompletableFuture<Void> kickUser(User var1, String var2);

    default public CompletableFuture<Void> banUser(User user) {
        return this.banUser(user.getId(), 0L, TimeUnit.SECONDS, null);
    }

    default public CompletableFuture<Void> banUser(User user, long deleteMessageDuration, TimeUnit unit) {
        return this.banUser(user.getId(), deleteMessageDuration, unit);
    }

    default public CompletableFuture<Void> banUser(User user, long deleteMessageDuration, TimeUnit unit, String reason) {
        return this.banUser(user.getId(), deleteMessageDuration, unit, reason);
    }

    default public CompletableFuture<Void> banUser(User user, Duration duration) {
        return this.banUser(user.getId(), duration, null);
    }

    default public CompletableFuture<Void> banUser(User user, Duration duration, String reason) {
        return this.banUser(user.getId(), duration, reason);
    }

    default public CompletableFuture<Void> banUser(String userId) {
        return this.banUser(userId, null);
    }

    default public CompletableFuture<Void> banUser(long userId) {
        return this.banUser(Long.toUnsignedString(userId));
    }

    default public CompletableFuture<Void> banUser(String userId, long deleteMessageDuration, TimeUnit unit) {
        return this.banUser(userId, deleteMessageDuration, unit, null);
    }

    default public CompletableFuture<Void> banUser(long userId, long deleteMessageDuration, TimeUnit unit) {
        return this.banUser(Long.toUnsignedString(userId), deleteMessageDuration, unit);
    }

    default public CompletableFuture<Void> banUser(String userId, Duration duration) {
        return this.banUser(userId, duration, null);
    }

    default public CompletableFuture<Void> banUser(long userId, Duration duration) {
        return this.banUser(Long.toUnsignedString(userId), duration);
    }

    public CompletableFuture<Void> banUser(String var1, long var2, TimeUnit var4, String var5);

    default public CompletableFuture<Void> banUser(long userId, long deleteMessageDuration, TimeUnit unit, String reason) {
        return this.banUser(Long.toUnsignedString(userId), deleteMessageDuration, unit, reason);
    }

    default public CompletableFuture<Void> banUser(String userId, Duration duration, String reason) {
        return this.banUser(userId, duration.getSeconds(), TimeUnit.SECONDS, reason);
    }

    default public CompletableFuture<Void> banUser(long userId, Duration duration, String reason) {
        return this.banUser(Long.toUnsignedString(userId), duration, reason);
    }

    default public CompletableFuture<Void> unbanUser(User user) {
        return this.unbanUser(user.getId(), null);
    }

    default public CompletableFuture<Void> unbanUser(long userId) {
        return this.unbanUser(userId, null);
    }

    default public CompletableFuture<Void> unbanUser(String userId) {
        return this.unbanUser(userId, null);
    }

    default public CompletableFuture<Void> unbanUser(User user, String reason) {
        return this.unbanUser(user.getId(), reason);
    }

    public CompletableFuture<Void> unbanUser(long var1, String var3);

    default public CompletableFuture<Void> unbanUser(String userId, String reason) {
        try {
            return this.unbanUser(Long.parseLong(userId), reason);
        }
        catch (NumberFormatException e) {
            CompletableFuture<Void> future = new CompletableFuture<Void>();
            future.completeExceptionally(e);
            return future;
        }
    }

    default public CompletableFuture<Ban> requestBan(User user) {
        return this.requestBan(user.getId());
    }

    public CompletableFuture<Ban> requestBan(long var1);

    public CompletableFuture<Set<Ban>> getBans();

    public CompletableFuture<Set<Ban>> getBans(Integer var1, Long var2);

    public CompletableFuture<List<Webhook>> getWebhooks();

    public CompletableFuture<List<Webhook>> getAllIncomingWebhooks();

    public CompletableFuture<List<IncomingWebhook>> getIncomingWebhooks();

    public CompletableFuture<AuditLog> getAuditLog(int var1);

    public CompletableFuture<AuditLog> getAuditLog(int var1, AuditLogActionType var2);

    public CompletableFuture<AuditLog> getAuditLogBefore(int var1, AuditLogEntry var2);

    public CompletableFuture<AuditLog> getAuditLogBefore(int var1, AuditLogEntry var2, AuditLogActionType var3);

    default public boolean hasPermission(User user, PermissionType permission) {
        return this.getAllowedPermissions(user).contains((Object)permission);
    }

    public Set<KnownCustomEmoji> getCustomEmojis();

    default public Optional<KnownCustomEmoji> getCustomEmojiById(long id) {
        return this.getCustomEmojis().stream().filter(emoji -> emoji.getId() == id).findAny();
    }

    default public Optional<KnownCustomEmoji> getCustomEmojiById(String id) {
        try {
            return this.getCustomEmojiById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public Set<KnownCustomEmoji> getCustomEmojisByName(String name) {
        return Collections.unmodifiableSet(this.getCustomEmojis().stream().filter(emoji -> emoji.getName().equals(name)).collect(Collectors.toSet()));
    }

    default public Set<KnownCustomEmoji> getCustomEmojisByNameIgnoreCase(String name) {
        return Collections.unmodifiableSet(this.getCustomEmojis().stream().filter(emoji -> emoji.getName().equalsIgnoreCase(name)).collect(Collectors.toSet()));
    }

    public CompletableFuture<Set<SlashCommand>> getSlashCommands();

    public CompletableFuture<SlashCommand> getSlashCommandById(long var1);

    default public ChannelCategoryBuilder createChannelCategoryBuilder() {
        return new ChannelCategoryBuilder(this);
    }

    default public ServerTextChannelBuilder createTextChannelBuilder() {
        return new ServerTextChannelBuilder(this);
    }

    default public ServerVoiceChannelBuilder createVoiceChannelBuilder() {
        return new ServerVoiceChannelBuilder(this);
    }

    default public RoleBuilder createRoleBuilder() {
        return new RoleBuilder(this);
    }

    public List<ServerChannel> getChannels();

    public Set<ServerChannel> getUnorderedChannels();

    public List<RegularServerChannel> getRegularChannels();

    public List<ChannelCategory> getChannelCategories();

    public List<ServerTextChannel> getTextChannels();

    public List<ServerForumChannel> getForumChannels();

    public List<ServerVoiceChannel> getVoiceChannels();

    public List<ServerThreadChannel> getThreadChannels();

    public Optional<ServerChannel> getChannelById(long var1);

    default public Optional<ServerChannel> getChannelById(String id) {
        try {
            return this.getChannelById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public List<ServerChannel> getChannelsByName(String name) {
        return Collections.unmodifiableList(this.getChannels().stream().filter(channel -> channel.getName().equals(name)).collect(Collectors.toList()));
    }

    default public List<ServerChannel> getChannelsByNameIgnoreCase(String name) {
        return Collections.unmodifiableList(this.getChannels().stream().filter(channel -> channel.getName().equalsIgnoreCase(name)).collect(Collectors.toList()));
    }

    public Optional<RegularServerChannel> getRegularChannelById(long var1);

    default public Optional<RegularServerChannel> getRegularChannelById(String id) {
        try {
            return this.getRegularChannelById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public List<RegularServerChannel> getRegularChannelsByName(String name) {
        return Collections.unmodifiableList(this.getRegularChannels().stream().filter(channel -> channel.getName().equals(name)).collect(Collectors.toList()));
    }

    default public List<RegularServerChannel> getRegularChannelsByNameIgnoreCase(String name) {
        return Collections.unmodifiableList(this.getRegularChannels().stream().filter(channel -> channel.getName().equalsIgnoreCase(name)).collect(Collectors.toList()));
    }

    default public Optional<ChannelCategory> getChannelCategoryById(long id) {
        return this.getChannelById(id).filter(channel -> channel instanceof ChannelCategory).map(channel -> (ChannelCategory)channel);
    }

    default public Optional<ChannelCategory> getChannelCategoryById(String id) {
        try {
            return this.getChannelCategoryById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public List<ChannelCategory> getChannelCategoriesByName(String name) {
        return Collections.unmodifiableList(this.getChannelCategories().stream().filter(channel -> channel.getName().equals(name)).collect(Collectors.toList()));
    }

    default public List<ChannelCategory> getChannelCategoriesByNameIgnoreCase(String name) {
        return Collections.unmodifiableList(this.getChannelCategories().stream().filter(channel -> channel.getName().equalsIgnoreCase(name)).collect(Collectors.toList()));
    }

    default public Optional<UnknownServerChannel> getUnknownChannelById(long id) {
        return this.getChannelById(id).filter(channel -> channel instanceof UnknownServerChannel).map(channel -> (UnknownServerChannel)channel);
    }

    default public Optional<UnknownServerChannel> getUnknownChannelById(String id) {
        try {
            return this.getUnknownChannelById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public Optional<UnknownRegularServerChannel> getUnknownRegularChannelById(long id) {
        return this.getChannelById(id).filter(channel -> channel instanceof UnknownRegularServerChannel).map(channel -> (UnknownRegularServerChannel)channel);
    }

    default public Optional<UnknownRegularServerChannel> getUnknownRegularChannelById(String id) {
        try {
            return this.getUnknownRegularChannelById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public Optional<ServerTextChannel> getTextChannelById(long id) {
        return this.getChannelById(id).filter(channel -> channel instanceof ServerTextChannel).map(channel -> (ServerTextChannel)channel);
    }

    default public Optional<ServerTextChannel> getTextChannelById(String id) {
        try {
            return this.getTextChannelById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public Optional<ServerForumChannel> getForumChannelById(long id) {
        return this.getChannelById(id).filter(channel -> channel instanceof ServerForumChannel).map(channel -> (ServerForumChannel)channel);
    }

    default public Optional<ServerForumChannel> getForumChannelById(String id) {
        try {
            return this.getForumChannelById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public Optional<ServerThreadChannel> getThreadChannelById(long id) {
        return this.getChannelById(id).flatMap(Channel::asServerThreadChannel);
    }

    default public Optional<ServerThreadChannel> getThreadChannelById(String id) {
        try {
            return this.getThreadChannelById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public List<ServerTextChannel> getTextChannelsByName(String name) {
        return Collections.unmodifiableList(this.getTextChannels().stream().filter(channel -> channel.getName().equals(name)).collect(Collectors.toList()));
    }

    default public List<ServerTextChannel> getTextChannelsByNameIgnoreCase(String name) {
        return Collections.unmodifiableList(this.getTextChannels().stream().filter(channel -> channel.getName().equalsIgnoreCase(name)).collect(Collectors.toList()));
    }

    default public List<ServerForumChannel> getForumChannelsByName(String name) {
        return Collections.unmodifiableList(this.getForumChannels().stream().filter(channel -> channel.getName().equals(name)).collect(Collectors.toList()));
    }

    default public List<ServerForumChannel> getForumChannelsByNameIgnoreCase(String name) {
        return Collections.unmodifiableList(this.getForumChannels().stream().filter(channel -> channel.getName().equalsIgnoreCase(name)).collect(Collectors.toList()));
    }

    default public Optional<ServerVoiceChannel> getVoiceChannelById(long id) {
        return this.getChannelById(id).filter(channel -> channel instanceof ServerVoiceChannel).map(channel -> (ServerVoiceChannel)channel);
    }

    default public Optional<ServerVoiceChannel> getVoiceChannelById(String id) {
        try {
            return this.getVoiceChannelById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public Optional<ServerStageVoiceChannel> getStageVoiceChannelById(long id) {
        return this.getChannelById(id).filter(channel -> channel instanceof ServerStageVoiceChannel).map(channel -> (ServerStageVoiceChannel)channel);
    }

    default public Optional<ServerStageVoiceChannel> getStageVoiceChannelById(String id) {
        try {
            return this.getStageVoiceChannelById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public List<ServerVoiceChannel> getVoiceChannelsByName(String name) {
        return Collections.unmodifiableList(this.getVoiceChannels().stream().filter(channel -> channel.getName().equals(name)).collect(Collectors.toList()));
    }

    default public List<ServerVoiceChannel> getVoiceChannelsByNameIgnoreCase(String name) {
        return Collections.unmodifiableList(this.getVoiceChannels().stream().filter(channel -> channel.getName().equalsIgnoreCase(name)).collect(Collectors.toList()));
    }

    default public Optional<ServerVoiceChannel> getConnectedVoiceChannel(long userId) {
        return this.getVoiceChannels().stream().filter(serverVoiceChannel -> serverVoiceChannel.isConnected(userId)).findAny();
    }

    default public Optional<ServerVoiceChannel> getConnectedVoiceChannel(User user) {
        return this.getConnectedVoiceChannel(user.getId());
    }

    default public List<ServerChannel> getVisibleChannels(User user) {
        return Collections.unmodifiableList(this.getChannels().stream().filter(channel -> channel.canSee(user)).collect(Collectors.toList()));
    }

    default public Optional<Role> getHighestRole(User user) {
        List<Role> roles = this.getRoles(user);
        if (roles.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(roles.get(roles.size() - 1));
    }

    default public boolean isOwner(User user) {
        return this.getOwnerId() == user.getId();
    }

    default public boolean isAdmin(User user) {
        return this.hasPermission(user, PermissionType.ADMINISTRATOR);
    }

    default public boolean canCreateChannels(User user) {
        return this.hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.MANAGE_CHANNELS);
    }

    default public boolean canYouCreateChannels() {
        return this.canCreateChannels(this.getApi().getYourself());
    }

    default public boolean canViewAuditLog(User user) {
        return this.hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.VIEW_AUDIT_LOG);
    }

    default public boolean canYouViewAuditLog() {
        return this.canViewAuditLog(this.getApi().getYourself());
    }

    default public boolean canChangeOwnNickname(User user) {
        return this.hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.CHANGE_NICKNAME, PermissionType.MANAGE_NICKNAMES);
    }

    default public boolean canYouChangeOwnNickname() {
        return this.canChangeOwnNickname(this.getApi().getYourself());
    }

    default public boolean canManageNicknames(User user) {
        return this.hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.MANAGE_NICKNAMES);
    }

    default public boolean canYouManageNicknames() {
        return this.canManageNicknames(this.getApi().getYourself());
    }

    default public boolean canMuteMembers(User user) {
        return this.hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.MUTE_MEMBERS);
    }

    default public boolean canYouMuteMembers() {
        return this.canMuteMembers(this.getApi().getYourself());
    }

    default public boolean canDeafenMembers(User user) {
        return this.hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.DEAFEN_MEMBERS);
    }

    default public boolean canYouDeafenMembers() {
        return this.canDeafenMembers(this.getApi().getYourself());
    }

    default public boolean canMoveMembers(User user) {
        return this.hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.MOVE_MEMBERS);
    }

    default public boolean canYouMoveMembers() {
        return this.canMoveMembers(this.getApi().getYourself());
    }

    default public boolean canManageEmojis(User user) {
        return this.hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.MANAGE_EMOJIS);
    }

    default public boolean canYouManageEmojis() {
        return this.canManageEmojis(this.getApi().getYourself());
    }

    default public boolean canUseSlashCommands(User user) {
        return this.hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.USE_APPLICATION_COMMANDS);
    }

    default public boolean canYouUseSlashCommands() {
        return this.canUseSlashCommands(this.getApi().getYourself());
    }

    default public boolean canManageRoles(User user) {
        return this.hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.MANAGE_ROLES);
    }

    default public boolean canYouManageRoles() {
        return this.canManageRoles(this.getApi().getYourself());
    }

    default public boolean canManageRolesOf(User user, User target) {
        return this.canManageRole(user, this.getHighestRole(target).orElseGet(this::getEveryoneRole));
    }

    default public boolean canManageRole(User user, Role target) {
        return target.getServer() == this && (this.isOwner(user) || this.canManageRoles(user) && this.getHighestRole(user).orElseGet(this::getEveryoneRole).compareTo(target) > 0);
    }

    default public boolean canManage(User user) {
        return this.hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.MANAGE_SERVER);
    }

    default public boolean canYouManage() {
        return this.canManage(this.getApi().getYourself());
    }

    default public boolean canKickUsers(User user) {
        return this.hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.KICK_MEMBERS);
    }

    default public boolean canYouKickUsers() {
        return this.canKickUsers(this.getApi().getYourself());
    }

    default public boolean canKickUser(User user, User userToKick) {
        if (this.isOwner(user)) {
            return true;
        }
        if (!this.canKickUsers(user)) {
            return false;
        }
        Role ownRole = this.getHighestRole(user).orElseThrow(AssertionError::new);
        Optional<Role> otherRole = this.getHighestRole(userToKick);
        boolean userToKickOnServer = otherRole.isPresent();
        return !userToKickOnServer || ownRole.compareTo(otherRole.get()) > 0;
    }

    default public boolean canYouKickUser(User userToKick) {
        return this.canKickUser(this.getApi().getYourself(), userToKick);
    }

    default public boolean canBanUsers(User user) {
        return this.hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.BAN_MEMBERS);
    }

    default public boolean canYouBanUsers() {
        return this.canBanUsers(this.getApi().getYourself());
    }

    default public boolean canBanUser(User user, User userToBan) {
        if (this.isOwner(user)) {
            return true;
        }
        if (!this.canBanUsers(user)) {
            return false;
        }
        Role ownRole = this.getHighestRole(user).orElseThrow(AssertionError::new);
        Optional<Role> otherRole = this.getHighestRole(userToBan);
        boolean userToBanOnServer = otherRole.isPresent();
        return !userToBanOnServer || ownRole.compareTo(otherRole.get()) > 0;
    }

    default public boolean canYouBanUser(User userToBan) {
        return this.canBanUser(this.getApi().getYourself(), userToBan);
    }

    default public boolean canTimeoutUsers(User user) {
        return this.hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.MODERATE_MEMBERS);
    }

    default public boolean canYouTimeoutUsers() {
        return this.canTimeoutUsers(this.getApi().getYourself());
    }

    default public boolean canYouTimeoutUser(User userToTimeout) {
        return this.canTimeoutUser(this.getApi().getYourself(), userToTimeout);
    }

    default public boolean canTimeoutUser(User user, User userToTimeOut) {
        if (this.isOwner(user)) {
            return true;
        }
        if (!this.canTimeoutUsers(user)) {
            return false;
        }
        Role ownRole = this.getHighestRole(user).orElseThrow(AssertionError::new);
        Optional<Role> otherRole = this.getHighestRole(userToTimeOut);
        boolean userToKickOnServer = otherRole.isPresent();
        return !userToKickOnServer || ownRole.compareTo(otherRole.get()) > 0;
    }

    @Override
    default public Optional<Server> getCurrentCachedInstance() {
        return this.getApi().getServerById(this.getId());
    }

    default public CompletableFuture<Void> joinServerThreadChannel(ServerThreadChannel serverThreadChannel) {
        return this.joinServerThreadChannel(serverThreadChannel.getId());
    }

    public CompletableFuture<Void> joinServerThreadChannel(long var1);

    default public CompletableFuture<Void> leaveServerThreadChannel(ServerThreadChannel serverThreadChannel) {
        return this.leaveServerThreadChannel(serverThreadChannel.getId());
    }

    public CompletableFuture<Void> leaveServerThreadChannel(long var1);

    public CompletableFuture<ActiveThreads> getActiveThreads();

    public Set<Sticker> getStickers();

    public CompletableFuture<Set<Sticker>> requestStickers();

    default public Optional<Sticker> getStickerById(long id) {
        return this.getStickers().stream().filter(sticker -> sticker.getId() == id).findAny();
    }

    default public Optional<Sticker> getStickerById(String id) {
        try {
            return this.getStickerById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public Set<Sticker> getStickersByName(String name) {
        return this.getStickers().stream().filter(sticker -> sticker.getName().equals(name)).collect(Collectors.toSet());
    }

    default public Set<Sticker> getStickersByNameIgnoreCase(String name) {
        return this.getStickers().stream().filter(sticker -> sticker.getName().equalsIgnoreCase(name)).collect(Collectors.toSet());
    }

    public CompletableFuture<Sticker> requestStickerById(long var1);

    default public CompletableFuture<Sticker> requestStickerById(String id) {
        try {
            return this.requestStickerById(Long.parseLong(id));
        }
        catch (NumberFormatException exception) {
            CompletableFuture<Sticker> future = new CompletableFuture<Sticker>();
            future.completeExceptionally(exception);
            return future;
        }
    }

    public boolean isWidgetEnabled();

    public Optional<Long> getWidgetChannelId();

    public Optional<Integer> getMaxPresences();

    public Optional<Integer> getMaxMembers();

    public Optional<Integer> getMaxVideoChannelUsers();

    public Optional<WelcomeScreen> getWelcomeScreen();

    public boolean isPremiumProgressBarEnabled();

    default public Optional<RegularServerChannel> getWidgetChannel() {
        return this.getWidgetChannelId().flatMap(this::getRegularChannelById);
    }

    public EnumSet<SystemChannelFlag> getSystemChannelFlags();
}

