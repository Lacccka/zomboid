/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.validation.Schema;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

public interface Marshaller {
    public static final String JAXB_ENCODING = "jaxb.encoding";
    public static final String JAXB_FORMATTED_OUTPUT = "jaxb.formatted.output";
    public static final String JAXB_SCHEMA_LOCATION = "jaxb.schemaLocation";
    public static final String JAXB_NO_NAMESPACE_SCHEMA_LOCATION = "jaxb.noNamespaceSchemaLocation";
    public static final String JAXB_FRAGMENT = "jaxb.fragment";

    public void marshal(Object var1, Result var2) throws JAXBException;

    public void marshal(Object var1, OutputStream var2) throws JAXBException;

    public void marshal(Object var1, File var2) throws JAXBException;

    public void marshal(Object var1, Writer var2) throws JAXBException;

    public void marshal(Object var1, ContentHandler var2) throws JAXBException;

    public void marshal(Object var1, Node var2) throws JAXBException;

    public void marshal(Object var1, XMLStreamWriter var2) throws JAXBException;

    public void marshal(Object var1, XMLEventWriter var2) throws JAXBException;

    public Node getNode(Object var1) throws JAXBException;

    public void setProperty(String var1, Object var2) throws PropertyException;

    public Object getProperty(String var1) throws PropertyException;

    public void setEventHandler(ValidationEventHandler var1) throws JAXBException;

    public ValidationEventHandler getEventHandler() throws JAXBException;

    public void setAdapter(XmlAdapter var1);

    public <A extends XmlAdapter> void setAdapter(Class<A> var1, A var2);

    public <A extends XmlAdapter> A getAdapter(Class<A> var1);

    public void setAttachmentMarshaller(AttachmentMarshaller var1);

    public AttachmentMarshaller getAttachmentMarshaller();

    public void setSchema(Schema var1);

    public Schema getSchema();

    public void setListener(Listener var1);

    public Listener getListener();

    public static abstract class Listener {
        public void beforeMarshal(Object source2) {
        }

        public void afterMarshal(Object source2) {
        }
    }
}

