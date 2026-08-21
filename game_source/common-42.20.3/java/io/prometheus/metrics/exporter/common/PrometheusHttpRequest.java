/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.exporter.common;

import io.prometheus.metrics.model.registry.PrometheusScrapeRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;

public interface PrometheusHttpRequest
extends PrometheusScrapeRequest {
    public String getQueryString();

    public Enumeration<String> getHeaders(String var1);

    public String getMethod();

    default public String getHeader(String name) {
        Enumeration<String> headers = this.getHeaders(name);
        if (headers == null || !headers.hasMoreElements()) {
            return null;
        }
        return headers.nextElement();
    }

    default public String getParameter(String name) {
        String[] values2 = this.getParameterValues(name);
        if (values2 == null || values2.length == 0) {
            return null;
        }
        return values2[0];
    }

    @Override
    default public String[] getParameterValues(String name) {
        try {
            ArrayList<String> result = new ArrayList<String>();
            String queryString = this.getQueryString();
            if (queryString != null) {
                String[] pairs;
                for (String pair : pairs = queryString.split("&")) {
                    int idx = pair.indexOf("=");
                    if (idx == -1 || !URLDecoder.decode(pair.substring(0, idx), "UTF-8").equals(name)) continue;
                    result.add(URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                }
            }
            if (result.isEmpty()) {
                return null;
            }
            return result.toArray(new String[0]);
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}

