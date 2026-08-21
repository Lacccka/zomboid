/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.examples;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import org.apache.commons.compress.archivers.examples.CloseableConsumer;

final class CloseableConsumerAdapter
implements Closeable {
    private final CloseableConsumer consumer;
    private Closeable closeable;

    CloseableConsumerAdapter(CloseableConsumer consumer) {
        this.consumer = Objects.requireNonNull(consumer, "consumer");
    }

    @Override
    public void close() throws IOException {
        if (this.closeable != null) {
            this.consumer.accept(this.closeable);
        }
    }

    <C extends Closeable> C track(C closeable) {
        this.closeable = closeable;
        return closeable;
    }
}

