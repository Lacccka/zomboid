/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.exporter.httpserver;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.exporter.httpserver.BlockingRejectedExecutionHandler;
import io.prometheus.metrics.exporter.httpserver.DefaultHandler;
import io.prometheus.metrics.exporter.httpserver.HealthyHandler;
import io.prometheus.metrics.exporter.httpserver.MetricsHandler;
import io.prometheus.metrics.exporter.httpserver.NamedDaemonThreadFactory;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.PrivilegedActionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.security.auth.Subject;

public class HTTPServer
implements Closeable {
    protected final HttpServer server;
    protected final ExecutorService executorService;

    private HTTPServer(PrometheusProperties config, ExecutorService executorService, HttpServer httpServer, PrometheusRegistry registry, Authenticator authenticator, String authenticatedSubjectAttributeName, HttpHandler defaultHandler) {
        if (httpServer.getAddress() == null) {
            throw new IllegalArgumentException("HttpServer hasn't been bound to an address");
        }
        this.server = httpServer;
        this.executorService = executorService;
        this.registerHandler("/", defaultHandler == null ? new DefaultHandler() : defaultHandler, authenticator, authenticatedSubjectAttributeName);
        this.registerHandler("/metrics", new MetricsHandler(config, registry), authenticator, authenticatedSubjectAttributeName);
        this.registerHandler("/-/healthy", new HealthyHandler(), authenticator, authenticatedSubjectAttributeName);
        try {
            this.executorService.submit(this.server::start).get();
        }
        catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerHandler(String path, HttpHandler handler, Authenticator authenticator, String subjectAttributeName) {
        HttpContext context = this.server.createContext(path, this.wrapWithDoAs(handler, subjectAttributeName));
        if (authenticator != null) {
            context.setAuthenticator(authenticator);
        }
    }

    private HttpHandler wrapWithDoAs(final HttpHandler handler, final String subjectAttributeName) {
        if (subjectAttributeName == null) {
            return handler;
        }
        return new HttpHandler(){

            @Override
            public void handle(HttpExchange exchange) throws IOException {
                Object authSubject = exchange.getAttribute(subjectAttributeName);
                if (authSubject instanceof Subject) {
                    try {
                        Subject.doAs((Subject)authSubject, () -> {
                            handler.handle(exchange);
                            return null;
                        });
                    }
                    catch (PrivilegedActionException e) {
                        if (e.getException() != null) {
                            throw new IOException(e.getException());
                        }
                        throw new IOException(e);
                    }
                } else {
                    HTTPServer.this.drainInputAndClose(exchange);
                    exchange.sendResponseHeaders(403, -1L);
                }
            }
        };
    }

    private void drainInputAndClose(HttpExchange httpExchange) throws IOException {
        InputStream inputStream2 = httpExchange.getRequestBody();
        byte[] b = new byte[4096];
        while (inputStream2.read(b) != -1) {
        }
        inputStream2.close();
    }

    public void stop() {
        this.close();
    }

    @Override
    public void close() {
        this.server.stop(0);
        this.executorService.shutdown();
    }

    public int getPort() {
        return this.server.getAddress().getPort();
    }

    public static Builder builder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder builder(PrometheusProperties config) {
        return new Builder(config);
    }

    static {
        if (!System.getProperties().containsKey("sun.net.httpserver.maxReqTime")) {
            System.setProperty("sun.net.httpserver.maxReqTime", "60");
        }
        if (!System.getProperties().containsKey("sun.net.httpserver.maxRspTime")) {
            System.setProperty("sun.net.httpserver.maxRspTime", "600");
        }
    }

    public static class Builder {
        private final PrometheusProperties config;
        private Integer port = null;
        private String hostname = null;
        private InetAddress inetAddress = null;
        private ExecutorService executorService = null;
        private PrometheusRegistry registry = null;
        private Authenticator authenticator = null;
        private HttpsConfigurator httpsConfigurator = null;
        private HttpHandler defaultHandler = null;
        private String authenticatedSubjectAttributeName = null;

        private Builder(PrometheusProperties config) {
            this.config = config;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder inetAddress(InetAddress address) {
            this.inetAddress = address;
            return this;
        }

        public Builder executorService(ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        public Builder registry(PrometheusRegistry registry) {
            this.registry = registry;
            return this;
        }

        public Builder authenticator(Authenticator authenticator) {
            this.authenticator = authenticator;
            return this;
        }

        public Builder authenticatedSubjectAttributeName(String authenticatedSubjectAttributeName) {
            this.authenticatedSubjectAttributeName = authenticatedSubjectAttributeName;
            return this;
        }

        public Builder httpsConfigurator(HttpsConfigurator configurator) {
            this.httpsConfigurator = configurator;
            return this;
        }

        public Builder defaultHandler(HttpHandler defaultHandler) {
            this.defaultHandler = defaultHandler;
            return this;
        }

        public HTTPServer buildAndStart() throws IOException {
            HttpServer httpServer;
            if (this.registry == null) {
                this.registry = PrometheusRegistry.defaultRegistry;
            }
            if (this.httpsConfigurator != null) {
                httpServer = HttpsServer.create(this.makeInetSocketAddress(), 3);
                ((HttpsServer)httpServer).setHttpsConfigurator(this.httpsConfigurator);
            } else {
                httpServer = HttpServer.create(this.makeInetSocketAddress(), 3);
            }
            ExecutorService executorService = this.makeExecutorService();
            httpServer.setExecutor(executorService);
            return new HTTPServer(this.config, executorService, httpServer, this.registry, this.authenticator, this.authenticatedSubjectAttributeName, this.defaultHandler);
        }

        private InetSocketAddress makeInetSocketAddress() {
            if (this.inetAddress != null) {
                this.assertNull(this.hostname, "cannot configure 'inetAddress' and 'hostname' at the same time");
                return new InetSocketAddress(this.inetAddress, this.findPort());
            }
            if (this.hostname != null) {
                return new InetSocketAddress(this.hostname, this.findPort());
            }
            return new InetSocketAddress(this.findPort());
        }

        private ExecutorService makeExecutorService() {
            if (this.executorService != null) {
                return this.executorService;
            }
            return new ThreadPoolExecutor(1, 10, 120L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(true), NamedDaemonThreadFactory.defaultThreadFactory(true), new BlockingRejectedExecutionHandler());
        }

        private int findPort() {
            Integer port;
            if (this.config != null && this.config.getExporterHttpServerProperties() != null && (port = this.config.getExporterHttpServerProperties().getPort()) != null) {
                return port;
            }
            if (this.port != null) {
                return this.port;
            }
            return 0;
        }

        private void assertNull(Object o, String msg) {
            if (o != null) {
                throw new IllegalStateException(msg);
            }
        }
    }
}

