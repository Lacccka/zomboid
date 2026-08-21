/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

public class MetricNameFilter
implements Predicate<String> {
    public static final Predicate<String> ALLOW_ALL = name -> true;
    private final Collection<String> nameIsEqualTo;
    private final Collection<String> nameIsNotEqualTo;
    private final Collection<String> nameStartsWith;
    private final Collection<String> nameDoesNotStartWith;

    private MetricNameFilter(Collection<String> nameIsEqualTo, Collection<String> nameIsNotEqualTo, Collection<String> nameStartsWith, Collection<String> nameDoesNotStartWith) {
        this.nameIsEqualTo = Collections.unmodifiableCollection(new ArrayList<String>(nameIsEqualTo));
        this.nameIsNotEqualTo = Collections.unmodifiableCollection(new ArrayList<String>(nameIsNotEqualTo));
        this.nameStartsWith = Collections.unmodifiableCollection(new ArrayList<String>(nameStartsWith));
        this.nameDoesNotStartWith = Collections.unmodifiableCollection(new ArrayList<String>(nameDoesNotStartWith));
    }

    @Override
    public boolean test(String sampleName) {
        return this.matchesNameEqualTo(sampleName) && !this.matchesNameNotEqualTo(sampleName) && this.matchesNameStartsWith(sampleName) && !this.matchesNameDoesNotStartWith(sampleName);
    }

    private boolean matchesNameEqualTo(String metricName) {
        if (this.nameIsEqualTo.isEmpty()) {
            return true;
        }
        for (String name : this.nameIsEqualTo) {
            if (!name.startsWith(metricName)) continue;
            return true;
        }
        return false;
    }

    private boolean matchesNameNotEqualTo(String metricName) {
        if (this.nameIsNotEqualTo.isEmpty()) {
            return false;
        }
        for (String name : this.nameIsNotEqualTo) {
            if (!name.startsWith(metricName)) continue;
            return true;
        }
        return false;
    }

    private boolean matchesNameStartsWith(String metricName) {
        if (this.nameStartsWith.isEmpty()) {
            return true;
        }
        for (String prefix : this.nameStartsWith) {
            if (!metricName.startsWith(prefix)) continue;
            return true;
        }
        return false;
    }

    private boolean matchesNameDoesNotStartWith(String metricName) {
        if (this.nameDoesNotStartWith.isEmpty()) {
            return false;
        }
        for (String prefix : this.nameDoesNotStartWith) {
            if (!metricName.startsWith(prefix)) continue;
            return true;
        }
        return false;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Collection<String> nameEqualTo = new ArrayList<String>();
        private final Collection<String> nameNotEqualTo = new ArrayList<String>();
        private final Collection<String> nameStartsWith = new ArrayList<String>();
        private final Collection<String> nameDoesNotStartWith = new ArrayList<String>();

        private Builder() {
        }

        public Builder nameMustBeEqualTo(String ... names) {
            return this.nameMustBeEqualTo(Arrays.asList(names));
        }

        public Builder nameMustBeEqualTo(Collection<String> names) {
            if (names != null) {
                this.nameEqualTo.addAll(names);
            }
            return this;
        }

        public Builder nameMustNotBeEqualTo(String ... names) {
            return this.nameMustNotBeEqualTo(Arrays.asList(names));
        }

        public Builder nameMustNotBeEqualTo(Collection<String> names) {
            if (names != null) {
                this.nameNotEqualTo.addAll(names);
            }
            return this;
        }

        public Builder nameMustStartWith(String ... prefixes) {
            return this.nameMustStartWith(Arrays.asList(prefixes));
        }

        public Builder nameMustStartWith(Collection<String> prefixes) {
            if (prefixes != null) {
                this.nameStartsWith.addAll(prefixes);
            }
            return this;
        }

        public Builder nameMustNotStartWith(String ... prefixes) {
            return this.nameMustNotStartWith(Arrays.asList(prefixes));
        }

        public Builder nameMustNotStartWith(Collection<String> prefixes) {
            if (prefixes != null) {
                this.nameDoesNotStartWith.addAll(prefixes);
            }
            return this;
        }

        public MetricNameFilter build() {
            return new MetricNameFilter(this.nameEqualTo, this.nameNotEqualTo, this.nameStartsWith, this.nameDoesNotStartWith);
        }
    }
}

