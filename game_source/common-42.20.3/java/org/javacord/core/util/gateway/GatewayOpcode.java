/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.gateway;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum GatewayOpcode {
    DISPATCH(0),
    HEARTBEAT(1),
    IDENTIFY(2),
    STATUS_UPDATE(3),
    VOICE_STATE_UPDATE(4),
    VOICE_SERVER_PING(5),
    RESUME(6),
    RECONNECT(7),
    REQUEST_GUILD_MEMBERS(8),
    INVALID_SESSION(9),
    HELLO(10),
    HEARTBEAT_ACK(11);

    private static final Map<Integer, GatewayOpcode> instanceByCode;
    private final int code;

    private GatewayOpcode(int code) {
        this.code = code;
    }

    public static Optional<GatewayOpcode> fromCode(int code) {
        return Optional.ofNullable(instanceByCode.get(code));
    }

    public int getCode() {
        return this.code;
    }

    static {
        instanceByCode = Collections.unmodifiableMap(Arrays.stream(GatewayOpcode.values()).collect(Collectors.toMap(GatewayOpcode::getCode, Function.identity())));
    }
}

