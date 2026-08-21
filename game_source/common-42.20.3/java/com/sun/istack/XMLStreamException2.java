/*
 * Decompiled with CFR 0.152.
 */
package com.sun.istack;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

public class XMLStreamException2
extends XMLStreamException {
    public XMLStreamException2(String msg) {
        super(msg);
    }

    public XMLStreamException2(Throwable th) {
        super(th);
    }

    public XMLStreamException2(String msg, Throwable th) {
        super(msg, th);
    }

    public XMLStreamException2(String msg, Location location) {
        super(msg, location);
    }

    public XMLStreamException2(String msg, Location location, Throwable th) {
        super(msg, location, th);
    }

    @Override
    public Throwable getCause() {
        return this.getNestedException();
    }
}

