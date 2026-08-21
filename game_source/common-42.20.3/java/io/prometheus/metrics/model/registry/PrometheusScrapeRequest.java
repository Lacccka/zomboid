/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.registry;

public interface PrometheusScrapeRequest {
    public String getRequestPath();

    public String[] getParameterValues(String var1);
}

