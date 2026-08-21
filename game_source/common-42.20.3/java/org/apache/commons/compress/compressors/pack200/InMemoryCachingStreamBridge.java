/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.pack200;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.compress.compressors.pack200.AbstractStreamBridge;

final class InMemoryCachingStreamBridge
extends AbstractStreamBridge {
    InMemoryCachingStreamBridge() {
        super(new ByteArrayOutputStream());
    }

    @Override
    InputStream createInputStream() throws IOException {
        return new ByteArrayInputStream(((ByteArrayOutputStream)this.out).toByteArray());
    }
}

