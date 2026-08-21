/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.jcajce.provider.config;

import java.io.OutputStream;
import java.security.KeyStore;

public class PKCS12StoreParameter
extends org.bouncycastle.jcajce.PKCS12StoreParameter {
    public PKCS12StoreParameter(OutputStream outputStream2, char[] cArray) {
        super(outputStream2, cArray, false);
    }

    public PKCS12StoreParameter(OutputStream outputStream2, KeyStore.ProtectionParameter protectionParameter) {
        super(outputStream2, protectionParameter, false);
    }

    public PKCS12StoreParameter(OutputStream outputStream2, char[] cArray, boolean bl) {
        super(outputStream2, new KeyStore.PasswordProtection(cArray), bl);
    }

    public PKCS12StoreParameter(OutputStream outputStream2, KeyStore.ProtectionParameter protectionParameter, boolean bl) {
        super(outputStream2, protectionParameter, bl);
    }
}

