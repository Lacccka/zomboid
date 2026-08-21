/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.crypto.tls;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import org.bouncycastle.asn1.ocsp.ResponderID;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.crypto.tls.TlsUtils;
import org.bouncycastle.util.io.Streams;

public class OCSPStatusRequest {
    protected Vector responderIDList;
    protected Extensions requestExtensions;

    public OCSPStatusRequest(Vector vector, Extensions extensions) {
        this.responderIDList = vector;
        this.requestExtensions = extensions;
    }

    public Vector getResponderIDList() {
        return this.responderIDList;
    }

    public Extensions getRequestExtensions() {
        return this.requestExtensions;
    }

    public void encode(OutputStream outputStream2) throws IOException {
        Object object;
        if (this.responderIDList == null || this.responderIDList.isEmpty()) {
            TlsUtils.writeUint16(0, outputStream2);
        } else {
            object = new ByteArrayOutputStream();
            for (int i = 0; i < this.responderIDList.size(); ++i) {
                ResponderID responderID = (ResponderID)this.responderIDList.elementAt(i);
                byte[] byArray = responderID.getEncoded("DER");
                TlsUtils.writeOpaque16(byArray, (OutputStream)object);
            }
            TlsUtils.checkUint16(((ByteArrayOutputStream)object).size());
            TlsUtils.writeUint16(((ByteArrayOutputStream)object).size(), outputStream2);
            Streams.writeBufTo((ByteArrayOutputStream)object, outputStream2);
        }
        if (this.requestExtensions == null) {
            TlsUtils.writeUint16(0, outputStream2);
        } else {
            object = this.requestExtensions.getEncoded("DER");
            TlsUtils.checkUint16(((Object)object).length);
            TlsUtils.writeUint16(((Object)object).length, outputStream2);
            outputStream2.write((byte[])object);
        }
    }

    public static OCSPStatusRequest parse(InputStream inputStream2) throws IOException {
        Object object;
        Vector<ResponderID> vector = new Vector<ResponderID>();
        int n = TlsUtils.readUint16(inputStream2);
        if (n > 0) {
            byte[] byArray = TlsUtils.readFully(n, inputStream2);
            object = new ByteArrayInputStream(byArray);
            do {
                byte[] byArray2 = TlsUtils.readOpaque16((InputStream)object);
                ResponderID responderID = ResponderID.getInstance(TlsUtils.readDERObject(byArray2));
                vector.addElement(responderID);
            } while (((ByteArrayInputStream)object).available() > 0);
        }
        Extensions extensions = null;
        int n2 = TlsUtils.readUint16(inputStream2);
        if (n2 > 0) {
            object = TlsUtils.readFully(n2, inputStream2);
            extensions = Extensions.getInstance(TlsUtils.readDERObject((byte[])object));
        }
        return new OCSPStatusRequest(vector, extensions);
    }
}

