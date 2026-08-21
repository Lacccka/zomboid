/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind.helpers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.bind.helpers.Messages;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public abstract class AbstractUnmarshallerImpl
implements Unmarshaller {
    private ValidationEventHandler eventHandler = new DefaultValidationEventHandler();
    protected boolean validating = false;
    private XMLReader reader = null;

    protected XMLReader getXMLReader() throws JAXBException {
        if (this.reader == null) {
            try {
                SAXParserFactory parserFactory = SAXParserFactory.newInstance();
                parserFactory.setNamespaceAware(true);
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

    @Override
    public Object unmarshal(Source source2) throws JAXBException {
        if (source2 == null) {
            throw new IllegalArgumentException(Messages.format("Shared.MustNotBeNull", "source"));
        }
        if (source2 instanceof SAXSource) {
            return this.unmarshal((SAXSource)source2);
        }
        if (source2 instanceof StreamSource) {
            return this.unmarshal(AbstractUnmarshallerImpl.streamSourceToInputSource((StreamSource)source2));
        }
        if (source2 instanceof DOMSource) {
            return this.unmarshal(((DOMSource)source2).getNode());
        }
        throw new IllegalArgumentException();
    }

    private Object unmarshal(SAXSource source2) throws JAXBException {
        XMLReader r = source2.getXMLReader();
        if (r == null) {
            r = this.getXMLReader();
        }
        return this.unmarshal(r, source2.getInputSource());
    }

    protected abstract Object unmarshal(XMLReader var1, InputSource var2) throws JAXBException;

    @Override
    public final Object unmarshal(InputSource source2) throws JAXBException {
        if (source2 == null) {
            throw new IllegalArgumentException(Messages.format("Shared.MustNotBeNull", "source"));
        }
        return this.unmarshal(this.getXMLReader(), source2);
    }

    private Object unmarshal(String url) throws JAXBException {
        return this.unmarshal(new InputSource(url));
    }

    @Override
    public final Object unmarshal(URL url) throws JAXBException {
        if (url == null) {
            throw new IllegalArgumentException(Messages.format("Shared.MustNotBeNull", "url"));
        }
        return this.unmarshal(url.toExternalForm());
    }

    @Override
    public final Object unmarshal(File f) throws JAXBException {
        if (f == null) {
            throw new IllegalArgumentException(Messages.format("Shared.MustNotBeNull", "file"));
        }
        try {
            return this.unmarshal(new BufferedInputStream(new FileInputStream(f)));
        }
        catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public final Object unmarshal(InputStream is) throws JAXBException {
        if (is == null) {
            throw new IllegalArgumentException(Messages.format("Shared.MustNotBeNull", "is"));
        }
        InputSource isrc = new InputSource(is);
        return this.unmarshal(isrc);
    }

    @Override
    public final Object unmarshal(Reader reader) throws JAXBException {
        if (reader == null) {
            throw new IllegalArgumentException(Messages.format("Shared.MustNotBeNull", "reader"));
        }
        InputSource isrc = new InputSource(reader);
        return this.unmarshal(isrc);
    }

    private static InputSource streamSourceToInputSource(StreamSource ss) {
        InputSource is = new InputSource();
        is.setSystemId(ss.getSystemId());
        is.setByteStream(ss.getInputStream());
        is.setCharacterStream(ss.getReader());
        return is;
    }

    @Override
    public boolean isValidating() throws JAXBException {
        return this.validating;
    }

    @Override
    public void setEventHandler(ValidationEventHandler handler) throws JAXBException {
        this.eventHandler = handler == null ? new DefaultValidationEventHandler() : handler;
    }

    @Override
    public void setValidating(boolean validating) throws JAXBException {
        this.validating = validating;
    }

    @Override
    public ValidationEventHandler getEventHandler() throws JAXBException {
        return this.eventHandler;
    }

    protected UnmarshalException createUnmarshalException(SAXException e) {
        Exception nested = e.getException();
        if (nested instanceof UnmarshalException) {
            return (UnmarshalException)nested;
        }
        if (nested instanceof RuntimeException) {
            throw (RuntimeException)nested;
        }
        if (nested != null) {
            return new UnmarshalException(nested);
        }
        return new UnmarshalException(e);
    }

    @Override
    public void setProperty(String name, Object value) throws PropertyException {
        if (name == null) {
            throw new IllegalArgumentException(Messages.format("Shared.MustNotBeNull", "name"));
        }
        throw new PropertyException(name, value);
    }

    @Override
    public Object getProperty(String name) throws PropertyException {
        if (name == null) {
            throw new IllegalArgumentException(Messages.format("Shared.MustNotBeNull", "name"));
        }
        throw new PropertyException(name);
    }

    @Override
    public Object unmarshal(XMLEventReader reader) throws JAXBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object unmarshal(XMLStreamReader reader) throws JAXBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> JAXBElement<T> unmarshal(Node node, Class<T> expectedType) throws JAXBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> JAXBElement<T> unmarshal(Source source2, Class<T> expectedType) throws JAXBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> JAXBElement<T> unmarshal(XMLStreamReader reader, Class<T> expectedType) throws JAXBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> JAXBElement<T> unmarshal(XMLEventReader reader, Class<T> expectedType) throws JAXBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSchema(Schema schema) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Schema getSchema() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAdapter(XmlAdapter adapter) {
        if (adapter == null) {
            throw new IllegalArgumentException();
        }
        this.setAdapter(adapter.getClass(), adapter);
    }

    @Override
    public <A extends XmlAdapter> void setAdapter(Class<A> type, A adapter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <A extends XmlAdapter> A getAdapter(Class<A> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttachmentUnmarshaller(AttachmentUnmarshaller au) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AttachmentUnmarshaller getAttachmentUnmarshaller() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setListener(Unmarshaller.Listener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Unmarshaller.Listener getListener() {
        throw new UnsupportedOperationException();
    }
}

