/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.ValueLayout;
import java.util.Arrays;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Checks;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.StackWalkUtil;
import org.lwjgl.system.ffm.StackAllocator;

public class SegmentStack
implements StackAllocator<SegmentStack>,
AutoCloseable {
    private static final long DEFAULT_STACK_SIZE = (long)Configuration.STACK_SIZE.get(64).intValue() * 1024L;
    private static final int DEFAULT_STACK_FRAMES = 8;
    private static final ThreadLocal<SegmentStack> TLS = ThreadLocal.withInitial(SegmentStack::create);
    private final MemorySegment container;
    private final long size;
    private long pointer;
    private long[] frames;
    protected int frameIndex;

    protected SegmentStack(MemorySegment container) {
        this.container = container;
        this.size = container.byteSize();
        this.pointer = container.byteSize();
        this.frames = new long[8];
    }

    public static SegmentStack create() {
        return SegmentStack.create(DEFAULT_STACK_SIZE);
    }

    public static SegmentStack create(long capacity) {
        return SegmentStack.create(Arena.ofAuto().allocate(capacity));
    }

    public static SegmentStack create(MemorySegment segment) {
        return Configuration.DEBUG_STACK.get(false) != false ? new DebugMemoryStack(segment) : new SegmentStack(segment);
    }

    @Override
    public SegmentStack push() {
        if (this.frameIndex == this.frames.length) {
            this.frameOverflow();
        }
        this.frames[this.frameIndex++] = this.pointer;
        return this;
    }

    private void frameOverflow() {
        if (Checks.DEBUG) {
            APIUtil.apiLog("[WARNING] Out of frame stack space (" + this.frames.length + ") in thread: " + String.valueOf(Thread.currentThread()));
        }
        this.frames = Arrays.copyOf(this.frames, this.frames.length * 3 / 2);
    }

    @Override
    public SegmentStack pop() {
        this.pointer = this.frames[--this.frameIndex];
        return this;
    }

    @Override
    public void close() {
        this.pop();
    }

    public long getAddress() {
        return this.container.address();
    }

    public long getSize() {
        return this.container.byteSize();
    }

    public int getFrameIndex() {
        return this.frameIndex;
    }

    public long getPointerAddress() {
        return this.container.address() + this.pointer;
    }

    public long getPointer() {
        return this.pointer;
    }

    public void setPointer(long pointer) {
        if (Checks.CHECKS) {
            this.checkPointer(pointer);
        }
        this.pointer = pointer;
    }

    private void checkPointer(long pointer) {
        if (pointer < 0L || this.size < pointer) {
            throw new IndexOutOfBoundsException("Invalid stack pointer");
        }
    }

    private static void checkAlignment(long alignment) {
        if (Long.bitCount(alignment) != 1) {
            throw new IllegalArgumentException("Alignment must be a power-of-two value.");
        }
    }

    @Override
    public MemorySegment allocate(long byteSize) {
        return this.allocate(byteSize, ValueLayout.ADDRESS.byteAlignment());
    }

    @Override
    public MemorySegment allocate(long byteSize, long byteAlignment) {
        if (Checks.DEBUG) {
            SegmentStack.checkAlignment(byteAlignment);
        }
        long address = this.container.address() + this.pointer - byteSize & -byteAlignment;
        this.pointer = address - this.container.address();
        if (Checks.CHECKS && this.pointer < 0L) {
            throw new OutOfMemoryError("Out of stack space.");
        }
        return this.container.asSlice(this.pointer, byteSize, 1L);
    }

    @Override
    public MemorySegment allocate(MemoryLayout layout) {
        Objects.requireNonNull(layout);
        return this.allocate(layout.byteSize(), layout.byteAlignment());
    }

    @Override
    public MemorySegment allocate(MemoryLayout elementLayout, long count) {
        Objects.requireNonNull(elementLayout);
        if (count < 0L) {
            throw new IllegalArgumentException("Negative array size");
        }
        SequenceLayout layout = MemoryLayout.sequenceLayout(count, elementLayout);
        return this.allocate(layout.byteSize(), layout.byteAlignment());
    }

    public MemorySegment calloc(long byteSize) {
        return this.allocate(byteSize).fill((byte)0);
    }

    public MemorySegment calloc(long byteSize, long byteAlignment) {
        return this.allocate(byteSize, byteAlignment).fill((byte)0);
    }

    public MemorySegment calloc(MemoryLayout layout) {
        return this.allocate(layout).fill((byte)0);
    }

    public MemorySegment calloc(MemoryLayout elementLayout, long count) {
        return this.allocate(elementLayout, count).fill((byte)0);
    }

    public static SegmentStack stackGet() {
        return TLS.get();
    }

    public static SegmentStack stackPush() {
        return SegmentStack.stackGet().push();
    }

    static {
        if (DEFAULT_STACK_SIZE < 0L) {
            throw new IllegalStateException("Invalid stack size.");
        }
    }

    private static class DebugMemoryStack
    extends SegmentStack {
        private @Nullable Object[] debugFrames = new Object[8];

        DebugMemoryStack(MemorySegment container) {
            super(container);
        }

        @Override
        public SegmentStack push() {
            if (this.frameIndex == this.debugFrames.length) {
                this.frameOverflow();
            }
            this.debugFrames[this.frameIndex] = StackWalkUtil.stackWalkGetMethod(SegmentStack.class);
            return super.push();
        }

        @Override
        private void frameOverflow() {
            this.debugFrames = Arrays.copyOf(this.debugFrames, this.debugFrames.length * 3 / 2);
        }

        @Override
        public SegmentStack pop() {
            Object pushed = Objects.requireNonNull(this.debugFrames[this.frameIndex - 1]);
            Object popped = StackWalkUtil.stackWalkCheckPop(SegmentStack.class, pushed);
            if (popped != null) {
                DebugMemoryStack.reportAsymmetricPop(pushed, popped);
            }
            this.debugFrames[this.frameIndex - 1] = null;
            return super.pop();
        }

        @Override
        public void close() {
            this.debugFrames[this.frameIndex - 1] = null;
            super.pop();
        }

        private static void reportAsymmetricPop(Object pushed, Object popped) {
            APIUtil.DEBUG_STREAM.format("[LWJGL] Asymmetric pop detected:\n\tPUSHED: %s\n\tPOPPED: %s\n\tTHREAD: %s\n", pushed, popped, Thread.currentThread());
        }
    }
}

