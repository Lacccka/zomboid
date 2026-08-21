/*
 * Decompiled with CFR 0.152.
 */
package oshi.util.platform.windows;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.ffm.windows.Advapi32FFM;
import oshi.ffm.windows.Kernel32FFM;
import oshi.ffm.windows.Win32Exception;
import oshi.ffm.windows.WinNTFFM;
import oshi.ffm.windows.WindowsForeignFunctions;
import oshi.util.GlobalConfig;
import oshi.util.Memoizer;
import oshi.util.platform.windows.Kernel32UtilFFM;

public final class Advapi32UtilFFM {
    private static final Logger LOG = LoggerFactory.getLogger(Advapi32UtilFFM.class);
    private static Supplier<String> systemLog = Memoizer.memoize(Advapi32UtilFFM::querySystemLog, TimeUnit.HOURS.toNanos(1L));

    private Advapi32UtilFFM() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive exception aggregation
     */
    public static boolean isCurrentProcessElevated() {
        try (Arena arena = Arena.ofConfined();){
            MemorySegment elevation;
            MemorySegment hToken;
            block17: {
                boolean bl;
                MemorySegment hTokenPtr;
                Optional<MemorySegment> hProcessOpt = Kernel32FFM.GetCurrentProcess();
                if (hProcessOpt.isEmpty()) {
                    boolean bl2 = false;
                    return bl2;
                }
                MemorySegment hProcess = hProcessOpt.get();
                if (!Advapi32FFM.OpenProcessToken(hProcess, 8, hTokenPtr = arena.allocate(ValueLayout.ADDRESS))) {
                    boolean bl3 = false;
                    return bl3;
                }
                hToken = hTokenPtr.get(ValueLayout.ADDRESS, 0L);
                try {
                    elevation = arena.allocate(WinNTFFM.TOKEN_ELEVATION);
                    MemorySegment returnLength = arena.allocate(ValueLayout.JAVA_INT);
                    boolean success = Advapi32FFM.GetTokenInformation(hToken, 20, elevation, (int)WinNTFFM.TOKEN_ELEVATION.byteSize(), returnLength);
                    if (success) break block17;
                    bl = false;
                }
                catch (Throwable throwable) {
                    Kernel32FFM.CloseHandle(hToken);
                    throw throwable;
                }
                Kernel32FFM.CloseHandle(hToken);
                return bl;
            }
            int tokenIsElevated = elevation.get(ValueLayout.JAVA_INT, WinNTFFM.TOKEN_ELEVATION.byteOffset(MemoryLayout.PathElement.groupElement("TokenIsElevated")));
            boolean bl = tokenIsElevated > 0;
            Kernel32FFM.CloseHandle(hToken);
            return bl;
        }
        catch (Throwable t) {
            LOG.debug("Advapi32FFM.isCurrentProcessElevated failed", t);
            return false;
        }
    }

    public static String[] registryGetKeys(MemorySegment hKey) throws Throwable {
        try (Arena arena = Arena.ofConfined();){
            MemorySegment lpcSubKeys = arena.allocate(ValueLayout.JAVA_INT);
            MemorySegment lpcMaxSubKeyLen = arena.allocate(ValueLayout.JAVA_INT);
            int rc = Advapi32FFM.RegQueryInfoKey(hKey, MemorySegment.NULL, MemorySegment.NULL, MemorySegment.NULL, lpcSubKeys, lpcMaxSubKeyLen, MemorySegment.NULL, MemorySegment.NULL, MemorySegment.NULL, MemorySegment.NULL, MemorySegment.NULL, MemorySegment.NULL);
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
            int subKeyCount = lpcSubKeys.get(ValueLayout.JAVA_INT, 0L);
            int maxNameLen = lpcMaxSubKeyLen.get(ValueLayout.JAVA_INT, 0L);
            ArrayList<String> keys2 = new ArrayList<String>(subKeyCount);
            for (int i = 0; i < subKeyCount; ++i) {
                MemorySegment nameBuf = arena.allocate((maxNameLen + 1) * 2);
                MemorySegment nameLen = arena.allocate(ValueLayout.JAVA_INT);
                nameLen.set(ValueLayout.JAVA_INT, 0L, maxNameLen + 1);
                rc = Advapi32FFM.RegEnumKeyEx(hKey, i, nameBuf, nameLen, MemorySegment.NULL, MemorySegment.NULL, MemorySegment.NULL, MemorySegment.NULL);
                if (rc != 0) {
                    throw new Win32Exception(rc);
                }
                keys2.add(WindowsForeignFunctions.readWideString(nameBuf));
            }
            String[] stringArray = (String[])keys2.toArray(String[]::new);
            return stringArray;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String[] registryGetKeys(MemorySegment rootKey, String keyPath, int samDesiredExtra) throws Throwable {
        try (Arena arena = Arena.ofConfined();){
            String[] stringArray;
            MemorySegment phkResult = arena.allocate(ValueLayout.ADDRESS);
            int rc = Advapi32FFM.RegOpenKeyEx(rootKey, WindowsForeignFunctions.toWideString(arena, keyPath), 0, 0x20019 | samDesiredExtra, phkResult);
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
            MemorySegment hKey = phkResult.get(ValueLayout.ADDRESS, 0L);
            try {
                stringArray = Advapi32UtilFFM.registryGetKeys(hKey);
            }
            catch (Throwable throwable) {
                rc = Advapi32FFM.RegCloseKey(hKey);
                if (rc != 0) {
                    throw new Win32Exception(rc);
                }
                throw throwable;
            }
            rc = Advapi32FFM.RegCloseKey(hKey);
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
            return stringArray;
        }
    }

    public static int registryGetDword(MemorySegment hKey, String valueName) throws Throwable {
        try (Arena arena = Arena.ofConfined();){
            MemorySegment pData = arena.allocate(ValueLayout.JAVA_INT);
            MemorySegment lpType = arena.allocate(ValueLayout.JAVA_INT);
            MemorySegment lpcbData = arena.allocate(ValueLayout.JAVA_INT);
            lpcbData.set(ValueLayout.JAVA_INT, 0L, 4);
            int rc = Advapi32FFM.RegQueryValueEx(hKey, WindowsForeignFunctions.toWideString(arena, valueName), 0, lpType, pData, lpcbData);
            WindowsForeignFunctions.checkSuccess(rc, 122);
            int n = pData.get(ValueLayout.JAVA_INT, 0L);
            return n;
        }
    }

    public static String registryGetString(MemorySegment hKey, String valueName, int size) throws Throwable {
        try (Arena arena = Arena.ofConfined();){
            MemorySegment data = arena.allocate(size + 2);
            MemorySegment lpType = arena.allocate(ValueLayout.JAVA_INT);
            MemorySegment lpcbData = arena.allocate(ValueLayout.JAVA_INT);
            lpcbData.set(ValueLayout.JAVA_INT, 0L, size);
            int rc = Advapi32FFM.RegQueryValueEx(hKey, WindowsForeignFunctions.toWideString(arena, valueName), 0, lpType, data, lpcbData);
            WindowsForeignFunctions.checkSuccess(rc, 122);
            String string = WindowsForeignFunctions.readWideString(data);
            return string;
        }
    }

    public static Object registryGetValue(MemorySegment hKey, String valueName) throws Throwable {
        try (Arena arena = Arena.ofConfined();){
            MemorySegment lpType = arena.allocate(ValueLayout.JAVA_INT);
            MemorySegment lpcbData = arena.allocate(ValueLayout.JAVA_INT);
            int rc = Advapi32FFM.RegQueryValueEx(hKey, WindowsForeignFunctions.toWideString(arena, valueName), 0, lpType, MemorySegment.NULL, lpcbData);
            WindowsForeignFunctions.checkSuccess(rc, 122);
            int type = lpType.get(ValueLayout.JAVA_INT, 0L);
            int size = lpcbData.get(ValueLayout.JAVA_INT, 0L);
            Object object = switch (type) {
                case 1, 2 -> Advapi32UtilFFM.registryGetString(hKey, valueName, size);
                case 4 -> Advapi32UtilFFM.registryGetDword(hKey, valueName);
                default -> {
                    LOG.warn("Unsupported registry data type " + type + " for " + valueName);
                    yield null;
                }
            };
            return object;
        }
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static long querySystemBootTime() {
        String eventLog = systemLog.get();
        try (Arena arena = Arena.ofConfined();){
            Optional<MemorySegment> hEventLogOpt = Advapi32FFM.OpenEventLog(arena, eventLog);
            if (hEventLogOpt.isEmpty()) {
                LOG.warn("Unable to open system Event Log. Falling back to uptime.");
                long l = System.currentTimeMillis() / 1000L - Kernel32UtilFFM.querySystemUptime();
                return l;
            }
            MemorySegment hEventLog = hEventLogOpt.get();
            int bufSize = 65536;
            MemorySegment buffer = arena.allocate(bufSize);
            MemorySegment bytesRead = arena.allocate(ValueLayout.JAVA_INT);
            MemorySegment minBytesNeeded = arena.allocate(ValueLayout.JAVA_INT);
            long event6005Time = 0L;
            long OFFSET_EVENTID = WinNTFFM.OFFSET_EVENTID;
            long OFFSET_TIME_GENERATED = WinNTFFM.OFFSET_TIME_GENERATED;
            long OFFSET_LENGTH = WinNTFFM.OFFSET_LENGTH;
            while (Advapi32FFM.ReadEventLog(hEventLog, 9, buffer, bufSize, bytesRead, minBytesNeeded)) {
                int length;
                int read = bytesRead.get(ValueLayout.JAVA_INT, 0L);
                for (int offset = 0; offset < read; offset += length) {
                    MemorySegment record = buffer.asSlice((long)offset, WinNTFFM.EVENTLOGRECORD.byteSize());
                    int eventId = record.get(ValueLayout.JAVA_INT, (long)((int)OFFSET_EVENTID));
                    long timeGenerated = Integer.toUnsignedLong(record.get(ValueLayout.JAVA_INT, (long)((int)OFFSET_TIME_GENERATED)));
                    if (eventId == 12) {
                        Advapi32FFM.CloseEventLog(hEventLog);
                        long l = timeGenerated;
                        return l;
                    }
                    if (eventId == 6005) {
                        if (event6005Time > 0L) {
                            Advapi32FFM.CloseEventLog(hEventLog);
                            long l = event6005Time;
                            return l;
                        }
                        event6005Time = timeGenerated;
                    }
                    length = record.get(ValueLayout.JAVA_INT, (long)((int)OFFSET_LENGTH));
                }
            }
            Advapi32FFM.CloseEventLog(hEventLog);
            if (event6005Time > 0L) {
                long l = event6005Time;
                return l;
            }
        }
        catch (Throwable t) {
            LOG.error("Exception while querying system boottime, fallback to boot time from uptime", t);
        }
        return System.currentTimeMillis() / 1000L - Kernel32UtilFFM.querySystemUptime();
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static String querySystemLog() {
        String systemLog = GlobalConfig.get("oshi.os.windows.eventlog", "System");
        if (systemLog.isEmpty()) {
            return null;
        }
        try (Arena arena = Arena.ofConfined();){
            MemorySegment sourceName = WindowsForeignFunctions.toWideString(arena, systemLog);
            Optional<MemorySegment> hEventLog = Advapi32FFM.OpenEventLog(MemorySegment.NULL, sourceName);
            if (hEventLog.isEmpty()) {
                LOG.warn("Unable to open configured system Event log \"{}\". Calculating boot time from uptime.", (Object)systemLog);
                String string = null;
                return string;
            }
            String string = systemLog;
            return string;
        }
        catch (Throwable t) {
            LOG.error("Exception while opening system event log", t);
            return null;
        }
    }
}

