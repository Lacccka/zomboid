/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.InputStream;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.output.QueueOutputStream;

public class QueueInputStream
extends InputStream {
    private final BlockingQueue<Integer> blockingQueue;
    private final long timeoutNanos;

    public static Builder builder() {
        return new Builder();
    }

    public QueueInputStream() {
        this(new LinkedBlockingQueue<Integer>());
    }

    @Deprecated
    public QueueInputStream(BlockingQueue<Integer> blockingQueue) {
        this(blockingQueue, Duration.ZERO);
    }

    private QueueInputStream(BlockingQueue<Integer> blockingQueue, Duration timeout2) {
        this.blockingQueue = Objects.requireNonNull(blockingQueue, "blockingQueue");
        this.timeoutNanos = Objects.requireNonNull(timeout2, "timeout").toNanos();
    }

    BlockingQueue<Integer> getBlockingQueue() {
        return this.blockingQueue;
    }

    Duration getTimeout() {
        return Duration.ofNanos(this.timeoutNanos);
    }

    public QueueOutputStream newQueueOutputStream() {
        return new QueueOutputStream(this.blockingQueue);
    }

    @Override
    public int read() {
        try {
            Integer value = this.blockingQueue.poll(this.timeoutNanos, TimeUnit.NANOSECONDS);
            return value == null ? -1 : 0xFF & value;
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    public static class Builder
    extends AbstractStreamBuilder<QueueInputStream, Builder> {
        private BlockingQueue<Integer> blockingQueue = new LinkedBlockingQueue<Integer>();
        private Duration timeout = Duration.ZERO;

        @Override
        public QueueInputStream get() {
            return new QueueInputStream(this.blockingQueue, this.timeout);
        }

        public Builder setBlockingQueue(BlockingQueue<Integer> blockingQueue) {
            this.blockingQueue = blockingQueue != null ? blockingQueue : new LinkedBlockingQueue();
            return this;
        }

        public Builder setTimeout(Duration timeout2) {
            if (timeout2 != null && timeout2.toNanos() < 0L) {
                throw new IllegalArgumentException("timeout must not be negative");
            }
            this.timeout = timeout2 != null ? timeout2 : Duration.ZERO;
            return this;
        }
    }
}

