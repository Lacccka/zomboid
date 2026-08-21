/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.output;

import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl;
import com.sun.xml.bind.v2.runtime.output.Pcdata;
import com.sun.xml.bind.v2.runtime.output.XmlOutput;
import com.sun.xml.bind.v2.runtime.output.XmlOutputAbstractImpl;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public final class ForkXmlOutput
extends XmlOutputAbstractImpl {
    private final XmlOutput lhs;
    private final XmlOutput rhs;

    public ForkXmlOutput(XmlOutput lhs, XmlOutput rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public void startDocument(XMLSerializer serializer, boolean fragment, int[] nsUriIndex2prefixIndex, NamespaceContextImpl nsContext) throws IOException, SAXException, XMLStreamException {
        this.lhs.startDocument(serializer, fragment, nsUriIndex2prefixIndex, nsContext);
        this.rhs.startDocument(serializer, fragment, nsUriIndex2prefixIndex, nsContext);
    }

    @Override
    public void endDocument(boolean fragment) throws IOException, SAXException, XMLStreamException {
        this.lhs.endDocument(fragment);
        this.rhs.endDocument(fragment);
    }

    @Override
    public void beginStartTag(Name name) throws IOException, XMLStreamException {
        this.lhs.beginStartTag(name);
        this.rhs.beginStartTag(name);
    }

    @Override
    public void attribute(Name name, String value) throws IOException, XMLStreamException {
        this.lhs.attribute(name, value);
        this.rhs.attribute(name, value);
    }

    @Override
    public void endTag(Name name) throws IOException, SAXException, XMLStreamException {
        this.lhs.endTag(name);
        this.rhs.endTag(name);
    }

    @Override
    public void beginStartTag(int prefix, String localName) throws IOException, XMLStreamException {
        this.lhs.beginStartTag(prefix, localName);
        this.rhs.beginStartTag(prefix, localName);
    }

    @Override
    public void attribute(int prefix, String localName, String value) throws IOException, XMLStreamException {
        this.lhs.attribute(prefix, localName, value);
        this.rhs.attribute(prefix, localName, value);
    }

    @Override
    public void endStartTag() throws IOException, SAXException {
        this.lhs.endStartTag();
        this.rhs.endStartTag();
    }

    @Override
    public void endTag(int prefix, String localName) throws IOException, SAXException, XMLStreamException {
        this.lhs.endTag(prefix, localName);
        this.rhs.endTag(prefix, localName);
    }

    @Override
    public void text(String value, boolean needsSeparatingWhitespace) throws IOException, SAXException, XMLStreamException {
        this.lhs.text(value, needsSeparatingWhitespace);
        this.rhs.text(value, needsSeparatingWhitespace);
    }

    @Override
    public void text(Pcdata value, boolean needsSeparatingWhitespace) throws IOException, SAXException, XMLStreamException {
        this.lhs.text(value, needsSeparatingWhitespace);
        this.rhs.text(value, needsSeparatingWhitespace);
    }
}

