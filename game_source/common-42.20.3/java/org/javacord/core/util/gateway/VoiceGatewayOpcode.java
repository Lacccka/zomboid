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

public enum VoiceGatewayOpcode {
    IDENTIFY(0),
    SELECT_PROTOCOL(1),
    READY(2),
    HEARTBEAT(3),
    SESSION_DESCRIPTION(4),
    SPEAKING(5),
    HEARTBEAT_ACK(6),
    RESUME(7),
    HELLO(8),
    RESUMED(9),
    CLIENT_CONNECT(12),
    CLIENT_DISCONNECT(13);

    private static final Map<Integer, VoiceGatewayOpcode> instanceByCode;
    private final int code;

    private VoiceGatewayOpcode(int code) {
        this.code = code;
    }

    public static Optional<VoiceGatewayOpcode> fromCode(int code) {
        return Optional.ofNullable(instanceByCode.get(code));
    }

    public int getCode() {
        return this.code;
    }

    static {
        instanceByCode = Collections.unmodifiableMap(Arrays.stream(VoiceGatewayOpcode.values()).collect(Collectors.toMap(VoiceGatewayOpcode::getCode, Function.identity())));
    }
}

