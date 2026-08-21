/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind;

import javax.xml.bind.JAXBException;

public class MarshalException
extends JAXBException {
    public MarshalException(String message) {
        this(message, null, null);
    }

    public MarshalException(String message, String errorCode) {
        this(message, errorCode, null);
    }

    public MarshalException(Throwable exception) {
        this(null, null, exception);
    }

    public MarshalException(String message, Throwable exception) {
        this(message, null, exception);
    }

    public MarshalException(String message, String errorCode, Throwable exception) {
        super(message, errorCode, exception);
    }
}

