/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public interface Unmarshaller {
    public Object unmarshal(File var1) throws JAXBException;

    public Object unmarshal(InputStream var1) throws JAXBException;

    public Object unmarshal(Reader var1) throws JAXBException;

    public Object unmarshal(URL var1) throws JAXBException;

    public Object unmarshal(InputSource var1) throws JAXBException;

    public Object unmarshal(Node var1) throws JAXBException;

    public <T> JAXBElement<T> unmarshal(Node var1, Class<T> var2) throws JAXBException;

    public Object unmarshal(Source var1) throws JAXBException;

    public <T> JAXBElement<T> unmarshal(Source var1, Class<T> var2) throws JAXBException;

    public Object unmarshal(XMLStreamReader var1) throws JAXBException;

    public <T> JAXBElement<T> unmarshal(XMLStreamReader var1, Class<T> var2) throws JAXBException;

    public Object unmarshal(XMLEventReader var1) throws JAXBException;

    public <T> JAXBElement<T> unmarshal(XMLEventReader var1, Class<T> var2) throws JAXBException;

    public UnmarshallerHandler getUnmarshallerHandler();

    public void setValidating(boolean var1) throws JAXBException;

    public boolean isValidating() throws JAXBException;

    public void setEventHandler(ValidationEventHandler var1) throws JAXBException;

    public ValidationEventHandler getEventHandler() throws JAXBException;

    public void setProperty(String var1, Object var2) throws PropertyException;

    public Object getProperty(String var1) throws PropertyException;

    public void setSchema(Schema var1);

    public Schema getSchema();

    public void setAdapter(XmlAdapter var1);

    public <A extends XmlAdapter> void setAdapter(Class<A> var1, A var2);

    public <A extends XmlAdapter> A getAdapter(Class<A> var1);

    public void setAttachmentUnmarshaller(AttachmentUnmarshaller var1);

    public AttachmentUnmarshaller getAttachmentUnmarshaller();

    public void setListener(Listener var1);

    public Listener getListener();

    public static abstract class Listener {
        public void beforeUnmarshal(Object target, Object parent) {
        }

        public void afterUnmarshal(Object target, Object parent) {
        }
    }
}

