/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx;
import com.sun.xml.bind.v2.runtime.unmarshaller.TagName;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.XmlVisitor;
import javax.xml.namespace.NamespaceContext;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public final class InterningXmlVisitor
implements XmlVisitor {
    private final XmlVisitor next;
    private final AttributesImpl attributes = new AttributesImpl();

    public InterningXmlVisitor(XmlVisitor next) {
        this.next = next;
    }

    @Override
    public void startDocument(LocatorEx locator, NamespaceContext nsContext) throws SAXException {
        this.next.startDocument(locator, nsContext);
    }

    @Override
    public void endDocument() throws SAXException {
        this.next.endDocument();
    }

    @Override
    public void startElement(TagName tagName) throws SAXException {
        this.attributes.setAttributes(tagName.atts);
        tagName.atts = this.attributes;
        tagName.uri = InterningXmlVisitor.intern(tagName.uri);
        tagName.local = InterningXmlVisitor.intern(tagName.local);
        this.next.startElement(tagName);
    }

    @Override
    public void endElement(TagName tagName) throws SAXException {
        tagName.uri = InterningXmlVisitor.intern(tagName.uri);
        tagName.local = InterningXmlVisitor.intern(tagName.local);
        this.next.endElement(tagName);
    }

    @Override
    public void startPrefixMapping(String prefix, String nsUri) throws SAXException {
        this.next.startPrefixMapping(InterningXmlVisitor.intern(prefix), InterningXmlVisitor.intern(nsUri));
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        this.next.endPrefixMapping(InterningXmlVisitor.intern(prefix));
    }

    @Override
    public void text(CharSequence pcdata) throws SAXException {
        this.next.text(pcdata);
    }

    @Override
    public UnmarshallingContext getContext() {
        return this.next.getContext();
    }

    @Override
    public XmlVisitor.TextPredictor getPredictor() {
        return this.next.getPredictor();
    }

    private static String intern(String s) {
        if (s == null) {
            return null;
        }
        return s.intern();
    }

    private static class AttributesImpl
    implements Attributes {
        private Attributes core;

        private AttributesImpl() {
        }

        void setAttributes(Attributes att) {
            this.core = att;
        }

        @Override
        public int getIndex(String qName) {
            return this.core.getIndex(qName);
        }

        @Override
        public int getIndex(String uri, String localName) {
            return this.core.getIndex(uri, localName);
        }

        @Override
        public int getLength() {
            return this.core.getLength();
        }

        @Override
        public String getLocalName(int index) {
            return InterningXmlVisitor.intern(this.core.getLocalName(index));
        }

        @Override
        public String getQName(int index) {
            return InterningXmlVisitor.intern(this.core.getQName(index));
        }

        @Override
        public String getType(int index) {
            return InterningXmlVisitor.intern(this.core.getType(index));
        }

        @Override
        public String getType(String qName) {
            return InterningXmlVisitor.intern(this.core.getType(qName));
        }

        @Override
        public String getType(String uri, String localName) {
            return InterningXmlVisitor.intern(this.core.getType(uri, localName));
        }

        @Override
        public String getURI(int index) {
            return InterningXmlVisitor.intern(this.core.getURI(index));
        }

        @Override
        public String getValue(int index) {
            return this.core.getValue(index);
        }

        @Override
        public String getValue(String qName) {
            return this.core.getValue(qName);
        }

        @Override
        public String getValue(String uri, String localName) {
            return this.core.getValue(uri, localName);
        }
    }
}

