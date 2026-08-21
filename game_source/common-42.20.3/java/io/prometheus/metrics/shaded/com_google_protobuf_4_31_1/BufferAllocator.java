/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.AllocatedBuffer;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.CheckReturnValue;
import java.nio.ByteBuffer;

@CheckReturnValue
abstract class BufferAllocator {
    private static final BufferAllocator UNPOOLED = new BufferAllocator(){

        @Override
        public AllocatedBuffer allocateHeapBuffer(int capacity) {
            return AllocatedBuffer.wrap(new byte[capacity]);
        }

        @Override
        public AllocatedBuffer allocateDirectBuffer(int capacity) {
            return AllocatedBuffer.wrap(ByteBuffer.allocateDirect(capacity));
        }
    };

    BufferAllocator() {
    }

    public static BufferAllocator unpooled() {
        return UNPOOLED;
    }

    public abstract AllocatedBuffer allocateHeapBuffer(int var1);

    public abstract AllocatedBuffer allocateDirectBuffer(int var1);
}

