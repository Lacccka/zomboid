/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.WhiteSpaceProcessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.InterningXmlVisitor;
import com.sun.xml.bind.v2.runtime.unmarshaller.StAXConnector;
import com.sun.xml.bind.v2.runtime.unmarshaller.XmlVisitor;
import java.lang.reflect.Constructor;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class StAXStreamConnector
extends StAXConnector {
    private final XMLStreamReader staxStreamReader;
    protected final StringBuilder buffer = new StringBuilder();
    protected boolean textReported = false;
    private final Attributes attributes = new Attributes(){

        @Override
        public int getLength() {
            return StAXStreamConnector.this.staxStreamReader.getAttributeCount();
        }

        @Override
        public String getURI(int index) {
            String uri = StAXStreamConnector.this.staxStreamReader.getAttributeNamespace(index);
            if (uri == null) {
                return "";
            }
            return uri;
        }

        @Override
        public String getLocalName(int index) {
            return StAXStreamConnector.this.staxStreamReader.getAttributeLocalName(index);
        }

        @Override
        public String getQName(int index) {
            String prefix = StAXStreamConnector.this.staxStreamReader.getAttributePrefix(index);
            if (prefix == null || prefix.length() == 0) {
                return this.getLocalName(index);
            }
            return prefix + ':' + this.getLocalName(index);
        }

        @Override
        public String getType(int index) {
            return StAXStreamConnector.this.staxStreamReader.getAttributeType(index);
        }

        @Override
        public String getValue(int index) {
            return StAXStreamConnector.this.staxStreamReader.getAttributeValue(index);
        }

        @Override
        public int getIndex(String uri, String localName) {
            for (int i = this.getLength() - 1; i >= 0; --i) {
                if (!localName.equals(this.getLocalName(i)) || !uri.equals(this.getURI(i))) continue;
                return i;
            }
            return -1;
        }

        @Override
        public int getIndex(String qName) {
            for (int i = this.getLength() - 1; i >= 0; --i) {
                if (!qName.equals(this.getQName(i))) continue;
                return i;
            }
            return -1;
        }

        @Override
        public String getType(String uri, String localName) {
            int index = this.getIndex(uri, localName);
            if (index < 0) {
                return null;
            }
            return this.getType(index);
        }

        @Override
        public String getType(String qName) {
            int index = this.getIndex(qName);
            if (index < 0) {
                return null;
            }
            return this.getType(index);
        }

        @Override
        public String getValue(String uri, String localName) {
            int index = this.getIndex(uri, localName);
            if (index < 0) {
                return null;
            }
            return this.getValue(index);
        }

        @Override
        public String getValue(String qName) {
            int index = this.getIndex(qName);
            if (index < 0) {
                return null;
            }
            return this.getValue(index);
        }
    };
    private static final Class FI_STAX_READER_CLASS = StAXStreamConnector.initFIStAXReaderClass();
    private static final Constructor<? extends StAXConnector> FI_CONNECTOR_CTOR = StAXStreamConnector.initFastInfosetConnectorClass();
    private static final Class STAX_EX_READER_CLASS = StAXStreamConnector.initStAXExReader();
    private static final Constructor<? extends StAXConnector> STAX_EX_CONNECTOR_CTOR = StAXStreamConnector.initStAXExConnector();

    public static StAXConnector create(XMLStreamReader reader, XmlVisitor visitor) {
        Class<?> readerClass = reader.getClass();
        if (FI_STAX_READER_CLASS != null && FI_STAX_READER_CLASS.isAssignableFrom(readerClass) && FI_CONNECTOR_CTOR != null) {
            try {
                return FI_CONNECTOR_CTOR.newInstance(reader, visitor);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        boolean isZephyr = readerClass.getName().equals("com.sun.xml.stream.XMLReaderImpl");
        if (!(StAXStreamConnector.getBoolProp(reader, "org.codehaus.stax2.internNames") && StAXStreamConnector.getBoolProp(reader, "org.codehaus.stax2.internNsUris") || isZephyr || StAXStreamConnector.checkImplementaionNameOfSjsxp(reader))) {
            visitor = new InterningXmlVisitor(visitor);
        }
        if (STAX_EX_READER_CLASS != null && STAX_EX_READER_CLASS.isAssignableFrom(readerClass)) {
            try {
                return STAX_EX_CONNECTOR_CTOR.newInstance(reader, visitor);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return new StAXStreamConnector(reader, visitor);
    }

    private static boolean checkImplementaionNameOfSjsxp(XMLStreamReader reader) {
        try {
            Object name = reader.getProperty("http://java.sun.com/xml/stream/properties/implementation-name");
            return name != null && name.equals("sjsxp");
        }
        catch (Exception e) {
            return false;
        }
    }

    private static boolean getBoolProp(XMLStreamReader r, String n) {
        try {
            Object o = r.getProperty(n);
            if (o instanceof Boolean) {
                return (Boolean)o;
            }
            return false;
        }
        catch (Exception e) {
            return false;
        }
    }

    protected StAXStreamConnector(XMLStreamReader staxStreamReader, XmlVisitor visitor) {
        super(visitor);
        this.staxStreamReader = staxStreamReader;
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
            int event = this.staxStreamReader.getEventType();
            if (event == 7) {
                while (!this.staxStreamReader.isStartElement()) {
                    event = this.staxStreamReader.next();
                }
            }
            if (event != 1) {
                throw new IllegalStateException("The current event is not START_ELEMENT\n but " + event);
            }
            this.handleStartDocument(this.staxStreamReader.getNamespaceContext());
            block8: while (true) {
                switch (event) {
                    case 1: {
                        this.handleStartElement();
                        ++depth;
                        break;
                    }
                    case 2: {
                        this.handleEndElement();
                        if (--depth != 0) break;
                        break block8;
                    }
                    case 4: 
                    case 6: 
                    case 12: {
                        this.handleCharacters();
                    }
                }
                event = this.staxStreamReader.next();
            }
            this.staxStreamReader.next();
            this.handleEndDocument();
            return;
        }
        catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    @Override
    protected Location getCurrentLocation() {
        return this.staxStreamReader.getLocation();
    }

    @Override
    protected String getCurrentQName() {
        return this.getQName(this.staxStreamReader.getPrefix(), this.staxStreamReader.getLocalName());
    }

    private void handleEndElement() throws SAXException {
        this.processText(false);
        this.tagName.uri = StAXStreamConnector.fixNull(this.staxStreamReader.getNamespaceURI());
        this.tagName.local = this.staxStreamReader.getLocalName();
        this.visitor.endElement(this.tagName);
        int nsCount = this.staxStreamReader.getNamespaceCount();
        for (int i = nsCount - 1; i >= 0; --i) {
            this.visitor.endPrefixMapping(StAXStreamConnector.fixNull(this.staxStreamReader.getNamespacePrefix(i)));
        }
    }

    private void handleStartElement() throws SAXException {
        this.processText(true);
        int nsCount = this.staxStreamReader.getNamespaceCount();
        for (int i = 0; i < nsCount; ++i) {
            this.visitor.startPrefixMapping(StAXStreamConnector.fixNull(this.staxStreamReader.getNamespacePrefix(i)), StAXStreamConnector.fixNull(this.staxStreamReader.getNamespaceURI(i)));
        }
        this.tagName.uri = StAXStreamConnector.fixNull(this.staxStreamReader.getNamespaceURI());
        this.tagName.local = this.staxStreamReader.getLocalName();
        this.tagName.atts = this.attributes;
        this.visitor.startElement(this.tagName);
    }

    protected void handleCharacters() throws XMLStreamException, SAXException {
        if (this.predictor.expectText()) {
            this.buffer.append(this.staxStreamReader.getTextCharacters(), this.staxStreamReader.getTextStart(), this.staxStreamReader.getTextLength());
        }
    }

    private void processText(boolean ignorable) throws SAXException {
        if (this.predictor.expectText() && (!ignorable || !WhiteSpaceProcessor.isWhiteSpace(this.buffer) || this.context.getCurrentState().isMixed())) {
            if (this.textReported) {
                this.textReported = false;
            } else {
                this.visitor.text(this.buffer);
            }
        }
        this.buffer.setLength(0);
    }

    private static Class initFIStAXReaderClass() {
        try {
            Class<?> fisr = Class.forName("org.jvnet.fastinfoset.stax.FastInfosetStreamReader");
            Class<?> sdp = Class.forName("com.sun.xml.fastinfoset.stax.StAXDocumentParser");
            if (fisr.isAssignableFrom(sdp)) {
                return sdp;
            }
            return null;
        }
        catch (Throwable e) {
            return null;
        }
    }

    private static Constructor<? extends StAXConnector> initFastInfosetConnectorClass() {
        try {
            if (FI_STAX_READER_CLASS == null) {
                return null;
            }
            Class<?> c = Class.forName("com.sun.xml.bind.v2.runtime.unmarshaller.FastInfosetConnector");
            return c.getConstructor(FI_STAX_READER_CLASS, XmlVisitor.class);
        }
        catch (Throwable e) {
            return null;
        }
    }

    private static Class initStAXExReader() {
        try {
            return Class.forName("org.jvnet.staxex.XMLStreamReaderEx");
        }
        catch (Throwable e) {
            return null;
        }
    }

    private static Constructor<? extends StAXConnector> initStAXExConnector() {
        try {
            Class<?> c = Class.forName("com.sun.xml.bind.v2.runtime.unmarshaller.StAXExConnector");
            return c.getConstructor(STAX_EX_READER_CLASS, XmlVisitor.class);
        }
        catch (Throwable e) {
            return null;
        }
    }
}

