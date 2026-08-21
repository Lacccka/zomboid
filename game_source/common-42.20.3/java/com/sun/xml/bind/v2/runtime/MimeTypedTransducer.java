/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.FilterTransducer;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import java.io.IOException;
import javax.activation.MimeType;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public final class MimeTypedTransducer<V>
extends FilterTransducer<V> {
    private final MimeType expectedMimeType;

    public MimeTypedTransducer(Transducer<V> core, MimeType expectedMimeType) {
        super(core);
        this.expectedMimeType = expectedMimeType;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public CharSequence print(V o) throws AccessorException {
        XMLSerializer w = XMLSerializer.getInstance();
        MimeType old = w.setExpectedMimeType(this.expectedMimeType);
        try {
            CharSequence charSequence = this.core.print(o);
            return charSequence;
        }
        finally {
            w.setExpectedMimeType(old);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void writeText(XMLSerializer w, V o, String fieldName) throws IOException, SAXException, XMLStreamException, AccessorException {
        MimeType old = w.setExpectedMimeType(this.expectedMimeType);
        try {
            this.core.writeText(w, o, fieldName);
        }
        finally {
            w.setExpectedMimeType(old);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void writeLeafElement(XMLSerializer w, Name tagName, V o, String fieldName) throws IOException, SAXException, XMLStreamException, AccessorException {
        MimeType old = w.setExpectedMimeType(this.expectedMimeType);
        try {
            this.core.writeLeafElement(w, tagName, o, fieldName);
        }
        finally {
            w.setExpectedMimeType(old);
        }
    }
}

