/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.crypto.tls;

import org.bouncycastle.crypto.tls.CertificateRequest;
import org.bouncycastle.crypto.tls.TlsAuthentication;
import org.bouncycastle.crypto.tls.TlsCredentials;

public abstract class ServerOnlyTlsAuthentication
implements TlsAuthentication {
    public final TlsCredentials getClientCredentials(CertificateRequest certificateRequest) {
        return null;
    }
}

