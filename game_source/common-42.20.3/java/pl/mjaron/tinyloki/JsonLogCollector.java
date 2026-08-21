/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import pl.mjaron.tinyloki.IBuffering;
import pl.mjaron.tinyloki.ILogCollector;
import pl.mjaron.tinyloki.ILogListener;
import pl.mjaron.tinyloki.ILogStream;
import pl.mjaron.tinyloki.ITimestampProvider;
import pl.mjaron.tinyloki.ITimestampProviderFactory;
import pl.mjaron.tinyloki.JsonLogStream;
import pl.mjaron.tinyloki.LabelSettings;
import pl.mjaron.tinyloki.Labels;

public class JsonLogCollector
implements ILogCollector {
    public static final String CONTENT_TYPE = "application/json";
    private final List<JsonLogStream> streams = new ArrayList<JsonLogStream>();
    private int logEntriesCount = 0;
    private ILogListener logListener = null;
    private IBuffering bufferingManager = null;
    private LabelSettings structuredMetadataLabelSettings = null;
    private ITimestampProvider timestampProvider = null;

    @Override
    public void configure(ILogListener logListener, IBuffering bufferingManager, LabelSettings structuredMetadataLabelSettings, ITimestampProviderFactory timestampProviderFactory) {
        this.logListener = logListener;
        this.bufferingManager = bufferingManager;
        this.structuredMetadataLabelSettings = structuredMetadataLabelSettings;
        this.timestampProvider = ITimestampProviderFactory.orDefault(timestampProviderFactory).create();
    }

    public LabelSettings getStructuredMetadataLabelSettings() {
        return this.structuredMetadataLabelSettings;
    }

    @Override
    public synchronized ILogStream createStream(Labels labels) {
        JsonLogStream stream = new JsonLogStream(this, labels);
        this.streams.add(stream);
        return stream;
    }

    public synchronized void onStreamReleased(ILogStream stream) {
        this.streams.remove((JsonLogStream)stream);
    }

    public synchronized String collectAsString() {
        StringBuilder b = new StringBuilder("{\"streams\":[");
        boolean isFirst = true;
        boolean anyStreamNotEmpty = false;
        for (JsonLogStream stream : this.streams) {
            String streamData = stream.flush();
            if (streamData == null) continue;
            if (isFirst) {
                isFirst = false;
            } else {
                b.append(',');
            }
            b.append(streamData);
            anyStreamNotEmpty = true;
        }
        if (!anyStreamNotEmpty) {
            return null;
        }
        b.append("]}");
        return b.toString();
    }

    @Override
    public byte[] collect() {
        String collectedAsString = this.collectAsString();
        if (collectedAsString == null) {
            return null;
        }
        return collectedAsString.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public synchronized byte[][] collectAll() {
        return this.bufferingManager.collectAll();
    }

    @Override
    public String contentType() {
        return CONTENT_TYPE;
    }

    synchronized void logImplementation(JsonLogStream stream, long timestamp, String line, Labels structuredMetadata) {
        int logCandidateSize = 25 + line.length();
        if (!this.bufferingManager.beforeLog(logCandidateSize)) {
            return;
        }
        if (timestamp == -1L) {
            timestamp = this.timestampProvider.next(line);
        }
        int acceptedLogSize = stream.logUnsafe(timestamp, line, structuredMetadata);
        this.bufferingManager.logAccepted(acceptedLogSize);
        ++this.logEntriesCount;
        this.logListener.onLog(this.logEntriesCount);
    }
}

