/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.expositionformats.ExpositionFormatWriter;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.IOException;
import java.io.OutputStream;
import javax.annotation.Nullable;

public class PrometheusProtobufWriter
implements ExpositionFormatWriter {
    @Nullable
    private static final ExpositionFormatWriter DELEGATE = PrometheusProtobufWriter.createProtobufWriter();
    public static final String CONTENT_TYPE = "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited";

    @Nullable
    private static ExpositionFormatWriter createProtobufWriter() {
        try {
            return Class.forName("io.prometheus.metrics.expositionformats.internal.PrometheusProtobufWriterImpl").asSubclass(ExpositionFormatWriter.class).getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean accepts(String acceptHeader) {
        if (acceptHeader == null) {
            return false;
        }
        return acceptHeader.contains("application/vnd.google.protobuf") && acceptHeader.contains("proto=io.prometheus.client.MetricFamily");
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public boolean isAvailable() {
        return DELEGATE != null;
    }

    @Override
    public String toDebugString(MetricSnapshots metricSnapshots) {
        this.checkAvailable();
        return DELEGATE.toDebugString(metricSnapshots);
    }

    @Override
    public void write(OutputStream out, MetricSnapshots metricSnapshots) throws IOException {
        this.checkAvailable();
        DELEGATE.write(out, metricSnapshots);
    }

    private void checkAvailable() {
        if (DELEGATE == null) {
            throw new UnsupportedOperationException("Prometheus protobuf writer not available");
        }
    }
}

