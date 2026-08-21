/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.gateway;

import org.javacord.core.util.gateway.WebSocketCloseCode;

public enum WebSocketCloseReason {
    DISCONNECT(WebSocketCloseCode.NORMAL),
    HEARTBEAT_NOT_PROPERLY_ANSWERED(WebSocketCloseCode.UNKNOWN_ERROR, "Heartbeat was not answered properly"),
    INVALID_SESSION_RECONNECT(WebSocketCloseCode.INVALID_SESSION_RECONNECT, "Session is invalid (Received opcode 9)"),
    COMMANDED_RECONNECT(WebSocketCloseCode.COMMANDED_RECONNECT, "Discord commanded a reconnect (Received opcode 7)");

    private final WebSocketCloseCode closeCode;
    private final String closeReason;

    private WebSocketCloseReason(WebSocketCloseCode closeCode) {
        this(closeCode, null);
    }

    private WebSocketCloseReason(WebSocketCloseCode closeCode, String closeReason) {
        this.closeCode = closeCode;
        this.closeReason = closeReason;
    }

    public int getNumericCloseCode() {
        return this.closeCode.getCode();
    }

    public String getCloseReason() {
        return this.closeReason;
    }
}

