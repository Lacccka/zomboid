/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.Label;
import io.prometheus.metrics.model.snapshots.PrometheusNaming;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public final class Labels
implements Comparable<Labels>,
Iterable<Label> {
    public static final Labels EMPTY;
    private final String[] prometheusNames;
    private final String[] names;
    private final String[] values;

    private Labels(String[] names, String[] prometheusNames, String[] values2) {
        this.names = names;
        this.prometheusNames = prometheusNames;
        this.values = values2;
    }

    public boolean isEmpty() {
        return this == EMPTY || this.equals(EMPTY);
    }

    public static Labels of(String ... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Key/value pairs must have an even length");
        }
        if (keyValuePairs.length == 0) {
            return EMPTY;
        }
        String[] names = new String[keyValuePairs.length / 2];
        String[] values2 = new String[keyValuePairs.length / 2];
        int i = 0;
        while (2 * i < keyValuePairs.length) {
            names[i] = keyValuePairs[2 * i];
            values2[i] = keyValuePairs[2 * i + 1];
            ++i;
        }
        String[] prometheusNames = Labels.makePrometheusNames(names);
        Labels.sortAndValidate(names, prometheusNames, values2);
        return new Labels(names, prometheusNames, values2);
    }

    public static Labels of(List<String> names, List<String> values2) {
        if (names.size() != values2.size()) {
            throw new IllegalArgumentException("Names and values must have the same size.");
        }
        if (names.isEmpty()) {
            return EMPTY;
        }
        String[] namesCopy = names.toArray(new String[0]);
        String[] valuesCopy = values2.toArray(new String[0]);
        String[] prometheusNames = Labels.makePrometheusNames(namesCopy);
        Labels.sortAndValidate(namesCopy, prometheusNames, valuesCopy);
        return new Labels(namesCopy, prometheusNames, valuesCopy);
    }

    public static Labels of(String[] names, String[] values2) {
        if (names.length != values2.length) {
            throw new IllegalArgumentException("Names and values must have the same length.");
        }
        if (names.length == 0) {
            return EMPTY;
        }
        String[] namesCopy = Arrays.copyOf(names, names.length);
        String[] valuesCopy = Arrays.copyOf(values2, values2.length);
        String[] prometheusNames = Labels.makePrometheusNames(namesCopy);
        Labels.sortAndValidate(namesCopy, prometheusNames, valuesCopy);
        return new Labels(namesCopy, prometheusNames, valuesCopy);
    }

    static String[] makePrometheusNames(String[] names) {
        String[] prometheusNames = names;
        for (int i = 0; i < names.length; ++i) {
            if (!names[i].contains(".")) continue;
            if (prometheusNames == names) {
                prometheusNames = Arrays.copyOf(names, names.length);
            }
            prometheusNames[i] = PrometheusNaming.prometheusName(names[i]);
        }
        return prometheusNames;
    }

    public boolean contains(String labelName) {
        return this.get(labelName) != null;
    }

    public String get(String labelName) {
        labelName = PrometheusNaming.prometheusName(labelName);
        for (int i = 0; i < this.prometheusNames.length; ++i) {
            if (!this.prometheusNames[i].equals(labelName)) continue;
            return this.values[i];
        }
        return null;
    }

    private static void sortAndValidate(String[] names, String[] prometheusNames, String[] values2) {
        Labels.sort(names, prometheusNames, values2);
        Labels.validateNames(names, prometheusNames);
    }

    private static void validateNames(String[] names, String[] prometheusNames) {
        for (int i = 0; i < names.length; ++i) {
            if (!PrometheusNaming.isValidLabelName(names[i])) {
                throw new IllegalArgumentException("'" + names[i] + "' is an illegal label name");
            }
            if (i <= 0 || !prometheusNames[i - 1].equals(prometheusNames[i])) continue;
            throw new IllegalArgumentException(names[i] + ": duplicate label name");
        }
    }

    private static void sort(String[] names, String[] prometheusNames, String[] values2) {
        int n = prometheusNames.length;
        for (int i = 0; i < n - 1; ++i) {
            for (int j = 0; j < n - i - 1; ++j) {
                if (prometheusNames[j].compareTo(prometheusNames[j + 1]) <= 0) continue;
                Labels.swap(j, j + 1, names, prometheusNames, values2);
            }
        }
    }

    private static void swap(int i, int j, String[] names, String[] prometheusNames, String[] values2) {
        String tmp = names[j];
        names[j] = names[i];
        names[i] = tmp;
        tmp = values2[j];
        values2[j] = values2[i];
        values2[i] = tmp;
        if (prometheusNames != names) {
            tmp = prometheusNames[j];
            prometheusNames[j] = prometheusNames[i];
            prometheusNames[i] = tmp;
        }
    }

    @Override
    public Iterator<Label> iterator() {
        return this.asList().iterator();
    }

    public Stream<Label> stream() {
        return this.asList().stream();
    }

    public int size() {
        return this.names.length;
    }

    public String getName(int i) {
        return this.names[i];
    }

    public String getPrometheusName(int i) {
        return this.prometheusNames[i];
    }

    public String getValue(int i) {
        return this.values[i];
    }

    public Labels merge(Labels other) {
        String[] names;
        if (this.isEmpty()) {
            return other;
        }
        if (other.isEmpty()) {
            return this;
        }
        String[] prometheusNames = names = new String[this.names.length + other.names.length];
        if (this.names != this.prometheusNames || other.names != other.prometheusNames) {
            prometheusNames = new String[names.length];
        }
        String[] values2 = new String[names.length];
        int thisPos = 0;
        int otherPos = 0;
        while (thisPos + otherPos < names.length) {
            if (thisPos >= this.names.length) {
                names[thisPos + otherPos] = other.names[otherPos];
                values2[thisPos + otherPos] = other.values[otherPos];
                if (prometheusNames != names) {
                    prometheusNames[thisPos + otherPos] = other.prometheusNames[otherPos];
                }
                ++otherPos;
                continue;
            }
            if (otherPos >= other.names.length) {
                names[thisPos + otherPos] = this.names[thisPos];
                values2[thisPos + otherPos] = this.values[thisPos];
                if (prometheusNames != names) {
                    prometheusNames[thisPos + otherPos] = this.prometheusNames[thisPos];
                }
                ++thisPos;
                continue;
            }
            if (this.prometheusNames[thisPos].compareTo(other.prometheusNames[otherPos]) < 0) {
                names[thisPos + otherPos] = this.names[thisPos];
                values2[thisPos + otherPos] = this.values[thisPos];
                if (prometheusNames != names) {
                    prometheusNames[thisPos + otherPos] = this.prometheusNames[thisPos];
                }
                ++thisPos;
                continue;
            }
            if (this.prometheusNames[thisPos].compareTo(other.prometheusNames[otherPos]) > 0) {
                names[thisPos + otherPos] = other.names[otherPos];
                values2[thisPos + otherPos] = other.values[otherPos];
                if (prometheusNames != names) {
                    prometheusNames[thisPos + otherPos] = other.prometheusNames[otherPos];
                }
                ++otherPos;
                continue;
            }
            throw new IllegalArgumentException("Duplicate label name: '" + this.names[thisPos] + "'.");
        }
        return new Labels(names, prometheusNames, values2);
    }

    public Labels merge(String[] names, String[] values2) {
        if (this.equals(EMPTY)) {
            return Labels.of(names, values2);
        }
        String[] mergedNames = new String[this.names.length + names.length];
        String[] mergedValues = new String[this.values.length + values2.length];
        System.arraycopy(this.names, 0, mergedNames, 0, this.names.length);
        System.arraycopy(this.values, 0, mergedValues, 0, this.values.length);
        System.arraycopy(names, 0, mergedNames, this.names.length, names.length);
        System.arraycopy(values2, 0, mergedValues, this.values.length, values2.length);
        String[] prometheusNames = Labels.makePrometheusNames(mergedNames);
        Labels.sortAndValidate(mergedNames, prometheusNames, mergedValues);
        return new Labels(mergedNames, prometheusNames, mergedValues);
    }

    public Labels add(String name, String value) {
        return this.merge(Labels.of(name, value));
    }

    public boolean hasSameNames(Labels other) {
        return Arrays.equals(this.prometheusNames, other.prometheusNames);
    }

    public boolean hasSameValues(Labels other) {
        return Arrays.equals(this.values, other.values);
    }

    @Override
    public int compareTo(Labels other) {
        int result = this.compare(this.prometheusNames, other.prometheusNames);
        if (result != 0) {
            return result;
        }
        return this.compare(this.values, other.values);
    }

    private int compare(String[] array1, String[] array2) {
        for (int i = 0; i < array1.length; ++i) {
            if (array2.length <= i) {
                return 1;
            }
            int result = array1[i].compareTo(array2[i]);
            if (result == 0) continue;
            return result;
        }
        if (array2.length > array1.length) {
            return -1;
        }
        return 0;
    }

    private List<Label> asList() {
        ArrayList<Label> result = new ArrayList<Label>(this.names.length);
        for (int i = 0; i < this.names.length; ++i) {
            result.add(new Label(this.names[i], this.values[i]));
        }
        return Collections.unmodifiableList(result);
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("{");
        for (int i = 0; i < this.names.length; ++i) {
            if (i > 0) {
                b.append(",");
            }
            b.append(this.names[i]);
            b.append("=\"");
            this.appendEscapedLabelValue(b, this.values[i]);
            b.append("\"");
        }
        b.append("}");
        return b.toString();
    }

    private void appendEscapedLabelValue(StringBuilder b, String value) {
        block5: for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            switch (c) {
                case '\\': {
                    b.append("\\\\");
                    continue block5;
                }
                case '\"': {
                    b.append("\\\"");
                    continue block5;
                }
                case '\n': {
                    b.append("\\n");
                    continue block5;
                }
                default: {
                    b.append(c);
                }
            }
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Labels labels = (Labels)o;
        return labels.hasSameNames(this) && labels.hasSameValues(this);
    }

    public int hashCode() {
        int result = Arrays.hashCode(this.prometheusNames);
        result = 31 * result + Arrays.hashCode(this.values);
        return result;
    }

    public static Builder builder() {
        return new Builder();
    }

    static {
        String[] names = new String[]{};
        String[] values2 = new String[]{};
        EMPTY = new Labels(names, names, values2);
    }

    public static class Builder {
        private final List<String> names = new ArrayList<String>();
        private final List<String> values = new ArrayList<String>();

        private Builder() {
        }

        public Builder label(String name, String value) {
            this.names.add(name);
            this.values.add(value);
            return this;
        }

        public Labels build() {
            return Labels.of(this.names, this.values);
        }
    }
}

