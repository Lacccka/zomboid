/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.unix.openbsd;

import java.util.List;
import java.util.Map;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.software.common.AbstractOSThread;
import oshi.software.os.OSProcess;
import oshi.software.os.unix.openbsd.OpenBsdOSProcess;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;

@ThreadSafe
public class OpenBsdOSThread
extends AbstractOSThread {
    private int threadId;
    private String name = "";
    private OSProcess.State state = OSProcess.State.INVALID;
    private long minorFaults;
    private long majorFaults;
    private long startMemoryAddress;
    private long contextSwitches;
    private long kernelTime;
    private long userTime;
    private long startTime;
    private long upTime;
    private int priority;

    public OpenBsdOSThread(int processId, Map<OpenBsdOSProcess.PsThreadColumns, String> threadMap) {
        super(processId);
        this.updateAttributes(threadMap);
    }

    public OpenBsdOSThread(int processId, int threadId) {
        super(processId);
        this.threadId = threadId;
        this.updateAttributes();
    }

    @Override
    public int getThreadId() {
        return this.threadId;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public OSProcess.State getState() {
        return this.state;
    }

    @Override
    public long getStartMemoryAddress() {
        return this.startMemoryAddress;
    }

    @Override
    public long getContextSwitches() {
        return this.contextSwitches;
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
    public int getPriority() {
        return this.priority;
    }

    @Override
    public boolean updateAttributes() {
        String psCommand = "ps -aHwwxo " + OpenBsdOSProcess.PS_THREAD_COLUMNS + " -p " + this.getOwningProcessId();
        List<String> threadList = ExecutingCommand.runNative(psCommand);
        String tidStr = Integer.toString(this.threadId);
        for (String psOutput : threadList) {
            Map<OpenBsdOSProcess.PsThreadColumns, String> threadMap = ParseUtil.stringToEnumMap(OpenBsdOSProcess.PsThreadColumns.class, psOutput.trim(), ' ');
            if (!threadMap.containsKey((Object)OpenBsdOSProcess.PsThreadColumns.ARGS) || !tidStr.equals(threadMap.get((Object)OpenBsdOSProcess.PsThreadColumns.TID))) continue;
            return this.updateAttributes(threadMap);
        }
        this.state = OSProcess.State.INVALID;
        return false;
    }

    private boolean updateAttributes(Map<OpenBsdOSProcess.PsThreadColumns, String> threadMap) {
        this.threadId = ParseUtil.parseIntOrDefault(threadMap.get((Object)OpenBsdOSProcess.PsThreadColumns.TID), 0);
        switch (threadMap.get((Object)OpenBsdOSProcess.PsThreadColumns.STATE).charAt(0)) {
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
        long elapsedTime = ParseUtil.parseDHMSOrDefault(threadMap.get((Object)OpenBsdOSProcess.PsThreadColumns.ETIME), 0L);
        this.upTime = elapsedTime < 1L ? 1L : elapsedTime;
        long now = System.currentTimeMillis();
        this.startTime = now - this.upTime;
        this.kernelTime = 0L;
        this.userTime = ParseUtil.parseDHMSOrDefault(threadMap.get((Object)OpenBsdOSProcess.PsThreadColumns.CPUTIME), 0L);
        this.startMemoryAddress = 0L;
        long nonVoluntaryContextSwitches = ParseUtil.parseLongOrDefault(threadMap.get((Object)OpenBsdOSProcess.PsThreadColumns.NIVCSW), 0L);
        long voluntaryContextSwitches = ParseUtil.parseLongOrDefault(threadMap.get((Object)OpenBsdOSProcess.PsThreadColumns.NVCSW), 0L);
        this.contextSwitches = voluntaryContextSwitches + nonVoluntaryContextSwitches;
        this.majorFaults = ParseUtil.parseLongOrDefault(threadMap.get((Object)OpenBsdOSProcess.PsThreadColumns.MAJFLT), 0L);
        this.minorFaults = ParseUtil.parseLongOrDefault(threadMap.get((Object)OpenBsdOSProcess.PsThreadColumns.MINFLT), 0L);
        this.priority = ParseUtil.parseIntOrDefault(threadMap.get((Object)OpenBsdOSProcess.PsThreadColumns.PRI), 0);
        this.name = threadMap.get((Object)OpenBsdOSProcess.PsThreadColumns.ARGS);
        return true;
    }
}

