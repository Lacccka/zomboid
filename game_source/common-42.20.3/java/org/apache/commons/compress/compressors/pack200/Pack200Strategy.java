/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.pack200;

import java.io.IOException;
import org.apache.commons.compress.compressors.pack200.AbstractStreamBridge;
import org.apache.commons.compress.compressors.pack200.InMemoryCachingStreamBridge;
import org.apache.commons.compress.compressors.pack200.TempFileCachingStreamBridge;

public enum Pack200Strategy {
    IN_MEMORY{

        @Override
        AbstractStreamBridge newStreamBridge() {
            return new InMemoryCachingStreamBridge();
        }
    }
    ,
    TEMP_FILE{

        @Override
        AbstractStreamBridge newStreamBridge() throws IOException {
            return new TempFileCachingStreamBridge();
        }
    };


    abstract AbstractStreamBridge newStreamBridge() throws IOException;
}

