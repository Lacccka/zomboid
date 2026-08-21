/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime;

import com.sun.istack.FinalArrayList;
import com.sun.istack.SAXException2;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

final class ContentHandlerAdaptor
extends DefaultHandler {
    private final FinalArrayList<String> prefixMap = new FinalArrayList();
    private final XMLSerializer serializer;
    private final StringBuffer text = new StringBuffer();

    ContentHandlerAdaptor(XMLSerializer _serializer) {
        this.serializer = _serializer;
    }

    @Override
    public void startDocument() {
        this.prefixMap.clear();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) {
        this.prefixMap.add(prefix);
        this.prefixMap.add(uri);
    }

    private boolean containsPrefixMapping(String prefix, String uri) {
        for (int i = 0; i < this.prefixMap.size(); i += 2) {
            if (!((String)this.prefixMap.get(i)).equals(prefix) || !((String)this.prefixMap.get(i + 1)).equals(uri)) continue;
            return true;
        }
        return false;
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        try {
            int i;
            this.flushText();
            int len = atts.getLength();
            String p = this.getPrefix(qName);
            if (this.containsPrefixMapping(p, namespaceURI)) {
                this.serializer.startElementForce(namespaceURI, localName, p, null);
            } else {
                this.serializer.startElement(namespaceURI, localName, p, null);
            }
            for (i = 0; i < this.prefixMap.size(); i += 2) {
                this.serializer.getNamespaceContext().force((String)this.prefixMap.get(i + 1), (String)this.prefixMap.get(i));
            }
            for (i = 0; i < len; ++i) {
                String qname = atts.getQName(i);
                if (qname.startsWith("xmlns") || atts.getURI(i).length() == 0) continue;
                String prefix = this.getPrefix(qname);
                this.serializer.getNamespaceContext().declareNamespace(atts.getURI(i), prefix, true);
            }
            this.serializer.endNamespaceDecls(null);
            for (i = 0; i < len; ++i) {
                if (atts.getQName(i).startsWith("xmlns")) continue;
                this.serializer.attribute(atts.getURI(i), atts.getLocalName(i), atts.getValue(i));
            }
            this.prefixMap.clear();
            this.serializer.endAttributes();
        }
        catch (IOException e) {
            throw new SAXException2(e);
        }
        catch (XMLStreamException e) {
            throw new SAXException2(e);
        }
    }

    private String getPrefix(String qname) {
        int idx = qname.indexOf(58);
        String prefix = idx == -1 ? "" : qname.substring(0, idx);
        return prefix;
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        try {
            this.flushText();
            this.serializer.endElement();
        }
        catch (IOException e) {
            throw new SAXException2(e);
        }
        catch (XMLStreamException e) {
            throw new SAXException2(e);
        }
    }

    private void flushText() throws SAXException, IOException, XMLStreamException {
        if (this.text.length() != 0) {
            this.serializer.text(this.text.toString(), null);
            this.text.setLength(0);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        this.text.append(ch, start, length);
    }
}

