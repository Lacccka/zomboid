/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.server;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Region;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.DefaultMessageNotificationLevel;
import org.javacord.api.entity.server.ExplicitContentFilterLevel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.VerificationLevel;
import org.javacord.api.entity.server.internal.ServerUpdaterDelegate;
import org.javacord.api.entity.user.User;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.FileContainer;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class ServerUpdaterDelegateImpl
implements ServerUpdaterDelegate {
    private final Server server;
    private String reason = null;
    private final Map<User, Collection<Role>> userRoles = new HashMap<User, Collection<Role>>();
    private final Map<User, String> userNicknames = new HashMap<User, String>();
    private final Map<User, Boolean> userMuted = new HashMap<User, Boolean>();
    private final Map<User, Boolean> userDeafened = new HashMap<User, Boolean>();
    private final Map<User, ServerVoiceChannel> userMoveTargets = new HashMap<User, ServerVoiceChannel>();
    private final Map<User, Instant> userTimeouts = new HashMap<User, Instant>();
    private List<Role> newRolesOrder = null;
    private String name = null;
    private Region region = null;
    private ExplicitContentFilterLevel explicitContentFilterLevel = null;
    private VerificationLevel verificationLevel = null;
    private DefaultMessageNotificationLevel defaultMessageNotificationLevel = null;
    private ServerChannel afkChannel = null;
    private boolean updateAfkChannel = false;
    private Integer afkTimeout = null;
    private FileContainer icon = null;
    private boolean updateIcon = false;
    private User owner = null;
    private FileContainer splash = null;
    private boolean updateSplash = false;
    private ServerChannel systemChannel = null;
    private boolean updateSystemChannel = false;
    private FileContainer banner = null;
    private boolean updateBanner = false;
    private ServerChannel rulesChannel = null;
    private boolean updateRulesChannel = false;
    private ServerChannel moderatorsOnlyChannel = null;
    private boolean updateModeratorsOnlyChannel = false;
    private Locale locale = null;
    private boolean updateLocale = false;

    public ServerUpdaterDelegateImpl(Server server) {
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
    public void setAfkChannel(ServerVoiceChannel afkChannel) {
        this.afkChannel = afkChannel;
        this.updateAfkChannel = true;
    }

    @Override
    public void removeAfkChannel() {
        this.setAfkChannel(null);
    }

    @Override
    public void setAfkTimeoutInSeconds(int afkTimeout) {
        this.afkTimeout = afkTimeout;
    }

    @Override
    public void setIcon(BufferedImage icon) {
        this.icon = icon == null ? null : new FileContainer(icon, "png");
        this.updateIcon = true;
    }

    @Override
    public void setIcon(BufferedImage icon, String fileType) {
        this.icon = icon == null ? null : new FileContainer(icon, fileType);
        this.updateIcon = true;
    }

    @Override
    public void setIcon(File icon) {
        this.icon = icon == null ? null : new FileContainer(icon);
        this.updateIcon = true;
    }

    @Override
    public void setIcon(Icon icon) {
        this.icon = icon == null ? null : new FileContainer(icon);
        this.updateIcon = true;
    }

    @Override
    public void setIcon(URL icon) {
        this.icon = icon == null ? null : new FileContainer(icon);
        this.updateIcon = true;
    }

    @Override
    public void setIcon(byte[] icon) {
        this.icon = icon == null ? null : new FileContainer(icon, "png");
        this.updateIcon = true;
    }

    @Override
    public void setIcon(byte[] icon, String fileType) {
        this.icon = icon == null ? null : new FileContainer(icon, fileType);
        this.updateIcon = true;
    }

    @Override
    public void setIcon(InputStream icon) {
        this.icon = icon == null ? null : new FileContainer(icon, "png");
        this.updateIcon = true;
    }

    @Override
    public void setIcon(InputStream icon, String fileType) {
        this.icon = icon == null ? null : new FileContainer(icon, fileType);
        this.updateIcon = true;
    }

    @Override
    public void removeIcon() {
        this.icon = null;
        this.updateIcon = true;
    }

    @Override
    public void setOwner(User owner) {
        this.owner = owner;
    }

    @Override
    public void setSplash(BufferedImage splash) {
        this.splash = splash == null ? null : new FileContainer(splash, "png");
        this.updateSplash = true;
    }

    @Override
    public void setSplash(BufferedImage splash, String fileType) {
        this.splash = splash == null ? null : new FileContainer(splash, fileType);
        this.updateSplash = true;
    }

    @Override
    public void setSplash(File splash) {
        this.splash = splash == null ? null : new FileContainer(splash);
        this.updateSplash = true;
    }

    @Override
    public void setSplash(Icon splash) {
        this.splash = splash == null ? null : new FileContainer(splash);
        this.updateSplash = true;
    }

    @Override
    public void setSplash(URL splash) {
        this.splash = splash == null ? null : new FileContainer(splash);
        this.updateSplash = true;
    }

    @Override
    public void setSplash(byte[] splash) {
        this.splash = splash == null ? null : new FileContainer(splash, "png");
        this.updateSplash = true;
    }

    @Override
    public void setSplash(byte[] splash, String fileType) {
        this.splash = splash == null ? null : new FileContainer(splash, fileType);
        this.updateSplash = true;
    }

    @Override
    public void setSplash(InputStream splash) {
        this.splash = splash == null ? null : new FileContainer(splash, "png");
        this.updateSplash = true;
    }

    @Override
    public void setSplash(InputStream splash, String fileType) {
        this.splash = splash == null ? null : new FileContainer(splash, fileType);
        this.updateSplash = true;
    }

    @Override
    public void removeSplash() {
        this.splash = null;
        this.updateSplash = true;
    }

    @Override
    public void setBanner(BufferedImage banner) {
        this.banner = banner == null ? null : new FileContainer(banner, "png");
        this.updateBanner = true;
    }

    @Override
    public void setBanner(BufferedImage banner, String fileType) {
        this.banner = banner == null ? null : new FileContainer(banner, fileType);
        this.updateBanner = true;
    }

    @Override
    public void setBanner(File banner) {
        this.banner = banner == null ? null : new FileContainer(banner);
        this.updateBanner = true;
    }

    @Override
    public void setBanner(Icon banner) {
        this.banner = banner == null ? null : new FileContainer(banner);
        this.updateBanner = true;
    }

    @Override
    public void setBanner(URL banner) {
        this.banner = banner == null ? null : new FileContainer(banner);
        this.updateBanner = true;
    }

    @Override
    public void setBanner(byte[] banner) {
        this.banner = banner == null ? null : new FileContainer(banner, "png");
        this.updateBanner = true;
    }

    @Override
    public void setBanner(byte[] banner, String fileType) {
        this.banner = banner == null ? null : new FileContainer(banner, fileType);
        this.updateBanner = true;
    }

    @Override
    public void setBanner(InputStream banner) {
        this.banner = banner == null ? null : new FileContainer(banner, "png");
        this.updateBanner = true;
    }

    @Override
    public void setBanner(InputStream banner, String fileType) {
        this.banner = banner == null ? null : new FileContainer(banner, fileType);
        this.updateBanner = true;
    }

    @Override
    public void removeBanner() {
        this.banner = null;
        this.updateBanner = true;
    }

    @Override
    public void setRulesChannel(ServerTextChannel rulesChannel) {
        this.rulesChannel = rulesChannel;
        this.updateRulesChannel = true;
    }

    @Override
    public void removeRulesChannel() {
        this.setRulesChannel(null);
    }

    @Override
    public void setModeratorsOnlyChannel(ServerTextChannel moderatorsOnlyChannel) {
        this.moderatorsOnlyChannel = moderatorsOnlyChannel;
        this.updateModeratorsOnlyChannel = true;
    }

    @Override
    public void removeModeratorsOnlyChannel() {
        this.setModeratorsOnlyChannel(null);
    }

    @Override
    public void setPreferredLocale(Locale locale) {
        this.locale = locale;
        this.updateLocale = true;
    }

    @Override
    public void setSystemChannel(ServerTextChannel systemChannel) {
        this.systemChannel = systemChannel;
        this.updateSystemChannel = true;
    }

    @Override
    public void removeSystemChannel() {
        this.setSystemChannel(null);
    }

    @Override
    public void setNickname(User user, String nickname) {
        this.userNicknames.put(user, nickname);
    }

    @Override
    public void setUserTimeout(User user, Instant timeout2) {
        this.userTimeouts.put(user, timeout2);
    }

    @Override
    public void setMuted(User user, boolean muted) {
        this.userMuted.put(user, muted);
    }

    @Override
    public void setDeafened(User user, boolean deafened) {
        this.userDeafened.put(user, deafened);
    }

    @Override
    public void setVoiceChannel(User user, ServerVoiceChannel channel) {
        this.userMoveTargets.put(user, channel);
    }

    @Override
    public void reorderRoles(List<Role> roles) {
        this.newRolesOrder = roles;
    }

    @Override
    public void addRoleToUser(User user, Role role) {
        Collection userRoles = this.userRoles.computeIfAbsent(user, u -> new ArrayList<Role>(this.server.getRoles((User)u)));
        userRoles.add(role);
    }

    @Override
    public void addRolesToUser(User user, Collection<Role> roles) {
        Collection userRoles = this.userRoles.computeIfAbsent(user, u -> new ArrayList<Role>(this.server.getRoles((User)u)));
        userRoles.addAll(roles);
    }

    @Override
    public void removeRoleFromUser(User user, Role role) {
        Collection userRoles = this.userRoles.computeIfAbsent(user, u -> new ArrayList<Role>(this.server.getRoles((User)u)));
        userRoles.remove(role);
    }

    @Override
    public void removeRolesFromUser(User user, Collection<Role> roles) {
        Collection userRoles = this.userRoles.computeIfAbsent(user, u -> new ArrayList<Role>(this.server.getRoles((User)u)));
        userRoles.removeAll(roles);
    }

    @Override
    public void removeAllRolesFromUser(User user) {
        Collection userRoles = this.userRoles.computeIfAbsent(user, u -> new ArrayList<Role>(this.server.getRoles((User)u)));
        userRoles.clear();
    }

    @Override
    public CompletableFuture<Void> update() {
        HashSet<User> members = new HashSet<User>(this.userRoles.keySet());
        members.addAll(this.userNicknames.keySet());
        members.addAll(this.userMuted.keySet());
        members.addAll(this.userDeafened.keySet());
        members.addAll(this.userMoveTargets.keySet());
        members.addAll(this.userTimeouts.keySet());
        ArrayList<CompletionStage> tasks = new ArrayList<CompletionStage>();
        members.forEach(member -> {
            boolean patchMember = false;
            ObjectNode updateNode = JsonNodeFactory.instance.objectNode();
            Collection<Role> roles = this.userRoles.get(member);
            if (roles != null) {
                ArrayNode rolesJson = updateNode.putArray("roles");
                roles.stream().map(DiscordEntity::getIdAsString).forEach(rolesJson::add);
                patchMember = true;
            }
            if (this.userNicknames.containsKey(member)) {
                String nickname = this.userNicknames.get(member);
                if (member.isYourself()) {
                    tasks.add(new RestRequest(this.server.getApi(), RestMethod.PATCH, RestEndpoint.OWN_NICKNAME).setUrlParameters(this.server.getIdAsString()).setBody(JsonNodeFactory.instance.objectNode().put("nick", nickname)).setAuditLogReason(this.reason).execute(result -> null));
                } else {
                    updateNode.put("nick", nickname == null ? "" : nickname);
                    patchMember = true;
                }
            }
            if (this.userMuted.containsKey(member)) {
                updateNode.put("mute", this.userMuted.get(member));
                patchMember = true;
            }
            if (this.userDeafened.containsKey(member)) {
                updateNode.put("deaf", this.userDeafened.get(member));
                patchMember = true;
            }
            if (this.userMoveTargets.containsKey(member)) {
                ServerVoiceChannel channel = this.userMoveTargets.get(member);
                if (member.isYourself()) {
                    ((DiscordApiImpl)this.server.getApi()).getWebSocketAdapter().sendVoiceStateUpdate(this.server, channel, null, null);
                } else if (channel != null) {
                    updateNode.put("channel_id", channel.getId());
                    patchMember = true;
                } else {
                    updateNode.putNull("channel_id");
                    patchMember = true;
                }
            }
            if (this.userTimeouts.containsKey(member)) {
                updateNode.put("communication_disabled_until", this.userTimeouts.get(member).equals(Instant.MIN) ? null : DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC)).format(this.userTimeouts.get(member)));
                patchMember = true;
            }
            if (patchMember) {
                tasks.add(new RestRequest(this.server.getApi(), RestMethod.PATCH, RestEndpoint.SERVER_MEMBER).setUrlParameters(this.server.getIdAsString(), member.getIdAsString()).setBody(updateNode).setAuditLogReason(this.reason).execute(result -> null));
            }
        });
        if (this.newRolesOrder != null) {
            tasks.add(this.server.reorderRoles(this.newRolesOrder, this.reason));
        }
        boolean patchServer = false;
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        if (this.name != null) {
            body.put("name", this.name);
            patchServer = true;
        }
        if (this.region != null) {
            body.put("region", this.region.getKey());
            patchServer = true;
        }
        if (this.explicitContentFilterLevel != null) {
            body.put("explicit_content_filter", this.explicitContentFilterLevel.getId());
            patchServer = true;
        }
        if (this.verificationLevel != null) {
            body.put("verification_level", this.verificationLevel.getId());
            patchServer = true;
        }
        if (this.defaultMessageNotificationLevel != null) {
            body.put("default_message_notifications", this.defaultMessageNotificationLevel.getId());
            patchServer = true;
        }
        if (this.updateAfkChannel) {
            if (this.afkChannel != null) {
                body.put("afk_channel_id", this.afkChannel.getIdAsString());
            } else {
                body.putNull("afk_channel_id");
            }
            patchServer = true;
        }
        if (this.afkTimeout != null) {
            body.put("afk_timeout", (int)this.afkTimeout);
            patchServer = true;
        }
        if (this.updateIcon) {
            if (this.icon == null) {
                body.putNull("icon");
            }
            patchServer = true;
        }
        if (this.updateSplash) {
            if (this.splash == null) {
                body.putNull("splash");
            }
            patchServer = true;
        }
        if (this.owner != null) {
            body.put("owner_id", this.owner.getIdAsString());
            patchServer = true;
        }
        if (this.updateSystemChannel) {
            if (this.systemChannel != null) {
                body.put("system_channel_id", this.systemChannel.getIdAsString());
            } else {
                body.putNull("system_channel_id");
            }
            patchServer = true;
        }
        if (this.updateModeratorsOnlyChannel) {
            if (this.moderatorsOnlyChannel != null) {
                body.put("public_updates_channel_id", this.moderatorsOnlyChannel.getIdAsString());
            } else {
                body.putNull("public_updates_channel_id");
            }
            patchServer = true;
        }
        if (this.updateRulesChannel) {
            if (this.rulesChannel != null) {
                body.put("rules_channel_id", this.rulesChannel.getIdAsString());
            } else {
                body.putNull("rules_channel_id");
            }
            patchServer = true;
        }
        if (this.updateBanner) {
            if (this.banner == null) {
                body.putNull("banner");
            }
            patchServer = true;
        }
        if (this.updateLocale) {
            if (this.locale == null) {
                body.putNull("preferred_locale");
            } else {
                body.put("preferred_locale", this.locale.toLanguageTag());
            }
            patchServer = true;
        }
        if (patchServer) {
            if (this.icon != null || this.splash != null || this.banner != null) {
                CompletionStage iconFuture = null;
                if (this.icon != null) {
                    iconFuture = this.icon.asByteArray(this.server.getApi()).thenAccept(bytes -> {
                        String base64Icon = "data:image/" + this.icon.getFileType() + ";base64," + Base64.getEncoder().encodeToString((byte[])bytes);
                        body.put("icon", base64Icon);
                    });
                }
                CompletionStage splashFuture = null;
                if (this.splash != null) {
                    splashFuture = this.splash.asByteArray(this.server.getApi()).thenAccept(bytes -> {
                        String base64Splash = "data:image/" + this.splash.getFileType() + ";base64," + Base64.getEncoder().encodeToString((byte[])bytes);
                        body.put("splash", base64Splash);
                    });
                }
                CompletionStage bannerFuture = null;
                if (this.banner != null) {
                    bannerFuture = this.banner.asByteArray(this.server.getApi()).thenAccept(bytes -> {
                        String base64Banner = "data:image/" + this.banner.getFileType() + ";base64," + Base64.getEncoder().encodeToString((byte[])bytes);
                        body.put("banner", base64Banner);
                    });
                }
                ArrayList<CompletionStage> futureList = new ArrayList<CompletionStage>();
                if (iconFuture != null) {
                    futureList.add(iconFuture);
                }
                if (splashFuture != null) {
                    futureList.add(splashFuture);
                }
                if (bannerFuture != null) {
                    futureList.add(bannerFuture);
                }
                CompletableFuture<Void> future = CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()]));
                tasks.add(future.thenCompose(aVoid -> new RestRequest(this.server.getApi(), RestMethod.PATCH, RestEndpoint.SERVER).setUrlParameters(this.server.getIdAsString()).setBody(body).setAuditLogReason(this.reason).execute(result -> null)));
            } else {
                tasks.add(new RestRequest(this.server.getApi(), RestMethod.PATCH, RestEndpoint.SERVER).setUrlParameters(this.server.getIdAsString()).setBody(body).setAuditLogReason(this.reason).execute(result -> null));
            }
        }
        CompletableFuture[] tasksArray = tasks.toArray(new CompletableFuture[tasks.size()]);
        return CompletableFuture.allOf(tasksArray);
    }
}

