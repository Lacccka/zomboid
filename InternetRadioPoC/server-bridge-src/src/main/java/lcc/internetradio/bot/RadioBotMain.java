package lcc.internetradio.bot;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

/** Child-process entrypoint for the vanilla FakeClient connection probe. */
public final class RadioBotMain {
    private static final long CONNECT_TIMEOUT_MS = 120_000L;
    private RadioBotMain() { }

    public static void main(String[] args) {
        if (args.length != 2) {
            log("ARGUMENT_ERROR", "expected: <scenario.json> <movementId>");
            System.exit(2);
        }
        Path scenario = Paths.get(args[0]).toAbsolutePath().normalize();
        String movementId = args[1];
        if (!Files.isRegularFile(scenario)) {
            log("SCENARIO_MISSING", scenario.toString());
            System.exit(3);
        }
        log("BOOT", "stage=connection-only; scenario=" + scenario
                + "; movementId=" + movementId + "; java=" + System.getProperty("java.version"));
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread fakeClient = new Thread(() -> runFakeClient(scenario, movementId, failure),
                "PZ-FakeClient-Main");
        fakeClient.setDaemon(false);
        fakeClient.start();
        try {
            monitorConnection(fakeClient, failure);
        } catch (Throwable error) {
            log("MONITOR_FAIL", describe(error));
            System.exit(11);
        }
    }

    private static void runFakeClient(Path scenario, String movementId,
            AtomicReference<Throwable> failure) {
        try {
            Class<?> manager = Class.forName("zombie.network.FakeClientManager");
            Method main = manager.getDeclaredMethod("main", String[].class);
            main.setAccessible(true);
            log("FAKECLIENT_ENTER", "loading RakNet64 and ZNetNoSteam64");
            main.invoke(null, (Object) new String[] {
                    "-scenarios=" + scenario, "-id=" + movementId });
            log("FAKECLIENT_RETURN", "vanilla main returned");
        } catch (InvocationTargetException invocation) {
            Throwable cause = invocation.getCause() == null ? invocation : invocation.getCause();
            failure.set(cause);
            log("FAKECLIENT_FAIL", describe(cause));
        } catch (Throwable error) {
            failure.set(error);
            log("FAKECLIENT_FAIL", describe(error));
        }
    }

    private static void monitorConnection(Thread fakeClient,
            AtomicReference<Throwable> failure) throws Exception {
        Class<?> manager = Class.forName("zombie.network.FakeClientManager");
        Method getGuid = manager.getMethod("getConnectedGUID");
        Method getOnlineId = manager.getMethod("getOnlineID");
        long startedAt = System.currentTimeMillis();
        long lastReport = 0L;
        long previousGuid = Long.MIN_VALUE;
        long previousOnlineId = Long.MIN_VALUE;
        while (true) {
            if (failure.get() != null) {
                log("CONNECTION_ABORTED", describe(failure.get()));
                System.exit(12);
            }
            if (!fakeClient.isAlive()) {
                log("CONNECTION_ABORTED", "FakeClient thread stopped before RUN state");
                System.exit(13);
            }
            long guid = ((Number) getGuid.invoke(null)).longValue();
            long onlineId = ((Number) getOnlineId.invoke(null)).longValue();
            long now = System.currentTimeMillis();
            if (guid != previousGuid || onlineId != previousOnlineId || now - lastReport >= 10_000L) {
                log("CONNECTION_STATE", "guid=" + guid + "; onlineId=" + onlineId
                        + "; elapsedMs=" + (now - startedAt));
                previousGuid = guid;
                previousOnlineId = onlineId;
                lastReport = now;
            }
            if (guid != -1L && guid != 0L && onlineId >= 0L) {
                log("CONNECTED", "authenticated=true; playerCreated=true; guid=" + guid
                        + "; onlineId=" + onlineId + "; voip=false; nextStage=client-rakvoice");
                return;
            }
            if (now - startedAt >= CONNECT_TIMEOUT_MS) {
                log("CONNECT_TIMEOUT", "elapsedMs=" + (now - startedAt)
                        + "; Steam/no-Steam compatibility remains unproven");
                System.exit(20);
            }
            Thread.sleep(500L);
        }
    }

    private static String describe(Throwable error) {
        Throwable root = error;
        while (root.getCause() != null) root = root.getCause();
        return root.getClass().getName()
                + (root.getMessage() == null ? "" : ": " + root.getMessage());
    }

    private static void log(String area, String message) {
        System.out.println("[" + area + "] " + message);
    }
}
