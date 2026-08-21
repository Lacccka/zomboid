/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.DataFormatException;
import javax.net.ssl.SSLContext;
import org.apache.logging.log4j.Logger;
import org.javacord.api.audio.SpeakingFlag;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.audio.AudioConnectionImpl;
import org.javacord.core.util.gateway.AudioUdpSocket;
import org.javacord.core.util.gateway.BinaryMessageDecompressor;
import org.javacord.core.util.gateway.Heart;
import org.javacord.core.util.gateway.VoiceGatewayOpcode;
import org.javacord.core.util.gateway.WebSocketCloseCode;
import org.javacord.core.util.gateway.WebSocketCloseReason;
import org.javacord.core.util.logging.LoggerUtil;
import org.javacord.core.util.logging.WebSocketLogger;

public class AudioWebSocketAdapter
extends WebSocketAdapter {
    private static final Logger logger = LoggerUtil.getLogger(AudioWebSocketAdapter.class);
    private final AudioConnectionImpl connection;
    private final DiscordApiImpl api;
    private final AtomicReference<WebSocket> websocket = new AtomicReference();
    private final Heart heart;
    private AudioUdpSocket socket;
    private int ssrc;
    private volatile boolean reconnect;
    private volatile boolean resuming;
    private final AtomicInteger reconnectAttempt = new AtomicInteger();

    public AudioWebSocketAdapter(AudioConnectionImpl connection) {
        this.connection = connection;
        this.reconnect = true;
        this.api = (DiscordApiImpl)connection.getChannel().getApi();
        this.heart = new Heart(this.api, heartbeatFrame -> this.websocket.get().sendFrame((WebSocketFrame)heartbeatFrame), (code, reason) -> this.websocket.get().sendClose((int)code, (String)reason), true);
        this.connect();
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        ObjectMapper mapper = this.api.getObjectMapper();
        JsonNode packet = mapper.readTree(text);
        this.heart.handlePacket(packet);
        int op = packet.get("op").asInt();
        Optional<VoiceGatewayOpcode> opcode = VoiceGatewayOpcode.fromCode(op);
        if (!opcode.isPresent()) {
            logger.debug("Received unknown audio websocket packet ({}, op: {}, content: {})", (Object)this.connection, (Object)op, (Object)packet);
            return;
        }
        switch (opcode.get()) {
            case HELLO: {
                logger.debug("Received {} packet for {}", (Object)opcode.get().name(), (Object)this.connection);
                if (!this.resuming) {
                    this.sendIdentify(websocket);
                }
                JsonNode data = packet.get("d");
                int heartbeatInterval = data.get("heartbeat_interval").asInt();
                this.heart.startBeating(heartbeatInterval);
                break;
            }
            case READY: {
                logger.debug("Received {} packet for {}", (Object)opcode.get().name(), (Object)this.connection);
                JsonNode data = packet.get("d");
                String ip = data.get("ip").asText();
                int port = data.get("port").asInt();
                this.ssrc = data.get("ssrc").asInt();
                this.socket = new AudioUdpSocket(this.connection, new InetSocketAddress(ip, port), this.ssrc);
                this.sendSelectProtocol(websocket);
                Thread.sleep(1000L);
                break;
            }
            case SESSION_DESCRIPTION: {
                this.sendSpeaking(websocket);
                JsonNode data = packet.get("d");
                byte[] secretKey = this.api.getObjectMapper().convertValue((Object)data.get("secret_key"), byte[].class);
                this.socket.setSecretKey(secretKey);
                this.socket.startSending();
                this.connection.getReadyFuture().complete(this.connection);
                break;
            }
            case HEARTBEAT_ACK: {
                break;
            }
            case RESUMED: {
                this.resuming = false;
                this.reconnectAttempt.set(0);
                logger.info("Successfully resumed audio websocket connection for {}", (Object)this.connection);
                break;
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

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
        if (this.resuming) {
            this.sendResume(websocket);
            this.socket.startSending();
        }
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
        Optional<WebSocketFrame> closeFrameOptional = Optional.ofNullable(closedByServer ? serverCloseFrame : clientCloseFrame);
        WebSocketCloseCode closeCode = closeFrameOptional.flatMap(closeFrame -> WebSocketCloseCode.fromCodeForVoice(closeFrame.getCloseCode())).orElse(WebSocketCloseCode.UNKNOWN);
        String closeReason = closeFrameOptional.map(WebSocketFrame::getCloseReason).orElse("unknown");
        String closeCodeString = (Object)((Object)closeCode) + " (" + (closeCode == WebSocketCloseCode.UNKNOWN ? "Unknown" : Integer.valueOf(closeCode.getCode())) + ")";
        logger.info("Websocket closed with reason '{}' and code {} by {} for {}!", (Object)closeReason, (Object)closeCodeString, (Object)(closedByServer ? "server" : "client"), (Object)this.connection);
        this.heart.squash();
        this.socket.stopSending();
        if (this.resuming) {
            logger.info("Could not resume, reconnecting in {} seconds", (Object)this.api.getReconnectDelay(this.reconnectAttempt.get()));
            this.resuming = false;
            this.reconnectAttempt.set(0);
            this.api.getThreadPool().getScheduler().schedule(this::connect, (long)this.api.getReconnectDelay(this.reconnectAttempt.get()), TimeUnit.SECONDS);
            return;
        }
        switch (closeCode) {
            case AUTHENTICATION_FAILED: 
            case SERVER_NOT_FOUND: {
                this.connection.close();
                break;
            }
            case SESSION_NO_LONGER_VALID: 
            case DISCONNECTED: {
                if (!this.connection.getReadyFuture().isDone()) {
                    this.connection.getReadyFuture().completeExceptionally(new IllegalStateException("Audio websocket closed with reason '" + closeReason + "' and code " + closeCodeString + " by " + (closedByServer ? "server" : "client") + " before " + VoiceGatewayOpcode.SESSION_DESCRIPTION.name() + " packet was received"));
                }
                this.disconnect();
                this.connection.reconnect();
                break;
            }
            case UNKNOWN_ERROR: 
            case UNKNOWN_OPCODE: 
            case UNKNOWN_PROTOCOL: 
            case UNKNOWN_ENCRYPTION_MODE: 
            case VOICE_SERVER_CRASHED: {
                this.resuming = true;
                logger.info("Trying to resume audio websocket in {} seconds!", (Object)this.api.getReconnectDelay(this.reconnectAttempt.get()));
                this.api.getThreadPool().getScheduler().schedule(this::connect, (long)this.api.getReconnectDelay(this.reconnectAttempt.get()), TimeUnit.SECONDS);
                break;
            }
            case NORMAL: {
                if (!closedByServer && this.connection.getDisconnectFuture() != null) {
                    this.connection.getDisconnectFuture().complete(null);
                    break;
                }
                this.reconnect();
                break;
            }
            default: {
                this.reconnect();
            }
        }
    }

    private void connect() {
        block4: {
            String endpoint = "wss://" + this.connection.getEndpoint().replace(":80", "") + "?v=" + "4";
            logger.debug("Trying to connect to websocket {}", (Object)endpoint);
            WebSocketFactory factory2 = new WebSocketFactory();
            try {
                factory2.setSSLContext(SSLContext.getDefault());
            }
            catch (NoSuchAlgorithmException e) {
                logger.warn("An error occurred while setting ssl context", (Throwable)e);
            }
            try {
                WebSocket websocket = factory2.createSocket(endpoint);
                this.websocket.set(websocket);
                websocket.addHeader("Accept-Encoding", "gzip");
                websocket.addListener(this);
                websocket.addListener(new WebSocketLogger());
                websocket.connect();
            }
            catch (Throwable t) {
                logger.warn("An error occurred while connecting to audio websocket for {} ({})", (Object)this.connection, (Object)t.getCause());
                if (!this.reconnect) break block4;
                this.reconnectAttempt.incrementAndGet();
                logger.info("Trying to reconnect/resume audio websocket in {} seconds!", (Object)this.api.getReconnectDelay(this.reconnectAttempt.get()));
                this.api.getThreadPool().getScheduler().schedule(this::connect, (long)this.api.getReconnectDelay(this.reconnectAttempt.get()), TimeUnit.SECONDS);
            }
        }
    }

    private void reconnect() {
        this.reconnectAttempt.incrementAndGet();
        logger.info("Trying to reconnect audio websocket in {} seconds!", (Object)this.api.getReconnectDelay(this.reconnectAttempt.get()));
        this.api.getThreadPool().getScheduler().schedule(this::connect, (long)this.api.getReconnectDelay(this.reconnectAttempt.get()), TimeUnit.SECONDS);
    }

    public void disconnect() {
        this.reconnect = false;
        this.socket.stopSending();
        this.websocket.get().sendClose(WebSocketCloseReason.DISCONNECT.getNumericCloseCode());
        this.api.getThreadPool().getDaemonScheduler().schedule(this.heart::squash, 1L, TimeUnit.MINUTES);
    }

    private void sendResume(WebSocket websocket) {
        ObjectNode resumePacket = JsonNodeFactory.instance.objectNode().put("op", VoiceGatewayOpcode.RESUME.getCode());
        ObjectNode data = resumePacket.putObject("d");
        data.put("server_id", this.connection.getServer().getIdAsString()).put("session_id", this.connection.getSessionId()).put("token", this.connection.getToken());
        logger.debug("Sending resume packet for {}", (Object)this.connection);
        WebSocketFrame resumeFrame = WebSocketFrame.createTextFrame(resumePacket.toString());
        websocket.sendFrame(resumeFrame);
    }

    private void sendIdentify(WebSocket websocket) {
        ObjectNode identifyPacket = JsonNodeFactory.instance.objectNode().put("op", VoiceGatewayOpcode.IDENTIFY.getCode());
        ObjectNode data = identifyPacket.putObject("d");
        data.put("server_id", this.connection.getServer().getIdAsString()).put("user_id", this.connection.getServer().getApi().getYourself().getIdAsString()).put("session_id", this.connection.getSessionId()).put("token", this.connection.getToken());
        logger.debug("Sending voice identify packet for {}", (Object)this.connection);
        WebSocketFrame identifyFrame = WebSocketFrame.createTextFrame(identifyPacket.toString());
        websocket.sendFrame(identifyFrame);
    }

    private void sendSelectProtocol(WebSocket websocket) throws IOException {
        InetSocketAddress address = this.socket.discoverIp();
        ObjectNode selectProtocolPacket = JsonNodeFactory.instance.objectNode();
        selectProtocolPacket.put("op", VoiceGatewayOpcode.SELECT_PROTOCOL.getCode()).putObject("d").put("protocol", "udp").putObject("data").put("address", address.getHostString()).put("port", address.getPort()).put("mode", "xsalsa20_poly1305");
        logger.debug("Sending select protocol packet for {}", (Object)this.connection);
        WebSocketFrame selectProtocolFrame = WebSocketFrame.createTextFrame(selectProtocolPacket.toString());
        websocket.sendFrame(selectProtocolFrame);
    }

    private void sendSpeaking(WebSocket websocket) {
        ObjectNode speakingPacket = JsonNodeFactory.instance.objectNode();
        int speakingFlags = 0;
        for (SpeakingFlag flag : this.connection.getSpeakingFlags()) {
            speakingFlags |= flag.asInt();
        }
        speakingPacket.put("op", VoiceGatewayOpcode.SPEAKING.getCode()).putObject("d").put("speaking", speakingFlags).put("delay", 0).put("ssrc", this.ssrc);
        logger.debug("Sending speaking packet for {} (packet: {})", (Object)this.connection, (Object)speakingPacket);
        WebSocketFrame speakingFrame = WebSocketFrame.createTextFrame(speakingPacket.toString());
        websocket.sendFrame(speakingFrame);
    }

    public void sendSpeaking() {
        this.sendSpeaking(this.websocket.get());
    }
}

