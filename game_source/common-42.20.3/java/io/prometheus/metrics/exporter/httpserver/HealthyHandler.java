/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.exporter.httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HealthyHandler
implements HttpHandler {
    private final byte[] responseBytes;
    private final String contentType;

    public HealthyHandler() {
        String responseString = "Exporter is healthy.\n";
        this.responseBytes = responseString.getBytes(StandardCharsets.UTF_8);
        this.contentType = "text/plain; charset=utf-8";
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            exchange.getResponseHeaders().set("Content-Type", this.contentType);
            exchange.getResponseHeaders().set("Content-Length", Integer.toString(this.responseBytes.length));
            exchange.sendResponseHeaders(200, this.responseBytes.length);
            exchange.getResponseBody().write(this.responseBytes);
        }
        finally {
            exchange.close();
        }
    }
}

