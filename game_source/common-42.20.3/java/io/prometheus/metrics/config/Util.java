/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.config;

import io.prometheus.metrics.config.PrometheusPropertiesException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

class Util {
    Util() {
    }

    static String getProperty(String name, Map<Object, Object> properties) {
        Object object = properties.remove(name);
        if (object != null) {
            return object.toString();
        }
        return null;
    }

    static Boolean loadBoolean(String name, Map<Object, Object> properties) throws PrometheusPropertiesException {
        String property = Util.getProperty(name, properties);
        if (property != null) {
            if (!"true".equalsIgnoreCase(property) && !"false".equalsIgnoreCase(property)) {
                throw new PrometheusPropertiesException(String.format("%s: Expecting 'true' or 'false'. Found: %s", name, property));
            }
            return Boolean.parseBoolean(property);
        }
        return null;
    }

    static List<Double> toList(double ... values2) {
        if (values2 == null) {
            return null;
        }
        ArrayList<Double> result = new ArrayList<Double>(values2.length);
        for (double value : values2) {
            result.add(value);
        }
        return result;
    }

    static String loadString(String name, Map<Object, Object> properties) throws PrometheusPropertiesException {
        return Util.getProperty(name, properties);
    }

    static String loadStringAddSuffix(String name, Map<Object, Object> properties, String suffix) {
        Object object = properties.remove(name);
        if (object != null) {
            return object + suffix;
        }
        return null;
    }

    static List<String> loadStringList(String name, Map<Object, Object> properties) throws PrometheusPropertiesException {
        String property = Util.getProperty(name, properties);
        if (property != null) {
            return Arrays.asList(property.split("\\s*,\\s*"));
        }
        return null;
    }

    static List<Double> loadDoubleList(String name, Map<Object, Object> properties) throws PrometheusPropertiesException {
        String property = Util.getProperty(name, properties);
        if (property != null) {
            String[] numbers = property.split("\\s*,\\s*");
            Double[] result = new Double[numbers.length];
            for (int i = 0; i < numbers.length; ++i) {
                try {
                    if ("+Inf".equals(numbers[i].trim())) {
                        result[i] = Double.POSITIVE_INFINITY;
                        continue;
                    }
                    result[i] = Double.parseDouble(numbers[i]);
                    continue;
                }
                catch (NumberFormatException e) {
                    throw new PrometheusPropertiesException(name + "=" + property + ": Expecting comma separated list of double values");
                }
            }
            return Arrays.asList(result);
        }
        return null;
    }

    static Map<String, String> loadMap(String name, Map<Object, Object> properties) throws PrometheusPropertiesException {
        HashMap<String, String> result = new HashMap<String, String>();
        String property = Util.getProperty(name, properties);
        if (property != null) {
            String[] pairs;
            for (String pair : pairs = property.split(",")) {
                String[] keyValue;
                if (!pair.contains("=") || (keyValue = pair.split("=", 2)).length != 2) continue;
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                if (key.isEmpty() || value.isEmpty()) continue;
                result.putIfAbsent(key, value);
            }
        }
        return result;
    }

    static Integer loadInteger(String name, Map<Object, Object> properties) throws PrometheusPropertiesException {
        String property = Util.getProperty(name, properties);
        if (property != null) {
            try {
                return Integer.parseInt(property);
            }
            catch (NumberFormatException e) {
                throw new PrometheusPropertiesException(name + "=" + property + ": Expecting integer value");
            }
        }
        return null;
    }

    static Double loadDouble(String name, Map<Object, Object> properties) throws PrometheusPropertiesException {
        String property = Util.getProperty(name, properties);
        if (property != null) {
            try {
                return Double.parseDouble(property);
            }
            catch (NumberFormatException e) {
                throw new PrometheusPropertiesException(name + "=" + property + ": Expecting double value");
            }
        }
        return null;
    }

    static Long loadLong(String name, Map<Object, Object> properties) throws PrometheusPropertiesException {
        String property = Util.getProperty(name, properties);
        if (property != null) {
            try {
                return Long.parseLong(property);
            }
            catch (NumberFormatException e) {
                throw new PrometheusPropertiesException(name + "=" + property + ": Expecting long value");
            }
        }
        return null;
    }

    static <T extends Number> void assertValue(T number, Predicate<T> predicate, String message, String prefix, String name) throws PrometheusPropertiesException {
        if (number != null && !predicate.test(number)) {
            String fullMessage = prefix == null ? name + ": " + message : String.format("%s.%s: %s Found: %s", prefix, name, message, number);
            throw new PrometheusPropertiesException(fullMessage);
        }
    }
}

