package lcc.internetradio.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import zombie.core.raknet.RakVoice;
import zombie.core.raknet.UdpConnection;

/** Server-only RakVoice capability probe. */
public final class ServerToneBridge {
    public static final String VERSION = "0.8.0";

    private static final long TEST_DELAY_MS = 5_000L;
    private static final long TARGET_EXPIRY_MS = 15_000L;
    private static final long TONE_DURATION_MS = 3_000L;
    private static final double TONE_HZ = 440.0;
    private static final double AMPLITUDE = 0.12;
    private static final Map<Long, Target> TARGETS = new ConcurrentHashMap<>();
    private static final AtomicBoolean STARTED = new AtomicBoolean();
    private static final AtomicBoolean CAPABILITIES_LOGGED = new AtomicBoolean();
    private static volatile boolean disabled;

    private ServerToneBridge() {
    }

    /** Called after vanilla updates VOIP routing for a connection. */
    public static void observe(UdpConnection connection) {
        if (disabled || connection == null) return;
        try {
            if (!connection.isFullyConnected()) return;
            short playerId = firstPlayerId(connection.playerIDs);
            if (playerId < 0) return;

            long now = System.currentTimeMillis();
            long guid = connection.getConnectedGUID();
            TARGETS.compute(guid, (ignored, previous) -> {
                if (previous == null) return new Target(guid, playerId, now);
                previous.playerId = playerId;
                previous.lastSeenAt = now;
                return previous;
            });
            logCapabilitiesOnce();
            startWorkerOnce();
        } catch (Throwable error) {
            fail("OBSERVE", error);
        }
    }

    private static short firstPlayerId(short[] playerIds) {
        if (playerIds == null) return -1;
        for (short playerId : playerIds) {
            if (playerId >= 0) return playerId;
        }
        return -1;
    }

    private static void logCapabilitiesOnce() {
        if (!CAPABILITIES_LOGGED.compareAndSet(false, true)) return;
        try {
            log("VOICE", "RakVoice initialized"
                    + "; enabled=" + RakVoice.GetServerVOIPEnable()
                    + "; sampleRate=" + RakVoice.GetSampleRate()
                    + "; frameBytes=" + RakVoice.GetBufferSizeBytes()
                    + "; framePeriodMs=" + RakVoice.GetSendFramePeriod()
                    + "; buffering=" + RakVoice.GetBuffering()
                    + "; is3D=" + RakVoice.GetIs3D()
                    + "; minDistance=" + RakVoice.GetMinDistance()
                    + "; maxDistance=" + RakVoice.GetMaxDistance());
        } catch (Throwable error) {
            fail("VOICE", error);
        }
    }

    private static void startWorkerOnce() {
        if (disabled || !STARTED.compareAndSet(false, true)) return;
        Thread worker = new Thread(ServerToneBridge::workerLoop,
                "LCC-InternetRadio-ToneProbe");
        worker.setDaemon(true);
        worker.setUncaughtExceptionHandler((thread, error) -> fail("WORKER", error));
        worker.start();
        log("TEST", "tone worker started; version=" + VERSION);
    }

    private static void workerLoop() {
        while (!disabled) {
            long now = System.currentTimeMillis();
            for (Target target : TARGETS.values()) {
                if (now - target.lastSeenAt > TARGET_EXPIRY_MS) {
                    TARGETS.remove(target.guid, target);
                    continue;
                }
                if (!target.sent && now - target.firstSeenAt >= TEST_DELAY_MS) {
                    target.sent = true;
                    sendTone(target);
                }
            }
            sleep(50L);
        }
    }

    private static void sendTone(Target target) {
        try {
            int sampleRate = RakVoice.GetSampleRate();
            int frameBytes = RakVoice.GetBufferSizeBytes();
            int framePeriodMs = RakVoice.GetSendFramePeriod();
            if (sampleRate <= 0 || frameBytes < 2 || framePeriodMs <= 0) {
                log("TEST", "FAIL invalid voice format"
                        + "; sampleRate=" + sampleRate
                        + "; frameBytes=" + frameBytes
                        + "; framePeriodMs=" + framePeriodMs);
                return;
            }

            // The vanilla capture path consumes mono signed 16-bit PCM. This
            // runtime test also validates that assumption for B42.20.2.
            byte[] frame = new byte[frameBytes];
            int samplesPerFrame = frameBytes / 2;
            int framesToSend = Math.max(1,
                    (int) Math.ceil((double) TONE_DURATION_MS / framePeriodMs));
            double phase = 0.0;
            double phaseStep = 2.0 * Math.PI * TONE_HZ / sampleRate;
            log("TEST", "440Hz generation started"
                    + "; guid=" + target.guid
                    + "; sourcePlayerId=" + target.playerId
                    + "; frames=" + framesToSend);

            int sent = 0;
            long nextFrameAt = System.nanoTime();
            for (int frameIndex = 0; frameIndex < framesToSend && !disabled; frameIndex++) {
                for (int sampleIndex = 0; sampleIndex < samplesPerFrame; sampleIndex++) {
                    short sample = (short) Math.round(
                            Math.sin(phase) * Short.MAX_VALUE * AMPLITUDE);
                    int byteIndex = sampleIndex * 2;
                    frame[byteIndex] = (byte) (sample & 0xff);
                    frame[byteIndex + 1] = (byte) ((sample >>> 8) & 0xff);
                    phase += phaseStep;
                    if (phase >= 2.0 * Math.PI) phase -= 2.0 * Math.PI;
                }
                RakVoice.SendFrame(target.guid, target.playerId, frame, frame.length);
                sent++;
                nextFrameAt += framePeriodMs * 1_000_000L;
                sleepUntil(nextFrameAt);
            }
            log("TEST", "tone finished; framesSent=" + sent
                    + "; guid=" + target.guid
                    + "; sourcePlayerId=" + target.playerId);
        } catch (Throwable error) {
            fail("TEST", error);
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

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            disabled = true;
        }
    }

    private static void fail(String area, Throwable error) {
        disabled = true;
        Throwable root = error;
        while (root.getCause() != null) root = root.getCause();
        log(area, "FAIL; bridge disabled; " + root.getClass().getSimpleName()
                + (root.getMessage() == null ? "" : ": " + root.getMessage()));
    }

    private static void log(String area, String message) {
        System.out.println("[InternetRadioBridge][" + area + "] " + message);
    }

    private static final class Target {
        private final long guid;
        private final long firstSeenAt;
        private volatile short playerId;
        private volatile long lastSeenAt;
        private volatile boolean sent;

        private Target(long guid, short playerId, long now) {
            this.guid = guid;
            this.playerId = playerId;
            this.firstSeenAt = now;
            this.lastSeenAt = now;
        }
    }
}
