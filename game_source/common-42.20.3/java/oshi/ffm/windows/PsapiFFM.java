/*
 * Decompiled with CFR 0.152.
 */
package oshi.ffm.windows;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import oshi.ffm.windows.WindowsForeignFunctions;

public final class PsapiFFM
extends WindowsForeignFunctions {
    private static final SymbolLookup PSAPI = PsapiFFM.lib("Psapi");
    private static final MethodHandle GetPerformanceInfo = PsapiFFM.downcall(PSAPI, "GetPerformanceInfo", ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);

    public static boolean GetPerformanceInfo(MemorySegment pPerfInfo, int size) throws Throwable {
        return PsapiFFM.isSuccess(GetPerformanceInfo.invokeExact(pPerfInfo, size));
    }
}

