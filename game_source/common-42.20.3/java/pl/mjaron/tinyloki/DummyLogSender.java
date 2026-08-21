/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import pl.mjaron.tinyloki.ILogMonitor;
import pl.mjaron.tinyloki.ILogSender;
import pl.mjaron.tinyloki.LogSenderSettings;

public class DummyLogSender
implements ILogSender {
    private ILogMonitor logMonitor;
    private int dummySendBlockTime = 0;

    public DummyLogSender() {
    }

    public DummyLogSender(int dummySendBlockTime) {
        if (dummySendBlockTime < 0) {
            throw new IllegalArgumentException("The block time must be 0 or positive value.");
        }
        this.dummySendBlockTime = dummySendBlockTime;
    }

    @Override
    public void configure(LogSenderSettings logSenderSettings, ILogMonitor logMonitor) {
        this.logMonitor = logMonitor;
    }

    @Override
    public void send(byte[] message) throws InterruptedException {
        this.logMonitor.send(message);
        if (this.dummySendBlockTime > 0) {
            Thread.sleep(this.dummySendBlockTime);
        }
        this.logMonitor.sendOk(200);
    }
}

