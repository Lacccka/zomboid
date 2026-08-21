/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.expositionformats.ExpositionFormatWriter;
import io.prometheus.metrics.expositionformats.TextFormatUtil;
import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.InfoSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.Quantile;
import io.prometheus.metrics.model.snapshots.StateSetSnapshot;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import io.prometheus.metrics.model.snapshots.UnknownSnapshot;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class PrometheusTextFormatWriter
implements ExpositionFormatWriter {
    public static final String CONTENT_TYPE = "text/plain; version=0.0.4; charset=utf-8";
    private final boolean writeCreatedTimestamps;
    private final boolean timestampsInMs;

    @Deprecated
    public PrometheusTextFormatWriter(boolean writeCreatedTimestamps) {
        this(writeCreatedTimestamps, false);
    }

    private PrometheusTextFormatWriter(boolean writeCreatedTimestamps, boolean timestampsInMs) {
        this.writeCreatedTimestamps = writeCreatedTimestamps;
        this.timestampsInMs = timestampsInMs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static PrometheusTextFormatWriter create() {
        return PrometheusTextFormatWriter.builder().build();
    }

    @Override
    public boolean accepts(String acceptHeader) {
        if (acceptHeader == null) {
            return false;
        }
        return acceptHeader.contains("text/plain");
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public void write(OutputStream out, MetricSnapshots metricSnapshots) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        for (MetricSnapshot snapshot : metricSnapshots) {
            if (snapshot.getDataPoints().isEmpty()) continue;
            if (snapshot instanceof CounterSnapshot) {
                this.writeCounter(writer, (CounterSnapshot)snapshot);
                continue;
            }
            if (snapshot instanceof GaugeSnapshot) {
                this.writeGauge(writer, (GaugeSnapshot)snapshot);
                continue;
            }
            if (snapshot instanceof HistogramSnapshot) {
                this.writeHistogram(writer, (HistogramSnapshot)snapshot);
                continue;
            }
            if (snapshot instanceof SummarySnapshot) {
                this.writeSummary(writer, (SummarySnapshot)snapshot);
                continue;
            }
            if (snapshot instanceof InfoSnapshot) {
                this.writeInfo(writer, (InfoSnapshot)snapshot);
                continue;
            }
            if (snapshot instanceof StateSetSnapshot) {
                this.writeStateSet(writer, (StateSetSnapshot)snapshot);
                continue;
            }
            if (!(snapshot instanceof UnknownSnapshot)) continue;
            this.writeUnknown(writer, (UnknownSnapshot)snapshot);
        }
        if (this.writeCreatedTimestamps) {
            for (MetricSnapshot snapshot : metricSnapshots) {
                if (snapshot.getDataPoints().isEmpty()) continue;
                if (snapshot instanceof CounterSnapshot) {
                    this.writeCreated(writer, snapshot);
                    continue;
                }
                if (snapshot instanceof HistogramSnapshot) {
                    this.writeCreated(writer, snapshot);
                    continue;
                }
                if (!(snapshot instanceof SummarySnapshot)) continue;
                this.writeCreated(writer, snapshot);
            }
        }
        ((Writer)writer).flush();
    }

    public void writeCreated(Writer writer, MetricSnapshot snapshot) throws IOException {
        boolean metadataWritten = false;
        MetricMetadata metadata = snapshot.getMetadata();
        for (DataPointSnapshot dataPointSnapshot : snapshot.getDataPoints()) {
            if (!dataPointSnapshot.hasCreatedTimestamp()) continue;
            if (!metadataWritten) {
                this.writeMetadata(writer, "_created", "gauge", metadata);
                metadataWritten = true;
            }
            this.writeNameAndLabels(writer, metadata.getPrometheusName(), "_created", dataPointSnapshot.getLabels());
            TextFormatUtil.writePrometheusTimestamp(writer, dataPointSnapshot.getCreatedTimestampMillis(), this.timestampsInMs);
            this.writeScrapeTimestampAndNewline(writer, dataPointSnapshot);
        }
    }

    private void writeCounter(Writer writer, CounterSnapshot snapshot) throws IOException {
        if (!snapshot.getDataPoints().isEmpty()) {
            MetricMetadata metadata = snapshot.getMetadata();
            this.writeMetadata(writer, "_total", "counter", metadata);
            for (CounterSnapshot.CounterDataPointSnapshot data : snapshot.getDataPoints()) {
                this.writeNameAndLabels(writer, metadata.getPrometheusName(), "_total", data.getLabels());
                TextFormatUtil.writeDouble(writer, data.getValue());
                this.writeScrapeTimestampAndNewline(writer, data);
            }
        }
    }

    private void writeGauge(Writer writer, GaugeSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        this.writeMetadata(writer, "", "gauge", metadata);
        for (GaugeSnapshot.GaugeDataPointSnapshot data : snapshot.getDataPoints()) {
            this.writeNameAndLabels(writer, metadata.getPrometheusName(), null, data.getLabels());
            TextFormatUtil.writeDouble(writer, data.getValue());
            this.writeScrapeTimestampAndNewline(writer, data);
        }
    }

    private void writeHistogram(Writer writer, HistogramSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        this.writeMetadata(writer, "", "histogram", metadata);
        for (HistogramSnapshot.HistogramDataPointSnapshot data : snapshot.getDataPoints()) {
            ClassicHistogramBuckets buckets = this.getClassicBuckets(data);
            long cumulativeCount = 0L;
            for (int i = 0; i < buckets.size(); ++i) {
                this.writeNameAndLabels(writer, metadata.getPrometheusName(), "_bucket", data.getLabels(), "le", buckets.getUpperBound(i));
                TextFormatUtil.writeLong(writer, cumulativeCount += buckets.getCount(i));
                this.writeScrapeTimestampAndNewline(writer, data);
            }
            if (snapshot.isGaugeHistogram()) continue;
            if (data.hasCount()) {
                this.writeNameAndLabels(writer, metadata.getPrometheusName(), "_count", data.getLabels());
                TextFormatUtil.writeLong(writer, data.getCount());
                this.writeScrapeTimestampAndNewline(writer, data);
            }
            if (!data.hasSum()) continue;
            this.writeNameAndLabels(writer, metadata.getPrometheusName(), "_sum", data.getLabels());
            TextFormatUtil.writeDouble(writer, data.getSum());
            this.writeScrapeTimestampAndNewline(writer, data);
        }
        if (snapshot.isGaugeHistogram()) {
            this.writeGaugeCountSum(writer, snapshot, metadata);
        }
    }

    private ClassicHistogramBuckets getClassicBuckets(HistogramSnapshot.HistogramDataPointSnapshot data) {
        if (data.getClassicBuckets().isEmpty()) {
            return ClassicHistogramBuckets.of(new double[]{Double.POSITIVE_INFINITY}, new long[]{data.getCount()});
        }
        return data.getClassicBuckets();
    }

    private void writeGaugeCountSum(Writer writer, HistogramSnapshot snapshot, MetricMetadata metadata) throws IOException {
        boolean metadataWritten = false;
        for (HistogramSnapshot.HistogramDataPointSnapshot data : snapshot.getDataPoints()) {
            if (!data.hasCount()) continue;
            if (!metadataWritten) {
                this.writeMetadata(writer, "_gcount", "gauge", metadata);
                metadataWritten = true;
            }
            this.writeNameAndLabels(writer, metadata.getPrometheusName(), "_gcount", data.getLabels());
            TextFormatUtil.writeLong(writer, data.getCount());
            this.writeScrapeTimestampAndNewline(writer, data);
        }
        metadataWritten = false;
        for (HistogramSnapshot.HistogramDataPointSnapshot data : snapshot.getDataPoints()) {
            if (!data.hasSum()) continue;
            if (!metadataWritten) {
                this.writeMetadata(writer, "_gsum", "gauge", metadata);
                metadataWritten = true;
            }
            this.writeNameAndLabels(writer, metadata.getPrometheusName(), "_gsum", data.getLabels());
            TextFormatUtil.writeDouble(writer, data.getSum());
            this.writeScrapeTimestampAndNewline(writer, data);
        }
    }

    private void writeSummary(Writer writer, SummarySnapshot snapshot) throws IOException {
        boolean metadataWritten = false;
        MetricMetadata metadata = snapshot.getMetadata();
        for (SummarySnapshot.SummaryDataPointSnapshot data : snapshot.getDataPoints()) {
            if (data.getQuantiles().size() == 0 && !data.hasCount() && !data.hasSum()) continue;
            if (!metadataWritten) {
                this.writeMetadata(writer, "", "summary", metadata);
                metadataWritten = true;
            }
            for (Quantile quantile : data.getQuantiles()) {
                this.writeNameAndLabels(writer, metadata.getPrometheusName(), null, data.getLabels(), "quantile", quantile.getQuantile());
                TextFormatUtil.writeDouble(writer, quantile.getValue());
                this.writeScrapeTimestampAndNewline(writer, data);
            }
            if (data.hasCount()) {
                this.writeNameAndLabels(writer, metadata.getPrometheusName(), "_count", data.getLabels());
                TextFormatUtil.writeLong(writer, data.getCount());
                this.writeScrapeTimestampAndNewline(writer, data);
            }
            if (!data.hasSum()) continue;
            this.writeNameAndLabels(writer, metadata.getPrometheusName(), "_sum", data.getLabels());
            TextFormatUtil.writeDouble(writer, data.getSum());
            this.writeScrapeTimestampAndNewline(writer, data);
        }
    }

    private void writeInfo(Writer writer, InfoSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        this.writeMetadata(writer, "_info", "gauge", metadata);
        for (InfoSnapshot.InfoDataPointSnapshot data : snapshot.getDataPoints()) {
            this.writeNameAndLabels(writer, metadata.getPrometheusName(), "_info", data.getLabels());
            writer.write("1");
            this.writeScrapeTimestampAndNewline(writer, data);
        }
    }

    private void writeStateSet(Writer writer, StateSetSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        this.writeMetadata(writer, "", "gauge", metadata);
        for (StateSetSnapshot.StateSetDataPointSnapshot data : snapshot.getDataPoints()) {
            for (int i = 0; i < data.size(); ++i) {
                writer.write(metadata.getPrometheusName());
                writer.write(123);
                for (int j = 0; j < data.getLabels().size(); ++j) {
                    if (j > 0) {
                        writer.write(",");
                    }
                    writer.write(data.getLabels().getPrometheusName(j));
                    writer.write("=\"");
                    TextFormatUtil.writeEscapedLabelValue(writer, data.getLabels().getValue(j));
                    writer.write("\"");
                }
                if (!data.getLabels().isEmpty()) {
                    writer.write(",");
                }
                writer.write(metadata.getPrometheusName());
                writer.write("=\"");
                TextFormatUtil.writeEscapedLabelValue(writer, data.getName(i));
                writer.write("\"} ");
                if (data.isTrue(i)) {
                    writer.write("1");
                } else {
                    writer.write("0");
                }
                this.writeScrapeTimestampAndNewline(writer, data);
            }
        }
    }

    private void writeUnknown(Writer writer, UnknownSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        this.writeMetadata(writer, "", "untyped", metadata);
        for (UnknownSnapshot.UnknownDataPointSnapshot data : snapshot.getDataPoints()) {
            this.writeNameAndLabels(writer, metadata.getPrometheusName(), null, data.getLabels());
            TextFormatUtil.writeDouble(writer, data.getValue());
            this.writeScrapeTimestampAndNewline(writer, data);
        }
    }

    private void writeNameAndLabels(Writer writer, String name, String suffix, Labels labels) throws IOException {
        this.writeNameAndLabels(writer, name, suffix, labels, null, 0.0);
    }

    private void writeNameAndLabels(Writer writer, String name, String suffix, Labels labels, String additionalLabelName, double additionalLabelValue) throws IOException {
        writer.write(name);
        if (suffix != null) {
            writer.write(suffix);
        }
        if (!labels.isEmpty() || additionalLabelName != null) {
            TextFormatUtil.writeLabels(writer, labels, additionalLabelName, additionalLabelValue);
        }
        writer.write(32);
    }

    private void writeMetadata(Writer writer, String suffix, String typeString, MetricMetadata metadata) throws IOException {
        if (metadata.getHelp() != null && !metadata.getHelp().isEmpty()) {
            writer.write("# HELP ");
            writer.write(metadata.getPrometheusName());
            if (suffix != null) {
                writer.write(suffix);
            }
            writer.write(32);
            this.writeEscapedHelp(writer, metadata.getHelp());
            writer.write(10);
        }
        writer.write("# TYPE ");
        writer.write(metadata.getPrometheusName());
        if (suffix != null) {
            writer.write(suffix);
        }
        writer.write(32);
        writer.write(typeString);
        writer.write(10);
    }

    private void writeEscapedHelp(Writer writer, String s) throws IOException {
        block4: for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': {
                    writer.append("\\\\");
                    continue block4;
                }
                case '\n': {
                    writer.append("\\n");
                    continue block4;
                }
                default: {
                    writer.append(c);
                }
            }
        }
    }

    private void writeScrapeTimestampAndNewline(Writer writer, DataPointSnapshot data) throws IOException {
        if (data.hasScrapeTimestamp()) {
            writer.write(32);
            TextFormatUtil.writePrometheusTimestamp(writer, data.getScrapeTimestampMillis(), this.timestampsInMs);
        }
        writer.write(10);
    }

    public static class Builder {
        boolean includeCreatedTimestamps;
        boolean timestampsInMs = true;

        private Builder() {
        }

        public Builder setIncludeCreatedTimestamps(boolean includeCreatedTimestamps) {
            this.includeCreatedTimestamps = includeCreatedTimestamps;
            return this;
        }

        @Deprecated
        public Builder setTimestampsInMs(boolean timestampsInMs) {
            this.timestampsInMs = timestampsInMs;
            return this;
        }

        public PrometheusTextFormatWriter build() {
            return new PrometheusTextFormatWriter(this.includeCreatedTimestamps, this.timestampsInMs);
        }
    }
}

