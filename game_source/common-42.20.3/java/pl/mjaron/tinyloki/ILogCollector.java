/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import pl.mjaron.tinyloki.IBuffering;
import pl.mjaron.tinyloki.ILogListener;
import pl.mjaron.tinyloki.ILogStream;
import pl.mjaron.tinyloki.ITimestampProviderFactory;
import pl.mjaron.tinyloki.LabelSettings;
import pl.mjaron.tinyloki.Labels;

public interface ILogCollector {
    public void configure(ILogListener var1, IBuffering var2, LabelSettings var3, ITimestampProviderFactory var4);

    public ILogStream createStream(Labels var1);

    public byte[] collect();

    public byte[][] collectAll();

    public String contentType();
}

