/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.unix.solaris;

import com.sun.jna.platform.unix.solaris.Kstat2;
import com.sun.jna.platform.unix.solaris.LibKstat;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.linux.proc.ProcessStat;
import oshi.driver.unix.solaris.Who;
import oshi.jna.platform.unix.SolarisLibc;
import oshi.software.common.AbstractOperatingSystem;
import oshi.software.os.FileSystem;
import oshi.software.os.InternetProtocolStats;
import oshi.software.os.NetworkParams;
import oshi.software.os.OSProcess;
import oshi.software.os.OSService;
import oshi.software.os.OSSession;
import oshi.software.os.OSThread;
import oshi.software.os.OperatingSystem;
import oshi.software.os.unix.solaris.SolarisFileSystem;
import oshi.software.os.unix.solaris.SolarisInternetProtocolStats;
import oshi.software.os.unix.solaris.SolarisNetworkParams;
import oshi.software.os.unix.solaris.SolarisOSProcess;
import oshi.software.os.unix.solaris.SolarisOSThread;
import oshi.util.Constants;
import oshi.util.ExecutingCommand;
import oshi.util.GlobalConfig;
import oshi.util.Memoizer;
import oshi.util.ParseUtil;
import oshi.util.platform.unix.solaris.KstatUtil;
import oshi.util.tuples.Pair;

@ThreadSafe
public class SolarisOperatingSystem
extends AbstractOperatingSystem {
    private static final String VERSION;
    private static final String BUILD_NUMBER;
    private static final boolean ALLOW_KSTAT2;
    public static final boolean HAS_KSTAT2;
    private static final Supplier<Pair<Long, Long>> BOOT_UPTIME;
    private static final long BOOTTIME;

    @Override
    public String queryManufacturer() {
        return "Oracle";
    }

    @Override
    public Pair<String, OperatingSystem.OSVersionInfo> queryFamilyVersionInfo() {
        return new Pair<String, OperatingSystem.OSVersionInfo>("SunOS", new OperatingSystem.OSVersionInfo(VERSION, "Solaris", BUILD_NUMBER));
    }

    @Override
    protected int queryBitness(int jvmBitness) {
        if (jvmBitness == 64) {
            return 64;
        }
        return ParseUtil.parseIntOrDefault(ExecutingCommand.getFirstAnswer("isainfo -b"), 32);
    }

    @Override
    public FileSystem getFileSystem() {
        return new SolarisFileSystem();
    }

    @Override
    public InternetProtocolStats getInternetProtocolStats() {
        return new SolarisInternetProtocolStats();
    }

    @Override
    public List<OSSession> getSessions() {
        return USE_WHO_COMMAND ? super.getSessions() : Who.queryUtxent();
    }

    @Override
    public OSProcess getProcess(int pid) {
        List<OSProcess> procs = this.getProcessListFromProcfs(pid);
        if (procs.isEmpty()) {
            return null;
        }
        return procs.get(0);
    }

    @Override
    public List<OSProcess> queryAllProcesses() {
        return this.queryAllProcessesFromPrStat();
    }

    @Override
    public List<OSProcess> queryChildProcesses(int parentPid) {
        List<OSProcess> allProcs = this.queryAllProcessesFromPrStat();
        Set<Integer> descendantPids = SolarisOperatingSystem.getChildrenOrDescendants(allProcs, parentPid, false);
        return allProcs.stream().filter(p -> descendantPids.contains(p.getProcessID())).collect(Collectors.toList());
    }

    @Override
    public List<OSProcess> queryDescendantProcesses(int parentPid) {
        List<OSProcess> allProcs = this.queryAllProcessesFromPrStat();
        Set<Integer> descendantPids = SolarisOperatingSystem.getChildrenOrDescendants(allProcs, parentPid, true);
        return allProcs.stream().filter(p -> descendantPids.contains(p.getProcessID())).collect(Collectors.toList());
    }

    private List<OSProcess> queryAllProcessesFromPrStat() {
        return this.getProcessListFromProcfs(-1);
    }

    private List<OSProcess> getProcessListFromProcfs(int pid) {
        ArrayList<OSProcess> procs = new ArrayList<OSProcess>();
        File[] numericFiles = null;
        if (pid < 0) {
            File directory = new File("/proc");
            numericFiles = directory.listFiles(file -> Constants.DIGITS.matcher(file.getName()).matches());
        } else {
            File pidFile = new File("/proc/" + pid);
            if (pidFile.exists()) {
                numericFiles = new File[]{pidFile};
            }
        }
        if (numericFiles == null) {
            return procs;
        }
        for (File pidFile : numericFiles) {
            int pidNum = ParseUtil.parseIntOrDefault(pidFile.getName(), 0);
            SolarisOSProcess proc = new SolarisOSProcess(pidNum, this);
            if (proc.getState() == OSProcess.State.INVALID) continue;
            procs.add(proc);
        }
        return procs;
    }

    @Override
    public int getProcessId() {
        return SolarisLibc.INSTANCE.getpid();
    }

    @Override
    public int getProcessCount() {
        return ProcessStat.getPidFiles().length;
    }

    @Override
    public int getThreadId() {
        return SolarisLibc.INSTANCE.thr_self();
    }

    @Override
    public OSThread getCurrentThread() {
        return new SolarisOSThread(this.getProcessId(), this.getThreadId());
    }

    @Override
    public int getThreadCount() {
        List<String> threadList = ExecutingCommand.runNative("ps -eLo pid");
        if (!threadList.isEmpty()) {
            return threadList.size() - 1;
        }
        return this.getProcessCount();
    }

    @Override
    public long getSystemUptime() {
        return SolarisOperatingSystem.querySystemUptime();
    }

    private static long querySystemUptime() {
        if (HAS_KSTAT2) {
            return BOOT_UPTIME.get().getB();
        }
        try (KstatUtil.KstatChain kc = KstatUtil.openChain();){
            LibKstat.Kstat ksp = kc.lookup("unix", 0, "system_misc");
            if (ksp != null && kc.read(ksp)) {
                long l = ksp.ks_snaptime / 1000000000L;
                return l;
            }
        }
        return 0L;
    }

    @Override
    public long getSystemBootTime() {
        return BOOTTIME;
    }

    private static long querySystemBootTime() {
        if (HAS_KSTAT2) {
            return BOOT_UPTIME.get().getA();
        }
        try (KstatUtil.KstatChain kc = KstatUtil.openChain();){
            LibKstat.Kstat ksp = kc.lookup("unix", 0, "system_misc");
            if (ksp != null && kc.read(ksp)) {
                long l = KstatUtil.dataLookupLong(ksp, "boot_time");
                return l;
            }
        }
        return System.currentTimeMillis() / 1000L - SolarisOperatingSystem.querySystemUptime();
    }

    private static Pair<Long, Long> queryBootAndUptime() {
        Object[] results = KstatUtil.queryKstat2("/misc/unix/system_misc", "boot_time", "snaptime");
        long boot = results[0] == null ? System.currentTimeMillis() : (Long)results[0];
        long snap = results[1] == null ? 0L : (Long)results[1] / 1000000000L;
        return new Pair<Long, Long>(boot, snap);
    }

    @Override
    public NetworkParams getNetworkParams() {
        return new SolarisNetworkParams();
    }

    @Override
    public List<OSService> getServices() {
        File[] listFiles;
        ArrayList<OSService> services = new ArrayList<OSService>();
        ArrayList<String> legacySvcs = new ArrayList<String>();
        File dir = new File("/etc/init.d");
        if (dir.exists() && dir.isDirectory() && (listFiles = dir.listFiles()) != null) {
            for (File f : listFiles) {
                legacySvcs.add(f.getName());
            }
        }
        List<String> svcs = ExecutingCommand.runNative("svcs -p");
        block1: for (String line : svcs) {
            if (line.startsWith("online")) {
                int delim = line.lastIndexOf(":/");
                if (delim <= 0) continue;
                String name = line.substring(delim + 1);
                if (name.endsWith(":default")) {
                    name = name.substring(0, name.length() - 8);
                }
                services.add(new OSService(name, 0, OSService.State.STOPPED));
                continue;
            }
            if (line.startsWith(" ")) {
                String[] split = ParseUtil.whitespaces.split(line.trim());
                if (split.length != 3) continue;
                services.add(new OSService(split[2], ParseUtil.parseIntOrDefault(split[1], 0), OSService.State.RUNNING));
                continue;
            }
            if (!line.startsWith("legacy_run")) continue;
            for (String svc : legacySvcs) {
                if (!line.endsWith(svc)) continue;
                services.add(new OSService(svc, 0, OSService.State.STOPPED));
                continue block1;
            }
        }
        return services;
    }

    static {
        String[] split = ParseUtil.whitespaces.split(ExecutingCommand.getFirstAnswer("uname -rv"));
        VERSION = split[0];
        BUILD_NUMBER = split.length > 1 ? split[1] : "";
        ALLOW_KSTAT2 = GlobalConfig.get("oshi.os.solaris.allowKstat2", true);
        Kstat2 lib = null;
        try {
            if (ALLOW_KSTAT2) {
                lib = Kstat2.INSTANCE;
            }
        }
        catch (UnsatisfiedLinkError unsatisfiedLinkError) {
            // empty catch block
        }
        HAS_KSTAT2 = lib != null;
        BOOT_UPTIME = Memoizer.memoize(SolarisOperatingSystem::queryBootAndUptime, Memoizer.defaultExpiration());
        BOOTTIME = SolarisOperatingSystem.querySystemBootTime();
    }
}

