/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.tracer.common;

public interface SpanContext {
    public static final String EXEMPLAR_ATTRIBUTE_NAME = "exemplar";
    public static final String EXEMPLAR_ATTRIBUTE_VALUE = "true";

    public String getCurrentTraceId();

    public String getCurrentSpanId();

    public boolean isCurrentSpanSampled();

    public void markCurrentSpanAsExemplar();
}

