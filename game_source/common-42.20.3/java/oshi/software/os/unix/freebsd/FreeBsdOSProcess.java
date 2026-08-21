/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.unix.freebsd;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.unix.LibCAPI;
import com.sun.jna.platform.unix.Resource;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.jna.ByRef;
import oshi.jna.platform.unix.FreeBsdLibc;
import oshi.software.common.AbstractOSProcess;
import oshi.software.os.OSProcess;
import oshi.software.os.OSThread;
import oshi.software.os.unix.freebsd.FreeBsdOSThread;
import oshi.software.os.unix.freebsd.FreeBsdOperatingSystem;
import oshi.util.ExecutingCommand;
import oshi.util.FileUtil;
import oshi.util.Memoizer;
import oshi.util.ParseUtil;
import oshi.util.platform.unix.freebsd.BsdSysctlUtil;
import oshi.util.platform.unix.freebsd.ProcstatUtil;

@ThreadSafe
public class FreeBsdOSProcess
extends AbstractOSProcess {
    private static final Logger LOG = LoggerFactory.getLogger(FreeBsdOSProcess.class);
    private static final int ARGMAX = BsdSysctlUtil.sysctl("kern.argmax", 0);
    private final FreeBsdOperatingSystem os;
    static final String PS_THREAD_COLUMNS = Arrays.stream(PsThreadColumns.values()).map(Enum::name).map(name -> name.toLowerCase(Locale.ROOT)).collect(Collectors.joining(","));
    private Supplier<Integer> bitness = Memoizer.memoize(this::queryBitness);
    private Supplier<String> commandLine = Memoizer.memoize(this::queryCommandLine);
    private Supplier<List<String>> arguments = Memoizer.memoize(this::queryArguments);
    private Supplier<Map<String, String>> environmentVariables = Memoizer.memoize(this::queryEnvironmentVariables);
    private String name;
    private String path = "";
    private String user;
    private String userID;
    private String group;
    private String groupID;
    private OSProcess.State state = OSProcess.State.INVALID;
    private int parentProcessID;
    private int threadCount;
    private int priority;
    private long virtualSize;
    private long residentSetSize;
    private long kernelTime;
    private long userTime;
    private long startTime;
    private long upTime;
    private long bytesRead;
    private long bytesWritten;
    private long minorFaults;
    private long majorFaults;
    private long contextSwitches;
    private String commandLineBackup;

    public FreeBsdOSProcess(int pid, Map<FreeBsdOperatingSystem.PsKeywords, String> psMap, FreeBsdOperatingSystem os) {
        super(pid);
        this.os = os;
        this.updateAttributes(psMap);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public String getCommandLine() {
        return this.commandLine.get();
    }

    private String queryCommandLine() {
        String cl = String.join((CharSequence)" ", this.getArguments());
        return cl.isEmpty() ? this.commandLineBackup : cl;
    }

    @Override
    public List<String> getArguments() {
        return this.arguments.get();
    }

    private List<String> queryArguments() {
        if (ARGMAX > 0) {
            int[] mib = new int[]{1, 14, 7, this.getProcessID()};
            try (Memory m = new Memory(ARGMAX);
                 ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference((long)ARGMAX);){
                if (FreeBsdLibc.INSTANCE.sysctl(mib, mib.length, m, size, null, LibCAPI.size_t.ZERO) == 0) {
                    List<String> list = Collections.unmodifiableList(ParseUtil.parseByteArrayToStrings(m.getByteArray(0L, size.getValue().intValue())));
                    return list;
                }
                LOG.warn("Failed sysctl call for process arguments (kern.proc.args), process {} may not exist. Error code: {}", (Object)this.getProcessID(), (Object)Native.getLastError());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
        return this.environmentVariables.get();
    }

    private Map<String, String> queryEnvironmentVariables() {
        if (ARGMAX > 0) {
            int[] mib = new int[]{1, 14, 35, this.getProcessID()};
            try (Memory m = new Memory(ARGMAX);
                 ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference((long)ARGMAX);){
                if (FreeBsdLibc.INSTANCE.sysctl(mib, mib.length, m, size, null, LibCAPI.size_t.ZERO) == 0) {
                    Map<String, String> map = Collections.unmodifiableMap(ParseUtil.parseByteArrayToStringMap(m.getByteArray(0L, size.getValue().intValue())));
                    return map;
                }
                LOG.warn("Failed sysctl call for process environment variables (kern.proc.env), process {} may not exist. Error code: {}", (Object)this.getProcessID(), (Object)Native.getLastError());
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public String getCurrentWorkingDirectory() {
        return ProcstatUtil.getCwd(this.getProcessID());
    }

    @Override
    public String getUser() {
        return this.user;
    }

    @Override
    public String getUserID() {
        return this.userID;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public String getGroupID() {
        return this.groupID;
    }

    @Override
    public OSProcess.State getState() {
        return this.state;
    }

    @Override
    public int getParentProcessID() {
        return this.parentProcessID;
    }

    @Override
    public int getThreadCount() {
        return this.threadCount;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public long getVirtualSize() {
        return this.virtualSize;
    }

    @Override
    public long getResidentSetSize() {
        return this.residentSetSize;
    }

    @Override
    public long getKernelTime() {
        return this.kernelTime;
    }

    @Override
    public long getUserTime() {
        return this.userTime;
    }

    @Override
    public long getUpTime() {
        return this.upTime;
    }

    @Override
    public long getStartTime() {
        return this.startTime;
    }

    @Override
    public long getBytesRead() {
        return this.bytesRead;
    }

    @Override
    public long getBytesWritten() {
        return this.bytesWritten;
    }

    @Override
    public long getOpenFiles() {
        return ProcstatUtil.getOpenFiles(this.getProcessID());
    }

    @Override
    public long getSoftOpenFileLimit() {
        if (this.getProcessID() == this.os.getProcessId()) {
            Resource.Rlimit rlimit = new Resource.Rlimit();
            FreeBsdLibc.INSTANCE.getrlimit(7, rlimit);
            return rlimit.rlim_cur;
        }
        return this.getProcessOpenFileLimit(this.getProcessID(), 1);
    }

    @Override
    public long getHardOpenFileLimit() {
        if (this.getProcessID() == this.os.getProcessId()) {
            Resource.Rlimit rlimit = new Resource.Rlimit();
            FreeBsdLibc.INSTANCE.getrlimit(7, rlimit);
            return rlimit.rlim_max;
        }
        return this.getProcessOpenFileLimit(this.getProcessID(), 2);
    }

    @Override
    public int getBitness() {
        return this.bitness.get();
    }

    @Override
    public long getAffinityMask() {
        long bitMask = 0L;
        String cpuset = ExecutingCommand.getFirstAnswer("cpuset -gp " + this.getProcessID());
        String[] split = cpuset.split(":");
        if (split.length > 1) {
            String[] bits;
            for (String bit : bits = split[1].split(",")) {
                int bitToSet = ParseUtil.parseIntOrDefault(bit.trim(), -1);
                if (bitToSet < 0) continue;
                bitMask |= 1L << bitToSet;
            }
        }
        return bitMask;
    }

    private int queryBitness() {
        int[] mib = new int[]{1, 14, 9, this.getProcessID()};
        try (Memory abi = new Memory(32L);
             ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference(32L);){
            if (0 == FreeBsdLibc.INSTANCE.sysctl(mib, mib.length, abi, size, null, LibCAPI.size_t.ZERO)) {
                String elf = abi.getString(0L);
                if (elf.contains("ELF32")) {
                    int n = 32;
                    return n;
                }
                if (elf.contains("ELF64")) {
                    int n = 64;
                    return n;
                }
            }
        }
        return 0;
    }

    @Override
    public List<OSThread> getThreadDetails() {
        String psCommand = "ps -awwxo " + PS_THREAD_COLUMNS + " -H";
        if (this.getProcessID() >= 0) {
            psCommand = psCommand + " -p " + this.getProcessID();
        }
        Predicate<Map> hasColumnsPri = threadMap -> threadMap.containsKey((Object)PsThreadColumns.PRI);
        return ((Stream)ExecutingCommand.runNative(psCommand).stream().skip(1L).parallel()).map(thread2 -> ParseUtil.stringToEnumMap(PsThreadColumns.class, thread2.trim(), ' ')).filter(hasColumnsPri).map(threadMap -> new FreeBsdOSThread(this.getProcessID(), (Map<PsThreadColumns, String>)threadMap)).filter(OSThread.ThreadFiltering.VALID_THREAD).collect(Collectors.toList());
    }

    @Override
    public long getMinorFaults() {
        return this.minorFaults;
    }

    @Override
    public long getMajorFaults() {
        return this.majorFaults;
    }

    @Override
    public long getContextSwitches() {
        return this.contextSwitches;
    }

    @Override
    public boolean updateAttributes() {
        Map<FreeBsdOperatingSystem.PsKeywords, String> psMap;
        String psCommand = "ps -awwxo " + FreeBsdOperatingSystem.PS_COMMAND_ARGS + " -p " + this.getProcessID();
        List<String> procList = ExecutingCommand.runNative(psCommand);
        if (procList.size() > 1 && (psMap = ParseUtil.stringToEnumMap(FreeBsdOperatingSystem.PsKeywords.class, procList.get(1).trim(), ' ')).containsKey((Object)FreeBsdOperatingSystem.PsKeywords.ARGS)) {
            return this.updateAttributes(psMap);
        }
        this.state = OSProcess.State.INVALID;
        return false;
    }

    private boolean updateAttributes(Map<FreeBsdOperatingSystem.PsKeywords, String> psMap) {
        long now = System.currentTimeMillis();
        switch (psMap.get((Object)FreeBsdOperatingSystem.PsKeywords.STATE).charAt(0)) {
            case 'R': {
                this.state = OSProcess.State.RUNNING;
                break;
            }
            case 'I': 
            case 'S': {
                this.state = OSProcess.State.SLEEPING;
                break;
            }
            case 'D': 
            case 'L': 
            case 'U': {
                this.state = OSProcess.State.WAITING;
                break;
            }
            case 'Z': {
                this.state = OSProcess.State.ZOMBIE;
                break;
            }
            case 'T': {
                this.state = OSProcess.State.STOPPED;
                break;
            }
            default: {
                this.state = OSProcess.State.OTHER;
            }
        }
        this.parentProcessID = ParseUtil.parseIntOrDefault(psMap.get((Object)FreeBsdOperatingSystem.PsKeywords.PPID), 0);
        this.user = psMap.get((Object)FreeBsdOperatingSystem.PsKeywords.USER);
        this.userID = psMap.get((Object)FreeBsdOperatingSystem.PsKeywords.UID);
        this.group = psMap.get((Object)FreeBsdOperatingSystem.PsKeywords.GROUP);
        this.groupID = psMap.get((Object)FreeBsdOperatingSystem.PsKeywords.GID);
        this.threadCount = ParseUtil.parseIntOrDefault(psMap.get((Object)FreeBsdOperatingSystem.PsKeywords.NLWP), 0);
        this.priority = ParseUtil.parseIntOrDefault(psMap.get((Object)FreeBsdOperatingSystem.PsKeywords.PRI), 0);
        this.virtualSize = ParseUtil.parseLongOrDefault(psMap.get((Object)FreeBsdOperatingSystem.PsKeywords.VSZ), 0L) * 1024L;
        this.residentSetSize = ParseUtil.parseLongOrDefault(psMap.get((Object)FreeBsdOperatingSystem.PsKeywords.RSS), 0L) * 1024L;
        long elapsedTime = ParseUtil.parseDHMSOrDefault(psMap.get((Object)FreeBsdOperatingSystem.PsKeywords.ETIMES), 0L);
        this.upTime = elapsedTime < 1L ? 1L : elapsedTime;
        this.startTime = now - this.upTime;
        this.kernelTime = ParseUtil.parseDHMSOrDefault(psMap.get((Object)FreeBsdOperatingSystem.PsKeywords.SYSTIME), 0L);
        this.userTime = ParseUtil.parseDHMSOrDefault(psMap.get((Object)FreeBsdOperatingSystem.PsKeywords.TIME), 0L) - this.kernelTime;
        this.path = psMap.get((Object)FreeBsdOperatingSystem.PsKeywords.COMM);
        this.name = this.path.substring(this.path.lastIndexOf(47) + 1);
        this.minorFaults = ParseUtil.parseLongOrDefault(psMap.get((Object)FreeBsdOperatingSystem.PsKeywords.MAJFLT), 0L);
        this.majorFaults = ParseUtil.parseLongOrDefault(psMap.get((Object)FreeBsdOperatingSystem.PsKeywords.MINFLT), 0L);
        long nonVoluntaryContextSwitches = ParseUtil.parseLongOrDefault(psMap.get((Object)FreeBsdOperatingSystem.PsKeywords.NVCSW), 0L);
        long voluntaryContextSwitches = ParseUtil.parseLongOrDefault(psMap.get((Object)FreeBsdOperatingSystem.PsKeywords.NIVCSW), 0L);
        this.contextSwitches = voluntaryContextSwitches + nonVoluntaryContextSwitches;
        this.commandLineBackup = psMap.get((Object)FreeBsdOperatingSystem.PsKeywords.ARGS);
        return true;
    }

    private long getProcessOpenFileLimit(long processId, int index) {
        String limitsPath = String.format(Locale.ROOT, "/proc/%d/limits", processId);
        if (!Files.exists(Paths.get(limitsPath, new String[0]), new LinkOption[0])) {
            return -1L;
        }
        List<String> lines = FileUtil.readFile(limitsPath);
        Optional<String> maxOpenFilesLine = lines.stream().filter(line -> line.startsWith("Max open files")).findFirst();
        if (!maxOpenFilesLine.isPresent()) {
            return -1L;
        }
        String[] split = maxOpenFilesLine.get().split("\\D+");
        return ParseUtil.parseLongOrDefault(split[index], -1L);
    }

    static enum PsThreadColumns {
        TDNAME,
        LWP,
        STATE,
        ETIMES,
        SYSTIME,
        TIME,
        TDADDR,
        NIVCSW,
        NVCSW,
        MAJFLT,
        MINFLT,
        PRI;

    }
}

