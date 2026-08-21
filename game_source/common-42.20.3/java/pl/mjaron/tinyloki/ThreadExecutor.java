/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import pl.mjaron.tinyloki.BlockingLogListener;
import pl.mjaron.tinyloki.IExecutor;
import pl.mjaron.tinyloki.ILogCollector;
import pl.mjaron.tinyloki.ILogMonitor;
import pl.mjaron.tinyloki.ILogProcessor;

public class ThreadExecutor
implements IExecutor,
Runnable {
    public static final int DEFAULT_PROCESSING_INTERVAL_TIME_MS = 5000;
    private final BlockingLogListener logListener = new BlockingLogListener();
    private ILogMonitor logMonitor = null;
    private ILogCollector logCollector = null;
    private ILogProcessor logProcessor = null;
    private Thread workerThread = null;
    private int processingIntervalTime = 5000;

    public ThreadExecutor() {
    }

    public ThreadExecutor(int processingIntervalTime) {
        this.processingIntervalTime = processingIntervalTime;
    }

    public int getProcessingIntervalTime() {
        return this.processingIntervalTime;
    }

    @Override
    public void configure(ILogCollector logCollector, ILogProcessor logProcessor, ILogMonitor logMonitor) {
        if (logCollector == null) {
            throw new NullPointerException("Cannot configure ThreadExecutor: Given log collector is null.");
        }
        if (logProcessor == null) {
            throw new NullPointerException("Cannot configure ThreadExecutor: Given log processor is null.");
        }
        if (logMonitor == null) {
            throw new NullPointerException("Cannot configure ThreadExecutor: Given log monitor is null.");
        }
        this.logMonitor = logMonitor;
        this.logCollector = logCollector;
        this.logProcessor = logProcessor;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void start() {
        if (this.logCollector == null) {
            throw new RuntimeException("Cannot start the executor: The ThreadExecutor is not configured.");
        }
        ThreadExecutor threadExecutor = this;
        synchronized (threadExecutor) {
            if (this.workerThread != null) {
                throw new RuntimeException("Cannot start the executor: Already started.");
            }
            this.workerThread = new Thread(this, "tinyloki." + this.getClass().getSimpleName());
            this.workerThread.start();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean stop(int timeout2) throws InterruptedException {
        Thread thread2;
        ThreadExecutor threadExecutor = this;
        synchronized (threadExecutor) {
            if (this.workerThread == null) {
                return true;
            }
            thread2 = this.workerThread;
        }
        thread2.interrupt();
        thread2.join(timeout2);
        if (thread2.getState() != Thread.State.TERMINATED) {
            return false;
        }
        threadExecutor = this;
        synchronized (threadExecutor) {
            if (this.workerThread == thread2) {
                this.workerThread = null;
            }
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void stopAsync() {
        ThreadExecutor threadExecutor = this;
        synchronized (threadExecutor) {
            if (this.workerThread != null) {
                this.workerThread.interrupt();
            }
        }
    }

    @Override
    public boolean sync(int timeout2) throws InterruptedException {
        return this.logListener.sync(timeout2);
    }

    @Override
    public void flush() {
        this.logListener.flush();
    }

    @Override
    public void run() {
        this.logMonitor.logInfo("Worker thread: started.");
        while (true) {
            try {
                while (true) {
                    int anyLogs;
                    if ((anyLogs = this.logListener.waitForLogs(this.processingIntervalTime)) <= 0) {
                        continue;
                    }
                    this.logProcessor.processLogs();
                }
            }
            catch (InterruptedException e) {
                this.logMonitor.logInfo("Worker thread: interrupted.");
                return;
            }
            catch (Exception e) {
                this.logMonitor.onException(e);
                continue;
            }
            break;
        }
    }

    @Override
    public void onLog(int cachedLogsCount) {
        this.logListener.onLog(cachedLogsCount);
    }
}

