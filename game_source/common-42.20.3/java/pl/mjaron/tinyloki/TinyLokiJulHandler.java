/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import pl.mjaron.tinyloki.ILogStream;
import pl.mjaron.tinyloki.Labels;
import pl.mjaron.tinyloki.Settings;
import pl.mjaron.tinyloki.TinyLoki;

public class TinyLokiJulHandler
extends Handler {
    private final TinyLoki loki;
    private final Map<StreamKey, ILogStream> streams = new HashMap<StreamKey, ILogStream>();

    public static Logger getJulRootLogger() {
        return LogManager.getLogManager().getLogger("");
    }

    public static TinyLokiJulHandler install(TinyLoki loki) {
        Logger rootLogger = TinyLokiJulHandler.getJulRootLogger();
        TinyLokiJulHandler handler = new TinyLokiJulHandler(loki);
        rootLogger.addHandler(handler);
        return handler;
    }

    public static TinyLokiJulHandler install(Settings settings) {
        return TinyLokiJulHandler.install(settings.open());
    }

    public static TinyLokiJulHandler install(String url) {
        return TinyLokiJulHandler.install(TinyLoki.withUrl(url));
    }

    public static TinyLokiJulHandler install(String url, String user, String pass) {
        return TinyLokiJulHandler.install(TinyLoki.withUrl(url).withBasicAuth(user, pass));
    }

    public void uninstall() {
        TinyLokiJulHandler.getJulRootLogger().removeHandler(this);
    }

    public static void setJulLogLevel(Level newLevel) {
        Logger rootLogger = TinyLokiJulHandler.getJulRootLogger();
        rootLogger.setLevel(newLevel);
        Arrays.stream(rootLogger.getHandlers()).forEach(h -> h.setLevel(newLevel));
    }

    public static String toTinyLokiLogLevel(Level level) {
        int value = level.intValue();
        if (value >= 1000) {
            return "critical";
        }
        if (value >= 900) {
            return "warning";
        }
        if (value >= 800) {
            return "info";
        }
        if (value >= 700) {
            return "debug";
        }
        return "trace";
    }

    public TinyLokiJulHandler(TinyLoki loki) {
        this.loki = loki;
    }

    public TinyLokiJulHandler(Settings settings) {
        this.loki = TinyLoki.open(settings);
    }

    public TinyLoki getLoki() {
        return this.loki;
    }

    @Override
    public synchronized void publish(LogRecord logRecord) {
        StreamKey key = new StreamKey(logRecord.getLoggerName(), logRecord.getLevel());
        ILogStream stream = this.streams.get(key);
        if (stream == null) {
            Labels streamLabels = Labels.of().l("tag", logRecord.getLoggerName()).l("level", TinyLokiJulHandler.toTinyLokiLogLevel(logRecord.getLevel()));
            stream = this.loki.openStream(streamLabels);
            this.streams.put(key, stream);
        }
        stream.log(logRecord.getMessage());
    }

    @Override
    public void flush() {
        try {
            this.loki.sync();
        }
        catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() throws SecurityException {
        try {
            this.loki.closeSync();
        }
        catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private static class StreamKey {
        final String tag;
        final Level level;
        final int hash;

        private StreamKey(String tag, Level level) {
            this.tag = tag;
            this.level = level;
            this.hash = Objects.hash(tag, level);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            StreamKey that = (StreamKey)o;
            return this.tag.equals(that.tag) && this.level == that.level;
        }

        public int hashCode() {
            return this.hash;
        }
    }
}

