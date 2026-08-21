/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor;
import com.sun.xml.bind.v2.runtime.reflect.opt.Bean;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public final class TransducedAccessor_field_Integer
extends DefaultTransducedAccessor {
    @Override
    public String print(Object o) {
        return DatatypeConverterImpl._printInt(((Bean)o).f_int);
    }

    @Override
    public void parse(Object o, CharSequence lexical) {
        ((Bean)o).f_int = DatatypeConverterImpl._parseInt(lexical);
    }

    @Override
    public boolean hasValue(Object o) {
        return true;
    }

    @Override
    public void writeLeafElement(XMLSerializer w, Name tagName, Object o, String fieldName) throws SAXException, AccessorException, IOException, XMLStreamException {
        w.leafElement(tagName, ((Bean)o).f_int, fieldName);
    }
}

