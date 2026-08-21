/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.mac;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.mac.ThreadInfo;
import oshi.ffm.ForeignFunctions;
import oshi.ffm.mac.IOKit;
import oshi.ffm.mac.MacSystem;
import oshi.ffm.mac.MacSystemFunctions;
import oshi.software.common.AbstractOSProcess;
import oshi.software.os.OSProcess;
import oshi.software.os.OSThread;
import oshi.software.os.mac.MacOSThread;
import oshi.software.os.mac.MacOperatingSystemFFM;
import oshi.util.GlobalConfig;
import oshi.util.Memoizer;
import oshi.util.ParseUtil;
import oshi.util.platform.mac.IOKitUtilFFM;
import oshi.util.platform.mac.SysctlUtilFFM;
import oshi.util.tuples.Pair;

@ThreadSafe
public class MacOSProcessFFM
extends AbstractOSProcess {
    private static final Logger LOG = LoggerFactory.getLogger(MacOSProcessFFM.class);
    private static final int ARGMAX = SysctlUtilFFM.sysctl("kern.argmax", 0);
    private static final long TICKS_PER_MS;
    private static final boolean LOG_MAC_SYSCTL_WARNING;
    private static final int MAC_RLIMIT_NOFILE = 8;
    private static final int P_LP64 = 4;
    private static final int SSLEEP = 1;
    private static final int SWAIT = 2;
    private static final int SRUN = 3;
    private static final int SIDL = 4;
    private static final int SZOMB = 5;
    private static final int SSTOP = 6;
    private int majorVersion;
    private int minorVersion;
    private final MacOperatingSystemFFM os;
    private Supplier<String> commandLine = Memoizer.memoize(this::queryCommandLine);
    private Supplier<Pair<List<String>, Map<String, String>>> argsEnviron = Memoizer.memoize(this::queryArgsAndEnvironment);
    private String name = "";
    private String path = "";
    private String currentWorkingDirectory;
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
    private long openFiles;
    private int bitness;
    private long minorFaults;
    private long majorFaults;
    private long contextSwitches;

    public MacOSProcessFFM(int pid, int major, int minor, MacOperatingSystemFFM os) {
        super(pid);
        this.majorVersion = major;
        this.minorVersion = minor;
        this.os = os;
        this.updateAttributes();
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
        return String.join((CharSequence)" ", this.getArguments());
    }

    @Override
    public List<String> getArguments() {
        return this.argsEnviron.get().getA();
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
        return this.argsEnviron.get().getB();
    }

    private Pair<List<String>, Map<String, String>> queryArgsAndEnvironment() {
        LinkedHashMap<String, String> env;
        ArrayList<String> args2;
        block15: {
            int pid = this.getProcessID();
            args2 = new ArrayList<String>();
            env = new LinkedHashMap<String, String>();
            int[] mib = new int[]{1, 49, pid};
            try (Arena arena = Arena.ofConfined();){
                MemorySegment procargs = arena.allocate(ARGMAX);
                procargs.fill((byte)0);
                long size = SysctlUtilFFM.sysctl(mib, procargs);
                if (size > 0L) {
                    int nargs = procargs.get(ValueLayout.JAVA_INT, 0L);
                    if (nargs <= 0 || nargs > 1024) break block15;
                    long offset = 4L;
                    offset += (long)procargs.getString(offset).length();
                    while (offset < size) {
                        while (offset < size && procargs.get(ValueLayout.JAVA_BYTE, offset) == 0) {
                            ++offset;
                        }
                        if (offset >= size) {
                            break block15;
                        }
                        String arg = procargs.getString(offset);
                        if (arg.isEmpty()) {
                            break block15;
                        }
                        if (nargs-- > 0) {
                            args2.add(arg);
                        } else {
                            int idx = arg.indexOf(61);
                            if (idx > 0) {
                                env.put(arg.substring(0, idx), arg.substring(idx + 1));
                            }
                        }
                        offset += (long)(arg.length() + 1);
                    }
                    break block15;
                }
                if (pid > 0 && LOG_MAC_SYSCTL_WARNING) {
                    LOG.warn("Failed sysctl call for process arguments (kern.procargs2), process {} may not exist.", (Object)pid);
                }
            }
        }
        return new Pair<List<String>, Map<String, String>>(Collections.unmodifiableList(args2), Collections.unmodifiableMap(env));
    }

    @Override
    public String getCurrentWorkingDirectory() {
        return this.currentWorkingDirectory;
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
    public List<OSThread> getThreadDetails() {
        long now = System.currentTimeMillis();
        return ((Stream)ThreadInfo.queryTaskThreads(this.getProcessID()).stream().parallel()).map(stat -> {
            long start = Math.max(now - stat.getUpTime(), this.getStartTime());
            return new MacOSThread(this.getProcessID(), stat.getThreadId(), stat.getState(), stat.getSystemTime(), stat.getUserTime(), start, now - start, stat.getPriority());
        }).collect(Collectors.toList());
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
        return this.openFiles;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public long getSoftOpenFileLimit() {
        if (this.getProcessID() != this.os.getProcessId()) return -1L;
        try (Arena arena = Arena.ofConfined();){
            MemorySegment buffer = arena.allocate(MacSystem.RLIMIT);
            int result = MacSystemFunctions.getrlimit(8, buffer);
            if (result <= 0) return 0L;
            long l = buffer.get(ValueLayout.JAVA_LONG, MacSystem.RLIMIT.byteOffset(MacSystem.RLIM_CUR));
            return l;
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return 0L;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public long getHardOpenFileLimit() {
        if (this.getProcessID() != this.os.getProcessId()) return -1L;
        try (Arena arena = Arena.ofConfined();){
            MemorySegment buffer = arena.allocate(MacSystem.RLIMIT);
            int result = MacSystemFunctions.getrlimit(8, buffer);
            if (result <= 0) return 0L;
            long l = buffer.get(ValueLayout.JAVA_LONG, MacSystem.RLIMIT.byteOffset(MacSystem.RLIM_MAX));
            return l;
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return 0L;
    }

    @Override
    public int getBitness() {
        return this.bitness;
    }

    @Override
    public long getAffinityMask() {
        int logicalProcessorCount = SysctlUtilFFM.sysctl("hw.logicalcpu", 1);
        return logicalProcessorCount < 64 ? (1L << logicalProcessorCount) - 1L : -1L;
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

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public boolean updateAttributes() {
        long now = System.currentTimeMillis();
        int pid = this.getProcessID();
        try (Arena arena = Arena.ofConfined();){
            MemorySegment vnodeInfo;
            MemorySegment rusage;
            MemorySegment taskAllInfo = arena.allocate(MacSystem.PROC_TASK_ALL_INFO);
            int infoResult = MacSystemFunctions.proc_pidinfo(pid, 2, 0L, taskAllInfo, (int)MacSystem.PROC_TASK_ALL_INFO.byteSize());
            if (infoResult <= 0) {
                this.state = OSProcess.State.INVALID;
                boolean bl = false;
                return bl;
            }
            MemorySegment ptinfo = taskAllInfo.asSlice(MacSystem.PROC_TASK_ALL_INFO.byteOffset(MacSystem.PTINFO), MacSystem.PROC_TASK_INFO.byteSize());
            if (ptinfo.get(ValueLayout.JAVA_INT, MacSystem.PROC_TASK_INFO.byteOffset(MacSystem.PTI_THREADNUM)) < 1) {
                this.state = OSProcess.State.INVALID;
                boolean bl = false;
                return bl;
            }
            MemorySegment pbsd = taskAllInfo.asSlice(MacSystem.PROC_TASK_ALL_INFO.byteOffset(MacSystem.PBSD), MacSystem.PROC_BSD_INFO.byteSize());
            MemorySegment pathBuf = arena.allocate(4096L);
            if (MacSystemFunctions.proc_pidpath(pid, pathBuf, 4096) > 0) {
                this.path = pathBuf.getString(0L).trim();
                String[] pathSplit = this.path.split("/");
                if (pathSplit.length > 0) {
                    this.name = pathSplit[pathSplit.length - 1];
                }
            }
            if (this.name.isEmpty()) {
                this.name = pbsd.asSlice(MacSystem.PROC_BSD_INFO.byteOffset(MacSystem.PBI_COMM)).getString(0L);
            }
            int status = pbsd.get(ValueLayout.JAVA_INT, MacSystem.PROC_BSD_INFO.byteOffset(MacSystem.PBI_STATUS));
            this.state = switch (status) {
                case 1 -> OSProcess.State.SLEEPING;
                case 2 -> OSProcess.State.WAITING;
                case 3 -> OSProcess.State.RUNNING;
                case 4 -> OSProcess.State.NEW;
                case 5 -> OSProcess.State.ZOMBIE;
                case 6 -> OSProcess.State.STOPPED;
                default -> OSProcess.State.OTHER;
            };
            this.parentProcessID = pbsd.get(ValueLayout.JAVA_INT, MacSystem.PROC_BSD_INFO.byteOffset(MacSystem.PBI_PPID));
            int uid = pbsd.get(ValueLayout.JAVA_INT, MacSystem.PROC_BSD_INFO.byteOffset(MacSystem.PBI_UID));
            this.userID = Integer.toString(uid);
            MemorySegment pwuid = MacSystemFunctions.getpwuid(uid);
            if (pwuid != null) {
                MemorySegment passwdStruct = ForeignFunctions.getStructFromNativePointer(pwuid, MacSystem.PASSWD, arena);
                MemorySegment nameAddress = passwdStruct.get(ValueLayout.ADDRESS, MacSystem.PASSWD.byteOffset(MemoryLayout.PathElement.groupElement("pw_name")));
                this.user = ForeignFunctions.getStringFromNativePointer(nameAddress, arena);
            } else {
                this.user = this.userID;
            }
            int gid = pbsd.get(ValueLayout.JAVA_INT, MacSystem.PROC_BSD_INFO.byteOffset(MacSystem.PBI_GID));
            this.groupID = Integer.toString(gid);
            MemorySegment grgid = MacSystemFunctions.getgrgid(gid);
            if (grgid != null) {
                MemorySegment groupStruct = ForeignFunctions.getStructFromNativePointer(pwuid, MacSystem.GROUP, arena);
                MemorySegment nameAddress = groupStruct.get(ValueLayout.ADDRESS, MacSystem.GROUP.byteOffset(MemoryLayout.PathElement.groupElement("gr_name")));
                this.group = ForeignFunctions.getStringFromNativePointer(nameAddress, arena);
            } else {
                this.group = this.groupID;
            }
            this.threadCount = ptinfo.get(ValueLayout.JAVA_INT, MacSystem.PROC_TASK_INFO.byteOffset(MacSystem.PTI_THREADNUM));
            this.priority = ptinfo.get(ValueLayout.JAVA_INT, MacSystem.PROC_TASK_INFO.byteOffset(MacSystem.PTI_PRIORITY));
            this.virtualSize = ptinfo.get(ValueLayout.JAVA_LONG, MacSystem.PROC_TASK_INFO.byteOffset(MacSystem.PTI_VIRTUAL_SIZE));
            this.residentSetSize = ptinfo.get(ValueLayout.JAVA_LONG, MacSystem.PROC_TASK_INFO.byteOffset(MacSystem.PTI_RESIDENT_SIZE));
            this.kernelTime = ptinfo.get(ValueLayout.JAVA_LONG, MacSystem.PROC_TASK_INFO.byteOffset(MacSystem.PTI_TOTAL_SYSTEM)) / TICKS_PER_MS;
            this.userTime = ptinfo.get(ValueLayout.JAVA_LONG, MacSystem.PROC_TASK_INFO.byteOffset(MacSystem.PTI_TOTAL_USER)) / TICKS_PER_MS;
            long startSec = pbsd.get(ValueLayout.JAVA_LONG, MacSystem.PROC_BSD_INFO.byteOffset(MacSystem.PBI_START_TVSEC));
            long startUsec = pbsd.get(ValueLayout.JAVA_LONG, MacSystem.PROC_BSD_INFO.byteOffset(MacSystem.PBI_START_TVUSEC));
            this.startTime = startSec * 1000L + startUsec / 1000L;
            this.upTime = now - this.startTime;
            this.openFiles = pbsd.get(ValueLayout.JAVA_INT, MacSystem.PROC_BSD_INFO.byteOffset(MacSystem.PBI_NFILES));
            int flags = pbsd.get(ValueLayout.JAVA_INT, MacSystem.PROC_BSD_INFO.byteOffset(MacSystem.PBI_FLAGS));
            this.bitness = (flags & 4) == 0 ? 32 : 64;
            this.majorFaults = ptinfo.get(ValueLayout.JAVA_INT, MacSystem.PROC_TASK_INFO.byteOffset(MacSystem.PTI_PAGEINS));
            int totalFaults = ptinfo.get(ValueLayout.JAVA_INT, MacSystem.PROC_TASK_INFO.byteOffset(MacSystem.PTI_FAULTS));
            this.minorFaults = (long)totalFaults - this.majorFaults;
            this.contextSwitches = ptinfo.get(ValueLayout.JAVA_INT, MacSystem.PROC_TASK_INFO.byteOffset(MacSystem.PTI_CSW));
            if ((this.majorVersion > 10 || this.minorVersion >= 9) && 0 == MacSystemFunctions.proc_pid_rusage(pid, 2, rusage = arena.allocate(MacSystem.RUSAGEINFOV2))) {
                this.bytesRead = rusage.get(ValueLayout.JAVA_LONG, MacSystem.RUSAGEINFOV2.byteOffset(MacSystem.RI_DISKIO_BYTESREAD));
                this.bytesWritten = rusage.get(ValueLayout.JAVA_LONG, MacSystem.RUSAGEINFOV2.byteOffset(MacSystem.RI_DISKIO_BYTESWRITTEN));
            }
            if (MacSystemFunctions.proc_pidinfo(pid, 9, 0L, vnodeInfo = arena.allocate(MacSystem.VNODE_PATH_INFO), (int)MacSystem.VNODE_PATH_INFO.byteSize()) <= 0) return true;
            this.currentWorkingDirectory = vnodeInfo.asSlice(MacSystem.VNODE_PATH_INFO.byteOffset(MacSystem.PVI_CDIR)).asSlice(MacSystem.VNODE_INFO_PATH.byteOffset(MacSystem.VIP_PATH)).getString(0L);
            return true;
        }
        catch (Throwable e) {
            this.state = OSProcess.State.INVALID;
            return false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static {
        long ticksPerSec = 1000000000L;
        IOKit.IOIterator iter = IOKitUtilFFM.getMatchingServices("IOPlatformDevice");
        if (iter != null) {
            IOKit.IORegistryEntry cpu = iter.next();
            while (cpu != null) {
                try {
                    byte[] data;
                    String s = cpu.getName().toLowerCase(Locale.ROOT);
                    if (s.startsWith("cpu") && s.length() > 3 && (data = cpu.getByteArrayProperty("timebase-frequency")) != null) {
                        ticksPerSec = ParseUtil.byteArrayToLong(data, 4, false);
                        break;
                    }
                }
                finally {
                    cpu.release();
                }
                cpu = iter.next();
            }
            iter.release();
        }
        TICKS_PER_MS = ticksPerSec / 1000L;
        LOG_MAC_SYSCTL_WARNING = GlobalConfig.get("oshi.os.mac.sysctl.logwarning", false);
    }
}

