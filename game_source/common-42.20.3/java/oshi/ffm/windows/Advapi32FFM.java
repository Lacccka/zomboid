/*
 * Decompiled with CFR 0.152.
 */
package oshi.ffm.windows;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.Optional;
import oshi.ffm.windows.WindowsForeignFunctions;

public final class Advapi32FFM
extends WindowsForeignFunctions {
    private static final SymbolLookup ADV = Advapi32FFM.lib("Advapi32");
    private static final MethodHandle AdjustTokenPrivileges = Advapi32FFM.downcall(ADV, "AdjustTokenPrivileges", ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
    private static final MethodHandle CloseEventLog = Advapi32FFM.downcall(ADV, "CloseEventLog", ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
    private static final MethodHandle GetTokenInformation = Advapi32FFM.downcall(ADV, "GetTokenInformation", ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
    private static final MethodHandle LookupPrivilegeValue = Advapi32FFM.downcall(ADV, "LookupPrivilegeValueW", ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
    private static final MethodHandle OpenEventLog = Advapi32FFM.downcall(ADV, "OpenEventLogW", ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
    private static final MethodHandle OpenProcessToken = Advapi32FFM.downcall(ADV, "OpenProcessToken", ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
    private static final MethodHandle ReadEventLog = Advapi32FFM.downcall(ADV, "ReadEventLogW", ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
    private static final MethodHandle RegCloseKey = Advapi32FFM.downcall(ADV, "RegCloseKey", ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
    private static final MethodHandle RegEnumKeyEx = Advapi32FFM.downcall(ADV, "RegEnumKeyExW", ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
    private static final MethodHandle RegOpenKeyEx = Advapi32FFM.downcall(ADV, "RegOpenKeyExW", ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
    private static final MethodHandle RegQueryInfoKey = Advapi32FFM.downcall(ADV, "RegQueryInfoKeyW", ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
    private static final MethodHandle RegQueryValueEx = Advapi32FFM.downcall(ADV, "RegQueryValueExW", ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS);

    public static boolean AdjustTokenPrivileges(MemorySegment hToken, MemorySegment tkp) throws Throwable {
        return Advapi32FFM.isSuccess(AdjustTokenPrivileges.invokeExact(hToken, 0, tkp, 0, MemorySegment.NULL, MemorySegment.NULL));
    }

    public static boolean CloseEventLog(MemorySegment hEventLog) throws Throwable {
        return Advapi32FFM.isSuccess(CloseEventLog.invokeExact(hEventLog));
    }

    public static boolean GetTokenInformation(MemorySegment hToken, int tokenInfoClass, MemorySegment tokenInfo, int tokenInfoLength, MemorySegment returnLength) throws Throwable {
        return Advapi32FFM.isSuccess(GetTokenInformation.invokeExact(hToken, tokenInfoClass, tokenInfo, tokenInfoLength, returnLength));
    }

    public static boolean LookupPrivilegeValue(String name, MemorySegment luid, Arena arena) throws Throwable {
        MemorySegment nameSeg = Advapi32FFM.toWideString(arena, name);
        return Advapi32FFM.isSuccess(LookupPrivilegeValue.invokeExact(MemorySegment.NULL, nameSeg, luid));
    }

    public static Optional<MemorySegment> OpenEventLog(MemorySegment serverName, MemorySegment sourceName) throws Throwable {
        MemorySegment handle = OpenEventLog.invokeExact(serverName, sourceName);
        if (handle == null || handle.equals(MemorySegment.NULL)) {
            return Optional.empty();
        }
        return Optional.of(handle);
    }

    public static Optional<MemorySegment> OpenEventLog(Arena arena, String source2) {
        try {
            MemorySegment lpSource = WindowsForeignFunctions.toWideString(arena, source2);
            MemorySegment handle = OpenEventLog.invokeExact(MemorySegment.NULL, lpSource);
            if (handle.address() == 0L) {
                return Optional.empty();
            }
            return Optional.of(handle);
        }
        catch (Throwable t) {
            return Optional.empty();
        }
    }

    public static boolean OpenProcessToken(MemorySegment process, int desiredAccess, MemorySegment hTokenOut) throws Throwable {
        return Advapi32FFM.isSuccess(OpenProcessToken.invokeExact(process, desiredAccess, hTokenOut));
    }

    public static boolean ReadEventLog(MemorySegment hEventLog, int flags, MemorySegment buffer, int bufSize, MemorySegment bytesRead, MemorySegment minBytesNeeded) throws Throwable {
        return Advapi32FFM.isSuccess(ReadEventLog.invokeExact(hEventLog, flags, 0, buffer, bufSize, bytesRead, minBytesNeeded));
    }

    public static int RegCloseKey(MemorySegment hKey) throws Throwable {
        return RegCloseKey.invokeExact(hKey);
    }

    public static int RegEnumKeyEx(MemorySegment hKey, int dwIndex, MemorySegment lpName, MemorySegment lpcchName, MemorySegment lpReserved, MemorySegment lpClass, MemorySegment lpcchClass, MemorySegment lpftLastWriteTime) throws Throwable {
        return RegEnumKeyEx.invokeExact(hKey, dwIndex, lpName, lpcchName, lpReserved, lpClass, lpcchClass, lpftLastWriteTime);
    }

    public static int RegOpenKeyEx(MemorySegment hKey, MemorySegment subKey, int options, int samDesired, MemorySegment phkResult) throws Throwable {
        return RegOpenKeyEx.invokeExact(hKey, subKey, options, samDesired, phkResult);
    }

    public static int RegQueryInfoKey(MemorySegment hKey, MemorySegment lpClass, MemorySegment lpcchClass, MemorySegment lpReserved, MemorySegment lpcSubKeys, MemorySegment lpcMaxSubKeyLen, MemorySegment lpcMaxClassLen, MemorySegment lpcValues, MemorySegment lpcMaxValueNameLen, MemorySegment lpcMaxValueLen, MemorySegment lpcbSecurityDescriptor, MemorySegment lpftLastWriteTime) throws Throwable {
        return RegQueryInfoKey.invokeExact(hKey, lpClass, lpcchClass, lpReserved, lpcSubKeys, lpcMaxSubKeyLen, lpcMaxClassLen, lpcValues, lpcMaxValueNameLen, lpcMaxValueLen, lpcbSecurityDescriptor, lpftLastWriteTime);
    }

    public static int RegQueryValueEx(MemorySegment hKey, MemorySegment lpValueName, int reserved, MemorySegment lpType, MemorySegment lpData, MemorySegment lpcbData) throws Throwable {
        return RegQueryValueEx.invokeExact(hKey, lpValueName, reserved, lpType, lpData, lpcbData);
    }
}

