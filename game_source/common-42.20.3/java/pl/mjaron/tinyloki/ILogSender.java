/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import java.io.IOException;
import pl.mjaron.tinyloki.ILogMonitor;
import pl.mjaron.tinyloki.LogSenderSettings;

public interface ILogSender {
    public void configure(LogSenderSettings var1, ILogMonitor var2);

    public void send(byte[] var1) throws InterruptedException, IOException;
}

