package lcc.internetradio.server;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.core.raknet.UdpEngine;
import zombie.network.GameServer;
import zombie.scripting.ScriptManager;

/** Starts the isolated, server-managed RadioBot connection probe. */
public final class ServerToneBridge {
    public static final String VERSION = "0.10.0";
    private static final long POLL_PERIOD_MS = 500L;
    private static final long PLAYER_SETTLE_MS = 5_000L;
    private static final AtomicBoolean BOOTSTRAPPED = new AtomicBoolean();
    private static final AtomicBoolean ENGINE_WAIT_LOGGED = new AtomicBoolean();
    private static final AtomicBoolean PLAYER_WAIT_LOGGED = new AtomicBoolean();
    private static volatile long firstPlayerSeenAt;
    private static volatile boolean stopped;

    private ServerToneBridge() { }

    public static void bootstrap() {
        if (!BOOTSTRAPPED.compareAndSet(false, true)) return;
        log("BOOT", "version=" + VERSION
                + "; environment=dedicated-server"
                + "; transport=isolated-radio-bot"
                + "; stage=connection-only; frequency=104.6");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stopped = true;
            RadioBotProcess.stop();
        }, "LCC-RadioBot-Shutdown"));
        Thread monitor = new Thread(ServerToneBridge::monitorLoop,
                "LCC-RadioBot-Supervisor");
        monitor.setDaemon(true);
        monitor.setUncaughtExceptionHandler((thread, error) ->
                log("SUPERVISOR_FAIL", describe(error)));
        monitor.start();
    }

    private static void monitorLoop() {
        log("SUPERVISOR_OK", "pollPeriodMs=" + POLL_PERIOD_MS);
        while (!stopped) {
            tryStartBot();
            sleep(POLL_PERIOD_MS);
        }
    }

    private static void tryStartBot() {
        if (RadioBotProcess.wasStartAttempted()) return;
        UdpEngine engine = GameServer.udpEngine;
        if (engine == null) {
            if (ENGINE_WAIT_LOGGED.compareAndSet(false, true))
                log("WAIT", "GameServer.udpEngine is not initialized yet");
            return;
        }
        IsoPlayer player = firstConnectedPlayer(engine.connections);
        if (player == null) {
            firstPlayerSeenAt = 0L;
            if (PLAYER_WAIT_LOGGED.compareAndSet(false, true))
                log("WAIT", "join one ordinary player to start RadioBot Stage 1");
            return;
        }
        long now = System.currentTimeMillis();
        if (firstPlayerSeenAt == 0L) {
            firstPlayerSeenAt = now;
            log("PLAYER_READY", "onlineId=" + player.getOnlineID()
                    + "; x=" + player.getX() + "; y=" + player.getY()
                    + "; botStartInMs=" + PLAYER_SETTLE_MS);
            return;
        }
        if (now - firstPlayerSeenAt < PLAYER_SETTLE_MS) return;

        String luaChecksum = nullToEmpty(GameServer.checksum);
        String scriptChecksum = "";
        try {
            if (ScriptManager.instance != null)
                scriptChecksum = nullToEmpty(ScriptManager.instance.getChecksum());
        } catch (Throwable error) {
            log("CHECKSUM_WARN", "script checksum unavailable; " + describe(error));
        }
        log("CHECKSUM", "luaPresent=" + !luaChecksum.isEmpty()
                + "; scriptPresent=" + !scriptChecksum.isEmpty());
        RadioBotProcess.start(Math.round(player.getX()), Math.round(player.getY()),
                luaChecksum, scriptChecksum);
    }

    private static IsoPlayer firstConnectedPlayer(List<UdpConnection> connections) {
        if (connections == null) return null;
        for (Object value : connections.toArray()) {
            if (!(value instanceof UdpConnection)) continue;
            UdpConnection connection = (UdpConnection) value;
            if (!connection.isFullyConnected() || connection.players == null) continue;
            for (IsoPlayer player : connection.players)
                if (player != null && player.getOnlineID() >= 0) return player;
        }
        return null;
    }

    private static void sleep(long millis) {
        try { Thread.sleep(millis); }
        catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            stopped = true;
        }
    }

    private static String nullToEmpty(String value) { return value == null ? "" : value; }

    static String describe(Throwable error) {
        Throwable root = error;
        while (root.getCause() != null) root = root.getCause();
        return root.getClass().getName()
                + (root.getMessage() == null ? "" : ": " + root.getMessage());
    }

    static void log(String area, String message) {
        System.out.println("[InternetRadioBridge][" + area + "] " + message);
    }
}
