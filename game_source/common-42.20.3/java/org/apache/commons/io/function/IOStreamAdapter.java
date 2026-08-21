/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import java.util.stream.Stream;
import org.apache.commons.io.function.IOBaseStreamAdapter;
import org.apache.commons.io.function.IOStream;

final class IOStreamAdapter<T>
extends IOBaseStreamAdapter<T, IOStream<T>, Stream<T>>
implements IOStream<T> {
    static <T> IOStream<T> adapt(Stream<T> delegate) {
        return delegate != null ? new IOStreamAdapter<T>(delegate) : IOStream.empty();
    }

    private IOStreamAdapter(Stream<T> delegate) {
        super(delegate);
    }

    @Override
    public IOStream<T> wrap(Stream<T> delegate) {
        return this.unwrap() == delegate ? this : IOStreamAdapter.adapt(delegate);
    }
}

