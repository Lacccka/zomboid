/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.TypeDescriptor;
import java.lang.runtime.ObjectMethods;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.Checks;
import org.lwjgl.system.SegmentStack;
import org.lwjgl.system.ffm.Binder;
import org.lwjgl.system.ffm.GroupArray;
import org.lwjgl.system.ffm.GroupSpliterator;
import org.lwjgl.system.ffm.StructBinder;
import org.lwjgl.system.ffm.UnionBinder;
import org.lwjgl.system.ffm.mapping.GroupMapping;

public sealed interface GroupBinder<L extends GroupLayout, T>
extends Binder<T>,
GroupMapping<L>
permits StructBinder, UnionBinder {
    @Override
    public L layout();

    public T ofAddress(long var1);

    public long addressOf(T var1);

    public T copy(T var1, T var2);

    public T clear(T var1);

    public T get(MemorySegment var1);

    public T get(MemorySegment var1, long var2);

    public T getAtIndex(MemorySegment var1, long var2);

    default public long sizeof() {
        return this.layout().byteSize();
    }

    default public long alignof() {
        return this.layout().byteAlignment();
    }

    default public @Nullable T ofAddressSafe(long address) {
        return address == 0L ? null : (T)this.ofAddress(address);
    }

    default public long addressOfSafe(@Nullable T value) {
        return value == null ? 0L : this.addressOf(value);
    }

    public GroupBinder<L, T> set(MemorySegment var1, T var2);

    public GroupBinder<L, T> set(MemorySegment var1, long var2, T var4);

    public GroupBinder<L, T> setAtIndex(MemorySegment var1, long var2, T var4);

    public GroupArray<L, T> array(MemorySegment var1);

    public GroupArray<L, T> array(MemorySegment var1, long var2);

    public GroupArray<L, T> array(MemorySegment var1, long var2, long var4);

    default public MemorySegment asSegment(T value) {
        return MemorySegment.ofAddress(this.addressOf(value)).reinterpret(this.layout().byteSize());
    }

    default public T malloc(SegmentStack stack) {
        return this.get(stack.allocate(this.layout()));
    }

    public GroupArray<L, T> malloc(SegmentStack var1, long var2);

    default public T allocate(SegmentStack stack) {
        return this.get(stack.calloc(this.layout()));
    }

    default public T allocate(SegmentAllocator allocator) {
        return this.get(allocator.allocate(this.layout()));
    }

    public GroupArray<L, T> allocate(SegmentStack var1, long var2);

    public GroupArray<L, T> allocate(SegmentAllocator var1, long var2);

    public GroupBinder<L, T> apply(MemorySegment var1, long var2, Consumer<T> var4);

    public GroupBinder<L, T> applyAtIndex(MemorySegment var1, long var2, Consumer<T> var4);

    default public void forEach(MemorySegment segment, Consumer<? super T> action) {
        Objects.requireNonNull(action);
        long sizeof = this.sizeof();
        long fence = segment.byteSize();
        long offset = 0L;
        while (offset + sizeof <= fence) {
            action.accept(this.get(segment, offset));
            offset += sizeof;
        }
    }

    default public Iterable<T> iterable(MemorySegment segment) {
        return () -> this.iterator(segment);
    }

    default public Iterator<T> iterator(final MemorySegment segment) {
        return new Iterator<T>(this){
            private final long sizeof;
            private final long fence;
            private long offset;
            final /* synthetic */ GroupBinder this$0;
            {
                GroupBinder groupBinder = this$0;
                Objects.requireNonNull(groupBinder);
                this.this$0 = groupBinder;
                this.sizeof = this.this$0.sizeof();
                this.fence = segment.byteSize();
            }

            @Override
            public boolean hasNext() {
                return this.offset + this.sizeof <= this.fence;
            }

            @Override
            public T next() {
                if (Checks.DEBUG && this.fence < this.offset + this.sizeof) {
                    throw new NoSuchElementException();
                }
                try {
                    Object t = this.this$0.get(segment, this.offset);
                    return t;
                }
                finally {
                    this.offset += this.sizeof;
                }
            }
        };
    }

    default public Spliterator<T> spliterator(MemorySegment segment) {
        return new GroupSpliterator(this, segment, 0L, segment.byteSize() / this.sizeof());
    }

    default public Stream<T> stream(MemorySegment segment) {
        return StreamSupport.stream(this.spliterator(segment), false);
    }

    default public Stream<T> parallelStream(MemorySegment segment) {
        return StreamSupport.stream(this.spliterator(segment), true);
    }

    default public MemorySegment asSlice(MemorySegment segment, long index) {
        return segment.asSlice(this.layout().byteSize() * index);
    }

    default public MemorySegment asSlice(MemorySegment segment, long index, long elementCount) {
        long sizeof = this.layout().byteSize();
        return segment.asSlice(sizeof * index, sizeof * elementCount, this.layout().byteAlignment());
    }

    default public MemorySegment reinterpret(MemorySegment addr) {
        return addr.reinterpret(this.layout().byteSize());
    }

    default public MemorySegment reinterpret(MemorySegment addr, long elementCount) {
        return addr.reinterpret(this.layout().byteSize() * elementCount);
    }

    default public MemorySegment reinterpret(MemorySegment addr, Arena arena, Consumer<MemorySegment> cleanup) {
        return addr.reinterpret(this.layout().byteSize(), arena, cleanup);
    }

    default public MemorySegment reinterpret(MemorySegment addr, long elementCount, Arena arena, Consumer<MemorySegment> cleanup) {
        return addr.reinterpret(this.layout().byteSize() * elementCount, arena, cleanup);
    }

    public static Object bootstrapRecord(MethodHandles.Lookup lookup, String methodName, TypeDescriptor type, String names, String ... getterNames) throws Throwable {
        Object callSite;
        Class<?> recordClass = lookup.lookupClass();
        MethodType methodType = (MethodType)type;
        MethodHandle[] methodHandles = new MethodHandle[getterNames.length];
        for (int i = 0; i < getterNames.length; ++i) {
            methodHandles[i] = lookup.unreflect(recordClass.getDeclaredMethod(getterNames[i], new Class[0]));
        }
        if ("toString".equals(methodName)) {
            Class<?> recordInterface = recordClass.getInterfaces()[0];
            for (int i = 0; i < methodHandles.length; ++i) {
                methodHandles[i] = methodHandles[i].asType(methodHandles[i].type().changeParameterType(0, recordInterface));
            }
            callSite = ObjectMethods.bootstrap(lookup, methodName, methodType, recordInterface, names, methodHandles);
        } else {
            MethodHandle adapted = ((ConstantCallSite)ObjectMethods.bootstrap(lookup, methodName, methodType.changeParameterType(0, recordClass), recordClass, names, methodHandles)).getTarget().asType(methodType);
            callSite = new ConstantCallSite(adapted);
        }
        return callSite;
    }
}

