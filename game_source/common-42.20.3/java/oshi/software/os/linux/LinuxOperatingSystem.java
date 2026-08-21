/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.linux;

import com.sun.jna.Native;
import com.sun.jna.platform.linux.LibC;
import com.sun.jna.platform.linux.Udev;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.linux.Who;
import oshi.driver.linux.proc.Auxv;
import oshi.driver.linux.proc.CpuStat;
import oshi.driver.linux.proc.ProcessStat;
import oshi.driver.linux.proc.UpTime;
import oshi.jna.Struct;
import oshi.jna.platform.linux.LinuxLibc;
import oshi.software.common.AbstractOperatingSystem;
import oshi.software.os.ApplicationInfo;
import oshi.software.os.FileSystem;
import oshi.software.os.InternetProtocolStats;
import oshi.software.os.NetworkParams;
import oshi.software.os.OSProcess;
import oshi.software.os.OSService;
import oshi.software.os.OSSession;
import oshi.software.os.OSThread;
import oshi.software.os.OperatingSystem;
import oshi.software.os.linux.LinuxFileSystem;
import oshi.software.os.linux.LinuxInstalledApps;
import oshi.software.os.linux.LinuxInternetProtocolStats;
import oshi.software.os.linux.LinuxNetworkParams;
import oshi.software.os.linux.LinuxOSProcess;
import oshi.software.os.linux.LinuxOSThread;
import oshi.util.ExecutingCommand;
import oshi.util.FileUtil;
import oshi.util.GlobalConfig;
import oshi.util.Memoizer;
import oshi.util.ParseUtil;
import oshi.util.platform.linux.ProcPath;
import oshi.util.tuples.Pair;
import oshi.util.tuples.Triplet;

@ThreadSafe
public class LinuxOperatingSystem
extends AbstractOperatingSystem {
    private static final Logger LOG = LoggerFactory.getLogger(LinuxOperatingSystem.class);
    private static final String OS_RELEASE_LOG = "os-release: {}";
    private static final String LSB_RELEASE_A_LOG = "lsb_release -a: {}";
    private static final String LSB_RELEASE_LOG = "lsb-release: {}";
    private static final String RELEASE_DELIM = " release ";
    private static final String DOUBLE_QUOTES = "(?:^\")|(?:\"$)";
    private static final String FILENAME_PROPERTIES = "oshi.linux.filename.properties";
    public static final boolean HAS_UDEV;
    public static final boolean HAS_GETTID;
    public static final boolean HAS_SYSCALL_GETTID;
    private final Supplier<List<ApplicationInfo>> installedAppsSupplier = Memoizer.memoize(LinuxInstalledApps::queryInstalledApps, Memoizer.installedAppsExpiration());
    private static final long USER_HZ;
    private static final long PAGE_SIZE;
    private static final String OS_NAME;
    static final long BOOTTIME;
    private static final int[] PPID_INDEX;

    @Override
    public String queryManufacturer() {
        return OS_NAME;
    }

    @Override
    public Pair<String, OperatingSystem.OSVersionInfo> queryFamilyVersionInfo() {
        Triplet<String, String, String> familyVersionCodename = LinuxOperatingSystem.queryFamilyVersionCodenameFromReleaseFiles();
        String buildNumber = null;
        List<String> procVersion = FileUtil.readFile(ProcPath.VERSION);
        if (!procVersion.isEmpty()) {
            String[] split;
            for (String s : split = ParseUtil.whitespaces.split(procVersion.get(0))) {
                if ("Linux".equals(s) || "version".equals(s)) continue;
                buildNumber = s;
                break;
            }
        }
        OperatingSystem.OSVersionInfo versionInfo = new OperatingSystem.OSVersionInfo(familyVersionCodename.getB(), familyVersionCodename.getC(), buildNumber);
        return new Pair<String, OperatingSystem.OSVersionInfo>(familyVersionCodename.getA(), versionInfo);
    }

    @Override
    protected int queryBitness(int jvmBitness) {
        if (jvmBitness < 64 && !ExecutingCommand.getFirstAnswer("uname -m").contains("64")) {
            return jvmBitness;
        }
        return 64;
    }

    @Override
    public FileSystem getFileSystem() {
        return new LinuxFileSystem();
    }

    @Override
    public InternetProtocolStats getInternetProtocolStats() {
        return new LinuxInternetProtocolStats();
    }

    @Override
    public List<OSSession> getSessions() {
        return USE_WHO_COMMAND ? super.getSessions() : Who.queryUtxent();
    }

    @Override
    public OSProcess getProcess(int pid) {
        LinuxOSProcess proc = new LinuxOSProcess(pid, this);
        if (!proc.getState().equals((Object)OSProcess.State.INVALID)) {
            return proc;
        }
        return null;
    }

    @Override
    public List<OSProcess> queryAllProcesses() {
        return this.queryChildProcesses(-1);
    }

    @Override
    public List<OSProcess> queryChildProcesses(int parentPid) {
        File[] pidFiles = ProcessStat.getPidFiles();
        if (parentPid >= 0) {
            return this.queryProcessList(LinuxOperatingSystem.getChildrenOrDescendants(LinuxOperatingSystem.getParentPidsFromProcFiles(pidFiles), parentPid, false));
        }
        HashSet<Integer> descendantPids = new HashSet<Integer>();
        for (File procFile : pidFiles) {
            int pid = ParseUtil.parseIntOrDefault(procFile.getName(), -2);
            if (pid == -2) continue;
            descendantPids.add(pid);
        }
        return this.queryProcessList(descendantPids);
    }

    @Override
    public List<OSProcess> queryDescendantProcesses(int parentPid) {
        File[] pidFiles = ProcessStat.getPidFiles();
        return this.queryProcessList(LinuxOperatingSystem.getChildrenOrDescendants(LinuxOperatingSystem.getParentPidsFromProcFiles(pidFiles), parentPid, true));
    }

    private List<OSProcess> queryProcessList(Set<Integer> descendantPids) {
        ArrayList<OSProcess> procs = new ArrayList<OSProcess>();
        for (int pid : descendantPids) {
            LinuxOSProcess proc = new LinuxOSProcess(pid, this);
            if (proc.getState().equals((Object)OSProcess.State.INVALID)) continue;
            procs.add(proc);
        }
        return procs;
    }

    private static Map<Integer, Integer> getParentPidsFromProcFiles(File[] pidFiles) {
        HashMap<Integer, Integer> parentPidMap = new HashMap<Integer, Integer>();
        for (File procFile : pidFiles) {
            int pid = ParseUtil.parseIntOrDefault(procFile.getName(), 0);
            parentPidMap.put(pid, LinuxOperatingSystem.getParentPidFromProcFile(pid));
        }
        return parentPidMap;
    }

    private static int getParentPidFromProcFile(int pid) {
        String stat = FileUtil.getStringFromFile(String.format(Locale.ROOT, "/proc/%d/stat", pid));
        if (stat.isEmpty()) {
            return 0;
        }
        long[] statArray = ParseUtil.parseStringToLongArray(stat, PPID_INDEX, ProcessStat.PROC_PID_STAT_LENGTH, ' ');
        return (int)statArray[0];
    }

    @Override
    public int getProcessId() {
        return LinuxLibc.INSTANCE.getpid();
    }

    @Override
    public int getProcessCount() {
        return ProcessStat.getPidFiles().length;
    }

    @Override
    public int getThreadId() {
        if (HAS_SYSCALL_GETTID) {
            return HAS_GETTID ? LinuxLibc.INSTANCE.gettid() : LinuxLibc.INSTANCE.syscall(LinuxLibc.SYS_GETTID, new Object[0]).intValue();
        }
        try {
            return ParseUtil.parseIntOrDefault(Files.readSymbolicLink(new File(ProcPath.THREAD_SELF).toPath()).getFileName().toString(), 0);
        }
        catch (IOException e) {
            return 0;
        }
    }

    @Override
    public OSThread getCurrentThread() {
        return new LinuxOSThread(this.getProcessId(), this.getThreadId());
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public int getThreadCount() {
        try (Struct.CloseableSysinfo info = new Struct.CloseableSysinfo();){
            if (0 != LibC.INSTANCE.sysinfo(info)) {
                LOG.error("Failed to get process thread count. Error code: {}", (Object)Native.getLastError());
                int n = 0;
                return n;
            }
            short s = info.procs;
            return s;
        }
        catch (NoClassDefFoundError | UnsatisfiedLinkError e) {
            LOG.error("Failed to get procs from sysinfo. {}", (Object)e.getMessage());
            return 0;
        }
    }

    @Override
    public long getSystemUptime() {
        return (long)UpTime.getSystemUptimeSeconds();
    }

    @Override
    public long getSystemBootTime() {
        return BOOTTIME;
    }

    @Override
    public NetworkParams getNetworkParams() {
        return new LinuxNetworkParams();
    }

    @Override
    public List<ApplicationInfo> getInstalledApplications() {
        return this.installedAppsSupplier.get();
    }

    private static Triplet<String, String, String> queryFamilyVersionCodenameFromReleaseFiles() {
        Triplet<String, String, String> familyVersionCodename = LinuxOperatingSystem.readDistribRelease("/etc/system-release");
        if (familyVersionCodename != null) {
            return familyVersionCodename;
        }
        familyVersionCodename = LinuxOperatingSystem.readOsRelease();
        if (familyVersionCodename != null) {
            return familyVersionCodename;
        }
        familyVersionCodename = LinuxOperatingSystem.execLsbRelease();
        if (familyVersionCodename != null) {
            return familyVersionCodename;
        }
        familyVersionCodename = LinuxOperatingSystem.readLsbRelease();
        if (familyVersionCodename != null) {
            return familyVersionCodename;
        }
        String etcDistribRelease = LinuxOperatingSystem.getReleaseFilename();
        familyVersionCodename = LinuxOperatingSystem.readDistribRelease(etcDistribRelease);
        if (familyVersionCodename != null) {
            return familyVersionCodename;
        }
        String family = LinuxOperatingSystem.filenameToFamily(etcDistribRelease.replace("/etc/", "").replace("release", "").replace("version", "").replace("-", "").replace("_", ""));
        return new Triplet<String, String, String>(family, "unknown", "unknown");
    }

    private static Triplet<String, String, String> readOsRelease() {
        String family = null;
        String versionId = "unknown";
        String codeName = "unknown";
        List<String> osRelease = FileUtil.readFile("/etc/os-release");
        for (String line : osRelease) {
            if (line.startsWith("VERSION=")) {
                LOG.debug(OS_RELEASE_LOG, (Object)line);
                line = line.replace("VERSION=", "").replaceAll(DOUBLE_QUOTES, "").trim();
                String[] split = line.split("[()]");
                if (split.length <= 1) {
                    split = line.split(", ");
                }
                if (split.length > 0) {
                    versionId = split[0].trim();
                }
                if (split.length <= 1) continue;
                codeName = split[1].trim();
                continue;
            }
            if (line.startsWith("NAME=") && family == null) {
                LOG.debug(OS_RELEASE_LOG, (Object)line);
                family = line.replace("NAME=", "").replaceAll(DOUBLE_QUOTES, "").trim();
                continue;
            }
            if (!line.startsWith("VERSION_ID=") || !versionId.equals("unknown")) continue;
            LOG.debug(OS_RELEASE_LOG, (Object)line);
            versionId = line.replace("VERSION_ID=", "").replaceAll(DOUBLE_QUOTES, "").trim();
        }
        return family == null ? null : new Triplet<Object, String, String>(family, versionId, codeName);
    }

    private static Triplet<String, String, String> execLsbRelease() {
        String family = null;
        String versionId = "unknown";
        String codeName = "unknown";
        for (String line : ExecutingCommand.runNative("lsb_release -a")) {
            if (line.startsWith("Description:")) {
                LOG.debug(LSB_RELEASE_A_LOG, (Object)line);
                if (!(line = line.replace("Description:", "").trim()).contains(RELEASE_DELIM)) continue;
                Triplet<String, String, String> triplet = LinuxOperatingSystem.parseRelease(line, RELEASE_DELIM);
                family = triplet.getA();
                if (versionId.equals("unknown")) {
                    versionId = triplet.getB();
                }
                if (!codeName.equals("unknown")) continue;
                codeName = triplet.getC();
                continue;
            }
            if (line.startsWith("Distributor ID:") && family == null) {
                LOG.debug(LSB_RELEASE_A_LOG, (Object)line);
                family = line.replace("Distributor ID:", "").trim();
                continue;
            }
            if (line.startsWith("Release:") && versionId.equals("unknown")) {
                LOG.debug(LSB_RELEASE_A_LOG, (Object)line);
                versionId = line.replace("Release:", "").trim();
                continue;
            }
            if (!line.startsWith("Codename:") || !codeName.equals("unknown")) continue;
            LOG.debug(LSB_RELEASE_A_LOG, (Object)line);
            codeName = line.replace("Codename:", "").trim();
        }
        return family == null ? null : new Triplet<Object, String, String>(family, versionId, codeName);
    }

    private static Triplet<String, String, String> readLsbRelease() {
        String family = null;
        String versionId = "unknown";
        String codeName = "unknown";
        List<String> osRelease = FileUtil.readFile("/etc/lsb-release");
        for (String line : osRelease) {
            if (line.startsWith("DISTRIB_DESCRIPTION=")) {
                LOG.debug(LSB_RELEASE_LOG, (Object)line);
                if (!(line = line.replace("DISTRIB_DESCRIPTION=", "").replaceAll(DOUBLE_QUOTES, "").trim()).contains(RELEASE_DELIM)) continue;
                Triplet<String, String, String> triplet = LinuxOperatingSystem.parseRelease(line, RELEASE_DELIM);
                family = triplet.getA();
                if (versionId.equals("unknown")) {
                    versionId = triplet.getB();
                }
                if (!codeName.equals("unknown")) continue;
                codeName = triplet.getC();
                continue;
            }
            if (line.startsWith("DISTRIB_ID=") && family == null) {
                LOG.debug(LSB_RELEASE_LOG, (Object)line);
                family = line.replace("DISTRIB_ID=", "").replaceAll(DOUBLE_QUOTES, "").trim();
                continue;
            }
            if (line.startsWith("DISTRIB_RELEASE=") && versionId.equals("unknown")) {
                LOG.debug(LSB_RELEASE_LOG, (Object)line);
                versionId = line.replace("DISTRIB_RELEASE=", "").replaceAll(DOUBLE_QUOTES, "").trim();
                continue;
            }
            if (!line.startsWith("DISTRIB_CODENAME=") || !codeName.equals("unknown")) continue;
            LOG.debug(LSB_RELEASE_LOG, (Object)line);
            codeName = line.replace("DISTRIB_CODENAME=", "").replaceAll(DOUBLE_QUOTES, "").trim();
        }
        return family == null ? null : new Triplet<Object, String, String>(family, versionId, codeName);
    }

    private static Triplet<String, String, String> readDistribRelease(String filename) {
        if (new File(filename).exists()) {
            List<String> osRelease = FileUtil.readFile(filename);
            for (String line : osRelease) {
                LOG.debug("{}: {}", (Object)filename, (Object)line);
                if (line.contains(RELEASE_DELIM)) {
                    return LinuxOperatingSystem.parseRelease(line, RELEASE_DELIM);
                }
                if (!line.contains(" VERSION ")) continue;
                return LinuxOperatingSystem.parseRelease(line, " VERSION ");
            }
        }
        return null;
    }

    private static Triplet<String, String, String> parseRelease(String line, String splitLine) {
        String[] split = line.split(splitLine);
        String family = split[0].trim();
        String versionId = "unknown";
        String codeName = "unknown";
        if (split.length > 1) {
            if ((split = split[1].split("[()]")).length > 0) {
                versionId = split[0].trim();
            }
            if (split.length > 1) {
                codeName = split[1].trim();
            }
        }
        return new Triplet<String, String, String>(family, versionId, codeName);
    }

    protected static String getReleaseFilename() {
        File etc = new File("/etc");
        File[] matchingFiles = etc.listFiles(f -> (f.getName().endsWith("-release") || f.getName().endsWith("-version") || f.getName().endsWith("_release") || f.getName().endsWith("_version")) && !f.getName().endsWith("os-release") && !f.getName().endsWith("lsb-release") && !f.getName().endsWith("system-release"));
        if (matchingFiles != null && matchingFiles.length > 0) {
            return matchingFiles[0].getPath();
        }
        if (new File("/etc/release").exists()) {
            return "/etc/release";
        }
        return "/etc/issue";
    }

    private static String filenameToFamily(String name) {
        if (name.isEmpty()) {
            return "Solaris";
        }
        if ("issue".equalsIgnoreCase(name)) {
            return "Unknown";
        }
        Properties filenameProps = FileUtil.readPropertiesFromFilename(FILENAME_PROPERTIES);
        String family = filenameProps.getProperty(name.toLowerCase(Locale.ROOT));
        return family != null ? family : name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
    }

    @Override
    public List<OSService> getServices() {
        ArrayList<OSService> services = new ArrayList<OSService>();
        HashSet<String> running = new HashSet<String>();
        for (OSProcess p : this.getChildProcesses(1, OperatingSystem.ProcessFiltering.ALL_PROCESSES, OperatingSystem.ProcessSorting.PID_ASC, 0)) {
            OSService s = new OSService(p.getName(), p.getProcessID(), OSService.State.RUNNING);
            services.add(s);
            running.add(p.getName());
        }
        boolean systemctlFound = false;
        List<String> systemctl = ExecutingCommand.runNative("systemctl list-unit-files");
        for (String str : systemctl) {
            String shortName;
            String[] split = ParseUtil.whitespaces.split(str);
            if (split.length < 2 || !split[0].endsWith(".service") || !"enabled".equals(split[1])) continue;
            String name2 = split[0].substring(0, split[0].length() - 8);
            int index = name2.lastIndexOf(46);
            String string = shortName = index < 0 || index > name2.length() - 2 ? name2 : name2.substring(index + 1);
            if (running.contains(name2) || running.contains(shortName)) continue;
            OSService s = new OSService(name2, 0, OSService.State.STOPPED);
            services.add(s);
            systemctlFound = true;
        }
        if (!systemctlFound) {
            File dir = new File("/etc/init");
            if (dir.exists() && dir.isDirectory()) {
                for (File f2 : dir.listFiles((f, name) -> name.toLowerCase(Locale.ROOT).endsWith(".conf"))) {
                    String shortName;
                    String name3 = f2.getName().substring(0, f2.getName().length() - 5);
                    int index = name3.lastIndexOf(46);
                    String string = shortName = index < 0 || index > name3.length() - 2 ? name3 : name3.substring(index + 1);
                    if (running.contains(name3) || running.contains(shortName)) continue;
                    OSService s = new OSService(name3, 0, OSService.State.STOPPED);
                    services.add(s);
                }
            } else {
                LOG.error("Directory: /etc/init does not exist");
            }
        }
        return services;
    }

    public static long getHz() {
        return USER_HZ;
    }

    public static long getPageSize() {
        return PAGE_SIZE;
    }

    static {
        boolean hasUdev = false;
        boolean hasGettid = false;
        boolean hasSyscallGettid = false;
        try {
            if (GlobalConfig.get("oshi.os.linux.allowudev", true)) {
                try {
                    Udev lib = Udev.INSTANCE;
                    hasUdev = true;
                }
                catch (UnsatisfiedLinkError e) {
                    LOG.warn("Did not find udev library in operating system. Some features may not work.");
                }
            } else {
                LOG.info("Loading of udev not allowed by configuration. Some features may not work.");
            }
            try {
                LinuxLibc.INSTANCE.gettid();
                hasGettid = true;
            }
            catch (UnsatisfiedLinkError e) {
                LOG.debug("Did not find gettid function in operating system. Using fallbacks.");
            }
            hasSyscallGettid = hasGettid;
            if (!hasGettid) {
                try {
                    hasSyscallGettid = LinuxLibc.INSTANCE.syscall(LinuxLibc.SYS_GETTID, new Object[0]).intValue() > 0;
                }
                catch (UnsatisfiedLinkError e) {
                    LOG.debug("Did not find working syscall gettid function in operating system. Using procfs");
                }
            }
        }
        catch (NoClassDefFoundError e) {
            LOG.error("Did not JNA classes. Investigate incompatible version or missing native dll.");
        }
        HAS_UDEV = hasUdev;
        HAS_GETTID = hasGettid;
        HAS_SYSCALL_GETTID = hasSyscallGettid;
        Map<Integer, Long> auxv = Auxv.queryAuxv();
        long hz = auxv.getOrDefault(17, 0L);
        USER_HZ = hz > 0L ? hz : ParseUtil.parseLongOrDefault(ExecutingCommand.getFirstAnswer("getconf CLK_TCK"), 100L);
        long pagesz = Auxv.queryAuxv().getOrDefault(6, 0L);
        PAGE_SIZE = pagesz > 0L ? pagesz : ParseUtil.parseLongOrDefault(ExecutingCommand.getFirstAnswer("getconf PAGE_SIZE"), 4096L);
        OS_NAME = ExecutingCommand.getFirstAnswer("uname -o");
        long tempBT = CpuStat.getBootTime();
        if (tempBT == 0L) {
            tempBT = System.currentTimeMillis() / 1000L - (long)UpTime.getSystemUptimeSeconds();
        }
        BOOTTIME = tempBT;
        PPID_INDEX = new int[]{3};
    }
}

