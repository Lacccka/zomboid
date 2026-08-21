/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.mac;

import com.sun.jna.platform.mac.SystemB;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.mac.Who;
import oshi.driver.mac.WindowInfo;
import oshi.jna.Struct;
import oshi.software.os.FileSystem;
import oshi.software.os.InternetProtocolStats;
import oshi.software.os.NetworkParams;
import oshi.software.os.OSDesktopWindow;
import oshi.software.os.OSProcess;
import oshi.software.os.OSSession;
import oshi.software.os.OperatingSystem;
import oshi.software.os.mac.MacFileSystem;
import oshi.software.os.mac.MacInternetProtocolStats;
import oshi.software.os.mac.MacNetworkParams;
import oshi.software.os.mac.MacOSProcess;
import oshi.software.os.mac.MacOperatingSystem;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;
import oshi.util.platform.mac.SysctlUtil;
import oshi.util.tuples.Pair;

@ThreadSafe
public class MacOperatingSystemJNA
extends MacOperatingSystem {
    private static final long BOOTTIME;

    public MacOperatingSystemJNA() {
        super(SysctlUtil.sysctl("kern.maxproc", 4096));
    }

    protected MacOperatingSystemJNA(int maxproc) {
        super(maxproc);
    }

    @Override
    public long getSystemBootTime() {
        return BOOTTIME;
    }

    @Override
    public FileSystem getFileSystem() {
        return new MacFileSystem();
    }

    @Override
    public InternetProtocolStats getInternetProtocolStats() {
        return new MacInternetProtocolStats(this.isElevated());
    }

    @Override
    public Pair<String, OperatingSystem.OSVersionInfo> queryFamilyVersionInfo() {
        String family = this.major > 10 || this.major == 10 && this.minor >= 12 ? "macOS" : System.getProperty("os.name");
        String codeName = this.parseCodeName();
        String buildNumber = SysctlUtil.sysctl("kern.osversion", "");
        return new Pair<String, OperatingSystem.OSVersionInfo>(family, new OperatingSystem.OSVersionInfo(this.osXVersion, codeName, buildNumber));
    }

    @Override
    public List<OSSession> getSessions() {
        return USE_WHO_COMMAND ? super.getSessions() : Who.queryUtxent();
    }

    @Override
    public List<OSProcess> queryAllProcesses() {
        ArrayList<OSProcess> procs = new ArrayList<OSProcess>();
        int[] pids = new int[this.maxProc];
        Arrays.fill(pids, -1);
        int numberOfProcesses = SystemB.INSTANCE.proc_listpids(1, 0, pids, pids.length * SystemB.INT_SIZE) / SystemB.INT_SIZE;
        for (int i = 0; i < numberOfProcesses; ++i) {
            OSProcess proc;
            if (pids[i] < 0 || (proc = this.getProcess(pids[i])) == null) continue;
            procs.add(proc);
        }
        return procs;
    }

    @Override
    public OSProcess getProcess(int pid) {
        MacOSProcess proc = new MacOSProcess(pid, this.major, this.minor, this);
        return proc.getState().equals((Object)OSProcess.State.INVALID) ? null : proc;
    }

    @Override
    public int getProcessId() {
        return SystemB.INSTANCE.getpid();
    }

    @Override
    public int getProcessCount() {
        return SystemB.INSTANCE.proc_listpids(1, 0, null, 0) / SystemB.INT_SIZE;
    }

    @Override
    public int getThreadCount() {
        int[] pids = new int[this.getProcessCount() + 10];
        int numberOfProcesses = SystemB.INSTANCE.proc_listpids(1, 0, pids, pids.length) / SystemB.INT_SIZE;
        int numberOfThreads = 0;
        try (Struct.CloseableProcTaskInfo taskInfo = new Struct.CloseableProcTaskInfo();){
            for (int i = 0; i < numberOfProcesses; ++i) {
                int exit = SystemB.INSTANCE.proc_pidinfo(pids[i], 4, 0L, taskInfo, taskInfo.size());
                if (exit == -1) continue;
                numberOfThreads += taskInfo.pti_threadnum;
            }
        }
        return numberOfThreads;
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
        try (Struct.CloseableTimeval tv = new Struct.CloseableTimeval();){
            BOOTTIME = !SysctlUtil.sysctl("kern.boottime", tv) || tv.tv_sec.longValue() == 0L ? ParseUtil.parseLongOrDefault(ExecutingCommand.getFirstAnswer("sysctl -n kern.boottime").split(",")[0].replaceAll("\\D", ""), System.currentTimeMillis() / 1000L) : tv.tv_sec.longValue();
        }
    }
}

