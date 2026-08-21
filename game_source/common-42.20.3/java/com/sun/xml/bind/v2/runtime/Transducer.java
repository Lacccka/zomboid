/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime;

import com.sun.istack.NotNull;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public interface Transducer<ValueT> {
    public boolean useNamespace();

    public void declareNamespace(ValueT var1, XMLSerializer var2) throws AccessorException;

    @NotNull
    public CharSequence print(@NotNull ValueT var1) throws AccessorException;

    public ValueT parse(CharSequence var1) throws AccessorException, SAXException;

    public void writeText(XMLSerializer var1, ValueT var2, String var3) throws IOException, SAXException, XMLStreamException, AccessorException;

    public void writeLeafElement(XMLSerializer var1, Name var2, @NotNull ValueT var3, String var4) throws IOException, SAXException, XMLStreamException, AccessorException;

    public QName getTypeName(@NotNull ValueT var1);
}

