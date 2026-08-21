/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.model.snapshots.Labels;
import java.io.IOException;
import java.io.Writer;

public class TextFormatUtil {
    static void writeLong(Writer writer, long value) throws IOException {
        writer.append(Long.toString(value));
    }

    static void writeDouble(Writer writer, double d) throws IOException {
        if (d == Double.POSITIVE_INFINITY) {
            writer.write("+Inf");
        } else if (d == Double.NEGATIVE_INFINITY) {
            writer.write("-Inf");
        } else {
            writer.write(Double.toString(d));
        }
    }

    static void writePrometheusTimestamp(Writer writer, long timestampMs, boolean timestampsInMs) throws IOException {
        if (timestampsInMs) {
            writer.write(Long.toString(timestampMs));
        } else {
            TextFormatUtil.writeOpenMetricsTimestamp(writer, timestampMs);
        }
    }

    static void writeOpenMetricsTimestamp(Writer writer, long timestampMs) throws IOException {
        writer.write(Long.toString(timestampMs / 1000L));
        writer.write(".");
        long ms = timestampMs % 1000L;
        if (ms < 100L) {
            writer.write("0");
        }
        if (ms < 10L) {
            writer.write("0");
        }
        writer.write(Long.toString(ms));
    }

    static void writeEscapedLabelValue(Writer writer, String s) throws IOException {
        int start = 0;
        int backslashIndex = s.indexOf(92, start);
        int quoteIndex = s.indexOf(34, start);
        int newlineIndex = s.indexOf(10, start);
        int allEscapesIndex = backslashIndex & quoteIndex & newlineIndex;
        while (allEscapesIndex != -1) {
            int escapeStart = Integer.MAX_VALUE;
            if (backslashIndex != -1) {
                escapeStart = backslashIndex;
            }
            if (quoteIndex != -1) {
                escapeStart = Math.min(escapeStart, quoteIndex);
            }
            if (newlineIndex != -1) {
                escapeStart = Math.min(escapeStart, newlineIndex);
            }
            if (escapeStart > start) {
                writer.write(s, start, escapeStart - start);
            }
            char c = s.charAt(escapeStart);
            start = escapeStart + 1;
            switch (c) {
                case '\\': {
                    writer.write("\\\\");
                    backslashIndex = s.indexOf(92, start);
                    break;
                }
                case '\"': {
                    writer.write("\\\"");
                    quoteIndex = s.indexOf(34, start);
                    break;
                }
                case '\n': {
                    writer.write("\\n");
                    newlineIndex = s.indexOf(10, start);
                }
            }
            allEscapesIndex = backslashIndex & quoteIndex & newlineIndex;
        }
        int remaining = s.length() - start;
        if (remaining > 0) {
            writer.write(s, start, remaining);
        }
    }

    static void writeLabels(Writer writer, Labels labels, String additionalLabelName, double additionalLabelValue) throws IOException {
        writer.write(123);
        for (int i = 0; i < labels.size(); ++i) {
            if (i > 0) {
                writer.write(",");
            }
            writer.write(labels.getPrometheusName(i));
            writer.write("=\"");
            TextFormatUtil.writeEscapedLabelValue(writer, labels.getValue(i));
            writer.write("\"");
        }
        if (additionalLabelName != null) {
            if (!labels.isEmpty()) {
                writer.write(",");
            }
            writer.write(additionalLabelName);
            writer.write("=\"");
            TextFormatUtil.writeDouble(writer, additionalLabelValue);
            writer.write("\"");
        }
        writer.write(125);
    }
}

