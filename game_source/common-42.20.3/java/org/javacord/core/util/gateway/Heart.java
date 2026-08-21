/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neovisionaries.ws.client.WebSocketFrame;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.apache.logging.log4j.Logger;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.gateway.GatewayOpcode;
import org.javacord.core.util.gateway.VoiceGatewayOpcode;
import org.javacord.core.util.logging.LoggerUtil;

public class Heart {
    private static final Logger stethoscope = LoggerUtil.getLogger(Heart.class);
    private final DiscordApiImpl api;
    private final Consumer<WebSocketFrame> heartbeatFrameSender;
    private final BiConsumer<Integer, String> closeFrameSender;
    private final boolean voice;
    private final AtomicReference<Future<?>> heartbeatTimer = new AtomicReference();
    private final AtomicBoolean heartbeatAckReceived = new AtomicBoolean();
    private int lastSeq = 0;
    private volatile long lastHeartbeatSentTimeNanos = -1L;

    public Heart(DiscordApiImpl api, Consumer<WebSocketFrame> heartbeatFrameSender, BiConsumer<Integer, String> closeFrameSender, boolean voice) {
        this.api = api;
        this.heartbeatFrameSender = heartbeatFrameSender;
        this.closeFrameSender = closeFrameSender;
        this.voice = voice;
    }

    public void handlePacket(JsonNode packet) {
        int heartbeatAckOp;
        if (!this.voice && packet.has("s") && !packet.get("s").isNull()) {
            this.lastSeq = packet.get("s").asInt();
        }
        int n = heartbeatAckOp = this.voice ? VoiceGatewayOpcode.HEARTBEAT_ACK.getCode() : GatewayOpcode.HEARTBEAT_ACK.getCode();
        if (packet.get("op").asInt() == heartbeatAckOp) {
            long gatewayLatency = System.nanoTime() - this.lastHeartbeatSentTimeNanos;
            if (!this.voice) {
                this.api.setLatestGatewayLatencyNanos(gatewayLatency);
            }
            stethoscope.debug("Heartbeat ACK received (voice: {}, packet: {}). Took {} ms to receive ACK", (Object)this.voice, (Object)packet, (Object)TimeUnit.NANOSECONDS.toMillis(gatewayLatency));
            this.heartbeatAckReceived.set(true);
        }
    }

    public void startBeating(int interval) {
        this.heartbeatAckReceived.set(true);
        this.heartbeatTimer.updateAndGet(future -> {
            if (future != null) {
                future.cancel(false);
            }
            return this.api.getThreadPool().getScheduler().scheduleWithFixedDelay(() -> {
                try {
                    if (this.heartbeatAckReceived.getAndSet(false)) {
                        this.beat();
                    } else {
                        stethoscope.debug("Heartbeat not answered properly. This might be because of a busy websocket");
                    }
                }
                catch (Throwable t) {
                    stethoscope.error("Failed to send heartbeat or close web socket!", t);
                }
            }, 0L, interval, TimeUnit.MILLISECONDS);
        });
    }

    public void beat() {
        ObjectNode heartbeatPacket = JsonNodeFactory.instance.objectNode().put("op", this.voice ? VoiceGatewayOpcode.HEARTBEAT.getCode() : GatewayOpcode.HEARTBEAT.getCode()).put("d", this.voice ? (int)(Math.random() * 2.147483647E9) : this.lastSeq);
        WebSocketFrame heartbeatFrame = WebSocketFrame.createTextFrame(heartbeatPacket.toString());
        this.heartbeatFrameSender.accept(heartbeatFrame);
        this.lastHeartbeatSentTimeNanos = System.nanoTime();
        stethoscope.debug("Sent heartbeat (voice: {}, packet: {})", (Object)this.voice, (Object)heartbeatPacket);
    }

    public void squash() {
        this.heartbeatTimer.updateAndGet(future -> {
            if (future != null) {
                future.cancel(false);
            }
            return null;
        });
    }
}

