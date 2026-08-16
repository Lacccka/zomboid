package lcc.internetradio.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Owns the child JVM so native client failures cannot terminate the server. */
final class RadioBotProcess {
    private static final int BOT_ID = 1046;
    private static final AtomicBoolean START_ATTEMPTED = new AtomicBoolean();
    private static volatile Process process;

    private RadioBotProcess() { }
    static boolean wasStartAttempted() { return START_ATTEMPTED.get(); }

    static void start(int spawnX, int spawnY, String luaChecksum, String scriptChecksum) {
        if (!START_ATTEMPTED.compareAndSet(false, true)) return;
        try {
            Path root = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
            Path gameJar = root.resolve("java").resolve("projectzomboid.jar");
            Path natives = root.resolve("natives");
            Path bridgeJar = ownJar();
            Path runtimeDir = root.resolve(".leaf").resolve("radio-bot");
            Path scenario = runtimeDir.resolve("stage1-scenario.json");
            requireFile(gameJar, "Project Zomboid game JAR");
            requireFile(bridgeJar, "Internet Radio bridge JAR");
            if (!Files.isDirectory(natives))
                throw new IOException("native library directory is missing: " + natives);
            Files.createDirectories(runtimeDir);
            Files.writeString(scenario,
                    scenarioJson(spawnX, spawnY, luaChecksum, scriptChecksum),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

            List<String> command = new ArrayList<>();
            command.add(javaExecutable().toString());
            command.add("-Djava.awt.headless=true");
            command.add("-Dzomboid.steam=0");
            command.add("-Djava.library.path=" + natives);
            command.add("-cp");
            command.add(gameJar + System.getProperty("path.separator") + bridgeJar);
            command.add("lcc.internetradio.bot.RadioBotMain");
            command.add(scenario.toString());
            command.add(Integer.toString(BOT_ID));
            ServerToneBridge.log("BOT_START", "mode=child-jvm; id=" + BOT_ID
                    + "; spawn=" + spawnX + "," + spawnY
                    + "; java=" + javaExecutable() + "; scenario=" + scenario);
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.directory(root.toFile());
            builder.redirectErrorStream(true);
            process = builder.start();
            pumpOutput(process);
            watchExit(process);
            ServerToneBridge.log("BOT_PROCESS", "pid=" + process.pid() + "; started=true");
        } catch (Throwable error) {
            ServerToneBridge.log("BOT_START_FAIL", ServerToneBridge.describe(error));
        }
    }

    static void stop() {
        Process current = process;
        if (current == null || !current.isAlive()) return;
        ServerToneBridge.log("BOT_STOP", "pid=" + current.pid());
        current.destroy();
        try {
            if (!current.waitFor(5, TimeUnit.SECONDS)) current.destroyForcibly();
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            current.destroyForcibly();
        }
    }

    private static void pumpOutput(Process child) {
        Thread output = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    child.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null)
                    System.out.println("[InternetRadioBot] " + line);
            } catch (IOException error) {
                if (child.isAlive()) ServerToneBridge.log("BOT_OUTPUT_FAIL",
                        ServerToneBridge.describe(error));
            }
        }, "LCC-RadioBot-Output");
        output.setDaemon(true);
        output.start();
    }

    private static void watchExit(Process child) {
        Thread watcher = new Thread(() -> {
            try {
                int exitCode = child.waitFor();
                ServerToneBridge.log("BOT_EXIT", "pid=" + child.pid()
                        + "; exitCode=" + exitCode + "; automaticRestart=false");
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        }, "LCC-RadioBot-Watcher");
        watcher.setDaemon(true);
        watcher.start();
    }

    private static Path ownJar() throws URISyntaxException {
        return Paths.get(RadioBotProcess.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).toAbsolutePath().normalize();
    }

    private static Path javaExecutable() {
        String executable = System.getProperty("os.name", "").toLowerCase().contains("win")
                ? "java.exe" : "java";
        return Paths.get(System.getProperty("java.home"), "bin", executable)
                .toAbsolutePath().normalize();
    }

    private static void requireFile(Path path, String label) throws IOException {
        if (!Files.isRegularFile(path)) throw new IOException(label + " is missing: " + path);
    }

    private static String scenarioJson(int x, int y, String lua, String script) {
        return "{\n"
                + "  \"version\": \"1.0\",\n"
                + "  \"config\": {\"client\": {\n"
                + "    \"connection\": {\"serverHost\": \"127.0.0.1\", \"interval\": 1000, \"timeout\": 15000, \"delay\": 3000},\n"
                + "    \"statistics\": {\"period\": 1, \"id\": -1},\n"
                + "    \"checksum\": {\"lua\": \"" + json(lua) + "\", \"script\": \"" + json(script) + "\"},\n"
                + "    \"player\": {\"fps\": 10, \"predict\": 1000, \"damage\": 0.0, \"voip\": false},\n"
                + "    \"movement\": {\"radius\": 0, \"motion\": {\"aim\": 0, \"sneak\": 0, \"sneakrun\": 0, \"walk\": 0, \"run\": 0, \"sprint\": 0, \"pedestrian\": {\"min\": 0, \"max\": 0}, \"vehicle\": {\"min\": 0, \"max\": 0}}}\n"
                + "  }},\n"
                + "  \"movements\": [{\"id\": " + BOT_ID
                + ", \"description\": \"LCC Internet Radio Stage 1\""
                + ", \"spawn\": {\"x\": " + x + ", \"y\": " + y + "}"
                + ", \"destination\": {\"x\": " + x + ", \"y\": " + y + "}"
                + ", \"motion\": \"Pedestrian\", \"speed\": 0, \"type\": \"Stay\""
                + ", \"radius\": 0, \"direction\": \"N\", \"ghost\": true"
                + ", \"connect\": 0, \"disconnect\": 0, \"reconnect\": 0, \"teleport\": 0}]\n"
                + "}\n";
    }

    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n");
    }
}
