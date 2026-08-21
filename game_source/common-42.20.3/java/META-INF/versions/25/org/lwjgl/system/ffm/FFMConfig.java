/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.ffm.Binder;
import org.lwjgl.system.ffm.StructBinder;
import org.lwjgl.system.ffm.TraceConsumer;
import org.lwjgl.system.ffm.UnionBinder;
import org.lwjgl.system.ffm.UpcallBinder;

public final class FFMConfig {
    final MethodHandles.Lookup lookup;
    final HashMap<Class<?>, BinderField> binders = new HashMap();
    final @Nullable Class<? extends Annotation> nullableAnnotation;
    final boolean nullableAnnotationOnType;
    final @Nullable SymbolLookup symbolLookup;
    final @Nullable TraceConsumer traceConsumer;
    final @Nullable Predicate<Method> tracingFilter;
    final @Nullable Function<Method, Boolean> criticalOverride;
    final boolean checks;
    final boolean debugGenerator;

    FFMConfig(@Nullable Class<? extends Annotation> nullableAnnotation, MethodHandles.Lookup lookup, @Nullable SymbolLookup symbolLookup, @Nullable TraceConsumer traceConsumer, @Nullable Predicate<Method> tracingFilter, @Nullable Function<Method, Boolean> criticalOverride, boolean checks, boolean debugGenerator) {
        this.nullableAnnotation = nullableAnnotation;
        this.lookup = lookup;
        this.symbolLookup = symbolLookup;
        this.traceConsumer = traceConsumer;
        this.tracingFilter = tracingFilter;
        this.criticalOverride = criticalOverride;
        this.checks = checks;
        this.debugGenerator = debugGenerator;
        this.nullableAnnotationOnType = FFMConfig.validateNullableAnnotationClass(nullableAnnotation);
    }

    public MethodHandles.Lookup getLookup() {
        return this.lookup;
    }

    public boolean hasBinder(Class<?> type) {
        return this.binders.containsKey(type);
    }

    public <T> StructBinder<T> getStructBinder(Class<T> type) {
        return (StructBinder)Objects.requireNonNull(this.binders.get(type)).binder;
    }

    public <T> UnionBinder<T> getUnionBinder(Class<T> type) {
        return (UnionBinder)Objects.requireNonNull(this.binders.get(type)).binder;
    }

    public <T> UpcallBinder<T> getUpcallBinder(Class<T> type) {
        return (UpcallBinder)Objects.requireNonNull(this.binders.get(type)).binder;
    }

    public @Nullable Class<? extends Annotation> getNullableAnnotation() {
        return this.nullableAnnotation;
    }

    public @Nullable SymbolLookup getSymbolLookup() {
        return this.symbolLookup;
    }

    public @Nullable TraceConsumer getTraceConsumer() {
        return this.traceConsumer;
    }

    public @Nullable Predicate<Method> getTracingFilter() {
        return this.tracingFilter;
    }

    public @Nullable Function<Method, Boolean> getCriticalOverride() {
        return this.criticalOverride;
    }

    public boolean checks() {
        return this.checks;
    }

    public boolean debugGenerator() {
        return this.debugGenerator;
    }

    static boolean validateNullableAnnotationClass(@Nullable Class<? extends Annotation> nullableAnnotation) {
        if (nullableAnnotation == null) {
            return false;
        }
        Retention retention = nullableAnnotation.getDeclaredAnnotation(Retention.class);
        if (retention == null || retention.value() != RetentionPolicy.RUNTIME) {
            throw new IllegalStateException("Nullable annotation " + String.valueOf(nullableAnnotation) + " must have RUNTIME retention");
        }
        Target target = nullableAnnotation.getDeclaredAnnotation(Target.class);
        if (target != null) {
            List<ElementType> elementTypes = List.of(target.value());
            if (elementTypes.contains((Object)ElementType.TYPE_USE)) {
                return true;
            }
            if (!elementTypes.contains((Object)ElementType.METHOD) || !elementTypes.contains((Object)ElementType.PARAMETER)) {
                throw new IllegalStateException("Nullable annotation " + String.valueOf(nullableAnnotation) + " must @Target either TYPE_USE or METHOD+PARAMETER");
            }
        }
        return false;
    }

    record BinderField(String name, Binder<?> binder) {
    }
}

