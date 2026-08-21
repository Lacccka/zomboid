/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import pl.mjaron.tinyloki.IExecutor;
import pl.mjaron.tinyloki.ILogCollector;
import pl.mjaron.tinyloki.ILogMonitor;

public interface IBuffering {
    public static final int DEFAULT_MAX_MESSAGE_SIZE = 0x200000;
    public static final int DEFAULT_MAX_BUFFERS_COUNT = 8;

    public void configure(ILogCollector var1, int var2, int var3, IExecutor var4, ILogMonitor var5);

    public boolean beforeLog(int var1);

    public void logAccepted(int var1);

    public byte[][] collectAll();
}

