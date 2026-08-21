/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os;

import java.util.List;
import java.util.Map;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.software.os.OSThread;

@ThreadSafe
public interface OSProcess {
    public String getName();

    public String getPath();

    public String getCommandLine();

    public List<String> getArguments();

    public Map<String, String> getEnvironmentVariables();

    public String getCurrentWorkingDirectory();

    public String getUser();

    public String getUserID();

    public String getGroup();

    public String getGroupID();

    public State getState();

    public int getProcessID();

    public int getParentProcessID();

    public int getThreadCount();

    public int getPriority();

    public long getVirtualSize();

    public long getResidentSetSize();

    public long getKernelTime();

    public long getUserTime();

    public long getUpTime();

    public long getStartTime();

    public long getBytesRead();

    public long getBytesWritten();

    public long getOpenFiles();

    public long getSoftOpenFileLimit();

    public long getHardOpenFileLimit();

    public double getProcessCpuLoadCumulative();

    public double getProcessCpuLoadBetweenTicks(OSProcess var1);

    public int getBitness();

    public long getAffinityMask();

    public boolean updateAttributes();

    public List<OSThread> getThreadDetails();

    default public long getMinorFaults() {
        return 0L;
    }

    default public long getMajorFaults() {
        return 0L;
    }

    default public long getContextSwitches() {
        return 0L;
    }

    public static enum State {
        NEW,
        RUNNING,
        SLEEPING,
        WAITING,
        ZOMBIE,
        STOPPED,
        OTHER,
        INVALID,
        SUSPENDED;

    }
}

