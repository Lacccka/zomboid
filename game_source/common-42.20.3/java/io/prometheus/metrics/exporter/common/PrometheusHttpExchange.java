/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.exporter.common;

import io.prometheus.metrics.exporter.common.PrometheusHttpRequest;
import io.prometheus.metrics.exporter.common.PrometheusHttpResponse;
import java.io.IOException;

public interface PrometheusHttpExchange
extends AutoCloseable {
    public PrometheusHttpRequest getRequest();

    public PrometheusHttpResponse getResponse();

    public void handleException(IOException var1) throws IOException;

    public void handleException(RuntimeException var1);

    @Override
    public void close();
}

