/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.opentelemetry.api.trace.Span
 *  io.opentelemetry.api.trace.SpanId
 *  io.opentelemetry.api.trace.TraceId
 */
package io.prometheus.metrics.tracer.otel;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.prometheus.metrics.tracer.common.SpanContext;

public class OpenTelemetrySpanContext
implements SpanContext {
    public static boolean isAvailable() {
        try {
            OpenTelemetrySpanContext test = new OpenTelemetrySpanContext();
            test.getCurrentSpanId();
            test.getCurrentTraceId();
            test.isCurrentSpanSampled();
            return true;
        }
        catch (LinkageError ignored) {
            return false;
        }
    }

    @Override
    public String getCurrentTraceId() {
        String traceId = Span.current().getSpanContext().getTraceId();
        return TraceId.isValid((CharSequence)traceId) ? traceId : null;
    }

    @Override
    public String getCurrentSpanId() {
        String spanId = Span.current().getSpanContext().getSpanId();
        return SpanId.isValid((CharSequence)spanId) ? spanId : null;
    }

    @Override
    public boolean isCurrentSpanSampled() {
        return Span.current().getSpanContext().isSampled();
    }

    @Override
    public void markCurrentSpanAsExemplar() {
        Span.current().setAttribute("exemplar", "true");
    }
}

