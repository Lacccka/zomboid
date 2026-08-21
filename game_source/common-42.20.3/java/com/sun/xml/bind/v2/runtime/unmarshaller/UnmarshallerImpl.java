/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.IDResolver;
import com.sun.xml.bind.api.ClassResolver;
import com.sun.xml.bind.unmarshaller.DOMScanner;
import com.sun.xml.bind.unmarshaller.InfosetScanner;
import com.sun.xml.bind.unmarshaller.Messages;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.runtime.AssociationMap;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.unmarshaller.DefaultIDResolver;
import com.sun.xml.bind.v2.runtime.unmarshaller.InterningXmlVisitor;
import com.sun.xml.bind.v2.runtime.unmarshaller.MTOMDecorator;
import com.sun.xml.bind.v2.runtime.unmarshaller.SAXConnector;
import com.sun.xml.bind.v2.runtime.unmarshaller.StAXConnector;
import com.sun.xml.bind.v2.runtime.unmarshaller.StAXEventConnector;
import com.sun.xml.bind.v2.runtime.unmarshaller.StAXStreamConnector;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.ValidatingUnmarshaller;
import com.sun.xml.bind.v2.runtime.unmarshaller.XmlVisitor;
import com.sun.xml.bind.v2.util.XmlFactory;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.bind.helpers.AbstractUnmarshallerImpl;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public final class UnmarshallerImpl
extends AbstractUnmarshallerImpl
implements ValidationEventHandler,
Closeable {
    protected final JAXBContextImpl context;
    private Schema schema;
    public final UnmarshallingContext coordinator;
    private Unmarshaller.Listener externalListener;
    private AttachmentUnmarshaller attachmentUnmarshaller;
    private IDResolver idResolver = new DefaultIDResolver();
    private XMLReader reader = null;
    private static final DefaultHandler dummyHandler = new DefaultHandler();
    public static final String FACTORY = "com.sun.xml.bind.ObjectFactory";

    public UnmarshallerImpl(JAXBContextImpl context, AssociationMap assoc) {
        this.context = context;
        this.coordinator = new UnmarshallingContext(this, assoc);
        try {
            this.setEventHandler(this);
        }
        catch (JAXBException e) {
            throw new AssertionError((Object)e);
        }
    }

    @Override
    public UnmarshallerHandler getUnmarshallerHandler() {
        return this.getUnmarshallerHandler(true, null);
    }

    @Override
    protected XMLReader getXMLReader() throws JAXBException {
        if (this.reader == null) {
            try {
                SAXParserFactory parserFactory = XmlFactory.createParserFactory(this.context.disableSecurityProcessing);
                parserFactory.setValidating(false);
                this.reader = parserFactory.newSAXParser().getXMLReader();
            }
            catch (ParserConfigurationException e) {
                throw new JAXBException(e);
            }
            catch (SAXException e) {
                throw new JAXBException(e);
            }
        }
        return this.reader;
    }

    private SAXConnector getUnmarshallerHandler(boolean intern, JaxBeanInfo expectedType) {
        XmlVisitor h = this.createUnmarshallerHandler(null, false, expectedType);
        if (intern) {
            h = new InterningXmlVisitor(h);
        }
        return new SAXConnector(h, null);
    }

    public final XmlVisitor createUnmarshallerHandler(InfosetScanner scanner, boolean inplace, JaxBeanInfo expectedType) {
        this.coordinator.reset(scanner, inplace, expectedType, this.idResolver);
        XmlVisitor unmarshaller = this.coordinator;
        if (this.schema != null) {
            unmarshaller = new ValidatingUnmarshaller(this.schema, unmarshaller);
        }
        if (this.attachmentUnmarshaller != null && this.attachmentUnmarshaller.isXOPPackage()) {
            unmarshaller = new MTOMDecorator(this, unmarshaller, this.attachmentUnmarshaller);
        }
        return unmarshaller;
    }

    public static boolean needsInterning(XMLReader reader) {
        try {
            reader.setFeature("http://xml.org/sax/features/string-interning", true);
        }
        catch (SAXException sAXException) {
            // empty catch block
        }
        try {
            if (reader.getFeature("http://xml.org/sax/features/string-interning")) {
                return false;
            }
        }
        catch (SAXException sAXException) {
            // empty catch block
        }
        return true;
    }

    @Override
    protected Object unmarshal(XMLReader reader, InputSource source2) throws JAXBException {
        return this.unmarshal0(reader, source2, null);
    }

    protected <T> JAXBElement<T> unmarshal(XMLReader reader, InputSource source2, Class<T> expectedType) throws JAXBException {
        if (expectedType == null) {
            throw new IllegalArgumentException();
        }
        return (JAXBElement)this.unmarshal0(reader, source2, this.getBeanInfo(expectedType));
    }

    private Object unmarshal0(XMLReader reader, InputSource source2, JaxBeanInfo expectedType) throws JAXBException {
        SAXConnector connector = this.getUnmarshallerHandler(UnmarshallerImpl.needsInterning(reader), expectedType);
        reader.setContentHandler(connector);
        reader.setErrorHandler(this.coordinator);
        try {
            reader.parse(source2);
        }
        catch (IOException e) {
            this.coordinator.clearStates();
            throw new UnmarshalException(e);
        }
        catch (SAXException e) {
            this.coordinator.clearStates();
            throw this.createUnmarshalException(e);
        }
        Object result = connector.getResult();
        reader.setContentHandler(dummyHandler);
        reader.setErrorHandler(dummyHandler);
        return result;
    }

    @Override
    public <T> JAXBElement<T> unmarshal(Source source2, Class<T> expectedType) throws JAXBException {
        if (source2 instanceof SAXSource) {
            SAXSource ss = (SAXSource)source2;
            XMLReader locReader = ss.getXMLReader();
            if (locReader == null) {
                locReader = this.getXMLReader();
            }
            return this.unmarshal(locReader, ss.getInputSource(), expectedType);
        }
        if (source2 instanceof StreamSource) {
            return this.unmarshal(this.getXMLReader(), UnmarshallerImpl.streamSourceToInputSource((StreamSource)source2), expectedType);
        }
        if (source2 instanceof DOMSource) {
            return this.unmarshal(((DOMSource)source2).getNode(), expectedType);
        }
        throw new IllegalArgumentException();
    }

    public Object unmarshal0(Source source2, JaxBeanInfo expectedType) throws JAXBException {
        if (source2 instanceof SAXSource) {
            SAXSource ss = (SAXSource)source2;
            XMLReader locReader = ss.getXMLReader();
            if (locReader == null) {
                locReader = this.getXMLReader();
            }
            return this.unmarshal0(locReader, ss.getInputSource(), expectedType);
        }
        if (source2 instanceof StreamSource) {
            return this.unmarshal0(this.getXMLReader(), UnmarshallerImpl.streamSourceToInputSource((StreamSource)source2), expectedType);
        }
        if (source2 instanceof DOMSource) {
            return this.unmarshal0(((DOMSource)source2).getNode(), expectedType);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public final ValidationEventHandler getEventHandler() {
        try {
            return super.getEventHandler();
        }
        catch (JAXBException e) {
            throw new AssertionError();
        }
    }

    public final boolean hasEventHandler() {
        return this.getEventHandler() != this;
    }

    @Override
    public <T> JAXBElement<T> unmarshal(Node node, Class<T> expectedType) throws JAXBException {
        if (expectedType == null) {
            throw new IllegalArgumentException();
        }
        return (JAXBElement)this.unmarshal0(node, this.getBeanInfo(expectedType));
    }

    @Override
    public final Object unmarshal(Node node) throws JAXBException {
        return this.unmarshal0(node, null);
    }

    @Deprecated
    public final Object unmarshal(SAXSource source2) throws JAXBException {
        return super.unmarshal((Source)source2);
    }

    public final Object unmarshal0(Node node, JaxBeanInfo expectedType) throws JAXBException {
        try {
            DOMScanner scanner = new DOMScanner();
            InterningXmlVisitor handler = new InterningXmlVisitor(this.createUnmarshallerHandler(null, false, expectedType));
            scanner.setContentHandler(new SAXConnector(handler, scanner));
            if (node.getNodeType() == 1) {
                scanner.scan((Element)node);
            } else if (node.getNodeType() == 9) {
                scanner.scan((Document)node);
            } else {
                throw new IllegalArgumentException("Unexpected node type: " + node);
            }
            Object retVal = handler.getContext().getResult();
            handler.getContext().clearResult();
            return retVal;
        }
        catch (SAXException e) {
            throw this.createUnmarshalException(e);
        }
    }

    @Override
    public Object unmarshal(XMLStreamReader reader) throws JAXBException {
        return this.unmarshal0(reader, null);
    }

    @Override
    public <T> JAXBElement<T> unmarshal(XMLStreamReader reader, Class<T> expectedType) throws JAXBException {
        if (expectedType == null) {
            throw new IllegalArgumentException();
        }
        return (JAXBElement)this.unmarshal0(reader, this.getBeanInfo(expectedType));
    }

    public Object unmarshal0(XMLStreamReader reader, JaxBeanInfo expectedType) throws JAXBException {
        if (reader == null) {
            throw new IllegalArgumentException(Messages.format("Unmarshaller.NullReader"));
        }
        int eventType = reader.getEventType();
        if (eventType != 1 && eventType != 7) {
            throw new IllegalStateException(Messages.format("Unmarshaller.IllegalReaderState", eventType));
        }
        XmlVisitor h = this.createUnmarshallerHandler(null, false, expectedType);
        StAXConnector connector = StAXStreamConnector.create(reader, h);
        try {
            connector.bridge();
        }
        catch (XMLStreamException e) {
            throw UnmarshallerImpl.handleStreamException(e);
        }
        Object retVal = h.getContext().getResult();
        h.getContext().clearResult();
        return retVal;
    }

    @Override
    public <T> JAXBElement<T> unmarshal(XMLEventReader reader, Class<T> expectedType) throws JAXBException {
        if (expectedType == null) {
            throw new IllegalArgumentException();
        }
        return (JAXBElement)this.unmarshal0(reader, this.getBeanInfo(expectedType));
    }

    @Override
    public Object unmarshal(XMLEventReader reader) throws JAXBException {
        return this.unmarshal0(reader, null);
    }

    private Object unmarshal0(XMLEventReader reader, JaxBeanInfo expectedType) throws JAXBException {
        if (reader == null) {
            throw new IllegalArgumentException(Messages.format("Unmarshaller.NullReader"));
        }
        try {
            XMLEvent event = reader.peek();
            if (!event.isStartElement() && !event.isStartDocument()) {
                throw new IllegalStateException(Messages.format("Unmarshaller.IllegalReaderState", event.getEventType()));
            }
            boolean isZephyr = reader.getClass().getName().equals("com.sun.xml.stream.XMLReaderImpl");
            XmlVisitor h = this.createUnmarshallerHandler(null, false, expectedType);
            if (!isZephyr) {
                h = new InterningXmlVisitor(h);
            }
            new StAXEventConnector(reader, h).bridge();
            return h.getContext().getResult();
        }
        catch (XMLStreamException e) {
            throw UnmarshallerImpl.handleStreamException(e);
        }
    }

    public Object unmarshal0(InputStream input, JaxBeanInfo expectedType) throws JAXBException {
        return this.unmarshal0(this.getXMLReader(), new InputSource(input), expectedType);
    }

    private static JAXBException handleStreamException(XMLStreamException e) {
        Throwable ne = e.getNestedException();
        if (ne instanceof JAXBException) {
            return (JAXBException)ne;
        }
        if (ne instanceof SAXException) {
            return new UnmarshalException(ne);
        }
        return new UnmarshalException(e);
    }

    @Override
    public Object getProperty(String name) throws PropertyException {
        if (name.equals(IDResolver.class.getName())) {
            return this.idResolver;
        }
        return super.getProperty(name);
    }

    @Override
    public void setProperty(String name, Object value) throws PropertyException {
        if (name.equals(FACTORY)) {
            this.coordinator.setFactories(value);
            return;
        }
        if (name.equals(IDResolver.class.getName())) {
            this.idResolver = (IDResolver)value;
            return;
        }
        if (name.equals(ClassResolver.class.getName())) {
            this.coordinator.classResolver = (ClassResolver)value;
            return;
        }
        if (name.equals(ClassLoader.class.getName())) {
            this.coordinator.classLoader = (ClassLoader)value;
            return;
        }
        super.setProperty(name, value);
    }

    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    @Override
    public Schema getSchema() {
        return this.schema;
    }

    @Override
    public AttachmentUnmarshaller getAttachmentUnmarshaller() {
        return this.attachmentUnmarshaller;
    }

    @Override
    public void setAttachmentUnmarshaller(AttachmentUnmarshaller au) {
        this.attachmentUnmarshaller = au;
    }

    @Override
    public boolean isValidating() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValidating(boolean validating) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <A extends XmlAdapter> void setAdapter(Class<A> type, A adapter) {
        if (type == null) {
            throw new IllegalArgumentException();
        }
        this.coordinator.putAdapter(type, adapter);
    }

    @Override
    public <A extends XmlAdapter> A getAdapter(Class<A> type) {
        if (type == null) {
            throw new IllegalArgumentException();
        }
        if (this.coordinator.containsAdapter(type)) {
            return this.coordinator.getAdapter(type);
        }
        return null;
    }

    @Override
    public UnmarshalException createUnmarshalException(SAXException e) {
        return super.createUnmarshalException(e);
    }

    @Override
    public boolean handleEvent(ValidationEvent event) {
        return event.getSeverity() != 2;
    }

    private static InputSource streamSourceToInputSource(StreamSource ss) {
        InputSource is = new InputSource();
        is.setSystemId(ss.getSystemId());
        is.setByteStream(ss.getInputStream());
        is.setCharacterStream(ss.getReader());
        return is;
    }

    public <T> JaxBeanInfo<T> getBeanInfo(Class<T> clazz) throws JAXBException {
        return this.context.getBeanInfo(clazz, true);
    }

    @Override
    public Unmarshaller.Listener getListener() {
        return this.externalListener;
    }

    @Override
    public void setListener(Unmarshaller.Listener listener) {
        this.externalListener = listener;
    }

    public UnmarshallingContext getContext() {
        return this.coordinator;
    }

    protected void finalize() throws Throwable {
        try {
            ClassFactory.cleanCache();
        }
        finally {
            super.finalize();
        }
    }

    @Override
    public void close() throws IOException {
        ClassFactory.cleanCache();
    }
}

