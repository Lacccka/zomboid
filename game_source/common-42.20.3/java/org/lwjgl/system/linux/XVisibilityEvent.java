/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.linux;

import java.nio.ByteBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.Checks;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

public class XVisibilityEvent
extends Struct<XVisibilityEvent>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int TYPE;
    public static final int SERIAL;
    public static final int SEND_EVENT;
    public static final int DISPLAY;
    public static final int WINDOW;
    public static final int STATE;

    protected XVisibilityEvent(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected XVisibilityEvent create(long address, @Nullable ByteBuffer container) {
        return new XVisibilityEvent(address, container);
    }

    public XVisibilityEvent(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), XVisibilityEvent.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    public int type() {
        return XVisibilityEvent.ntype(this.address());
    }

    @NativeType(value="unsigned long")
    public long serial() {
        return XVisibilityEvent.nserial(this.address());
    }

    @NativeType(value="Bool")
    public boolean send_event() {
        return XVisibilityEvent.nsend_event(this.address()) != 0;
    }

    @NativeType(value="Display *")
    public long display() {
        return XVisibilityEvent.ndisplay(this.address());
    }

    @NativeType(value="Window")
    public long window() {
        return XVisibilityEvent.nwindow(this.address());
    }

    public int state() {
        return XVisibilityEvent.nstate(this.address());
    }

    public XVisibilityEvent type(int value) {
        XVisibilityEvent.ntype(this.address(), value);
        return this;
    }

    public XVisibilityEvent serial(@NativeType(value="unsigned long") long value) {
        XVisibilityEvent.nserial(this.address(), value);
        return this;
    }

    public XVisibilityEvent send_event(@NativeType(value="Bool") boolean value) {
        XVisibilityEvent.nsend_event(this.address(), value ? 1 : 0);
        return this;
    }

    public XVisibilityEvent display(@NativeType(value="Display *") long value) {
        XVisibilityEvent.ndisplay(this.address(), value);
        return this;
    }

    public XVisibilityEvent window(@NativeType(value="Window") long value) {
        XVisibilityEvent.nwindow(this.address(), value);
        return this;
    }

    public XVisibilityEvent state(int value) {
        XVisibilityEvent.nstate(this.address(), value);
        return this;
    }

    public XVisibilityEvent set(int type, long serial, boolean send_event, long display, long window, int state) {
        this.type(type);
        this.serial(serial);
        this.send_event(send_event);
        this.display(display);
        this.window(window);
        this.state(state);
        return this;
    }

    public XVisibilityEvent set(XVisibilityEvent src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static XVisibilityEvent malloc() {
        return new XVisibilityEvent(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static XVisibilityEvent calloc() {
        return new XVisibilityEvent(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static XVisibilityEvent create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new XVisibilityEvent(MemoryUtil.memAddress(container), container);
    }

    public static XVisibilityEvent create(long address) {
        return new XVisibilityEvent(address, null);
    }

    public static @Nullable XVisibilityEvent createSafe(long address) {
        return address == 0L ? null : new XVisibilityEvent(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(XVisibilityEvent.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = XVisibilityEvent.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static XVisibilityEvent malloc(MemoryStack stack) {
        return new XVisibilityEvent(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static XVisibilityEvent calloc(MemoryStack stack) {
        return new XVisibilityEvent(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static int ntype(long struct) {
        return MemoryUtil.memGetInt(struct + (long)TYPE);
    }

    public static long nserial(long struct) {
        return MemoryUtil.memGetCLong(struct + (long)SERIAL);
    }

    public static int nsend_event(long struct) {
        return MemoryUtil.memGetInt(struct + (long)SEND_EVENT);
    }

    public static long ndisplay(long struct) {
        return MemoryUtil.memGetAddress(struct + (long)DISPLAY);
    }

    public static long nwindow(long struct) {
        return MemoryUtil.memGetCLong(struct + (long)WINDOW);
    }

    public static int nstate(long struct) {
        return MemoryUtil.memGetInt(struct + (long)STATE);
    }

    public static void ntype(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)TYPE, value);
    }

    public static void nserial(long struct, long value) {
        MemoryUtil.memPutCLong(struct + (long)SERIAL, value);
    }

    public static void nsend_event(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)SEND_EVENT, value);
    }

    public static void ndisplay(long struct, long value) {
        MemoryUtil.memPutAddress(struct + (long)DISPLAY, Checks.check(value));
    }

    public static void nwindow(long struct, long value) {
        MemoryUtil.memPutCLong(struct + (long)WINDOW, value);
    }

    public static void nstate(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)STATE, value);
    }

    public static void validate(long struct) {
        Checks.check(MemoryUtil.memGetAddress(struct + (long)DISPLAY));
    }

    static {
        Struct.Layout layout = XVisibilityEvent.__struct(XVisibilityEvent.__member(4), XVisibilityEvent.__member(CLONG_SIZE), XVisibilityEvent.__member(4), XVisibilityEvent.__member(POINTER_SIZE), XVisibilityEvent.__member(CLONG_SIZE), XVisibilityEvent.__member(4));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        TYPE = layout.offsetof(0);
        SERIAL = layout.offsetof(1);
        SEND_EVENT = layout.offsetof(2);
        DISPLAY = layout.offsetof(3);
        WINDOW = layout.offsetof(4);
        STATE = layout.offsetof(5);
    }

    public static class Buffer
    extends StructBuffer<XVisibilityEvent, Buffer>
    implements NativeResource {
        private static final XVisibilityEvent ELEMENT_FACTORY = XVisibilityEvent.create(-1L);

        public Buffer(ByteBuffer container) {
            super(container, container.remaining() / SIZEOF);
        }

        public Buffer(long address, int cap) {
            super(address, null, -1, 0, cap, cap);
        }

        Buffer(long address, @Nullable ByteBuffer container, int mark, int pos, int lim, int cap) {
            super(address, container, mark, pos, lim, cap);
        }

        @Override
        protected Buffer self() {
            return this;
        }

        @Override
        protected Buffer create(long address, @Nullable ByteBuffer container, int mark, int position, int limit, int capacity) {
            return new Buffer(address, container, mark, position, limit, capacity);
        }

        @Override
        protected XVisibilityEvent getElementFactory() {
            return ELEMENT_FACTORY;
        }

        public int type() {
            return XVisibilityEvent.ntype(this.address());
        }

        @NativeType(value="unsigned long")
        public long serial() {
            return XVisibilityEvent.nserial(this.address());
        }

        @NativeType(value="Bool")
        public boolean send_event() {
            return XVisibilityEvent.nsend_event(this.address()) != 0;
        }

        @NativeType(value="Display *")
        public long display() {
            return XVisibilityEvent.ndisplay(this.address());
        }

        @NativeType(value="Window")
        public long window() {
            return XVisibilityEvent.nwindow(this.address());
        }

        public int state() {
            return XVisibilityEvent.nstate(this.address());
        }

        public Buffer type(int value) {
            XVisibilityEvent.ntype(this.address(), value);
            return this;
        }

        public Buffer serial(@NativeType(value="unsigned long") long value) {
            XVisibilityEvent.nserial(this.address(), value);
            return this;
        }

        public Buffer send_event(@NativeType(value="Bool") boolean value) {
            XVisibilityEvent.nsend_event(this.address(), value ? 1 : 0);
            return this;
        }

        public Buffer display(@NativeType(value="Display *") long value) {
            XVisibilityEvent.ndisplay(this.address(), value);
            return this;
        }

        public Buffer window(@NativeType(value="Window") long value) {
            XVisibilityEvent.nwindow(this.address(), value);
            return this;
        }

        public Buffer state(int value) {
            XVisibilityEvent.nstate(this.address(), value);
            return this;
        }
    }
}

