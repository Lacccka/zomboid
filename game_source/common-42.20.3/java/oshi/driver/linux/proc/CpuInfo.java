/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.linux.proc;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.FileUtil;
import oshi.util.ParseUtil;
import oshi.util.platform.linux.ProcPath;
import oshi.util.tuples.Quartet;

@ThreadSafe
public final class CpuInfo {
    private CpuInfo() {
    }

    public static String queryCpuManufacturer() {
        List<String> cpuInfo = FileUtil.readFile(ProcPath.CPUINFO);
        for (String line : cpuInfo) {
            if (!line.startsWith("CPU implementer")) continue;
            int part = ParseUtil.parseLastInt(line, 0);
            switch (part) {
                case 65: {
                    return "ARM";
                }
                case 66: {
                    return "Broadcom";
                }
                case 67: {
                    return "Cavium";
                }
                case 68: {
                    return "DEC";
                }
                case 78: {
                    return "Nvidia";
                }
                case 80: {
                    return "APM";
                }
                case 81: {
                    return "Qualcomm";
                }
                case 83: {
                    return "Samsung";
                }
                case 86: {
                    return "Marvell";
                }
                case 102: {
                    return "Faraday";
                }
                case 105: {
                    return "Intel";
                }
            }
            return null;
        }
        return null;
    }

    public static Quartet<String, String, String, String> queryBoardInfo() {
        String pcManufacturer = null;
        String pcModel = null;
        String pcVersion = null;
        String pcSerialNumber = null;
        List<String> cpuInfo = FileUtil.readFile(ProcPath.CPUINFO);
        for (String line : cpuInfo) {
            String[] splitLine = ParseUtil.whitespacesColonWhitespace.split(line);
            if (splitLine.length < 2) continue;
            switch (splitLine[0]) {
                case "Hardware": {
                    pcModel = splitLine[1];
                    break;
                }
                case "Revision": {
                    pcVersion = splitLine[1];
                    if (pcVersion.length() <= 1) break;
                    pcManufacturer = CpuInfo.queryBoardManufacturer(pcVersion.charAt(1));
                    break;
                }
                case "Serial": {
                    pcSerialNumber = splitLine[1];
                    break;
                }
            }
        }
        return new Quartet<Object, Object, Object, Object>(pcManufacturer, pcModel, pcVersion, pcSerialNumber);
    }

    private static String queryBoardManufacturer(char digit) {
        switch (digit) {
            case '0': {
                return "Sony UK";
            }
            case '1': {
                return "Egoman";
            }
            case '2': {
                return "Embest";
            }
            case '3': {
                return "Sony Japan";
            }
            case '4': {
                return "Embest";
            }
            case '5': {
                return "Stadium";
            }
        }
        return "unknown";
    }

    public static List<String> queryFeatureFlags() {
        return FileUtil.readFile(ProcPath.CPUINFO).stream().filter(f -> {
            String s = f.toLowerCase(Locale.ROOT);
            return s.startsWith("flags") || s.startsWith("features");
        }).distinct().collect(Collectors.toList());
    }
}

