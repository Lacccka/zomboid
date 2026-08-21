/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.unix.aix;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;
import oshi.software.os.ApplicationInfo;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;

public final class AixInstalledApps {
    private static final Pattern COLON_PATTERN = Pattern.compile(":");

    private AixInstalledApps() {
    }

    public static List<ApplicationInfo> queryInstalledApps() {
        List<String> output = ExecutingCommand.runNative("lslpp -Lc");
        return AixInstalledApps.parseAixAppInfo(output);
    }

    private static List<ApplicationInfo> parseAixAppInfo(List<String> lines) {
        LinkedHashSet<ApplicationInfo> appInfoSet = new LinkedHashSet<ApplicationInfo>();
        String architecture = System.getProperty("os.arch");
        boolean isFirstLine = true;
        for (String line : lines) {
            if (isFirstLine) {
                isFirstLine = false;
                continue;
            }
            String[] parts = COLON_PATTERN.split(line, -1);
            String name = ParseUtil.getStringValueOrUnknown(parts[0]);
            if (name.equals("unknown")) continue;
            String version = ParseUtil.getStringValueOrUnknown(parts[2]);
            String vendor = "unknown";
            String buildDate = ParseUtil.getStringValueOrUnknown(parts[17]);
            long timestamp = 0L;
            if (!buildDate.equals("unknown")) {
                if (buildDate.matches("\\d{4}")) {
                    String isoWeekString = "20" + buildDate.substring(0, 2) + "-W" + buildDate.substring(2) + "-2";
                    timestamp = ParseUtil.parseDateToEpoch(isoWeekString, "YYYY-'W'ww-e");
                } else {
                    timestamp = ParseUtil.parseDateToEpoch(buildDate, "EEE MMM dd HH:mm:ss yyyy");
                }
            }
            String description = ParseUtil.getStringValueOrUnknown(parts[7].trim());
            String installPath = ParseUtil.getStringValueOrUnknown(parts[16].trim());
            LinkedHashMap<String, String> additionalInfo = new LinkedHashMap<String, String>();
            additionalInfo.put("architecture", architecture);
            additionalInfo.put("description", description);
            additionalInfo.put("installPath", installPath);
            ApplicationInfo app = new ApplicationInfo(name, version, vendor, timestamp, additionalInfo);
            appInfoSet.add(app);
        }
        return new ArrayList<ApplicationInfo>(appInfoSet);
    }
}

