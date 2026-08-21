/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.gateway;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neovisionaries.ws.client.ProxySettings;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import java.lang.invoke.LambdaMetafactory;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.zip.DataFormatException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Supplier;
import org.javacord.api.entity.Nameable;
import org.javacord.api.entity.activity.Activity;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.auth.Request;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.event.connection.LostConnectionEventImpl;
import org.javacord.core.event.connection.ReconnectEventImpl;
import org.javacord.core.event.connection.ResumeEventImpl;
import org.javacord.core.util.auth.NvWebSocketResponseImpl;
import org.javacord.core.util.auth.NvWebSocketRouteImpl;
import org.javacord.core.util.gateway.BinaryMessageDecompressor;
import org.javacord.core.util.gateway.GatewayOpcode;
import org.javacord.core.util.gateway.Heart;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.gateway.WebSocketCloseCode;
import org.javacord.core.util.gateway.WebSocketCloseReason;
import org.javacord.core.util.gateway.WebSocketFrameSendingQueueEntry;
import org.javacord.core.util.handler.ReadyHandler;
import org.javacord.core.util.handler.ResumedHandler;
import org.javacord.core.util.handler.channel.ChannelCreateHandler;
import org.javacord.core.util.handler.channel.ChannelDeleteHandler;
import org.javacord.core.util.handler.channel.ChannelPinsUpdateHandler;
import org.javacord.core.util.handler.channel.ChannelUpdateHandler;
import org.javacord.core.util.handler.channel.WebhooksUpdateHandler;
import org.javacord.core.util.handler.channel.invite.InviteCreateHandler;
import org.javacord.core.util.handler.channel.invite.InviteDeleteHandler;
import org.javacord.core.util.handler.channel.thread.ThreadCreateHandler;
import org.javacord.core.util.handler.channel.thread.ThreadDeleteHandler;
import org.javacord.core.util.handler.channel.thread.ThreadListSyncHandler;
import org.javacord.core.util.handler.channel.thread.ThreadMembersUpdateHandler;
import org.javacord.core.util.handler.channel.thread.ThreadUpdateHandler;
import org.javacord.core.util.handler.guild.ApplicationCommandPermissionsUpdateHandler;
import org.javacord.core.util.handler.guild.GuildBanAddHandler;
import org.javacord.core.util.handler.guild.GuildBanRemoveHandler;
import org.javacord.core.util.handler.guild.GuildCreateHandler;
import org.javacord.core.util.handler.guild.GuildDeleteHandler;
import org.javacord.core.util.handler.guild.GuildEmojisUpdateHandler;
import org.javacord.core.util.handler.guild.GuildMemberAddHandler;
import org.javacord.core.util.handler.guild.GuildMemberRemoveHandler;
import org.javacord.core.util.handler.guild.GuildMemberUpdateHandler;
import org.javacord.core.util.handler.guild.GuildMembersChunkHandler;
import org.javacord.core.util.handler.guild.GuildStickersUpdateHandler;
import org.javacord.core.util.handler.guild.GuildUpdateHandler;
import org.javacord.core.util.handler.guild.VoiceServerUpdateHandler;
import org.javacord.core.util.handler.guild.VoiceStateUpdateHandler;
import org.javacord.core.util.handler.guild.role.GuildRoleCreateHandler;
import org.javacord.core.util.handler.guild.role.GuildRoleDeleteHandler;
import org.javacord.core.util.handler.guild.role.GuildRoleUpdateHandler;
import org.javacord.core.util.handler.interaction.InteractionCreateHandler;
import org.javacord.core.util.handler.message.MessageCreateHandler;
import org.javacord.core.util.handler.message.MessageDeleteBulkHandler;
import org.javacord.core.util.handler.message.MessageDeleteHandler;
import org.javacord.core.util.handler.message.MessageUpdateHandler;
import org.javacord.core.util.handler.message.reaction.MessageReactionAddHandler;
import org.javacord.core.util.handler.message.reaction.MessageReactionRemoveAllHandler;
import org.javacord.core.util.handler.message.reaction.MessageReactionRemoveHandler;
import org.javacord.core.util.handler.user.PresenceUpdateHandler;
import org.javacord.core.util.handler.user.PresencesReplaceHandler;
import org.javacord.core.util.handler.user.TypingStartHandler;
import org.javacord.core.util.handler.user.UserUpdateHandler;
import org.javacord.core.util.http.TrustAllTrustManager;
import org.javacord.core.util.logging.LoggerUtil;
import org.javacord.core.util.logging.WebSocketLogger;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class DiscordWebSocketAdapter
extends WebSocketAdapter {
    private static final Logger logger = LoggerUtil.getLogger(DiscordWebSocketAdapter.class);
    private static String gateway;
    private static final ReadWriteLock gatewayLock;
    private static final Lock gatewayReadLock;
    private static final Lock gatewayWriteLock;
    private static final long WEB_SOCKET_FRAME_SENDING_RATELIMIT_DURATION;
    private static final long ONE_SECOND;
    private static final int WEB_SOCKET_FRAME_SENDING_RATELIMIT = 120;
    private final DiscordApiImpl api;
    private final HashMap<String, PacketHandler> handlers = new HashMap();
    private final CompletableFuture<Boolean> ready = new CompletableFuture();
    private final AtomicReference<WebSocket> websocket = new AtomicReference();
    private final Heart heart;
    private volatile int lastSeq = -1;
    private volatile String sessionId = null;
    private volatile String resumeUrl = null;
    private volatile boolean reconnect;
    private final Lock reconnectingOrResumingLock = new ReentrantLock();
    private final Condition finishedReconnectingOrResumingCondition = this.reconnectingOrResumingLock.newCondition();
    private final AtomicMarkableReference<WebSocketFrame> lastSentFrameWasIdentify = new AtomicMarkableReference<Object>(null, false);
    private final AtomicReference<WebSocketFrame> nextHeartbeatFrame = new AtomicReference<Object>(null);
    private final List<WebSocketListener> identifyFrameListeners = Collections.synchronizedList(new ArrayList());
    private volatile boolean triedToResume = false;
    private volatile long lastGuildMembersChunkReceived = System.currentTimeMillis();
    private final AtomicInteger reconnectAttempt = new AtomicInteger();
    private final BlockingQueue<Long> requestGuildMembersQueue = new LinkedBlockingQueue<Long>();
    private BlockingQueue<WebSocketFrameSendingQueueEntry> webSocketFrameSendingQueue = new PriorityBlockingQueue<WebSocketFrameSendingQueueEntry>();
    private AtomicReference<Thread> webSocketFrameSenderThread = new AtomicReference();
    private AtomicInteger webSocketFrameSendingLimit = new AtomicInteger(120);

    public DiscordWebSocketAdapter(DiscordApiImpl api) {
        this(api, true);
    }

    DiscordWebSocketAdapter(DiscordApiImpl api, boolean reconnect) {
        this.api = api;
        this.reconnect = reconnect;
        this.heart = new Heart(api, heartbeatFrame -> this.sendFrame(this.websocket.get(), (WebSocketFrame)heartbeatFrame, true, true), (code, reason) -> this.sendCloseFrame(this.websocket.get(), (int)code, (String)reason), false);
        this.registerHandlers();
        this.connect();
        ExecutorService requestGuildMembersQueueConsumer = api.getThreadPool().getSingleDaemonThreadExecutorService("Request Server Members Queue Consumer");
        requestGuildMembersQueueConsumer.submit(() -> {
            while (!requestGuildMembersQueueConsumer.isShutdown()) {
                try {
                    Long nextServerId = this.requestGuildMembersQueue.poll(1L, TimeUnit.MINUTES);
                    if (nextServerId == null) continue;
                    this.requestGuildMembersQueue.add(nextServerId);
                    this.requestGuildMembersQueue.stream().distinct().forEach(serverId -> {
                        this.requestGuildMembersQueue.remove(serverId);
                        ObjectNode requestGuildMembersPacket = JsonNodeFactory.instance.objectNode().put("op", GatewayOpcode.REQUEST_GUILD_MEMBERS.getCode());
                        ObjectNode data = requestGuildMembersPacket.putObject("d").put("query", "").put("limit", 0);
                        data.put("guild_id", Long.toUnsignedString(serverId));
                        logger.debug("Sending request guild members packet {}", (Object)requestGuildMembersPacket);
                        this.sendTextFrame(requestGuildMembersPacket.toString());
                    });
                    Thread.sleep(1000L);
                }
                catch (InterruptedException nextServerId) {
                }
                catch (Throwable t) {
                    logger.error("Failed to process request guild members queue!", t);
                }
            }
        });
        ExecutorService webSocketFrameSenderService = api.getThreadPool().getSingleDaemonThreadExecutorService("Web Socket Frame Sender");
        webSocketFrameSenderService.submit(() -> {
            this.webSocketFrameSenderThread.set(Thread.currentThread());
            WeakHashMap<WebSocket, List> sendTimeLists = new WeakHashMap<WebSocket, List>();
            while (!webSocketFrameSenderService.isShutdown()) {
                WebSocketFrameSendingQueueEntry webSocketFrameSendingQueueEntry = null;
                try {
                    int webSocketFrameSendingLimit;
                    webSocketFrameSendingQueueEntry = this.webSocketFrameSendingQueue.poll(1L, TimeUnit.MINUTES);
                    if (webSocketFrameSendingQueueEntry == null) continue;
                    WebSocket webSocket = webSocketFrameSendingQueueEntry.getWebSocket().orElseGet(this.websocket::get);
                    List sendTimeList = sendTimeLists.computeIfAbsent(webSocket, key -> new ArrayList(120));
                    long currentNanoTime = System.nanoTime();
                    if (!sendTimeList.isEmpty() && currentNanoTime - (Long)sendTimeList.get(0) > WEB_SOCKET_FRAME_SENDING_RATELIMIT_DURATION) {
                        sendTimeList.clear();
                    }
                    int n = webSocketFrameSendingLimit = webSocketFrameSendingQueueEntry.isPriorityLifecycle() ? 120 : this.webSocketFrameSendingLimit.get();
                    if (sendTimeList.size() >= webSocketFrameSendingLimit) {
                        long waitDuration = WEB_SOCKET_FRAME_SENDING_RATELIMIT_DURATION - (currentNanoTime - (Long)sendTimeList.get(0));
                        if (waitDuration <= 0L) continue;
                        logger.debug("Waiting {}ns for web socket frame sending cool down", (Object)(waitDuration += ONE_SECOND));
                        TimeUnit.NANOSECONDS.sleep(waitDuration);
                        continue;
                    }
                    if (this.reconnectAttempt.intValue() > 0 && !webSocketFrameSendingQueueEntry.isLifecycle()) {
                        this.reconnectingOrResumingLock.lock();
                        try {
                            this.finishedReconnectingOrResumingCondition.await(1L, TimeUnit.SECONDS);
                            continue;
                        }
                        finally {
                            this.reconnectingOrResumingLock.unlock();
                            continue;
                        }
                    }
                    sendTimeList.add(currentNanoTime);
                    WebSocketFrame frame = webSocketFrameSendingQueueEntry.getFrame();
                    logger.debug("Sending {}frame {}", (Object)(webSocketFrameSendingQueueEntry.isPriorityLifecycle() ? "priority lifecycle " : ""), (Object)frame);
                    webSocket.sendFrame(frame);
                    webSocketFrameSendingQueueEntry = null;
                    continue;
                }
                catch (InterruptedException webSocket) {
                    continue;
                }
                catch (Throwable t) {
                    logger.error("Failed to process web socket frame sending queue!", t);
                    continue;
                }
                finally {
                    if (webSocketFrameSendingQueueEntry == null) continue;
                    this.webSocketFrameSendingQueue.add(webSocketFrameSendingQueueEntry);
                    continue;
                }
                break;
            }
            return;
        });
    }

    private static String getGateway(DiscordApiImpl api) {
        gatewayReadLock.lock();
        if (gateway == null) {
            gatewayReadLock.unlock();
            gatewayWriteLock.lock();
            try {
                if (gateway == null) {
                    gateway = new RestRequest(api, RestMethod.GET, RestEndpoint.GATEWAY).includeAuthorizationHeader(false).execute(result -> result.getJsonBody().get("url").asText()).join();
                }
                gatewayReadLock.lock();
            }
            finally {
                gatewayWriteLock.unlock();
            }
        }
        try {
            String string = gateway;
            return string;
        }
        finally {
            gatewayReadLock.unlock();
        }
    }

    public static void setGateway(String gateway) {
        gatewayWriteLock.lock();
        try {
            DiscordWebSocketAdapter.gateway = gateway;
        }
        finally {
            gatewayWriteLock.unlock();
        }
    }

    public void disconnect() {
        this.reconnect = false;
        this.sendCloseFrame(WebSocketCloseReason.DISCONNECT.getNumericCloseCode());
        this.api.getThreadPool().getDaemonScheduler().schedule(this.heart::squash, 1L, TimeUnit.MINUTES);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void connect() {
        block13: {
            try {
                WebSocketFactory factory2 = new WebSocketFactory();
                String webSocketUri = (this.resumeUrl != null ? this.resumeUrl : DiscordWebSocketAdapter.getGateway(this.api)) + "?encoding=json&v=" + "10";
                Proxy proxy = this.api.getProxy().orElseGet(() -> {
                    List<Proxy> proxies = this.api.getProxySelector().orElseGet(ProxySelector::getDefault).select(URI.create(webSocketUri.replace("wss://", "https://").replace("ws://", "http://")));
                    return proxies.stream().filter(p -> p.type() == Proxy.Type.DIRECT).findAny().orElseGet(() -> proxies.stream().filter(p -> p.type() == Proxy.Type.HTTP).findAny().orElseGet(() -> (Proxy)proxies.get(0)));
                });
                switch (proxy.type()) {
                    case DIRECT: {
                        break;
                    }
                    case HTTP: {
                        SocketAddress proxyAddress = proxy.address();
                        if (!(proxyAddress instanceof InetSocketAddress)) {
                            throw new WebSocketException(null, "HTTP proxies without an InetSocketAddress are not supported currently");
                        }
                        InetSocketAddress proxyInetAddress = (InetSocketAddress)proxyAddress;
                        String proxyHost = proxyInetAddress.getHostString();
                        int proxyPort = proxyInetAddress.getPort();
                        ProxySettings proxySettings = factory2.getProxySettings();
                        proxySettings.setHost(proxyHost).setPort(proxyPort);
                        Optional<org.javacord.api.util.auth.Authenticator> proxyAuthenticator = this.api.getProxyAuthenticator();
                        URL webSocketUrl = URI.create(webSocketUri.replace("wss://", "https://").replace("ws://", "http://")).toURL();
                        if (proxyAuthenticator.isPresent()) {
                            Map<String, List<String>> requestHeaders = proxyAuthenticator.get().authenticate(new NvWebSocketRouteImpl(webSocketUrl, proxy, proxyInetAddress), new Request(){}, new NvWebSocketResponseImpl());
                            if (requestHeaders == null) break;
                            requestHeaders.forEach((headerName, headerValues) -> {
                                if (headerValues == null) {
                                    proxySettings.getHeaders().remove(headerName);
                                    return;
                                }
                                if (headerValues.isEmpty()) {
                                    return;
                                }
                                String firstHeaderValue = (String)headerValues.get(0);
                                if (firstHeaderValue == null) {
                                    proxySettings.getHeaders().remove(headerName);
                                } else {
                                    proxySettings.addHeader((String)headerName, firstHeaderValue);
                                }
                                headerValues.stream().skip(1L).forEach(headerValue -> proxySettings.addHeader((String)headerName, (String)headerValue));
                            });
                            break;
                        }
                        PasswordAuthentication credentials = Authenticator.requestPasswordAuthentication(proxyHost, proxyInetAddress.getAddress(), proxyPort, webSocketUrl.getProtocol(), null, "Basic", webSocketUrl, Authenticator.RequestorType.PROXY);
                        if (credentials == null) break;
                        proxySettings.setId(credentials.getUserName()).setPassword(String.valueOf(credentials.getPassword()));
                        break;
                    }
                    default: {
                        throw new WebSocketException(null, "Proxies of type '" + (Object)((Object)proxy.type()) + "' are not supported currently");
                    }
                }
                if (this.api.isTrustAllCertificates()) {
                    factory2.setSSLSocketFactory(new TrustAllTrustManager().createSslSocketFactory());
                }
                WebSocket websocket = factory2.createSocket(webSocketUri);
                this.websocket.set(websocket);
                websocket.addHeader("Accept-Encoding", "gzip");
                websocket.addListener(this);
                websocket.addListener(new WebSocketLogger());
                if (this.sessionId == null) {
                    this.api.getGatewayIdentifyRatelimiter().requestQuota();
                }
                this.triedToResume = false;
                websocket.connect();
            }
            catch (Throwable t) {
                this.resumeUrl = null;
                logger.warn("An error occurred while connecting to websocket", t);
                if (!this.reconnect) break block13;
                this.reconnectingOrResumingLock.lock();
                try {
                    this.reconnectAttempt.incrementAndGet();
                }
                finally {
                    this.reconnectingOrResumingLock.unlock();
                }
                logger.info("Trying to reconnect/resume in {} seconds!", (Object)this.api.getReconnectDelay(this.reconnectAttempt.get()));
                this.api.getThreadPool().getScheduler().schedule(() -> {
                    gatewayWriteLock.lock();
                    try {
                        gateway = null;
                    }
                    finally {
                        gatewayWriteLock.unlock();
                    }
                    this.connect();
                }, (long)this.api.getReconnectDelay(this.reconnectAttempt.get()), TimeUnit.SECONDS);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
        Optional<WebSocketFrame> closeFrameOptional = Optional.ofNullable(closedByServer ? serverCloseFrame : clientCloseFrame);
        String closeReason = closeFrameOptional.map(WebSocketFrame::getCloseReason).orElse("unknown");
        String closeCodeString = closeFrameOptional.map(closeFrame -> {
            int code = closeFrame.getCloseCode();
            return WebSocketCloseCode.fromCode(code).map(closeCode -> (Object)closeCode + " (" + code + ")").orElseGet(() -> String.valueOf(code));
        }).orElse("'unknown'");
        logger.info("Websocket closed with reason '{}' and code {} by {}!", (Object)closeReason, (Object)closeCodeString, (Object)(closedByServer ? "server" : "client"));
        LostConnectionEventImpl lostConnectionEvent = new LostConnectionEventImpl(this.api);
        this.api.getEventDispatcher().dispatchLostConnectionEvent(null, lostConnectionEvent);
        this.heart.squash();
        if (!this.ready.isDone() && closeFrameOptional.map(closeFrame -> closeFrame.getCloseCode() != WebSocketCloseReason.INVALID_SESSION_RECONNECT.getNumericCloseCode()).orElse(true).booleanValue()) {
            this.ready.complete(false);
            return;
        }
        if (this.reconnect) {
            this.reconnectingOrResumingLock.lock();
            try {
                this.reconnectAttempt.incrementAndGet();
            }
            finally {
                this.reconnectingOrResumingLock.unlock();
            }
            logger.info("Trying to reconnect/resume in {} seconds!", (Object)this.api.getReconnectDelay(this.reconnectAttempt.get()));
            this.api.getThreadPool().getScheduler().schedule(this::connect, (long)this.api.getReconnectDelay(this.reconnectAttempt.get()), TimeUnit.SECONDS);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        mapper = this.api.getObjectMapper();
        packet = mapper.readTree(text);
        this.heart.handlePacket(packet);
        op = packet.get("op").asInt();
        opcode = GatewayOpcode.fromCode(op);
        if (!opcode.isPresent()) {
            DiscordWebSocketAdapter.logger.debug("Received unknown packet (op: {}, content: {})", (Object)op, (Object)packet);
            return;
        }
        switch (3.$SwitchMap$org$javacord$core$util$gateway$GatewayOpcode[opcode.get().ordinal()]) {
            case 1: {
                this.lastSeq = packet.get("s").asInt();
                type = packet.get("t").asText();
                handler = this.handlers.get(type);
                if (handler != null) {
                    handler.handlePacket(packet.get("d"));
                } else {
                    DiscordWebSocketAdapter.logger.debug("Received unknown packet of type {} (packet: {})", (Object)type, (Object)packet);
                }
                if (type.equals("GUILD_MEMBERS_CHUNK")) {
                    this.lastGuildMembersChunkReceived = System.currentTimeMillis();
                }
                if (!type.equals("RESUMED")) ** GOTO lbl33
                this.reconnectingOrResumingLock.lock();
                try {
                    this.triedToResume = false;
                    this.reconnectAttempt.set(0);
                    this.finishedReconnectingOrResumingCondition.signalAll();
                }
                finally {
                    this.reconnectingOrResumingLock.unlock();
                }
                DiscordWebSocketAdapter.logger.debug("Received RESUMED packet");
                resumeEvent = new ResumeEventImpl(this.api);
                this.api.getEventDispatcher().dispatchResumeEvent(null, resumeEvent);
lbl33:
                // 2 sources

                if (!type.equals("READY")) break;
                this.reconnectingOrResumingLock.lock();
                try {
                    this.triedToResume = false;
                    this.reconnectAttempt.set(0);
                    this.finishedReconnectingOrResumingCondition.signalAll();
                }
                finally {
                    this.reconnectingOrResumingLock.unlock();
                }
                this.sessionId = packet.get("d").get("session_id").asText();
                this.resumeUrl = packet.get("d").hasNonNull("resume_gateway_url") != false ? packet.get("d").get("resume_gateway_url").asText() : null;
                this.api.getThreadPool().getSingleThreadExecutorService("Startup Servers Wait Thread").submit((Runnable)LambdaMetafactory.metafactory(null, null, null, ()V, lambda$onTextMessage$20(), ()V)((DiscordWebSocketAdapter)this));
                DiscordWebSocketAdapter.logger.debug("Received READY packet");
                break;
            }
            case 2: {
                this.heart.beat();
                break;
            }
            case 3: {
                this.sendCloseFrame(websocket, WebSocketCloseReason.COMMANDED_RECONNECT.getNumericCloseCode(), WebSocketCloseReason.COMMANDED_RECONNECT.getCloseReason());
                break;
            }
            case 4: {
                this.sessionId = null;
                this.resumeUrl = null;
                if (this.lastSentFrameWasIdentify.isMarked()) {
                    DiscordWebSocketAdapter.logger.info("Hit identifying rate limit. Reconnecting...");
                } else if (this.triedToResume) {
                    oneToFiveSeconds = 1000 + (int)(Math.random() * 4000.0);
                    DiscordWebSocketAdapter.logger.info("Could not resume session. Reconnecting in {}.{} seconds...", new Supplier[]{(Supplier<Object>)LambdaMetafactory.metafactory(null, null, null, ()Ljava/lang/Object;, lambda$onTextMessage$21(int ), ()Ljava/lang/Object;)((int)oneToFiveSeconds), (Supplier<Object>)LambdaMetafactory.metafactory(null, null, null, ()Ljava/lang/Object;, lambda$onTextMessage$22(int ), ()Ljava/lang/Object;)((int)oneToFiveSeconds)});
                    try {
                        Thread.sleep(oneToFiveSeconds);
                    }
                    catch (InterruptedException e) {
                        DiscordWebSocketAdapter.logger.error("Interrupted while delaying reconnect!");
                        return;
                    }
                }
                this.sendCloseFrame(websocket, WebSocketCloseReason.INVALID_SESSION_RECONNECT.getNumericCloseCode(), WebSocketCloseReason.INVALID_SESSION_RECONNECT.getCloseReason());
                break;
            }
            case 5: {
                DiscordWebSocketAdapter.logger.debug("Received HELLO packet");
                data = packet.get("d");
                heartbeatInterval = data.get("heartbeat_interval").asInt();
                this.webSocketFrameSendingLimit.set(119 - 60000 / heartbeatInterval);
                this.heart.startBeating(heartbeatInterval);
                if (this.sessionId == null) {
                    this.sendIdentify(websocket);
                    break;
                }
                this.sendResume(websocket);
                break;
            }
            case 6: {
                break;
            }
            default: {
                DiscordWebSocketAdapter.logger.debug("Received unknown packet (op: {}, content: {})", (Object)op, (Object)packet);
            }
        }
    }

    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
        String message;
        try {
            message = BinaryMessageDecompressor.decompress(binary);
        }
        catch (DataFormatException e) {
            logger.warn("An error occurred while decompressing data", (Throwable)e);
            return;
        }
        logger.trace("onTextMessage: text='{}'", (Object)message);
        this.onTextMessage(websocket, message);
    }

    private void sendResume(WebSocket websocket) {
        ObjectNode resumePacket = JsonNodeFactory.instance.objectNode().put("op", GatewayOpcode.RESUME.getCode());
        resumePacket.putObject("d").put("token", this.api.getPrefixedToken()).put("session_id", this.sessionId).put("seq", this.lastSeq);
        logger.debug("Sending resume packet");
        this.triedToResume = true;
        this.sendLifecycleTextFrame(websocket, resumePacket.toString());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void sendIdentify(WebSocket websocket) {
        ObjectNode identifyPacket = JsonNodeFactory.instance.objectNode().put("op", GatewayOpcode.IDENTIFY.getCode());
        ObjectNode data = identifyPacket.putObject("d");
        String token = this.api.getPrefixedToken();
        data.put("token", token).put("compress", true).put("large_threshold", 250).putObject("properties").put("os", System.getProperty("os.name")).put("browser", "Javacord").put("device", "Javacord");
        data.put("intents", Intent.calculateBitmask(this.api.getIntents().toArray(new Intent[0])));
        if (this.api.getTotalShards() > 1) {
            data.putArray("shard").add(this.api.getCurrentShard()).add(this.api.getTotalShards());
        }
        List<WebSocketListener> list = this.identifyFrameListeners;
        synchronized (list) {
            websocket.removeListeners(this.identifyFrameListeners);
            this.identifyFrameListeners.clear();
        }
        WebSocketFrame identifyFrame = WebSocketFrame.createTextFrame(identifyPacket.toString());
        this.lastSentFrameWasIdentify.set(identifyFrame, false);
        WebSocketAdapter identifyFrameListener = new WebSocketAdapter(){

            @Override
            public void onFrameSent(WebSocket websocket, WebSocketFrame frame) {
                if (DiscordWebSocketAdapter.this.lastSentFrameWasIdentify.isMarked()) {
                    if (!DiscordWebSocketAdapter.this.nextHeartbeatFrame.compareAndSet(frame, null)) {
                        DiscordWebSocketAdapter.this.lastSentFrameWasIdentify.set(null, false);
                        websocket.removeListener(this);
                        DiscordWebSocketAdapter.this.identifyFrameListeners.remove(this);
                    }
                } else {
                    DiscordWebSocketAdapter.this.lastSentFrameWasIdentify.compareAndSet(frame, null, false, true);
                }
            }
        };
        this.identifyFrameListeners.add(identifyFrameListener);
        websocket.addListener(identifyFrameListener);
        logger.debug("Sending identify packet");
        this.sendLifecycleFrame(websocket, identifyFrame);
    }

    public void sendVoiceStateUpdate(Server server, ServerVoiceChannel channel, Boolean selfMuted, Boolean selfDeafened) {
        ObjectNode updateVoiceStatePacket = JsonNodeFactory.instance.objectNode().put("op", GatewayOpcode.VOICE_STATE_UPDATE.getCode());
        if (server == null) {
            if (channel == null) {
                throw new IllegalArgumentException("Either server or channel must be given");
            }
            server = channel.getServer();
        }
        User yourself = this.api.getYourself();
        updateVoiceStatePacket.putObject("d").put("guild_id", server.getIdAsString()).put("channel_id", channel == null ? null : channel.getIdAsString()).put("self_mute", selfMuted == null ? server.isSelfMuted(yourself) : selfMuted.booleanValue()).put("self_deaf", selfDeafened == null ? server.isSelfDeafened(yourself) : selfDeafened.booleanValue());
        logger.debug("Sending VOICE_STATE_UPDATE packet for {} on {}", (Object)channel, (Object)server);
        this.sendTextFrame(updateVoiceStatePacket.toString());
    }

    private void registerHandlers() {
        this.addHandler(new ReadyHandler(this.api));
        this.addHandler(new ResumedHandler(this.api));
        this.addHandler(new GuildBanAddHandler(this.api));
        this.addHandler(new GuildBanRemoveHandler(this.api));
        this.addHandler(new GuildCreateHandler(this.api));
        this.addHandler(new GuildDeleteHandler(this.api));
        this.addHandler(new GuildMembersChunkHandler(this.api));
        this.addHandler(new GuildMemberAddHandler(this.api));
        this.addHandler(new GuildMemberRemoveHandler(this.api));
        this.addHandler(new GuildMemberUpdateHandler(this.api));
        this.addHandler(new GuildUpdateHandler(this.api));
        this.addHandler(new VoiceServerUpdateHandler(this.api));
        this.addHandler(new VoiceStateUpdateHandler(this.api));
        this.addHandler(new ApplicationCommandPermissionsUpdateHandler(this.api));
        this.addHandler(new GuildRoleCreateHandler(this.api));
        this.addHandler(new GuildRoleDeleteHandler(this.api));
        this.addHandler(new GuildRoleUpdateHandler(this.api));
        this.addHandler(new GuildEmojisUpdateHandler(this.api));
        this.addHandler(new GuildStickersUpdateHandler(this.api));
        this.addHandler(new ChannelCreateHandler(this.api));
        this.addHandler(new ChannelDeleteHandler(this.api));
        this.addHandler(new ChannelPinsUpdateHandler(this.api));
        this.addHandler(new ChannelUpdateHandler(this.api));
        this.addHandler(new WebhooksUpdateHandler(this.api));
        this.addHandler(new ThreadCreateHandler(this.api));
        this.addHandler(new ThreadDeleteHandler(this.api));
        this.addHandler(new ThreadListSyncHandler(this.api));
        this.addHandler(new ThreadMembersUpdateHandler(this.api));
        this.addHandler(new ThreadUpdateHandler(this.api));
        this.addHandler(new PresencesReplaceHandler(this.api));
        this.addHandler(new PresenceUpdateHandler(this.api));
        this.addHandler(new TypingStartHandler(this.api));
        this.addHandler(new UserUpdateHandler(this.api));
        this.addHandler(new MessageCreateHandler(this.api));
        this.addHandler(new MessageDeleteBulkHandler(this.api));
        this.addHandler(new MessageDeleteHandler(this.api));
        this.addHandler(new MessageUpdateHandler(this.api));
        this.addHandler(new MessageReactionAddHandler(this.api));
        this.addHandler(new MessageReactionRemoveAllHandler(this.api));
        this.addHandler(new MessageReactionRemoveHandler(this.api));
        this.addHandler(new InviteCreateHandler(this.api));
        this.addHandler(new InviteDeleteHandler(this.api));
        this.addHandler(new InteractionCreateHandler(this.api));
    }

    private void addHandler(PacketHandler handler) {
        this.handlers.put(handler.getType(), handler);
    }

    public WebSocket getWebSocket() {
        return this.websocket.get();
    }

    public CompletableFuture<Boolean> isReady() {
        return this.ready;
    }

    public void updateStatus() {
        Optional<Activity> activity = this.api.getActivity();
        ObjectNode updateStatus = JsonNodeFactory.instance.objectNode().put("op", GatewayOpcode.STATUS_UPDATE.getCode());
        ObjectNode data = updateStatus.putObject("d").put("status", this.api.getStatus().getStatusString()).put("afk", false).putNull("since");
        ObjectNode activityJson = data.putObject("game");
        activityJson.put("name", (String)activity.map(Nameable::getName).orElse(null));
        activityJson.put("type", activity.flatMap(g -> {
            int type = g.getType().getId();
            if (type == 4) {
                logger.warn("Can't set the activity to ActivityType.CUSTOM, using ActivityType.PLAYING instead");
                return Optional.empty();
            }
            return Optional.of(type);
        }).orElse(0));
        activity.flatMap(Activity::getStreamingUrl).ifPresent(url -> activityJson.put("url", (String)url));
        logger.debug("Updating status (content: {})", (Object)updateStatus);
        this.sendTextFrame(updateStatus.toString());
    }

    public void sendCloseFrame(int closeCode) {
        this.sendCloseFrame(null, closeCode);
    }

    public void sendCloseFrame(WebSocket webSocket, int closeCode) {
        this.sendLifecycleFrame(webSocket, WebSocketFrame.createCloseFrame(closeCode));
    }

    public void sendCloseFrame(int closeCode, String reason) {
        this.sendCloseFrame(null, closeCode, reason);
    }

    public void sendCloseFrame(WebSocket webSocket, int closeCode, String reason) {
        this.sendLifecycleFrame(webSocket, WebSocketFrame.createCloseFrame(closeCode, reason));
    }

    public void sendLifecycleTextFrame(String message) {
        this.sendLifecycleTextFrame(null, message);
    }

    public void sendLifecycleTextFrame(WebSocket webSocket, String message) {
        this.sendLifecycleFrame(webSocket, WebSocketFrame.createTextFrame(message));
    }

    public void sendTextFrame(String message) {
        this.sendFrame(null, WebSocketFrame.createTextFrame(message), false, false);
    }

    public void sendLifecycleFrame(WebSocketFrame frame) {
        this.sendLifecycleFrame(null, frame);
    }

    public void sendLifecycleFrame(WebSocket webSocket, WebSocketFrame frame) {
        this.sendFrame(webSocket, frame, false, true);
    }

    public void sendFrame(WebSocketFrame frame) {
        this.sendFrame(null, frame, false, false);
    }

    private void sendFrame(WebSocket webSocket, WebSocketFrame frame, boolean priority, boolean lifecycle) {
        logger.debug("Queued {}lifecycle frame for sending with{} priority: {}", (Object)(lifecycle ? "" : "non-"), (Object)(priority ? "" : "out"), (Object)frame);
        this.webSocketFrameSendingQueue.add(new WebSocketFrameSendingQueueEntry(Optional.ofNullable(webSocket).orElseGet(() -> lifecycle ? this.websocket.get() : null), frame, priority, lifecycle));
        if (priority && lifecycle) {
            Optional.ofNullable(this.webSocketFrameSenderThread.get()).ifPresent(Thread::interrupt);
        }
    }

    public void queueRequestGuildMembers(Server server) {
        logger.debug("Queued {} for request guild members packet", (Object)server);
        this.requestGuildMembersQueue.add(server.getId());
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) {
        switch (cause.getMessage()) {
            case "Flushing frames to the server failed: Connection closed by remote host": 
            case "Flushing frames to the server failed: Socket is closed": 
            case "Flushing frames to the server failed: Connection has been shutdown: javax.net.ssl.SSLException: java.net.SocketException: Connection reset": 
            case "An I/O error occurred while a frame was being read from the web socket: Connection reset": {
                break;
            }
            default: {
                logger.warn("Websocket error!", (Throwable)cause);
            }
        }
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) {
        logger.error("Websocket callback error!", cause);
    }

    @Override
    public void onUnexpectedError(WebSocket websocket, WebSocketException cause) {
        logger.warn("Websocket onUnexpected error!", (Throwable)cause);
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) {
        logger.warn("Websocket onConnect error!", (Throwable)exception);
    }

    private static /* synthetic */ Object lambda$onTextMessage$22(int oneToFiveSeconds) {
        return oneToFiveSeconds / 100 % 10;
    }

    private static /* synthetic */ Object lambda$onTextMessage$21(int oneToFiveSeconds) {
        return oneToFiveSeconds / 1000;
    }

    /*
     * Unable to fully structure code
     */
    private /* synthetic */ void lambda$onTextMessage$20() {
        allUsersLoaded = false;
        allServersLoaded = false;
        lastUnavailableServerAmount = 0;
        sameUnavailableServerCounter = 0;
        while (!(!this.api.isWaitingForServersOnStartup() || allServersLoaded && allUsersLoaded)) {
            block7: {
                if (this.api.getUnavailableServers().size() == lastUnavailableServerAmount) {
                    ++sameUnavailableServerCounter;
                } else {
                    lastUnavailableServerAmount = this.api.getUnavailableServers().size();
                    sameUnavailableServerCounter = 0;
                }
                allServersLoaded = this.api.getUnavailableServers().isEmpty();
                if (!allServersLoaded) break block7;
                if (!this.api.hasUserCacheEnabled() || !this.api.isWaitingForUsersOnStartup()) ** GOTO lbl-1000
                if (this.api.getAllServers().stream().map((Function<Server, ServerImpl>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, cast(java.lang.Object ), (Lorg/javacord/api/entity/server/Server;)Lorg/javacord/core/entity/server/ServerImpl;)(ServerImpl.class)).noneMatch((Predicate<ServerImpl>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Z, lambda$onTextMessage$19(org.javacord.core.entity.server.ServerImpl ), (Lorg/javacord/core/entity/server/ServerImpl;)Z)())) lbl-1000:
                // 2 sources

                {
                    v0 = true;
                } else {
                    v0 = allUsersLoaded = false;
                }
            }
            if (sameUnavailableServerCounter > 1000 && this.lastGuildMembersChunkReceived + 5000L < System.currentTimeMillis()) break;
            try {
                Thread.sleep(100L);
            }
            catch (InterruptedException var5_5) {}
        }
        reconnectEvent = new ReconnectEventImpl(this.api);
        this.api.getEventDispatcher().dispatchReconnectEvent(null, reconnectEvent);
        this.ready.complete(true);
    }

    private static /* synthetic */ boolean lambda$onTextMessage$19(ServerImpl server) {
        return server.getMemberCount() != server.getRealMembers().size();
    }

    static {
        gatewayLock = new ReentrantReadWriteLock();
        gatewayReadLock = gatewayLock.readLock();
        gatewayWriteLock = gatewayLock.writeLock();
        WEB_SOCKET_FRAME_SENDING_RATELIMIT_DURATION = TimeUnit.NANOSECONDS.convert(1L, TimeUnit.MINUTES);
        ONE_SECOND = TimeUnit.NANOSECONDS.convert(1L, TimeUnit.SECONDS);
    }
}

