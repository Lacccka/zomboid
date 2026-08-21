/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import java.util.Map;
import pl.mjaron.tinyloki.ILogStream;
import pl.mjaron.tinyloki.JsonLogCollector;
import pl.mjaron.tinyloki.Labels;
import pl.mjaron.tinyloki.Utils;

public class JsonLogStream
implements ILogStream {
    private static final String SEQUENCE_OPENING_STREAM = "{\"stream\":{";
    private static final String SEQUENCE_CLOSING_STREAM = "]}";
    private final JsonLogCollector collector;
    private final StringBuilder b = new StringBuilder("{\"stream\":{");
    private final String initialSequenceWithHeaders;
    private int cachedLogsCount = 0;

    public JsonLogStream(JsonLogCollector collector, Labels labels) {
        this.collector = collector;
        boolean isFirst = true;
        for (Map.Entry<String, String> entry : labels.getMap().entrySet()) {
            if (isFirst) {
                isFirst = false;
            } else {
                this.b.append(',');
            }
            this.b.append('\"');
            this.b.append(entry.getKey());
            this.b.append("\":\"");
            Utils.escapeJsonString(this.b, entry.getValue());
            this.b.append('\"');
        }
        this.b.append("},\"values\":[");
        this.initialSequenceWithHeaders = this.b.toString();
    }

    int logUnsafe(long timestamp, String line, Labels structuredMetadata) {
        int beforeSize = this.b.length();
        if (this.cachedLogsCount != 0) {
            this.b.append(',');
        }
        ++this.cachedLogsCount;
        this.b.append("[\"");
        this.b.append(timestamp);
        this.b.append("\",\"");
        Utils.escapeJsonString(this.b, line);
        this.b.append('\"');
        if (structuredMetadata != null && this.collector.getStructuredMetadataLabelSettings() != null) {
            Labels prettified = Labels.prettify(structuredMetadata, this.collector.getStructuredMetadataLabelSettings());
            this.b.append(",{");
            boolean isFirst = true;
            for (Map.Entry<String, String> entry : prettified.getMap().entrySet()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    this.b.append(',');
                }
                this.b.append('\"');
                this.b.append(entry.getKey());
                this.b.append("\":\"");
                Utils.escapeJsonString(this.b, entry.getValue());
                this.b.append('\"');
            }
            this.b.append("}");
        }
        this.b.append("]");
        return this.b.length() - beforeSize;
    }

    @Override
    public void log(long timestampNs, String line, Labels structuredMetadata) {
        this.collector.logImplementation(this, timestampNs, line, structuredMetadata);
    }

    @Override
    public void release() {
        this.collector.onStreamReleased(this);
    }

    public void closeStreamsEntryTag() {
        this.b.append(SEQUENCE_CLOSING_STREAM);
    }

    public StringBuilder getStringBuilder() {
        return this.b;
    }

    public void clear() {
        this.b.setLength(0);
        this.b.append(this.initialSequenceWithHeaders);
        this.cachedLogsCount = 0;
    }

    public String flush() {
        if (this.cachedLogsCount == 0) {
            return null;
        }
        this.closeStreamsEntryTag();
        String result = this.b.toString();
        this.clear();
        return result;
    }
}

