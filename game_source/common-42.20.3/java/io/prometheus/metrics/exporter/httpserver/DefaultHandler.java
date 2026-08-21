/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.exporter.httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DefaultHandler
implements HttpHandler {
    private final byte[] responseBytes;
    private final String contentType;

    public DefaultHandler() {
        String responseString = "<html>\n<head><title>Prometheus Java Client</title></head>\n<body>\n<h1>Prometheus Java Client</h1>\n<h2>Metrics Path</h2>\nThe metrics path is <a href=\"metrics\">/metrics</a>.\n<h2>Name Filter</h2>\nIf you want to scrape only specific metrics, use the <tt>name[]</tt> parameter like this:\n<ul>\n<li><a href=\"metrics?name[]=my_metric\">/metrics?name[]=my_metric</a></li>\n</ul>\nYou can also use multiple <tt>name[]</tt> parameters to query multiple metrics:\n<ul>\n<li><a href=\"metrics?name[]=my_metric_a&name=my_metrics_b\">/metrics?name[]=my_metric_a&amp;name=[]=my_metric_b</a></li>\n</ul>\nThe <tt>name[]</tt> parameter can be used by the Prometheus server for scraping. Add the following snippet to your scrape job configuration in <tt>prometheus.yaml</tt>:\n<pre>\nparams:\n    name[]:\n        - my_metric_a\n        - my_metric_b\n</pre>\n<h2>Debug Parameter</h2>\nThe Prometheus Java metrics library supports multiple exposition formats.\nThe Prometheus server sends the <tt>Accept</tt> header to indicate which format it accepts.\nBy default, the Prometheus server accepts OpenMetrics text format, unless the Prometheus server is started with feature flag <tt>--enable-feature=native-histograms</tt>,\nin which case the default is Prometheus protobuf.\nThe Prometheus Java metrics library supports a <tt>debug</tt> query parameter for viewing the different formats in a Web browser:\n<ul>\n<li><a href=\"metrics?debug=openmetrics\">/metrics?debug=openmetrics</a>: View OpenMetrics text format.</li>\n<li><a href=\"metrics?debug=text\">/metrics?debug=text</a>: View Prometheus text format (this is the default when accessing the <a href=\"metrics\">/metrics</a> endpoint with a Web browser).</li>\n<li><a href=\"metrics?debug=prometheus-protobuf\">/metrics?debug=prometheus-protobuf</a>: View a text representation of the Prometheus protobuf format.</li>\n</ul>\nNote that the <tt>debug</tt> parameter is only for viewing different formats in a Web browser, it should not be used by the Prometheus server for scraping. The Prometheus server uses the <tt>Accept</tt> header for indicating which format it accepts.\n</body>\n</html>\n";
        this.responseBytes = responseString.getBytes(StandardCharsets.UTF_8);
        this.contentType = "text/html; charset=utf-8";
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

