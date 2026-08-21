/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.exporter.httpserver;

import com.sun.net.httpserver.HttpExchange;
import io.prometheus.metrics.exporter.common.PrometheusHttpExchange;
import io.prometheus.metrics.exporter.common.PrometheusHttpRequest;
import io.prometheus.metrics.exporter.common.PrometheusHttpResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpExchangeAdapter
implements PrometheusHttpExchange {
    private final HttpExchange httpExchange;
    private final HttpRequest request = new HttpRequest();
    private final HttpResponse response = new HttpResponse();
    private volatile boolean responseSent = false;

    public HttpExchangeAdapter(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
    }

    @Override
    public HttpRequest getRequest() {
        return this.request;
    }

    @Override
    public HttpResponse getResponse() {
        return this.response;
    }

    @Override
    public void handleException(IOException e) throws IOException {
        this.sendErrorResponseWithStackTrace(e);
    }

    @Override
    public void handleException(RuntimeException e) {
        this.sendErrorResponseWithStackTrace(e);
    }

    private void sendErrorResponseWithStackTrace(Exception requestHandlerException) {
        if (!this.responseSent) {
            this.responseSent = true;
            try {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                printWriter.write("An Exception occurred while scraping metrics: ");
                requestHandlerException.printStackTrace(new PrintWriter(printWriter));
                byte[] stackTrace = stringWriter.toString().getBytes(StandardCharsets.UTF_8);
                this.httpExchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
                this.httpExchange.sendResponseHeaders(500, stackTrace.length);
                this.httpExchange.getResponseBody().write(stackTrace);
            }
            catch (Exception errorWriterException) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "The Prometheus metrics HTTPServer caught an Exception during scrape and failed to send an error response to the client.", errorWriterException);
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Original Exception that caused the Prometheus scrape error:", requestHandlerException);
            }
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "The Prometheus metrics HTTPServer caught an Exception while trying to send the metrics response.", requestHandlerException);
        }
    }

    @Override
    public void close() {
        this.httpExchange.close();
    }

    public class HttpRequest
    implements PrometheusHttpRequest {
        @Override
        public String getQueryString() {
            return HttpExchangeAdapter.this.httpExchange.getRequestURI().getRawQuery();
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            Object headers = HttpExchangeAdapter.this.httpExchange.getRequestHeaders().get(name);
            if (headers == null) {
                return Collections.emptyEnumeration();
            }
            return Collections.enumeration(headers);
        }

        @Override
        public String getMethod() {
            return HttpExchangeAdapter.this.httpExchange.getRequestMethod();
        }

        @Override
        public String getRequestPath() {
            URI requestURI = HttpExchangeAdapter.this.httpExchange.getRequestURI();
            String uri = requestURI.toString();
            int qx = uri.indexOf(63);
            if (qx != -1) {
                uri = uri.substring(0, qx);
            }
            return uri;
        }
    }

    public class HttpResponse
    implements PrometheusHttpResponse {
        @Override
        public void setHeader(String name, String value) {
            HttpExchangeAdapter.this.httpExchange.getResponseHeaders().set(name, value);
        }

        @Override
        public OutputStream sendHeadersAndGetBody(int statusCode, int contentLength) throws IOException {
            if (HttpExchangeAdapter.this.responseSent) {
                throw new IOException("Cannot send multiple HTTP responses for a single HTTP exchange.");
            }
            HttpExchangeAdapter.this.responseSent = true;
            HttpExchangeAdapter.this.httpExchange.sendResponseHeaders(statusCode, contentLength);
            return HttpExchangeAdapter.this.httpExchange.getResponseBody();
        }
    }
}

