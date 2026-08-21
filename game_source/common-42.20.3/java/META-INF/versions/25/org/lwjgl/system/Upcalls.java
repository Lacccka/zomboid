/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.CallbackI;
import org.lwjgl.system.Checks;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.ffm.FFM;
import org.lwjgl.system.ffm.UpcallBinder;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
final class Upcalls {
    private static final ConcurrentHashMap<Class<?>, Class<?>> CALLBACK_INTERFACE_CACHE = new ConcurrentHashMap();
    private static final ConcurrentHashMap<Class<?>, UpcallBinder<?>> BINDER_CACHE = new ConcurrentHashMap();
    private static final ConcurrentHashMap<Long, Upcall> UPCALL_REGISTRY = new ConcurrentHashMap();
    private static final ArenaType ARENA_TYPE = switch (Configuration.FFM_UPCALL_ARENA.get("auto")) {
        case "auto" -> ArenaType.AUTO;
        case "confined" -> ArenaType.CONFINED;
        case "shared" -> ArenaType.SHARED;
        default -> throw new IllegalArgumentException("Unsupported arena type specified: " + Configuration.FFM_UPCALL_ARENA.get());
    };
    private static final MethodHandle WRAP_EXCEPTION_V;
    private static final MethodHandle WRAP_EXCEPTION_B;
    private static final MethodHandle WRAP_EXCEPTION_S;
    private static final MethodHandle WRAP_EXCEPTION_I;
    private static final MethodHandle WRAP_EXCEPTION_J;
    private static final MethodHandle WRAP_EXCEPTION_F;
    private static final MethodHandle WRAP_EXCEPTION_D;
    private static final MethodHandle WRAP_EXCEPTION_A;

    private Upcalls() {
    }

    static long upcallCreate(Callback.Descriptor callbackDescriptor, Object instance) {
        UpcallBinder binder = Upcalls.getBinder(callbackDescriptor, instance);
        FunctionDescriptor descriptor = binder.descriptor();
        ScopedValue<Arena> scopedArena = FFM.ffmScopedArena();
        Arena arena = scopedArena.isBound() ? scopedArena.get() : ARENA_TYPE.create();
        MethodHandle handle = binder.handle().bindTo(instance);
        MemoryLayout stack = binder.stack();
        if (stack != null) {
            handle = handle.bindTo(arena.allocate(stack));
        }
        if (Configuration.FFM_UPCALL_EXCEPTION_CATCH.get(true).booleanValue()) {
            handle = MethodHandles.catchException(handle, Throwable.class, Upcalls.wrapException(descriptor));
        }
        MemorySegment upcall = Linker.nativeLinker().upcallStub(handle, descriptor, arena, new Linker.Option[0]);
        UPCALL_REGISTRY.put(upcall.address(), new Upcall(arena, instance));
        return upcall.address();
    }

    static <T extends CallbackI> T upcallGet(long functionPointer) {
        return (T)((CallbackI)Upcalls.UPCALL_REGISTRY.get((Object)Long.valueOf((long)functionPointer)).javaCallback);
    }

    static void upcallFree(long functionPointer) {
        Upcall upcall = UPCALL_REGISTRY.remove(functionPointer);
        if (upcall != null && ARENA_TYPE.isCloseable()) {
            upcall.arena.close();
        }
    }

    private static UpcallBinder getBinder(Callback.Descriptor descriptor, Object instance) {
        Class upcallInterface = CALLBACK_INTERFACE_CACHE.computeIfAbsent(instance.getClass(), it -> {
            Class<?> iface2;
            block0: while (true) {
                if (it.isHidden() || !it.isAnonymousClass()) {
                    for (Class<?> iface2 : it.getInterfaces()) {
                        if (CallbackI.class.isAssignableFrom(iface2)) break block0;
                    }
                }
                it = it.getSuperclass();
            }
            it = iface2;
            if (!it.isInterface()) {
                throw new IllegalStateException("Failed to find upcall interface for " + String.valueOf(instance.getClass()));
            }
            return it;
        });
        return BINDER_CACHE.computeIfAbsent(upcallInterface, it -> {
            FFM.ffmConfig(it, FFM.ffmConfigBuilder(descriptor.lookup).build());
            return FFM.ffmUpcall(it, descriptor.cif);
        });
    }

    private static MethodHandle wrapException(FunctionDescriptor descriptor) {
        return descriptor.returnLayout().map(it -> {
            MethodHandle methodHandle;
            MemoryLayout memoryLayout = it;
            Objects.requireNonNull(memoryLayout);
            MemoryLayout selector0$temp = memoryLayout;
            int index$1 = 0;
            block9: while (true) {
                switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ValueLayout.OfByte.class, ValueLayout.OfShort.class, ValueLayout.OfInt.class, ValueLayout.OfLong.class, ValueLayout.OfFloat.class, ValueLayout.OfDouble.class, AddressLayout.class, GroupLayout.class}, (MemoryLayout)selector0$temp, index$1)) {
                    case 0: {
                        methodHandle = WRAP_EXCEPTION_B;
                        break block9;
                    }
                    case 1: {
                        methodHandle = WRAP_EXCEPTION_S;
                        break block9;
                    }
                    case 2: {
                        methodHandle = WRAP_EXCEPTION_I;
                        break block9;
                    }
                    case 3: {
                        methodHandle = WRAP_EXCEPTION_J;
                        break block9;
                    }
                    case 4: {
                        methodHandle = WRAP_EXCEPTION_F;
                        break block9;
                    }
                    case 5: {
                        methodHandle = WRAP_EXCEPTION_D;
                        break block9;
                    }
                    case 6: 
                    case 7: {
                        if (!(selector0$temp instanceof AddressLayout) && !(selector0$temp instanceof GroupLayout)) {
                            index$1 = 8;
                            continue block9;
                        }
                        methodHandle = WRAP_EXCEPTION_A;
                        break block9;
                    }
                    default: {
                        throw new UnsupportedOperationException("Unsupported callback return type: " + String.valueOf(it));
                    }
                }
                break;
            }
            return methodHandle;
        }).orElse(WRAP_EXCEPTION_V);
    }

    private static @Nullable Consumer<Throwable> getUncaughtExceptionHandlerInstance(Object handler) {
        String className = handler.toString();
        try {
            return (Consumer)Class.forName(className).getConstructor(new Class[0]).newInstance(new Object[0]);
        }
        catch (Throwable t) {
            if (Checks.DEBUG) {
                t.printStackTrace(APIUtil.DEBUG_STREAM);
            }
            APIUtil.apiLog(String.format("Warning: Failed to instantiate uncaught exception handler: %s. Using the default.", className));
            return null;
        }
    }

    private static void wrapException(Throwable t) {
        Object handler = Configuration.FFM_UPCALL_EXCEPTION_HANDLER.get();
        if (handler != null && !"default".equals(handler)) {
            if (handler instanceof Consumer) {
                Consumer consumer = (Consumer)handler;
                consumer.accept(t);
                return;
            }
            Consumer<Throwable> consumer = Upcalls.getUncaughtExceptionHandlerInstance(handler);
            if (consumer != null) {
                consumer.accept(t);
                return;
            }
        }
        APIUtil.DEBUG_STREAM.println("[LWJGL] Unhandled exception in callback:");
        t.printStackTrace(APIUtil.DEBUG_STREAM);
    }

    private static void wrapExceptionV(Throwable t) {
        Upcalls.wrapException(t);
    }

    private static byte wrapExceptionB(Throwable t) {
        Upcalls.wrapException(t);
        return 0;
    }

    private static short wrapExceptionS(Throwable t) {
        Upcalls.wrapException(t);
        return 0;
    }

    private static int wrapExceptionI(Throwable t) {
        Upcalls.wrapException(t);
        return 0;
    }

    private static long wrapExceptionJ(Throwable t) {
        Upcalls.wrapException(t);
        return 0L;
    }

    private static float wrapExceptionF(Throwable t) {
        Upcalls.wrapException(t);
        return 0.0f;
    }

    private static double wrapExceptionD(Throwable t) {
        Upcalls.wrapException(t);
        return 0.0;
    }

    private static MemorySegment wrapExceptionA(Throwable t) {
        Upcalls.wrapException(t);
        return MemorySegment.NULL;
    }

    static {
        APIUtil.apiLog("Upcall Arena: " + ARENA_TYPE.name().toLowerCase());
        APIUtil.apiLog("Upcall Registry: ConcurrentHashMap");
        MemoryUtil.getAllocator();
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            WRAP_EXCEPTION_V = lookup.findStatic(Upcalls.class, "wrapExceptionV", MethodType.methodType(Void.TYPE, Throwable.class));
            WRAP_EXCEPTION_B = lookup.findStatic(Upcalls.class, "wrapExceptionB", MethodType.methodType(Byte.TYPE, Throwable.class));
            WRAP_EXCEPTION_S = lookup.findStatic(Upcalls.class, "wrapExceptionS", MethodType.methodType(Short.TYPE, Throwable.class));
            WRAP_EXCEPTION_I = lookup.findStatic(Upcalls.class, "wrapExceptionI", MethodType.methodType(Integer.TYPE, Throwable.class));
            WRAP_EXCEPTION_J = lookup.findStatic(Upcalls.class, "wrapExceptionJ", MethodType.methodType(Long.TYPE, Throwable.class));
            WRAP_EXCEPTION_F = lookup.findStatic(Upcalls.class, "wrapExceptionF", MethodType.methodType(Float.TYPE, Throwable.class));
            WRAP_EXCEPTION_D = lookup.findStatic(Upcalls.class, "wrapExceptionD", MethodType.methodType(Double.TYPE, Throwable.class));
            WRAP_EXCEPTION_A = lookup.findStatic(Upcalls.class, "wrapExceptionA", MethodType.methodType(MemorySegment.class, Throwable.class));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static enum ArenaType {
        AUTO{

            @Override
            Arena create() {
                return Arena.ofAuto();
            }

            @Override
            boolean isCloseable() {
                return false;
            }
        }
        ,
        CONFINED{

            @Override
            Arena create() {
                return Arena.ofConfined();
            }

            @Override
            boolean isCloseable() {
                return true;
            }
        }
        ,
        SHARED{

            @Override
            Arena create() {
                return Arena.ofShared();
            }

            @Override
            boolean isCloseable() {
                return true;
            }
        };


        abstract Arena create();

        abstract boolean isCloseable();
    }

    private record Upcall(Arena arena, Object javaCallback) {
    }
}

