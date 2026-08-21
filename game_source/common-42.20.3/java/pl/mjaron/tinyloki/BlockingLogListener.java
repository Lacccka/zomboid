/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import pl.mjaron.tinyloki.ILogListener;
import pl.mjaron.tinyloki.Utils;

class BlockingLogListener
implements ILogListener {
    private final Object cachedLogsMonitor = new Object();
    private int cachedLogsCount = 0;
    private long logEntryLevel = 0L;
    private long syncLevel = 0L;

    BlockingLogListener() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void onLog(int cachedLogsCount) {
        Object object = this.cachedLogsMonitor;
        synchronized (object) {
            this.cachedLogsCount = cachedLogsCount;
        }
    }

    private int consumeCachedLogsUnsafe() {
        int cachedLogsCopy = this.cachedLogsCount;
        this.cachedLogsCount = 0;
        return cachedLogsCopy;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int waitForLogs(int time) throws InterruptedException {
        Object object = this.cachedLogsMonitor;
        synchronized (object) {
            ++this.logEntryLevel;
            this.cachedLogsMonitor.notifyAll();
            if (this.syncLevel <= this.logEntryLevel) {
                long timePoint = Utils.MonotonicClock.timePoint(time);
                while (!Utils.MonotonicClock.waitUntil(this.cachedLogsMonitor, timePoint) && this.syncLevel <= this.logEntryLevel) {
                }
            }
            return this.consumeCachedLogsUnsafe();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean sync(int timeout2) throws InterruptedException {
        long timePoint = Utils.MonotonicClock.timePoint(timeout2);
        Object object = this.cachedLogsMonitor;
        synchronized (object) {
            long targetLevel;
            this.syncLevel = targetLevel = this.logEntryLevel + 2L;
            this.cachedLogsMonitor.notifyAll();
            while (targetLevel > this.logEntryLevel) {
                if (!Utils.MonotonicClock.waitUntil(this.cachedLogsMonitor, timePoint)) continue;
                return false;
            }
            return true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void flush() {
        Object object = this.cachedLogsMonitor;
        synchronized (object) {
            this.syncLevel += 2L;
            this.cachedLogsMonitor.notifyAll();
        }
    }
}

