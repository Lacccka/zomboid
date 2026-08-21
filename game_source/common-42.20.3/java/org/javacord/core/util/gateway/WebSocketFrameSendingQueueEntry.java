/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.gateway;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFrame;
import java.util.Comparator;
import java.util.Optional;

public class WebSocketFrameSendingQueueEntry
implements Comparable<WebSocketFrameSendingQueueEntry> {
    private static final Comparator<WebSocketFrameSendingQueueEntry> ENTRY_COMPARATOR = Comparator.comparing(entry -> !entry.lifecycle).thenComparing(entry -> !entry.priority).thenComparing(entry -> entry.timestamp);
    private final WebSocket webSocket;
    private final WebSocketFrame webSocketFrame;
    private final boolean priority;
    private final boolean lifecycle;
    private final long timestamp = System.nanoTime();

    public WebSocketFrameSendingQueueEntry(WebSocket webSocket, WebSocketFrame webSocketFrame, boolean priority, boolean lifecycle) {
        if (lifecycle && webSocket == null) {
            throw new IllegalArgumentException("lifecycle frame sending requests must specify the web socket");
        }
        this.webSocket = webSocket;
        this.webSocketFrame = webSocketFrame;
        this.priority = priority;
        this.lifecycle = lifecycle;
    }

    public Optional<WebSocket> getWebSocket() {
        return Optional.ofNullable(this.webSocket);
    }

    public WebSocketFrame getFrame() {
        return this.webSocketFrame;
    }

    public boolean isPriorityLifecycle() {
        return this.priority && this.lifecycle;
    }

    public boolean isLifecycle() {
        return this.lifecycle;
    }

    @Override
    public int compareTo(WebSocketFrameSendingQueueEntry other) {
        return ENTRY_COMPARATOR.compare(this, other);
    }
}

