/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import java.io.Closeable;
import java.io.IOException;
import java.util.stream.BaseStream;
import org.apache.commons.io.function.Erase;
import org.apache.commons.io.function.IOIterator;
import org.apache.commons.io.function.IOIteratorAdapter;
import org.apache.commons.io.function.IORunnable;
import org.apache.commons.io.function.IOSpliterator;
import org.apache.commons.io.function.IOSpliteratorAdapter;
import org.apache.commons.io.function.UncheckedIOBaseStream;

public interface IOBaseStream<T, S extends IOBaseStream<T, S, B>, B extends BaseStream<T, B>>
extends Closeable {
    default public BaseStream<T, B> asBaseStream() {
        return new UncheckedIOBaseStream(this);
    }

    @Override
    default public void close() {
        this.unwrap().close();
    }

    default public boolean isParallel() {
        return this.unwrap().isParallel();
    }

    default public IOIterator<T> iterator() {
        return IOIteratorAdapter.adapt(this.unwrap().iterator());
    }

    default public S onClose(IORunnable closeHandler) throws IOException {
        return this.wrap(this.unwrap().onClose(() -> Erase.run(closeHandler)));
    }

    default public S parallel() {
        return (S)(this.isParallel() ? this : this.wrap(this.unwrap().parallel()));
    }

    default public S sequential() {
        return (S)(this.isParallel() ? this.wrap(this.unwrap().sequential()) : this);
    }

    default public IOSpliterator<T> spliterator() {
        return IOSpliteratorAdapter.adapt(this.unwrap().spliterator());
    }

    default public S unordered() {
        return this.wrap(this.unwrap().unordered());
    }

    public B unwrap();

    public S wrap(B var1);
}

