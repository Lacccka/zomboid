/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.builder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.apache.commons.lang3.ArraySorter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.DiffBuilder;
import org.apache.commons.lang3.builder.DiffExclude;
import org.apache.commons.lang3.builder.DiffResult;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.reflect.FieldUtils;

public class ReflectionDiffBuilder<T>
implements org.apache.commons.lang3.builder.Builder<DiffResult<T>> {
    private final DiffBuilder<T> diffBuilder;
    private String[] excludeFieldNames;

    public static <T> Builder<T> builder() {
        return new Builder();
    }

    private static String[] toExcludeFieldNames(String[] excludeFieldNames) {
        if (excludeFieldNames == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        return ArraySorter.sort(ReflectionToStringBuilder.toNoNullStringArray(excludeFieldNames));
    }

    private ReflectionDiffBuilder(DiffBuilder<T> diffBuilder, String[] excludeFieldNames) {
        this.diffBuilder = diffBuilder;
        this.excludeFieldNames = excludeFieldNames;
    }

    @Deprecated
    public ReflectionDiffBuilder(T left, T right, ToStringStyle style) {
        this(DiffBuilder.builder().setLeft(left).setRight(right).setStyle(style).build(), null);
    }

    private boolean accept(Field field) {
        if (field.getName().indexOf(36) != -1) {
            return false;
        }
        if (Modifier.isTransient(field.getModifiers())) {
            return false;
        }
        if (Modifier.isStatic(field.getModifiers())) {
            return false;
        }
        if (this.excludeFieldNames != null && Arrays.binarySearch(this.excludeFieldNames, field.getName()) >= 0) {
            return false;
        }
        return !field.isAnnotationPresent(DiffExclude.class);
    }

    private void appendFields(Class<?> clazz) {
        for (Field field : FieldUtils.getAllFields(clazz)) {
            if (!this.accept(field)) continue;
            try {
                this.diffBuilder.append(field.getName(), this.readField(field, this.getLeft()), this.readField(field, this.getRight()));
            }
            catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Unexpected IllegalAccessException: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public DiffResult<T> build() {
        if (this.getLeft().equals(this.getRight())) {
            return this.diffBuilder.build();
        }
        this.appendFields(this.getLeft().getClass());
        return this.diffBuilder.build();
    }

    public String[] getExcludeFieldNames() {
        return (String[])this.excludeFieldNames.clone();
    }

    private T getLeft() {
        return this.diffBuilder.getLeft();
    }

    private T getRight() {
        return this.diffBuilder.getRight();
    }

    private Object readField(Field field, Object target) throws IllegalAccessException {
        return FieldUtils.readField(field, target, true);
    }

    @Deprecated
    public ReflectionDiffBuilder<T> setExcludeFieldNames(String ... excludeFieldNames) {
        this.excludeFieldNames = ReflectionDiffBuilder.toExcludeFieldNames(excludeFieldNames);
        return this;
    }

    public static final class Builder<T> {
        private String[] excludeFieldNames = ArrayUtils.EMPTY_STRING_ARRAY;
        private DiffBuilder<T> diffBuilder;

        public ReflectionDiffBuilder<T> build() {
            return new ReflectionDiffBuilder(this.diffBuilder, this.excludeFieldNames);
        }

        public Builder<T> setDiffBuilder(DiffBuilder<T> diffBuilder) {
            this.diffBuilder = diffBuilder;
            return this;
        }

        public Builder<T> setExcludeFieldNames(String ... excludeFieldNames) {
            this.excludeFieldNames = ReflectionDiffBuilder.toExcludeFieldNames(excludeFieldNames);
            return this;
        }
    }
}

