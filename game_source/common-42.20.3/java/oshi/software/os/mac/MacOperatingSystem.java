/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.mac;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.software.common.AbstractOperatingSystem;
import oshi.software.os.ApplicationInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OSService;
import oshi.software.os.OSThread;
import oshi.software.os.OperatingSystem;
import oshi.software.os.mac.MacInstalledApps;
import oshi.software.os.mac.MacOSThread;
import oshi.software.os.mac.MacOperatingSystemJNA;
import oshi.util.ExecutingCommand;
import oshi.util.FileUtil;
import oshi.util.Memoizer;
import oshi.util.ParseUtil;
import oshi.util.Util;

@ThreadSafe
public abstract class MacOperatingSystem
extends AbstractOperatingSystem {
    private static final Logger LOG = LoggerFactory.getLogger(MacOperatingSystemJNA.class);
    public static final String MACOS_VERSIONS_PROPERTIES = "oshi.macos.versions.properties";
    private static final String SYSTEM_LIBRARY_LAUNCH_AGENTS = "/System/Library/LaunchAgents";
    private static final String SYSTEM_LIBRARY_LAUNCH_DAEMONS = "/System/Library/LaunchDaemons";
    protected final int maxProc;
    protected final String osXVersion;
    protected final int major;
    protected final int minor;
    private final Supplier<List<ApplicationInfo>> installedAppsSupplier = Memoizer.memoize(MacInstalledApps::queryInstalledApps, Memoizer.installedAppsExpiration());

    protected MacOperatingSystem(int maxproc) {
        String version = System.getProperty("os.version");
        int verMajor = ParseUtil.getFirstIntValue(version);
        int verMinor = ParseUtil.getNthIntValue(version, 2);
        if (verMajor == 10 && verMinor > 15) {
            String swVers = ExecutingCommand.getFirstAnswer("sw_vers -productVersion");
            if (!swVers.isEmpty()) {
                version = swVers;
            }
            verMajor = ParseUtil.getFirstIntValue(version);
            verMinor = ParseUtil.getNthIntValue(version, 2);
        }
        this.osXVersion = version;
        this.major = verMajor;
        this.minor = verMinor;
        this.maxProc = maxproc;
    }

    @Override
    public String queryManufacturer() {
        return "Apple";
    }

    protected String parseCodeName() {
        Properties verProps = FileUtil.readPropertiesFromFilename(MACOS_VERSIONS_PROPERTIES);
        String codeName = null;
        if (this.major > 10) {
            codeName = verProps.getProperty(Integer.toString(this.major));
        } else if (this.major == 10) {
            codeName = verProps.getProperty(this.major + "." + this.minor);
        }
        if (Util.isBlank(codeName)) {
            LOG.warn("Unable to parse version {}.{} to a codename.", (Object)this.major, (Object)this.minor);
        }
        return codeName;
    }

    @Override
    protected int queryBitness(int jvmBitness) {
        if (jvmBitness == 64 || this.major == 10 && this.minor > 6) {
            return 64;
        }
        return ParseUtil.parseIntOrDefault(ExecutingCommand.getFirstAnswer("getconf LONG_BIT"), 32);
    }

    @Override
    public List<OSProcess> queryChildProcesses(int parentPid) {
        List<OSProcess> allProcs = this.queryAllProcesses();
        Set<Integer> descendantPids = MacOperatingSystem.getChildrenOrDescendants(allProcs, parentPid, false);
        return allProcs.stream().filter(p -> descendantPids.contains(p.getProcessID())).collect(Collectors.toList());
    }

    @Override
    public List<OSProcess> queryDescendantProcesses(int parentPid) {
        List<OSProcess> allProcs = this.queryAllProcesses();
        Set<Integer> descendantPids = MacOperatingSystem.getChildrenOrDescendants(allProcs, parentPid, true);
        return allProcs.stream().filter(p -> descendantPids.contains(p.getProcessID())).collect(Collectors.toList());
    }

    @Override
    public int getThreadId() {
        OSThread thread2 = this.getCurrentThread();
        if (thread2 == null) {
            return 0;
        }
        return thread2.getThreadId();
    }

    @Override
    public OSThread getCurrentThread() {
        return this.getCurrentProcess().getThreadDetails().stream().sorted(Comparator.comparingLong(OSThread::getStartTime)).findFirst().orElse(new MacOSThread(this.getProcessId()));
    }

    @Override
    public long getSystemUptime() {
        return System.currentTimeMillis() / 1000L - this.getSystemBootTime();
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
        ArrayList<File> files = new ArrayList<File>();
        File dir = new File(SYSTEM_LIBRARY_LAUNCH_AGENTS);
        if (dir.exists() && dir.isDirectory()) {
            files.addAll(Arrays.asList(dir.listFiles((f, name) -> name.toLowerCase(Locale.ROOT).endsWith(".plist"))));
        } else {
            LOG.error("Directory: /System/Library/LaunchAgents does not exist");
        }
        dir = new File(SYSTEM_LIBRARY_LAUNCH_DAEMONS);
        if (dir.exists() && dir.isDirectory()) {
            files.addAll(Arrays.asList(dir.listFiles((f, name) -> name.toLowerCase(Locale.ROOT).endsWith(".plist"))));
        } else {
            LOG.error("Directory: /System/Library/LaunchDaemons does not exist");
        }
        for (File f2 : files) {
            String shortName;
            String name2 = f2.getName().substring(0, f2.getName().length() - 6);
            int index = name2.lastIndexOf(46);
            String string = shortName = index < 0 || index > name2.length() - 2 ? name2 : name2.substring(index + 1);
            if (running.contains(name2) || running.contains(shortName)) continue;
            OSService s = new OSService(name2, 0, OSService.State.STOPPED);
            services.add(s);
        }
        return services;
    }

    @Override
    public List<ApplicationInfo> getInstalledApplications() {
        return this.installedAppsSupplier.get();
    }
}

