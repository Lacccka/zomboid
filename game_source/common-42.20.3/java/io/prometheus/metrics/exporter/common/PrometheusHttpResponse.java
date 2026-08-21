/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.exporter.common;

import java.io.IOException;
import java.io.OutputStream;

public interface PrometheusHttpResponse {
    public void setHeader(String var1, String var2);

    public OutputStream sendHeadersAndGetBody(int var1, int var2) throws IOException;
}

