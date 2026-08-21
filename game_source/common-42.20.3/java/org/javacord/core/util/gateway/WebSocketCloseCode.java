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

public enum WebSocketCloseCode {
    NORMAL(1000, Usage.BOTH),
    AWAY(1001, Usage.BOTH),
    UNCONFORMED(1002, Usage.BOTH),
    UNACCEPTABLE(1003, Usage.BOTH),
    NONE(1005, Usage.BOTH),
    ABNORMAL(1006, Usage.BOTH),
    INCONSISTENT(1007, Usage.BOTH),
    VIOLATED(1008, Usage.BOTH),
    OVERSIZE(1009, Usage.BOTH),
    UNEXTENDED(1010, Usage.BOTH),
    UNEXPECTED(1011, Usage.BOTH),
    INSECURE(1015, Usage.BOTH),
    UNKNOWN_ERROR(4000, Usage.BOTH),
    UNKNOWN_OPCODE(4001, Usage.BOTH),
    DECODE_ERROR(4002, Usage.NORMAL),
    NOT_AUTHENTICATED(4003, Usage.BOTH),
    AUTHENTICATION_FAILED(4004, Usage.BOTH),
    ALREADY_AUTHENTICATED(4005, Usage.BOTH),
    SESSION_NO_LONGER_VALID(4006, Usage.VOICE),
    INVALID_SEQ(4007, Usage.NORMAL),
    RATE_LIMITED(4008, Usage.NORMAL),
    SESSION_TIMEOUT(4009, Usage.BOTH),
    INVALID_SHARD(4010, Usage.NORMAL),
    SHARDING_REQUIRED(4011, Usage.NORMAL),
    SERVER_NOT_FOUND(4011, Usage.VOICE),
    INVALID_API_VERSION(4012, Usage.NORMAL),
    UNKNOWN_PROTOCOL(4012, Usage.VOICE),
    INVALID_INTENTS(4013, Usage.NORMAL),
    DISALLOWED_INTENTS(4014, Usage.NORMAL),
    DISCONNECTED(4014, Usage.VOICE),
    VOICE_SERVER_CRASHED(4015, Usage.VOICE),
    UNKNOWN_ENCRYPTION_MODE(4016, Usage.VOICE),
    INVALID_SESSION_RECONNECT(4998, Usage.NORMAL),
    COMMANDED_RECONNECT(4999, Usage.NORMAL),
    UNKNOWN(-1, Usage.BOTH);

    private static final Map<Integer, WebSocketCloseCode> instanceByCode;
    private static final Map<Integer, WebSocketCloseCode> voiceInstanceByCode;
    private final int code;
    private final Usage usage;

    private WebSocketCloseCode(int code, Usage usage) {
        this.code = code;
        this.usage = usage;
    }

    public static Optional<WebSocketCloseCode> fromCode(int code) {
        return Optional.ofNullable(instanceByCode.get(code));
    }

    public static Optional<WebSocketCloseCode> fromCodeForVoice(int code) {
        return Optional.ofNullable(voiceInstanceByCode.get(code));
    }

    public int getCode() {
        return this.code;
    }

    static {
        instanceByCode = Collections.unmodifiableMap(Arrays.stream(WebSocketCloseCode.values()).filter(closeCode -> closeCode.usage == Usage.BOTH || closeCode.usage == Usage.NORMAL).collect(Collectors.toMap(WebSocketCloseCode::getCode, Function.identity())));
        voiceInstanceByCode = Collections.unmodifiableMap(Arrays.stream(WebSocketCloseCode.values()).filter(closeCode -> closeCode.usage == Usage.BOTH || closeCode.usage == Usage.VOICE).collect(Collectors.toMap(WebSocketCloseCode::getCode, Function.identity())));
    }

    private static enum Usage {
        NORMAL,
        VOICE,
        BOTH;

    }
}

