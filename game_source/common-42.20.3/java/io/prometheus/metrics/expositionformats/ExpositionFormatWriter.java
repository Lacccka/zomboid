/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public interface ExpositionFormatWriter {
    public boolean accepts(String var1);

    public void write(OutputStream var1, MetricSnapshots var2) throws IOException;

    default public String toDebugString(MetricSnapshots metricSnapshots) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            this.write(out, metricSnapshots);
            return out.toString("UTF-8");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getContentType();

    default public boolean isAvailable() {
        return true;
    }
}

