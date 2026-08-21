/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.v2.runtime.unmarshaller.StAXConnector;
import com.sun.xml.bind.v2.runtime.unmarshaller.XmlVisitor;
import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

final class StAXEventConnector
extends StAXConnector {
    private final XMLEventReader staxEventReader;
    private XMLEvent event;
    private final AttributesImpl attrs = new AttributesImpl();
    private final StringBuilder buffer = new StringBuilder();
    private boolean seenText;

    public StAXEventConnector(XMLEventReader staxCore, XmlVisitor visitor) {
        super(visitor);
        this.staxEventReader = staxCore;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public void bridge() throws XMLStreamException {
        try {
            int depth = 0;
            this.event = this.staxEventReader.peek();
            if (!this.event.isStartDocument() && !this.event.isStartElement()) {
                throw new IllegalStateException();
            }
            do {
                this.event = this.staxEventReader.nextEvent();
            } while (!this.event.isStartElement());
            this.handleStartDocument(this.event.asStartElement().getNamespaceContext());
            block8: while (true) {
                switch (this.event.getEventType()) {
                    case 1: {
                        this.handleStartElement(this.event.asStartElement());
                        ++depth;
                        break;
                    }
                    case 2: {
                        this.handleEndElement(this.event.asEndElement());
                        if (--depth != 0) break;
                        break block8;
                    }
                    case 4: 
                    case 6: 
                    case 12: {
                        this.handleCharacters(this.event.asCharacters());
                    }
                }
                this.event = this.staxEventReader.nextEvent();
            }
            this.handleEndDocument();
            this.event = null;
            return;
        }
        catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    @Override
    protected Location getCurrentLocation() {
        return this.event.getLocation();
    }

    @Override
    protected String getCurrentQName() {
        QName qName = this.event.isEndElement() ? this.event.asEndElement().getName() : this.event.asStartElement().getName();
        return this.getQName(qName.getPrefix(), qName.getLocalPart());
    }

    private void handleCharacters(Characters event) throws SAXException, XMLStreamException {
        XMLEvent next;
        if (!this.predictor.expectText()) {
            return;
        }
        this.seenText = true;
        while (this.isIgnorable(next = this.staxEventReader.peek())) {
            this.staxEventReader.nextEvent();
        }
        if (this.isTag(next)) {
            this.visitor.text(event.getData());
            return;
        }
        this.buffer.append(event.getData());
        while (true) {
            if (this.isIgnorable(next = this.staxEventReader.peek())) {
                this.staxEventReader.nextEvent();
                continue;
            }
            if (this.isTag(next)) {
                this.visitor.text(this.buffer);
                this.buffer.setLength(0);
                return;
            }
            this.buffer.append(next.asCharacters().getData());
            this.staxEventReader.nextEvent();
        }
    }

    private boolean isTag(XMLEvent event) {
        int eventType = event.getEventType();
        return eventType == 1 || eventType == 2;
    }

    private boolean isIgnorable(XMLEvent event) {
        int eventType = event.getEventType();
        return eventType == 5 || eventType == 3;
    }

    private void handleEndElement(EndElement event) throws SAXException {
        if (!this.seenText && this.predictor.expectText()) {
            this.visitor.text("");
        }
        QName qName = event.getName();
        this.tagName.uri = StAXEventConnector.fixNull(qName.getNamespaceURI());
        this.tagName.local = qName.getLocalPart();
        this.visitor.endElement(this.tagName);
        Iterator<Namespace> i = event.getNamespaces();
        while (i.hasNext()) {
            String prefix = StAXEventConnector.fixNull(i.next().getPrefix());
            this.visitor.endPrefixMapping(prefix);
        }
        this.seenText = false;
    }

    private void handleStartElement(StartElement event) throws SAXException {
        Iterator<Namespace> i = event.getNamespaces();
        while (i.hasNext()) {
            Namespace ns = i.next();
            this.visitor.startPrefixMapping(StAXEventConnector.fixNull(ns.getPrefix()), StAXEventConnector.fixNull(ns.getNamespaceURI()));
        }
        QName qName = event.getName();
        this.tagName.uri = StAXEventConnector.fixNull(qName.getNamespaceURI());
        String localName = qName.getLocalPart();
        this.tagName.uri = StAXEventConnector.fixNull(qName.getNamespaceURI());
        this.tagName.local = localName;
        this.tagName.atts = this.getAttributes(event);
        this.visitor.startElement(this.tagName);
        this.seenText = false;
    }

    private Attributes getAttributes(StartElement event) {
        this.attrs.clear();
        Iterator<Attribute> i = event.getAttributes();
        while (i.hasNext()) {
            Attribute staxAttr = i.next();
            QName name = staxAttr.getName();
            String uri = StAXEventConnector.fixNull(name.getNamespaceURI());
            String localName = name.getLocalPart();
            String prefix = name.getPrefix();
            String qName = prefix == null || prefix.length() == 0 ? localName : prefix + ':' + localName;
            String type = staxAttr.getDTDType();
            String value = staxAttr.getValue();
            this.attrs.addAttribute(uri, localName, qName, type, value);
        }
        return this.attrs;
    }
}

