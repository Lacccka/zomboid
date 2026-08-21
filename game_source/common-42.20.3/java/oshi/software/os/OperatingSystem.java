/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import oshi.annotation.concurrent.Immutable;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.unix.Who;
import oshi.driver.unix.Xwininfo;
import oshi.software.os.ApplicationInfo;
import oshi.software.os.FileSystem;
import oshi.software.os.InternetProtocolStats;
import oshi.software.os.NetworkParams;
import oshi.software.os.OSDesktopWindow;
import oshi.software.os.OSProcess;
import oshi.software.os.OSService;
import oshi.software.os.OSSession;
import oshi.software.os.OSThread;
import oshi.util.UserGroupInfo;
import oshi.util.Util;

@ThreadSafe
public interface OperatingSystem {
    public String getFamily();

    public String getManufacturer();

    public OSVersionInfo getVersionInfo();

    public FileSystem getFileSystem();

    public InternetProtocolStats getInternetProtocolStats();

    default public List<OSProcess> getProcesses() {
        return this.getProcesses(null, null, 0);
    }

    public List<OSProcess> getProcesses(Predicate<OSProcess> var1, Comparator<OSProcess> var2, int var3);

    default public List<OSProcess> getProcesses(Collection<Integer> pids) {
        return pids.stream().map(this::getProcess).filter(Objects::nonNull).filter(ProcessFiltering.VALID_PROCESS).collect(Collectors.toList());
    }

    public OSProcess getProcess(int var1);

    public List<OSProcess> getChildProcesses(int var1, Predicate<OSProcess> var2, Comparator<OSProcess> var3, int var4);

    public List<OSProcess> getDescendantProcesses(int var1, Predicate<OSProcess> var2, Comparator<OSProcess> var3, int var4);

    public int getProcessId();

    default public OSProcess getCurrentProcess() {
        return this.getProcess(this.getProcessId());
    }

    public int getProcessCount();

    public int getThreadId();

    public OSThread getCurrentThread();

    public int getThreadCount();

    public int getBitness();

    public long getSystemUptime();

    public long getSystemBootTime();

    default public boolean isElevated() {
        return UserGroupInfo.isElevated();
    }

    public NetworkParams getNetworkParams();

    default public List<OSService> getServices() {
        return new ArrayList<OSService>();
    }

    default public List<OSSession> getSessions() {
        return Who.queryWho();
    }

    default public List<OSDesktopWindow> getDesktopWindows(boolean visibleOnly) {
        return Xwininfo.queryXWindows(visibleOnly);
    }

    default public List<ApplicationInfo> getInstalledApplications() {
        return Collections.emptyList();
    }

    public static final class ProcessFiltering {
        public static final Predicate<OSProcess> ALL_PROCESSES = p -> true;
        public static final Predicate<OSProcess> VALID_PROCESS = p -> !p.getState().equals((Object)OSProcess.State.INVALID);
        public static final Predicate<OSProcess> NO_PARENT = p -> p.getParentProcessID() == p.getProcessID();
        public static final Predicate<OSProcess> BITNESS_64 = p -> p.getBitness() == 64;
        public static final Predicate<OSProcess> BITNESS_32 = p -> p.getBitness() == 32;

        private ProcessFiltering() {
        }
    }

    @Immutable
    public static class OSVersionInfo {
        private final String version;
        private final String codeName;
        private final String buildNumber;
        private final String versionStr;

        public OSVersionInfo(String version, String codeName, String buildNumber) {
            this.version = version;
            this.codeName = codeName;
            this.buildNumber = buildNumber;
            StringBuilder sb = new StringBuilder(this.getVersion() != null ? this.getVersion() : "unknown");
            if (!Util.isBlank(this.getCodeName())) {
                sb.append(" (").append(this.getCodeName()).append(')');
            }
            if (!Util.isBlank(this.getBuildNumber())) {
                sb.append(" build ").append(this.getBuildNumber());
            }
            this.versionStr = sb.toString();
        }

        public String getVersion() {
            return this.version;
        }

        public String getCodeName() {
            return this.codeName;
        }

        public String getBuildNumber() {
            return this.buildNumber;
        }

        public String toString() {
            return this.versionStr;
        }
    }

    public static final class ProcessSorting {
        public static final Comparator<OSProcess> NO_SORTING = (p1, p2) -> 0;
        public static final Comparator<OSProcess> CPU_DESC = Comparator.comparingDouble(OSProcess::getProcessCpuLoadCumulative).reversed();
        public static final Comparator<OSProcess> RSS_DESC = Comparator.comparingLong(OSProcess::getResidentSetSize).reversed();
        public static final Comparator<OSProcess> UPTIME_ASC = Comparator.comparingLong(OSProcess::getUpTime);
        public static final Comparator<OSProcess> UPTIME_DESC = UPTIME_ASC.reversed();
        public static final Comparator<OSProcess> PID_ASC = Comparator.comparingInt(OSProcess::getProcessID);
        public static final Comparator<OSProcess> PARENTPID_ASC = Comparator.comparingInt(OSProcess::getParentProcessID);
        public static final Comparator<OSProcess> NAME_ASC = Comparator.comparing(OSProcess::getName, String.CASE_INSENSITIVE_ORDER);

        private ProcessSorting() {
        }
    }
}

