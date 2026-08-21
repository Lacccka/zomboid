/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.Unit;
import java.util.regex.Pattern;

public class PrometheusNaming {
    private static final Pattern METRIC_NAME_PATTERN = Pattern.compile("^[a-zA-Z_.:][a-zA-Z0-9_.:]*$");
    private static final Pattern LABEL_NAME_PATTERN = Pattern.compile("^[a-zA-Z_.][a-zA-Z0-9_.]*$");
    private static final Pattern UNIT_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.:]+$");
    private static final String[] RESERVED_METRIC_NAME_SUFFIXES = new String[]{"_total", "_created", "_bucket", "_info", ".total", ".created", ".bucket", ".info"};

    public static boolean isValidMetricName(String name) {
        return PrometheusNaming.validateMetricName(name) == null;
    }

    public static String validateMetricName(String name) {
        for (String reservedSuffix : RESERVED_METRIC_NAME_SUFFIXES) {
            if (!name.endsWith(reservedSuffix)) continue;
            return "The metric name must not include the '" + reservedSuffix + "' suffix.";
        }
        if (!METRIC_NAME_PATTERN.matcher(name).matches()) {
            return "The metric name contains unsupported characters";
        }
        return null;
    }

    public static boolean isValidLabelName(String name) {
        return LABEL_NAME_PATTERN.matcher(name).matches() && !name.startsWith("__") && !name.startsWith("._") && !name.startsWith("..") && !name.startsWith("_.");
    }

    public static boolean isValidUnitName(String name) {
        return PrometheusNaming.validateUnitName(name) == null;
    }

    public static String validateUnitName(String name) {
        if (name.isEmpty()) {
            return "The unit name must not be empty.";
        }
        for (String reservedSuffix : RESERVED_METRIC_NAME_SUFFIXES) {
            String suffixName = reservedSuffix.substring(1);
            if (!name.endsWith(suffixName)) continue;
            return suffixName + " is a reserved suffix in Prometheus";
        }
        if (!UNIT_NAME_PATTERN.matcher(name).matches()) {
            return "The unit name contains unsupported characters";
        }
        return null;
    }

    public static String prometheusName(String name) {
        return name.replace(".", "_");
    }

    public static String sanitizeMetricName(String metricName) {
        if (metricName.isEmpty()) {
            throw new IllegalArgumentException("Cannot convert an empty string to a valid metric name.");
        }
        String sanitizedName = PrometheusNaming.replaceIllegalCharsInMetricName(metricName);
        boolean modified = true;
        while (modified) {
            modified = false;
            for (String reservedSuffix : RESERVED_METRIC_NAME_SUFFIXES) {
                if (sanitizedName.equals(reservedSuffix)) {
                    return reservedSuffix.substring(1);
                }
                if (!sanitizedName.endsWith(reservedSuffix)) continue;
                sanitizedName = sanitizedName.substring(0, sanitizedName.length() - reservedSuffix.length());
                modified = true;
            }
        }
        return sanitizedName;
    }

    public static String sanitizeMetricName(String metricName, Unit unit) {
        String result = PrometheusNaming.sanitizeMetricName(metricName);
        if (unit != null && !result.endsWith("_" + unit) && !result.endsWith("." + unit)) {
            result = result + "_" + unit;
        }
        return result;
    }

    public static String sanitizeLabelName(String labelName) {
        if (labelName.isEmpty()) {
            throw new IllegalArgumentException("Cannot convert an empty string to a valid label name.");
        }
        String sanitizedName = PrometheusNaming.replaceIllegalCharsInLabelName(labelName);
        while (sanitizedName.startsWith("__") || sanitizedName.startsWith("_.") || sanitizedName.startsWith("._") || sanitizedName.startsWith("..")) {
            sanitizedName = sanitizedName.substring(1);
        }
        return sanitizedName;
    }

    public static String sanitizeUnitName(String unitName) {
        if (unitName.isEmpty()) {
            throw new IllegalArgumentException("Cannot convert an empty string to a valid unit name.");
        }
        String sanitizedName = PrometheusNaming.replaceIllegalCharsInUnitName(unitName);
        boolean modified = true;
        while (modified) {
            modified = false;
            while (sanitizedName.startsWith("_") || sanitizedName.startsWith(".")) {
                sanitizedName = sanitizedName.substring(1);
                modified = true;
            }
            while (sanitizedName.endsWith(".") || sanitizedName.endsWith("_")) {
                sanitizedName = sanitizedName.substring(0, sanitizedName.length() - 1);
                modified = true;
            }
            for (String reservedSuffix : RESERVED_METRIC_NAME_SUFFIXES) {
                String suffixName = reservedSuffix.substring(1);
                if (!sanitizedName.endsWith(suffixName)) continue;
                sanitizedName = sanitizedName.substring(0, sanitizedName.length() - suffixName.length());
                modified = true;
            }
        }
        if (sanitizedName.isEmpty()) {
            throw new IllegalArgumentException("Cannot convert '" + unitName + "' into a valid unit name.");
        }
        return sanitizedName;
    }

    private static String replaceIllegalCharsInMetricName(String name) {
        int length = name.length();
        char[] sanitized = new char[length];
        for (int i = 0; i < length; ++i) {
            int ch = name.charAt(i);
            sanitized[i] = ch == 46 || ch >= 97 && ch <= 122 || ch >= 65 && ch <= 90 || i > 0 && ch >= 48 && ch <= 57 ? ch : 95;
        }
        return new String(sanitized);
    }

    private static String replaceIllegalCharsInLabelName(String name) {
        int length = name.length();
        char[] sanitized = new char[length];
        for (int i = 0; i < length; ++i) {
            int ch = name.charAt(i);
            sanitized[i] = ch == 46 || ch >= 97 && ch <= 122 || ch >= 65 && ch <= 90 || i > 0 && ch >= 48 && ch <= 57 ? ch : 95;
        }
        return new String(sanitized);
    }

    private static String replaceIllegalCharsInUnitName(String name) {
        int length = name.length();
        char[] sanitized = new char[length];
        for (int i = 0; i < length; ++i) {
            int ch = name.charAt(i);
            sanitized[i] = ch == 58 || ch == 46 || ch >= 97 && ch <= 122 || ch >= 65 && ch <= 90 || ch >= 48 && ch <= 57 ? ch : 95;
        }
        return new String(sanitized);
    }
}

