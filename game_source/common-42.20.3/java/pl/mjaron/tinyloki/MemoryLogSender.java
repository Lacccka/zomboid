/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import pl.mjaron.tinyloki.ILogMonitor;
import pl.mjaron.tinyloki.ILogSender;
import pl.mjaron.tinyloki.LogSenderSettings;
import pl.mjaron.tinyloki.StreamLogSender;

public class MemoryLogSender
implements ILogSender {
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final StreamLogSender streamLogSender = new StreamLogSender(this.outputStream);

    @Override
    public void configure(LogSenderSettings logSenderSettings, ILogMonitor logMonitor) {
        this.streamLogSender.configure(logSenderSettings, logMonitor);
    }

    @Override
    public void send(byte[] message) throws InterruptedException, IOException {
        this.streamLogSender.send(message);
    }

    public byte[] get() {
        return this.outputStream.toByteArray();
    }

    public String getAsString() {
        return new String(this.get(), StandardCharsets.UTF_8);
    }

    void clear() {
        this.outputStream.reset();
    }
}

