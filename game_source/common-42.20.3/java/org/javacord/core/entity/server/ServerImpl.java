/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.DiscordClient;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Region;
import org.javacord.api.entity.VanityUrlCode;
import org.javacord.api.entity.auditlog.AuditLog;
import org.javacord.api.entity.auditlog.AuditLogActionType;
import org.javacord.api.entity.auditlog.AuditLogEntry;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.RegularServerChannel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerForumChannel;
import org.javacord.api.entity.channel.ServerStageVoiceChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.UnknownRegularServerChannel;
import org.javacord.api.entity.channel.UnknownServerChannel;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.ActiveThreads;
import org.javacord.api.entity.server.Ban;
import org.javacord.api.entity.server.BoostLevel;
import org.javacord.api.entity.server.DefaultMessageNotificationLevel;
import org.javacord.api.entity.server.ExplicitContentFilterLevel;
import org.javacord.api.entity.server.MultiFactorAuthenticationLevel;
import org.javacord.api.entity.server.NsfwLevel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.ServerFeature;
import org.javacord.api.entity.server.SystemChannelFlag;
import org.javacord.api.entity.server.VerificationLevel;
import org.javacord.api.entity.server.invite.RichInvite;
import org.javacord.api.entity.server.invite.WelcomeScreen;
import org.javacord.api.entity.sticker.Sticker;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.entity.webhook.Webhook;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.audio.AudioConnectionImpl;
import org.javacord.core.entity.IconImpl;
import org.javacord.core.entity.VanityUrlCodeImpl;
import org.javacord.core.entity.activity.ActivityImpl;
import org.javacord.core.entity.auditlog.AuditLogImpl;
import org.javacord.core.entity.channel.ChannelCategoryImpl;
import org.javacord.core.entity.channel.RegularServerChannelImpl;
import org.javacord.core.entity.channel.ServerForumChannelImpl;
import org.javacord.core.entity.channel.ServerStageVoiceChannelImpl;
import org.javacord.core.entity.channel.ServerTextChannelImpl;
import org.javacord.core.entity.channel.ServerThreadChannelImpl;
import org.javacord.core.entity.channel.ServerVoiceChannelImpl;
import org.javacord.core.entity.channel.UnknownRegularServerChannelImpl;
import org.javacord.core.entity.channel.UnknownServerChannelImpl;
import org.javacord.core.entity.permission.RoleImpl;
import org.javacord.core.entity.server.ActiveThreadsImpl;
import org.javacord.core.entity.server.BanImpl;
import org.javacord.core.entity.server.invite.InviteImpl;
import org.javacord.core.entity.server.invite.WelcomeScreenImpl;
import org.javacord.core.entity.sticker.StickerImpl;
import org.javacord.core.entity.user.Member;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.entity.webhook.IncomingWebhookImpl;
import org.javacord.core.entity.webhook.WebhookImpl;
import org.javacord.core.listener.server.InternalServerAttachableListenerManager;
import org.javacord.core.util.Cleanupable;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.logging.LoggerUtil;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;
import org.javacord.core.util.rest.RestRequestResult;

public class ServerImpl
implements Server,
Cleanupable,
InternalServerAttachableListenerManager,
DispatchQueueSelector {
    private static final Logger logger = LoggerUtil.getLogger(ServerImpl.class);
    private final DiscordApiImpl api;
    private final long id;
    private volatile String name;
    private volatile Region region;
    private final boolean large;
    private volatile long ownerId;
    private volatile long applicationId = -1L;
    private volatile VerificationLevel verificationLevel;
    private volatile ExplicitContentFilterLevel explicitContentFilterLevel;
    private volatile DefaultMessageNotificationLevel defaultMessageNotificationLevel;
    private volatile MultiFactorAuthenticationLevel multiFactorAuthenticationLevel;
    private final AtomicInteger memberCount = new AtomicInteger();
    private volatile String iconHash;
    private volatile String splash;
    private volatile long systemChannelId = -1L;
    private volatile long afkChannelId = -1L;
    private volatile int afkTimeout = 0;
    private volatile boolean ready;
    private final ReentrantLock audioConnectionLock = new ReentrantLock();
    private final List<Consumer<Server>> readyConsumers = new ArrayList<Consumer<Server>>();
    private final ConcurrentHashMap<Long, Role> roles = new ConcurrentHashMap();
    private final Set<KnownCustomEmoji> customEmojis = new HashSet<KnownCustomEmoji>();
    private final Collection<ServerFeature> serverFeatures = new ArrayList<ServerFeature>();
    private final ConcurrentHashMap<Long, Sticker> stickers = new ConcurrentHashMap();
    private volatile BoostLevel boostLevel;
    private volatile NsfwLevel nsfwLevel;
    private volatile int serverBoostCount = 0;
    private volatile long rulesChannelId = -1L;
    private volatile String description;
    private volatile long moderatorsOnlyChannelId = -1L;
    private volatile Locale preferredLocale;
    private volatile VanityUrlCode vanityUrlCode;
    private volatile String discoverySplash;
    private volatile boolean widgetEnabled;
    private volatile Long widgetChannelId;
    private volatile Integer maxPresences;
    private volatile Integer maxMembers;
    private volatile Integer maxVideoChannelUsers;
    private volatile WelcomeScreen welcomeScreen;
    private volatile boolean premiumProgressBarEnabled;
    private final EnumSet<SystemChannelFlag> systemChannelFlags = EnumSet.noneOf(SystemChannelFlag.class);

    public ServerImpl(DiscordApiImpl api, JsonNode data) {
        this.api = api;
        this.ready = !api.hasUserCacheEnabled() || !api.isWaitingForUsersOnStartup();
        this.id = Long.parseLong(data.get("id").asText());
        this.name = data.get("name").asText();
        this.region = Region.getRegionByKey(data.get("region").asText());
        this.large = data.get("large").asBoolean();
        this.memberCount.set(data.get("member_count").asInt());
        this.ownerId = Long.parseLong(data.get("owner_id").asText());
        this.verificationLevel = VerificationLevel.fromId(data.get("verification_level").asInt());
        this.explicitContentFilterLevel = ExplicitContentFilterLevel.fromId(data.get("explicit_content_filter").asInt());
        this.defaultMessageNotificationLevel = DefaultMessageNotificationLevel.fromId(data.get("default_message_notifications").asInt());
        this.multiFactorAuthenticationLevel = MultiFactorAuthenticationLevel.fromId(data.get("mfa_level").asInt());
        this.boostLevel = BoostLevel.fromId(data.get("premium_tier").asInt());
        this.nsfwLevel = NsfwLevel.fromId(data.get("nsfw_level").asInt());
        this.preferredLocale = new Locale.Builder().setLanguageTag(data.get("preferred_locale").asText()).build();
        if (data.has("icon") && !data.get("icon").isNull()) {
            this.iconHash = data.get("icon").asText();
        }
        if (data.has("splash") && !data.get("splash").isNull()) {
            this.splash = data.get("splash").asText();
        }
        if (data.hasNonNull("afk_channel_id")) {
            this.afkChannelId = data.get("afk_channel_id").asLong();
        }
        if (data.hasNonNull("afk_timeout")) {
            this.afkTimeout = data.get("afk_timeout").asInt();
        }
        if (data.hasNonNull("system_channel_id")) {
            this.systemChannelId = data.get("system_channel_id").asLong();
        }
        if (data.hasNonNull("application_id")) {
            this.applicationId = data.get("application_id").asLong();
        }
        if (data.has("features")) {
            data.get("features").forEach(jsonNode -> this.addFeature(jsonNode.asText()));
        }
        if (data.has("premium_subscription_count")) {
            this.serverBoostCount = data.get("premium_subscription_count").asInt();
        }
        if (data.hasNonNull("rules_channel_id")) {
            this.rulesChannelId = data.get("rules_channel_id").asLong();
        }
        if (data.hasNonNull("description")) {
            this.description = data.get("description").asText();
        }
        if (data.hasNonNull("public_updates_channel_id")) {
            this.moderatorsOnlyChannelId = data.get("public_updates_channel_id").asLong();
        }
        if (data.hasNonNull("discovery_splash")) {
            this.discoverySplash = data.get("discovery_splash").asText();
        }
        if (data.hasNonNull("vanity_url_code")) {
            this.vanityUrlCode = new VanityUrlCodeImpl(data.get("vanity_url_code").asText());
        }
        if (data.hasNonNull("system_channel_flags")) {
            int systemChannelFlag = data.get("system_channel_flags").asInt();
            this.setSystemChannelFlag(systemChannelFlag);
        }
        if (data.has("channels")) {
            block14: for (JsonNode channel : data.get("channels")) {
                switch (ChannelType.fromId(channel.get("type").asInt())) {
                    case SERVER_TEXT_CHANNEL: {
                        this.getOrCreateServerTextChannel(channel);
                        continue block14;
                    }
                    case SERVER_FORUM_CHANNEL: {
                        this.getOrCreateServerForumChannel(channel);
                        continue block14;
                    }
                    case SERVER_VOICE_CHANNEL: {
                        this.getOrCreateServerVoiceChannel(channel);
                        continue block14;
                    }
                    case SERVER_STAGE_VOICE_CHANNEL: {
                        this.getOrCreateServerStageVoiceChannel(channel);
                        continue block14;
                    }
                    case CHANNEL_CATEGORY: {
                        this.getOrCreateChannelCategory(channel);
                        continue block14;
                    }
                    case SERVER_NEWS_CHANNEL: {
                        logger.debug("{} has a news channel. In this Javacord version it is treated as a normal text channel!", (Object)this);
                        this.getOrCreateServerTextChannel(channel);
                        continue block14;
                    }
                    case SERVER_STORE_CHANNEL: {
                        logger.debug("{} has a store channel. These are not supported in this Javacord version and get ignored!", (Object)this);
                        continue block14;
                    }
                }
                try {
                    if (channel.has("position")) {
                        this.showFallbackWarningMessage(channel.get("type").asInt(), "UnknownRegularServerChannel");
                        this.getOrCreateUnknownRegularServerChannel(channel);
                        continue;
                    }
                    this.showFallbackWarningMessage(channel.get("type").asInt(), "UnknownServerChannel");
                    this.getOrCreateUnknownServerChannel(channel);
                }
                catch (Exception exception) {
                    logger.warn("An error occurred when trying to use a fallback channel implementation", (Throwable)exception);
                }
            }
        }
        if (data.has("threads")) {
            block15: for (JsonNode channel : data.get("threads")) {
                switch (ChannelType.fromId(channel.get("type").asInt())) {
                    case SERVER_PUBLIC_THREAD: 
                    case SERVER_PRIVATE_THREAD: 
                    case SERVER_NEWS_THREAD: {
                        this.getOrCreateServerThreadChannel(channel);
                        continue block15;
                    }
                }
                logger.warn("Unknown or unexpected channel type. Your Javacord version might be outdated!");
            }
        }
        if (data.has("roles")) {
            for (JsonNode roleJson : data.get("roles")) {
                RoleImpl role = new RoleImpl(api, this, roleJson);
                this.roles.put(role.getId(), role);
            }
        }
        if (data.has("members")) {
            this.addMembers(data.get("members"));
        }
        if (data.hasNonNull("voice_states")) {
            for (JsonNode voiceStateJson : data.get("voice_states")) {
                Optional<ServerVoiceChannelImpl> channel = this.getVoiceChannelById(voiceStateJson.get("channel_id").asLong()).map(ch -> (ServerVoiceChannelImpl)ch);
                if (channel.isPresent()) {
                    channel.get().addConnectedUser(voiceStateJson.get("user_id").asLong());
                    continue;
                }
                logger.warn("Channel " + voiceStateJson.get("channel_id").asLong() + " was found in the voice_states property for server " + this.id + " but was not found in the channels property. It will not be loaded.");
            }
        }
        if ((this.isLarge() || !api.getIntents().contains((Object)Intent.GUILD_PRESENCES)) && this.getMembers().size() < this.getMemberCount() && api.hasUserCacheEnabled()) {
            api.getWebSocketAdapter().queueRequestGuildMembers(this);
        }
        if (data.has("emojis")) {
            for (JsonNode emojiJson : data.get("emojis")) {
                KnownCustomEmoji emoji = api.getOrCreateKnownCustomEmoji(this, emojiJson);
                this.addCustomEmoji(emoji);
            }
        }
        if (data.has("stickers")) {
            for (JsonNode stickerJson : data.get("stickers")) {
                Sticker sticker = api.getOrCreateSticker(stickerJson);
                this.addSticker(sticker);
            }
        }
        if (data.has("presences")) {
            for (JsonNode presenceJson : data.get("presences")) {
                long userId = Long.parseLong(presenceJson.get("user").get("id").asText());
                UserImpl user = api.getCachedUserById(userId).map(UserImpl.class::cast).orElse(null);
                if (user == null) continue;
                if (presenceJson.hasNonNull("activities")) {
                    HashSet<ActivityImpl> activities = new HashSet<ActivityImpl>();
                    for (JsonNode activityJson : presenceJson.get("activities")) {
                        if (activityJson.isNull()) continue;
                        activities.add(new ActivityImpl(api, activityJson));
                    }
                    api.updateUserPresence(userId, presence -> presence.setActivities(activities));
                }
                if (presenceJson.has("status")) {
                    UserStatus status = UserStatus.fromString(presenceJson.get("status").asText());
                    api.updateUserPresence(userId, presence -> presence.setStatus(status));
                }
                if (!presenceJson.has("client_status")) continue;
                JsonNode clientStatus = presenceJson.get("client_status");
                for (DiscordClient client : DiscordClient.values()) {
                    if (clientStatus.hasNonNull(client.getName())) {
                        UserStatus status = UserStatus.fromString(clientStatus.get(client.getName()).asText());
                        api.updateUserPresence(userId, presence -> presence.setClientStatus(presence.getClientStatus().put(client, status)));
                        continue;
                    }
                    api.updateUserPresence(userId, presence -> presence.setClientStatus(presence.getClientStatus().put(client, UserStatus.OFFLINE)));
                }
            }
        }
        this.welcomeScreen = data.has("welcome_screen") ? new WelcomeScreenImpl(data.get("welcome_screen")) : null;
        this.widgetEnabled = data.path("widget_enabled").asBoolean(false);
        this.widgetChannelId = data.hasNonNull("widget_channel_id") ? Long.valueOf(data.get("widget_channel_id").asLong()) : null;
        this.maxPresences = data.hasNonNull("max_presences") ? Integer.valueOf(data.get("max_presences").asInt()) : null;
        this.maxMembers = data.hasNonNull("max_members") ? Integer.valueOf(data.get("max_members").asInt()) : null;
        this.maxVideoChannelUsers = data.hasNonNull("max_video_channel_users") ? Integer.valueOf(data.get("max_video_channel_users").asInt()) : null;
        this.premiumProgressBarEnabled = data.path("premium_progress_bar_enabled").asBoolean(false);
        api.addServerToCache(this);
    }

    private void showFallbackWarningMessage(int channelType, String fallbackName) {
        logger.warn("Encountered not handled channel type: {}. Trying to use the {} fallback implementation", (Object)channelType, (Object)fallbackName);
    }

    public void setSystemChannelFlag(int systemChannelFlag) {
        for (SystemChannelFlag flag : SystemChannelFlag.values()) {
            if ((flag.asInt() & systemChannelFlag) != flag.asInt()) continue;
            this.systemChannelFlags.add(flag);
        }
    }

    private void addFeature(String feature) {
        try {
            this.serverFeatures.add(ServerFeature.valueOf(feature));
        }
        catch (Exception ignored) {
            logger.debug("Encountered server with unknown feature {}. Please update to the latest Javacord version or create an issue on the Javacord GitHub page if you are already on the latest version.", (Object)feature);
        }
    }

    public boolean isReady() {
        return this.ready;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addServerReadyConsumer(Consumer<Server> consumer) {
        List<Consumer<Server>> list = this.readyConsumers;
        synchronized (list) {
            if (this.ready) {
                consumer.accept(this);
            } else {
                this.readyConsumers.add(consumer);
            }
        }
    }

    public String getIconHash() {
        return this.iconHash;
    }

    public void setIconHash(String iconHash) {
        this.iconHash = iconHash;
    }

    public String getSplashHash() {
        return this.splash;
    }

    public void setSplashHash(String splashHash) {
        this.splash = splashHash;
    }

    public void setSystemChannelId(long systemChannelId) {
        this.systemChannelId = systemChannelId;
    }

    public void setAfkChannelId(long afkChannelId) {
        this.afkChannelId = afkChannelId;
    }

    public void setAfkTimeout(int afkTimeout) {
        this.afkTimeout = afkTimeout;
    }

    public void setVerificationLevel(VerificationLevel verificationLevel) {
        this.verificationLevel = verificationLevel;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public void setDefaultMessageNotificationLevel(DefaultMessageNotificationLevel defaultMessageNotificationLevel) {
        this.defaultMessageNotificationLevel = defaultMessageNotificationLevel;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public void setApplicationId(long applicationId) {
        this.applicationId = applicationId;
    }

    public void setExplicitContentFilterLevel(ExplicitContentFilterLevel explicitContentFilterLevel) {
        this.explicitContentFilterLevel = explicitContentFilterLevel;
    }

    public void setMultiFactorAuthenticationLevel(MultiFactorAuthenticationLevel multiFactorAuthenticationLevel) {
        this.multiFactorAuthenticationLevel = multiFactorAuthenticationLevel;
    }

    public void removeRole(long roleId) {
        this.roles.remove(roleId);
    }

    public void addCustomEmoji(KnownCustomEmoji emoji) {
        this.customEmojis.add(emoji);
    }

    public void removeCustomEmoji(KnownCustomEmoji emoji) {
        this.customEmojis.remove(emoji);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Role getOrCreateRole(JsonNode data) {
        long id = Long.parseLong(data.get("id").asText());
        ServerImpl serverImpl = this;
        synchronized (serverImpl) {
            return this.getRoleById(id).orElseGet(() -> {
                RoleImpl role = new RoleImpl(this.api, this, data);
                this.roles.put(role.getId(), role);
                return role;
            });
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ChannelCategory getOrCreateChannelCategory(JsonNode data) {
        long id = Long.parseLong(data.get("id").asText());
        ChannelType type = ChannelType.fromId(data.get("type").asInt());
        ServerImpl serverImpl = this;
        synchronized (serverImpl) {
            if (type == ChannelType.CHANNEL_CATEGORY) {
                return this.getChannelCategoryById(id).orElseGet(() -> new ChannelCategoryImpl(this.api, this, data));
            }
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ServerTextChannel getOrCreateServerTextChannel(JsonNode data) {
        long id = Long.parseLong(data.get("id").asText());
        ChannelType type = ChannelType.fromId(data.get("type").asInt());
        ServerImpl serverImpl = this;
        synchronized (serverImpl) {
            switch (type) {
                case SERVER_TEXT_CHANNEL: 
                case SERVER_NEWS_CHANNEL: {
                    return this.getTextChannelById(id).orElseGet(() -> new ServerTextChannelImpl(this.api, this, data));
                }
            }
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ServerThreadChannel getOrCreateServerThreadChannel(JsonNode data) {
        long id = Long.parseLong(data.get("id").asText());
        ChannelType type = ChannelType.fromId(data.get("type").asInt());
        ServerImpl serverImpl = this;
        synchronized (serverImpl) {
            switch (type) {
                case SERVER_PUBLIC_THREAD: 
                case SERVER_PRIVATE_THREAD: 
                case SERVER_NEWS_THREAD: {
                    return this.getThreadChannelById(id).orElseGet(() -> new ServerThreadChannelImpl(this.api, this, data));
                }
            }
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ServerVoiceChannel getOrCreateServerVoiceChannel(JsonNode data) {
        long id = Long.parseLong(data.get("id").asText());
        ChannelType type = ChannelType.fromId(data.get("type").asInt());
        ServerImpl serverImpl = this;
        synchronized (serverImpl) {
            if (type == ChannelType.SERVER_VOICE_CHANNEL) {
                return this.getVoiceChannelById(id).orElseGet(() -> new ServerVoiceChannelImpl(this.api, this, data));
            }
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ServerStageVoiceChannel getOrCreateServerStageVoiceChannel(JsonNode data) {
        long id = Long.parseLong(data.get("id").asText());
        ChannelType type = ChannelType.fromId(data.get("type").asInt());
        ServerImpl serverImpl = this;
        synchronized (serverImpl) {
            if (type == ChannelType.SERVER_STAGE_VOICE_CHANNEL) {
                return this.getStageVoiceChannelById(id).orElseGet(() -> new ServerStageVoiceChannelImpl(this.api, this, data));
            }
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ServerForumChannel getOrCreateServerForumChannel(JsonNode data) {
        long id = Long.parseLong(data.get("id").asText());
        ChannelType type = ChannelType.fromId(data.get("type").asInt());
        ServerImpl serverImpl = this;
        synchronized (serverImpl) {
            switch (type) {
                case SERVER_FORUM_CHANNEL: {
                    return this.getForumChannelById(id).orElseGet(() -> new ServerForumChannelImpl(this.api, this, data));
                }
            }
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public UnknownServerChannel getOrCreateUnknownServerChannel(JsonNode data) {
        long id = Long.parseLong(data.get("id").asText());
        ChannelType type = ChannelType.fromId(data.get("type").asInt());
        ServerImpl serverImpl = this;
        synchronized (serverImpl) {
            return this.getUnknownChannelById(id).orElseGet(() -> new UnknownServerChannelImpl(this.api, this, data));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public UnknownRegularServerChannel getOrCreateUnknownRegularServerChannel(JsonNode data) {
        long id = Long.parseLong(data.get("id").asText());
        ChannelType type = ChannelType.fromId(data.get("type").asInt());
        ServerImpl serverImpl = this;
        synchronized (serverImpl) {
            return this.getUnknownRegularChannelById(id).orElseGet(() -> new UnknownRegularServerChannelImpl(this.api, this, data));
        }
    }

    public void removeMember(long userId) {
        this.api.removeMemberFromCache(userId, this.getId());
    }

    public void decrementMemberCount() {
        this.memberCount.decrementAndGet();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public MemberImpl addMember(JsonNode memberJson) {
        MemberImpl member = new MemberImpl(this.api, this, memberJson, null);
        this.api.addMemberToCacheOrReplaceExisting(member);
        List<Consumer<Server>> list = this.readyConsumers;
        synchronized (list) {
            if (!this.ready && this.getRealMembers().size() == this.getMemberCount()) {
                this.ready = true;
                this.readyConsumers.forEach(consumer -> consumer.accept(this));
                this.readyConsumers.clear();
            }
        }
        return member;
    }

    public void incrementMemberCount() {
        this.memberCount.incrementAndGet();
    }

    public void addMembers(JsonNode membersJson) {
        for (JsonNode memberJson : membersJson) {
            this.addMember(memberJson);
        }
    }

    public List<Member> addAndGetMembers(JsonNode membersJson) {
        ArrayList<Member> members = new ArrayList<Member>();
        for (JsonNode memberJson : membersJson) {
            MemberImpl member = this.addMember(memberJson);
            members.add(member);
        }
        return members;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRulesChannelId(long rulesChannelId) {
        this.rulesChannelId = rulesChannelId;
    }

    public void setModeratorsOnlyChannelId(long moderatorsOnlyChannelId) {
        this.moderatorsOnlyChannelId = moderatorsOnlyChannelId;
    }

    public void setBoostLevel(BoostLevel boostLevel) {
        this.boostLevel = boostLevel;
    }

    public void setNsfwLevel(NsfwLevel nsfwLevel) {
        this.nsfwLevel = nsfwLevel;
    }

    public void setPreferredLocale(Locale preferredLocale) {
        this.preferredLocale = preferredLocale;
    }

    public void setServerBoostCount(int serverBoostCount) {
        this.serverBoostCount = serverBoostCount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDiscoverySplashHash(String discoverySplashHash) {
        this.discoverySplash = discoverySplashHash;
    }

    public String getDiscoverySplashHash() {
        return this.discoverySplash;
    }

    public void setVanityUrlCode(VanityUrlCode vanityUrlCode) {
        this.vanityUrlCode = vanityUrlCode;
    }

    public void setServerFeatures(Collection<ServerFeature> serverFeatures) {
        this.serverFeatures.clear();
        this.serverFeatures.addAll(serverFeatures);
    }

    @Override
    public Set<ServerChannel> getUnorderedChannels() {
        return this.api.getEntityCache().get().getChannelCache().getChannelsOfServer(this.getId());
    }

    public void setAudioConnection(AudioConnectionImpl audioConnection) {
        this.audioConnectionLock.lock();
        try {
            this.api.setAudioConnection(this.getId(), audioConnection);
        }
        finally {
            this.audioConnectionLock.unlock();
        }
    }

    public void setPendingAudioConnection(AudioConnectionImpl audioConnection) {
        this.audioConnectionLock.lock();
        try {
            this.api.setPendingAudioConnection(this.getId(), audioConnection);
        }
        finally {
            this.audioConnectionLock.unlock();
        }
    }

    public void removeAudioConnection(AudioConnection audioConnection) {
        this.audioConnectionLock.lock();
        try {
            if (this.api.getPendingAudioConnectionByServerId(this.getId()) == audioConnection) {
                this.api.removePendingAudioConnection(this.getId());
            }
            if (this.api.getAudioConnectionByServerId(this.getId()) == audioConnection) {
                this.api.removeAudioConnection(this.getId());
            }
        }
        finally {
            this.audioConnectionLock.unlock();
        }
    }

    @Override
    public DiscordApi getApi() {
        return this.api;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Optional<AudioConnection> getAudioConnection() {
        return Optional.ofNullable(this.api.getAudioConnectionByServerId(this.getId()));
    }

    @Override
    public Set<ServerFeature> getFeatures() {
        return Collections.unmodifiableSet(new HashSet<ServerFeature>(this.serverFeatures));
    }

    @Override
    public BoostLevel getBoostLevel() {
        return this.boostLevel;
    }

    @Override
    public int getBoostCount() {
        return this.serverBoostCount;
    }

    @Override
    public Optional<ServerTextChannel> getRulesChannel() {
        return this.getTextChannelById(this.rulesChannelId);
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.ofNullable(this.description);
    }

    @Override
    public NsfwLevel getNsfwLevel() {
        return this.nsfwLevel;
    }

    @Override
    public Optional<ServerTextChannel> getModeratorsOnlyChannel() {
        return this.getTextChannelById(this.moderatorsOnlyChannelId);
    }

    @Override
    public Optional<VanityUrlCode> getVanityUrlCode() {
        return Optional.ofNullable(this.vanityUrlCode);
    }

    @Override
    public Optional<Icon> getDiscoverySplash() {
        if (this.splash == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new IconImpl(this.getApi(), new URL("https://cdn.discordapp.com/discovery-splashes/" + this.getIdAsString() + "/" + this.discoverySplash + ".png")));
        }
        catch (MalformedURLException e) {
            throw new AssertionError("Unexpected malformed discovery splash url", e);
        }
    }

    @Override
    public Locale getPreferredLocale() {
        return this.preferredLocale;
    }

    @Override
    public Region getRegion() {
        return this.region;
    }

    @Override
    public Optional<String> getNickname(User user) {
        return this.getRealMemberById(user.getId()).flatMap(Member::getNickname);
    }

    @Override
    public Optional<Instant> getServerBoostingSinceTimestamp(User user) {
        return this.getRealMemberById(user.getId()).flatMap(Member::getServerBoostingSinceTimestamp);
    }

    @Override
    public Optional<Instant> getTimeout(User user) {
        return this.getRealMemberById(user.getId()).flatMap(Member::getTimeout);
    }

    @Override
    public Optional<String> getUserServerAvatarHash(User user) {
        return this.getRealMemberById(user.getId()).flatMap(Member::getServerAvatarHash);
    }

    @Override
    public Optional<Icon> getUserServerAvatar(User user) {
        return this.getRealMemberById(user.getId()).flatMap(Member::getServerAvatar);
    }

    @Override
    public Optional<Icon> getUserServerAvatar(User user, int size) {
        return this.getRealMemberById(user.getId()).flatMap(member -> member.getServerAvatar(size));
    }

    @Override
    public boolean isPending(long userId) {
        return this.getRealMemberById(userId).map(Member::isPending).orElse(false);
    }

    @Override
    public boolean isSelfMuted(long userId) {
        return this.getRealMemberById(userId).map(Member::isSelfMuted).orElse(false);
    }

    @Override
    public boolean isSelfDeafened(long userId) {
        return this.getRealMemberById(userId).map(Member::isSelfDeafened).orElse(false);
    }

    @Override
    public boolean isMuted(long userId) {
        return this.getRealMemberById(userId).map(Member::isMuted).orElse(false);
    }

    @Override
    public boolean isDeafened(long userId) {
        return this.getRealMemberById(userId).map(Member::isDeafened).orElse(false);
    }

    @Override
    public Optional<Instant> getJoinedAtTimestamp(User user) {
        return this.getRealMemberById(user.getId()).map(Member::getJoinedAtTimestamp);
    }

    @Override
    public boolean isLarge() {
        return this.large;
    }

    @Override
    public int getMemberCount() {
        return this.memberCount.get();
    }

    @Override
    public Optional<User> getOwner() {
        return this.api.getCachedUserById(this.ownerId);
    }

    @Override
    public long getOwnerId() {
        return this.ownerId;
    }

    @Override
    public Optional<Long> getApplicationId() {
        return this.applicationId != -1L ? Optional.of(this.applicationId) : Optional.empty();
    }

    @Override
    public VerificationLevel getVerificationLevel() {
        return this.verificationLevel;
    }

    @Override
    public ExplicitContentFilterLevel getExplicitContentFilterLevel() {
        return this.explicitContentFilterLevel;
    }

    @Override
    public DefaultMessageNotificationLevel getDefaultMessageNotificationLevel() {
        return this.defaultMessageNotificationLevel;
    }

    @Override
    public MultiFactorAuthenticationLevel getMultiFactorAuthenticationLevel() {
        return this.multiFactorAuthenticationLevel;
    }

    @Override
    public Optional<Icon> getIcon() {
        if (this.iconHash == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new IconImpl(this.getApi(), new URL("https://cdn.discordapp.com/icons/" + this.getIdAsString() + "/" + this.iconHash + ".png")));
        }
        catch (MalformedURLException e) {
            logger.warn("Seems like the url of the icon is malformed! Please contact the developer!", (Throwable)e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Icon> getSplash() {
        if (this.splash == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new IconImpl(this.getApi(), new URL("https://cdn.discordapp.com/splashes/" + this.getIdAsString() + "/" + this.splash + ".png")));
        }
        catch (MalformedURLException e) {
            logger.warn("Seems like the url of the icon is malformed! Please contact the developer!", (Throwable)e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<ServerTextChannel> getSystemChannel() {
        return this.getTextChannelById(this.systemChannelId);
    }

    @Override
    public Optional<ServerVoiceChannel> getAfkChannel() {
        return this.getVoiceChannelById(this.afkChannelId);
    }

    @Override
    public int getAfkTimeoutInSeconds() {
        return this.afkTimeout;
    }

    @Override
    public CompletableFuture<Integer> getPruneCount(int days) {
        return new RestRequest(this.getApi(), RestMethod.GET, RestEndpoint.SERVER_PRUNE).setUrlParameters(this.getIdAsString()).addQueryParameter("days", String.valueOf(days)).execute(result -> result.getJsonBody().get("pruned").asInt());
    }

    @Override
    public CompletableFuture<Integer> pruneMembers(int days, String reason) {
        return new RestRequest(this.getApi(), RestMethod.POST, RestEndpoint.SERVER_PRUNE).setUrlParameters(this.getIdAsString()).addQueryParameter("days", String.valueOf(days)).setAuditLogReason(reason).execute(result -> result.getJsonBody().get("pruned").asInt());
    }

    @Override
    public CompletableFuture<Set<RichInvite>> getInvites() {
        return new RestRequest(this.getApi(), RestMethod.GET, RestEndpoint.SERVER_INVITE).setUrlParameters(this.getIdAsString()).execute(result -> {
            HashSet<InviteImpl> invites = new HashSet<InviteImpl>();
            for (JsonNode inviteJson : result.getJsonBody()) {
                invites.add(new InviteImpl(this.getApi(), inviteJson));
            }
            return Collections.unmodifiableSet(invites);
        });
    }

    @Override
    public boolean hasAllMembersInCache() {
        return this.getRealMembers().size() >= this.getMemberCount();
    }

    @Override
    public void requestMembersChunks() {
        this.api.getWebSocketAdapter().queueRequestGuildMembers(this);
    }

    @Override
    public Set<User> getMembers() {
        return this.api.getEntityCache().get().getMemberCache().getMembersByServer(this.getId()).stream().map(Member::getUser).collect(Collectors.toSet());
    }

    public Set<Member> getRealMembers() {
        return this.api.getEntityCache().get().getMemberCache().getMembersByServer(this.getId());
    }

    @Override
    public Optional<User> getMemberById(long id) {
        return this.api.getEntityCache().get().getMemberCache().getMemberByIdAndServer(id, this.getId()).map(Member::getUser);
    }

    public Optional<Member> getRealMemberById(long userId) {
        return this.api.getEntityCache().get().getMemberCache().getMemberByIdAndServer(userId, this.getId());
    }

    @Override
    public boolean isMember(User user) {
        return this.api.getEntityCache().get().getMemberCache().getMemberByIdAndServer(user.getId(), this.getId()).isPresent();
    }

    @Override
    public List<Role> getRoles() {
        return Collections.unmodifiableList(this.roles.values().stream().sorted().collect(Collectors.toList()));
    }

    @Override
    public List<Role> getRoles(User user) {
        return ((UserImpl)user).getMember().filter(member -> member.getServer().equals(this)).map(Member::getRoles).orElseGet(() -> this.getRealMemberById(user.getId()).map(Member::getRoles).orElseGet(Collections::emptyList));
    }

    @Override
    public boolean isWidgetEnabled() {
        return this.widgetEnabled;
    }

    @Override
    public Optional<Long> getWidgetChannelId() {
        return Optional.ofNullable(this.widgetChannelId);
    }

    @Override
    public Optional<Integer> getMaxPresences() {
        return Optional.ofNullable(this.maxPresences);
    }

    @Override
    public Optional<Integer> getMaxMembers() {
        return Optional.ofNullable(this.maxMembers);
    }

    @Override
    public Optional<Integer> getMaxVideoChannelUsers() {
        return Optional.ofNullable(this.maxVideoChannelUsers);
    }

    @Override
    public Optional<WelcomeScreen> getWelcomeScreen() {
        return Optional.ofNullable(this.welcomeScreen);
    }

    @Override
    public boolean isPremiumProgressBarEnabled() {
        return this.premiumProgressBarEnabled;
    }

    @Override
    public Optional<Role> getRoleById(long id) {
        return Optional.ofNullable(this.roles.get(id));
    }

    @Override
    public CompletableFuture<Void> delete(String reason) {
        return new RestRequest(this.getApi(), RestMethod.DELETE, RestEndpoint.SERVER).setUrlParameters(this.getIdAsString()).setAuditLogReason(reason).execute(result -> null);
    }

    @Override
    public CompletableFuture<Void> leave() {
        return new RestRequest(this.getApi(), RestMethod.DELETE, RestEndpoint.SERVER_SELF).setUrlParameters(this.getIdAsString()).execute(result -> null);
    }

    @Override
    public CompletableFuture<Void> addRoleToUser(User user, Role role, String reason) {
        return new RestRequest(this.getApi(), RestMethod.PUT, RestEndpoint.SERVER_MEMBER_ROLE).setUrlParameters(this.getIdAsString(), user.getIdAsString(), role.getIdAsString()).setAuditLogReason(reason).execute(result -> null);
    }

    @Override
    public CompletableFuture<Void> removeRoleFromUser(User user, Role role, String reason) {
        return new RestRequest(this.getApi(), RestMethod.DELETE, RestEndpoint.SERVER_MEMBER_ROLE).setUrlParameters(this.getIdAsString(), user.getIdAsString(), role.getIdAsString()).setAuditLogReason(reason).execute(result -> null);
    }

    @Override
    public CompletableFuture<Void> reorderRoles(List<Role> roles, String reason) {
        roles = new ArrayList<Role>(roles);
        ArrayNode body = JsonNodeFactory.instance.arrayNode();
        roles.removeIf(Role::isEveryoneRole);
        for (int i = 0; i < roles.size(); ++i) {
            body.addObject().put("id", roles.get(i).getIdAsString()).put("position", i + 1);
        }
        return new RestRequest(this.getApi(), RestMethod.PATCH, RestEndpoint.ROLE).setUrlParameters(this.getIdAsString()).setBody(body).setAuditLogReason(reason).execute(result -> null);
    }

    @Override
    public void selfMute() {
        this.api.getWebSocketAdapter().sendVoiceStateUpdate(this, this.getConnectedVoiceChannel(this.api.getYourself()).orElse(null), true, null);
    }

    @Override
    public void selfUnmute() {
        this.api.getWebSocketAdapter().sendVoiceStateUpdate(this, this.getConnectedVoiceChannel(this.api.getYourself()).orElse(null), false, null);
    }

    @Override
    public void selfDeafen() {
        this.api.getWebSocketAdapter().sendVoiceStateUpdate(this, this.getConnectedVoiceChannel(this.api.getYourself()).orElse(null), null, true);
    }

    @Override
    public void selfUndeafen() {
        this.api.getWebSocketAdapter().sendVoiceStateUpdate(this, this.getConnectedVoiceChannel(this.api.getYourself()).orElse(null), null, false);
    }

    @Override
    public CompletableFuture<User> requestMember(long userId) {
        return new RestRequest(this.getApi(), RestMethod.GET, RestEndpoint.SERVER_MEMBER).setUrlParameters(this.getIdAsString(), Long.toUnsignedString(userId)).execute(result -> new MemberImpl(this.api, this, result.getJsonBody(), null).getUser());
    }

    @Override
    public CompletableFuture<Void> kickUser(User user, String reason) {
        return new RestRequest(this.getApi(), RestMethod.DELETE, RestEndpoint.SERVER_MEMBER).setUrlParameters(this.getIdAsString(), user.getIdAsString()).setAuditLogReason(reason).execute(result -> null);
    }

    @Override
    public CompletableFuture<Void> banUser(String userId, long deleteMessageDuration, TimeUnit unit, String reason) {
        RestRequest<Void> request = new RestRequest(this.getApi(), RestMethod.PUT, RestEndpoint.BAN).setUrlParameters(this.getIdAsString(), userId);
        if (deleteMessageDuration > 0L) {
            request.setBody(JsonNodeFactory.instance.objectNode().put("delete_message_seconds", unit.toSeconds(deleteMessageDuration)));
        }
        if (reason != null) {
            request.setAuditLogReason(reason);
        }
        return request.execute(result -> null);
    }

    @Override
    public CompletableFuture<Void> unbanUser(long userId, String reason) {
        return new RestRequest(this.getApi(), RestMethod.DELETE, RestEndpoint.BAN).setUrlParameters(this.getIdAsString(), Long.toUnsignedString(userId)).setAuditLogReason(reason).execute(result -> null);
    }

    @Override
    public CompletableFuture<Ban> requestBan(long userId) {
        return new RestRequest(this.getApi(), RestMethod.GET, RestEndpoint.BAN).setUrlParameters(this.getIdAsString(), Long.toUnsignedString(userId)).execute(result -> new BanImpl(this, result.getJsonBody()));
    }

    @Override
    public CompletableFuture<Set<Ban>> getBans(Integer limit, Long after) {
        RestRequest<Set> request = new RestRequest(this.getApi(), RestMethod.GET, RestEndpoint.BAN).setUrlParameters(this.getIdAsString());
        if (limit != null) {
            request.addQueryParameter("limit", String.valueOf(limit));
        }
        if (after != null) {
            request.addQueryParameter("after", String.valueOf(after));
        }
        return request.execute(result -> {
            HashSet<BanImpl> bans = new HashSet<BanImpl>();
            for (JsonNode ban : result.getJsonBody()) {
                bans.add(new BanImpl(this, ban));
            }
            return Collections.unmodifiableSet(bans);
        });
    }

    @Override
    public CompletableFuture<Set<Ban>> getBans() {
        CompletableFuture<Set<Ban>> future = new CompletableFuture<Set<Ban>>();
        ArrayList<Ban> bans = new ArrayList<Ban>();
        this.fetchBansPageAndAddAllToCollection(null, bans, future);
        return future;
    }

    private void fetchBansPageAndAddAllToCollection(Long after, ArrayList<Ban> banList, CompletableFuture<Set<Ban>> futureToComplete) {
        ((CompletableFuture)this.getBans(1000, after).thenAccept(page -> {
            banList.addAll((Collection<Ban>)page);
            if (page.size() < 1000) {
                futureToComplete.complete(Collections.unmodifiableSet(new HashSet(banList)));
                return;
            }
            this.fetchBansPageAndAddAllToCollection(((Ban)banList.get(banList.size() - 1)).getUser().getId(), banList, futureToComplete);
        })).exceptionally(t -> {
            futureToComplete.completeExceptionally((Throwable)t);
            return null;
        });
    }

    @Override
    public CompletableFuture<List<Webhook>> getWebhooks() {
        return new RestRequest(this.getApi(), RestMethod.GET, RestEndpoint.SERVER_WEBHOOK).setUrlParameters(this.getIdAsString()).execute(result -> {
            ArrayList<WebhookImpl> webhooks = new ArrayList<WebhookImpl>();
            for (JsonNode webhookJson : result.getJsonBody()) {
                webhooks.add(WebhookImpl.createWebhook(this.getApi(), webhookJson));
            }
            return Collections.unmodifiableList(webhooks);
        });
    }

    @Override
    public CompletableFuture<List<Webhook>> getAllIncomingWebhooks() {
        return new RestRequest(this.getApi(), RestMethod.GET, RestEndpoint.SERVER_WEBHOOK).setUrlParameters(this.getIdAsString()).execute(result -> WebhookImpl.createAllIncomingWebhooksFromJsonArray(this.getApi(), result.getJsonBody()));
    }

    @Override
    public CompletableFuture<List<IncomingWebhook>> getIncomingWebhooks() {
        return new RestRequest(this.getApi(), RestMethod.GET, RestEndpoint.SERVER_WEBHOOK).setUrlParameters(this.getIdAsString()).execute(result -> IncomingWebhookImpl.createIncomingWebhooksFromJsonArray(this.getApi(), result.getJsonBody()));
    }

    @Override
    public CompletableFuture<AuditLog> getAuditLog(int limit) {
        return this.getAuditLogBefore(limit, null, null);
    }

    @Override
    public CompletableFuture<AuditLog> getAuditLog(int limit, AuditLogActionType type) {
        return this.getAuditLogBefore(limit, null, type);
    }

    @Override
    public CompletableFuture<AuditLog> getAuditLogBefore(int limit, AuditLogEntry before) {
        return this.getAuditLogBefore(limit, before, null);
    }

    @Override
    public CompletableFuture<AuditLog> getAuditLogBefore(int limit, AuditLogEntry before, AuditLogActionType type) {
        CompletableFuture<AuditLog> future = new CompletableFuture<AuditLog>();
        this.api.getThreadPool().getExecutorService().submit(() -> {
            try {
                AuditLogImpl auditLog = new AuditLogImpl(this);
                boolean requestMore = true;
                while (requestMore) {
                    int requestAmount = limit - auditLog.getEntries().size();
                    requestAmount = Math.min(requestAmount, 100);
                    RestRequest<JsonNode> request = new RestRequest(this.getApi(), RestMethod.GET, RestEndpoint.AUDIT_LOG).setUrlParameters(this.getIdAsString()).addQueryParameter("limit", String.valueOf(requestAmount));
                    List<AuditLogEntry> lastAuditLogEntries = auditLog.getEntries();
                    if (!lastAuditLogEntries.isEmpty()) {
                        request.addQueryParameter("before", lastAuditLogEntries.get(lastAuditLogEntries.size() - 1).getIdAsString());
                    } else if (before != null) {
                        request.addQueryParameter("before", before.getIdAsString());
                    }
                    if (type != null) {
                        request.addQueryParameter("action_type", String.valueOf(type.getValue()));
                    }
                    JsonNode data = request.execute(RestRequestResult::getJsonBody).join();
                    auditLog.addEntries(data);
                    requestMore = auditLog.getEntries().size() < limit && data.get("audit_log_entries").size() >= requestAmount;
                }
                future.complete(auditLog);
            }
            catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    @Override
    public Set<KnownCustomEmoji> getCustomEmojis() {
        return Collections.unmodifiableSet(this.customEmojis);
    }

    @Override
    public CompletableFuture<Set<SlashCommand>> getSlashCommands() {
        return this.api.getServerSlashCommands(this);
    }

    @Override
    public CompletableFuture<SlashCommand> getSlashCommandById(long commandId) {
        return this.api.getServerSlashCommandById(this, commandId);
    }

    @Override
    public List<ServerChannel> getChannels() {
        List channels = this.getUnorderedChannels().stream().filter(channel -> channel.asCategorizable().map(categorizable -> !categorizable.getCategory().isPresent()).orElse(false)).map(Channel::asRegularServerChannel).filter(Optional::isPresent).map(Optional::get).sorted(Comparator.comparingInt(channel -> channel.getType().getId()).thenComparing(RegularServerChannelImpl.COMPARE_BY_RAW_POSITION)).collect(Collectors.toList());
        this.getChannelCategories().forEach(category -> {
            channels.add(category);
            channels.addAll(category.getChannels());
        });
        HashMap<RegularServerChannel, List> regularServerChannelThreads = new HashMap<RegularServerChannel, List>();
        this.getThreadChannels().forEach(serverThreadChannel -> {
            RegularServerChannel regularServerChannel = serverThreadChannel.getParent();
            regularServerChannelThreads.merge(regularServerChannel, new ArrayList<ServerThreadChannel>(Collections.singletonList(serverThreadChannel)), (serverThreadChannels, serverThreadChannels2) -> {
                serverThreadChannels.addAll(serverThreadChannels2);
                return new ArrayList(serverThreadChannels);
            });
        });
        regularServerChannelThreads.forEach((serverTextChannel, serverThreadChannels) -> channels.addAll(channels.indexOf(serverTextChannel) + 1, serverThreadChannels));
        return Collections.unmodifiableList(channels);
    }

    @Override
    public List<RegularServerChannel> getRegularChannels() {
        return Collections.unmodifiableList(this.getUnorderedChannels().stream().filter(RegularServerChannel.class::isInstance).map(Channel::asRegularServerChannel).filter(Optional::isPresent).map(Optional::get).sorted(RegularServerChannelImpl.COMPARE_BY_RAW_POSITION).collect(Collectors.toList()));
    }

    @Override
    public List<ChannelCategory> getChannelCategories() {
        return Collections.unmodifiableList(this.getUnorderedChannels().stream().filter(ChannelCategory.class::isInstance).map(Channel::asChannelCategory).filter(Optional::isPresent).map(Optional::get).sorted(RegularServerChannelImpl.COMPARE_BY_RAW_POSITION).collect(Collectors.toList()));
    }

    @Override
    public List<ServerTextChannel> getTextChannels() {
        return Collections.unmodifiableList(this.getUnorderedChannels().stream().filter(ServerTextChannel.class::isInstance).map(Channel::asServerTextChannel).filter(Optional::isPresent).map(Optional::get).sorted(RegularServerChannelImpl.COMPARE_BY_RAW_POSITION).collect(Collectors.toList()));
    }

    @Override
    public List<ServerForumChannel> getForumChannels() {
        return Collections.unmodifiableList(this.getUnorderedChannels().stream().filter(ServerForumChannel.class::isInstance).map(Channel::asServerForumChannel).filter(Optional::isPresent).map(Optional::get).sorted(RegularServerChannelImpl.COMPARE_BY_RAW_POSITION).collect(Collectors.toList()));
    }

    @Override
    public List<ServerVoiceChannel> getVoiceChannels() {
        return Collections.unmodifiableList(this.getUnorderedChannels().stream().filter(ServerVoiceChannel.class::isInstance).map(Channel::asServerVoiceChannel).filter(Optional::isPresent).map(Optional::get).sorted(RegularServerChannelImpl.COMPARE_BY_RAW_POSITION).collect(Collectors.toList()));
    }

    @Override
    public List<ServerThreadChannel> getThreadChannels() {
        return Collections.unmodifiableList(this.getUnorderedChannels().stream().filter(ServerThreadChannel.class::isInstance).map(Channel::asServerThreadChannel).filter(Optional::isPresent).map(Optional::get).sorted(Comparator.comparing(channel -> channel.getMetadata().getArchiveTimestamp())).collect(Collectors.toList()));
    }

    @Override
    public Optional<ServerChannel> getChannelById(long id) {
        return this.api.getEntityCache().get().getChannelCache().getChannelById(id).filter(ServerChannel.class::isInstance).map(ServerChannel.class::cast);
    }

    @Override
    public Optional<RegularServerChannel> getRegularChannelById(long id) {
        return this.api.getEntityCache().get().getChannelCache().getChannelById(id).filter(RegularServerChannel.class::isInstance).map(RegularServerChannel.class::cast);
    }

    @Override
    public CompletableFuture<Void> joinServerThreadChannel(long channelId) {
        return new RestRequest(this.getApi(), RestMethod.PUT, RestEndpoint.JOIN_LEAVE_THREAD).setUrlParameters(String.valueOf(channelId)).execute(result -> null);
    }

    @Override
    public CompletableFuture<Void> leaveServerThreadChannel(long channelId) {
        return new RestRequest(this.getApi(), RestMethod.DELETE, RestEndpoint.JOIN_LEAVE_THREAD).setUrlParameters(String.valueOf(channelId)).execute(result -> null);
    }

    @Override
    public CompletableFuture<ActiveThreads> getActiveThreads() {
        return new RestRequest(this.getApi(), RestMethod.GET, RestEndpoint.LIST_ACTIVE_THREADS).setUrlParameters(this.getIdAsString()).execute(result -> new ActiveThreadsImpl((DiscordApiImpl)this.getApi(), this, result.getJsonBody()));
    }

    @Override
    public Set<Sticker> getStickers() {
        return Collections.unmodifiableSet(new HashSet<Sticker>(this.stickers.values()));
    }

    @Override
    public CompletableFuture<Set<Sticker>> requestStickers() {
        return new RestRequest(this.api, RestMethod.GET, RestEndpoint.SERVER_STICKER).setUrlParameters(this.getIdAsString()).execute(result -> {
            HashSet<StickerImpl> stickers = new HashSet<StickerImpl>();
            for (JsonNode stickerJson : result.getJsonBody()) {
                stickers.add(new StickerImpl(this.api, stickerJson));
            }
            return stickers;
        });
    }

    @Override
    public CompletableFuture<Sticker> requestStickerById(long id) {
        return new RestRequest(this.api, RestMethod.GET, RestEndpoint.SERVER_STICKER).setUrlParameters(this.getIdAsString()).execute(result -> new StickerImpl(this.api, result.getJsonBody()));
    }

    public void addSticker(Sticker sticker) {
        this.stickers.put(sticker.getId(), sticker);
    }

    public void removeSticker(Sticker sticker) {
        this.stickers.remove(sticker.getId());
    }

    @Override
    public void cleanup() {
        this.getUnorderedChannels().stream().map(DiscordEntity::getId).forEach(this.api::removeChannelFromCache);
        this.getMembers().forEach(user -> this.removeMember(user.getId()));
        this.stickers.values().forEach(this.api::removeSticker);
        this.getAudioConnection().ifPresent(this::removeAudioConnection);
    }

    public boolean equals(Object o) {
        return this == o || o != null && this.getClass() == o.getClass() && this.getId() == ((DiscordEntity)o).getId();
    }

    public int hashCode() {
        return Objects.hash(this.getId());
    }

    public String toString() {
        return String.format("Server (id: %s, name: %s)", this.getIdAsString(), this.getName());
    }

    @Override
    public EnumSet<SystemChannelFlag> getSystemChannelFlags() {
        return EnumSet.copyOf(this.systemChannelFlags);
    }
}

