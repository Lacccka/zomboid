/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.linux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import oshi.software.os.ApplicationInfo;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;

public final class LinuxInstalledApps {
    private static final Pattern PIPE_PATTERN = Pattern.compile("\\|");
    private static final Map<String, String> PACKAGE_MANAGER_COMMANDS = LinuxInstalledApps.initializePackageManagerCommands();

    private LinuxInstalledApps() {
    }

    private static Map<String, String> initializePackageManagerCommands() {
        HashMap<String, String> commands = new HashMap<String, String>();
        if (LinuxInstalledApps.isPackageManagerAvailable("dpkg")) {
            commands.put("dpkg", "dpkg-query -W -f=${Package}|${Version}|${Architecture}|${Installed-Size}|${db-fsys:Last-Modified}|${Maintainer}|${Source}|${Homepage}\\n");
        } else if (LinuxInstalledApps.isPackageManagerAvailable("rpm")) {
            commands.put("rpm", "rpm -qa --queryformat %{NAME}|%{VERSION}-%{RELEASE}|%{ARCH}|%{SIZE}|%{INSTALLTIME}|%{PACKAGER}|%{SOURCERPM}|%{URL}\\n");
        }
        return commands;
    }

    public static List<ApplicationInfo> queryInstalledApps() {
        List<String> output = LinuxInstalledApps.fetchInstalledApps();
        return LinuxInstalledApps.parseLinuxAppInfo(output);
    }

    private static List<String> fetchInstalledApps() {
        if (PACKAGE_MANAGER_COMMANDS.isEmpty()) {
            return Collections.emptyList();
        }
        String command = PACKAGE_MANAGER_COMMANDS.values().iterator().next();
        return ExecutingCommand.runNative(command);
    }

    private static boolean isPackageManagerAvailable(String packageManager) {
        List<String> result = ExecutingCommand.runNative(packageManager + " --version");
        return !result.isEmpty();
    }

    private static List<ApplicationInfo> parseLinuxAppInfo(List<String> output) {
        LinkedHashSet<ApplicationInfo> appInfoSet = new LinkedHashSet<ApplicationInfo>();
        for (String line : output) {
            String[] parts = PIPE_PATTERN.split(line, -1);
            if (parts.length < 8) continue;
            LinkedHashMap<String, String> additionalInfo = new LinkedHashMap<String, String>();
            additionalInfo.put("architecture", ParseUtil.getStringValueOrUnknown(parts[2]));
            additionalInfo.put("installedSize", String.valueOf(ParseUtil.parseLongOrDefault(parts[3], 0L)));
            additionalInfo.put("source", ParseUtil.getStringValueOrUnknown(parts[6]));
            additionalInfo.put("homepage", ParseUtil.getStringValueOrUnknown(parts[7]));
            ApplicationInfo app = new ApplicationInfo(ParseUtil.getStringValueOrUnknown(parts[0]), ParseUtil.getStringValueOrUnknown(parts[1]), ParseUtil.getStringValueOrUnknown(parts[5]), ParseUtil.parseLongOrDefault(parts[4], 0L), additionalInfo);
            appInfoSet.add(app);
        }
        return new ArrayList<ApplicationInfo>(appInfoSet);
    }
}

