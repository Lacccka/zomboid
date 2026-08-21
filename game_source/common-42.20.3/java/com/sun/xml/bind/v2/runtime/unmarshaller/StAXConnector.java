/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx;
import com.sun.xml.bind.v2.runtime.unmarshaller.TagName;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.XmlVisitor;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

abstract class StAXConnector {
    protected final XmlVisitor visitor;
    protected final UnmarshallingContext context;
    protected final XmlVisitor.TextPredictor predictor;
    protected final TagName tagName = new TagNameImpl();

    public abstract void bridge() throws XMLStreamException;

    protected StAXConnector(XmlVisitor visitor) {
        this.visitor = visitor;
        this.context = visitor.getContext();
        this.predictor = visitor.getPredictor();
    }

    protected abstract Location getCurrentLocation();

    protected abstract String getCurrentQName();

    protected final void handleStartDocument(NamespaceContext nsc) throws SAXException {
        this.visitor.startDocument(new LocatorEx(){

            @Override
            public ValidationEventLocator getLocation() {
                return new ValidationEventLocatorImpl(this);
            }

            @Override
            public int getColumnNumber() {
                return StAXConnector.this.getCurrentLocation().getColumnNumber();
            }

            @Override
            public int getLineNumber() {
                return StAXConnector.this.getCurrentLocation().getLineNumber();
            }

            @Override
            public String getPublicId() {
                return StAXConnector.this.getCurrentLocation().getPublicId();
            }

            @Override
            public String getSystemId() {
                return StAXConnector.this.getCurrentLocation().getSystemId();
            }
        }, nsc);
    }

    protected final void handleEndDocument() throws SAXException {
        this.visitor.endDocument();
    }

    protected static String fixNull(String s) {
        if (s == null) {
            return "";
        }
        return s;
    }

    protected final String getQName(String prefix, String localName) {
        if (prefix == null || prefix.length() == 0) {
            return localName;
        }
        return prefix + ':' + localName;
    }

    private final class TagNameImpl
    extends TagName {
        private TagNameImpl() {
        }

        @Override
        public String getQname() {
            return StAXConnector.this.getCurrentQName();
        }
    }
}

