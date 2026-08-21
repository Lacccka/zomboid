/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.output;

import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl;
import com.sun.xml.bind.v2.runtime.output.Pcdata;
import com.sun.xml.bind.v2.runtime.output.XmlOutputAbstractImpl;
import java.io.IOException;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import org.xml.sax.SAXException;

public class XMLEventWriterOutput
extends XmlOutputAbstractImpl {
    private final XMLEventWriter out;
    private final XMLEventFactory ef;
    private final Characters sp;

    public XMLEventWriterOutput(XMLEventWriter out) {
        this.out = out;
        this.ef = XMLEventFactory.newInstance();
        this.sp = this.ef.createCharacters(" ");
    }

    @Override
    public void startDocument(XMLSerializer serializer, boolean fragment, int[] nsUriIndex2prefixIndex, NamespaceContextImpl nsContext) throws IOException, SAXException, XMLStreamException {
        super.startDocument(serializer, fragment, nsUriIndex2prefixIndex, nsContext);
        if (!fragment) {
            this.out.add(this.ef.createStartDocument());
        }
    }

    @Override
    public void endDocument(boolean fragment) throws IOException, SAXException, XMLStreamException {
        if (!fragment) {
            this.out.add(this.ef.createEndDocument());
            this.out.flush();
        }
        super.endDocument(fragment);
    }

    @Override
    public void beginStartTag(int prefix, String localName) throws IOException, XMLStreamException {
        this.out.add(this.ef.createStartElement(this.nsContext.getPrefix(prefix), this.nsContext.getNamespaceURI(prefix), localName));
        NamespaceContextImpl.Element nse = this.nsContext.getCurrent();
        if (nse.count() > 0) {
            for (int i = nse.count() - 1; i >= 0; --i) {
                String uri = nse.getNsUri(i);
                if (uri.length() == 0 && nse.getBase() == 1) continue;
                this.out.add(this.ef.createNamespace(nse.getPrefix(i), uri));
            }
        }
    }

    @Override
    public void attribute(int prefix, String localName, String value) throws IOException, XMLStreamException {
        Attribute att = prefix == -1 ? this.ef.createAttribute(localName, value) : this.ef.createAttribute(this.nsContext.getPrefix(prefix), this.nsContext.getNamespaceURI(prefix), localName, value);
        this.out.add(att);
    }

    @Override
    public void endStartTag() throws IOException, SAXException {
    }

    @Override
    public void endTag(int prefix, String localName) throws IOException, SAXException, XMLStreamException {
        this.out.add(this.ef.createEndElement(this.nsContext.getPrefix(prefix), this.nsContext.getNamespaceURI(prefix), localName));
    }

    @Override
    public void text(String value, boolean needsSeparatingWhitespace) throws IOException, SAXException, XMLStreamException {
        if (needsSeparatingWhitespace) {
            this.out.add(this.sp);
        }
        this.out.add(this.ef.createCharacters(value));
    }

    @Override
    public void text(Pcdata value, boolean needsSeparatingWhitespace) throws IOException, SAXException, XMLStreamException {
        this.text(value.toString(), needsSeparatingWhitespace);
    }
}

