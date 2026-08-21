/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import com.sun.xml.bind.marshaller.DataWriter;
import com.sun.xml.bind.marshaller.DumbEscapeHandler;
import com.sun.xml.bind.marshaller.MinimumEscapeHandler;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import com.sun.xml.bind.marshaller.NioEscapeHandler;
import com.sun.xml.bind.marshaller.SAX2DOMEx;
import com.sun.xml.bind.marshaller.XMLWriter;
import com.sun.xml.bind.v2.runtime.AssociationMap;
import com.sun.xml.bind.v2.runtime.DomPostInitAction;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.Messages;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.StAXPostInitAction;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.output.C14nXmlOutput;
import com.sun.xml.bind.v2.runtime.output.Encoded;
import com.sun.xml.bind.v2.runtime.output.ForkXmlOutput;
import com.sun.xml.bind.v2.runtime.output.IndentingUTF8XmlOutput;
import com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl;
import com.sun.xml.bind.v2.runtime.output.SAXOutput;
import com.sun.xml.bind.v2.runtime.output.UTF8XmlOutput;
import com.sun.xml.bind.v2.runtime.output.XMLEventWriterOutput;
import com.sun.xml.bind.v2.runtime.output.XMLStreamWriterOutput;
import com.sun.xml.bind.v2.runtime.output.XmlOutput;
import com.sun.xml.bind.v2.util.FatalAdapter;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.helpers.AbstractMarshallerImpl;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.ValidatorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

public final class MarshallerImpl
extends AbstractMarshallerImpl
implements ValidationEventHandler {
    private static final Logger LOGGER = Logger.getLogger(MarshallerImpl.class.getName());
    private String indent = "    ";
    private NamespacePrefixMapper prefixMapper = null;
    private CharacterEscapeHandler escapeHandler = null;
    private String header = null;
    final JAXBContextImpl context;
    protected final XMLSerializer serializer;
    private Schema schema;
    private Marshaller.Listener externalListener = null;
    private boolean c14nSupport;
    private Flushable toBeFlushed;
    private Closeable toBeClosed;
    protected static final String INDENT_STRING = "com.sun.xml.bind.indentString";
    protected static final String PREFIX_MAPPER = "com.sun.xml.bind.namespacePrefixMapper";
    protected static final String ENCODING_HANDLER = "com.sun.xml.bind.characterEscapeHandler";
    protected static final String ENCODING_HANDLER2 = "com.sun.xml.bind.marshaller.CharacterEscapeHandler";
    protected static final String XMLDECLARATION = "com.sun.xml.bind.xmlDeclaration";
    protected static final String XML_HEADERS = "com.sun.xml.bind.xmlHeaders";
    protected static final String C14N = "com.sun.xml.bind.c14n";
    protected static final String OBJECT_IDENTITY_CYCLE_DETECTION = "com.sun.xml.bind.objectIdentitityCycleDetection";

    public MarshallerImpl(JAXBContextImpl c, AssociationMap assoc) {
        this.context = c;
        this.serializer = new XMLSerializer(this);
        this.c14nSupport = this.context.c14nSupport;
        try {
            this.setEventHandler(this);
        }
        catch (JAXBException e) {
            throw new AssertionError((Object)e);
        }
    }

    public JAXBContextImpl getContext() {
        return this.context;
    }

    public void marshal(Object obj, OutputStream out, NamespaceContext inscopeNamespace) throws JAXBException {
        this.write(obj, this.createWriter(out), new StAXPostInitAction(inscopeNamespace, this.serializer));
    }

    @Override
    public void marshal(Object obj, XMLStreamWriter writer) throws JAXBException {
        this.write(obj, XMLStreamWriterOutput.create(writer, this.context, this.escapeHandler), new StAXPostInitAction(writer, this.serializer));
    }

    @Override
    public void marshal(Object obj, XMLEventWriter writer) throws JAXBException {
        this.write(obj, new XMLEventWriterOutput(writer), new StAXPostInitAction(writer, this.serializer));
    }

    public void marshal(Object obj, XmlOutput output) throws JAXBException {
        this.write(obj, output, null);
    }

    final XmlOutput createXmlOutput(Result result) throws JAXBException {
        if (result instanceof SAXResult) {
            return new SAXOutput(((SAXResult)result).getHandler());
        }
        if (result instanceof DOMResult) {
            Node node = ((DOMResult)result).getNode();
            if (node == null) {
                Document doc = JAXBContextImpl.createDom(this.getContext().disableSecurityProcessing);
                ((DOMResult)result).setNode(doc);
                return new SAXOutput(new SAX2DOMEx(doc));
            }
            return new SAXOutput(new SAX2DOMEx(node));
        }
        if (result instanceof StreamResult) {
            StreamResult sr = (StreamResult)result;
            if (sr.getWriter() != null) {
                return this.createWriter(sr.getWriter());
            }
            if (sr.getOutputStream() != null) {
                return this.createWriter(sr.getOutputStream());
            }
            if (sr.getSystemId() != null) {
                String fileURL = sr.getSystemId();
                try {
                    fileURL = new URI(fileURL).getPath();
                }
                catch (URISyntaxException uRISyntaxException) {
                    // empty catch block
                }
                try {
                    FileOutputStream fos = new FileOutputStream(fileURL);
                    assert (this.toBeClosed == null);
                    this.toBeClosed = fos;
                    return this.createWriter(fos);
                }
                catch (IOException e) {
                    throw new MarshalException(e);
                }
            }
        }
        throw new MarshalException(Messages.UNSUPPORTED_RESULT.format(new Object[0]));
    }

    final Runnable createPostInitAction(Result result) {
        if (result instanceof DOMResult) {
            Node node = ((DOMResult)result).getNode();
            return new DomPostInitAction(node, this.serializer);
        }
        return null;
    }

    @Override
    public void marshal(Object target, Result result) throws JAXBException {
        this.write(target, this.createXmlOutput(result), this.createPostInitAction(result));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected final <T> void write(Name rootTagName, JaxBeanInfo<T> bi, T obj, XmlOutput out, Runnable postInitAction) throws JAXBException {
        try {
            try {
                this.prewrite(out, true, postInitAction);
                this.serializer.startElement(rootTagName, null);
                if (bi.jaxbType == Void.class || bi.jaxbType == Void.TYPE) {
                    this.serializer.endNamespaceDecls(null);
                    this.serializer.endAttributes();
                } else if (obj == null) {
                    this.serializer.writeXsiNilTrue();
                } else {
                    this.serializer.childAsXsiType(obj, "root", bi, false);
                }
                this.serializer.endElement();
                this.postwrite();
            }
            catch (SAXException e) {
                throw new MarshalException(e);
            }
            catch (IOException e) {
                throw new MarshalException(e);
            }
            catch (XMLStreamException e) {
                throw new MarshalException(e);
            }
            finally {
                this.serializer.close();
            }
        }
        finally {
            this.cleanUp();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void write(Object obj, XmlOutput out, Runnable postInitAction) throws JAXBException {
        try {
            if (obj == null) {
                throw new IllegalArgumentException(Messages.NOT_MARSHALLABLE.format(new Object[0]));
            }
            if (this.schema != null) {
                ValidatorHandler validator = this.schema.newValidatorHandler();
                validator.setErrorHandler(new FatalAdapter(this.serializer));
                XMLFilterImpl f = new XMLFilterImpl(){

                    @Override
                    public void startPrefixMapping(String prefix, String uri) throws SAXException {
                        super.startPrefixMapping(prefix.intern(), uri.intern());
                    }
                };
                f.setContentHandler(validator);
                out = new ForkXmlOutput(new SAXOutput(f){

                    @Override
                    public void startDocument(XMLSerializer serializer, boolean fragment, int[] nsUriIndex2prefixIndex, NamespaceContextImpl nsContext) throws SAXException, IOException, XMLStreamException {
                        super.startDocument(serializer, false, nsUriIndex2prefixIndex, nsContext);
                    }

                    @Override
                    public void endDocument(boolean fragment) throws SAXException, IOException, XMLStreamException {
                        super.endDocument(false);
                    }
                }, out);
            }
            try {
                this.prewrite(out, this.isFragment(), postInitAction);
                this.serializer.childAsRoot(obj);
                this.postwrite();
            }
            catch (SAXException e) {
                throw new MarshalException(e);
            }
            catch (IOException e) {
                throw new MarshalException(e);
            }
            catch (XMLStreamException e) {
                throw new MarshalException(e);
            }
            finally {
                this.serializer.close();
            }
        }
        finally {
            this.cleanUp();
        }
    }

    private void cleanUp() {
        if (this.toBeFlushed != null) {
            try {
                this.toBeFlushed.flush();
            }
            catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        if (this.toBeClosed != null) {
            try {
                this.toBeClosed.close();
            }
            catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        this.toBeFlushed = null;
        this.toBeClosed = null;
    }

    private void prewrite(XmlOutput out, boolean fragment, Runnable postInitAction) throws IOException, SAXException, XMLStreamException {
        String[] decls;
        this.serializer.startDocument(out, fragment, this.getSchemaLocation(), this.getNoNSSchemaLocation());
        if (postInitAction != null) {
            postInitAction.run();
        }
        if (this.prefixMapper != null && (decls = this.prefixMapper.getContextualNamespaceDecls()) != null) {
            for (int i = 0; i < decls.length; i += 2) {
                String prefix = decls[i];
                String nsUri = decls[i + 1];
                if (nsUri == null || prefix == null) continue;
                this.serializer.addInscopeBinding(nsUri, prefix);
            }
        }
        this.serializer.setPrefixMapper(this.prefixMapper);
    }

    private void postwrite() throws IOException, SAXException, XMLStreamException {
        this.serializer.endDocument();
        this.serializer.reconcileID();
    }

    CharacterEscapeHandler getEscapeHandler() {
        return this.escapeHandler;
    }

    protected CharacterEscapeHandler createEscapeHandler(String encoding) {
        if (this.escapeHandler != null) {
            return this.escapeHandler;
        }
        if (encoding.startsWith("UTF")) {
            return MinimumEscapeHandler.theInstance;
        }
        try {
            return new NioEscapeHandler(this.getJavaEncoding(encoding));
        }
        catch (Throwable e) {
            return DumbEscapeHandler.theInstance;
        }
    }

    public XmlOutput createWriter(Writer w, String encoding) {
        XMLWriter xw;
        if (!(w instanceof BufferedWriter)) {
            w = new BufferedWriter(w);
        }
        assert (this.toBeFlushed == null);
        this.toBeFlushed = w;
        CharacterEscapeHandler ceh = this.createEscapeHandler(encoding);
        if (this.isFormattedOutput()) {
            DataWriter d = new DataWriter(w, encoding, ceh);
            d.setIndentStep(this.indent);
            xw = d;
        } else {
            xw = new XMLWriter(w, encoding, ceh);
        }
        xw.setXmlDecl(!this.isFragment());
        xw.setHeader(this.header);
        return new SAXOutput(xw);
    }

    public XmlOutput createWriter(Writer w) {
        return this.createWriter(w, this.getEncoding());
    }

    public XmlOutput createWriter(OutputStream os) throws JAXBException {
        return this.createWriter(os, this.getEncoding());
    }

    public XmlOutput createWriter(OutputStream os, String encoding) throws JAXBException {
        if (encoding.equals("UTF-8")) {
            Encoded[] table = this.context.getUTF8NameTable();
            CharacterEscapeHandler ceh = this.createEscapeHandler(encoding);
            UTF8XmlOutput out = this.isFormattedOutput() ? new IndentingUTF8XmlOutput(os, this.indent, table, ceh) : (this.c14nSupport ? new C14nXmlOutput(os, table, this.context.c14nSupport, ceh) : new UTF8XmlOutput(os, table, ceh));
            if (this.header != null) {
                out.setHeader(this.header);
            }
            return out;
        }
        try {
            return this.createWriter(new OutputStreamWriter(os, this.getJavaEncoding(encoding)), encoding);
        }
        catch (UnsupportedEncodingException e) {
            throw new MarshalException(Messages.UNSUPPORTED_ENCODING.format(encoding), e);
        }
    }

    @Override
    public Object getProperty(String name) throws PropertyException {
        if (INDENT_STRING.equals(name)) {
            return this.indent;
        }
        if (ENCODING_HANDLER.equals(name) || ENCODING_HANDLER2.equals(name)) {
            return this.escapeHandler;
        }
        if (PREFIX_MAPPER.equals(name)) {
            return this.prefixMapper;
        }
        if (XMLDECLARATION.equals(name)) {
            return !this.isFragment();
        }
        if (XML_HEADERS.equals(name)) {
            return this.header;
        }
        if (C14N.equals(name)) {
            return this.c14nSupport;
        }
        if (OBJECT_IDENTITY_CYCLE_DETECTION.equals(name)) {
            return this.serializer.getObjectIdentityCycleDetection();
        }
        return super.getProperty(name);
    }

    @Override
    public void setProperty(String name, Object value) throws PropertyException {
        if (INDENT_STRING.equals(name)) {
            this.checkString(name, value);
            this.indent = (String)value;
            return;
        }
        if (ENCODING_HANDLER.equals(name) || ENCODING_HANDLER2.equals(name)) {
            if (!(value instanceof CharacterEscapeHandler)) {
                throw new PropertyException(Messages.MUST_BE_X.format(name, CharacterEscapeHandler.class.getName(), value.getClass().getName()));
            }
            this.escapeHandler = (CharacterEscapeHandler)value;
            return;
        }
        if (PREFIX_MAPPER.equals(name)) {
            if (!(value instanceof NamespacePrefixMapper)) {
                throw new PropertyException(Messages.MUST_BE_X.format(name, NamespacePrefixMapper.class.getName(), value.getClass().getName()));
            }
            this.prefixMapper = (NamespacePrefixMapper)value;
            return;
        }
        if (XMLDECLARATION.equals(name)) {
            this.checkBoolean(name, value);
            super.setProperty("jaxb.fragment", (Boolean)value == false);
            return;
        }
        if (XML_HEADERS.equals(name)) {
            this.checkString(name, value);
            this.header = (String)value;
            return;
        }
        if (C14N.equals(name)) {
            this.checkBoolean(name, value);
            this.c14nSupport = (Boolean)value;
            return;
        }
        if (OBJECT_IDENTITY_CYCLE_DETECTION.equals(name)) {
            this.checkBoolean(name, value);
            this.serializer.setObjectIdentityCycleDetection((Boolean)value);
            return;
        }
        super.setProperty(name, value);
    }

    private void checkBoolean(String name, Object value) throws PropertyException {
        if (!(value instanceof Boolean)) {
            throw new PropertyException(Messages.MUST_BE_X.format(name, Boolean.class.getName(), value.getClass().getName()));
        }
    }

    private void checkString(String name, Object value) throws PropertyException {
        if (!(value instanceof String)) {
            throw new PropertyException(Messages.MUST_BE_X.format(name, String.class.getName(), value.getClass().getName()));
        }
    }

    @Override
    public <A extends XmlAdapter> void setAdapter(Class<A> type, A adapter) {
        if (type == null) {
            throw new IllegalArgumentException();
        }
        this.serializer.putAdapter(type, adapter);
    }

    @Override
    public <A extends XmlAdapter> A getAdapter(Class<A> type) {
        if (type == null) {
            throw new IllegalArgumentException();
        }
        if (this.serializer.containsAdapter(type)) {
            return this.serializer.getAdapter(type);
        }
        return null;
    }

    @Override
    public void setAttachmentMarshaller(AttachmentMarshaller am) {
        this.serializer.attachmentMarshaller = am;
    }

    @Override
    public AttachmentMarshaller getAttachmentMarshaller() {
        return this.serializer.attachmentMarshaller;
    }

    @Override
    public Schema getSchema() {
        return this.schema;
    }

    @Override
    public void setSchema(Schema s) {
        this.schema = s;
    }

    @Override
    public boolean handleEvent(ValidationEvent event) {
        return false;
    }

    @Override
    public Marshaller.Listener getListener() {
        return this.externalListener;
    }

    @Override
    public void setListener(Marshaller.Listener listener) {
        this.externalListener = listener;
    }
}

