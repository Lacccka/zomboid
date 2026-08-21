/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.unix.aix;

import com.sun.jna.Native;
import com.sun.jna.platform.unix.Resource;
import com.sun.jna.platform.unix.aix.Perfstat;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.unix.aix.PsInfo;
import oshi.driver.unix.aix.perfstat.PerfstatCpu;
import oshi.jna.platform.unix.AixLibc;
import oshi.software.common.AbstractOSProcess;
import oshi.software.os.OSProcess;
import oshi.software.os.OSThread;
import oshi.software.os.unix.aix.AixOSThread;
import oshi.software.os.unix.aix.AixOperatingSystem;
import oshi.util.Constants;
import oshi.util.ExecutingCommand;
import oshi.util.Memoizer;
import oshi.util.ParseUtil;
import oshi.util.UserGroupInfo;
import oshi.util.tuples.Pair;

@ThreadSafe
public class AixOSProcess
extends AbstractOSProcess {
    private static final Logger LOG = LoggerFactory.getLogger(AixOSProcess.class);
    private Supplier<Integer> bitness = Memoizer.memoize(this::queryBitness);
    private Supplier<AixLibc.AixPsInfo> psinfo = Memoizer.memoize(this::queryPsInfo, Memoizer.defaultExpiration());
    private Supplier<String> commandLine = Memoizer.memoize(this::queryCommandLine);
    private Supplier<Pair<List<String>, Map<String, String>>> cmdEnv = Memoizer.memoize(this::queryCommandlineEnvironment);
    private final Supplier<Long> affinityMask = Memoizer.memoize(PerfstatCpu::queryCpuAffinityMask, Memoizer.defaultExpiration());
    private String name;
    private String path = "";
    private String commandLineBackup;
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
    private Supplier<Perfstat.perfstat_process_t[]> procCpu;
    private final AixOperatingSystem os;

    public AixOSProcess(int pid, Pair<Long, Long> userSysCpuTime, Supplier<Perfstat.perfstat_process_t[]> procCpu, AixOperatingSystem os) {
        super(pid);
        this.procCpu = procCpu;
        this.os = os;
        this.updateAttributes(userSysCpuTime);
    }

    private AixLibc.AixPsInfo queryPsInfo() {
        return PsInfo.queryPsInfo(this.getProcessID());
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
        return this.cmdEnv.get().getA();
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
        return this.cmdEnv.get().getB();
    }

    private Pair<List<String>, Map<String, String>> queryCommandlineEnvironment() {
        return PsInfo.queryArgsEnv(this.getProcessID(), this.psinfo.get());
    }

    @Override
    public String getCurrentWorkingDirectory() {
        try {
            String cwdLink = "/proc" + this.getProcessID() + "/cwd";
            String cwd = new File(cwdLink).getCanonicalPath();
            if (!cwd.equals(cwdLink)) {
                return cwd;
            }
        }
        catch (IOException e) {
            LOG.trace("Couldn't find cwd for pid {}: {}", (Object)this.getProcessID(), (Object)e.getMessage());
        }
        return "";
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
        long l;
        block8: {
            Stream<Path> fd = Files.list(Paths.get("/proc/" + this.getProcessID() + "/fd", new String[0]));
            try {
                l = fd.count();
                if (fd == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (fd != null) {
                        try {
                            fd.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    return 0L;
                }
            }
            fd.close();
        }
        return l;
    }

    @Override
    public long getSoftOpenFileLimit() {
        if (this.getProcessID() == this.os.getProcessId()) {
            Resource.Rlimit rlimit = new Resource.Rlimit();
            AixLibc.INSTANCE.getrlimit(7, rlimit);
            return rlimit.rlim_cur;
        }
        return -1L;
    }

    @Override
    public long getHardOpenFileLimit() {
        if (this.getProcessID() == this.os.getProcessId()) {
            Resource.Rlimit rlimit = new Resource.Rlimit();
            AixLibc.INSTANCE.getrlimit(7, rlimit);
            return rlimit.rlim_max;
        }
        return -1L;
    }

    @Override
    public int getBitness() {
        return this.bitness.get();
    }

    private int queryBitness() {
        List<String> pflags = ExecutingCommand.runNative("pflags " + this.getProcessID());
        for (String line : pflags) {
            if (!line.contains("data model")) continue;
            if (line.contains("LP32")) {
                return 32;
            }
            if (!line.contains("LP64")) continue;
            return 64;
        }
        return 0;
    }

    @Override
    public long getAffinityMask() {
        long mask = 0L;
        File directory = new File(String.format(Locale.ROOT, "/proc/%d/lwp", this.getProcessID()));
        File[] numericFiles = directory.listFiles(file -> Constants.DIGITS.matcher(file.getName()).matches());
        if (numericFiles == null) {
            return mask;
        }
        for (File lwpidFile : numericFiles) {
            int lwpidNum = ParseUtil.parseIntOrDefault(lwpidFile.getName(), 0);
            AixLibc.AixLwpsInfo info = PsInfo.queryLwpsInfo(this.getProcessID(), lwpidNum);
            if (info == null) continue;
            mask |= (long)info.pr_bindpro;
        }
        return mask &= this.affinityMask.get().longValue();
    }

    @Override
    public List<OSThread> getThreadDetails() {
        File directory = new File(String.format(Locale.ROOT, "/proc/%d/lwp", this.getProcessID()));
        File[] numericFiles = directory.listFiles(file -> Constants.DIGITS.matcher(file.getName()).matches());
        if (numericFiles == null) {
            return Collections.emptyList();
        }
        return ((Stream)Arrays.stream(numericFiles).parallel()).map(lwpidFile -> new AixOSThread(this.getProcessID(), ParseUtil.parseIntOrDefault(lwpidFile.getName(), 0))).filter(OSThread.ThreadFiltering.VALID_THREAD).collect(Collectors.toList());
    }

    @Override
    public boolean updateAttributes() {
        Perfstat.perfstat_process_t[] perfstat;
        for (Perfstat.perfstat_process_t stat : perfstat = this.procCpu.get()) {
            int statpid = (int)stat.pid;
            if (statpid != this.getProcessID()) continue;
            return this.updateAttributes(new Pair<Long, Long>((long)stat.ucpu_time, (long)stat.scpu_time));
        }
        this.state = OSProcess.State.INVALID;
        return false;
    }

    private boolean updateAttributes(Pair<Long, Long> userSysCpuTime) {
        AixLibc.AixPsInfo info = this.psinfo.get();
        if (info == null) {
            this.state = OSProcess.State.INVALID;
            return false;
        }
        long now = System.currentTimeMillis();
        this.state = AixOSProcess.getStateFromOutput((char)info.pr_lwp.pr_sname);
        this.parentProcessID = (int)info.pr_ppid;
        this.userID = Long.toString(info.pr_euid);
        this.user = UserGroupInfo.getUser(this.userID);
        this.groupID = Long.toString(info.pr_egid);
        this.group = UserGroupInfo.getGroupName(this.groupID);
        this.threadCount = info.pr_nlwp;
        this.priority = info.pr_lwp.pr_pri;
        this.virtualSize = info.pr_size * 1024L;
        this.residentSetSize = info.pr_rssize * 1024L;
        this.startTime = info.pr_start.tv_sec * 1000L + (long)info.pr_start.tv_nsec / 1000000L;
        long elapsedTime = now - this.startTime;
        this.upTime = elapsedTime < 1L ? 1L : elapsedTime;
        this.userTime = userSysCpuTime.getA();
        this.kernelTime = userSysCpuTime.getB();
        this.commandLineBackup = Native.toString(info.pr_psargs);
        this.path = ParseUtil.whitespaces.split(this.commandLineBackup)[0];
        this.name = this.path.substring(this.path.lastIndexOf(47) + 1);
        if (this.name.isEmpty()) {
            this.name = Native.toString(info.pr_fname);
        }
        return true;
    }

    static OSProcess.State getStateFromOutput(char stateValue) {
        return switch (stateValue) {
            case 'O' -> OSProcess.State.INVALID;
            case 'A', 'R' -> OSProcess.State.RUNNING;
            case 'I' -> OSProcess.State.WAITING;
            case 'S', 'W' -> OSProcess.State.SLEEPING;
            case 'Z' -> OSProcess.State.ZOMBIE;
            case 'T' -> OSProcess.State.STOPPED;
            default -> OSProcess.State.OTHER;
        };
    }
}

