/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

abstract class InternalBridge<T>
extends Bridge<T> {
    protected InternalBridge(JAXBContextImpl context) {
        super(context);
    }

    @Override
    public JAXBContextImpl getContext() {
        return this.context;
    }

    abstract void marshal(T var1, XMLSerializer var2) throws IOException, SAXException, XMLStreamException;
}

