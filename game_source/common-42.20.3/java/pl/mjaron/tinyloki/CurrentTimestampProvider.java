/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import pl.mjaron.tinyloki.ITimestampProvider;
import pl.mjaron.tinyloki.ITimestampProviderFactory;
import pl.mjaron.tinyloki.Utils;

public class CurrentTimestampProvider
implements ITimestampProvider {
    @Override
    public long next(String message) {
        return Utils.Nanoseconds.currentTime();
    }

    public static ITimestampProviderFactory factory() {
        return new Factory();
    }

    public static class Factory
    implements ITimestampProviderFactory {
        @Override
        public ITimestampProvider create() {
            return new CurrentTimestampProvider();
        }
    }
}

