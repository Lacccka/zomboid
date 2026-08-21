/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.util.logging;

import java.util.concurrent.atomic.AtomicBoolean;

public class FallbackLoggerConfiguration {
    private static final AtomicBoolean debug = new AtomicBoolean();
    private static final AtomicBoolean trace = new AtomicBoolean();

    private FallbackLoggerConfiguration() {
        throw new UnsupportedOperationException();
    }

    public static boolean isDebugEnabled() {
        return debug.get();
    }

    public static void setDebug(boolean debug) {
        FallbackLoggerConfiguration.debug.set(debug);
        if (!debug) {
            trace.set(false);
        }
    }

    public static boolean isTraceEnabled() {
        return trace.get();
    }

    public static void setTrace(boolean trace) {
        FallbackLoggerConfiguration.trace.set(trace);
        if (trace) {
            debug.set(true);
        }
    }
}

