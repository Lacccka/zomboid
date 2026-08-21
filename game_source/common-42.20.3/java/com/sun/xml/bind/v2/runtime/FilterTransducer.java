/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime;

import com.sun.istack.NotNull;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public abstract class FilterTransducer<T>
implements Transducer<T> {
    protected final Transducer<T> core;

    protected FilterTransducer(Transducer<T> core) {
        this.core = core;
    }

    @Override
    public boolean useNamespace() {
        return this.core.useNamespace();
    }

    @Override
    public void declareNamespace(T o, XMLSerializer w) throws AccessorException {
        this.core.declareNamespace(o, w);
    }

    @Override
    @NotNull
    public CharSequence print(@NotNull T o) throws AccessorException {
        return this.core.print(o);
    }

    @Override
    public T parse(CharSequence lexical) throws AccessorException, SAXException {
        return this.core.parse(lexical);
    }

    @Override
    public void writeText(XMLSerializer w, T o, String fieldName) throws IOException, SAXException, XMLStreamException, AccessorException {
        this.core.writeText(w, o, fieldName);
    }

    @Override
    public void writeLeafElement(XMLSerializer w, Name tagName, T o, String fieldName) throws IOException, SAXException, XMLStreamException, AccessorException {
        this.core.writeLeafElement(w, tagName, o, fieldName);
    }

    @Override
    public QName getTypeName(T instance) {
        return null;
    }
}

