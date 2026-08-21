/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.Proxy;
import java.net.ProxySelector;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.Javacord;
import org.javacord.api.entity.ApplicationInfo;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.activity.Activity;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.channel.RegularServerChannel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerForumChannel;
import org.javacord.api.entity.channel.ServerStageVoiceChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.channel.VoiceChannel;
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.message.UncachedMessageUtil;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.invite.Invite;
import org.javacord.api.entity.sticker.Sticker;
import org.javacord.api.entity.sticker.StickerPack;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.entity.webhook.Webhook;
import org.javacord.api.interaction.ApplicationCommand;
import org.javacord.api.interaction.ApplicationCommandBuilder;
import org.javacord.api.interaction.ApplicationCommandType;
import org.javacord.api.interaction.MessageContextMenu;
import org.javacord.api.interaction.ServerApplicationCommandPermissions;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.UserContextMenu;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.util.auth.Authenticator;
import org.javacord.api.util.concurrent.ThreadPool;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.api.util.ratelimit.LocalRatelimiter;
import org.javacord.api.util.ratelimit.Ratelimiter;
import org.javacord.core.audio.AudioConnectionImpl;
import org.javacord.core.entity.activity.ActivityImpl;
import org.javacord.core.entity.activity.ApplicationInfoImpl;
import org.javacord.core.entity.emoji.CustomEmojiImpl;
import org.javacord.core.entity.emoji.KnownCustomEmojiImpl;
import org.javacord.core.entity.message.MessageImpl;
import org.javacord.core.entity.message.MessageSetImpl;
import org.javacord.core.entity.message.UncachedMessageUtilImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.server.invite.InviteImpl;
import org.javacord.core.entity.sticker.StickerImpl;
import org.javacord.core.entity.sticker.StickerPackImpl;
import org.javacord.core.entity.user.Member;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.entity.user.UserPresence;
import org.javacord.core.entity.webhook.IncomingWebhookImpl;
import org.javacord.core.entity.webhook.WebhookImpl;
import org.javacord.core.interaction.ApplicationCommandBuilderDelegateImpl;
import org.javacord.core.interaction.ApplicationCommandImpl;
import org.javacord.core.interaction.MessageContextMenuImpl;
import org.javacord.core.interaction.ServerApplicationCommandPermissionsImpl;
import org.javacord.core.interaction.SlashCommandImpl;
import org.javacord.core.interaction.UserContextMenuImpl;
import org.javacord.core.util.ClassHelper;
import org.javacord.core.util.Cleanupable;
import org.javacord.core.util.cache.JavacordEntityCache;
import org.javacord.core.util.concurrent.ThreadPoolImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.event.EventDispatcher;
import org.javacord.core.util.event.ListenerManagerImpl;
import org.javacord.core.util.gateway.DiscordWebSocketAdapter;
import org.javacord.core.util.http.ProxyAuthenticator;
import org.javacord.core.util.http.TrustAllTrustManager;
import org.javacord.core.util.logging.LoggerUtil;
import org.javacord.core.util.ratelimit.RatelimitManager;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class DiscordApiImpl
implements DiscordApi,
DispatchQueueSelector {
    private static final Logger logger = LoggerUtil.getLogger(DiscordApiImpl.class);
    private static final Map<String, Ratelimiter> defaultGlobalRatelimiter = new ConcurrentHashMap<String, Ratelimiter>();
    private static final String BOT_TOKEN_PREFIX = "Bot ";
    private static final Map<String, Ratelimiter> defaultGatewayIdentifyRatelimiter = new ConcurrentHashMap<String, Ratelimiter>();
    private final ThreadPoolImpl threadPool = new ThreadPoolImpl();
    private final OkHttpClient httpClient;
    private final EventDispatcher eventDispatcher;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RatelimitManager ratelimitManager = new RatelimitManager(this);
    private final UncachedMessageUtil uncachedMessageUtil = new UncachedMessageUtilImpl(this);
    private volatile DiscordWebSocketAdapter websocketAdapter = null;
    private final String token;
    private final AtomicReference<CompletableFuture<Void>> disconnectFuture = new AtomicReference<Object>(null);
    private volatile UserStatus status = UserStatus.ONLINE;
    private volatile Activity activity;
    private volatile int defaultMessageCacheCapacity = 50;
    private volatile int defaultMessageCacheStorageTimeInSeconds = 43200;
    private boolean defaultAutomaticMessageCacheCleanupEnabled = true;
    private volatile Function<Integer, Integer> reconnectDelayProvider;
    private final int currentShard;
    private final int totalShards;
    private final Set<Intent> intents;
    private boolean dispatchEvents = true;
    private final boolean waitForServersOnStartup;
    private final boolean waitForUsersOnStartup;
    private volatile long latestGatewayLatencyNanos = -1L;
    private final Lock restLatencyLock = new ReentrantLock();
    private final Ratelimiter globalRatelimiter;
    private final Ratelimiter gatewayIdentifyRatelimiter;
    private final ProxySelector proxySelector;
    private final Proxy proxy;
    private final Authenticator proxyAuthenticator;
    private final boolean trustAllCertificates;
    private volatile User you;
    private volatile ApplicationInfo applicationInfo;
    private volatile Long timeOffset = null;
    private final AtomicReference<JavacordEntityCache> entityCache = new AtomicReference<JavacordEntityCache>(JavacordEntityCache.empty());
    private final boolean userCacheEnabled;
    private final ConcurrentHashMap<Long, Server> servers = new ConcurrentHashMap();
    private final ConcurrentHashMap<Long, AudioConnectionImpl> audioConnections = new ConcurrentHashMap();
    private final ConcurrentHashMap<Long, AudioConnectionImpl> pendingAudioConnections = new ConcurrentHashMap();
    private final ConcurrentHashMap<Long, Server> nonReadyServers = new ConcurrentHashMap();
    private final HashSet<Long> unavailableServers = new HashSet();
    private final ConcurrentHashMap<Long, KnownCustomEmoji> customEmojis = new ConcurrentHashMap();
    private final ConcurrentHashMap<Long, Sticker> stickers = new ConcurrentHashMap();
    private final Map<Long, WeakReference<Message>> messages = new ConcurrentHashMap<Long, WeakReference<Message>>();
    private final ReentrantLock messageCacheLock = new ReentrantLock();
    private final Map<Reference<? extends Message>, Long> messageIdByRef = Collections.synchronizedMap(new WeakHashMap());
    private final ReferenceQueue<Message> messagesCleanupQueue = new ReferenceQueue();
    private final Map<Class<? extends GloballyAttachableListener>, Map<GloballyAttachableListener, ListenerManagerImpl<? extends GloballyAttachableListener>>> listeners = Collections.synchronizedMap(new ConcurrentHashMap());
    private final Map<Class<?>, Map<Long, Map<Class<? extends ObjectAttachableListener>, Map<ObjectAttachableListener, ListenerManagerImpl<? extends ObjectAttachableListener>>>>> objectListeners = Collections.synchronizedMap(new ConcurrentHashMap());

    public DiscordApiImpl(String token, Ratelimiter globalRatelimiter, Ratelimiter gatewayIdentifyRatelimiter, ProxySelector proxySelector, Proxy proxy, Authenticator proxyAuthenticator, boolean trustAllCertificates) {
        this(token, 0, 1, Collections.emptySet(), true, false, globalRatelimiter, gatewayIdentifyRatelimiter, proxySelector, proxy, proxyAuthenticator, trustAllCertificates, null);
    }

    public DiscordApiImpl(String token, int currentShard, int totalShards, Set<Intent> intents, boolean waitForServersOnStartup, boolean waitForUsersOnStartup, Ratelimiter globalRatelimiter, Ratelimiter gatewayIdentifyRatelimiter, ProxySelector proxySelector, Proxy proxy, Authenticator proxyAuthenticator, boolean trustAllCertificates, CompletableFuture<DiscordApi> ready) {
        this(token, currentShard, totalShards, intents, waitForServersOnStartup, waitForUsersOnStartup, true, globalRatelimiter, gatewayIdentifyRatelimiter, proxySelector, proxy, proxyAuthenticator, trustAllCertificates, ready, null, Collections.emptyMap(), Collections.emptyList(), false, true);
    }

    private DiscordApiImpl(String token, int currentShard, int totalShards, Set<Intent> intents, boolean waitForServersOnStartup, boolean waitForUsersOnStartup, Ratelimiter globalRatelimiter, Ratelimiter gatewayIdentifyRatelimiter, ProxySelector proxySelector, Proxy proxy, Authenticator proxyAuthenticator, boolean trustAllCertificates, CompletableFuture<DiscordApi> ready, Dns dns) {
        this(token, currentShard, totalShards, intents, waitForServersOnStartup, waitForUsersOnStartup, true, globalRatelimiter, gatewayIdentifyRatelimiter, proxySelector, proxy, proxyAuthenticator, trustAllCertificates, ready, dns, Collections.emptyMap(), Collections.emptyList(), false, true);
    }

    public DiscordApiImpl(String token, int currentShard, int totalShards, Set<Intent> intents, boolean waitForServersOnStartup, boolean waitForUsersOnStartup, boolean registerShutdownHook, Ratelimiter globalRatelimiter, Ratelimiter gatewayIdentifyRatelimiter, ProxySelector proxySelector, Proxy proxy, Authenticator proxyAuthenticator, boolean trustAllCertificates, CompletableFuture<DiscordApi> ready, Dns dns, Map<Class<? extends GloballyAttachableListener>, List<Function<DiscordApi, GloballyAttachableListener>>> listenerSourceMap, List<Function<DiscordApi, GloballyAttachableListener>> unspecifiedListeners, boolean userCacheEnabled, boolean dispatchEvents) {
        this.token = token;
        this.currentShard = currentShard;
        this.totalShards = totalShards;
        this.waitForServersOnStartup = waitForServersOnStartup;
        this.waitForUsersOnStartup = waitForUsersOnStartup;
        this.globalRatelimiter = globalRatelimiter;
        this.gatewayIdentifyRatelimiter = gatewayIdentifyRatelimiter;
        this.proxySelector = proxySelector;
        this.proxy = proxy;
        this.proxyAuthenticator = proxyAuthenticator;
        this.trustAllCertificates = trustAllCertificates;
        this.userCacheEnabled = userCacheEnabled;
        this.dispatchEvents = dispatchEvents;
        this.reconnectDelayProvider = x -> (int)Math.round(Math.pow(x.intValue(), 1.5) - 1.0 / (1.0 / (0.1 * (double)x.intValue()) + 1.0) * Math.pow(x.intValue(), 1.5));
        this.intents = Stream.concat(intents.stream(), Stream.of(Intent.GUILDS)).collect(Collectors.toSet());
        if (proxySelector != null && proxy != null) {
            throw new IllegalStateException("proxy and proxySelector must not be configured both");
        }
        if (!intents.contains((Object)Intent.GUILD_MEMBERS) && this.isWaitingForUsersOnStartup()) {
            throw new IllegalArgumentException("Cannot wait for users when GUILD_MEMBERS intent is not set!");
        }
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(chain -> chain.proceed(chain.request().newBuilder().addHeader("User-Agent", Javacord.USER_AGENT).build())).addInterceptor(new HttpLoggingInterceptor(LoggerUtil.getLogger(OkHttpClient.class)::trace).setLevel(HttpLoggingInterceptor.Level.BODY)).proxyAuthenticator(new ProxyAuthenticator(proxyAuthenticator)).proxy(proxy);
        if (proxySelector != null) {
            httpClientBuilder.proxySelector(proxySelector);
        }
        if (dns != null) {
            httpClientBuilder.dns(dns);
        }
        if (trustAllCertificates) {
            logger.warn("All SSL certificates are trusted when connecting to the Discord API and websocket. This increases the risk of man-in-the-middle attacks!");
            TrustAllTrustManager trustManager = new TrustAllTrustManager();
            httpClientBuilder.sslSocketFactory(trustManager.createSslSocketFactory(), trustManager);
        }
        this.httpClient = httpClientBuilder.build();
        this.eventDispatcher = new EventDispatcher(this);
        if (ready != null) {
            this.getThreadPool().getExecutorService().submit(() -> {
                try {
                    this.websocketAdapter = new DiscordWebSocketAdapter(this);
                    this.websocketAdapter.isReady().whenComplete((readyReceived, throwable) -> {
                        if (readyReceived.booleanValue()) {
                            listenerSourceMap.forEach((clazz, listenerSources) -> listenerSources.forEach(listenerSource -> {
                                Class type = clazz;
                                GloballyAttachableListener listener = (GloballyAttachableListener)listenerSource.apply(this);
                                this.addListener(type, (GloballyAttachableListener)type.cast(listener));
                            }));
                            unspecifiedListeners.stream().map(source2 -> (GloballyAttachableListener)source2.apply(this)).forEach(this::addListener);
                            this.requestApplicationInfo().whenComplete((applicationInfo, exception) -> {
                                if (exception != null) {
                                    logger.error("Could not access self application info on startup!", (Throwable)exception);
                                    this.threadPool.shutdown();
                                    ready.completeExceptionally((Throwable)exception);
                                } else {
                                    this.applicationInfo = applicationInfo;
                                    ready.complete(this);
                                }
                            });
                        } else {
                            this.threadPool.shutdown();
                            ready.completeExceptionally(new IllegalStateException("Websocket closed before READY packet was received!"));
                        }
                    });
                }
                catch (Throwable t) {
                    if (this.websocketAdapter != null) {
                        this.websocketAdapter.disconnect();
                    }
                    ready.completeExceptionally(t);
                }
            });
            this.getThreadPool().getScheduler().scheduleWithFixedDelay(() -> {
                this.messageCacheLock.lock();
                try {
                    Reference<Message> messageRef = this.messagesCleanupQueue.poll();
                    while (messageRef != null) {
                        Long messageId = this.messageIdByRef.remove(messageRef);
                        if (messageId != null) {
                            this.messages.remove(messageId, messageRef);
                        }
                        messageRef = this.messagesCleanupQueue.poll();
                    }
                }
                catch (Throwable t) {
                    logger.error("Failed to process messages cleanup queue!", t);
                }
                finally {
                    this.messageCacheLock.unlock();
                }
            }, 30L, 30L, TimeUnit.SECONDS);
            if (registerShutdownHook) {
                ready.thenAccept(api -> {
                    WeakReference<DiscordApi> discordApiReference = new WeakReference<DiscordApi>((DiscordApi)api);
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> Optional.ofNullable((DiscordApi)discordApiReference.get()).ifPresent(DiscordApi::disconnect), String.format("Javacord - Shutdown Disconnector (%s)", api)));
                });
            }
        } else if (registerShutdownHook) {
            WeakReference<DiscordApiImpl> discordApiReference = new WeakReference<DiscordApiImpl>(this);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> Optional.ofNullable((DiscordApi)discordApiReference.get()).ifPresent(DiscordApi::disconnect), String.format("Javacord - Shutdown Disconnector (%s)", this)));
        }
    }

    public AtomicReference<JavacordEntityCache> getEntityCache() {
        return this.entityCache;
    }

    public boolean hasUserCacheEnabled() {
        return this.userCacheEnabled;
    }

    @Override
    public void setEventsDispatchable(boolean dispatchEvents) {
        this.dispatchEvents = dispatchEvents;
    }

    @Override
    public boolean canDispatchEvents() {
        return this.dispatchEvents;
    }

    public OkHttpClient getHttpClient() {
        if (this.disconnectFuture.get() != null) {
            throw new IllegalStateException("disconnect was called already");
        }
        return this.httpClient;
    }

    public EventDispatcher getEventDispatcher() {
        return this.eventDispatcher;
    }

    public RatelimitManager getRatelimitManager() {
        return this.ratelimitManager;
    }

    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    public void purgeCache() {
        this.servers.values().stream().map(Cleanupable.class::cast).forEach(Cleanupable::cleanup);
        this.servers.clear();
        this.entityCache.get().getChannelCache().getChannels().stream().filter(Cleanupable.class::isInstance).map(Cleanupable.class::cast).forEach(Cleanupable::cleanup);
        this.entityCache.set(JavacordEntityCache.empty());
        this.unavailableServers.clear();
        this.customEmojis.clear();
        this.messageCacheLock.lock();
        try {
            this.messages.clear();
            this.messageIdByRef.clear();
        }
        finally {
            this.messageCacheLock.unlock();
        }
        this.timeOffset = null;
    }

    public AudioConnectionImpl getAudioConnectionByServerId(long serverId) {
        return this.audioConnections.get(serverId);
    }

    public void setAudioConnection(long serverId, AudioConnectionImpl connection) {
        this.audioConnections.put(serverId, connection);
    }

    public void removeAudioConnection(long serverId) {
        this.audioConnections.remove(serverId);
    }

    public AudioConnectionImpl getPendingAudioConnectionByServerId(long serverId) {
        return this.pendingAudioConnections.get(serverId);
    }

    public void setPendingAudioConnection(long serverId, AudioConnectionImpl connection) {
        this.pendingAudioConnections.put(serverId, connection);
    }

    public void removePendingAudioConnection(long serverId) {
        this.pendingAudioConnections.remove(serverId);
    }

    public Collection<Server> getAllServers() {
        ArrayList<Server> allServers = new ArrayList<Server>(this.nonReadyServers.values());
        allServers.addAll(this.servers.values());
        return Collections.unmodifiableList(allServers);
    }

    public Optional<Server> getPossiblyUnreadyServerById(long id) {
        if (this.nonReadyServers.containsKey(id)) {
            return Optional.ofNullable(this.nonReadyServers.get(id));
        }
        return Optional.ofNullable(this.servers.get(id));
    }

    public void addServerToCache(ServerImpl server) {
        this.removeServerFromCache(server.getId());
        this.nonReadyServers.put(server.getId(), server);
        server.addServerReadyConsumer(s -> {
            this.nonReadyServers.remove(s.getId());
            this.removeUnavailableServerFromCache(s.getId());
            this.servers.put(s.getId(), (Server)s);
        });
    }

    public void removeServerFromCache(long serverId) {
        this.servers.computeIfPresent(serverId, (key, server) -> {
            ((Cleanupable)((Object)server)).cleanup();
            return null;
        });
        this.nonReadyServers.computeIfPresent(serverId, (key, server) -> {
            ((Cleanupable)((Object)server)).cleanup();
            return null;
        });
    }

    public void addChannelToCache(Channel channel) {
        this.entityCache.getAndUpdate(cache -> {
            Channel oldChannel = cache.getChannelCache().getChannelById(channel.getId()).orElse(null);
            if (oldChannel != channel && oldChannel instanceof Cleanupable) {
                ((Cleanupable)((Object)oldChannel)).cleanup();
            }
            return cache.updateChannelCache(channelCache -> channelCache.addChannel(channel));
        });
    }

    public void updateUserPresence(long userId, UnaryOperator<UserPresence> mapper) {
        this.entityCache.getAndUpdate(cache -> {
            UserPresence presence = cache.getUserPresenceCache().getPresenceByUserId(userId).orElseGet(() -> new UserPresence(userId, null, null, io.vavr.collection.HashMap.empty()));
            return cache.updateUserPresenceCache(userPresenceCache -> userPresenceCache.removeUserPresence(presence).addUserPresence((UserPresence)mapper.apply(presence)));
        });
    }

    public void removeChannelFromCache(long channelId) {
        this.entityCache.getAndUpdate(cache -> {
            Channel channel = cache.getChannelCache().getChannelById(channelId).orElse(null);
            if (channel == null) {
                return cache;
            }
            channel.asServerChannel().ifPresent(serverChannel -> {
                if (serverChannel.asServerThreadChannel().isPresent()) {
                    return;
                }
                serverChannel.getServer().getThreadChannels().stream().filter(c -> c.getParent().getId() == serverChannel.getId()).mapToLong(DiscordEntity::getId).forEach(this::removeChannelFromCache);
            });
            if (channel instanceof Cleanupable) {
                ((Cleanupable)((Object)channel)).cleanup();
            }
            return cache.updateChannelCache(channelCache -> channelCache.removeChannel(channel));
        });
    }

    public void addMemberToCacheOrReplaceExisting(Member member) {
        if (!this.isUserCacheEnabled()) {
            return;
        }
        this.entityCache.getAndUpdate(cache -> {
            Member oldMember = cache.getMemberCache().getMemberByIdAndServer(member.getId(), member.getServer().getId()).orElse(null);
            return cache.updateMemberCache(memberCache -> memberCache.removeMember(oldMember).addMember(member));
        });
    }

    public void updateUserOfAllMembers(User user) {
        this.entityCache.getAndUpdate(cache -> {
            JavacordEntityCache newCache = cache;
            for (Member member : cache.getMemberCache().getMembersById(user.getId())) {
                newCache = newCache.updateMemberCache(memberCache -> memberCache.removeMember(member).addMember(((MemberImpl)member).setUser((UserImpl)user)));
            }
            return newCache;
        });
    }

    public void removeMemberFromCache(long memberId, long serverId) {
        this.entityCache.getAndUpdate(cache -> {
            Member member = cache.getMemberCache().getMemberByIdAndServer(memberId, serverId).orElse(null);
            if (member == null) {
                return cache;
            }
            return cache.updateMemberCache(memberCache -> memberCache.removeMember(member));
        });
    }

    public void addUnavailableServerToCache(long serverId) {
        this.unavailableServers.add(serverId);
    }

    private void removeUnavailableServerFromCache(long serverId) {
        this.unavailableServers.remove(serverId);
    }

    public long getLatestGatewayLatencyNanos() {
        return this.latestGatewayLatencyNanos;
    }

    public void setLatestGatewayLatencyNanos(long latestGatewayLatencyNanos) {
        this.latestGatewayLatencyNanos = latestGatewayLatencyNanos;
    }

    public void setYourself(User yourself) {
        this.you = yourself;
    }

    public Long getTimeOffset() {
        return this.timeOffset;
    }

    public void setTimeOffset(Long timeOffset) {
        this.timeOffset = timeOffset;
    }

    public KnownCustomEmoji getOrCreateKnownCustomEmoji(Server server, JsonNode data) {
        long id = Long.parseLong(data.get("id").asText());
        return this.customEmojis.computeIfAbsent(id, key -> new KnownCustomEmojiImpl(this, server, data));
    }

    public CustomEmoji getKnownCustomEmojiOrCreateCustomEmoji(JsonNode data) {
        long id = Long.parseLong(data.get("id").asText());
        CustomEmoji emoji = this.customEmojis.get(id);
        return emoji == null ? new CustomEmojiImpl(this, data) : emoji;
    }

    @Override
    public CustomEmoji getKnownCustomEmojiOrCreateCustomEmoji(long id, String name, boolean animated) {
        CustomEmoji emoji = this.customEmojis.get(id);
        return emoji == null ? new CustomEmojiImpl(this, id, name, animated) : emoji;
    }

    public void removeCustomEmoji(KnownCustomEmoji emoji) {
        this.customEmojis.remove(emoji.getId());
    }

    public Sticker getOrCreateSticker(JsonNode data) {
        long id = data.get("id").asLong();
        return this.stickers.computeIfAbsent(id, key -> new StickerImpl(this, data));
    }

    public void removeSticker(Sticker sticker) {
        this.stickers.remove(sticker.getId());
    }

    @Override
    public Optional<Sticker> getStickerById(long id) {
        return Optional.ofNullable(this.stickers.get(id));
    }

    @Override
    public CompletableFuture<Sticker> requestStickerById(long id) {
        return new RestRequest(this, RestMethod.GET, RestEndpoint.STICKER).setUrlParameters(String.valueOf(id)).execute(result -> new StickerImpl(this, result.getJsonBody()));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Message getOrCreateMessage(TextChannel channel, JsonNode data) {
        long id = Long.parseLong(data.get("id").asText());
        this.messageCacheLock.lock();
        try {
            Message message = this.getCachedMessageById(id).orElseGet(() -> new MessageImpl(this, channel, data));
            return message;
        }
        finally {
            this.messageCacheLock.unlock();
        }
    }

    public void addMessageToCache(Message message) {
        this.messageCacheLock.lock();
        try {
            this.messages.compute(message.getId(), (key, value) -> {
                if (value == null || value.get() == null) {
                    WeakReference<Message> result = new WeakReference<Message>(message, this.messagesCleanupQueue);
                    this.messageIdByRef.put((Reference<? extends Message>)result, (Long)key);
                    return result;
                }
                return value;
            });
        }
        finally {
            this.messageCacheLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeMessageFromCache(long messageId) {
        this.messageCacheLock.lock();
        try {
            WeakReference<Message> messageRef = this.messages.remove(messageId);
            if (messageRef != null) {
                this.messageIdByRef.remove(messageRef, messageId);
            }
        }
        finally {
            this.messageCacheLock.unlock();
        }
    }

    public <T extends ObjectAttachableListener> ListenerManager<T> addObjectListener(Class<?> objectClass, long objectId, Class<T> listenerClass, T listener) {
        Map listeners = this.objectListeners.computeIfAbsent(objectClass, key -> new ConcurrentHashMap()).computeIfAbsent(objectId, key -> new ConcurrentHashMap()).computeIfAbsent(listenerClass, c -> Collections.synchronizedMap(new LinkedHashMap()));
        return listeners.computeIfAbsent(listener, key -> new ListenerManagerImpl<ObjectAttachableListener>(this, listener, listenerClass, objectClass, objectId));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public <T extends ObjectAttachableListener> void removeObjectListener(Class<?> objectClass, long objectId, Class<T> listenerClass, T listener) {
        Map<Class<?>, Map<Long, Map<Class<? extends ObjectAttachableListener>, Map<ObjectAttachableListener, ListenerManagerImpl<? extends ObjectAttachableListener>>>>> map = this.objectListeners;
        synchronized (map) {
            if (objectClass == null) {
                return;
            }
            Map<Long, Map<Class<? extends ObjectAttachableListener>, Map<ObjectAttachableListener, ListenerManagerImpl<? extends ObjectAttachableListener>>>> objectListener = this.objectListeners.get(objectClass);
            if (objectListener == null) {
                return;
            }
            Map<Class<? extends ObjectAttachableListener>, Map<ObjectAttachableListener, ListenerManagerImpl<? extends ObjectAttachableListener>>> listeners = objectListener.get(objectId);
            if (listeners == null) {
                return;
            }
            Map<ObjectAttachableListener, ListenerManagerImpl<? extends ObjectAttachableListener>> classListeners = listeners.get(listenerClass);
            if (classListeners == null) {
                return;
            }
            ListenerManagerImpl<? extends ObjectAttachableListener> listenerManager = classListeners.get(listener);
            if (listenerManager == null) {
                return;
            }
            classListeners.remove(listener);
            listenerManager.removed();
            if (classListeners.isEmpty()) {
                listeners.remove(listenerClass);
                if (listeners.isEmpty()) {
                    objectListener.remove(objectId);
                    if (objectListener.isEmpty()) {
                        this.objectListeners.remove(objectClass);
                    }
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeObjectListeners(Class<?> objectClass, long objectId) {
        if (objectClass == null) {
            return;
        }
        Map<Class<?>, Map<Long, Map<Class<? extends ObjectAttachableListener>, Map<ObjectAttachableListener, ListenerManagerImpl<? extends ObjectAttachableListener>>>>> map = this.objectListeners;
        synchronized (map) {
            Map<Long, Map<Class<? extends ObjectAttachableListener>, Map<ObjectAttachableListener, ListenerManagerImpl<? extends ObjectAttachableListener>>>> objects = this.objectListeners.get(objectClass);
            if (objects == null) {
                return;
            }
            objects.computeIfPresent(objectId, (id, listeners) -> {
                listeners.values().stream().flatMap(map -> map.values().stream()).forEach(ListenerManagerImpl::removed);
                listeners.clear();
                return null;
            });
            if (objects.isEmpty()) {
                this.objectListeners.remove(objectClass);
            }
        }
    }

    public <T extends ObjectAttachableListener> Map<T, List<Class<T>>> getObjectListeners(Class<?> objectClass, long objectId) {
        return Collections.unmodifiableMap(Optional.ofNullable(objectClass).map(this.objectListeners::get).map(objectListener -> (Map)objectListener.get(objectId)).map(Map::entrySet).map(Collection::stream).map(entryStream -> entryStream.flatMap(entry -> ((Map)entry.getValue()).keySet().stream().map(listener -> new AbstractMap.SimpleEntry<ObjectAttachableListener, Class>((ObjectAttachableListener)listener, (Class)entry.getKey())))).map(entryStream -> entryStream.collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())))).orElseGet(HashMap::new));
    }

    public <T extends ObjectAttachableListener> List<T> getObjectListeners(Class<?> objectClass, long objectId, Class<T> listenerClass) {
        return Collections.unmodifiableList(Optional.ofNullable(objectClass).map(this.objectListeners::get).map(objectListener -> (Map)objectListener.get(objectId)).map(listeners -> (Map)listeners.get(listenerClass)).map(Map::keySet).map(ArrayList::new).orElseGet(ArrayList::new));
    }

    @Override
    public <T extends GloballyAttachableListener> Map<T, List<Class<T>>> getListeners() {
        return Collections.unmodifiableMap(this.listeners.entrySet().stream().flatMap(entry -> ((Map)entry.getValue()).keySet().stream().map(listener -> new AbstractMap.SimpleEntry<GloballyAttachableListener, Class>((GloballyAttachableListener)listener, (Class)entry.getKey()))).collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList()))));
    }

    @Override
    public <T extends GloballyAttachableListener> List<T> getListeners(Class<T> listenerClass) {
        return Collections.unmodifiableList(Optional.ofNullable(listenerClass).map(this.listeners::get).map(Map::keySet).map(ArrayList::new).orElseGet(ArrayList::new));
    }

    @Override
    public String getPrefixedToken() {
        return BOT_TOKEN_PREFIX + this.token;
    }

    @Override
    public Set<Intent> getIntents() {
        return Collections.unmodifiableSet(this.intents);
    }

    @Override
    public String getToken() {
        return this.token;
    }

    @Override
    public ThreadPool getThreadPool() {
        return this.threadPool;
    }

    @Override
    public CompletableFuture<Set<ApplicationCommand>> getGlobalApplicationCommands() {
        return new RestRequest(this, RestMethod.GET, RestEndpoint.APPLICATION_COMMANDS).setUrlParameters(String.valueOf(this.getClientId())).execute(result -> this.jsonToApplicationCommandList(result.getJsonBody()));
    }

    @Override
    public CompletableFuture<ApplicationCommand> getGlobalApplicationCommandById(long commandId) {
        return new RestRequest(this, RestMethod.GET, RestEndpoint.APPLICATION_COMMANDS).setUrlParameters(String.valueOf(this.getClientId()), String.valueOf(commandId)).execute(result -> this.jsonToApplicationCommand(result.getJsonBody()));
    }

    @Override
    public CompletableFuture<Set<ApplicationCommand>> getServerApplicationCommands(Server server) {
        return new RestRequest(this, RestMethod.GET, RestEndpoint.SERVER_APPLICATION_COMMANDS).setUrlParameters(String.valueOf(this.getClientId()), server.getIdAsString()).execute(result -> this.jsonToApplicationCommandList(result.getJsonBody()));
    }

    @Override
    public CompletableFuture<ApplicationCommand> getServerApplicationCommandById(Server server, long commandId) {
        return new RestRequest(this, RestMethod.GET, RestEndpoint.SERVER_APPLICATION_COMMANDS).setUrlParameters(String.valueOf(this.getClientId()), server.getIdAsString(), String.valueOf(commandId)).execute(result -> this.jsonToApplicationCommand(result.getJsonBody()));
    }

    @Override
    public CompletableFuture<Set<SlashCommand>> getGlobalSlashCommands() {
        return new RestRequest(this, RestMethod.GET, RestEndpoint.APPLICATION_COMMANDS).setUrlParameters(String.valueOf(this.getClientId())).execute(result -> this.jsonToSlashCommandList(result.getJsonBody()));
    }

    @Override
    public CompletableFuture<SlashCommand> getGlobalSlashCommandById(long commandId) {
        return new RestRequest(this, RestMethod.GET, RestEndpoint.APPLICATION_COMMANDS).setUrlParameters(String.valueOf(this.getClientId()), String.valueOf(commandId)).execute(result -> (SlashCommand)this.jsonToApplicationCommand(result.getJsonBody()));
    }

    @Override
    public CompletableFuture<Set<SlashCommand>> getServerSlashCommands(Server server) {
        return new RestRequest(this, RestMethod.GET, RestEndpoint.SERVER_APPLICATION_COMMANDS).setUrlParameters(String.valueOf(this.getClientId()), server.getIdAsString()).execute(result -> this.jsonToSlashCommandList(result.getJsonBody()));
    }

    @Override
    public CompletableFuture<SlashCommand> getServerSlashCommandById(Server server, long commandId) {
        return new RestRequest(this, RestMethod.GET, RestEndpoint.SERVER_APPLICATION_COMMANDS).setUrlParameters(String.valueOf(this.getClientId()), server.getIdAsString(), String.valueOf(commandId)).execute(result -> (SlashCommand)this.jsonToApplicationCommand(result.getJsonBody()));
    }

    @Override
    public CompletableFuture<Set<UserContextMenu>> getGlobalUserContextMenus() {
        return new RestRequest(this, RestMethod.GET, RestEndpoint.APPLICATION_COMMANDS).setUrlParameters(String.valueOf(this.getClientId())).execute(result -> this.jsonToUserContextMenuList(result.getJsonBody()));
    }

    @Override
    public CompletableFuture<UserContextMenu> getGlobalUserContextMenuById(long commandId) {
        return new RestRequest(this, RestMethod.GET, RestEndpoint.APPLICATION_COMMANDS).setUrlParameters(String.valueOf(this.getClientId()), String.valueOf(commandId)).execute(result -> (UserContextMenu)this.jsonToApplicationCommand(result.getJsonBody()));
    }

    @Override
    public CompletableFuture<Set<UserContextMenu>> getServerUserContextMenus(Server server) {
        return new RestRequest(this, RestMethod.GET, RestEndpoint.SERVER_APPLICATION_COMMANDS).setUrlParameters(String.valueOf(this.getClientId()), server.getIdAsString()).execute(result -> this.jsonToUserContextMenuList(result.getJsonBody()));
    }

    @Override
    public CompletableFuture<UserContextMenu> getServerUserContextMenuById(Server server, long commandId) {
        return new RestRequest(this, RestMethod.GET, RestEndpoint.SERVER_APPLICATION_COMMANDS).setUrlParameters(String.valueOf(this.getClientId()), server.getIdAsString(), String.valueOf(commandId)).execute(result -> (UserContextMenu)this.jsonToApplicationCommand(result.getJsonBody()));
    }

    @Override
    public CompletableFuture<Set<MessageContextMenu>> getGlobalMessageContextMenus() {
        return new RestRequest(this, RestMethod.GET, RestEndpoint.APPLICATION_COMMANDS).setUrlParameters(String.valueOf(this.getClientId())).execute(result -> this.jsonToMessageContextMenuList(result.getJsonBody()));
    }

    @Override
    public CompletableFuture<MessageContextMenu> getGlobalMessageContextMenuById(long commandId) {
        return new RestRequest(this, RestMethod.GET, RestEndpoint.APPLICATION_COMMANDS).setUrlParameters(String.valueOf(this.getClientId()), String.valueOf(commandId)).execute(result -> (MessageContextMenu)this.jsonToApplicationCommand(result.getJsonBody()));
    }

    @Override
    public CompletableFuture<Set<MessageContextMenu>> getServerMessageContextMenus(Server server) {
        return new RestRequest(this, RestMethod.GET, RestEndpoint.SERVER_APPLICATION_COMMANDS).setUrlParameters(String.valueOf(this.getClientId()), server.getIdAsString()).execute(result -> this.jsonToMessageContextMenuList(result.getJsonBody()));
    }

    @Override
    public CompletableFuture<MessageContextMenu> getServerMessageContextMenuById(Server server, long commandId) {
        return new RestRequest(this, RestMethod.GET, RestEndpoint.SERVER_APPLICATION_COMMANDS).setUrlParameters(String.valueOf(this.getClientId()), server.getIdAsString(), String.valueOf(commandId)).execute(result -> (MessageContextMenu)this.jsonToApplicationCommand(result.getJsonBody()));
    }

    @Override
    public CompletableFuture<Set<ServerApplicationCommandPermissions>> getServerApplicationCommandPermissions(Server server) {
        return new RestRequest(this, RestMethod.GET, RestEndpoint.SERVER_APPLICATION_COMMAND_PERMISSIONS).setUrlParameters(String.valueOf(this.getClientId()), server.getIdAsString()).execute(result -> this.jsonToServerApplicationCommandPermissionsSet(result.getJsonBody()));
    }

    @Override
    public CompletableFuture<ServerApplicationCommandPermissions> getServerApplicationCommandPermissionsById(Server server, long commandId) {
        return new RestRequest(this, RestMethod.GET, RestEndpoint.APPLICATION_COMMAND_PERMISSIONS).setUrlParameters(String.valueOf(this.getClientId()), server.getIdAsString(), String.valueOf(commandId)).execute(result -> new ServerApplicationCommandPermissionsImpl(this, result.getJsonBody()));
    }

    @Override
    public CompletableFuture<Set<ApplicationCommand>> bulkOverwriteGlobalApplicationCommands(Set<? extends ApplicationCommandBuilder<?, ?, ?>> applicationCommandBuilderList) {
        return new RestRequest(this, RestMethod.PUT, RestEndpoint.APPLICATION_COMMANDS).setUrlParameters(String.valueOf(this.getClientId())).setBody(this.applicationCommandBuildersToArrayNode(applicationCommandBuilderList)).execute(result -> this.jsonToApplicationCommandList(result.getJsonBody()));
    }

    @Override
    public CompletableFuture<Set<ApplicationCommand>> bulkOverwriteServerApplicationCommands(long server, Set<? extends ApplicationCommandBuilder<?, ?, ?>> applicationCommandBuilderList) {
        return new RestRequest(this, RestMethod.PUT, RestEndpoint.SERVER_APPLICATION_COMMANDS).setUrlParameters(String.valueOf(this.getClientId()), String.valueOf(server)).setBody(this.applicationCommandBuildersToArrayNode(applicationCommandBuilderList)).execute(result -> this.jsonToApplicationCommandList(result.getJsonBody()));
    }

    private ArrayNode applicationCommandBuildersToArrayNode(Set<? extends ApplicationCommandBuilder<?, ?, ?>> applicationCommandBuilderList) {
        ArrayNode body = JsonNodeFactory.instance.arrayNode();
        for (ApplicationCommandBuilder<?, ?, ?> applicationCommandBuilder : applicationCommandBuilderList) {
            body.add(((ApplicationCommandBuilderDelegateImpl)applicationCommandBuilder.getDelegate()).getJsonBodyForApplicationCommand());
        }
        return body;
    }

    private Set<ServerApplicationCommandPermissions> jsonToServerApplicationCommandPermissionsSet(JsonNode resultJson) {
        HashSet<ServerApplicationCommandPermissions> permissions = new HashSet<ServerApplicationCommandPermissions>();
        for (JsonNode jsonNode : resultJson) {
            permissions.add(new ServerApplicationCommandPermissionsImpl(this, jsonNode));
        }
        return permissions;
    }

    private Set<ApplicationCommand> jsonToApplicationCommandList(JsonNode resultJson) {
        HashSet<ApplicationCommand> applicationCommands = new HashSet<ApplicationCommand>();
        for (JsonNode applicationCommandJson : resultJson) {
            applicationCommands.add(this.jsonToApplicationCommand(applicationCommandJson));
        }
        return Collections.unmodifiableSet(applicationCommands);
    }

    private ApplicationCommand jsonToApplicationCommand(JsonNode applicationCommandJson) {
        ApplicationCommandType type = ApplicationCommandType.fromValue(applicationCommandJson.get("type").asInt());
        if (type == ApplicationCommandType.SLASH) {
            return new SlashCommandImpl(this, applicationCommandJson);
        }
        if (type == ApplicationCommandType.USER) {
            return new UserContextMenuImpl(this, applicationCommandJson);
        }
        if (type == ApplicationCommandType.MESSAGE) {
            return new MessageContextMenuImpl(this, applicationCommandJson);
        }
        return new ApplicationCommandImpl(this, applicationCommandJson){

            @Override
            public ApplicationCommandType getType() {
                return ApplicationCommandType.APPLICATION_COMMAND;
            }
        };
    }

    private Set<SlashCommand> jsonToSlashCommandList(JsonNode resultJson) {
        return this.jsonToApplicationCommandList(resultJson).stream().filter(applicationCommand -> applicationCommand.getType() == ApplicationCommandType.SLASH).map(SlashCommand.class::cast).collect(Collectors.toSet());
    }

    private Set<UserContextMenu> jsonToUserContextMenuList(JsonNode resultJson) {
        return this.jsonToApplicationCommandList(resultJson).stream().filter(applicationCommand -> applicationCommand.getType() == ApplicationCommandType.USER).map(UserContextMenu.class::cast).collect(Collectors.toSet());
    }

    private Set<MessageContextMenu> jsonToMessageContextMenuList(JsonNode resultJson) {
        return this.jsonToApplicationCommandList(resultJson).stream().filter(applicationCommand -> applicationCommand.getType() == ApplicationCommandType.MESSAGE).map(MessageContextMenu.class::cast).collect(Collectors.toSet());
    }

    @Override
    public UncachedMessageUtil getUncachedMessageUtil() {
        return this.uncachedMessageUtil;
    }

    public DiscordWebSocketAdapter getWebSocketAdapter() {
        return this.websocketAdapter;
    }

    @Override
    public Optional<Ratelimiter> getGlobalRatelimiter() {
        if (this.globalRatelimiter == null) {
            Ratelimiter ratelimiter = defaultGlobalRatelimiter.computeIfAbsent(this.getToken(), token -> new LocalRatelimiter(5, Duration.ofMillis(112L)));
            return Optional.of(ratelimiter);
        }
        return Optional.of(this.globalRatelimiter);
    }

    @Override
    public Ratelimiter getGatewayIdentifyRatelimiter() {
        if (this.gatewayIdentifyRatelimiter == null) {
            return defaultGatewayIdentifyRatelimiter.computeIfAbsent(this.getToken(), token -> new LocalRatelimiter(1, Duration.ofMillis(5500L)));
        }
        return this.gatewayIdentifyRatelimiter;
    }

    @Override
    public Duration getLatestGatewayLatency() {
        return Duration.ofNanos(this.latestGatewayLatencyNanos);
    }

    @Override
    public CompletableFuture<Duration> measureRestLatency() {
        return CompletableFuture.supplyAsync(() -> {
            this.restLatencyLock.lock();
            try {
                RestRequest<Duration> request = new RestRequest<Duration>(this, RestMethod.GET, RestEndpoint.CURRENT_USER);
                long nanoStart = System.nanoTime();
                Duration duration = request.execute(result -> Duration.ofNanos(System.nanoTime() - nanoStart)).join();
                return duration;
            }
            finally {
                this.restLatencyLock.unlock();
            }
        }, this.getThreadPool().getExecutorService());
    }

    @Override
    public void setMessageCacheSize(int capacity, int storageTimeInSeconds) {
        this.defaultMessageCacheCapacity = capacity;
        this.defaultMessageCacheStorageTimeInSeconds = storageTimeInSeconds;
        this.getChannels().stream().filter(channel -> channel instanceof TextChannel).map(channel -> (TextChannel)channel).forEach(channel -> {
            channel.getMessageCache().setCapacity(capacity);
            channel.getMessageCache().setStorageTimeInSeconds(storageTimeInSeconds);
        });
    }

    @Override
    public int getDefaultMessageCacheCapacity() {
        return this.defaultMessageCacheCapacity;
    }

    @Override
    public int getDefaultMessageCacheStorageTimeInSeconds() {
        return this.defaultMessageCacheStorageTimeInSeconds;
    }

    @Override
    public void setAutomaticMessageCacheCleanupEnabled(boolean automaticMessageCacheCleanupEnabled) {
        this.defaultAutomaticMessageCacheCleanupEnabled = automaticMessageCacheCleanupEnabled;
        this.getChannels().stream().filter(TextChannel.class::isInstance).map(TextChannel.class::cast).forEach(channel -> channel.getMessageCache().setAutomaticCleanupEnabled(automaticMessageCacheCleanupEnabled));
    }

    @Override
    public boolean isDefaultAutomaticMessageCacheCleanupEnabled() {
        return this.defaultAutomaticMessageCacheCleanupEnabled;
    }

    @Override
    public int getCurrentShard() {
        return this.currentShard;
    }

    @Override
    public int getTotalShards() {
        return this.totalShards;
    }

    @Override
    public boolean isWaitingForServersOnStartup() {
        return this.waitForServersOnStartup;
    }

    @Override
    public boolean isWaitingForUsersOnStartup() {
        return this.waitForUsersOnStartup;
    }

    public Optional<ProxySelector> getProxySelector() {
        return Optional.ofNullable(this.proxySelector);
    }

    public Optional<Proxy> getProxy() {
        return Optional.ofNullable(this.proxy);
    }

    public Optional<Authenticator> getProxyAuthenticator() {
        return Optional.ofNullable(this.proxyAuthenticator);
    }

    public boolean isTrustAllCertificates() {
        return this.trustAllCertificates;
    }

    @Override
    public void updateStatus(UserStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("The status cannot be null");
        }
        this.status = status;
        this.websocketAdapter.updateStatus();
    }

    @Override
    public UserStatus getStatus() {
        return this.status;
    }

    private void updateActivity(ActivityType type, String name, String streamingUrl) {
        this.activity = name == null ? null : (streamingUrl == null ? new ActivityImpl(type, name, null) : new ActivityImpl(type, name, streamingUrl));
        this.websocketAdapter.updateStatus();
    }

    @Override
    public void updateActivity(String name) {
        this.updateActivity(ActivityType.PLAYING, name, null);
    }

    @Override
    public void updateActivity(ActivityType type, String name) {
        this.updateActivity(type, name, null);
    }

    @Override
    public void updateActivity(String name, String streamingUrl) {
        this.updateActivity(ActivityType.STREAMING, name, streamingUrl);
    }

    @Override
    public void unsetActivity() {
        this.updateActivity(null);
    }

    @Override
    public Optional<Activity> getActivity() {
        return Optional.ofNullable(this.activity);
    }

    @Override
    public User getYourself() {
        return this.you;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public CompletableFuture<Void> disconnect() {
        boolean doDisconnect = false;
        AtomicReference<CompletableFuture<Void>> atomicReference = this.disconnectFuture;
        synchronized (atomicReference) {
            if (this.disconnectFuture.get() == null) {
                this.disconnectFuture.set(new CompletableFuture());
                doDisconnect = true;
            }
        }
        if (doDisconnect) {
            if (this.websocketAdapter == null) {
                this.threadPool.shutdown();
                this.disconnectFuture.get().complete(null);
            } else {
                this.addLostConnectionListener(event -> {
                    this.threadPool.shutdown();
                    this.disconnectFuture.get().complete(null);
                });
                this.websocketAdapter.disconnect();
                this.threadPool.getDaemonScheduler().schedule(() -> {
                    this.threadPool.shutdown();
                    this.disconnectFuture.get().complete(null);
                }, 1L, TimeUnit.MINUTES);
            }
        }
        return this.disconnectFuture.get();
    }

    @Override
    public void setReconnectDelay(Function<Integer, Integer> reconnectDelayProvider) {
        this.reconnectDelayProvider = reconnectDelayProvider;
    }

    @Override
    public int getReconnectDelay(int attempt) {
        if (attempt < 0) {
            throw new IllegalArgumentException("attempt must be 1 or greater");
        }
        return this.reconnectDelayProvider.apply(attempt);
    }

    @Override
    public ApplicationInfo getCachedApplicationInfo() {
        return this.applicationInfo;
    }

    @Override
    public CompletableFuture<ApplicationInfo> requestApplicationInfo() {
        return new RestRequest<ApplicationInfo>(this, RestMethod.GET, RestEndpoint.SELF_INFO).execute(result -> {
            ApplicationInfoImpl applicationInfo = new ApplicationInfoImpl(this, result.getJsonBody());
            this.applicationInfo = applicationInfo;
            return applicationInfo;
        });
    }

    @Override
    public CompletableFuture<Webhook> getWebhookById(long id) {
        return new RestRequest(this, RestMethod.GET, RestEndpoint.WEBHOOK).setUrlParameters(Long.toUnsignedString(id)).execute(result -> WebhookImpl.createWebhook(this, result.getJsonBody()));
    }

    @Override
    public CompletableFuture<IncomingWebhook> getIncomingWebhookByIdAndToken(String id, String token) {
        return new RestRequest(this, RestMethod.GET, RestEndpoint.WEBHOOK).setUrlParameters(id, token).execute(result -> new IncomingWebhookImpl(this, result.getJsonBody()));
    }

    @Override
    public Set<Long> getUnavailableServers() {
        return Collections.unmodifiableSet(this.unavailableServers);
    }

    @Override
    public CompletableFuture<Invite> getInviteByCode(String code) {
        return new RestRequest(this, RestMethod.GET, RestEndpoint.INVITE).setUrlParameters(code).addQueryParameter("with_counts", "false").execute(result -> new InviteImpl(this, result.getJsonBody()));
    }

    @Override
    public CompletableFuture<Invite> getInviteWithMemberCountsByCode(String code) {
        return new RestRequest(this, RestMethod.GET, RestEndpoint.INVITE).setUrlParameters(code).addQueryParameter("with_counts", "true").execute(result -> new InviteImpl(this, result.getJsonBody()));
    }

    @Override
    public boolean isUserCacheEnabled() {
        return this.userCacheEnabled;
    }

    @Override
    public Set<User> getCachedUsers() {
        return this.getEntityCache().get().getMemberCache().getUserCache().getUsers();
    }

    @Override
    public Optional<User> getCachedUserById(long id) {
        return this.getEntityCache().get().getMemberCache().getUserCache().getUserById(id);
    }

    @Override
    public CompletableFuture<User> getUserById(long id) {
        return this.getCachedUserById(id).map(CompletableFuture::completedFuture).orElseGet(() -> new RestRequest(this, RestMethod.GET, RestEndpoint.USER).setUrlParameters(Long.toUnsignedString(id)).execute(result -> new UserImpl(this, result.getJsonBody(), (MemberImpl)null, null)));
    }

    @Override
    public MessageSet getCachedMessages() {
        this.messageCacheLock.lock();
        try {
            MessageSetImpl messageSetImpl = new MessageSetImpl(this.messages.values().stream().map(Reference::get).filter(Objects::nonNull).collect(Collectors.toList()));
            return messageSetImpl;
        }
        finally {
            this.messageCacheLock.unlock();
        }
    }

    public MessageSet getCachedMessagesWhere(Predicate<Message> filter) {
        this.messageCacheLock.lock();
        try {
            MessageSetImpl messageSetImpl = new MessageSetImpl(this.messages.values().stream().map(Reference::get).filter(Objects::nonNull).filter(filter).collect(Collectors.toList()));
            return messageSetImpl;
        }
        finally {
            this.messageCacheLock.unlock();
        }
    }

    public void forEachCachedMessageWhere(Predicate<Message> filter, Consumer<Message> action) {
        this.messageCacheLock.lock();
        try {
            this.messages.values().stream().map(Reference::get).filter(Objects::nonNull).filter(filter).forEach(action);
        }
        finally {
            this.messageCacheLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Optional<Message> getCachedMessageById(long id) {
        this.messageCacheLock.lock();
        try {
            Optional<Message> optional = Optional.ofNullable(this.messages.get(id)).map(Reference::get);
            return optional;
        }
        finally {
            this.messageCacheLock.unlock();
        }
    }

    @Override
    public Set<Server> getServers() {
        return Collections.unmodifiableSet(new HashSet<Server>(this.servers.values()));
    }

    @Override
    public Optional<Server> getServerById(long id) {
        return Optional.ofNullable(this.servers.get(id));
    }

    @Override
    public Set<KnownCustomEmoji> getCustomEmojis() {
        return Collections.unmodifiableSet(new HashSet<KnownCustomEmoji>(this.customEmojis.values()));
    }

    @Override
    public Optional<KnownCustomEmoji> getCustomEmojiById(long id) {
        return Optional.ofNullable(this.customEmojis.get(id));
    }

    @Override
    public CompletableFuture<Set<StickerPack>> getNitroStickerPacks() {
        return new RestRequest<Set>(this, RestMethod.GET, RestEndpoint.STICKER_PACK).execute(result -> {
            HashSet<StickerPackImpl> stickerPacks = new HashSet<StickerPackImpl>();
            for (JsonNode stickerPackJson : result.getJsonBody().get("sticker_packs")) {
                stickerPacks.add(new StickerPackImpl(this, stickerPackJson));
            }
            return stickerPacks;
        });
    }

    @Override
    public Set<Channel> getChannels() {
        return this.entityCache.get().getChannelCache().getChannels();
    }

    @Override
    public Set<PrivateChannel> getPrivateChannels() {
        return this.entityCache.get().getChannelCache().getChannelsWithTypes(ChannelType.PRIVATE_CHANNEL);
    }

    @Override
    public Set<ServerChannel> getServerChannels() {
        return this.entityCache.get().getChannelCache().getChannelsWithTypes(ChannelType.getServerChannelTypes());
    }

    @Override
    public Set<RegularServerChannel> getRegularServerChannels() {
        return this.entityCache.get().getChannelCache().getChannelsWithTypes(ChannelType.getRegularServerChannelTypes());
    }

    @Override
    public Set<ChannelCategory> getChannelCategories() {
        return this.entityCache.get().getChannelCache().getChannelsWithTypes(ChannelType.CHANNEL_CATEGORY);
    }

    @Override
    public Set<ServerTextChannel> getServerTextChannels() {
        return this.entityCache.get().getChannelCache().getChannelsWithTypes(ChannelType.SERVER_TEXT_CHANNEL);
    }

    @Override
    public Set<ServerForumChannel> getServerForumChannels() {
        return this.entityCache.get().getChannelCache().getChannelsWithTypes(ChannelType.SERVER_FORUM_CHANNEL);
    }

    @Override
    public Set<ServerThreadChannel> getServerThreadChannels() {
        return this.entityCache.get().getChannelCache().getChannelsWithTypes(ChannelType.SERVER_PRIVATE_THREAD, ChannelType.SERVER_PUBLIC_THREAD, ChannelType.SERVER_NEWS_THREAD);
    }

    @Override
    public Set<ServerThreadChannel> getPrivateServerThreadChannels() {
        return this.entityCache.get().getChannelCache().getChannelsWithTypes(ChannelType.SERVER_PRIVATE_THREAD);
    }

    @Override
    public Set<ServerThreadChannel> getPublicServerThreadChannels() {
        return this.entityCache.get().getChannelCache().getChannelsWithTypes(ChannelType.SERVER_PUBLIC_THREAD);
    }

    @Override
    public Set<ServerVoiceChannel> getServerVoiceChannels() {
        return this.entityCache.get().getChannelCache().getChannelsWithTypes(ChannelType.SERVER_VOICE_CHANNEL);
    }

    @Override
    public Set<ServerStageVoiceChannel> getServerStageVoiceChannels() {
        return this.entityCache.get().getChannelCache().getChannelsWithTypes(ChannelType.SERVER_STAGE_VOICE_CHANNEL);
    }

    @Override
    public Set<TextChannel> getTextChannels() {
        return this.entityCache.get().getChannelCache().getChannelsWithTypes(ChannelType.getTextChannelTypes());
    }

    @Override
    public Set<VoiceChannel> getVoiceChannels() {
        return this.entityCache.get().getChannelCache().getChannelsWithTypes(ChannelType.getVoiceChannelTypes());
    }

    @Override
    public Optional<Channel> getChannelById(long id) {
        return this.entityCache.get().getChannelCache().getChannelById(id);
    }

    public ReentrantLock getMessageCacheLock() {
        return this.messageCacheLock;
    }

    @Override
    public Collection<ListenerManager<? extends GloballyAttachableListener>> addListener(GloballyAttachableListener listener) {
        return ClassHelper.getInterfacesAsStream(listener.getClass()).filter(GloballyAttachableListener.class::isAssignableFrom).filter(listenerClass -> listenerClass != GloballyAttachableListener.class).map(listenerClass -> listenerClass).map(listenerClass -> this.addListener((Class)listenerClass, (GloballyAttachableListener)listener)).collect(Collectors.toList());
    }

    @Override
    public <T extends GloballyAttachableListener> ListenerManager<T> addListener(Class<T> listenerClass, T listener) {
        return this.listeners.computeIfAbsent(listenerClass, key -> Collections.synchronizedMap(new LinkedHashMap())).computeIfAbsent(listener, key -> new ListenerManagerImpl<GloballyAttachableListener>(this, listener, listenerClass));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public <T extends GloballyAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        Map<Class<? extends GloballyAttachableListener>, Map<GloballyAttachableListener, ListenerManagerImpl<? extends GloballyAttachableListener>>> map = this.listeners;
        synchronized (map) {
            Map<GloballyAttachableListener, ListenerManagerImpl<? extends GloballyAttachableListener>> classListeners = this.listeners.get(listenerClass);
            if (classListeners == null) {
                return;
            }
            ListenerManagerImpl<? extends GloballyAttachableListener> listenerManager = classListeners.get(listener);
            if (listenerManager == null) {
                return;
            }
            classListeners.remove(listener);
            listenerManager.removed();
            if (classListeners.isEmpty()) {
                this.listeners.remove(listenerClass);
            }
        }
    }

    @Override
    public void removeListener(GloballyAttachableListener listener) {
        ClassHelper.getInterfacesAsStream(listener.getClass()).filter(GloballyAttachableListener.class::isAssignableFrom).filter(listenerClass -> listenerClass != GloballyAttachableListener.class).map(listenerClass -> listenerClass).forEach(listenerClass -> this.removeListener((Class)listenerClass, (GloballyAttachableListener)listener));
    }

    protected void finalize() throws Throwable {
        this.disconnect();
        super.finalize();
    }
}

