/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime;

import com.sun.istack.SAXException2;
import com.sun.xml.bind.CycleRecoverable;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import com.sun.xml.bind.util.ValidationEventLocatorExImpl;
import com.sun.xml.bind.v2.runtime.ContentHandlerAdaptor;
import com.sun.xml.bind.v2.runtime.Coordinator;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.MarshallerImpl;
import com.sun.xml.bind.v2.runtime.Messages;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.NameList;
import com.sun.xml.bind.v2.runtime.NamespaceContext2;
import com.sun.xml.bind.v2.runtime.output.MTOMXmlOutput;
import com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl;
import com.sun.xml.bind.v2.runtime.output.Pcdata;
import com.sun.xml.bind.v2.runtime.output.XmlOutput;
import com.sun.xml.bind.v2.runtime.property.Property;
import com.sun.xml.bind.v2.runtime.unmarshaller.Base64Data;
import com.sun.xml.bind.v2.runtime.unmarshaller.IntData;
import com.sun.xml.bind.v2.util.CollisionCheckStack;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.activation.MimeType;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.annotation.DomHandler;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.helpers.NotIdentifiableEventImpl;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import org.xml.sax.SAXException;

public final class XMLSerializer
extends Coordinator {
    public final JAXBContextImpl grammar;
    private XmlOutput out;
    public final NameList nameList;
    public final int[] knownUri2prefixIndexMap;
    private final NamespaceContextImpl nsContext;
    private NamespaceContextImpl.Element nse;
    ThreadLocal<Property> currentProperty = new ThreadLocal();
    private boolean textHasAlreadyPrinted = false;
    private boolean seenRoot = false;
    private final MarshallerImpl marshaller;
    private final Set<Object> idReferencedObjects = new HashSet<Object>();
    private final Set<Object> objectsWithId = new HashSet<Object>();
    private final CollisionCheckStack<Object> cycleDetectionStack = new CollisionCheckStack();
    private String schemaLocation;
    private String noNsSchemaLocation;
    private Transformer identityTransformer;
    private ContentHandlerAdaptor contentHandlerAdapter;
    private boolean fragment;
    private Base64Data base64Data;
    private final IntData intData = new IntData();
    public AttachmentMarshaller attachmentMarshaller;
    private MimeType expectedMimeType;
    private boolean inlineBinaryFlag;
    private QName schemaType;

    XMLSerializer(MarshallerImpl _owner) {
        this.marshaller = _owner;
        this.grammar = this.marshaller.context;
        this.nsContext = new NamespaceContextImpl(this);
        this.nameList = this.marshaller.context.nameList;
        this.knownUri2prefixIndexMap = new int[this.nameList.namespaceURIs.length];
    }

    public Base64Data getCachedBase64DataInstance() {
        return new Base64Data();
    }

    private String getIdFromObject(Object identifiableObject) throws SAXException, JAXBException {
        return this.grammar.getBeanInfo(identifiableObject, true).getId(identifiableObject, this);
    }

    private void handleMissingObjectError(String fieldName) throws SAXException, IOException, XMLStreamException {
        this.reportMissingObjectError(fieldName);
        this.endNamespaceDecls(null);
        this.endAttributes();
    }

    public void reportError(ValidationEvent ve) throws SAXException {
        ValidationEventHandler handler;
        try {
            handler = this.marshaller.getEventHandler();
        }
        catch (JAXBException e) {
            throw new SAXException2(e);
        }
        if (!handler.handleEvent(ve)) {
            if (ve.getLinkedException() instanceof Exception) {
                throw new SAXException2((Exception)ve.getLinkedException());
            }
            throw new SAXException2(ve.getMessage());
        }
    }

    public final void reportError(String fieldName, Throwable t) throws SAXException {
        ValidationEventImpl ve = new ValidationEventImpl(1, t.getMessage(), this.getCurrentLocation(fieldName), t);
        this.reportError(ve);
    }

    public void startElement(Name tagName, Object outerPeer) {
        this.startElement();
        this.nse.setTagName(tagName, outerPeer);
    }

    public void startElement(String nsUri, String localName, String preferredPrefix, Object outerPeer) {
        this.startElement();
        int idx = this.nsContext.declareNsUri(nsUri, preferredPrefix, false);
        this.nse.setTagName(idx, localName, outerPeer);
    }

    public void startElementForce(String nsUri, String localName, String forcedPrefix, Object outerPeer) {
        this.startElement();
        int idx = this.nsContext.force(nsUri, forcedPrefix);
        this.nse.setTagName(idx, localName, outerPeer);
    }

    public void endNamespaceDecls(Object innerPeer) throws IOException, XMLStreamException {
        this.nsContext.collectionMode = false;
        this.nse.startElement(this.out, innerPeer);
    }

    public void endAttributes() throws SAXException, IOException, XMLStreamException {
        if (!this.seenRoot) {
            this.seenRoot = true;
            if (this.schemaLocation != null || this.noNsSchemaLocation != null) {
                int p = this.nsContext.getPrefixIndex("http://www.w3.org/2001/XMLSchema-instance");
                if (this.schemaLocation != null) {
                    this.out.attribute(p, "schemaLocation", this.schemaLocation);
                }
                if (this.noNsSchemaLocation != null) {
                    this.out.attribute(p, "noNamespaceSchemaLocation", this.noNsSchemaLocation);
                }
            }
        }
        this.out.endStartTag();
    }

    public void endElement() throws SAXException, IOException, XMLStreamException {
        this.nse.endElement(this.out);
        this.nse = this.nse.pop();
        this.textHasAlreadyPrinted = false;
    }

    public void leafElement(Name tagName, String data, String fieldName) throws SAXException, IOException, XMLStreamException {
        if (this.seenRoot) {
            this.textHasAlreadyPrinted = false;
            this.nse = this.nse.push();
            this.out.beginStartTag(tagName);
            this.out.endStartTag();
            if (data != null) {
                try {
                    this.out.text(data, false);
                }
                catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(Messages.ILLEGAL_CONTENT.format(fieldName, e.getMessage()));
                }
            }
            this.out.endTag(tagName);
            this.nse = this.nse.pop();
        } else {
            this.startElement(tagName, null);
            this.endNamespaceDecls(null);
            this.endAttributes();
            try {
                this.out.text(data, false);
            }
            catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(Messages.ILLEGAL_CONTENT.format(fieldName, e.getMessage()));
            }
            this.endElement();
        }
    }

    public void leafElement(Name tagName, Pcdata data, String fieldName) throws SAXException, IOException, XMLStreamException {
        if (this.seenRoot) {
            this.textHasAlreadyPrinted = false;
            this.nse = this.nse.push();
            this.out.beginStartTag(tagName);
            this.out.endStartTag();
            if (data != null) {
                this.out.text(data, false);
            }
            this.out.endTag(tagName);
            this.nse = this.nse.pop();
        } else {
            this.startElement(tagName, null);
            this.endNamespaceDecls(null);
            this.endAttributes();
            this.out.text(data, false);
            this.endElement();
        }
    }

    public void leafElement(Name tagName, int data, String fieldName) throws SAXException, IOException, XMLStreamException {
        this.intData.reset(data);
        this.leafElement(tagName, this.intData, fieldName);
    }

    public void text(String text, String fieldName) throws SAXException, IOException, XMLStreamException {
        if (text == null) {
            this.reportMissingObjectError(fieldName);
            return;
        }
        this.out.text(text, this.textHasAlreadyPrinted);
        this.textHasAlreadyPrinted = true;
    }

    public void text(Pcdata text, String fieldName) throws SAXException, IOException, XMLStreamException {
        if (text == null) {
            this.reportMissingObjectError(fieldName);
            return;
        }
        this.out.text(text, this.textHasAlreadyPrinted);
        this.textHasAlreadyPrinted = true;
    }

    public void attribute(String uri, String local, String value) throws SAXException {
        int prefix = uri.length() == 0 ? -1 : this.nsContext.getPrefixIndex(uri);
        try {
            this.out.attribute(prefix, local, value);
        }
        catch (IOException e) {
            throw new SAXException2(e);
        }
        catch (XMLStreamException e) {
            throw new SAXException2(e);
        }
    }

    public void attribute(Name name, CharSequence value) throws IOException, XMLStreamException {
        this.out.attribute(name, value.toString());
    }

    public NamespaceContext2 getNamespaceContext() {
        return this.nsContext;
    }

    public String onID(Object owner, String value) {
        this.objectsWithId.add(owner);
        return value;
    }

    public String onIDREF(Object obj) throws SAXException {
        String id;
        try {
            id = this.getIdFromObject(obj);
        }
        catch (JAXBException e) {
            this.reportError(null, e);
            return null;
        }
        this.idReferencedObjects.add(obj);
        if (id == null) {
            this.reportError(new NotIdentifiableEventImpl(1, Messages.NOT_IDENTIFIABLE.format(new Object[0]), new ValidationEventLocatorImpl(obj)));
        }
        return id;
    }

    public void childAsRoot(Object obj) throws JAXBException, IOException, SAXException, XMLStreamException {
        JaxBeanInfo beanInfo = this.grammar.getBeanInfo(obj, true);
        this.cycleDetectionStack.pushNocheck(obj);
        boolean lookForLifecycleMethods = beanInfo.lookForLifecycleMethods();
        if (lookForLifecycleMethods) {
            this.fireBeforeMarshalEvents(beanInfo, obj);
        }
        beanInfo.serializeRoot(obj, this);
        if (lookForLifecycleMethods) {
            this.fireAfterMarshalEvents(beanInfo, obj);
        }
        this.cycleDetectionStack.pop();
    }

    private Object pushObject(Object obj, String fieldName) throws SAXException {
        if (!this.cycleDetectionStack.push(obj)) {
            return obj;
        }
        if (obj instanceof CycleRecoverable) {
            if ((obj = ((CycleRecoverable)obj).onCycleDetected(new CycleRecoverable.Context(){

                @Override
                public Marshaller getMarshaller() {
                    return XMLSerializer.this.marshaller;
                }
            })) != null) {
                this.cycleDetectionStack.pop();
                return this.pushObject(obj, fieldName);
            }
            return null;
        }
        this.reportError(new ValidationEventImpl(1, Messages.CYCLE_IN_MARSHALLER.format(this.cycleDetectionStack.getCycleString()), this.getCurrentLocation(fieldName), null));
        return null;
    }

    public final void childAsSoleContent(Object child, String fieldName) throws SAXException, IOException, XMLStreamException {
        if (child == null) {
            this.handleMissingObjectError(fieldName);
        } else {
            JaxBeanInfo beanInfo;
            if ((child = this.pushObject(child, fieldName)) == null) {
                this.endNamespaceDecls(null);
                this.endAttributes();
                this.cycleDetectionStack.pop();
            }
            try {
                beanInfo = this.grammar.getBeanInfo(child, true);
            }
            catch (JAXBException e) {
                this.reportError(fieldName, e);
                this.endNamespaceDecls(null);
                this.endAttributes();
                this.cycleDetectionStack.pop();
                return;
            }
            boolean lookForLifecycleMethods = beanInfo.lookForLifecycleMethods();
            if (lookForLifecycleMethods) {
                this.fireBeforeMarshalEvents(beanInfo, child);
            }
            beanInfo.serializeURIs(child, this);
            this.endNamespaceDecls(child);
            beanInfo.serializeAttributes(child, this);
            this.endAttributes();
            beanInfo.serializeBody(child, this);
            if (lookForLifecycleMethods) {
                this.fireAfterMarshalEvents(beanInfo, child);
            }
            this.cycleDetectionStack.pop();
        }
    }

    public final void childAsXsiType(Object child, String fieldName, JaxBeanInfo expected, boolean nillable) throws SAXException, IOException, XMLStreamException {
        if (child == null) {
            this.handleMissingObjectError(fieldName);
        } else {
            if ((child = this.pushObject(child, fieldName)) == null) {
                this.endNamespaceDecls(null);
                this.endAttributes();
                return;
            }
            boolean asExpected = child.getClass() == expected.jaxbType;
            JaxBeanInfo actual = expected;
            QName actualTypeName = null;
            if (asExpected && actual.lookForLifecycleMethods()) {
                this.fireBeforeMarshalEvents(actual, child);
            }
            if (!asExpected) {
                try {
                    actual = this.grammar.getBeanInfo(child, true);
                    if (actual.lookForLifecycleMethods()) {
                        this.fireBeforeMarshalEvents(actual, child);
                    }
                }
                catch (JAXBException e) {
                    this.reportError(fieldName, e);
                    this.endNamespaceDecls(null);
                    this.endAttributes();
                    return;
                }
                if (actual == expected) {
                    asExpected = true;
                } else {
                    actualTypeName = actual.getTypeName(child);
                    if (actualTypeName == null) {
                        this.reportError(new ValidationEventImpl(1, Messages.SUBSTITUTED_BY_ANONYMOUS_TYPE.format(expected.jaxbType.getName(), child.getClass().getName(), actual.jaxbType.getName()), this.getCurrentLocation(fieldName)));
                    } else {
                        this.getNamespaceContext().declareNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi", true);
                        this.getNamespaceContext().declareNamespace(actualTypeName.getNamespaceURI(), null, false);
                    }
                }
            }
            actual.serializeURIs(child, this);
            if (nillable) {
                this.getNamespaceContext().declareNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi", true);
            }
            this.endNamespaceDecls(child);
            if (!asExpected) {
                this.attribute("http://www.w3.org/2001/XMLSchema-instance", "type", DatatypeConverter.printQName(actualTypeName, this.getNamespaceContext()));
            }
            actual.serializeAttributes(child, this);
            boolean nilDefined = actual.isNilIncluded();
            if (nillable && !nilDefined) {
                this.attribute("http://www.w3.org/2001/XMLSchema-instance", "nil", "true");
            }
            this.endAttributes();
            actual.serializeBody(child, this);
            if (actual.lookForLifecycleMethods()) {
                this.fireAfterMarshalEvents(actual, child);
            }
            this.cycleDetectionStack.pop();
        }
    }

    private void fireAfterMarshalEvents(JaxBeanInfo beanInfo, Object currentTarget) {
        Marshaller.Listener externalListener;
        if (beanInfo.hasAfterMarshalMethod()) {
            Method m = beanInfo.getLifecycleMethods().afterMarshal;
            this.fireMarshalEvent(currentTarget, m);
        }
        if ((externalListener = this.marshaller.getListener()) != null) {
            externalListener.afterMarshal(currentTarget);
        }
    }

    private void fireBeforeMarshalEvents(JaxBeanInfo beanInfo, Object currentTarget) {
        Marshaller.Listener externalListener;
        if (beanInfo.hasBeforeMarshalMethod()) {
            Method m = beanInfo.getLifecycleMethods().beforeMarshal;
            this.fireMarshalEvent(currentTarget, m);
        }
        if ((externalListener = this.marshaller.getListener()) != null) {
            externalListener.beforeMarshal(currentTarget);
        }
    }

    private void fireMarshalEvent(Object target, Method m) {
        try {
            m.invoke(target, this.marshaller);
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void attWildcardAsURIs(Map<QName, String> attributes, String fieldName) {
        if (attributes == null) {
            return;
        }
        for (Map.Entry<QName, String> e : attributes.entrySet()) {
            QName n = e.getKey();
            String nsUri = n.getNamespaceURI();
            if (nsUri.length() <= 0) continue;
            String p = n.getPrefix();
            if (p.length() == 0) {
                p = null;
            }
            this.nsContext.declareNsUri(nsUri, p, true);
        }
    }

    public void attWildcardAsAttributes(Map<QName, String> attributes, String fieldName) throws SAXException {
        if (attributes == null) {
            return;
        }
        for (Map.Entry<QName, String> e : attributes.entrySet()) {
            QName n = e.getKey();
            this.attribute(n.getNamespaceURI(), n.getLocalPart(), e.getValue());
        }
    }

    public final void writeXsiNilTrue() throws SAXException, IOException, XMLStreamException {
        this.getNamespaceContext().declareNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi", true);
        this.endNamespaceDecls(null);
        this.attribute("http://www.w3.org/2001/XMLSchema-instance", "nil", "true");
        this.endAttributes();
    }

    public <E> void writeDom(E element, DomHandler<E, ?> domHandler, Object parentBean, String fieldName) throws SAXException {
        Source source2 = domHandler.marshal(element, this);
        if (this.contentHandlerAdapter == null) {
            this.contentHandlerAdapter = new ContentHandlerAdaptor(this);
        }
        try {
            this.getIdentityTransformer().transform(source2, new SAXResult(this.contentHandlerAdapter));
        }
        catch (TransformerException e) {
            this.reportError(fieldName, e);
        }
    }

    public Transformer getIdentityTransformer() {
        if (this.identityTransformer == null) {
            this.identityTransformer = JAXBContextImpl.createTransformer(this.grammar.disableSecurityProcessing);
        }
        return this.identityTransformer;
    }

    public void setPrefixMapper(NamespacePrefixMapper prefixMapper) {
        this.nsContext.setPrefixMapper(prefixMapper);
    }

    public void startDocument(XmlOutput out, boolean fragment, String schemaLocation, String noNsSchemaLocation) throws IOException, SAXException, XMLStreamException {
        this.pushCoordinator();
        this.nsContext.reset();
        this.nse = this.nsContext.getCurrent();
        if (this.attachmentMarshaller != null && this.attachmentMarshaller.isXOPPackage()) {
            out = new MTOMXmlOutput(out);
        }
        this.out = out;
        this.objectsWithId.clear();
        this.idReferencedObjects.clear();
        this.textHasAlreadyPrinted = false;
        this.seenRoot = false;
        this.schemaLocation = schemaLocation;
        this.noNsSchemaLocation = noNsSchemaLocation;
        this.fragment = fragment;
        this.inlineBinaryFlag = false;
        this.expectedMimeType = null;
        this.cycleDetectionStack.reset();
        out.startDocument(this, fragment, this.knownUri2prefixIndexMap, this.nsContext);
    }

    public void endDocument() throws IOException, SAXException, XMLStreamException {
        this.out.endDocument(this.fragment);
    }

    public void close() {
        this.out = null;
        this.clearCurrentProperty();
        this.popCoordinator();
    }

    public void addInscopeBinding(String nsUri, String prefix) {
        this.nsContext.put(nsUri, prefix);
    }

    public String getXMIMEContentType() {
        String v = this.grammar.getXMIMEContentType(this.cycleDetectionStack.peek());
        if (v != null) {
            return v;
        }
        if (this.expectedMimeType != null) {
            return this.expectedMimeType.toString();
        }
        return null;
    }

    private void startElement() {
        this.nse = this.nse.push();
        if (!this.seenRoot) {
            String[] pairs;
            if (this.grammar.getXmlNsSet() != null) {
                for (XmlNs xmlNs : this.grammar.getXmlNsSet()) {
                    this.nsContext.declareNsUri(xmlNs.namespaceURI(), xmlNs.prefix() == null ? "" : xmlNs.prefix(), xmlNs.prefix() != null);
                }
            }
            String[] knownUris = this.nameList.namespaceURIs;
            for (int i = 0; i < knownUris.length; ++i) {
                this.knownUri2prefixIndexMap[i] = this.nsContext.declareNsUri(knownUris[i], null, this.nameList.nsUriCannotBeDefaulted[i]);
            }
            String[] uris = this.nsContext.getPrefixMapper().getPreDeclaredNamespaceUris();
            if (uris != null) {
                for (String uri : uris) {
                    if (uri == null) continue;
                    this.nsContext.declareNsUri(uri, null, false);
                }
            }
            if ((pairs = this.nsContext.getPrefixMapper().getPreDeclaredNamespaceUris2()) != null) {
                for (int i = 0; i < pairs.length; i += 2) {
                    String prefix = pairs[i];
                    String nsUri = pairs[i + 1];
                    if (prefix == null || nsUri == null) continue;
                    this.nsContext.put(nsUri, prefix);
                }
            }
            if (this.schemaLocation != null || this.noNsSchemaLocation != null) {
                this.nsContext.declareNsUri("http://www.w3.org/2001/XMLSchema-instance", "xsi", true);
            }
        }
        this.nsContext.collectionMode = true;
        this.textHasAlreadyPrinted = false;
    }

    public MimeType setExpectedMimeType(MimeType expectedMimeType) {
        MimeType old = this.expectedMimeType;
        this.expectedMimeType = expectedMimeType;
        return old;
    }

    public boolean setInlineBinaryFlag(boolean value) {
        boolean old = this.inlineBinaryFlag;
        this.inlineBinaryFlag = value;
        return old;
    }

    public boolean getInlineBinaryFlag() {
        return this.inlineBinaryFlag;
    }

    public QName setSchemaType(QName st) {
        QName old = this.schemaType;
        this.schemaType = st;
        return old;
    }

    public QName getSchemaType() {
        return this.schemaType;
    }

    public void setObjectIdentityCycleDetection(boolean val) {
        this.cycleDetectionStack.setUseIdentity(val);
    }

    public boolean getObjectIdentityCycleDetection() {
        return this.cycleDetectionStack.getUseIdentity();
    }

    void reconcileID() throws SAXException {
        this.idReferencedObjects.removeAll(this.objectsWithId);
        for (Object idObj : this.idReferencedObjects) {
            try {
                String id = this.getIdFromObject(idObj);
                this.reportError(new NotIdentifiableEventImpl(1, Messages.DANGLING_IDREF.format(id), new ValidationEventLocatorImpl(idObj)));
            }
            catch (JAXBException jAXBException) {}
        }
        this.idReferencedObjects.clear();
        this.objectsWithId.clear();
    }

    public boolean handleError(Exception e) {
        return this.handleError(e, this.cycleDetectionStack.peek(), null);
    }

    public boolean handleError(Exception e, Object source2, String fieldName) {
        return this.handleEvent(new ValidationEventImpl(1, e.getMessage(), new ValidationEventLocatorExImpl(source2, fieldName), e));
    }

    @Override
    public boolean handleEvent(ValidationEvent event) {
        try {
            return this.marshaller.getEventHandler().handleEvent(event);
        }
        catch (JAXBException e) {
            throw new Error(e);
        }
    }

    private void reportMissingObjectError(String fieldName) throws SAXException {
        this.reportError(new ValidationEventImpl(1, Messages.MISSING_OBJECT.format(fieldName), this.getCurrentLocation(fieldName), new NullPointerException()));
    }

    public void errorMissingId(Object obj) throws SAXException {
        this.reportError(new ValidationEventImpl(1, Messages.MISSING_ID.format(obj), new ValidationEventLocatorImpl(obj)));
    }

    public ValidationEventLocator getCurrentLocation(String fieldName) {
        return new ValidationEventLocatorExImpl(this.cycleDetectionStack.peek(), fieldName);
    }

    @Override
    protected ValidationEventLocator getLocation() {
        return this.getCurrentLocation(null);
    }

    public Property getCurrentProperty() {
        return this.currentProperty.get();
    }

    public void clearCurrentProperty() {
        if (this.currentProperty != null) {
            this.currentProperty.remove();
        }
    }

    public static XMLSerializer getInstance() {
        return (XMLSerializer)Coordinator._getInstance();
    }
}

