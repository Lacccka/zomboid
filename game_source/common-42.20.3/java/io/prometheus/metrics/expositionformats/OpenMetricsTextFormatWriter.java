/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.expositionformats.ExpositionFormatWriter;
import io.prometheus.metrics.expositionformats.TextFormatUtil;
import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import io.prometheus.metrics.model.snapshots.DistributionDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.Exemplars;
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
import java.util.List;

public class OpenMetricsTextFormatWriter
implements ExpositionFormatWriter {
    public static final String CONTENT_TYPE = "application/openmetrics-text; version=1.0.0; charset=utf-8";
    private final boolean createdTimestampsEnabled;
    private final boolean exemplarsOnAllMetricTypesEnabled;

    public OpenMetricsTextFormatWriter(boolean createdTimestampsEnabled, boolean exemplarsOnAllMetricTypesEnabled) {
        this.createdTimestampsEnabled = createdTimestampsEnabled;
        this.exemplarsOnAllMetricTypesEnabled = exemplarsOnAllMetricTypesEnabled;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static OpenMetricsTextFormatWriter create() {
        return OpenMetricsTextFormatWriter.builder().build();
    }

    @Override
    public boolean accepts(String acceptHeader) {
        if (acceptHeader == null) {
            return false;
        }
        return acceptHeader.contains("application/openmetrics-text");
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
        writer.write("# EOF\n");
        ((Writer)writer).flush();
    }

    private void writeCounter(Writer writer, CounterSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        this.writeMetadata(writer, "counter", metadata);
        for (CounterSnapshot.CounterDataPointSnapshot data : snapshot.getDataPoints()) {
            this.writeNameAndLabels(writer, metadata.getPrometheusName(), "_total", data.getLabels());
            TextFormatUtil.writeDouble(writer, data.getValue());
            this.writeScrapeTimestampAndExemplar(writer, data, data.getExemplar());
            this.writeCreated(writer, metadata, data);
        }
    }

    private void writeGauge(Writer writer, GaugeSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        this.writeMetadata(writer, "gauge", metadata);
        for (GaugeSnapshot.GaugeDataPointSnapshot data : snapshot.getDataPoints()) {
            this.writeNameAndLabels(writer, metadata.getPrometheusName(), null, data.getLabels());
            TextFormatUtil.writeDouble(writer, data.getValue());
            if (this.exemplarsOnAllMetricTypesEnabled) {
                this.writeScrapeTimestampAndExemplar(writer, data, data.getExemplar());
                continue;
            }
            this.writeScrapeTimestampAndExemplar(writer, data, null);
        }
    }

    private void writeHistogram(Writer writer, HistogramSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        if (snapshot.isGaugeHistogram()) {
            this.writeMetadata(writer, "gaugehistogram", metadata);
            this.writeClassicHistogramBuckets(writer, metadata, "_gcount", "_gsum", snapshot.getDataPoints());
        } else {
            this.writeMetadata(writer, "histogram", metadata);
            this.writeClassicHistogramBuckets(writer, metadata, "_count", "_sum", snapshot.getDataPoints());
        }
    }

    private void writeClassicHistogramBuckets(Writer writer, MetricMetadata metadata, String countSuffix, String sumSuffix, List<HistogramSnapshot.HistogramDataPointSnapshot> dataList) throws IOException {
        for (HistogramSnapshot.HistogramDataPointSnapshot data : dataList) {
            ClassicHistogramBuckets buckets = this.getClassicBuckets(data);
            Exemplars exemplars = data.getExemplars();
            long cumulativeCount = 0L;
            for (int i = 0; i < buckets.size(); ++i) {
                this.writeNameAndLabels(writer, metadata.getPrometheusName(), "_bucket", data.getLabels(), "le", buckets.getUpperBound(i));
                TextFormatUtil.writeLong(writer, cumulativeCount += buckets.getCount(i));
                Exemplar exemplar = i == 0 ? exemplars.get(Double.NEGATIVE_INFINITY, buckets.getUpperBound(i)) : exemplars.get(buckets.getUpperBound(i - 1), buckets.getUpperBound(i));
                this.writeScrapeTimestampAndExemplar(writer, data, exemplar);
            }
            if (data.hasCount() && data.hasSum()) {
                this.writeCountAndSum(writer, metadata, data, countSuffix, sumSuffix, exemplars);
            }
            this.writeCreated(writer, metadata, data);
        }
    }

    private ClassicHistogramBuckets getClassicBuckets(HistogramSnapshot.HistogramDataPointSnapshot data) {
        if (data.getClassicBuckets().isEmpty()) {
            return ClassicHistogramBuckets.of(new double[]{Double.POSITIVE_INFINITY}, new long[]{data.getCount()});
        }
        return data.getClassicBuckets();
    }

    private void writeSummary(Writer writer, SummarySnapshot snapshot) throws IOException {
        boolean metadataWritten = false;
        MetricMetadata metadata = snapshot.getMetadata();
        for (SummarySnapshot.SummaryDataPointSnapshot data : snapshot.getDataPoints()) {
            if (data.getQuantiles().size() == 0 && !data.hasCount() && !data.hasSum()) continue;
            if (!metadataWritten) {
                this.writeMetadata(writer, "summary", metadata);
                metadataWritten = true;
            }
            Exemplars exemplars = data.getExemplars();
            int exemplarIndex = 1;
            for (Quantile quantile : data.getQuantiles()) {
                this.writeNameAndLabels(writer, metadata.getPrometheusName(), null, data.getLabels(), "quantile", quantile.getQuantile());
                TextFormatUtil.writeDouble(writer, quantile.getValue());
                if (exemplars.size() > 0 && this.exemplarsOnAllMetricTypesEnabled) {
                    exemplarIndex = (exemplarIndex + 1) % exemplars.size();
                    this.writeScrapeTimestampAndExemplar(writer, data, exemplars.get(exemplarIndex));
                    continue;
                }
                this.writeScrapeTimestampAndExemplar(writer, data, null);
            }
            this.writeCountAndSum(writer, metadata, data, "_count", "_sum", exemplars);
            this.writeCreated(writer, metadata, data);
        }
    }

    private void writeInfo(Writer writer, InfoSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        this.writeMetadata(writer, "info", metadata);
        for (InfoSnapshot.InfoDataPointSnapshot data : snapshot.getDataPoints()) {
            this.writeNameAndLabels(writer, metadata.getPrometheusName(), "_info", data.getLabels());
            writer.write("1");
            this.writeScrapeTimestampAndExemplar(writer, data, null);
        }
    }

    private void writeStateSet(Writer writer, StateSetSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        this.writeMetadata(writer, "stateset", metadata);
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
                this.writeScrapeTimestampAndExemplar(writer, data, null);
            }
        }
    }

    private void writeUnknown(Writer writer, UnknownSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        this.writeMetadata(writer, "unknown", metadata);
        for (UnknownSnapshot.UnknownDataPointSnapshot data : snapshot.getDataPoints()) {
            this.writeNameAndLabels(writer, metadata.getPrometheusName(), null, data.getLabels());
            TextFormatUtil.writeDouble(writer, data.getValue());
            if (this.exemplarsOnAllMetricTypesEnabled) {
                this.writeScrapeTimestampAndExemplar(writer, data, data.getExemplar());
                continue;
            }
            this.writeScrapeTimestampAndExemplar(writer, data, null);
        }
    }

    private void writeCountAndSum(Writer writer, MetricMetadata metadata, DistributionDataPointSnapshot data, String countSuffix, String sumSuffix, Exemplars exemplars) throws IOException {
        if (data.hasCount()) {
            this.writeNameAndLabels(writer, metadata.getPrometheusName(), countSuffix, data.getLabels());
            TextFormatUtil.writeLong(writer, data.getCount());
            if (this.exemplarsOnAllMetricTypesEnabled) {
                this.writeScrapeTimestampAndExemplar(writer, data, exemplars.getLatest());
            } else {
                this.writeScrapeTimestampAndExemplar(writer, data, null);
            }
        }
        if (data.hasSum()) {
            this.writeNameAndLabels(writer, metadata.getPrometheusName(), sumSuffix, data.getLabels());
            TextFormatUtil.writeDouble(writer, data.getSum());
            this.writeScrapeTimestampAndExemplar(writer, data, null);
        }
    }

    private void writeCreated(Writer writer, MetricMetadata metadata, DataPointSnapshot data) throws IOException {
        if (this.createdTimestampsEnabled && data.hasCreatedTimestamp()) {
            this.writeNameAndLabels(writer, metadata.getPrometheusName(), "_created", data.getLabels());
            TextFormatUtil.writeOpenMetricsTimestamp(writer, data.getCreatedTimestampMillis());
            if (data.hasScrapeTimestamp()) {
                writer.write(32);
                TextFormatUtil.writeOpenMetricsTimestamp(writer, data.getScrapeTimestampMillis());
            }
            writer.write(10);
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

    private void writeScrapeTimestampAndExemplar(Writer writer, DataPointSnapshot data, Exemplar exemplar) throws IOException {
        if (data.hasScrapeTimestamp()) {
            writer.write(32);
            TextFormatUtil.writeOpenMetricsTimestamp(writer, data.getScrapeTimestampMillis());
        }
        if (exemplar != null) {
            writer.write(" # ");
            TextFormatUtil.writeLabels(writer, exemplar.getLabels(), null, 0.0);
            writer.write(32);
            TextFormatUtil.writeDouble(writer, exemplar.getValue());
            if (exemplar.hasTimestamp()) {
                writer.write(32);
                TextFormatUtil.writeOpenMetricsTimestamp(writer, exemplar.getTimestampMillis());
            }
        }
        writer.write(10);
    }

    private void writeMetadata(Writer writer, String typeName, MetricMetadata metadata) throws IOException {
        writer.write("# TYPE ");
        writer.write(metadata.getPrometheusName());
        writer.write(32);
        writer.write(typeName);
        writer.write(10);
        if (metadata.getUnit() != null) {
            writer.write("# UNIT ");
            writer.write(metadata.getPrometheusName());
            writer.write(32);
            TextFormatUtil.writeEscapedLabelValue(writer, metadata.getUnit().toString());
            writer.write(10);
        }
        if (metadata.getHelp() != null && !metadata.getHelp().isEmpty()) {
            writer.write("# HELP ");
            writer.write(metadata.getPrometheusName());
            writer.write(32);
            TextFormatUtil.writeEscapedLabelValue(writer, metadata.getHelp());
            writer.write(10);
        }
    }

    public static class Builder {
        boolean createdTimestampsEnabled;
        boolean exemplarsOnAllMetricTypesEnabled;

        private Builder() {
        }

        public Builder setCreatedTimestampsEnabled(boolean createdTimestampsEnabled) {
            this.createdTimestampsEnabled = createdTimestampsEnabled;
            return this;
        }

        public Builder setExemplarsOnAllMetricTypesEnabled(boolean exemplarsOnAllMetricTypesEnabled) {
            this.exemplarsOnAllMetricTypesEnabled = exemplarsOnAllMetricTypesEnabled;
            return this;
        }

        public OpenMetricsTextFormatWriter build() {
            return new OpenMetricsTextFormatWriter(this.createdTimestampsEnabled, this.exemplarsOnAllMetricTypesEnabled);
        }
    }
}

