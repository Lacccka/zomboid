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
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public class SchemaTypeTransducer<V>
extends FilterTransducer<V> {
    private final QName schemaType;

    public SchemaTypeTransducer(Transducer<V> core, QName schemaType) {
        super(core);
        this.schemaType = schemaType;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public CharSequence print(V o) throws AccessorException {
        XMLSerializer w = XMLSerializer.getInstance();
        QName old = w.setSchemaType(this.schemaType);
        try {
            CharSequence charSequence = this.core.print(o);
            return charSequence;
        }
        finally {
            w.setSchemaType(old);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void writeText(XMLSerializer w, V o, String fieldName) throws IOException, SAXException, XMLStreamException, AccessorException {
        QName old = w.setSchemaType(this.schemaType);
        try {
            this.core.writeText(w, o, fieldName);
        }
        finally {
            w.setSchemaType(old);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void writeLeafElement(XMLSerializer w, Name tagName, V o, String fieldName) throws IOException, SAXException, XMLStreamException, AccessorException {
        QName old = w.setSchemaType(this.schemaType);
        try {
            this.core.writeLeafElement(w, tagName, o, fieldName);
        }
        finally {
            w.setSchemaType(old);
        }
    }
}

