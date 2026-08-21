/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.crypto.tls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.bouncycastle.crypto.tls.TlsUtils;

public class NewSessionTicket {
    protected long ticketLifetimeHint;
    protected byte[] ticket;

    public NewSessionTicket(long l, byte[] byArray) {
        this.ticketLifetimeHint = l;
        this.ticket = byArray;
    }

    public long getTicketLifetimeHint() {
        return this.ticketLifetimeHint;
    }

    public byte[] getTicket() {
        return this.ticket;
    }

    public void encode(OutputStream outputStream2) throws IOException {
        TlsUtils.writeUint32(this.ticketLifetimeHint, outputStream2);
        TlsUtils.writeOpaque16(this.ticket, outputStream2);
    }

    public static NewSessionTicket parse(InputStream inputStream2) throws IOException {
        long l = TlsUtils.readUint32(inputStream2);
        byte[] byArray = TlsUtils.readOpaque16(inputStream2);
        return new NewSessionTicket(l, byArray);
    }
}

