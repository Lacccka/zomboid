/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import pl.mjaron.tinyloki.LabelSettings;
import pl.mjaron.tinyloki.Utils;

public class Labels
implements Cloneable {
    public static final String LEVEL = "level";
    public static final String FATAL = "critical";
    public static final String WARN = "warning";
    public static final String INFO = "info";
    public static final String DEBUG = "debug";
    public static final String VERBOSE = "trace";
    public static final String TRACE = "trace";
    public static final String UNKNOWN = "unknown";
    public static final String SERVICE_NAME = "service_name";
    private TreeMap<String, String> map;

    public Labels() {
        this.map = new TreeMap();
    }

    public Labels(Labels other) {
        this.map = new TreeMap<String, String>((SortedMap<String, String>)other.map);
    }

    public Labels(Map<String, String> map) {
        this.map = new TreeMap<String, String>(map);
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public static void assertLabelIdentifierNotNullOrEmpty(String labelIdentifier) {
        if (labelIdentifier == null) {
            throw new RuntimeException("Label identifier is null.");
        }
        if (labelIdentifier.isEmpty()) {
            throw new RuntimeException("Label identifier is empty.");
        }
    }

    @Deprecated
    public static void validateLabelIdentifierOrThrow(String labelIdentifier) {
        Labels.assertLabelIdentifierNotNullOrEmpty(labelIdentifier);
        char firstChar = labelIdentifier.charAt(0);
        if (!Character.isLetter(firstChar)) {
            throw new RuntimeException("Cannot validate given label identifier: [" + labelIdentifier + "]:  First character is not a letter: [" + firstChar + "].");
        }
        for (int i = 1; i < labelIdentifier.length(); ++i) {
            char ch = labelIdentifier.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == '_') continue;
            throw new RuntimeException("Cannot validate given label identifier: [" + labelIdentifier + "]:  Given character is not a letter or digit: [" + ch + "].");
        }
    }

    @Deprecated
    public static boolean checkLabelIdentifierWhenNotEmpty(String labelIdentifier) {
        return Labels.checkLabelNameWhenNotEmpty(labelIdentifier);
    }

    public static boolean isNameFirstCharacterCorrect(char firstChar) {
        return Utils.isAsciiLetter(firstChar) || firstChar == '_';
    }

    public static boolean isNameNotFirstCharacterCorrect(char notFirstChar) {
        return Utils.isAsciiLetterOrDigit(notFirstChar) || notFirstChar == '_';
    }

    public static boolean isNameReservedForInternalUse(String labelName) {
        return labelName.startsWith("__");
    }

    public static boolean checkLabelNameWhenNotEmpty(String labelName) {
        if (!Labels.isNameFirstCharacterCorrect(labelName.charAt(0))) {
            return false;
        }
        if (Labels.isNameReservedForInternalUse(labelName)) {
            return false;
        }
        for (int i = 1; i < labelName.length(); ++i) {
            char ch = labelName.charAt(i);
            if (Labels.isNameNotFirstCharacterCorrect(ch)) continue;
            return false;
        }
        return true;
    }

    public static String correctLabelName(String labelName) {
        char[] stringBytes = labelName.toCharArray();
        if (!Labels.isNameFirstCharacterCorrect(stringBytes[0]) || Labels.isNameReservedForInternalUse(labelName)) {
            stringBytes[0] = 65;
        }
        for (int i = 1; i < stringBytes.length; ++i) {
            char ch = stringBytes[i];
            if (Labels.isNameNotFirstCharacterCorrect(ch)) continue;
            stringBytes[i] = 95;
        }
        return new String(stringBytes);
    }

    public static String prettifyLabelName(String labelName, int maxLength) {
        Labels.assertLabelIdentifierNotNullOrEmpty(labelName);
        String validLengthIdentifier = Labels.narrowLabelIdentifierLength(labelName, maxLength);
        if (Labels.checkLabelNameWhenNotEmpty(validLengthIdentifier)) {
            return validLengthIdentifier;
        }
        return Labels.correctLabelName(validLengthIdentifier);
    }

    public static String narrowLabelIdentifierLength(String labelIdentifier, int maxLength) {
        if (labelIdentifier.length() > maxLength) {
            return labelIdentifier.substring(0, maxLength);
        }
        return labelIdentifier;
    }

    public static String prettifyLabelValue(String labelValue, int maxLength) {
        Labels.assertLabelIdentifierNotNullOrEmpty(labelValue);
        return Labels.narrowLabelIdentifierLength(labelValue, maxLength);
    }

    @Deprecated
    public static String prettifyLabelIdentifier(String labelIdentifier, int maxLength) {
        Labels.assertLabelIdentifierNotNullOrEmpty(labelIdentifier);
        String validLengthIdentifier = labelIdentifier.length() > maxLength ? labelIdentifier.substring(0, maxLength) : labelIdentifier;
        if (Labels.checkLabelIdentifierWhenNotEmpty(validLengthIdentifier)) {
            return validLengthIdentifier;
        }
        return Labels.correctLabelName(validLengthIdentifier);
    }

    @Deprecated
    public static String prettifyLabelIdentifier(String labelIdentifier) {
        return Labels.prettifyLabelIdentifier(labelIdentifier, Integer.MAX_VALUE);
    }

    public static Labels prettify(Labels labels, int maxLabelNameLength, int maxLabelValueLength) {
        return Labels.prettify(labels, new LabelSettings(maxLabelNameLength, maxLabelValueLength));
    }

    public static Labels prettify(Labels labels, LabelSettings labelSettings) {
        Labels prettified = new Labels();
        for (Map.Entry<String, String> entry : labels.getMap().entrySet()) {
            String name = Labels.prettifyLabelName(entry.getKey(), labelSettings.getMaxLabelNameLength());
            String value = Labels.prettifyLabelValue(entry.getValue(), labelSettings.getMaxLabelValueLength());
            prettified.l(name, value);
        }
        return prettified;
    }

    public static Labels of(Labels labels) {
        return new Labels(labels);
    }

    public static Labels of(Map<String, String> labels) {
        return new Labels(labels);
    }

    public static Labels of(String name, String value) {
        return new Labels().l(name, value);
    }

    public static Labels of(String name, int value) {
        return new Labels().l(name, value);
    }

    public static Labels of(String name, long value) {
        return new Labels().l(name, value);
    }

    public static Labels of(String name, float value) {
        return new Labels().l(name, value);
    }

    public static Labels of(String name, double value) {
        return new Labels().l(name, value);
    }

    public static Labels of(String name, char value) {
        return new Labels().l(name, value);
    }

    public static Labels of(String name, byte value) {
        return new Labels().l(name, value);
    }

    public static Labels of(String name, short value) {
        return new Labels().l(name, value);
    }

    public static Labels of() {
        return new Labels();
    }

    public Map<String, String> getMap() {
        return this.map;
    }

    public Labels clone() {
        try {
            Labels cloned = (Labels)super.clone();
            cloned.map = new TreeMap<String, String>((SortedMap<String, String>)this.map);
            return cloned;
        }
        catch (CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone.", e);
        }
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!this.getClass().equals(other.getClass())) {
            return false;
        }
        Labels otherLabels = (Labels)other;
        return this.map.equals(otherLabels.map);
    }

    public int hashCode() {
        return this.map.hashCode();
    }

    public String toString() {
        return this.map.toString();
    }

    public Labels l(String name, String value) {
        this.map.put(name, value);
        return this;
    }

    public Labels l(String name, float value) {
        this.map.put(name, Float.toString(value));
        return this;
    }

    public Labels l(String name, double value) {
        this.map.put(name, Double.toString(value));
        return this;
    }

    public Labels l(String name, int value) {
        this.map.put(name, Integer.toString(value));
        return this;
    }

    public Labels l(String name, long value) {
        this.map.put(name, Long.toString(value));
        return this;
    }

    public Labels l(String name, char value) {
        this.map.put(name, Character.toString(value));
        return this;
    }

    public Labels l(String name, byte value) {
        this.map.put(name, Byte.toString(value));
        return this;
    }

    public Labels l(String name, short value) {
        this.map.put(name, Short.toString(value));
        return this;
    }

    public Labels l(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            this.l(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public Labels l(Labels other) {
        return this.l(other.map);
    }

    public Labels critical() {
        return this.l(LEVEL, FATAL);
    }

    public Labels fatal() {
        return this.l(LEVEL, FATAL);
    }

    public Labels warning() {
        return this.l(LEVEL, WARN);
    }

    public Labels info() {
        return this.l(LEVEL, INFO);
    }

    public Labels debug() {
        return this.l(LEVEL, DEBUG);
    }

    public Labels verbose() {
        return this.l(LEVEL, "trace");
    }

    public Labels trace() {
        return this.l(LEVEL, "trace");
    }

    public Labels unknown() {
        return this.l(LEVEL, UNKNOWN);
    }
}

