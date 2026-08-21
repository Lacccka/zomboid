/*
 * Decompiled with CFR 0.152.
 */
package oshi.ffm.driver.windows.registry;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.ffm.windows.Advapi32FFM;
import oshi.ffm.windows.Win32Exception;
import oshi.ffm.windows.WindowsForeignFunctions;
import oshi.software.os.ApplicationInfo;
import oshi.util.ParseUtil;
import oshi.util.platform.windows.Advapi32UtilFFM;

public final class InstalledAppsDataFFM {
    private static final Logger LOG = LoggerFactory.getLogger(InstalledAppsDataFFM.class);
    private static final long THIRTY_YEARS_IN_SECS = 946080000L;
    private static final Map<MemorySegment, List<String>> REGISTRY_PATHS = new LinkedHashMap<MemorySegment, List<String>>();
    private static final int[] ACCESS_FLAGS = new int[]{256, 512};

    private InstalledAppsDataFFM() {
    }

    public static List<ApplicationInfo> queryInstalledApps() {
        LinkedHashSet<ApplicationInfo> appInfoSet = new LinkedHashSet<ApplicationInfo>();
        for (Map.Entry<MemorySegment, List<String>> entry : REGISTRY_PATHS.entrySet()) {
            MemorySegment rootKey = entry.getKey();
            List<String> uninstallPaths = entry.getValue();
            for (String registryPath : uninstallPaths) {
                for (int accessFlag : ACCESS_FLAGS) {
                    try {
                        String[] keys2;
                        for (String key : keys2 = Advapi32UtilFFM.registryGetKeys(rootKey, registryPath, accessFlag)) {
                            String fullPath = registryPath + "\\" + key;
                            try {
                                String name = InstalledAppsDataFFM.registryValueToString(InstalledAppsDataFFM.getRegistryValueOrNull(rootKey, fullPath, "DisplayName", accessFlag));
                                if (name == null) continue;
                                String version = InstalledAppsDataFFM.registryValueToString(InstalledAppsDataFFM.getRegistryValueOrNull(rootKey, fullPath, "DisplayVersion", accessFlag));
                                String publisher = InstalledAppsDataFFM.registryValueToString(InstalledAppsDataFFM.getRegistryValueOrNull(rootKey, fullPath, "Publisher", accessFlag));
                                long installDate = InstalledAppsDataFFM.registryValueToLong(InstalledAppsDataFFM.getRegistryValueOrNull(rootKey, fullPath, "InstallDate", accessFlag));
                                String installLocation = InstalledAppsDataFFM.registryValueToString(InstalledAppsDataFFM.getRegistryValueOrNull(rootKey, fullPath, "InstallLocation", accessFlag));
                                String installSource = InstalledAppsDataFFM.registryValueToString(InstalledAppsDataFFM.getRegistryValueOrNull(rootKey, fullPath, "InstallSource", accessFlag));
                                LinkedHashMap<String, String> additionalInfo = new LinkedHashMap<String, String>();
                                additionalInfo.put("installLocation", installLocation);
                                additionalInfo.put("installSource", installSource);
                                ApplicationInfo app = new ApplicationInfo(name, version, publisher, installDate, additionalInfo);
                                appInfoSet.add(app);
                            }
                            catch (Throwable e) {
                                LOG.trace("Skipping key {}: {}", (Object)fullPath, (Object)e.getMessage());
                            }
                        }
                    }
                    catch (Throwable e) {
                        LOG.trace("Skipping path {}: {}", (Object)registryPath, (Object)e.getMessage());
                    }
                }
            }
        }
        return new ArrayList<ApplicationInfo>(appInfoSet);
    }

    private static String registryValueToString(Object registryValueOrNull) {
        if (registryValueOrNull instanceof Integer) {
            Integer i = (Integer)registryValueOrNull;
            return Integer.toString(i);
        }
        return (String)registryValueOrNull;
    }

    private static long registryValueToLong(Object registryValueOrNull) {
        if (registryValueOrNull == null) {
            return 0L;
        }
        long currentTimeSecs = System.currentTimeMillis() / 1000L;
        long minSaneTimestamp = currentTimeSecs - 946080000L;
        if (registryValueOrNull instanceof Integer) {
            Integer i = (Integer)registryValueOrNull;
            if ((long)i.intValue() > minSaneTimestamp && (long)i.intValue() < currentTimeSecs) {
                return (long)i.intValue() * 1000L;
            }
            return i.intValue();
        }
        if (registryValueOrNull instanceof String) {
            String s = (String)registryValueOrNull;
            String dateStr = s.trim();
            long epoch = ParseUtil.parseDateToEpoch(dateStr, "yyyyMMdd");
            if (epoch == 0L) {
                epoch = ParseUtil.parseDateToEpoch(dateStr, "MM/dd/yyyy");
            }
            return epoch;
        }
        return 0L;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive exception aggregation
     */
    private static Object getRegistryValueOrNull(MemorySegment rootKey, String path, String key, int accessFlag) throws Throwable {
        try (Arena arena = Arena.ofConfined();){
            Object object;
            MemorySegment phkKey = arena.allocate(ValueLayout.ADDRESS);
            int rc = Advapi32FFM.RegOpenKeyEx(rootKey, WindowsForeignFunctions.toWideString(arena, path), 0, 0x20019 | accessFlag, phkKey);
            WindowsForeignFunctions.checkSuccess(rc, new int[0]);
            MemorySegment hKey = phkKey.get(ValueLayout.ADDRESS, 0L);
            try {
                object = Advapi32UtilFFM.registryGetValue(hKey, key);
            }
            catch (Throwable throwable) {
                rc = Advapi32FFM.RegCloseKey(hKey);
                WindowsForeignFunctions.checkSuccess(rc, new int[0]);
                throw throwable;
            }
            rc = Advapi32FFM.RegCloseKey(hKey);
            WindowsForeignFunctions.checkSuccess(rc, new int[0]);
            return object;
        }
        catch (Win32Exception e) {
            LOG.trace("Unable to access " + path + " with flag " + accessFlag + ": " + e.getMessage());
            return null;
        }
    }

    static {
        REGISTRY_PATHS.put(MemorySegment.ofAddress(0x80000002L), Arrays.asList("SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall", "SOFTWARE\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall"));
        REGISTRY_PATHS.put(MemorySegment.ofAddress(0x80000001L), Arrays.asList("SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall"));
    }
}

