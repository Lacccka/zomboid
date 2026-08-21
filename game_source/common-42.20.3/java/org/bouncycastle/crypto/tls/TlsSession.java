/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.crypto.tls;

import org.bouncycastle.crypto.tls.SessionParameters;

public interface TlsSession {
    public SessionParameters exportSessionParameters();

    public byte[] getSessionID();

    public void invalidate();

    public boolean isResumable();
}

