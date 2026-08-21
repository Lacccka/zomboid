/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.exporter.httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.exporter.common.PrometheusScrapeHandler;
import io.prometheus.metrics.exporter.httpserver.HttpExchangeAdapter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.io.IOException;

public class MetricsHandler
implements HttpHandler {
    private final PrometheusScrapeHandler prometheusScrapeHandler;

    public MetricsHandler() {
        this.prometheusScrapeHandler = new PrometheusScrapeHandler();
    }

    public MetricsHandler(PrometheusRegistry registry) {
        this.prometheusScrapeHandler = new PrometheusScrapeHandler(registry);
    }

    public MetricsHandler(PrometheusProperties config) {
        this.prometheusScrapeHandler = new PrometheusScrapeHandler(config);
    }

    public MetricsHandler(PrometheusProperties config, PrometheusRegistry registry) {
        this.prometheusScrapeHandler = new PrometheusScrapeHandler(config, registry);
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        this.prometheusScrapeHandler.handleRequest(new HttpExchangeAdapter(t));
    }
}

