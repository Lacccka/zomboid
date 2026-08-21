/*
 * Decompiled with CFR 0.152.
 */
package oshi.ffm.windows;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.ffm.windows.WindowsForeignFunctions;

public final class Kernel32FFM
extends WindowsForeignFunctions {
    private static final Logger LOG = LoggerFactory.getLogger(Kernel32FFM.class);
    private static final SymbolLookup K32 = Kernel32FFM.lib("Kernel32");
    private static final MethodHandle CloseHandle = Kernel32FFM.downcall(K32, "CloseHandle", ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
    private static final MethodHandle GetComputerName = Kernel32FFM.downcall(K32, "GetComputerNameW", ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
    private static final MethodHandle GetComputerNameEx = Kernel32FFM.downcall(K32, "GetComputerNameExW", ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
    private static final MethodHandle GetCurrentProcess = Kernel32FFM.downcall(K32, "GetCurrentProcess", ValueLayout.ADDRESS, new MemoryLayout[0]);
    private static final MethodHandle GetCurrentProcessId = Kernel32FFM.downcall(K32, "GetCurrentProcessId", ValueLayout.JAVA_INT, new MemoryLayout[0]);
    private static final MethodHandle GetLastError = Kernel32FFM.downcall(K32, "GetLastError", ValueLayout.JAVA_INT, new MemoryLayout[0]);
    private static final MethodHandle GetCurrentThreadId = Kernel32FFM.downcall(K32, "GetCurrentThreadId", ValueLayout.JAVA_INT, new MemoryLayout[0]);
    private static final MethodHandle GetTickCount = Kernel32FFM.downcall(K32, "GetTickCount64", ValueLayout.JAVA_LONG, new MemoryLayout[0]);

    public static OptionalInt CloseHandle(MemorySegment handle) {
        try {
            return OptionalInt.of(CloseHandle.invokeExact(handle));
        }
        catch (Throwable t) {
            LOG.debug("Kernel32FFM.closeHandle failed", t);
            return OptionalInt.empty();
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static Optional<String> GetComputerName() {
        try (Arena arena = Arena.ofConfined();){
            int maxLen = 32;
            MemorySegment buffer = arena.allocate(ValueLayout.JAVA_CHAR, (long)maxLen);
            MemorySegment sizeSegment = arena.allocate(ValueLayout.JAVA_INT);
            sizeSegment.set(ValueLayout.JAVA_INT, 0L, maxLen);
            boolean success = Kernel32FFM.isSuccess(GetComputerName.invokeExact(buffer, sizeSegment));
            if (!success) {
                int errorCode = GetLastError.invokeExact();
                LOG.error("Failed to get computer name name. Error code: {}", (Object)errorCode);
                Optional<String> optional = Optional.empty();
                return optional;
            }
            Optional<String> optional = Optional.of(Kernel32FFM.readWideString(buffer));
            return optional;
        }
        catch (Throwable t) {
            LOG.debug("Kernel32FFM.GetComputerName failed: {}", (Object)t.getMessage());
            return Optional.empty();
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static Optional<String> GetComputerNameEx() {
        try (Arena arena = Arena.ofConfined();){
            int maxLen = 256;
            MemorySegment buffer = arena.allocate(ValueLayout.JAVA_CHAR, (long)maxLen);
            MemorySegment sizeSegment = arena.allocate(ValueLayout.JAVA_INT);
            sizeSegment.set(ValueLayout.JAVA_INT, 0L, maxLen);
            int COMPUTER_NAME_DNS_DOMAIN_FULLY_QUALIFIED = 3;
            boolean success = Kernel32FFM.isSuccess(GetComputerNameEx.invokeExact(3, buffer, sizeSegment));
            if (!success) {
                int errorCode = GetLastError.invokeExact();
                LOG.error("Failed to get DNS domain name. Error code: {}", (Object)errorCode);
                Optional<String> optional = Optional.empty();
                return optional;
            }
            Optional<String> optional = Optional.of(Kernel32FFM.readWideString(buffer));
            return optional;
        }
        catch (Throwable t) {
            LOG.debug("Kernel32FFM.GetComputerNameEx failed: {}", (Object)t.getMessage());
            return Optional.empty();
        }
    }

    public static Optional<MemorySegment> GetCurrentProcess() {
        try {
            return Optional.of(GetCurrentProcess.invokeExact());
        }
        catch (Throwable t) {
            LOG.debug("Kernel32FFM.GetCurrentProcess failed: {}", (Object)t.getMessage());
            return Optional.empty();
        }
    }

    public static OptionalInt GetCurrentProcessId() {
        try {
            return OptionalInt.of(GetCurrentProcessId.invokeExact());
        }
        catch (Throwable t) {
            LOG.debug("Kernel32FFM.GetCurrentProcessId failed: {}", (Object)t.getMessage());
            return OptionalInt.empty();
        }
    }

    public static OptionalInt GetLastError() {
        try {
            return OptionalInt.of(GetLastError.invokeExact());
        }
        catch (Throwable t) {
            LOG.debug("Kernel32FFM.GetLastError failed: {}", (Object)t.getMessage());
            return OptionalInt.empty();
        }
    }

    public static OptionalInt GetCurrentThreadId() {
        try {
            int tid = GetCurrentThreadId.invokeExact();
            return OptionalInt.of(tid);
        }
        catch (Throwable t) {
            LOG.debug("Kernel32FFM.GetCurrentThreadId failed: {}", (Object)t.getMessage());
            return OptionalInt.empty();
        }
    }

    public static OptionalLong GetTickCount() {
        try {
            return OptionalLong.of(GetTickCount.invokeExact());
        }
        catch (Throwable t) {
            LOG.debug("Kernel32FFM.GetTickCount64 failed: {}", (Object)t.getMessage());
            return OptionalLong.empty();
        }
    }
}

