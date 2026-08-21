/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.CallbackI;
import org.lwjgl.system.Checks;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryAccessJNI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.StackWalkUtil;
import org.lwjgl.system.libc.LibCStdlib;
import org.lwjgl.system.libffi.LibFFI;

final class MemoryManage {
    private MemoryManage() {
    }

    static MemoryUtil.MemoryAllocator getInstance() {
        Object allocator = Configuration.MEMORY_ALLOCATOR.get();
        if (allocator instanceof MemoryUtil.MemoryAllocator) {
            return (MemoryUtil.MemoryAllocator)allocator;
        }
        if (!"system".equals(allocator)) {
            String className = allocator == null || "jemalloc".equals(allocator) ? "org.lwjgl.system.jemalloc.JEmallocAllocator" : ("rpmalloc".equals(allocator) ? "org.lwjgl.system.rpmalloc.RPmallocAllocator" : allocator.toString());
            try {
                Class<?> allocatorClass = Class.forName(className);
                return (MemoryUtil.MemoryAllocator)allocatorClass.getConstructor(new Class[0]).newInstance(new Object[0]);
            }
            catch (Throwable t) {
                if (Checks.DEBUG && (allocator != null || !(t instanceof ClassNotFoundException))) {
                    t.printStackTrace(APIUtil.DEBUG_STREAM);
                }
                APIUtil.apiLog(String.format("Warning: Failed to instantiate memory allocator: %s. Using the system default.", className));
            }
        }
        return new StdlibAllocator();
    }

    static class DebugAllocator
    implements MemoryUtil.MemoryAllocator {
        private static final ConcurrentMap<Allocation, Allocation> ALLOCATIONS = new ConcurrentHashMap<Allocation, Allocation>();
        private static final ConcurrentMap<Long, String> THREADS = new ConcurrentHashMap<Long, String>();
        private final MemoryUtil.MemoryAllocator allocator;
        private final long[] callbacks;

        DebugAllocator(MemoryUtil.MemoryAllocator allocator) {
            this.allocator = allocator;
            this.callbacks = new long[]{((CallbackPP)this::malloc).address(), ((CallbackPPP)this::calloc).address(), ((CallbackPPP)this::realloc).address(), ((CallbackIV)this::free).address(), ((CallbackPPP)this::aligned_alloc).address(), ((CallbackIV)this::aligned_free).address()};
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                for (long callback : this.callbacks) {
                    Callback.free(callback);
                }
                if (ALLOCATIONS.isEmpty()) {
                    return;
                }
                boolean missingStacktrace = false;
                for (Allocation allocation : ALLOCATIONS.keySet()) {
                    StringBuilder sb = new StringBuilder(512);
                    sb.append("[LWJGL] ").append(allocation.size).append(" bytes leaked, thread ").append(allocation.threadId).append(" (").append((String)THREADS.get(allocation.threadId)).append("), address: 0x").append(Long.toHexString(allocation.address).toUpperCase()).append("\n");
                    StackTraceElement[] stackTrace = allocation.getElements();
                    if (stackTrace != null) {
                        for (StackTraceElement el : stackTrace) {
                            sb.append("\tat ").append(el).append("\n");
                        }
                    } else {
                        missingStacktrace = true;
                    }
                    APIUtil.DEBUG_STREAM.print(sb);
                }
                if (missingStacktrace) {
                    APIUtil.DEBUG_STREAM.print("[LWJGL] Reminder: disable Configuration.DEBUG_MEMORY_ALLOCATOR_FAST to get stacktraces of leaking allocations.\n");
                }
            }));
        }

        @Override
        public long getMalloc() {
            return this.callbacks[0];
        }

        @Override
        public long getCalloc() {
            return this.callbacks[1];
        }

        @Override
        public long getRealloc() {
            return this.callbacks[2];
        }

        @Override
        public long getFree() {
            return this.callbacks[3];
        }

        @Override
        public long getAlignedAlloc() {
            return this.callbacks[4];
        }

        @Override
        public long getAlignedFree() {
            return this.callbacks[5];
        }

        @Override
        public long malloc(long size) {
            return DebugAllocator.track(this.allocator.malloc(size), size);
        }

        @Override
        public long calloc(long num, long size) {
            return DebugAllocator.track(this.allocator.calloc(num, size), num * size);
        }

        @Override
        public long realloc(long ptr, long size) {
            long oldSize = DebugAllocator.untrack(ptr);
            long address = this.allocator.realloc(ptr, size);
            if (address != 0L) {
                DebugAllocator.track(address, size);
            } else if (size != 0L) {
                DebugAllocator.track(ptr, oldSize);
            }
            return address;
        }

        @Override
        public void free(long ptr) {
            DebugAllocator.untrack(ptr);
            this.allocator.free(ptr);
        }

        @Override
        public long aligned_alloc(long alignment, long size) {
            return DebugAllocator.track(this.allocator.aligned_alloc(alignment, size), size);
        }

        @Override
        public void aligned_free(long ptr) {
            DebugAllocator.untrack(ptr);
            this.allocator.aligned_free(ptr);
        }

        static long track(long address, long size) {
            if (address != 0L) {
                Thread t = Thread.currentThread();
                THREADS.putIfAbsent(t.getId(), t.getName());
                Allocation allocationNew = new Allocation(address, size, t.getId(), Configuration.DEBUG_MEMORY_ALLOCATOR_FAST.get(false) != false ? null : StackWalkUtil.stackWalkGetTrace());
                Allocation allocationOld = ALLOCATIONS.put(allocationNew, allocationNew);
                if (allocationOld != null) {
                    DebugAllocator.trackAbort(address, allocationOld, allocationNew);
                }
            }
            return address;
        }

        private static void trackAbort(long address, Allocation allocationOld, Allocation allocationNew) {
            String addressHex = Long.toHexString(address).toUpperCase();
            DebugAllocator.trackAbortPrint(allocationOld, "Old", addressHex);
            DebugAllocator.trackAbortPrint(allocationNew, "New", addressHex);
            throw new IllegalStateException("The memory address specified is already being tracked: 0x" + addressHex);
        }

        private static void trackAbortPrint(Allocation allocation, String name, String address) {
            StringBuilder sb = new StringBuilder(512);
            sb.append("[LWJGL] ").append(name).append(" allocation with size ").append(allocation.size).append(", thread ").append(allocation.threadId).append(" (").append((String)THREADS.get(allocation.threadId)).append("), address: 0x").append(address).append("\n");
            StackTraceElement[] stackTrace = allocation.getElements();
            if (stackTrace != null) {
                for (StackTraceElement el : stackTrace) {
                    sb.append("\tat ").append(el).append("\n");
                }
            }
            APIUtil.DEBUG_STREAM.print(sb);
        }

        static long untrack(long address) {
            if (address == 0L) {
                return 0L;
            }
            Allocation allocation = (Allocation)ALLOCATIONS.remove(new Allocation(address, 0L, 0L, null));
            if (allocation == null) {
                throw DebugAllocator.untrackAbort(address);
            }
            return allocation.size;
        }

        private static IllegalStateException untrackAbort(long address) {
            String addressHex = Long.toHexString(address).toUpperCase();
            return new IllegalStateException("The memory address specified is not being tracked: 0x" + addressHex);
        }

        static void report(MemoryUtil.MemoryAllocationReport report) {
            for (Allocation allocation : ALLOCATIONS.keySet()) {
                report.invoke(allocation.address, allocation.size, allocation.threadId, (String)THREADS.get(allocation.threadId), allocation.getElements());
            }
        }

        private static <T> void aggregate(T t, long size, Map<T, AtomicLong> map) {
            AtomicLong node = map.computeIfAbsent(t, k -> new AtomicLong());
            node.set(node.get() + size);
        }

        static void report(MemoryUtil.MemoryAllocationReport report, MemoryUtil.MemoryAllocationReport.Aggregate groupByStackTrace, boolean groupByThread) {
            switch (groupByStackTrace) {
                case ALL: {
                    DebugAllocator.reportAll(report, groupByThread);
                    break;
                }
                case GROUP_BY_METHOD: {
                    DebugAllocator.reportByMethod(report, groupByThread);
                    break;
                }
                case GROUP_BY_STACKTRACE: {
                    DebugAllocator.reportByStacktrace(report, groupByThread);
                }
            }
        }

        private static void reportAll(MemoryUtil.MemoryAllocationReport report, boolean groupByThread) {
            if (groupByThread) {
                HashMap mapThread = new HashMap();
                for (Allocation allocation : ALLOCATIONS.values()) {
                    DebugAllocator.aggregate(allocation.threadId, allocation.size, mapThread);
                }
                for (Map.Entry entry : mapThread.entrySet()) {
                    report.invoke(0L, ((AtomicLong)entry.getValue()).get(), (Long)entry.getKey(), (String)THREADS.get(entry.getKey()), null);
                }
            } else {
                long total = 0L;
                for (Allocation allocation : ALLOCATIONS.values()) {
                    total += allocation.size;
                }
                report.invoke(0L, total, 0L, null, null);
            }
        }

        private static void reportByMethod(MemoryUtil.MemoryAllocationReport report, boolean groupByThread) {
            if (groupByThread) {
                HashMap<Long, Map> mapThreadMethod = new HashMap<Long, Map>();
                for (Allocation allocation : ALLOCATIONS.keySet()) {
                    StackTraceElement[] elements = allocation.getElements();
                    if (elements == null) continue;
                    Map mapMethod = mapThreadMethod.computeIfAbsent(allocation.threadId, k -> new HashMap());
                    DebugAllocator.aggregate(elements[0], allocation.size, mapMethod);
                }
                for (Map.Entry entry : mapThreadMethod.entrySet()) {
                    long threadId = (Long)entry.getKey();
                    String threadName = (String)THREADS.get(threadId);
                    for (Map.Entry ms : ((Map)entry.getValue()).entrySet()) {
                        report.invoke(0L, ((AtomicLong)ms.getValue()).get(), threadId, threadName, (StackTraceElement)ms.getKey());
                    }
                }
            } else {
                HashMap mapMethod = new HashMap();
                for (Allocation allocation : ALLOCATIONS.keySet()) {
                    StackTraceElement[] elements = allocation.getElements();
                    if (elements == null) continue;
                    DebugAllocator.aggregate(elements[0], allocation.size, mapMethod);
                }
                for (Map.Entry entry : mapMethod.entrySet()) {
                    report.invoke(0L, ((AtomicLong)entry.getValue()).get(), 0L, null, (StackTraceElement)entry.getKey());
                }
            }
        }

        private static void reportByStacktrace(MemoryUtil.MemoryAllocationReport report, boolean groupByThread) {
            if (groupByThread) {
                HashMap<Long, Map> mapThreadStackTrace = new HashMap<Long, Map>();
                for (Allocation allocation : ALLOCATIONS.keySet()) {
                    StackTraceElement[] elements = allocation.getElements();
                    if (elements == null) continue;
                    Map mapStackTrace = mapThreadStackTrace.computeIfAbsent(allocation.threadId, k -> new HashMap());
                    DebugAllocator.aggregate(new AllocationKey(elements), allocation.size, mapStackTrace);
                }
                for (Map.Entry entry : mapThreadStackTrace.entrySet()) {
                    long threadId = (Long)entry.getKey();
                    for (Map.Entry ss : ((Map)entry.getValue()).entrySet()) {
                        report.invoke(0L, ((AtomicLong)ss.getValue()).get(), threadId, (String)THREADS.get(threadId), ((AllocationKey)ss.getKey()).elements);
                    }
                }
            } else {
                HashMap mapStackTrace = new HashMap();
                for (Allocation allocation : ALLOCATIONS.keySet()) {
                    StackTraceElement[] elements = allocation.getElements();
                    if (elements == null) continue;
                    DebugAllocator.aggregate(new AllocationKey(elements), allocation.size, mapStackTrace);
                }
                for (Map.Entry entry : mapStackTrace.entrySet()) {
                    report.invoke(0L, ((AtomicLong)entry.getValue()).get(), 0L, null, ((AllocationKey)entry.getKey()).elements);
                }
            }
        }

        private static class AllocationKey {
            final StackTraceElement[] elements;

            AllocationKey(StackTraceElement[] elements) {
                this.elements = elements;
            }

            public boolean equals(Object other) {
                return this == other || Arrays.equals(this.elements, ((AllocationKey)other).elements);
            }

            public int hashCode() {
                return Arrays.hashCode(this.elements);
            }
        }

        private static class Allocation {
            final long address;
            final long size;
            final long threadId;
            private final Object @Nullable [] stacktrace;

            Allocation(long address, long size, long threadId, Object @Nullable [] stacktrace) {
                this.address = address;
                this.size = size;
                this.threadId = threadId;
                this.stacktrace = stacktrace;
            }

            private StackTraceElement @Nullable [] getElements() {
                return this.stacktrace == null ? null : StackWalkUtil.stackWalkArray(this.stacktrace);
            }

            public boolean equals(Object other) {
                return this.address == ((Allocation)other).address;
            }

            public int hashCode() {
                return Long.hashCode(this.address);
            }
        }

        @FunctionalInterface
        public static interface CallbackIV
        extends CallbackI {
            public static final Callback.Descriptor DESCRIPTOR = new Callback.Descriptor(MethodHandles.lookup(), APIUtil.apiCreateCIF(LibFFI.ffi_type_void, LibFFI.ffi_type_pointer));

            @Override
            default public Callback.Descriptor getDescriptor() {
                return DESCRIPTOR;
            }

            @Override
            default public void callback(long ret, long args2) {
                long ptr = MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args2));
                this.invoke(ptr);
            }

            public void invoke(long var1);
        }

        @FunctionalInterface
        public static interface CallbackPPP
        extends CallbackI {
            public static final Callback.Descriptor DESCRIPTOR = new Callback.Descriptor(MethodHandles.lookup(), APIUtil.apiCreateCIF(LibFFI.ffi_type_pointer, LibFFI.ffi_type_pointer, LibFFI.ffi_type_pointer));

            @Override
            default public Callback.Descriptor getDescriptor() {
                return DESCRIPTOR;
            }

            @Override
            default public void callback(long ret, long args2) {
                long num = MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args2));
                long size = MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args2 + (long)POINTER_SIZE));
                MemoryUtil.memPutAddress(ret, this.invoke(num, size));
            }

            public long invoke(long var1, long var3);
        }

        @FunctionalInterface
        public static interface CallbackPP
        extends CallbackI {
            public static final Callback.Descriptor DESCRIPTOR = new Callback.Descriptor(MethodHandles.lookup(), APIUtil.apiCreateCIF(LibFFI.ffi_type_pointer, LibFFI.ffi_type_pointer));

            @Override
            default public Callback.Descriptor getDescriptor() {
                return DESCRIPTOR;
            }

            @Override
            default public void callback(long ret, long args2) {
                long size = MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args2));
                MemoryUtil.memPutAddress(ret, this.invoke(size));
            }

            public long invoke(long var1);
        }
    }

    private static class StdlibAllocator
    implements MemoryUtil.MemoryAllocator {
        private StdlibAllocator() {
        }

        @Override
        public long getMalloc() {
            return MemoryAccessJNI.malloc;
        }

        @Override
        public long getCalloc() {
            return MemoryAccessJNI.calloc;
        }

        @Override
        public long getRealloc() {
            return MemoryAccessJNI.realloc;
        }

        @Override
        public long getFree() {
            return MemoryAccessJNI.free;
        }

        @Override
        public long getAlignedAlloc() {
            return MemoryAccessJNI.aligned_alloc;
        }

        @Override
        public long getAlignedFree() {
            return MemoryAccessJNI.aligned_free;
        }

        @Override
        public long malloc(long size) {
            return LibCStdlib.nmalloc(size);
        }

        @Override
        public long calloc(long num, long size) {
            return LibCStdlib.ncalloc(num, size);
        }

        @Override
        public long realloc(long ptr, long size) {
            return LibCStdlib.nrealloc(ptr, size);
        }

        @Override
        public void free(long ptr) {
            LibCStdlib.nfree(ptr);
        }

        @Override
        public long aligned_alloc(long alignment, long size) {
            return LibCStdlib.naligned_alloc(alignment, size);
        }

        @Override
        public void aligned_free(long ptr) {
            LibCStdlib.naligned_free(ptr);
        }
    }
}

