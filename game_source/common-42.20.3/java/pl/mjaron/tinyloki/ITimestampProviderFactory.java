/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import pl.mjaron.tinyloki.CurrentTimestampProvider;
import pl.mjaron.tinyloki.ITimestampProvider;
import pl.mjaron.tinyloki.IncrementingTimestampProvider;

public interface ITimestampProviderFactory {
    public ITimestampProvider create();

    public static ITimestampProviderFactory orDefault(ITimestampProviderFactory factory2) {
        if (factory2 == null) {
            return ITimestampProviderFactory.getDefault();
        }
        return factory2;
    }

    public static ITimestampProviderFactory current() {
        return CurrentTimestampProvider.factory();
    }

    public static ITimestampProviderFactory incrementing() {
        return IncrementingTimestampProvider.factory();
    }

    public static ITimestampProviderFactory getDefault() {
        return ITimestampProviderFactory.current();
    }
}

