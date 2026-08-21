/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.input.ClosedInputStream;
import org.apache.commons.io.input.ProxyInputStream;

public class AutoCloseInputStream
extends ProxyInputStream {
    public static Builder builder() {
        return new Builder();
    }

    @Deprecated
    public AutoCloseInputStream(InputStream in) {
        super(in);
    }

    @Override
    protected void afterRead(int n) throws IOException {
        if (n == -1) {
            this.close();
        }
    }

    @Override
    public void close() throws IOException {
        this.in.close();
        this.in = ClosedInputStream.INSTANCE;
    }

    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

    public static class Builder
    extends AbstractStreamBuilder<AutoCloseInputStream, Builder> {
        @Override
        public AutoCloseInputStream get() throws IOException {
            return new AutoCloseInputStream(this.getInputStream());
        }
    }
}

