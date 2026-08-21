/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.mac;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.mac.Who;
import oshi.driver.mac.WindowInfo;
import oshi.ffm.mac.MacSystem;
import oshi.ffm.mac.MacSystemFunctions;
import oshi.software.os.FileSystem;
import oshi.software.os.InternetProtocolStats;
import oshi.software.os.NetworkParams;
import oshi.software.os.OSDesktopWindow;
import oshi.software.os.OSProcess;
import oshi.software.os.OSSession;
import oshi.software.os.OperatingSystem;
import oshi.software.os.mac.MacFileSystemFFM;
import oshi.software.os.mac.MacInternetProtocolStatsFFM;
import oshi.software.os.mac.MacNetworkParams;
import oshi.software.os.mac.MacOSProcessFFM;
import oshi.software.os.mac.MacOperatingSystem;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;
import oshi.util.platform.mac.SysctlUtilFFM;
import oshi.util.tuples.Pair;

@ThreadSafe
public class MacOperatingSystemFFM
extends MacOperatingSystem {
    private static final Logger LOG = LoggerFactory.getLogger(MacOperatingSystemFFM.class);
    private static final long BOOTTIME;

    public MacOperatingSystemFFM() {
        super(SysctlUtilFFM.sysctl("kern.maxproc", 4096));
    }

    @Override
    public FileSystem getFileSystem() {
        return new MacFileSystemFFM();
    }

    @Override
    public InternetProtocolStats getInternetProtocolStats() {
        return new MacInternetProtocolStatsFFM(this.isElevated());
    }

    @Override
    public Pair<String, OperatingSystem.OSVersionInfo> queryFamilyVersionInfo() {
        String family = this.major > 10 || this.major == 10 && this.minor >= 12 ? "macOS" : System.getProperty("os.name");
        String codeName = this.parseCodeName();
        String buildNumber = SysctlUtilFFM.sysctl("kern.osversion", "");
        return new Pair<String, OperatingSystem.OSVersionInfo>(family, new OperatingSystem.OSVersionInfo(this.osXVersion, codeName, buildNumber));
    }

    @Override
    public List<OSSession> getSessions() {
        return USE_WHO_COMMAND ? super.getSessions() : Who.queryUtxent();
    }

    @Override
    public List<OSProcess> queryAllProcesses() {
        Arena arena = Arena.ofConfined();
        try {
            int numberOfProcesses = MacSystemFunctions.proc_listpids(1, 0, MemorySegment.NULL, 0) / MacSystem.INT_SIZE;
            MemorySegment pidSegment = arena.allocate(ValueLayout.JAVA_INT, (long)(numberOfProcesses + 10));
            numberOfProcesses = MacSystemFunctions.proc_listpids(1, 0, pidSegment, numberOfProcesses * MacSystem.INT_SIZE) / MacSystem.INT_SIZE;
            List<OSProcess> list = Arrays.stream(pidSegment.asSlice(0L, numberOfProcesses * MacSystem.INT_SIZE).toArray(ValueLayout.JAVA_INT)).distinct().parallel().mapToObj(this::getProcess).filter(Objects::nonNull).filter(OperatingSystem.ProcessFiltering.VALID_PROCESS).toList();
            if (arena != null) {
                arena.close();
            }
            return list;
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
            catch (Throwable e) {
                LOG.warn("Failed to query processes", (Object)e.getMessage());
                return Collections.emptyList();
            }
        }
    }

    @Override
    public OSProcess getProcess(int pid) {
        MacOSProcessFFM proc = new MacOSProcessFFM(pid, this.major, this.minor, this);
        return proc.getState().equals((Object)OSProcess.State.INVALID) ? null : proc;
    }

    @Override
    public int getProcessId() {
        try {
            return MacSystemFunctions.getpid();
        }
        catch (Throwable e) {
            LOG.warn("Failed to get current pid: {}", (Object)e.getMessage(), (Object)e);
            return 0;
        }
    }

    @Override
    public int getProcessCount() {
        try {
            return MacSystemFunctions.proc_listpids(1, 0, MemorySegment.NULL, 0) / MacSystem.INT_SIZE;
        }
        catch (Throwable e) {
            LOG.warn("Failed to query processes: {}", (Object)e.getMessage(), (Object)e);
            return 0;
        }
    }

    @Override
    public int getThreadCount() {
        Arena arena = Arena.ofConfined();
        try {
            int numberOfProcesses = MacSystemFunctions.proc_listpids(1, 0, MemorySegment.NULL, 0) / MacSystem.INT_SIZE;
            MemorySegment pidSegment = arena.allocate(ValueLayout.JAVA_INT, (long)(numberOfProcesses + 10));
            numberOfProcesses = MacSystemFunctions.proc_listpids(1, 0, pidSegment, numberOfProcesses * MacSystem.INT_SIZE) / MacSystem.INT_SIZE;
            int n = Arrays.stream(pidSegment.asSlice(0L, numberOfProcesses * MacSystem.INT_SIZE).toArray(ValueLayout.JAVA_INT)).distinct().parallel().map(this::threadsPerProc).sum();
            if (arena != null) {
                arena.close();
            }
            return n;
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
            catch (Throwable e) {
                LOG.warn("Failed to query processes", (Object)e.getMessage());
                return 0;
            }
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private int threadsPerProc(int pid) {
        try (Arena arena = Arena.ofConfined();){
            MemorySegment buffer = arena.allocate(MacSystem.PROC_TASK_INFO);
            int result = MacSystemFunctions.proc_pidinfo(pid, 4, 0L, buffer, (int)MacSystem.PROC_TASK_INFO.byteSize());
            if (result > 0) {
                int n = buffer.get(ValueLayout.JAVA_INT, MacSystem.PROC_TASK_INFO.byteOffset(MacSystem.PTI_THREADNUM));
                return n;
            }
            int n = 0;
            return n;
        }
        catch (Throwable e) {
            LOG.warn("Failed to get threads for process {}:", (Object)pid, (Object)e.getMessage());
            return 0;
        }
    }

    @Override
    public long getSystemBootTime() {
        return BOOTTIME;
    }

    @Override
    public NetworkParams getNetworkParams() {
        return new MacNetworkParams();
    }

    @Override
    public List<OSDesktopWindow> getDesktopWindows(boolean visibleOnly) {
        return WindowInfo.queryDesktopWindows(visibleOnly);
    }

    static {
        long bootTime = 0L;
        try (Arena arena = Arena.ofConfined();){
            MemorySegment timeval = arena.allocate(MacSystem.TIMEVAL);
            if (SysctlUtilFFM.sysctl("kern.boottime", timeval)) {
                bootTime = timeval.get(ValueLayout.JAVA_LONG, 0L);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (bootTime == 0L) {
            bootTime = ParseUtil.parseLongOrDefault(ExecutingCommand.getFirstAnswer("sysctl -n kern.boottime").split(",")[0].replaceAll("\\D", ""), System.currentTimeMillis() / 1000L);
        }
        BOOTTIME = bootTime;
    }
}

