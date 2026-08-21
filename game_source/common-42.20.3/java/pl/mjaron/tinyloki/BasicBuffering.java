/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import java.util.ArrayList;
import java.util.List;
import pl.mjaron.tinyloki.IBuffering;
import pl.mjaron.tinyloki.IExecutor;
import pl.mjaron.tinyloki.ILogCollector;
import pl.mjaron.tinyloki.ILogMonitor;

public class BasicBuffering
implements IBuffering {
    private ILogCollector logCollector;
    private int maxMessageSize = 0x200000;
    private int maxBuffersCount = 8;
    private IExecutor executor;
    private ILogMonitor logMonitor;
    private final List<byte[]> buffers = new ArrayList<byte[]>();
    private int alreadyBufferedLogsSize = 0;

    public BasicBuffering() {
    }

    public BasicBuffering(int maxMessageSize, int maxBuffersCount) {
        if (maxMessageSize > 0) {
            this.maxMessageSize = maxMessageSize;
        }
        if (maxBuffersCount > 0) {
            this.maxBuffersCount = maxBuffersCount;
        }
    }

    @Override
    public void configure(ILogCollector logCollector, int maxMessageSize, int maxBuffersCount, IExecutor executor, ILogMonitor logMonitor) {
        this.logCollector = logCollector;
        if (maxMessageSize > 0) {
            this.maxMessageSize = maxMessageSize;
        }
        if (maxBuffersCount > 0) {
            this.maxBuffersCount = maxBuffersCount;
        }
        this.executor = executor;
        this.logMonitor = logMonitor;
    }

    public List<byte[]> getBuffers() {
        return this.buffers;
    }

    private void addBuffer(byte[] collected) {
        if (collected == null) {
            return;
        }
        this.buffers.add(collected);
        if (this.buffers.size() > this.maxBuffersCount) {
            this.logMonitor.logError("Removing buffer due to max buffer count overflow: [" + this.buffers.size() + "].");
            this.buffers.remove(0);
        }
        if (this.buffers.size() > 1) {
            this.executor.flush();
        }
    }

    @Override
    public boolean beforeLog(int logCandidateSize) {
        if (logCandidateSize > this.maxMessageSize) {
            this.logMonitor.logError("Dropping the log due to size: [" + logCandidateSize + "] which exceeds max size: [" + this.maxMessageSize + "].");
            return false;
        }
        int bufferCandidateSize = logCandidateSize + this.alreadyBufferedLogsSize;
        if (bufferCandidateSize > this.maxMessageSize) {
            this.alreadyBufferedLogsSize = 0;
            byte[] collected = this.logCollector.collect();
            this.addBuffer(collected);
            return true;
        }
        return true;
    }

    @Override
    public void logAccepted(int logSize) {
        this.alreadyBufferedLogsSize += logSize;
    }

    @Override
    public byte[][] collectAll() {
        this.alreadyBufferedLogsSize = 0;
        byte[] collected = this.logCollector.collect();
        this.addBuffer(collected);
        if (this.buffers.isEmpty()) {
            return null;
        }
        byte[][] exported = new byte[this.buffers.size()][];
        for (int i = 0; i < this.buffers.size(); ++i) {
            exported[i] = this.buffers.get(i);
        }
        this.buffers.clear();
        return exported;
    }
}

