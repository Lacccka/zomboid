package lcc.internetradio.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import zombie.characters.IsoPlayer;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.RakVoice;
import zombie.core.raknet.UdpConnection;
import zombie.core.raknet.UdpEngine;
import zombie.iso.IsoCell;
import zombie.network.GameServer;
import zombie.network.PacketTypes;

/**
 * Server-only synthetic radio sender probe for Project Zomboid B42.20.2.
 *
 * <p>The server announces a minimal remote player identity to each recipient,
 * gives that identity a vanilla radio route on 104.6 MHz, and sends generated
 * PCM under the synthetic online ID. This tests whether a real network client
 * is required or whether a lightweight server-native station identity is
 * sufficient.</p>
 */
public final class ServerToneBridge {
    public static final String VERSION = "0.9.2";

    private static final int RADIO_FREQUENCY = 104_600;
    private static final int RADIO_RANGE = 30_000;
    private static final short SYNTHETIC_ONLINE_ID = 3_000;
    private static final String SYNTHETIC_USERNAME = "[Radio] WIVK-FM 104.6";

    private static final long MONITOR_PERIOD_MS = 250L;
    private static final long CLIENT_SETTLE_MS = 5_000L;
    private static final long IDENTITY_SETTLE_MS = 2_000L;
    private static final long TONE_INTERVAL_MS = 30_000L;
    private static final long TONE_DURATION_MS = 6_000L;

    private static final AtomicBoolean BOOTSTRAPPED = new AtomicBoolean();
    private static final AtomicBoolean MONITOR_STARTED = new AtomicBoolean();
    private static final AtomicBoolean ENGINE_WAIT_LOGGED = new AtomicBoolean();
    private static final AtomicBoolean CONNECTION_WAIT_LOGGED = new AtomicBoolean();
    private static final AtomicBoolean VOICE_STATE_LOGGED = new AtomicBoolean();
    private static final AtomicBoolean VOICE_DISABLED_LOGGED = new AtomicBoolean();
    private static final AtomicBoolean DIRECT_ROUTE_BLOCKED_LOGGED = new AtomicBoolean();
    private static final AtomicBoolean SYNTHETIC_ID_VALIDATED = new AtomicBoolean();

    private static final ConcurrentMap<Long, RecipientState> RECIPIENTS =
            new ConcurrentHashMap<>();
    private static final PcmSource TEST_SOURCE = new TonePcmSource(440.0, 0.20);

    private static volatile boolean disabled;

    private ServerToneBridge() {
    }

    /** Called by Leaf's deterministic server entrypoint. */
    public static void bootstrap() {
        if (!BOOTSTRAPPED.compareAndSet(false, true)) return;
        log("BOOT", "version=" + VERSION
                + "; environment=dedicated-server"
                + "; transport=synthetic-radio-sender"
                + "; frequency=" + formatFrequency(RADIO_FREQUENCY)
                + "; onlineId=" + SYNTHETIC_ONLINE_ID);

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

        while (!disabled) {
            pollOnce();
            sleep(MONITOR_PERIOD_MS);
        }
    }

    private static void pollOnce() {
        try {
            UdpEngine engine = GameServer.udpEngine;
            if (engine == null) {
                if (ENGINE_WAIT_LOGGED.compareAndSet(false, true)) {
                    log("WAIT", "GameServer.udpEngine is not initialized yet");
                }
                return;
            }

            List<Target> targets = snapshotTargets(engine.connections);
            if (targets.isEmpty()) {
                if (CONNECTION_WAIT_LOGGED.compareAndSet(false, true)) {
                    log("WAIT", "no fully-connected player is available");
                }
                return;
            }

            logVoiceStateOnce();
            if (disabled || !RakVoice.GetServerVOIPEnable()) {
                if (VOICE_DISABLED_LOGGED.compareAndSet(false, true)) {
                    log("WAIT", "server VOIP is disabled; synthetic probe paused");
                }
                return;
            }

            if (DIRECT_ROUTE_BLOCKED_LOGGED.compareAndSet(false, true)) {
                log("DIRECT_TEST_BLOCKED",
                        "RakVoice.SendFrame is disabled in dedicated-server "
                                + "context; B42.20.2 terminates the native "
                                + "server process; nextTransport=server-radio-bot");
            }
            return;
        } catch (Throwable error) {
            fail("MONITOR_POLL", error);
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

            IsoPlayer player = firstPlayer(connection.players);
            if (player == null) continue;
            long guid = connection.getConnectedGUID();
            if (guid == 0L || guid == -1L) continue;
            result.add(new Target(connection, player, guid));
        }
        return result;
    }

    private static IsoPlayer firstPlayer(IsoPlayer[] players) {
        if (players == null) return null;
        for (IsoPlayer player : players) {
            if (player != null && player.getOnlineID() >= 0) return player;
        }
        return null;
    }

    private static void validateSyntheticId(List<Target> targets) {
        if (SYNTHETIC_ID_VALIDATED.get()) return;
        for (Target target : targets) {
            if (target.player.getOnlineID() == SYNTHETIC_ONLINE_ID) {
                fail("SYNTHETIC_ID", new IllegalStateException(
                        "reserved onlineID collides with a connected player: "
                                + SYNTHETIC_ONLINE_ID));
                return;
            }
        }
        if (GameServer.IDToPlayerMap != null
                && GameServer.IDToPlayerMap.containsKey(SYNTHETIC_ONLINE_ID)) {
            fail("SYNTHETIC_ID", new IllegalStateException(
                    "reserved onlineID already exists in GameServer.IDToPlayerMap: "
                            + SYNTHETIC_ONLINE_ID));
            return;
        }
        if (SYNTHETIC_ID_VALIDATED.compareAndSet(false, true)) {
            log("SYNTHETIC_ID", "reserved onlineId=" + SYNTHETIC_ONLINE_ID
                    + "; collision=false");
        }
    }

    private static void advanceRecipient(
            Target target, RecipientState state, long now) {
        if (!state.identityAnnounced) {
            if (now - state.connectedAt < CLIENT_SETTLE_MS) return;
            announceSyntheticIdentity(target, state);
            return;
        }

        if (now - state.identityAnnouncedAt < IDENTITY_SETTLE_MS) return;
        if (state.sending.get()) return;
        if (state.lastToneStartedAt != 0L
                && now - state.lastToneStartedAt < TONE_INTERVAL_MS) return;
        if (!state.sending.compareAndSet(false, true)) return;

        state.lastToneStartedAt = now;
        Thread worker = new Thread(
                () -> sendTone(target, state),
                "LCC-RadioSender-" + target.guid);
        worker.setDaemon(true);
        worker.setUncaughtExceptionHandler((thread, error) -> {
            state.sending.set(false);
            failRecipient(target.guid, "WORKER", error);
        });
        worker.start();
    }

    private static void announceSyntheticIdentity(
            Target target, RecipientState state) {
        try {
            IsoCell cell = IsoCell.getInstance();
            if (cell == null) {
                logRecipient(target.guid, "WAIT", "IsoCell is not initialized");
                return;
            }

            IsoPlayer ghost = new IsoPlayer(cell);
            ghost.onlineId = SYNTHETIC_ONLINE_ID;
            ghost.remote = true;
            ghost.username = SYNTHETIC_USERNAME;
            ghost.setX(target.player.getX());
            ghost.setY(target.player.getY());
            ghost.setZ(target.player.getZ());

            logRecipient(target.guid, "IDENTITY_ENTER",
                    "onlineId=" + SYNTHETIC_ONLINE_ID
                            + "; username=" + SYNTHETIC_USERNAME
                            + "; x=" + ghost.getX()
                            + "; y=" + ghost.getY());
            GameServer.sendPlayerConnected(ghost, target.connection);
            logRecipient(target.guid, "IDENTITY_RETURN",
                    "GameServer.sendPlayerConnected returned");

            sendRadioData(target, ghost);
            state.ghost = ghost;
            state.identityAnnounced = true;
            state.identityAnnouncedAt = System.currentTimeMillis();
            logRecipient(target.guid, "IDENTITY_READY",
                    "synthetic sender announced; frequency="
                            + formatFrequency(RADIO_FREQUENCY));
        } catch (Throwable error) {
            failRecipient(target.guid, "IDENTITY", error);
        }
    }

    private static void sendRadioData(Target target, IsoPlayer ghost) {
        logRecipient(target.guid, "RADIO_ROUTE_ENTER",
                "onlineId=" + SYNTHETIC_ONLINE_ID
                        + "; frequency=" + RADIO_FREQUENCY
                        + "; range=" + RADIO_RANGE);
        ByteBufferWriter writer = target.connection.startPacket();
        PacketTypes.PacketType.SyncRadioData.doPacket(writer);
        writer.putShort(SYNTHETIC_ONLINE_ID);
        writer.putBoolean(false);
        writer.putInt(4);
        writer.putInt(RADIO_FREQUENCY);
        writer.putInt(RADIO_RANGE);
        writer.putInt((int) ghost.getX());
        writer.putInt((int) ghost.getY());
        PacketTypes.PacketType.SyncRadioData.send(target.connection);
        logRecipient(target.guid, "RADIO_ROUTE_RETURN",
                "SyncRadioData sent; values=4");
    }

    private static void sendTone(Target target, RecipientState state) {
        try {
            state.failed = true;
            logRecipient(target.guid, "DIRECT_TEST_BLOCKED",
                    "native SendFrame removed; nextTransport=server-radio-bot");
        } catch (Throwable error) {
            failRecipient(target.guid, "SYNTHETIC_TEST", error);
        } finally {
            state.sending.set(false);
        }
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

    private static void sleepUntil(long deadlineNanos) {
        long remaining;
        while ((remaining = deadlineNanos - System.nanoTime()) > 0L) {
            long millis = remaining / 1_000_000L;
            int nanos = (int) (remaining % 1_000_000L);
            try {
                Thread.sleep(millis, nanos);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private static int calculatePcmFrameBytes(
            int sampleRate, int framePeriodMs) {
        if (sampleRate <= 0 || framePeriodMs <= 0) return 0;
        long sampleMillis = (long) sampleRate * framePeriodMs;
        if (sampleMillis % 1_000L != 0L) {
            throw new IllegalStateException(
                    "voice frame does not contain a whole PCM sample count: "
                            + "sampleRate=" + sampleRate
                            + "; periodMs=" + framePeriodMs);
        }
        long bytes = sampleMillis / 1_000L * Short.BYTES;
        if (bytes > Integer.MAX_VALUE) {
            throw new IllegalStateException(
                    "derived voice frame is too large: " + bytes);
        }
        return (int) bytes;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            disabled = true;
        }
    }

    private static String formatFrequency(int frequency) {
        return String.format("%.1f", frequency / 1_000.0);
    }

    private static void fail(String area, Throwable error) {
        disabled = true;
        log(area, "FAIL; bridge disabled; " + describe(error));
    }

    private static void failRecipient(long guid, String area, Throwable error) {
        RecipientState state = RECIPIENTS.get(guid);
        if (state != null) state.failed = true;
        logRecipient(guid, area, "FAIL; recipient disabled; " + describe(error));
    }

    private static String describe(Throwable error) {
        Throwable root = error;
        while (root.getCause() != null) root = root.getCause();
        return root.getClass().getName()
                + (root.getMessage() == null ? "" : ": " + root.getMessage());
    }

    private static void logRecipient(long guid, String area, String message) {
        log(area, "recipientGuid=" + guid + "; " + message);
    }

    private static void log(String area, String message) {
        System.out.println("[InternetRadioBridge][" + area + "] " + message);
    }

    private static final class Target {
        private final UdpConnection connection;
        private final IsoPlayer player;
        private final long guid;

        private Target(UdpConnection connection, IsoPlayer player, long guid) {
            this.connection = connection;
            this.player = player;
            this.guid = guid;
        }
    }

    private static final class RecipientState {
        private final long connectedAt;
        private final AtomicBoolean sending = new AtomicBoolean();
        private volatile boolean identityAnnounced;
        private volatile boolean failed;
        private volatile long identityAnnouncedAt;
        private volatile long lastToneStartedAt;
        private volatile IsoPlayer ghost;

        private RecipientState(long connectedAt) {
            this.connectedAt = connectedAt;
        }
    }
}
