/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.linux;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.common.AbstractSensors;
import oshi.util.ExecutingCommand;
import oshi.util.FileUtil;
import oshi.util.GlobalConfig;
import oshi.util.ParseUtil;
import oshi.util.platform.linux.SysPath;

@ThreadSafe
final class LinuxSensors
extends AbstractSensors {
    public static final String OSHI_HWMON_NAME_PRIORITY = "oshi.os.linux.sensors.hwmon.names";
    public static final String OSHI_THERMAL_ZONE_TYPE_PRIORITY = "oshi.os.linux.sensors.cpuTemperature.types";
    private static final List<String> HWMON_NAME_PRIORITY = Stream.of(GlobalConfig.get("oshi.os.linux.sensors.hwmon.names", "coretemp,k10temp,zenpower,k8temp,via-cputemp,acpitz").split(",")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    private static final List<String> THERMAL_ZONE_TYPE_PRIORITY = Stream.of(GlobalConfig.get("oshi.os.linux.sensors.cpuTemperature.types", "cpu-thermal,x86_pkg_temp").split(",")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    private static final String TYPE = "type";
    private static final String NAME = "/name";
    private static final String TEMP = "temp";
    private static final String FAN = "fan";
    private static final String VOLTAGE = "in";
    private static final String INPUT_SUFFIX = "_input";
    private static final Pattern TEMP_INPUT_PATTERN = Pattern.compile("^temp\\d+_input$");
    private static final String HWMON = "hwmon";
    private static final String HWMON_PATH = SysPath.HWMON + "hwmon";
    private static final String THERMAL_ZONE = "thermal_zone";
    private static final String THERMAL_ZONE_PATH = SysPath.THERMAL + "thermal_zone";
    private static final boolean IS_PI = LinuxSensors.queryCpuTemperatureFromVcGenCmd() > 0.0;
    private final Map<String, String> sensorsMap = new HashMap<String, String>();

    LinuxSensors() {
        if (!IS_PI) {
            this.populateSensorsMapFromHwmon();
            if (!this.sensorsMap.containsKey(TEMP)) {
                this.populateSensorsMapFromThermalZone();
            }
        }
    }

    private void populateSensorsMapFromHwmon() {
        String selectedTempPath = null;
        int selectedPriority = Integer.MAX_VALUE;
        int i = 0;
        while (Paths.get(HWMON_PATH + i, new String[0]).toFile().isDirectory()) {
            int n;
            int priority;
            String path = HWMON_PATH + i;
            String sensorName = FileUtil.getStringFromFile(path + NAME).trim();
            File dir = new File(path);
            File[] tempInputs = dir.listFiles((d, name) -> TEMP_INPUT_PATTERN.matcher(name).matches());
            if (tempInputs != null && tempInputs.length > 0 && (priority = HWMON_NAME_PRIORITY.indexOf(sensorName)) >= 0 && priority < selectedPriority) {
                File[] fileArray = tempInputs;
                n = fileArray.length;
                for (int j = 0; j < n; ++j) {
                    File tempInput = fileArray[j];
                    long temp = FileUtil.getLongFromFile(tempInput.getPath());
                    if (temp <= 0L) continue;
                    selectedPriority = priority;
                    selectedTempPath = path;
                    break;
                }
            }
            String[] stringArray = new String[]{FAN, VOLTAGE};
            int n2 = stringArray.length;
            for (n = 0; n < n2; ++n) {
                String sensor;
                String sensorPrefix = sensor = stringArray[n];
                this.getSensorFilesFromPath(path, sensor, f -> {
                    try {
                        return f.getName().startsWith(sensorPrefix) && f.getName().endsWith(INPUT_SUFFIX) && FileUtil.getIntFromFile(f.getCanonicalPath()) > 0;
                    }
                    catch (IOException e) {
                        return false;
                    }
                });
            }
            ++i;
        }
        if (selectedTempPath != null) {
            this.sensorsMap.put(TEMP, selectedTempPath + "/temp");
        }
    }

    private void populateSensorsMapFromThermalZone() {
        this.getSensorFilesFromPath(THERMAL_ZONE_PATH, TEMP, f -> f.getName().equals(TYPE) || f.getName().equals(TEMP), files -> Stream.of(files).filter(f -> TYPE.equals(f.getName())).findFirst().map(File::getPath).map(FileUtil::getStringFromFile).map(THERMAL_ZONE_TYPE_PRIORITY::indexOf).filter(index -> index >= 0).orElse(THERMAL_ZONE_TYPE_PRIORITY.size()));
    }

    private void getSensorFilesFromPath(String sensorPath, String sensor, FileFilter sensorFileFilter) {
        this.getSensorFilesFromPath(sensorPath, sensor, sensorFileFilter, files -> 0);
    }

    private void getSensorFilesFromPath(String sensorPath, String sensor, FileFilter sensorFileFilter, ToIntFunction<File[]> prioritizer) {
        String selectedPath = null;
        int selectedPriority = Integer.MAX_VALUE;
        int i = 0;
        while (Paths.get(sensorPath + i, new String[0]).toFile().isDirectory()) {
            int priority;
            String path = sensorPath + i;
            File dir = new File(path);
            File[] matchingFiles = dir.listFiles(sensorFileFilter);
            if (matchingFiles != null && matchingFiles.length > 0 && (priority = prioritizer.applyAsInt(matchingFiles)) < selectedPriority) {
                selectedPriority = priority;
                selectedPath = path;
            }
            ++i;
        }
        if (selectedPath != null) {
            this.sensorsMap.put(sensor, String.format(Locale.ROOT, "%s/%s", selectedPath, sensor));
        }
    }

    @Override
    public double queryCpuTemperature() {
        if (IS_PI) {
            return LinuxSensors.queryCpuTemperatureFromVcGenCmd();
        }
        String tempStr = this.sensorsMap.get(TEMP);
        if (tempStr != null) {
            long millidegrees = 0L;
            if (tempStr.contains(HWMON)) {
                millidegrees = FileUtil.getLongFromFile(String.format(Locale.ROOT, "%s1%s", tempStr, INPUT_SUFFIX));
                if (millidegrees > 0L) {
                    return (double)millidegrees / 1000.0;
                }
                long sum = 0L;
                int count = 0;
                for (int i = 2; i <= 6; ++i) {
                    millidegrees = FileUtil.getLongFromFile(String.format(Locale.ROOT, "%s%d%s", tempStr, i, INPUT_SUFFIX));
                    if (millidegrees <= 0L) continue;
                    sum += millidegrees;
                    ++count;
                }
                if (count > 0) {
                    return (double)sum / ((double)count * 1000.0);
                }
            } else if (tempStr.contains(THERMAL_ZONE) && (millidegrees = FileUtil.getLongFromFile(tempStr)) > 0L) {
                return (double)millidegrees / 1000.0;
            }
        }
        return 0.0;
    }

    private static double queryCpuTemperatureFromVcGenCmd() {
        String tempStr = ExecutingCommand.getFirstAnswer("vcgencmd measure_temp");
        if (tempStr.startsWith("temp=")) {
            return ParseUtil.parseDoubleOrDefault(tempStr.replaceAll("[^\\d|\\.]+", ""), 0.0);
        }
        return 0.0;
    }

    @Override
    public int[] queryFanSpeeds() {
        String fanStr;
        if (!IS_PI && (fanStr = this.sensorsMap.get(FAN)) != null) {
            String fanPath;
            ArrayList<Integer> speeds = new ArrayList<Integer>();
            int fan = 1;
            while (new File(fanPath = String.format(Locale.ROOT, "%s%d%s", fanStr, fan, INPUT_SUFFIX)).exists()) {
                speeds.add(FileUtil.getIntFromFile(fanPath));
                ++fan;
            }
            int[] fanSpeeds = new int[speeds.size()];
            for (int i = 0; i < speeds.size(); ++i) {
                fanSpeeds[i] = (Integer)speeds.get(i);
            }
            return fanSpeeds;
        }
        return new int[0];
    }

    @Override
    public double queryCpuVoltage() {
        if (IS_PI) {
            return LinuxSensors.queryCpuVoltageFromVcGenCmd();
        }
        String voltageStr = this.sensorsMap.get(VOLTAGE);
        if (voltageStr != null) {
            return (double)FileUtil.getIntFromFile(String.format(Locale.ROOT, "%s1%s", voltageStr, INPUT_SUFFIX)) / 1000.0;
        }
        return 0.0;
    }

    private static double queryCpuVoltageFromVcGenCmd() {
        String voltageStr = ExecutingCommand.getFirstAnswer("vcgencmd measure_volts core");
        if (voltageStr.startsWith("volt=")) {
            return ParseUtil.parseDoubleOrDefault(voltageStr.replaceAll("[^\\d|\\.]+", ""), 0.0);
        }
        return 0.0;
    }
}

