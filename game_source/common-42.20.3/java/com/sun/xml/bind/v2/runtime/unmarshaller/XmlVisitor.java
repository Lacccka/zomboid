/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx;
import com.sun.xml.bind.v2.runtime.unmarshaller.TagName;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import javax.xml.namespace.NamespaceContext;
import org.xml.sax.SAXException;

public interface XmlVisitor {
    public void startDocument(LocatorEx var1, NamespaceContext var2) throws SAXException;

    public void endDocument() throws SAXException;

    public void startElement(TagName var1) throws SAXException;

    public void endElement(TagName var1) throws SAXException;

    public void startPrefixMapping(String var1, String var2) throws SAXException;

    public void endPrefixMapping(String var1) throws SAXException;

    public void text(CharSequence var1) throws SAXException;

    public UnmarshallingContext getContext();

    public TextPredictor getPredictor();

    public static interface TextPredictor {
        public boolean expectText();
    }
}

