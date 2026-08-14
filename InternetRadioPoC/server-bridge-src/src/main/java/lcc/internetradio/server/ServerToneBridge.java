package lcc.internetradio.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import zombie.core.raknet.RakVoice;
import zombie.core.raknet.UdpConnection;
import zombie.core.raknet.UdpEngine;
import zombie.network.GameServer;

/** Server-only Phase 0 probe for RakVoice.SendFrame after RVInitServer. */
public final class ServerToneBridge {
    public static final String VERSION = "0.8.5";

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
        for (Object value : snapshot) {
            if (!(value instanceof UdpConnection)) continue;
            UdpConnection connection = (UdpConnection) value;
            if (!connection.isFullyConnected()) continue;
            short playerId = firstPlayerId(connection.playerIDs);
            if (playerId < 0) continue;
            long guid = connection.getConnectedGUID();
            if (guid == 0L) continue;
            result.add(new Target(guid, playerId));
        }
        return result;
    }

    private static short firstPlayerId(short[] playerIds) {
        if (playerIds == null) return -1;
        for (short playerId : playerIds) {
            if (playerId >= 0) return playerId;
        }
        return -1;
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
}
