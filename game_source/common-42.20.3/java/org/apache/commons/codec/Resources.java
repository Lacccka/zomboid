/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.codec;

import java.io.InputStream;

public class Resources {
    public static InputStream getInputStream(String name) {
        InputStream inputStream2 = Resources.class.getClassLoader().getResourceAsStream(name);
        if (inputStream2 == null) {
            throw new IllegalArgumentException("Unable to resolve required resource: " + name);
        }
        return inputStream2;
    }

    @Deprecated
    public Resources() {
    }
}

