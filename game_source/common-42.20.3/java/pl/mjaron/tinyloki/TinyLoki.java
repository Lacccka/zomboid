/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import pl.mjaron.tinyloki.IBuffering;
import pl.mjaron.tinyloki.IExecutor;
import pl.mjaron.tinyloki.ILogCollector;
import pl.mjaron.tinyloki.ILogEncoder;
import pl.mjaron.tinyloki.ILogMonitor;
import pl.mjaron.tinyloki.ILogSender;
import pl.mjaron.tinyloki.ILogStream;
import pl.mjaron.tinyloki.ITimestampProviderFactory;
import pl.mjaron.tinyloki.LabelSettings;
import pl.mjaron.tinyloki.Labels;
import pl.mjaron.tinyloki.LogSenderSettings;
import pl.mjaron.tinyloki.Settings;
import pl.mjaron.tinyloki.StreamBuilder;
import pl.mjaron.tinyloki.StreamSet;
import pl.mjaron.tinyloki.StreamSetBuilder;
import pl.mjaron.tinyloki.Utils;

public class TinyLoki
implements Closeable {
    public static final String API_PUSH = "loki/api/v1/push";
    public static final int DEFAULT_STOP_TIMEOUT = 1000;
    public static final int DEFAULT_SYNC_TIMEOUT = 1000;
    @Deprecated
    private static final int DEFAULT_SOFT_STOP_WAIT_TIME = 2000;
    @Deprecated
    private static final int DEFAULT_HARD_STOP_WAIT_TIME = 1000;
    private final ILogCollector logCollector;
    private final ILogEncoder logEncoder;
    private final ILogSender logSender;
    private final LabelSettings labelSettings;
    private final IExecutor executor;
    private final Labels staticLabels;
    private final ILogMonitor logMonitor;

    public static Settings withUrl(String url) {
        return Settings.fromUrl(url);
    }

    public static Settings withExactUrl(String url) {
        return Settings.fromExactUrl(url);
    }

    public static TinyLoki open(Settings settings) {
        return new TinyLoki(settings.getLogCollector(), settings.getLogEncoder(), settings.getLogSenderSettings(), settings.getLogSender(), settings.getLabelSettings(), settings.getStructuredMetadataLabelSettings(), settings.getExecutor(), settings.getBuffering(), settings.getTimestampProviderFactory(), settings.getLabels(), settings.getLogMonitor()).start();
    }

    public static TinyLoki open(String url, String user, String pass) {
        return TinyLoki.withUrl(url).withBasicAuth(user, pass).open();
    }

    public TinyLoki(ILogCollector logCollector, ILogEncoder logEncoder, LogSenderSettings logSenderSettings, ILogSender logSender, LabelSettings labelSettings, LabelSettings structuredMetadataLabelSettings, IExecutor executor, IBuffering bufferingManager, ITimestampProviderFactory timestampProviderFactory, Labels staticLabels, ILogMonitor logMonitor) {
        this.logCollector = logCollector;
        this.logEncoder = logEncoder;
        this.logSender = logSender;
        this.labelSettings = labelSettings;
        this.executor = executor;
        this.staticLabels = staticLabels;
        this.logMonitor = logMonitor;
        bufferingManager.configure(this.logCollector, 0, 0, this.executor, this.logMonitor);
        this.logCollector.configure(this.executor, bufferingManager, structuredMetadataLabelSettings, timestampProviderFactory);
        logSenderSettings.setContentType(this.logCollector.contentType());
        String contentEncoding = this.logEncoder == null ? null : this.logEncoder.contentEncoding();
        logSenderSettings.setContentEncoding(contentEncoding);
        this.logSender.configure(logSenderSettings, logMonitor);
        this.executor.configure(logCollector, this::internalProcessLogs, logMonitor);
        this.logMonitor.onConfigured(this.logCollector.contentType(), contentEncoding);
    }

    public ILogMonitor getLogMonitor() {
        return this.logMonitor;
    }

    public ILogCollector getLogCollector() {
        return this.logCollector;
    }

    public IExecutor getExecutor() {
        return this.executor;
    }

    public Labels getLabels() {
        return this.staticLabels;
    }

    public ILogStream openStream(Map<String, String> labels) {
        return this.openStream(Labels.of(labels));
    }

    public ILogStream openStream(Labels labels) {
        Labels finalLabels = this.staticLabels.isEmpty() ? labels : (labels.isEmpty() ? this.staticLabels : Labels.of(this.staticLabels).l(labels));
        return this.logCollector.createStream(Labels.prettify(finalLabels, this.labelSettings));
    }

    public StreamBuilder stream() {
        return new StreamBuilder(this);
    }

    public StreamSetBuilder streamSet() {
        return new StreamSetBuilder(this);
    }

    public StreamSet openStreamSet(Labels labels) {
        return new StreamSet(this, labels);
    }

    public TinyLoki start() {
        try {
            this.executor.start();
            this.logMonitor.onStart();
        }
        catch (Exception e) {
            this.logMonitor.onException(e);
            throw e;
        }
        return this;
    }

    public boolean stop(int timeout2) throws InterruptedException {
        try {
            if (timeout2 <= 0) {
                throw new IllegalArgumentException("Cannot stop with timeout <= 0: [" + timeout2 + "].");
            }
            boolean result = this.executor.stop(timeout2);
            this.logMonitor.onStop(result);
            return result;
        }
        catch (InterruptedException e) {
            throw e;
        }
        catch (Exception e2) {
            this.logMonitor.onException(e2);
            return false;
        }
    }

    public boolean stop() throws InterruptedException {
        return this.stop(1000);
    }

    public boolean sync(int timeout2) throws InterruptedException {
        try {
            if (timeout2 <= 0) {
                throw new IllegalArgumentException("Cannot sync with timeout <= 0: [" + timeout2 + "].");
            }
            boolean result = this.executor.sync(timeout2);
            this.logMonitor.onSync(result);
            return result;
        }
        catch (InterruptedException e) {
            throw e;
        }
        catch (Exception e2) {
            this.logMonitor.onException(e2);
            return false;
        }
    }

    public boolean sync() throws InterruptedException {
        return this.sync(1000);
    }

    public boolean closeSync(int syncTimeout, int stopTimeout) throws InterruptedException {
        InterruptedException syncInterruptedException = null;
        int finalStopTimeout = stopTimeout;
        boolean syncSuccess = false;
        boolean stopSuccess = false;
        try {
            syncSuccess = this.sync(syncTimeout);
        }
        catch (InterruptedException e) {
            syncInterruptedException = e;
            finalStopTimeout = Math.min(stopTimeout, 10);
        }
        stopSuccess = this.stop(finalStopTimeout);
        if (syncInterruptedException != null) {
            InterruptedException finalException = new InterruptedException("The closeSync() has been interrupted during sync. Trying to stop with result: " + stopSuccess);
            finalException.initCause(syncInterruptedException);
            throw finalException;
        }
        return syncSuccess && stopSuccess;
    }

    public boolean closeSync(int timeout2) throws InterruptedException {
        return this.closeSync(timeout2 / 2, timeout2 / 2);
    }

    public boolean closeSync() throws InterruptedException {
        return this.closeSync(1000, 1000);
    }

    protected void internalProcessLogs() throws InterruptedException {
        byte[][] buffers = this.logCollector.collectAll();
        if (buffers == null) {
            if (this.logMonitor.isVerbose()) {
                this.logMonitor.logVerbose("Buffers count: [0] (null).");
            }
            return;
        }
        if (this.logMonitor.isVerbose()) {
            this.logMonitor.logVerbose("Buffers count: [" + buffers.length + "].");
        }
        for (byte[] logs : buffers) {
            byte[] toSend;
            if (this.logEncoder == null) {
                toSend = logs;
            } else {
                try {
                    toSend = this.logEncoder.encode(logs);
                }
                catch (IOException e) {
                    throw new RuntimeException("Failed to encode logs.", e);
                }
                this.logMonitor.onEncoded(logs, toSend);
            }
            try {
                this.logSender.send(toSend);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public TinyLoki stopAsync() {
        this.executor.stopAsync();
        return this;
    }

    @Deprecated
    public TinyLoki hardStop(long interruptTimeout) {
        try {
            this.stop(Utils.clampToInt(interruptTimeout));
        }
        catch (Exception e) {
            this.logMonitor.onException(e);
        }
        return this;
    }

    @Deprecated
    public TinyLoki hardStop() {
        return this.hardStop(1000L);
    }

    @Deprecated
    public TinyLoki softStop(long softTimeout) {
        try {
            int normalizedTimeout = Utils.clampToInt(softTimeout);
            this.closeSync(normalizedTimeout);
        }
        catch (Exception e) {
            this.logMonitor.onException(e);
        }
        return this;
    }

    @Deprecated
    public TinyLoki softStop() {
        return this.softStop(2000L);
    }

    @Deprecated
    public boolean isSoftStopped() {
        return true;
    }

    @Override
    public void close() {
        this.stopAsync();
    }

    protected void finalize() {
        this.stopAsync();
    }
}

