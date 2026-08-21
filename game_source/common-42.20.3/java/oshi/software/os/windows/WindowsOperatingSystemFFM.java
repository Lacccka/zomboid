/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.windows;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.ffm.windows.Advapi32FFM;
import oshi.ffm.windows.Kernel32FFM;
import oshi.ffm.windows.PsapiFFM;
import oshi.ffm.windows.WinNTFFM;
import oshi.ffm.windows.WindowsForeignFunctions;
import oshi.software.os.ApplicationInfo;
import oshi.software.os.InternetProtocolStats;
import oshi.software.os.NetworkParams;
import oshi.software.os.windows.WindowsInstalledAppsFFM;
import oshi.software.os.windows.WindowsInternetProtocolStatsFFM;
import oshi.software.os.windows.WindowsNetworkParamsFFM;
import oshi.software.os.windows.WindowsOperatingSystem;
import oshi.util.Memoizer;
import oshi.util.platform.windows.Advapi32UtilFFM;
import oshi.util.platform.windows.Kernel32UtilFFM;

public class WindowsOperatingSystemFFM
extends WindowsOperatingSystem {
    private static final Logger LOG = LoggerFactory.getLogger(WindowsOperatingSystemFFM.class);
    private static final long BOOTTIME = Advapi32UtilFFM.querySystemBootTime();
    private final Supplier<List<ApplicationInfo>> installedAppsSupplier = Memoizer.memoize(WindowsInstalledAppsFFM::queryInstalledApps, Memoizer.installedAppsExpiration());

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean enableDebugPrivilege() {
        MemorySegment hToken = null;
        try {
            Arena arena;
            block28: {
                MemorySegment luid;
                boolean success;
                block27: {
                    MemorySegment hTokenPtr;
                    block26: {
                        Optional<MemorySegment> hProcess;
                        block25: {
                            arena = Arena.ofConfined();
                            try {
                                hTokenPtr = arena.allocate(ValueLayout.ADDRESS);
                                hProcess = Kernel32FFM.GetCurrentProcess();
                                if (!hProcess.isEmpty()) break block25;
                                boolean bl = false;
                                if (arena != null) {
                                    arena.close();
                                }
                                return bl;
                            }
                            catch (Throwable throwable) {
                                try {
                                    if (arena != null) {
                                        try {
                                            arena.close();
                                        }
                                        catch (Throwable throwable2) {
                                            throwable.addSuppressed(throwable2);
                                        }
                                    }
                                    throw throwable;
                                }
                                catch (Throwable t) {
                                    LOG.error("enableDebugPrivilege exception: " + String.valueOf(t));
                                    boolean bl = false;
                                    return bl;
                                }
                            }
                        }
                        success = Advapi32FFM.OpenProcessToken(hProcess.get(), 40, hTokenPtr);
                        if (success) break block26;
                        LOG.error("OpenProcessToken failed, error: " + String.valueOf(Kernel32FFM.GetLastError()));
                        boolean bl = false;
                        if (arena != null) {
                            arena.close();
                        }
                        return bl;
                    }
                    hToken = hTokenPtr.get(ValueLayout.ADDRESS, 0L);
                    luid = arena.allocate(WinNTFFM.LUID);
                    success = Advapi32FFM.LookupPrivilegeValue("SeDebugPrivilege", luid, arena);
                    if (success) break block27;
                    LOG.error("LookupPrivilegeValue failed, error: " + String.valueOf(Kernel32FFM.GetLastError()));
                    boolean bl = false;
                    if (arena != null) {
                        arena.close();
                    }
                    return bl;
                }
                MemorySegment tkp = WindowsForeignFunctions.setupTokenPrivileges(arena, luid);
                success = Advapi32FFM.AdjustTokenPrivileges(hToken, tkp);
                if (success) break block28;
                LOG.error("AdjustTokenPrivileges failed, error: " + String.valueOf(Kernel32FFM.GetLastError()));
                boolean bl = false;
                if (arena != null) {
                    arena.close();
                }
                return bl;
            }
            boolean bl = true;
            if (arena != null) {
                arena.close();
            }
            return bl;
        }
        finally {
            if (hToken != null && hToken.address() != 0L) {
                Kernel32FFM.CloseHandle(hToken);
            }
        }
    }

    @Override
    public List<ApplicationInfo> getInstalledApplications() {
        return this.installedAppsSupplier.get();
    }

    @Override
    public InternetProtocolStats getInternetProtocolStats() {
        return new WindowsInternetProtocolStatsFFM();
    }

    @Override
    public NetworkParams getNetworkParams() {
        return new WindowsNetworkParamsFFM();
    }

    @Override
    public boolean isElevated() {
        return Advapi32UtilFFM.isCurrentProcessElevated();
    }

    @Override
    public int getProcessId() {
        return Kernel32FFM.GetCurrentProcessId().orElse(-1);
    }

    @Override
    public long getSystemBootTime() {
        return BOOTTIME;
    }

    @Override
    public long getSystemUptime() {
        return Kernel32UtilFFM.querySystemUptime();
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public int getThreadCount() {
        try (Arena arena = Arena.ofConfined();){
            int threadCount;
            MemorySegment perfInfo = arena.allocate(WinNTFFM.PERFORMANCE_INFORMATION);
            int size = (int)WinNTFFM.PERFORMANCE_INFORMATION.byteSize();
            perfInfo.set(ValueLayout.JAVA_INT, WinNTFFM.PERFORMANCE_INFORMATION.byteOffset(MemoryLayout.PathElement.groupElement("cb")), size);
            if (!PsapiFFM.GetPerformanceInfo(perfInfo, size)) {
                LOG.error("Failed to get Performance Info. Error code: {}", (Object)Kernel32FFM.GetLastError());
                int n = 0;
                return n;
            }
            int n = threadCount = perfInfo.get(ValueLayout.JAVA_INT, WinNTFFM.PERFORMANCE_INFORMATION.byteOffset(MemoryLayout.PathElement.groupElement("ThreadCount")));
            return n;
        }
        catch (Throwable t) {
            LOG.error("Exception getting thread count", t);
            return 0;
        }
    }

    @Override
    public int getThreadId() {
        return Kernel32FFM.GetCurrentThreadId().orElse(-1);
    }

    static {
        WindowsOperatingSystemFFM.enableDebugPrivilege();
    }
}

