/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.unix.openbsd;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.LibCAPI;
import com.sun.jna.platform.unix.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.jna.ByRef;
import oshi.jna.platform.unix.OpenBsdLibc;
import oshi.software.common.AbstractOSProcess;
import oshi.software.os.OSProcess;
import oshi.software.os.OSThread;
import oshi.software.os.unix.openbsd.OpenBsdOSThread;
import oshi.software.os.unix.openbsd.OpenBsdOperatingSystem;
import oshi.util.ExecutingCommand;
import oshi.util.Memoizer;
import oshi.util.ParseUtil;
import oshi.util.platform.unix.openbsd.FstatUtil;

@ThreadSafe
public class OpenBsdOSProcess
extends AbstractOSProcess {
    private static final Logger LOG = LoggerFactory.getLogger(OpenBsdOSProcess.class);
    static final String PS_THREAD_COLUMNS = Arrays.stream(PsThreadColumns.values()).map(Enum::name).map(name -> name.toLowerCase(Locale.ROOT)).collect(Collectors.joining(","));
    private static final int ARGMAX;
    private final OpenBsdOperatingSystem os;
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
    private int bitness;
    private String commandLineBackup;

    public OpenBsdOSProcess(int pid, Map<OpenBsdOperatingSystem.PsKeywords, String> psMap, OpenBsdOperatingSystem os) {
        super(pid);
        this.os = os;
        this.bitness = Native.LONG_SIZE * 8;
        this.updateThreadCount();
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
            int[] mib = new int[]{1, 55, this.getProcessID(), 1};
            try (Memory m = new Memory(ARGMAX);
                 ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference((long)ARGMAX);){
                if (OpenBsdLibc.INSTANCE.sysctl(mib, mib.length, m, size, null, LibCAPI.size_t.ZERO) == 0) {
                    ArrayList<String> args2 = new ArrayList<String>();
                    long offset = 0L;
                    long baseAddr = Pointer.nativeValue(m);
                    long maxAddr = baseAddr + size.getValue().longValue();
                    long argAddr = Pointer.nativeValue(m.getPointer(offset));
                    while (argAddr > baseAddr && argAddr < maxAddr) {
                        args2.add(m.getString(argAddr - baseAddr));
                        argAddr = Pointer.nativeValue(m.getPointer(offset += (long)Native.POINTER_SIZE));
                    }
                    List<String> list = Collections.unmodifiableList(args2);
                    return list;
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
        return this.environmentVariables.get();
    }

    private Map<String, String> queryEnvironmentVariables() {
        int[] mib = new int[]{1, 55, this.getProcessID(), 3};
        try (Memory m = new Memory(ARGMAX);
             ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference((long)ARGMAX);){
            if (OpenBsdLibc.INSTANCE.sysctl(mib, mib.length, m, size, null, LibCAPI.size_t.ZERO) == 0) {
                LinkedHashMap<String, String> env = new LinkedHashMap<String, String>();
                long offset = 0L;
                long baseAddr = Pointer.nativeValue(m);
                long maxAddr = baseAddr + size.longValue();
                long argAddr = Pointer.nativeValue(m.getPointer(offset));
                while (argAddr > baseAddr && argAddr < maxAddr) {
                    String envStr = m.getString(argAddr - baseAddr);
                    int idx = envStr.indexOf(61);
                    if (idx > 0) {
                        env.put(envStr.substring(0, idx), envStr.substring(idx + 1));
                    }
                    argAddr = Pointer.nativeValue(m.getPointer(offset += (long)Native.POINTER_SIZE));
                }
                Map<String, String> map = Collections.unmodifiableMap(env);
                return map;
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public String getCurrentWorkingDirectory() {
        return FstatUtil.getCwd(this.getProcessID());
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
        return FstatUtil.getOpenFiles(this.getProcessID());
    }

    @Override
    public long getSoftOpenFileLimit() {
        if (this.getProcessID() == this.os.getProcessId()) {
            Resource.Rlimit rlimit = new Resource.Rlimit();
            OpenBsdLibc.INSTANCE.getrlimit(7, rlimit);
            return rlimit.rlim_cur;
        }
        return -1L;
    }

    @Override
    public long getHardOpenFileLimit() {
        if (this.getProcessID() == this.os.getProcessId()) {
            Resource.Rlimit rlimit = new Resource.Rlimit();
            OpenBsdLibc.INSTANCE.getrlimit(7, rlimit);
            return rlimit.rlim_max;
        }
        return -1L;
    }

    @Override
    public int getBitness() {
        return this.bitness;
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

    @Override
    public List<OSThread> getThreadDetails() {
        String psCommand = "ps -aHwwxo " + PS_THREAD_COLUMNS;
        if (this.getProcessID() >= 0) {
            psCommand = psCommand + " -p " + this.getProcessID();
        }
        Predicate<Map> hasColumnsArgs = threadMap -> threadMap.containsKey((Object)PsThreadColumns.ARGS);
        return ExecutingCommand.runNative(psCommand).stream().skip(1L).map(thread2 -> ParseUtil.stringToEnumMap(PsThreadColumns.class, thread2.trim(), ' ')).filter(hasColumnsArgs).map(threadMap -> new OpenBsdOSThread(this.getProcessID(), (Map<PsThreadColumns, String>)threadMap)).filter(OSThread.ThreadFiltering.VALID_THREAD).collect(Collectors.toList());
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
        Map<OpenBsdOperatingSystem.PsKeywords, String> psMap;
        String psCommand = "ps -awwxo " + OpenBsdOperatingSystem.PS_COMMAND_ARGS + " -p " + this.getProcessID();
        List<String> procList = ExecutingCommand.runNative(psCommand);
        if (procList.size() > 1 && (psMap = ParseUtil.stringToEnumMap(OpenBsdOperatingSystem.PsKeywords.class, procList.get(1).trim(), ' ')).containsKey((Object)OpenBsdOperatingSystem.PsKeywords.ARGS)) {
            this.updateThreadCount();
            return this.updateAttributes(psMap);
        }
        this.state = OSProcess.State.INVALID;
        return false;
    }

    private boolean updateAttributes(Map<OpenBsdOperatingSystem.PsKeywords, String> psMap) {
        long now = System.currentTimeMillis();
        switch (psMap.get((Object)OpenBsdOperatingSystem.PsKeywords.STATE).charAt(0)) {
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
        this.parentProcessID = ParseUtil.parseIntOrDefault(psMap.get((Object)OpenBsdOperatingSystem.PsKeywords.PPID), 0);
        this.user = psMap.get((Object)OpenBsdOperatingSystem.PsKeywords.USER);
        this.userID = psMap.get((Object)OpenBsdOperatingSystem.PsKeywords.UID);
        this.group = psMap.get((Object)OpenBsdOperatingSystem.PsKeywords.GROUP);
        this.groupID = psMap.get((Object)OpenBsdOperatingSystem.PsKeywords.GID);
        this.priority = ParseUtil.parseIntOrDefault(psMap.get((Object)OpenBsdOperatingSystem.PsKeywords.PRI), 0);
        this.virtualSize = ParseUtil.parseLongOrDefault(psMap.get((Object)OpenBsdOperatingSystem.PsKeywords.VSZ), 0L) * 1024L;
        this.residentSetSize = ParseUtil.parseLongOrDefault(psMap.get((Object)OpenBsdOperatingSystem.PsKeywords.RSS), 0L) * 1024L;
        long elapsedTime = ParseUtil.parseDHMSOrDefault(psMap.get((Object)OpenBsdOperatingSystem.PsKeywords.ETIME), 0L);
        this.upTime = elapsedTime < 1L ? 1L : elapsedTime;
        this.startTime = now - this.upTime;
        this.userTime = ParseUtil.parseDHMSOrDefault(psMap.get((Object)OpenBsdOperatingSystem.PsKeywords.CPUTIME), 0L);
        this.kernelTime = 0L;
        this.path = psMap.get((Object)OpenBsdOperatingSystem.PsKeywords.COMM);
        this.name = this.path.substring(this.path.lastIndexOf(47) + 1);
        this.minorFaults = ParseUtil.parseLongOrDefault(psMap.get((Object)OpenBsdOperatingSystem.PsKeywords.MINFLT), 0L);
        this.majorFaults = ParseUtil.parseLongOrDefault(psMap.get((Object)OpenBsdOperatingSystem.PsKeywords.MAJFLT), 0L);
        long nonVoluntaryContextSwitches = ParseUtil.parseLongOrDefault(psMap.get((Object)OpenBsdOperatingSystem.PsKeywords.NIVCSW), 0L);
        long voluntaryContextSwitches = ParseUtil.parseLongOrDefault(psMap.get((Object)OpenBsdOperatingSystem.PsKeywords.NVCSW), 0L);
        this.contextSwitches = voluntaryContextSwitches + nonVoluntaryContextSwitches;
        this.commandLineBackup = psMap.get((Object)OpenBsdOperatingSystem.PsKeywords.ARGS);
        return true;
    }

    private void updateThreadCount() {
        List<String> threadList = ExecutingCommand.runNative("ps -axHo tid -p " + this.getProcessID());
        if (!threadList.isEmpty()) {
            this.threadCount = threadList.size() - 1;
        }
        this.threadCount = 1;
    }

    static {
        int[] mib = new int[]{1, 8};
        try (Memory m = new Memory(4L);
             ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference(4L);){
            if (OpenBsdLibc.INSTANCE.sysctl(mib, mib.length, m, size, null, LibCAPI.size_t.ZERO) == 0) {
                ARGMAX = m.getInt(0L);
            } else {
                LOG.warn("Failed sysctl call for process arguments max size (kern.argmax). Error code: {}", (Object)Native.getLastError());
                ARGMAX = 0;
            }
        }
    }

    static enum PsThreadColumns {
        TID,
        STATE,
        ETIME,
        CPUTIME,
        NIVCSW,
        NVCSW,
        MAJFLT,
        MINFLT,
        PRI,
        ARGS;

    }
}

