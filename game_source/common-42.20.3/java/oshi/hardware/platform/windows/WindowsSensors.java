/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.windows;

import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.WbemcliUtil;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.windows.wmi.MSAcpiThermalZoneTemperature;
import oshi.driver.windows.wmi.OhmHardware;
import oshi.driver.windows.wmi.OhmSensor;
import oshi.driver.windows.wmi.Win32Fan;
import oshi.driver.windows.wmi.Win32Processor;
import oshi.hardware.common.AbstractSensors;
import oshi.util.platform.windows.WmiQueryHandler;
import oshi.util.platform.windows.WmiUtil;

@ThreadSafe
final class WindowsSensors
extends AbstractSensors {
    private static final Logger LOG = LoggerFactory.getLogger(WindowsSensors.class);
    private static final String COM_EXCEPTION_MSG = "COM exception: {}";
    private static final String REFLECT_EXCEPTION_MSG = "Reflect exception: {}";
    private static final String JLIBREHARDWAREMONITOR_PACKAGE = "io.github.pandalxb.jlibrehardwaremonitor";

    WindowsSensors() {
    }

    @Override
    public double queryCpuTemperature() {
        double tempC = WindowsSensors.getTempFromOHM();
        if (tempC > 0.0) {
            return tempC;
        }
        tempC = WindowsSensors.getTempFromLHM();
        if (tempC > 0.0) {
            return tempC;
        }
        tempC = WindowsSensors.getTempFromWMI();
        return tempC;
    }

    private static double getTempFromOHM() {
        WbemcliUtil.WmiResult<OhmSensor.ValueProperty> ohmSensors = WindowsSensors.getOhmSensors("Hardware", "CPU", "Temperature", (h, ohmHardware) -> {
            String cpuIdentifier = WmiUtil.getString(ohmHardware, OhmHardware.IdentifierProperty.IDENTIFIER, 0);
            if (!cpuIdentifier.isEmpty()) {
                return OhmSensor.querySensorValue(h, cpuIdentifier, "Temperature");
            }
            return null;
        });
        if (ohmSensors != null && ohmSensors.getResultCount() > 0) {
            double sum = 0.0;
            for (int i = 0; i < ohmSensors.getResultCount(); ++i) {
                sum += (double)WmiUtil.getFloat(ohmSensors, OhmSensor.ValueProperty.VALUE, i);
            }
            return sum / (double)ohmSensors.getResultCount();
        }
        return 0.0;
    }

    private static double getTempFromLHM() {
        return WindowsSensors.getAverageValueFromLHM("CPU", "Temperature", (name, value) -> !name.contains("Max") && !name.contains("Average") && value > 0.0);
    }

    private static double getTempFromWMI() {
        double tempC = 0.0;
        long tempK = 0L;
        WbemcliUtil.WmiResult<MSAcpiThermalZoneTemperature.TemperatureProperty> result = MSAcpiThermalZoneTemperature.queryCurrentTemperature();
        if (result.getResultCount() > 0) {
            LOG.debug("Found Temperature data in WMI");
            tempK = WmiUtil.getUint32asLong(result, MSAcpiThermalZoneTemperature.TemperatureProperty.CURRENTTEMPERATURE, 0);
        }
        if (tempK > 2732L) {
            tempC = (double)tempK / 10.0 - 273.15;
        } else if (tempK > 274L) {
            tempC = (double)tempK - 273.0;
        }
        return Math.max(tempC, 0.0);
    }

    @Override
    public int[] queryFanSpeeds() {
        int[] fanSpeeds = WindowsSensors.getFansFromOHM();
        if (fanSpeeds.length > 0) {
            return fanSpeeds;
        }
        fanSpeeds = WindowsSensors.getFansFromLHM();
        if (fanSpeeds.length > 0) {
            return fanSpeeds;
        }
        fanSpeeds = WindowsSensors.getFansFromWMI();
        if (fanSpeeds.length > 0) {
            return fanSpeeds;
        }
        return new int[0];
    }

    private static int[] getFansFromOHM() {
        WbemcliUtil.WmiResult<OhmSensor.ValueProperty> ohmSensors = WindowsSensors.getOhmSensors("Hardware", "CPU", "Fan", (h, ohmHardware) -> {
            String cpuIdentifier = WmiUtil.getString(ohmHardware, OhmHardware.IdentifierProperty.IDENTIFIER, 0);
            if (!cpuIdentifier.isEmpty()) {
                return OhmSensor.querySensorValue(h, cpuIdentifier, "Fan");
            }
            return null;
        });
        if (ohmSensors != null && ohmSensors.getResultCount() > 0) {
            int[] fanSpeeds = new int[ohmSensors.getResultCount()];
            for (int i = 0; i < ohmSensors.getResultCount(); ++i) {
                fanSpeeds[i] = (int)WmiUtil.getFloat(ohmSensors, OhmSensor.ValueProperty.VALUE, i);
            }
            return fanSpeeds;
        }
        return new int[0];
    }

    private static int[] getFansFromLHM() {
        List<?> sensors = WindowsSensors.getLhmSensors("SuperIO", "Fan");
        if (sensors == null || sensors.isEmpty()) {
            return new int[0];
        }
        try {
            Class<?> sensorClass = Class.forName("io.github.pandalxb.jlibrehardwaremonitor.model.Sensor");
            Method getValueMethod = sensorClass.getMethod("getValue", new Class[0]);
            return sensors.stream().filter(sensor -> {
                try {
                    double value = (Double)getValueMethod.invoke(sensor, new Object[0]);
                    return value > 0.0;
                }
                catch (Exception e) {
                    LOG.warn(REFLECT_EXCEPTION_MSG, (Object)e.getMessage());
                    return false;
                }
            }).mapToInt(sensor -> {
                try {
                    return (int)((Double)getValueMethod.invoke(sensor, new Object[0])).doubleValue();
                }
                catch (Exception e) {
                    LOG.warn(REFLECT_EXCEPTION_MSG, (Object)e.getMessage());
                    return 0;
                }
            }).toArray();
        }
        catch (Exception e) {
            LOG.warn(REFLECT_EXCEPTION_MSG, (Object)e.getMessage());
            return new int[0];
        }
    }

    private static int[] getFansFromWMI() {
        WbemcliUtil.WmiResult<Win32Fan.SpeedProperty> fan = Win32Fan.querySpeed();
        if (fan.getResultCount() > 1) {
            LOG.debug("Found Fan data in WMI");
            int[] fanSpeeds = new int[fan.getResultCount()];
            for (int i = 0; i < fan.getResultCount(); ++i) {
                fanSpeeds[i] = (int)WmiUtil.getUint64(fan, Win32Fan.SpeedProperty.DESIREDSPEED, i);
            }
            return fanSpeeds;
        }
        return new int[0];
    }

    @Override
    public double queryCpuVoltage() {
        double volts = WindowsSensors.getVoltsFromOHM();
        if (volts > 0.0) {
            return volts;
        }
        volts = WindowsSensors.getVoltsFromLHM();
        if (volts > 0.0) {
            return volts;
        }
        volts = WindowsSensors.getVoltsFromWMI();
        return volts;
    }

    private static double getVoltsFromOHM() {
        WbemcliUtil.WmiResult<OhmSensor.ValueProperty> ohmSensors = WindowsSensors.getOhmSensors("Sensor", "Voltage", "Voltage", (h, ohmHardware) -> {
            String cpuIdentifier = null;
            for (int i = 0; i < ohmHardware.getResultCount(); ++i) {
                String id = WmiUtil.getString(ohmHardware, OhmHardware.IdentifierProperty.IDENTIFIER, i);
                if (!id.toLowerCase(Locale.ROOT).contains("cpu")) continue;
                cpuIdentifier = id;
                break;
            }
            if (cpuIdentifier == null) {
                cpuIdentifier = WmiUtil.getString(ohmHardware, OhmHardware.IdentifierProperty.IDENTIFIER, 0);
            }
            return OhmSensor.querySensorValue(h, cpuIdentifier, "Voltage");
        });
        if (ohmSensors != null && ohmSensors.getResultCount() > 0) {
            return WmiUtil.getFloat(ohmSensors, OhmSensor.ValueProperty.VALUE, 0);
        }
        return 0.0;
    }

    private static double getVoltsFromLHM() {
        return WindowsSensors.getAverageValueFromLHM("SuperIO", "Voltage", (name, value) -> name.toLowerCase(Locale.ROOT).contains("vcore") && value > 0.0);
    }

    private static double getVoltsFromWMI() {
        WbemcliUtil.WmiResult<Win32Processor.VoltProperty> voltage = Win32Processor.queryVoltage();
        if (voltage.getResultCount() > 1) {
            LOG.debug("Found Voltage data in WMI");
            int decivolts = WmiUtil.getUint16(voltage, Win32Processor.VoltProperty.CURRENTVOLTAGE, 0);
            if (decivolts > 0) {
                if ((decivolts & 0x80) == 0) {
                    decivolts = WmiUtil.getUint32(voltage, Win32Processor.VoltProperty.VOLTAGECAPS, 0);
                    if ((decivolts & 1) > 0) {
                        return 5.0;
                    }
                    if ((decivolts & 2) > 0) {
                        return 3.3;
                    }
                    if ((decivolts & 4) > 0) {
                        return 2.9;
                    }
                } else {
                    return (double)(decivolts & 0x7F) / 10.0;
                }
            }
        }
        return 0.0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static WbemcliUtil.WmiResult<OhmSensor.ValueProperty> getOhmSensors(String typeToQuery, String typeName, String sensorType, BiFunction<WmiQueryHandler, WbemcliUtil.WmiResult<OhmHardware.IdentifierProperty>, WbemcliUtil.WmiResult<OhmSensor.ValueProperty>> querySensorFunction) {
        WmiQueryHandler h = Objects.requireNonNull(WmiQueryHandler.createInstance());
        boolean comInit = false;
        WbemcliUtil.WmiResult<OhmSensor.ValueProperty> ohmSensors = null;
        try {
            comInit = h.initCOM();
            WbemcliUtil.WmiResult<OhmHardware.IdentifierProperty> ohmHardware = OhmHardware.queryHwIdentifier(h, typeToQuery, typeName);
            if (ohmHardware.getResultCount() > 0) {
                LOG.debug("Found {} data in Open Hardware Monitor", (Object)sensorType);
                ohmSensors = querySensorFunction.apply(h, ohmHardware);
            }
        }
        catch (COMException e) {
            LOG.warn(COM_EXCEPTION_MSG, (Object)e.getMessage());
        }
        finally {
            if (comInit) {
                h.unInitCOM();
            }
        }
        return ohmSensors;
    }

    private static double getAverageValueFromLHM(String hardwareType, String sensorType, BiFunction<String, Double, Boolean> sensorValidFunction) {
        List<?> sensors = WindowsSensors.getLhmSensors(hardwareType, sensorType);
        if (sensors == null || sensors.isEmpty()) {
            return 0.0;
        }
        try {
            Class<?> sensorClass = Class.forName("io.github.pandalxb.jlibrehardwaremonitor.model.Sensor");
            Method getNameMethod = sensorClass.getMethod("getName", new Class[0]);
            Method getValueMethod = sensorClass.getMethod("getValue", new Class[0]);
            double sum = 0.0;
            int validCount = 0;
            for (Object sensor : sensors) {
                double value;
                String name = (String)getNameMethod.invoke(sensor, new Object[0]);
                if (!sensorValidFunction.apply(name, value = ((Double)getValueMethod.invoke(sensor, new Object[0])).doubleValue()).booleanValue()) continue;
                sum += value;
                ++validCount;
            }
            return validCount > 0 ? sum / (double)validCount : 0.0;
        }
        catch (Exception e) {
            LOG.warn(REFLECT_EXCEPTION_MSG, (Object)e.getMessage());
            return 0.0;
        }
    }

    private static List<?> getLhmSensors(String hardwareType, String sensorType) {
        try {
            Class<?> computerConfigClass = Class.forName("io.github.pandalxb.jlibrehardwaremonitor.config.ComputerConfig");
            Class<?> libreHardwareManagerClass = Class.forName("io.github.pandalxb.jlibrehardwaremonitor.manager.LibreHardwareManager");
            Method computerConfigGetInstanceMethod = computerConfigClass.getMethod("getInstance", new Class[0]);
            Object computerConfigInstance = computerConfigGetInstanceMethod.invoke(null, new Object[0]);
            Method setEnabledMethod = computerConfigClass.getMethod("setCpuEnabled", Boolean.TYPE);
            setEnabledMethod.invoke(computerConfigInstance, true);
            setEnabledMethod = computerConfigClass.getMethod("setMotherboardEnabled", Boolean.TYPE);
            setEnabledMethod.invoke(computerConfigInstance, true);
            Method libreHardwareManagerGetInstanceMethod = libreHardwareManagerClass.getMethod("getInstance", computerConfigClass);
            Object instance = libreHardwareManagerGetInstanceMethod.invoke(null, computerConfigInstance);
            Method querySensorsMethod = libreHardwareManagerClass.getMethod("querySensors", String.class, String.class);
            return (List)querySensorsMethod.invoke(instance, hardwareType, sensorType);
        }
        catch (Exception e) {
            LOG.warn(REFLECT_EXCEPTION_MSG, (Object)e.getMessage());
            return Collections.emptyList();
        }
    }
}

