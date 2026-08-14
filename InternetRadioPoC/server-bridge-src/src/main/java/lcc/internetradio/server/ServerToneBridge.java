package lcc.internetradio.server;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import zombie.core.raknet.RakVoice;
import zombie.core.raknet.UdpConnection;
import zombie.core.raknet.UdpEngine;
import zombie.network.GameServer;

/** Server-only Phase 0 probe for RakVoice.SendFrame after RVInitServer. */
public final class ServerToneBridge {
    public static final String VERSION = "0.8.7";

    private static final long MONITOR_PERIOD_MS = 250L;
    private static final long TEST_DELAY_MS = 5_000L;
    private static final long TONE_DURATION_MS = 4_000L;
    private static final double TONE_HZ = 440.0;
    private static final double AMPLITUDE = 0.20;

    private static final AtomicBoolean BOOTSTRAPPED = new AtomicBoolean();
    private static final AtomicBoolean MONITOR_STARTED = new AtomicBoolean();
    private static final AtomicBoolean ENGINE_WAIT_LOGGED = new AtomicBoolean();
    private static final AtomicBoolean CONNECTION_WAIT_LOGGED = new AtomicBoolean();
    private static final AtomicBoolean VOICE_STATE_LOGGED = new AtomicBoolean();
    private static final AtomicBoolean VOICE_DISABLED_LOGGED = new AtomicBoolean();
    private static final AtomicBoolean TARGET_LOGGED = new AtomicBoolean();
    private static final AtomicBoolean TARGET_RESOLUTION_LOGGED = new AtomicBoolean();
    private static final AtomicBoolean TARGET_RESOLUTION_FAILURE_LOGGED = new AtomicBoolean();
    private static final AtomicBoolean PROBE_STARTED = new AtomicBoolean();

    private static volatile long eligibleSince;
    private static volatile boolean disabled;

    private ServerToneBridge() {
    }

    /** Called by Leaf's deterministic main entrypoint. */
    public static void bootstrap() {
        if (!BOOTSTRAPPED.compareAndSet(false, true)) return;
        log("BOOT", "version=" + VERSION + "; environment=dedicated-server"
                + "; directProbe=true; frequencyFuture=104.6");

        Thread monitor = new Thread(
                ServerToneBridge::monitorLoop,
                "LCC-InternetRadio-Monitor");
        monitor.setDaemon(true);
        monitor.setUncaughtExceptionHandler(
                (thread, error) -> fail("MONITOR", error));
        monitor.start();
    }

    private static void monitorLoop() {
        if (MONITOR_STARTED.compareAndSet(false, true)) {
            log("MONITOR_OK", "daemon polling started; periodMs="
                    + MONITOR_PERIOD_MS + "; thread="
                    + Thread.currentThread().getName());
        }

        while (!disabled && !PROBE_STARTED.get()) {
            pollOnce();
            if (!disabled && !PROBE_STARTED.get()) sleepMonitorPeriod();
        }
    }

    private static void pollOnce() {
        if (disabled) return;
        if (PROBE_STARTED.get()) return;

        try {
            UdpEngine engine = GameServer.udpEngine;
            if (engine == null) {
                if (ENGINE_WAIT_LOGGED.compareAndSet(false, true)) {
                    log("WAIT", "GameServer.udpEngine is not initialized yet");
                }
                return;
            }

            List<Target> connected = snapshotTargets(engine.connections);
            if (connected.isEmpty()) {
                eligibleSince = 0L;
                if (CONNECTION_WAIT_LOGGED.compareAndSet(false, true)) {
                    log("WAIT", "no fully-connected player with a valid onlineID");
                }
                return;
            }

            logVoiceStateOnce();
            if (disabled) return;
            if (!RakVoice.GetServerVOIPEnable()) {
                if (VOICE_DISABLED_LOGGED.compareAndSet(false, true)) {
                    log("WAIT", "server VOIP is disabled; direct probe is paused");
                }
                return;
            }

            long now = System.currentTimeMillis();
            if (eligibleSince == 0L) eligibleSince = now;

            Target source = connected.get(0);
            Target recipient = connected.size() >= 2 ? connected.get(1) : source;

            if (now - eligibleSince < TEST_DELAY_MS) return;
            if (!PROBE_STARTED.compareAndSet(false, true)) return;

            if (TARGET_LOGGED.compareAndSet(false, true)) {
                boolean selfTarget = source.guid == recipient.guid;
                log("TARGET", "sourceGuid=" + source.guid
                        + "; sourceOnlineId=" + source.playerId
                        + "; recipientGuid=" + recipient.guid
                        + "; connectedTargets=" + connected.size()
                        + "; mode=" + (selfTarget ? "self-target" : "cross-client")
                        + "; selfSuppressionPossible=" + selfTarget);
            }

            Thread worker = new Thread(
                    () -> sendTone(source, recipient),
                    "LCC-InternetRadio-DirectProbe");
            worker.setDaemon(true);
            worker.setUncaughtExceptionHandler(
                    (thread, error) -> fail("WORKER", error));
            worker.start();
        } catch (Throwable error) {
            fail("MONITOR_POLL", error);
        }
    }

    private static void sleepMonitorPeriod() {
        try {
            Thread.sleep(MONITOR_PERIOD_MS);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            disabled = true;
            log("MONITOR", "interrupted; probe disabled");
        }
    }

    private static List<Target> snapshotTargets(List<UdpConnection> connections) {
        List<Target> result = new ArrayList<>();
        if (connections == null) return result;

        Object[] snapshot = connections.toArray();
        List<UdpConnection> fullyConnected = new ArrayList<>();
        for (Object value : snapshot) {
            if (!(value instanceof UdpConnection)) continue;
            UdpConnection connection = (UdpConnection) value;
            if (!connection.isFullyConnected()) continue;
            fullyConnected.add(connection);
        }

        boolean allowGlobalFallback = fullyConnected.size() == 1;
        for (UdpConnection connection : fullyConnected) {
            ResolvedPlayer resolved = resolvePlayerId(
                    connection, allowGlobalFallback);
            short playerId = resolved.playerId;
            if (playerId < 0) continue;
            long guid = connection.getConnectedGUID();
            if (guid == 0L) continue;
            if (TARGET_RESOLUTION_LOGGED.compareAndSet(false, true)) {
                log("TARGET_RESOLVE", "strategy=" + resolved.strategy
                        + "; onlineId=" + playerId
                        + "; connectedCount=" + fullyConnected.size());
            }
            result.add(new Target(guid, playerId));
        }

        if (result.isEmpty() && !fullyConnected.isEmpty()
                && TARGET_RESOLUTION_FAILURE_LOGGED.compareAndSet(false, true)) {
            UdpConnection connection = fullyConnected.get(0);
            log("TARGET_RESOLVE", "unresolved; connectionClass="
                    + connection.getClass().getName()
                    + "; fields=" + describeFields(connection.getClass()));
        }
        return result;
    }

    private static ResolvedPlayer resolvePlayerId(
            UdpConnection connection, boolean allowGlobalFallback) {
        short playerId = firstOnlineId(readField(connection, "players"));
        if (playerId >= 0) {
            return new ResolvedPlayer(playerId, "connection.players");
        }

        playerId = firstNumericId(readField(connection, "playerIDs"));
        if (playerId >= 0) {
            return new ResolvedPlayer(playerId, "connection.playerIDs");
        }

        if (allowGlobalFallback) {
            playerId = firstOnlineId(readStaticField(GameServer.class, "Players"));
            if (playerId >= 0) {
                return new ResolvedPlayer(playerId, "GameServer.Players-single-connection");
            }

            playerId = firstOnlineId(
                    readStaticField(GameServer.class, "IDToPlayerMap"));
            if (playerId >= 0) {
                return new ResolvedPlayer(
                        playerId, "GameServer.IDToPlayerMap-single-connection");
            }
        }

        return ResolvedPlayer.NONE;
    }

    private static Object readField(Object target, String fieldName) {
        if (target == null) return null;
        Field field = findField(target.getClass(), fieldName);
        if (field == null) return null;
        try {
            field.setAccessible(true);
            return field.get(target);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Object readStaticField(Class<?> owner, String fieldName) {
        Field field = findField(owner, fieldName);
        if (field == null) return null;
        try {
            field.setAccessible(true);
            return field.get(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Field findField(Class<?> type, String fieldName) {
        for (Class<?> current = type; current != null;
                current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                // Continue through the runtime hierarchy.
            }
        }
        return null;
    }

    private static short firstOnlineId(Object value) {
        if (value == null) return -1;
        if (value instanceof Map<?, ?>) {
            for (Object entryValue : ((Map<?, ?>) value).values()) {
                short id = firstOnlineId(entryValue);
                if (id >= 0) return id;
            }
            return -1;
        }
        if (value instanceof Iterable<?>) {
            for (Object element : (Iterable<?>) value) {
                short id = firstOnlineId(element);
                if (id >= 0) return id;
            }
            return -1;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int index = 0; index < length; index++) {
                short id = firstOnlineId(Array.get(value, index));
                if (id >= 0) return id;
            }
            return -1;
        }

        Object result = invokeNoArg(value, "getOnlineID");
        return result instanceof Number
                ? validShort(((Number) result).longValue()) : -1;
    }

    private static short firstNumericId(Object value) {
        if (value == null) return -1;
        if (value instanceof Number) {
            return validShort(((Number) value).longValue());
        }
        if (value instanceof Iterable<?>) {
            for (Object element : (Iterable<?>) value) {
                short id = firstNumericId(element);
                if (id >= 0) return id;
            }
            return -1;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int index = 0; index < length; index++) {
                short id = firstNumericId(Array.get(value, index));
                if (id >= 0) return id;
            }
            return -1;
        }

        Object sizeValue = invokeNoArg(value, "size");
        if (!(sizeValue instanceof Number)) return -1;
        int size = ((Number) sizeValue).intValue();
        for (int index = 0; index < size; index++) {
            Object element = invokeIntArg(value, "get", index);
            short id = firstNumericId(element);
            if (id >= 0) return id;
        }
        return -1;
    }

    private static Object invokeNoArg(Object target, String methodName) {
        Method method = findMethod(target.getClass(), methodName);
        if (method == null) return null;
        try {
            method.setAccessible(true);
            return method.invoke(target);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Object invokeIntArg(
            Object target, String methodName, int argument) {
        Method method = findMethod(target.getClass(), methodName, int.class);
        if (method == null) return null;
        try {
            method.setAccessible(true);
            return method.invoke(target, argument);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Method findMethod(
            Class<?> type, String methodName, Class<?>... parameterTypes) {
        try {
            return type.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException ignored) {
            // Check non-public runtime declarations as a compatibility fallback.
        }
        for (Class<?> current = type; current != null;
                current = current.getSuperclass()) {
            try {
                return current.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException ignored) {
                // Continue through the runtime hierarchy.
            }
        }
        return null;
    }

    private static short validShort(long value) {
        return value >= 0L && value <= Short.MAX_VALUE ? (short) value : -1;
    }

    private static String describeFields(Class<?> type) {
        List<String> descriptions = new ArrayList<>();
        for (Class<?> current = type; current != null;
                current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                descriptions.add(field.getName() + ":"
                        + field.getType().getTypeName());
            }
        }
        Collections.sort(descriptions);
        String joined = String.join(",", descriptions);
        return joined.length() <= 1_000 ? joined : joined.substring(0, 1_000) + "...";
    }

    private static void logVoiceStateOnce() {
        if (!VOICE_STATE_LOGGED.compareAndSet(false, true)) return;
        try {
            log("VOICE_STATE", "serverEnabled=" + RakVoice.GetServerVOIPEnable()
                    + "; sampleRate=" + RakVoice.GetSampleRate()
                    + "; periodMs=" + RakVoice.GetSendFramePeriod()
                    + "; bufferSize=" + RakVoice.GetBufferSizeBytes()
                    + "; buffering=" + RakVoice.GetBuffering()
                    + "; is3D=" + RakVoice.GetIs3D()
                    + "; minDistance=" + RakVoice.GetMinDistance()
                    + "; maxDistance=" + RakVoice.GetMaxDistance());
        } catch (Throwable error) {
            fail("VOICE_STATE", error);
        }
    }

    private static void sendTone(Target source, Target recipient) {
        try {
            boolean selfTarget = source.guid == recipient.guid;
            int sampleRate = RakVoice.GetSampleRate();
            int frameBytes = RakVoice.GetBufferSizeBytes();
            int framePeriodMs = RakVoice.GetSendFramePeriod();
            if (sampleRate <= 0 || frameBytes < 2 || framePeriodMs <= 0) {
                log("DIRECT_TEST", "FAIL invalid voice format"
                        + "; sampleRate=" + sampleRate
                        + "; frameBytes=" + frameBytes
                        + "; framePeriodMs=" + framePeriodMs);
                return;
            }

            byte[] frame = new byte[frameBytes];
            int samplesPerFrame = frameBytes / 2;
            int framesToSend = Math.max(1,
                    (int) Math.ceil((double) TONE_DURATION_MS / framePeriodMs));
            double phase = 0.0;
            double phaseStep = 2.0 * Math.PI * TONE_HZ / sampleRate;

            log("DIRECT_TEST", "guid=" + recipient.guid
                    + "; onlineId=" + source.playerId
                    + "; bytes=" + frameBytes
                    + "; frames=" + framesToSend
                    + "; durationMs=" + TONE_DURATION_MS
                    + "; toneHz=" + TONE_HZ
                    + "; mode=" + (selfTarget ? "self-target" : "cross-client"));

            int sent = 0;
            long nextFrameAt = System.nanoTime();
            for (int frameIndex = 0; frameIndex < framesToSend && !disabled;
                    frameIndex++) {
                for (int sampleIndex = 0; sampleIndex < samplesPerFrame;
                        sampleIndex++) {
                    short sample = (short) Math.round(
                            Math.sin(phase) * Short.MAX_VALUE * AMPLITUDE);
                    int byteIndex = sampleIndex * 2;
                    frame[byteIndex] = (byte) (sample & 0xff);
                    frame[byteIndex + 1] = (byte) ((sample >>> 8) & 0xff);
                    phase += phaseStep;
                    if (phase >= 2.0 * Math.PI) phase -= 2.0 * Math.PI;
                }

                if (frameIndex == 0) {
                    log("SEND_ENTER", "guid=" + recipient.guid
                            + "; onlineId=" + source.playerId
                            + "; bytes=" + frame.length);
                }
                RakVoice.SendFrame(
                        recipient.guid, source.playerId, frame, frame.length);
                if (frameIndex == 0) {
                    log("SEND_RETURN", "first frame returned without Java/native exception");
                }
                sent++;
                nextFrameAt += framePeriodMs * 1_000_000L;
                sleepUntil(nextFrameAt);
            }

            log("DIRECT_RESULT", "sendFrameReturned=true; framesSent=" + sent
                    + "; audibleResult=" + (selfTarget
                            ? "inconclusive-if-silent-self-voice-may-be-suppressed"
                            : "must-be-confirmed-in-game")
                    + "; sourceOnlineId=" + source.playerId
                    + "; recipientGuid=" + recipient.guid);
        } catch (Throwable error) {
            fail("DIRECT_TEST", error);
        }
    }

    private static void sleepUntil(long deadlineNanos) {
        long remaining;
        while ((remaining = deadlineNanos - System.nanoTime()) > 0L) {
            long millis = remaining / 1_000_000L;
            int nanos = (int) (remaining % 1_000_000L);
            try {
                Thread.sleep(millis, nanos);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                disabled = true;
                return;
            }
        }
    }

    private static void fail(String area, Throwable error) {
        disabled = true;
        Throwable root = error;
        while (root.getCause() != null) root = root.getCause();
        log(area, "FAIL; probe disabled; " + root.getClass().getName()
                + (root.getMessage() == null ? "" : ": " + root.getMessage()));
    }

    private static void log(String area, String message) {
        System.out.println("[InternetRadioBridge][" + area + "] " + message);
    }

    private static final class Target {
        private final long guid;
        private final short playerId;

        private Target(long guid, short playerId) {
            this.guid = guid;
            this.playerId = playerId;
        }
    }

    private static final class ResolvedPlayer {
        private static final ResolvedPlayer NONE =
                new ResolvedPlayer((short) -1, "none");

        private final short playerId;
        private final String strategy;

        private ResolvedPlayer(short playerId, String strategy) {
            this.playerId = playerId;
            this.strategy = strategy;
        }
    }
}
