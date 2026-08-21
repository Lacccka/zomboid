/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import pl.mjaron.tinyloki.ILogCollector;
import pl.mjaron.tinyloki.ILogListener;
import pl.mjaron.tinyloki.ILogMonitor;
import pl.mjaron.tinyloki.ILogProcessor;

public interface IExecutor
extends ILogListener {
    public void configure(ILogCollector var1, ILogProcessor var2, ILogMonitor var3);

    public void start();

    public boolean sync(int var1) throws InterruptedException;

    public void flush();

    public boolean stop(int var1) throws InterruptedException;

    public void stopAsync();
}

