/*
 * Decompiled with CFR 0.152.
 */
package com.sun.istack;

import org.xml.sax.SAXException;

public class SAXException2
extends SAXException {
    public SAXException2(String message) {
        super(message);
    }

    public SAXException2(Exception e) {
        super(e);
    }

    public SAXException2(String message, Exception e) {
        super(message, e);
    }

    @Override
    public Throwable getCause() {
        return this.getException();
    }
}

