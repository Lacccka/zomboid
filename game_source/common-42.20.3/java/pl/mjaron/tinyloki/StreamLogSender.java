/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import java.io.IOException;
import java.io.OutputStream;
import pl.mjaron.tinyloki.ILogMonitor;
import pl.mjaron.tinyloki.ILogSender;
import pl.mjaron.tinyloki.LogSenderSettings;

public class StreamLogSender
implements ILogSender {
    private final OutputStream outputStream;
    ILogMonitor logMonitor = null;

    public StreamLogSender(OutputStream outputStream2) {
        this.outputStream = outputStream2;
    }

    @Override
    public void configure(LogSenderSettings logSenderSettings, ILogMonitor logMonitor) {
        this.logMonitor = logMonitor;
    }

    @Override
    public void send(byte[] message) throws InterruptedException, IOException {
        this.logMonitor.send(message);
        try {
            this.outputStream.write(message);
            this.logMonitor.sendOk(200);
        }
        catch (IOException e) {
            this.logMonitor.sendErr(-1, e.getMessage());
            throw e;
        }
    }
}

