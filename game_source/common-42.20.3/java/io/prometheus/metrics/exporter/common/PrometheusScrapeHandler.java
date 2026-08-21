/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.exporter.common;

import io.prometheus.metrics.config.ExporterFilterProperties;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.exporter.common.PrometheusHttpExchange;
import io.prometheus.metrics.exporter.common.PrometheusHttpRequest;
import io.prometheus.metrics.exporter.common.PrometheusHttpResponse;
import io.prometheus.metrics.expositionformats.ExpositionFormatWriter;
import io.prometheus.metrics.expositionformats.ExpositionFormats;
import io.prometheus.metrics.model.registry.MetricNameFilter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.zip.GZIPOutputStream;

public class PrometheusScrapeHandler {
    private final PrometheusRegistry registry;
    private final ExpositionFormats expositionFormats;
    private final Predicate<String> nameFilter;
    private final AtomicInteger lastResponseSize = new AtomicInteger(1024);
    private final List<String> supportedFormats;

    public PrometheusScrapeHandler() {
        this(PrometheusProperties.get(), PrometheusRegistry.defaultRegistry);
    }

    public PrometheusScrapeHandler(PrometheusRegistry registry) {
        this(PrometheusProperties.get(), registry);
    }

    public PrometheusScrapeHandler(PrometheusProperties config) {
        this(config, PrometheusRegistry.defaultRegistry);
    }

    public PrometheusScrapeHandler(PrometheusProperties config, PrometheusRegistry registry) {
        this.expositionFormats = ExpositionFormats.init(config.getExporterProperties());
        this.registry = registry;
        this.nameFilter = this.makeNameFilter(config.getExporterFilterProperties());
        this.supportedFormats = new ArrayList<String>(Arrays.asList("openmetrics", "text"));
        if (this.expositionFormats.getPrometheusProtobufWriter().isAvailable()) {
            this.supportedFormats.add("prometheus-protobuf");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void handleRequest(PrometheusHttpExchange exchange) throws IOException {
        block23: {
            try {
                PrometheusHttpRequest request = exchange.getRequest();
                MetricSnapshots snapshots2 = this.scrape(request);
                if (this.writeDebugResponse(snapshots2, exchange)) {
                    return;
                }
                ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream(this.lastResponseSize.get() + 1024);
                String acceptHeader = request.getHeader("Accept");
                ExpositionFormatWriter writer = this.expositionFormats.findWriter(acceptHeader);
                writer.write(responseBuffer, snapshots2);
                this.lastResponseSize.set(responseBuffer.size());
                PrometheusHttpResponse response = exchange.getResponse();
                response.setHeader("Content-Type", writer.getContentType());
                if (this.shouldUseCompression(request)) {
                    response.setHeader("Content-Encoding", "gzip");
                    try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(response.sendHeadersAndGetBody(200, 0));){
                        responseBuffer.writeTo(gzipOutputStream);
                        break block23;
                    }
                }
                int contentLength = responseBuffer.size();
                if (contentLength > 0) {
                    response.setHeader("Content-Length", String.valueOf(contentLength));
                }
                if (request.getMethod().equals("HEAD")) {
                    response.sendHeadersAndGetBody(200, -1);
                    break block23;
                }
                try (OutputStream outputStream2 = response.sendHeadersAndGetBody(200, contentLength);){
                    responseBuffer.writeTo(outputStream2);
                }
            }
            catch (IOException e) {
                exchange.handleException(e);
            }
            catch (RuntimeException e) {
                exchange.handleException(e);
            }
            finally {
                exchange.close();
            }
        }
    }

    private Predicate<String> makeNameFilter(ExporterFilterProperties props) {
        if (props.getAllowedMetricNames() == null && props.getExcludedMetricNames() == null && props.getAllowedMetricNamePrefixes() == null && props.getExcludedMetricNamePrefixes() == null) {
            return null;
        }
        return MetricNameFilter.builder().nameMustBeEqualTo(props.getAllowedMetricNames()).nameMustNotBeEqualTo(props.getExcludedMetricNames()).nameMustStartWith(props.getAllowedMetricNamePrefixes()).nameMustNotStartWith(props.getExcludedMetricNamePrefixes()).build();
    }

    private Predicate<String> makeNameFilter(String[] includedNames) {
        Predicate<String> result = null;
        if (includedNames != null && includedNames.length > 0) {
            result = MetricNameFilter.builder().nameMustBeEqualTo(includedNames).build();
        }
        if (result != null && this.nameFilter != null) {
            result = result.and(this.nameFilter);
        } else if (this.nameFilter != null) {
            result = this.nameFilter;
        }
        return result;
    }

    private MetricSnapshots scrape(PrometheusHttpRequest request) {
        Predicate<String> filter = this.makeNameFilter(request.getParameterValues("name[]"));
        if (filter != null) {
            return this.registry.scrape(filter, request);
        }
        return this.registry.scrape(request);
    }

    private boolean writeDebugResponse(MetricSnapshots snapshots2, PrometheusHttpExchange exchange) throws IOException {
        String debugParam = exchange.getRequest().getParameter("debug");
        PrometheusHttpResponse response = exchange.getResponse();
        if (debugParam == null) {
            return false;
        }
        response.setHeader("Content-Type", "text/plain; charset=utf-8");
        int responseStatus = this.supportedFormats.contains(debugParam) ? 200 : 500;
        OutputStream body = response.sendHeadersAndGetBody(responseStatus, 0);
        switch (debugParam) {
            case "openmetrics": {
                this.expositionFormats.getOpenMetricsTextFormatWriter().write(body, snapshots2);
                break;
            }
            case "text": {
                this.expositionFormats.getPrometheusTextFormatWriter().write(body, snapshots2);
                break;
            }
            case "prometheus-protobuf": {
                String debugString = this.expositionFormats.getPrometheusProtobufWriter().toDebugString(snapshots2);
                body.write(debugString.getBytes(StandardCharsets.UTF_8));
                break;
            }
            default: {
                body.write(("debug=" + debugParam + ": Unsupported query parameter. Valid values are 'openmetrics', 'text', and 'prometheus-protobuf'.").getBytes(StandardCharsets.UTF_8));
            }
        }
        return true;
    }

    private boolean shouldUseCompression(PrometheusHttpRequest request) {
        Enumeration<String> encodingHeaders = request.getHeaders("Accept-Encoding");
        if (encodingHeaders == null) {
            return false;
        }
        while (encodingHeaders.hasMoreElements()) {
            String[] encodings;
            String encodingHeader = encodingHeaders.nextElement();
            for (String encoding : encodings = encodingHeader.split(",")) {
                if (!encoding.trim().equalsIgnoreCase("gzip")) continue;
                return true;
            }
        }
        return false;
    }
}

