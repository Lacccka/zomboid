/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import pl.mjaron.tinyloki.ITimestampProvider;
import pl.mjaron.tinyloki.ITimestampProviderFactory;

public class IncrementingTimestampProvider
implements ITimestampProvider {
    private long lastCurrentTimeMillis = 0L;
    private int nanoseconds = 0;

    @Override
    public long next(String message) {
        long current = System.currentTimeMillis();
        if (current == this.lastCurrentTimeMillis) {
            if (this.nanoseconds < 999999) {
                ++this.nanoseconds;
            }
            return this.lastCurrentTimeMillis * 1000000L + (long)this.nanoseconds;
        }
        this.lastCurrentTimeMillis = current;
        this.nanoseconds = 0;
        return this.lastCurrentTimeMillis * 1000000L;
    }

    public static ITimestampProviderFactory factory() {
        return new Factory();
    }

    public static class Factory
    implements ITimestampProviderFactory {
        @Override
        public ITimestampProvider create() {
            return new IncrementingTimestampProvider();
        }
    }
}

