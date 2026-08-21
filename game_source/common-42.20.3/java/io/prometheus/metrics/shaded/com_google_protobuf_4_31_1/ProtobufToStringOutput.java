/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

public final class ProtobufToStringOutput {
    private static final ThreadLocal<OutputMode> outputMode = ThreadLocal.withInitial(() -> OutputMode.TEXT_FORMAT);

    private ProtobufToStringOutput() {
    }

    private static OutputMode setOutputMode(OutputMode newMode) {
        OutputMode oldMode = outputMode.get();
        outputMode.set(newMode);
        return oldMode;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void callWithSpecificFormat(Runnable impl, OutputMode mode) {
        OutputMode oldMode = ProtobufToStringOutput.setOutputMode(mode);
        try {
            impl.run();
        }
        finally {
            OutputMode outputMode = ProtobufToStringOutput.setOutputMode(oldMode);
        }
    }

    public static void callWithDebugFormat(Runnable impl) {
        ProtobufToStringOutput.callWithSpecificFormat(impl, OutputMode.DEBUG_FORMAT);
    }

    public static void callWithTextFormat(Runnable impl) {
        ProtobufToStringOutput.callWithSpecificFormat(impl, OutputMode.TEXT_FORMAT);
    }

    public static boolean shouldOutputDebugFormat() {
        return outputMode.get() == OutputMode.DEBUG_FORMAT;
    }

    private static enum OutputMode {
        DEBUG_FORMAT,
        TEXT_FORMAT;

    }
}

