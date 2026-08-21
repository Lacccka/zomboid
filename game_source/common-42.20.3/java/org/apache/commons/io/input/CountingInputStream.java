/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.input.ProxyInputStream;

@Deprecated
public class CountingInputStream
extends ProxyInputStream {
    private long count;

    public CountingInputStream(InputStream in) {
        super(in);
    }

    @Override
    protected synchronized void afterRead(int n) throws IOException {
        if (n != -1) {
            this.count += (long)n;
        }
    }

    public synchronized long getByteCount() {
        return this.count;
    }

    @Deprecated
    public int getCount() {
        long result = this.getByteCount();
        if (result > Integer.MAX_VALUE) {
            throw new ArithmeticException("The byte count " + result + " is too large to be converted to an int");
        }
        return (int)result;
    }

    public synchronized long resetByteCount() {
        long tmp = this.count;
        this.count = 0L;
        return tmp;
    }

    @Deprecated
    public int resetCount() {
        long result = this.resetByteCount();
        if (result > Integer.MAX_VALUE) {
            throw new ArithmeticException("The byte count " + result + " is too large to be converted to an int");
        }
        return (int)result;
    }

    @Override
    public synchronized long skip(long length) throws IOException {
        long skip = super.skip(length);
        this.count += skip;
        return skip;
    }
}

