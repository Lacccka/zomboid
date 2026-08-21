/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import pl.mjaron.tinyloki.IExecutor;
import pl.mjaron.tinyloki.ILogCollector;
import pl.mjaron.tinyloki.ILogMonitor;
import pl.mjaron.tinyloki.ILogProcessor;

public class SyncExecutor
implements IExecutor {
    private ILogProcessor logProcessor = null;

    @Override
    public synchronized void onLog(int cachedLogsCount) {
        try {
            this.logProcessor.processLogs();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void configure(ILogCollector logCollector, ILogProcessor logProcessor, ILogMonitor logMonitor) {
        this.logProcessor = logProcessor;
    }

    @Override
    public void start() {
    }

    @Override
    public boolean sync(int timeout2) {
        return true;
    }

    @Override
    public void flush() {
    }

    @Override
    public boolean stop(int timeout2) {
        return true;
    }

    @Override
    public void stopAsync() {
    }
}

