/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.windows.registry;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.COM.WbemcliUtil;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.VersionHelpers;
import com.sun.jna.platform.win32.Wtsapi32;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.Immutable;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.windows.wmi.Win32Process;
import oshi.jna.ByRef;
import oshi.util.platform.windows.WmiUtil;

@ThreadSafe
public final class ProcessWtsData {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessWtsData.class);
    private static final boolean IS_WINDOWS7_OR_GREATER = VersionHelpers.IsWindows7OrGreater();

    private ProcessWtsData() {
    }

    public static Map<Integer, WtsInfo> queryProcessWtsMap(Collection<Integer> pids) {
        if (IS_WINDOWS7_OR_GREATER) {
            return ProcessWtsData.queryProcessWtsMapFromWTS(pids);
        }
        return ProcessWtsData.queryProcessWtsMapFromPerfMon(pids);
    }

    private static Map<Integer, WtsInfo> queryProcessWtsMapFromWTS(Collection<Integer> pids) {
        HashMap<Integer, WtsInfo> wtsMap = new HashMap<Integer, WtsInfo>();
        try (ByRef.CloseableIntByReference pCount = new ByRef.CloseableIntByReference(0);
             ByRef.CloseablePointerByReference ppProcessInfo = new ByRef.CloseablePointerByReference();
             ByRef.CloseableIntByReference infoLevel1 = new ByRef.CloseableIntByReference(1);){
            Wtsapi32.WTS_PROCESS_INFO_EX[] processInfo;
            if (!Wtsapi32.INSTANCE.WTSEnumerateProcessesEx(Wtsapi32.WTS_CURRENT_SERVER_HANDLE, infoLevel1, -2, ppProcessInfo, pCount)) {
                LOG.error("Failed to enumerate Processes. Error code: {}", (Object)Kernel32.INSTANCE.GetLastError());
                HashMap<Integer, WtsInfo> hashMap = wtsMap;
                return hashMap;
            }
            Pointer pProcessInfo = ppProcessInfo.getValue();
            Wtsapi32.WTS_PROCESS_INFO_EX processInfoRef = new Wtsapi32.WTS_PROCESS_INFO_EX(pProcessInfo);
            for (Wtsapi32.WTS_PROCESS_INFO_EX info : processInfo = (Wtsapi32.WTS_PROCESS_INFO_EX[])processInfoRef.toArray(pCount.getValue())) {
                if (pids != null && !pids.contains(info.ProcessId)) continue;
                wtsMap.put(info.ProcessId, new WtsInfo(info.pProcessName, "", info.NumberOfThreads, (long)info.PagefileUsage & 0xFFFFFFFFL, info.KernelTime.getValue() / 10000L, info.UserTime.getValue() / 10000L, info.HandleCount));
            }
            if (!Wtsapi32.INSTANCE.WTSFreeMemoryEx(1, pProcessInfo, pCount.getValue())) {
                LOG.warn("Failed to Free Memory for Processes. Error code: {}", (Object)Kernel32.INSTANCE.GetLastError());
            }
        }
        return wtsMap;
    }

    private static Map<Integer, WtsInfo> queryProcessWtsMapFromPerfMon(Collection<Integer> pids) {
        HashMap<Integer, WtsInfo> wtsMap = new HashMap<Integer, WtsInfo>();
        WbemcliUtil.WmiResult<Win32Process.ProcessXPProperty> processWmiResult = Win32Process.queryProcesses(pids);
        for (int i = 0; i < processWmiResult.getResultCount(); ++i) {
            wtsMap.put(WmiUtil.getUint32(processWmiResult, Win32Process.ProcessXPProperty.PROCESSID, i), new WtsInfo(WmiUtil.getString(processWmiResult, Win32Process.ProcessXPProperty.NAME, i), WmiUtil.getString(processWmiResult, Win32Process.ProcessXPProperty.EXECUTABLEPATH, i), WmiUtil.getUint32(processWmiResult, Win32Process.ProcessXPProperty.THREADCOUNT, i), 1024L * ((long)WmiUtil.getUint32(processWmiResult, Win32Process.ProcessXPProperty.PAGEFILEUSAGE, i) & 0xFFFFFFFFL), WmiUtil.getUint64(processWmiResult, Win32Process.ProcessXPProperty.KERNELMODETIME, i) / 10000L, WmiUtil.getUint64(processWmiResult, Win32Process.ProcessXPProperty.USERMODETIME, i) / 10000L, WmiUtil.getUint32(processWmiResult, Win32Process.ProcessXPProperty.HANDLECOUNT, i)));
        }
        return wtsMap;
    }

    @Immutable
    public static class WtsInfo {
        private final String name;
        private final String path;
        private final int threadCount;
        private final long virtualSize;
        private final long kernelTime;
        private final long userTime;
        private final long openFiles;

        public WtsInfo(String name, String path, int threadCount, long virtualSize, long kernelTime, long userTime, long openFiles) {
            this.name = name;
            this.path = path;
            this.threadCount = threadCount;
            this.virtualSize = virtualSize;
            this.kernelTime = kernelTime;
            this.userTime = userTime;
            this.openFiles = openFiles;
        }

        public String getName() {
            return this.name;
        }

        public String getPath() {
            return this.path;
        }

        public int getThreadCount() {
            return this.threadCount;
        }

        public long getVirtualSize() {
            return this.virtualSize;
        }

        public long getKernelTime() {
            return this.kernelTime;
        }

        public long getUserTime() {
            return this.userTime;
        }

        public long getOpenFiles() {
            return this.openFiles;
        }
    }
}

