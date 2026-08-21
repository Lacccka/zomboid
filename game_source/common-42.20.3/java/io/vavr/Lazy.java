/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.GwtIncompatible;
import io.vavr.Value;
import io.vavr.collection.Iterator;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Option;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Lazy<T>
implements Value<T>,
Supplier<T>,
Serializable {
    private static final long serialVersionUID = 1L;
    private volatile transient Supplier<? extends T> supplier;
    private T value;

    private Lazy(Supplier<? extends T> supplier) {
        this.supplier = supplier;
    }

    public static <T> Lazy<T> narrow(Lazy<? extends T> lazy) {
        return lazy;
    }

    public static <T> Lazy<T> of(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        if (supplier instanceof Lazy) {
            return (Lazy)supplier;
        }
        return new Lazy<T>(supplier);
    }

    public static <T> Lazy<Seq<T>> sequence(Iterable<? extends Lazy<? extends T>> values2) {
        Objects.requireNonNull(values2, "values is null");
        return Lazy.of(() -> Vector.ofAll(values2).map((T lazy) -> lazy.get()));
    }

    @GwtIncompatible(value="reflection is not supported")
    public static <T> T val(Supplier<? extends T> supplier, Class<T> type) {
        Objects.requireNonNull(supplier, "supplier is null");
        Objects.requireNonNull(type, "type is null");
        if (!type.isInterface()) {
            throw new IllegalArgumentException("type has to be an interface");
        }
        Lazy lazy = Lazy.of(supplier);
        InvocationHandler handler = (proxy, method, args2) -> method.invoke(lazy.get(), args2);
        return (T)Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, handler);
    }

    public Option<T> filter(Predicate<? super T> predicate) {
        T v = this.get();
        return predicate.test(v) ? Option.some(v) : Option.none();
    }

    @Override
    public T get() {
        return this.supplier == null ? this.value : this.computeValue();
    }

    private synchronized T computeValue() {
        Supplier<T> s = this.supplier;
        if (s != null) {
            this.value = s.get();
            this.supplier = null;
        }
        return this.value;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public boolean isEvaluated() {
        return this.supplier == null;
    }

    @Override
    public boolean isLazy() {
        return true;
    }

    @Override
    public boolean isSingleValued() {
        return true;
    }

    @Override
    public Iterator<T> iterator() {
        return Iterator.of(this.get());
    }

    @Override
    public <U> Lazy<U> map(Function<? super T, ? extends U> mapper) {
        return Lazy.of(() -> mapper.apply((T)this.get()));
    }

    @Override
    public Lazy<T> peek(Consumer<? super T> action) {
        action.accept(this.get());
        return this;
    }

    public <U> U transform(Function<? super Lazy<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    @Override
    public String stringPrefix() {
        return "Lazy";
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof Lazy && Objects.equals(((Lazy)o).get(), this.get());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.get());
    }

    @Override
    public String toString() {
        return this.stringPrefix() + "(" + (!this.isEvaluated() ? "?" : this.value) + ")";
    }

    @GwtIncompatible(value="The Java serialization protocol is explicitly not supported")
    private void writeObject(ObjectOutputStream s) throws IOException {
        this.get();
        s.defaultWriteObject();
    }
}

