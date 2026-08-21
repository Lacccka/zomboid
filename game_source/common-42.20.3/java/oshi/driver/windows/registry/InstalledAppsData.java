/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.windows.registry;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import oshi.software.os.ApplicationInfo;
import oshi.util.platform.windows.RegistryUtil;

public final class InstalledAppsData {
    private static final Map<WinReg.HKEY, List<String>> REGISTRY_PATHS = new HashMap<WinReg.HKEY, List<String>>();
    private static final int[] ACCESS_FLAGS = new int[]{256, 512};

    private InstalledAppsData() {
    }

    public static List<ApplicationInfo> queryInstalledApps() {
        LinkedHashSet<ApplicationInfo> appInfoSet = new LinkedHashSet<ApplicationInfo>();
        for (Map.Entry<WinReg.HKEY, List<String>> entry : REGISTRY_PATHS.entrySet()) {
            WinReg.HKEY rootKey = entry.getKey();
            List<String> uninstallPaths = entry.getValue();
            for (String registryPath : uninstallPaths) {
                for (int accessFlag : ACCESS_FLAGS) {
                    try {
                        String[] keys2;
                        for (String key : keys2 = Advapi32Util.registryGetKeys(rootKey, registryPath, accessFlag)) {
                            String fullPath = registryPath + "\\" + key;
                            try {
                                String name = RegistryUtil.getStringValue(rootKey, fullPath, "DisplayName", accessFlag);
                                if (name == null) continue;
                                String version = RegistryUtil.getStringValue(rootKey, fullPath, "DisplayVersion", accessFlag);
                                String publisher = RegistryUtil.getStringValue(rootKey, fullPath, "Publisher", accessFlag);
                                long installDate = RegistryUtil.getLongValue(rootKey, fullPath, "InstallDate", accessFlag);
                                String installLocation = RegistryUtil.getStringValue(rootKey, fullPath, "InstallLocation", accessFlag);
                                String installSource = RegistryUtil.getStringValue(rootKey, fullPath, "InstallSource", accessFlag);
                                LinkedHashMap<String, String> additionalInfo = new LinkedHashMap<String, String>();
                                additionalInfo.put("installLocation", installLocation);
                                additionalInfo.put("installSource", installSource);
                                ApplicationInfo app = new ApplicationInfo(name, version, publisher, installDate, additionalInfo);
                                appInfoSet.add(app);
                            }
                            catch (Win32Exception win32Exception) {
                                // empty catch block
                            }
                        }
                    }
                    catch (Win32Exception win32Exception) {
                        // empty catch block
                    }
                }
            }
        }
        return new ArrayList<ApplicationInfo>(appInfoSet);
    }

    static {
        REGISTRY_PATHS.put(WinReg.HKEY_LOCAL_MACHINE, Arrays.asList("SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall", "SOFTWARE\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall"));
        REGISTRY_PATHS.put(WinReg.HKEY_CURRENT_USER, Arrays.asList("SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall"));
    }
}

