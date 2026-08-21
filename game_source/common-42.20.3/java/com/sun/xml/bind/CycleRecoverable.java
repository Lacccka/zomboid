/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind;

import javax.xml.bind.Marshaller;

public interface CycleRecoverable {
    public Object onCycleDetected(Context var1);

    public static interface Context {
        public Marshaller getMarshaller();
    }
}

