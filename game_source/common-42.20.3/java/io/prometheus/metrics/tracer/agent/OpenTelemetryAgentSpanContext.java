/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span
 *  io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.SpanId
 *  io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.TraceId
 */
package io.prometheus.metrics.tracer.agent;

import io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span;
import io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.TraceId;
import io.prometheus.metrics.tracer.common.SpanContext;

public class OpenTelemetryAgentSpanContext
implements SpanContext {
    public static boolean isAvailable() {
        try {
            OpenTelemetryAgentSpanContext test = new OpenTelemetryAgentSpanContext();
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

