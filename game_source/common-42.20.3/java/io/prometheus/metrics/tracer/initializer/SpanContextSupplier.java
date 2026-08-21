/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.tracer.initializer;

import io.prometheus.metrics.tracer.agent.OpenTelemetryAgentSpanContext;
import io.prometheus.metrics.tracer.common.SpanContext;
import io.prometheus.metrics.tracer.otel.OpenTelemetrySpanContext;
import java.util.concurrent.atomic.AtomicReference;

public class SpanContextSupplier {
    private static final AtomicReference<SpanContext> spanContextRef = new AtomicReference();

    public static void setSpanContext(SpanContext spanContext) {
        spanContextRef.set(spanContext);
    }

    public static boolean hasSpanContext() {
        return SpanContextSupplier.getSpanContext() != null;
    }

    public static SpanContext getSpanContext() {
        return spanContextRef.get();
    }

    static {
        try {
            if (OpenTelemetrySpanContext.isAvailable()) {
                spanContextRef.set(new OpenTelemetrySpanContext());
            }
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
        }
        catch (UnsupportedClassVersionError unsupportedClassVersionError) {
            // empty catch block
        }
        try {
            if (OpenTelemetryAgentSpanContext.isAvailable()) {
                spanContextRef.set(new OpenTelemetryAgentSpanContext());
            }
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
        }
        catch (UnsupportedClassVersionError unsupportedClassVersionError) {
            // empty catch block
        }
    }
}

