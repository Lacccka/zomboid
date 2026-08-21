/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.unix.openbsd;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.jna.platform.unix.OpenBsdLibc;
import oshi.software.common.AbstractOperatingSystem;
import oshi.software.os.FileSystem;
import oshi.software.os.InternetProtocolStats;
import oshi.software.os.NetworkParams;
import oshi.software.os.OSProcess;
import oshi.software.os.OSService;
import oshi.software.os.OSThread;
import oshi.software.os.OperatingSystem;
import oshi.software.os.unix.openbsd.OpenBsdFileSystem;
import oshi.software.os.unix.openbsd.OpenBsdInternetProtocolStats;
import oshi.software.os.unix.openbsd.OpenBsdNetworkParams;
import oshi.software.os.unix.openbsd.OpenBsdOSProcess;
import oshi.software.os.unix.openbsd.OpenBsdOSThread;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;
import oshi.util.platform.unix.openbsd.OpenBsdSysctlUtil;
import oshi.util.tuples.Pair;

@ThreadSafe
public class OpenBsdOperatingSystem
extends AbstractOperatingSystem {
    private static final Logger LOG = LoggerFactory.getLogger(OpenBsdOperatingSystem.class);
    private static final long BOOTTIME = OpenBsdOperatingSystem.querySystemBootTime();
    static final String PS_COMMAND_ARGS = Arrays.stream(PsKeywords.values()).map(Enum::name).map(name -> name.toLowerCase(Locale.ROOT)).collect(Collectors.joining(","));

    @Override
    public String queryManufacturer() {
        return "Unix/BSD";
    }

    @Override
    public Pair<String, OperatingSystem.OSVersionInfo> queryFamilyVersionInfo() {
        int[] mib = new int[]{1, 1};
        String family = OpenBsdSysctlUtil.sysctl(mib, "OpenBSD");
        mib[1] = 2;
        String version = OpenBsdSysctlUtil.sysctl(mib, "");
        mib[1] = 4;
        String versionInfo = OpenBsdSysctlUtil.sysctl(mib, "");
        String buildNumber = versionInfo.split(":")[0].replace(family, "").replace(version, "").trim();
        return new Pair<String, OperatingSystem.OSVersionInfo>(family, new OperatingSystem.OSVersionInfo(version, null, buildNumber));
    }

    @Override
    protected int queryBitness(int jvmBitness) {
        if (jvmBitness < 64 && ExecutingCommand.getFirstAnswer("uname -m").indexOf("64") == -1) {
            return jvmBitness;
        }
        return 64;
    }

    @Override
    public FileSystem getFileSystem() {
        return new OpenBsdFileSystem();
    }

    @Override
    public InternetProtocolStats getInternetProtocolStats() {
        return new OpenBsdInternetProtocolStats();
    }

    @Override
    public List<OSProcess> queryAllProcesses() {
        return this.getProcessListFromPS(-1);
    }

    @Override
    public List<OSProcess> queryChildProcesses(int parentPid) {
        List<OSProcess> allProcs = this.queryAllProcesses();
        Set<Integer> descendantPids = OpenBsdOperatingSystem.getChildrenOrDescendants(allProcs, parentPid, false);
        return allProcs.stream().filter(p -> descendantPids.contains(p.getProcessID())).collect(Collectors.toList());
    }

    @Override
    public List<OSProcess> queryDescendantProcesses(int parentPid) {
        List<OSProcess> allProcs = this.queryAllProcesses();
        Set<Integer> descendantPids = OpenBsdOperatingSystem.getChildrenOrDescendants(allProcs, parentPid, true);
        return allProcs.stream().filter(p -> descendantPids.contains(p.getProcessID())).collect(Collectors.toList());
    }

    @Override
    public OSProcess getProcess(int pid) {
        List<OSProcess> procs = this.getProcessListFromPS(pid);
        if (procs.isEmpty()) {
            return null;
        }
        return procs.get(0);
    }

    private List<OSProcess> getProcessListFromPS(int pid) {
        List<String> procList;
        ArrayList<OSProcess> procs = new ArrayList<OSProcess>();
        String psCommand = "ps -awwxo " + PS_COMMAND_ARGS;
        if (pid >= 0) {
            psCommand = psCommand + " -p " + pid;
        }
        if ((procList = ExecutingCommand.runNative(psCommand)).isEmpty() || procList.size() < 2) {
            return procs;
        }
        procList.remove(0);
        for (String proc : procList) {
            Map<PsKeywords, String> psMap = ParseUtil.stringToEnumMap(PsKeywords.class, proc.trim(), ' ');
            if (!psMap.containsKey((Object)PsKeywords.ARGS)) continue;
            procs.add(new OpenBsdOSProcess(pid < 0 ? ParseUtil.parseIntOrDefault(psMap.get((Object)PsKeywords.PID), 0) : pid, psMap, this));
        }
        return procs;
    }

    @Override
    public int getProcessId() {
        return OpenBsdLibc.INSTANCE.getpid();
    }

    @Override
    public int getProcessCount() {
        List<String> procList = ExecutingCommand.runNative("ps -axo pid");
        if (!procList.isEmpty()) {
            return procList.size() - 1;
        }
        return 0;
    }

    @Override
    public int getThreadId() {
        return OpenBsdLibc.INSTANCE.getthrid();
    }

    @Override
    public OSThread getCurrentThread() {
        OSProcess proc = this.getCurrentProcess();
        int tid = this.getThreadId();
        return proc.getThreadDetails().stream().filter(t -> t.getThreadId() == tid).findFirst().orElse(new OpenBsdOSThread(proc.getProcessID(), tid));
    }

    @Override
    public int getThreadCount() {
        List<String> threadList = ExecutingCommand.runNative("ps -axHo tid");
        if (!threadList.isEmpty()) {
            return threadList.size() - 1;
        }
        return 0;
    }

    @Override
    public long getSystemUptime() {
        return System.currentTimeMillis() / 1000L - BOOTTIME;
    }

    @Override
    public long getSystemBootTime() {
        return BOOTTIME;
    }

    private static long querySystemBootTime() {
        return ParseUtil.parseLongOrDefault(ExecutingCommand.getFirstAnswer("sysctl -n kern.boottime").split(",")[0].replaceAll("\\D", ""), System.currentTimeMillis() / 1000L);
    }

    @Override
    public NetworkParams getNetworkParams() {
        return new OpenBsdNetworkParams();
    }

    @Override
    public List<OSService> getServices() {
        File[] listFiles;
        ArrayList<OSService> services = new ArrayList<OSService>();
        HashSet<String> running = new HashSet<String>();
        for (OSProcess p : this.getChildProcesses(1, OperatingSystem.ProcessFiltering.ALL_PROCESSES, OperatingSystem.ProcessSorting.PID_ASC, 0)) {
            OSService s = new OSService(p.getName(), p.getProcessID(), OSService.State.RUNNING);
            services.add(s);
            running.add(p.getName());
        }
        File dir = new File("/etc/rc.d");
        if (dir.exists() && dir.isDirectory() && (listFiles = dir.listFiles()) != null) {
            for (File f : listFiles) {
                String name = f.getName();
                if (running.contains(name)) continue;
                OSService s = new OSService(name, 0, OSService.State.STOPPED);
                services.add(s);
            }
        } else {
            LOG.error("Directory: /etc/rc.d does not exist");
        }
        return services;
    }

    static enum PsKeywords {
        STATE,
        PID,
        PPID,
        USER,
        UID,
        GROUP,
        GID,
        PRI,
        VSZ,
        RSS,
        ETIME,
        CPUTIME,
        COMM,
        MAJFLT,
        MINFLT,
        NVCSW,
        NIVCSW,
        ARGS;

    }
}

