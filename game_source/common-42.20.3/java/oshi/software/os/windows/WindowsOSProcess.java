/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.windows;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.COM.WbemcliUtil;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.Shell32Util;
import com.sun.jna.platform.win32.VersionHelpers;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinNT;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.windows.registry.ProcessPerformanceData;
import oshi.driver.windows.registry.ProcessWtsData;
import oshi.driver.windows.registry.ThreadPerformanceData;
import oshi.driver.windows.wmi.Win32Process;
import oshi.driver.windows.wmi.Win32ProcessCached;
import oshi.jna.ByRef;
import oshi.jna.platform.windows.NtDll;
import oshi.software.common.AbstractOSProcess;
import oshi.software.os.OSProcess;
import oshi.software.os.OSThread;
import oshi.software.os.windows.WindowsFileSystem;
import oshi.software.os.windows.WindowsOSThread;
import oshi.software.os.windows.WindowsOperatingSystem;
import oshi.util.GlobalConfig;
import oshi.util.Memoizer;
import oshi.util.ParseUtil;
import oshi.util.platform.windows.WmiUtil;
import oshi.util.tuples.Pair;
import oshi.util.tuples.Triplet;

@ThreadSafe
public class WindowsOSProcess
extends AbstractOSProcess {
    private static final Logger LOG = LoggerFactory.getLogger(WindowsOSProcess.class);
    private static final boolean USE_BATCH_COMMANDLINE = GlobalConfig.get("oshi.os.windows.commandline.batch", false);
    private static final boolean USE_PROCSTATE_SUSPENDED = GlobalConfig.get("oshi.os.windows.procstate.suspended", false);
    private static final boolean IS_VISTA_OR_GREATER = VersionHelpers.IsWindowsVistaOrGreater();
    private static final boolean IS_WINDOWS7_OR_GREATER = VersionHelpers.IsWindows7OrGreater();
    private final WindowsOperatingSystem os;
    private Supplier<Pair<String, String>> userInfo = Memoizer.memoize(this::queryUserInfo);
    private Supplier<Pair<String, String>> groupInfo = Memoizer.memoize(this::queryGroupInfo);
    private Supplier<String> currentWorkingDirectory = Memoizer.memoize(this::queryCwd);
    private Supplier<String> commandLine = Memoizer.memoize(this::queryCommandLine);
    private Supplier<List<String>> args = Memoizer.memoize(this::queryArguments);
    private Supplier<Triplet<String, String, Map<String, String>>> cwdCmdEnv = Memoizer.memoize(this::queryCwdCommandlineEnvironment);
    private Map<Integer, ThreadPerformanceData.PerfCounterBlock> tcb;
    private String name;
    private String path;
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
    private long pageFaults;

    public WindowsOSProcess(int pid, WindowsOperatingSystem os, Map<Integer, ProcessPerformanceData.PerfCounterBlock> processMap, Map<Integer, ProcessWtsData.WtsInfo> processWtsMap, Map<Integer, ThreadPerformanceData.PerfCounterBlock> threadMap) {
        super(pid);
        this.os = os;
        this.bitness = os.getBitness();
        this.tcb = threadMap;
        this.updateAttributes(processMap.get(pid), processWtsMap.get(pid));
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

    @Override
    public List<String> getArguments() {
        return this.args.get();
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
        return this.cwdCmdEnv.get().getC();
    }

    @Override
    public String getCurrentWorkingDirectory() {
        return this.currentWorkingDirectory.get();
    }

    @Override
    public String getUser() {
        return this.userInfo.get().getA();
    }

    @Override
    public String getUserID() {
        return this.userInfo.get().getB();
    }

    @Override
    public String getGroup() {
        return this.groupInfo.get().getA();
    }

    @Override
    public String getGroupID() {
        return this.groupInfo.get().getB();
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
        return this.openFiles;
    }

    @Override
    public long getSoftOpenFileLimit() {
        return WindowsFileSystem.MAX_WINDOWS_HANDLES;
    }

    @Override
    public long getHardOpenFileLimit() {
        return WindowsFileSystem.MAX_WINDOWS_HANDLES;
    }

    @Override
    public int getBitness() {
        return this.bitness;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public long getAffinityMask() {
        WinNT.HANDLE pHandle = Kernel32.INSTANCE.OpenProcess(1024, false, this.getProcessID());
        if (pHandle != null) {
            try (ByRef.CloseableULONGptrByReference processAffinity = new ByRef.CloseableULONGptrByReference();
                 ByRef.CloseableULONGptrByReference systemAffinity = new ByRef.CloseableULONGptrByReference();){
                if (Kernel32.INSTANCE.GetProcessAffinityMask(pHandle, processAffinity, systemAffinity)) {
                    long l = Pointer.nativeValue(processAffinity.getValue().toPointer());
                    return l;
                }
            }
            finally {
                Kernel32.INSTANCE.CloseHandle(pHandle);
            }
        }
        return 0L;
    }

    @Override
    public long getMinorFaults() {
        return this.pageFaults;
    }

    @Override
    public List<OSThread> getThreadDetails() {
        Map<Integer, ThreadPerformanceData.PerfCounterBlock> threads = this.tcb == null ? this.queryMatchingThreads(Collections.singleton(this.getProcessID())) : this.tcb;
        return ((Stream)threads.entrySet().stream().parallel()).filter(entry -> ((ThreadPerformanceData.PerfCounterBlock)entry.getValue()).getOwningProcessID() == this.getProcessID()).map(entry -> new WindowsOSThread(this.getProcessID(), (Integer)entry.getKey(), this.name, (ThreadPerformanceData.PerfCounterBlock)entry.getValue())).collect(Collectors.toList());
    }

    @Override
    public boolean updateAttributes() {
        Set<Integer> pids = Collections.singleton(this.getProcessID());
        Map<Integer, ProcessPerformanceData.PerfCounterBlock> pcb = ProcessPerformanceData.buildProcessMapFromRegistry(null);
        if (pcb == null) {
            pcb = ProcessPerformanceData.buildProcessMapFromPerfCounters(pids);
        }
        if (USE_PROCSTATE_SUSPENDED) {
            this.tcb = this.queryMatchingThreads(pids);
        }
        Map<Integer, ProcessWtsData.WtsInfo> wts = ProcessWtsData.queryProcessWtsMap(pids);
        return this.updateAttributes(pcb.get(this.getProcessID()), wts.get(this.getProcessID()));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean updateAttributes(ProcessPerformanceData.PerfCounterBlock pcb, ProcessWtsData.WtsInfo wts) {
        WinNT.HANDLE pHandle;
        this.name = pcb.getName();
        this.path = wts.getPath();
        this.parentProcessID = pcb.getParentProcessID();
        this.threadCount = wts.getThreadCount();
        this.priority = pcb.getPriority();
        this.virtualSize = wts.getVirtualSize();
        this.residentSetSize = pcb.getResidentSetSize();
        this.kernelTime = wts.getKernelTime();
        this.userTime = wts.getUserTime();
        this.startTime = pcb.getStartTime();
        this.upTime = pcb.getUpTime();
        this.bytesRead = pcb.getBytesRead();
        this.bytesWritten = pcb.getBytesWritten();
        this.openFiles = wts.getOpenFiles();
        this.pageFaults = pcb.getPageFaults();
        this.state = OSProcess.State.RUNNING;
        if (this.tcb != null) {
            int pid = this.getProcessID();
            for (ThreadPerformanceData.PerfCounterBlock tpd : this.tcb.values()) {
                if (tpd.getOwningProcessID() != pid) continue;
                if (tpd.getThreadWaitReason() == 5) {
                    this.state = OSProcess.State.SUSPENDED;
                    continue;
                }
                this.state = OSProcess.State.RUNNING;
                break;
            }
        }
        if ((pHandle = Kernel32.INSTANCE.OpenProcess(1024, false, this.getProcessID())) != null) {
            try {
                if (IS_VISTA_OR_GREATER && this.bitness == 64) {
                    try (ByRef.CloseableIntByReference wow64 = new ByRef.CloseableIntByReference();){
                        if (Kernel32.INSTANCE.IsWow64Process(pHandle, wow64) && wow64.getValue() > 0) {
                            this.bitness = 32;
                        }
                    }
                }
                try {
                    if (IS_WINDOWS7_OR_GREATER) {
                        this.path = Kernel32Util.QueryFullProcessImageName(pHandle, 0);
                    }
                }
                catch (Win32Exception e) {
                    this.state = OSProcess.State.INVALID;
                }
            }
            finally {
                Kernel32.INSTANCE.CloseHandle(pHandle);
            }
        }
        return !this.state.equals((Object)OSProcess.State.INVALID);
    }

    private Map<Integer, ThreadPerformanceData.PerfCounterBlock> queryMatchingThreads(Set<Integer> pids) {
        Map<Integer, ThreadPerformanceData.PerfCounterBlock> threads = ThreadPerformanceData.buildThreadMapFromRegistry(pids);
        if (threads == null) {
            threads = ThreadPerformanceData.buildThreadMapFromPerfCounters(pids, this.getName(), -1);
        }
        return threads;
    }

    private String queryCommandLine() {
        if (!this.cwdCmdEnv.get().getB().isEmpty()) {
            return this.cwdCmdEnv.get().getB();
        }
        if (USE_BATCH_COMMANDLINE) {
            return Win32ProcessCached.getInstance().getCommandLine(this.getProcessID(), this.getStartTime());
        }
        WbemcliUtil.WmiResult<Win32Process.CommandLineProperty> commandLineProcs = Win32Process.queryCommandLines(Collections.singleton(this.getProcessID()));
        if (commandLineProcs.getResultCount() > 0) {
            return WmiUtil.getString(commandLineProcs, Win32Process.CommandLineProperty.COMMANDLINE, 0);
        }
        return "";
    }

    private List<String> queryArguments() {
        String cl = this.getCommandLine();
        if (!cl.isEmpty()) {
            return Arrays.asList(Shell32Util.CommandLineToArgv(cl));
        }
        return Collections.emptyList();
    }

    private String queryCwd() {
        String cwd;
        if (!this.cwdCmdEnv.get().getA().isEmpty()) {
            return this.cwdCmdEnv.get().getA();
        }
        if (this.getProcessID() == this.os.getProcessId() && !(cwd = new File(".").getAbsolutePath()).isEmpty()) {
            return cwd.substring(0, cwd.length() - 1);
        }
        return "";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Pair<String, String> queryUserInfo() {
        Pair<String, String> pair = null;
        WinNT.HANDLE pHandle = Kernel32.INSTANCE.OpenProcess(1024, false, this.getProcessID());
        if (pHandle != null) {
            try (ByRef.CloseableHANDLEByReference phToken = new ByRef.CloseableHANDLEByReference();){
                try {
                    if (Advapi32.INSTANCE.OpenProcessToken(pHandle, 10, phToken)) {
                        Advapi32Util.Account account = Advapi32Util.getTokenAccount(phToken.getValue());
                        pair = new Pair<String, String>(account.name, account.sidString);
                    } else {
                        int error = Kernel32.INSTANCE.GetLastError();
                        if (error != 5) {
                            LOG.error("Failed to get process token for process {}: {}", (Object)this.getProcessID(), (Object)Kernel32.INSTANCE.GetLastError());
                        }
                    }
                }
                catch (Win32Exception e) {
                    LOG.warn("Failed to query user info for process {} ({}): {}", this.getProcessID(), this.getName(), e.getMessage());
                }
                finally {
                    WinNT.HANDLE token = phToken.getValue();
                    if (token != null) {
                        Kernel32.INSTANCE.CloseHandle(token);
                    }
                    Kernel32.INSTANCE.CloseHandle(pHandle);
                }
            }
        }
        if (pair == null) {
            return new Pair<String, String>("unknown", "unknown");
        }
        return pair;
    }

    private Pair<String, String> queryGroupInfo() {
        Pair<String, String> pair = null;
        WinNT.HANDLE pHandle = Kernel32.INSTANCE.OpenProcess(1024, false, this.getProcessID());
        if (pHandle != null) {
            try (ByRef.CloseableHANDLEByReference phToken = new ByRef.CloseableHANDLEByReference();){
                if (Advapi32.INSTANCE.OpenProcessToken(pHandle, 10, phToken)) {
                    Advapi32Util.Account account = Advapi32Util.getTokenPrimaryGroup(phToken.getValue());
                    pair = new Pair<String, String>(account.name, account.sidString);
                } else {
                    int error = Kernel32.INSTANCE.GetLastError();
                    if (error != 5) {
                        LOG.error("Failed to get process token for process {}: {}", (Object)this.getProcessID(), (Object)Kernel32.INSTANCE.GetLastError());
                    }
                }
                WinNT.HANDLE token = phToken.getValue();
                if (token != null) {
                    Kernel32.INSTANCE.CloseHandle(token);
                }
                Kernel32.INSTANCE.CloseHandle(pHandle);
            }
        }
        if (pair == null) {
            return new Pair<String, String>("unknown", "unknown");
        }
        return pair;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    private Triplet<String, String, Map<String, String>> queryCwdCommandlineEnvironment() {
        block20: {
            h = Kernel32.INSTANCE.OpenProcess(1040, false, this.getProcessID());
            if (h == null) break block20;
            try {
                block25: {
                    block24: {
                        block23: {
                            block22: {
                                block21: {
                                    if (WindowsOperatingSystem.isX86() != WindowsOperatingSystem.isWow(h)) break block20;
                                    nRead = new ByRef.CloseableIntByReference();
                                    try {
                                        pbi = new NtDll.PROCESS_BASIC_INFORMATION();
                                        ret = NtDll.INSTANCE.NtQueryInformationProcess(h, 0, pbi.getPointer(), pbi.size(), nRead);
                                        if (ret == 0) break block21;
                                        var5_7 = WindowsOSProcess.defaultCwdCommandlineEnvironment();
                                        nRead.close();
                                        return var5_7;
                                    }
                                    catch (Throwable var3_4) {
                                        try {
                                            nRead.close();
                                        }
                                        catch (Throwable var4_6) {
                                            var3_4.addSuppressed(var4_6);
                                        }
                                        throw var3_4;
                                    }
                                }
                                pbi.read();
                                peb = new NtDll.PEB();
                                Kernel32.INSTANCE.ReadProcessMemory(h, pbi.PebBaseAddress, peb.getPointer(), peb.size(), nRead);
                                if (nRead.getValue() != 0) break block22;
                                var6_9 = WindowsOSProcess.defaultCwdCommandlineEnvironment();
                                nRead.close();
                                return var6_9;
                            }
                            peb.read();
                            upp = new NtDll.RTL_USER_PROCESS_PARAMETERS();
                            Kernel32.INSTANCE.ReadProcessMemory(h, peb.ProcessParameters, upp.getPointer(), upp.size(), nRead);
                            if (nRead.getValue() != 0) break block23;
                            var7_11 = WindowsOSProcess.defaultCwdCommandlineEnvironment();
                            nRead.close();
                            return var7_11;
                        }
                        upp.read();
                        cwd = WindowsOSProcess.readUnicodeString(h, upp.CurrentDirectory.DosPath);
                        cl = WindowsOSProcess.readUnicodeString(h, upp.CommandLine);
                        envSize = upp.EnvironmentSize.intValue();
                        if (envSize <= 0) ** GOTO lbl74
                        buffer = new Memory(envSize);
                        Kernel32.INSTANCE.ReadProcessMemory(h, upp.Environment, buffer, envSize, nRead);
                        if (nRead.getValue() <= 0) break block24;
                        env = buffer.getCharArray(0L, envSize / 2);
                        envMap = ParseUtil.parseCharArrayToStringMap(env);
                        envMap.remove("");
                        var13_20 = new Triplet<String, String, Map<String, String>>(cwd, cl, Collections.unmodifiableMap(envMap));
                        buffer.close();
                        nRead.close();
                        return var13_20;
                    }
                    buffer.close();
                    break block25;
                    {
                        catch (Throwable var11_17) {
                            try {
                                buffer.close();
                            }
                            catch (Throwable var12_19) {
                                var11_17.addSuppressed(var12_19);
                            }
                            throw var11_17;
                        }
                    }
                }
                var10_15 = new Triplet<String, String, Map<String, String>>(cwd, cl, Collections.emptyMap());
                nRead.close();
                return var10_15;
            }
            finally {
                Kernel32.INSTANCE.CloseHandle(h);
            }
        }
        return WindowsOSProcess.defaultCwdCommandlineEnvironment();
    }

    private static Triplet<String, String, Map<String, String>> defaultCwdCommandlineEnvironment() {
        return new Triplet<String, String, Map<String, String>>("", "", Collections.emptyMap());
    }

    private static String readUnicodeString(WinNT.HANDLE h, NtDll.UNICODE_STRING s) {
        if (s.Length > 0) {
            try (Memory m = new Memory((long)s.Length + 2L);
                 ByRef.CloseableIntByReference nRead = new ByRef.CloseableIntByReference();){
                m.clear();
                Kernel32.INSTANCE.ReadProcessMemory(h, s.Buffer, m, s.Length, nRead);
                if (nRead.getValue() > 0) {
                    String string = m.getWideString(0L);
                    return string;
                }
            }
        }
        return "";
    }
}

