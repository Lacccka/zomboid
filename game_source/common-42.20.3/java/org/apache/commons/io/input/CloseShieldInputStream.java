/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.InputStream;
import org.apache.commons.io.input.ClosedInputStream;
import org.apache.commons.io.input.ProxyInputStream;

public class CloseShieldInputStream
extends ProxyInputStream {
    public static CloseShieldInputStream wrap(InputStream inputStream2) {
        return new CloseShieldInputStream(inputStream2);
    }

    @Deprecated
    public CloseShieldInputStream(InputStream inputStream2) {
        super(inputStream2);
    }

    @Override
    public void close() {
        this.in = ClosedInputStream.INSTANCE;
    }
}

