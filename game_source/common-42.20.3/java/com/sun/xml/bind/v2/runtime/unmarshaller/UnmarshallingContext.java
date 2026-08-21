/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.istack.SAXParseException2;
import com.sun.xml.bind.IDResolver;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.api.ClassResolver;
import com.sun.xml.bind.unmarshaller.InfosetScanner;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.runtime.AssociationMap;
import com.sun.xml.bind.v2.runtime.Coordinator;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.unmarshaller.Intercepter;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx;
import com.sun.xml.bind.v2.runtime.unmarshaller.LocatorExWrapper;
import com.sun.xml.bind.v2.runtime.unmarshaller.Messages;
import com.sun.xml.bind.v2.runtime.unmarshaller.Patcher;
import com.sun.xml.bind.v2.runtime.unmarshaller.Receiver;
import com.sun.xml.bind.v2.runtime.unmarshaller.Scope;
import com.sun.xml.bind.v2.runtime.unmarshaller.StructureLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.TagName;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl;
import com.sun.xml.bind.v2.runtime.unmarshaller.XmlVisitor;
import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.XsiTypeLoader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.LocatorImpl;

public final class UnmarshallingContext
extends Coordinator
implements NamespaceContext,
ValidationEventHandler,
ErrorHandler,
XmlVisitor,
XmlVisitor.TextPredictor {
    private static final Logger logger = Logger.getLogger(UnmarshallingContext.class.getName());
    private final State root;
    private State current;
    private static final LocatorEx DUMMY_INSTANCE;
    @NotNull
    private LocatorEx locator = DUMMY_INSTANCE;
    private Object result;
    private JaxBeanInfo expectedType;
    private IDResolver idResolver;
    private boolean isUnmarshalInProgress = true;
    private boolean aborted = false;
    public final UnmarshallerImpl parent;
    private final AssociationMap assoc;
    private boolean isInplaceMode;
    private InfosetScanner scanner;
    private Object currentElement;
    private NamespaceContext environmentNamespaceContext;
    @Nullable
    public ClassResolver classResolver;
    @Nullable
    public ClassLoader classLoader;
    private static volatile int errorsCounter;
    private final Map<Class, Factory> factories = new HashMap<Class, Factory>();
    private Patcher[] patchers = null;
    private int patchersLen = 0;
    private String[] nsBind = new String[16];
    private int nsLen = 0;
    private Scope[] scopes = new Scope[16];
    private int scopeTop = 0;
    private static final Loader DEFAULT_ROOT_LOADER;
    private static final Loader EXPECTED_TYPE_ROOT_LOADER;

    public UnmarshallingContext(UnmarshallerImpl _parent, AssociationMap assoc) {
        for (int i = 0; i < this.scopes.length; ++i) {
            this.scopes[i] = new Scope(this);
        }
        this.parent = _parent;
        this.assoc = assoc;
        this.root = this.current = new State(null);
    }

    public void reset(InfosetScanner scanner, boolean isInplaceMode, JaxBeanInfo expectedType, IDResolver idResolver) {
        this.scanner = scanner;
        this.isInplaceMode = isInplaceMode;
        this.expectedType = expectedType;
        this.idResolver = idResolver;
    }

    public JAXBContextImpl getJAXBContext() {
        return this.parent.context;
    }

    public State getCurrentState() {
        return this.current;
    }

    public Loader selectRootLoader(State state, TagName tag) throws SAXException {
        try {
            Class<?> clazz;
            Loader l = this.getJAXBContext().selectRootLoader(state, tag);
            if (l != null) {
                return l;
            }
            if (this.classResolver != null && (clazz = this.classResolver.resolveElementName(tag.uri, tag.local)) != null) {
                JAXBContextImpl enhanced = this.getJAXBContext().createAugmented(clazz);
                JaxBeanInfo<?> bi = enhanced.getBeanInfo(clazz);
                return bi.getLoader(enhanced, true);
            }
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            this.handleError(e);
        }
        return null;
    }

    public void clearStates() {
        State last = this.current;
        while (last.next != null) {
            last = last.next;
        }
        while (last.prev != null) {
            last.loader = null;
            last.nil = false;
            last.receiver = null;
            last.intercepter = null;
            last.elementDefaultValue = null;
            last.target = null;
            last = last.prev;
            last.next.prev = null;
            last.next = null;
        }
        this.current = last;
    }

    public void setFactories(Object factoryInstances) {
        this.factories.clear();
        if (factoryInstances == null) {
            return;
        }
        if (factoryInstances instanceof Object[]) {
            for (Object factory2 : (Object[])factoryInstances) {
                this.addFactory(factory2);
            }
        } else {
            this.addFactory(factoryInstances);
        }
    }

    private void addFactory(Object factory2) {
        for (Method m : factory2.getClass().getMethods()) {
            if (!m.getName().startsWith("create") || m.getParameterTypes().length > 0) continue;
            Class<?> type = m.getReturnType();
            this.factories.put(type, new Factory(factory2, m));
        }
    }

    @Override
    public void startDocument(LocatorEx locator, NamespaceContext nsContext) throws SAXException {
        if (locator != null) {
            this.locator = locator;
        }
        this.environmentNamespaceContext = nsContext;
        this.result = null;
        this.current = this.root;
        this.patchersLen = 0;
        this.aborted = false;
        this.isUnmarshalInProgress = true;
        this.nsLen = 0;
        if (this.expectedType != null) {
            this.root.loader = UnmarshallingContext.EXPECTED_TYPE_ROOT_LOADER;
        } else {
            this.root.loader = UnmarshallingContext.DEFAULT_ROOT_LOADER;
        }
        this.idResolver.startDocument(this);
    }

    @Override
    public void startElement(TagName tagName) throws SAXException {
        this.pushCoordinator();
        try {
            this._startElement(tagName);
        }
        finally {
            this.popCoordinator();
        }
    }

    private void _startElement(TagName tagName) throws SAXException {
        if (this.assoc != null) {
            this.currentElement = this.scanner.getCurrentElement();
        }
        Loader h = this.current.loader;
        this.current.push();
        h.childElement(this.current, tagName);
        assert (this.current.loader != null);
        this.current.loader.startElement(this.current, tagName);
    }

    @Override
    public void text(CharSequence pcdata) throws SAXException {
        this.pushCoordinator();
        try {
            if (this.current.elementDefaultValue != null && pcdata.length() == 0) {
                pcdata = this.current.elementDefaultValue;
            }
            this.current.loader.text(this.current, pcdata);
        }
        finally {
            this.popCoordinator();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void endElement(TagName tagName) throws SAXException {
        this.pushCoordinator();
        try {
            State child = this.current;
            child.loader.leaveElement(child, tagName);
            Object target = child.target;
            Receiver recv = child.receiver;
            Intercepter intercepter = child.intercepter;
            child.pop();
            if (intercepter != null) {
                target = intercepter.intercept(this.current, target);
            }
            if (recv != null) {
                recv.receive(this.current, target);
            }
        }
        finally {
            this.popCoordinator();
        }
    }

    @Override
    public void endDocument() throws SAXException {
        this.runPatchers();
        this.idResolver.endDocument();
        this.isUnmarshalInProgress = false;
        this.currentElement = null;
        this.locator = DUMMY_INSTANCE;
        this.environmentNamespaceContext = null;
        assert (this.root == this.current);
    }

    @Override
    @Deprecated
    public boolean expectText() {
        return ((State)this.current).loader.expectText;
    }

    @Override
    @Deprecated
    public XmlVisitor.TextPredictor getPredictor() {
        return this;
    }

    @Override
    public UnmarshallingContext getContext() {
        return this;
    }

    public Object getResult() throws UnmarshalException {
        if (this.isUnmarshalInProgress) {
            throw new IllegalStateException();
        }
        if (!this.aborted) {
            return this.result;
        }
        throw new UnmarshalException((String)null);
    }

    void clearResult() {
        if (this.isUnmarshalInProgress) {
            throw new IllegalStateException();
        }
        this.result = null;
    }

    public Object createInstance(Class<?> clazz) throws SAXException {
        Factory factory2;
        if (!this.factories.isEmpty() && (factory2 = this.factories.get(clazz)) != null) {
            return factory2.createInstance();
        }
        return ClassFactory.create(clazz);
    }

    public Object createInstance(JaxBeanInfo beanInfo) throws SAXException {
        Factory factory2;
        if (!this.factories.isEmpty() && (factory2 = this.factories.get(beanInfo.jaxbType)) != null) {
            return factory2.createInstance();
        }
        try {
            return beanInfo.createInstance(this);
        }
        catch (IllegalAccessException e) {
            Loader.reportError("Unable to create an instance of " + beanInfo.jaxbType.getName(), e, false);
        }
        catch (InvocationTargetException e) {
            Loader.reportError("Unable to create an instance of " + beanInfo.jaxbType.getName(), e, false);
        }
        catch (InstantiationException e) {
            Loader.reportError("Unable to create an instance of " + beanInfo.jaxbType.getName(), e, false);
        }
        return null;
    }

    public void handleEvent(ValidationEvent event, boolean canRecover) throws SAXException {
        ValidationEventHandler eventHandler = this.parent.getEventHandler();
        boolean recover = eventHandler.handleEvent(event);
        if (!recover) {
            this.aborted = true;
        }
        if (!canRecover || !recover) {
            throw new SAXParseException2(event.getMessage(), this.locator, new UnmarshalException(event.getMessage(), event.getLinkedException()));
        }
    }

    @Override
    public boolean handleEvent(ValidationEvent event) {
        try {
            boolean recover = this.parent.getEventHandler().handleEvent(event);
            if (!recover) {
                this.aborted = true;
            }
            return recover;
        }
        catch (RuntimeException re) {
            return false;
        }
    }

    public void handleError(Exception e) throws SAXException {
        this.handleError(e, true);
    }

    public void handleError(Exception e, boolean canRecover) throws SAXException {
        this.handleEvent(new ValidationEventImpl(1, e.getMessage(), this.locator.getLocation(), e), canRecover);
    }

    public void handleError(String msg) {
        this.handleEvent(new ValidationEventImpl(1, msg, this.locator.getLocation()));
    }

    @Override
    protected ValidationEventLocator getLocation() {
        return this.locator.getLocation();
    }

    public LocatorEx getLocator() {
        return this.locator;
    }

    public void errorUnresolvedIDREF(Object bean, String idref, LocatorEx loc) throws SAXException {
        this.handleEvent(new ValidationEventImpl(1, Messages.UNRESOLVED_IDREF.format(idref), loc.getLocation()), true);
    }

    public void addPatcher(Patcher job) {
        if (this.patchers == null) {
            this.patchers = new Patcher[32];
        }
        if (this.patchers.length == this.patchersLen) {
            Patcher[] buf = new Patcher[this.patchersLen * 2];
            System.arraycopy(this.patchers, 0, buf, 0, this.patchersLen);
            this.patchers = buf;
        }
        this.patchers[this.patchersLen++] = job;
    }

    private void runPatchers() throws SAXException {
        if (this.patchers != null) {
            for (int i = 0; i < this.patchersLen; ++i) {
                this.patchers[i].run();
                this.patchers[i] = null;
            }
        }
    }

    public String addToIdTable(String id) throws SAXException {
        Object o = this.current.target;
        if (o == null) {
            o = this.current.prev.target;
        }
        this.idResolver.bind(id, o);
        return id;
    }

    public Callable getObjectFromId(String id, Class targetType) throws SAXException {
        return this.idResolver.resolve(id, targetType);
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) {
        if (this.nsBind.length == this.nsLen) {
            String[] n = new String[this.nsLen * 2];
            System.arraycopy(this.nsBind, 0, n, 0, this.nsLen);
            this.nsBind = n;
        }
        this.nsBind[this.nsLen++] = prefix;
        this.nsBind[this.nsLen++] = uri;
    }

    @Override
    public void endPrefixMapping(String prefix) {
        this.nsLen -= 2;
    }

    private String resolveNamespacePrefix(String prefix) {
        if (prefix.equals("xml")) {
            return "http://www.w3.org/XML/1998/namespace";
        }
        for (int i = this.nsLen - 2; i >= 0; i -= 2) {
            if (!prefix.equals(this.nsBind[i])) continue;
            return this.nsBind[i + 1];
        }
        if (this.environmentNamespaceContext != null) {
            return this.environmentNamespaceContext.getNamespaceURI(prefix.intern());
        }
        if (prefix.equals("")) {
            return "";
        }
        return null;
    }

    public String[] getNewlyDeclaredPrefixes() {
        return this.getPrefixList(this.current.prev.numNsDecl);
    }

    public String[] getAllDeclaredPrefixes() {
        return this.getPrefixList(0);
    }

    private String[] getPrefixList(int startIndex) {
        int size = (this.current.numNsDecl - startIndex) / 2;
        String[] r = new String[size];
        for (int i = 0; i < r.length; ++i) {
            r[i] = this.nsBind[startIndex + i * 2];
        }
        return r;
    }

    @Override
    public Iterator<String> getPrefixes(String uri) {
        return Collections.unmodifiableList(this.getAllPrefixesInList(uri)).iterator();
    }

    private List<String> getAllPrefixesInList(String uri) {
        ArrayList<String> a = new ArrayList<String>();
        if (uri == null) {
            throw new IllegalArgumentException();
        }
        if (uri.equals("http://www.w3.org/XML/1998/namespace")) {
            a.add("xml");
            return a;
        }
        if (uri.equals("http://www.w3.org/2000/xmlns/")) {
            a.add("xmlns");
            return a;
        }
        for (int i = this.nsLen - 2; i >= 0; i -= 2) {
            if (!uri.equals(this.nsBind[i + 1]) || !this.getNamespaceURI(this.nsBind[i]).equals(this.nsBind[i + 1])) continue;
            a.add(this.nsBind[i]);
        }
        return a;
    }

    @Override
    public String getPrefix(String uri) {
        if (uri == null) {
            throw new IllegalArgumentException();
        }
        if (uri.equals("http://www.w3.org/XML/1998/namespace")) {
            return "xml";
        }
        if (uri.equals("http://www.w3.org/2000/xmlns/")) {
            return "xmlns";
        }
        for (int i = this.nsLen - 2; i >= 0; i -= 2) {
            if (!uri.equals(this.nsBind[i + 1]) || !this.getNamespaceURI(this.nsBind[i]).equals(this.nsBind[i + 1])) continue;
            return this.nsBind[i];
        }
        if (this.environmentNamespaceContext != null) {
            return this.environmentNamespaceContext.getPrefix(uri);
        }
        return null;
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException();
        }
        if (prefix.equals("xmlns")) {
            return "http://www.w3.org/2000/xmlns/";
        }
        return this.resolveNamespacePrefix(prefix);
    }

    public void startScope(int frameSize) {
        this.scopeTop += frameSize;
        if (this.scopeTop >= this.scopes.length) {
            Scope[] s = new Scope[Math.max(this.scopeTop + 1, this.scopes.length * 2)];
            System.arraycopy(this.scopes, 0, s, 0, this.scopes.length);
            for (int i = this.scopes.length; i < s.length; ++i) {
                s[i] = new Scope(this);
            }
            this.scopes = s;
        }
    }

    public void endScope(int frameSize) throws SAXException {
        try {
            while (frameSize > 0) {
                this.scopes[this.scopeTop].finish();
                --frameSize;
                --this.scopeTop;
            }
        }
        catch (AccessorException e) {
            this.handleError(e);
            while (frameSize > 0) {
                this.scopes[this.scopeTop--] = new Scope(this);
                --frameSize;
            }
        }
    }

    public Scope getScope(int offset) {
        return this.scopes[this.scopeTop - offset];
    }

    public void recordInnerPeer(Object innerPeer) {
        if (this.assoc != null) {
            this.assoc.addInner(this.currentElement, innerPeer);
        }
    }

    public Object getInnerPeer() {
        if (this.assoc != null && this.isInplaceMode) {
            return this.assoc.getInnerPeer(this.currentElement);
        }
        return null;
    }

    public void recordOuterPeer(Object outerPeer) {
        if (this.assoc != null) {
            this.assoc.addOuter(this.currentElement, outerPeer);
        }
    }

    public Object getOuterPeer() {
        if (this.assoc != null && this.isInplaceMode) {
            return this.assoc.getOuterPeer(this.currentElement);
        }
        return null;
    }

    public String getXMIMEContentType() {
        Object t = this.current.target;
        if (t == null) {
            return null;
        }
        return this.getJAXBContext().getXMIMEContentType(t);
    }

    public static UnmarshallingContext getInstance() {
        return (UnmarshallingContext)Coordinator._getInstance();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Collection<QName> getCurrentExpectedElements() {
        this.pushCoordinator();
        try {
            State s = this.getCurrentState();
            Loader l = s.loader;
            Collection<QName> collection = l != null ? l.getExpectedChildElements() : null;
            return collection;
        }
        finally {
            this.popCoordinator();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Collection<QName> getCurrentExpectedAttributes() {
        this.pushCoordinator();
        try {
            State s = this.getCurrentState();
            Loader l = s.loader;
            Collection<QName> collection = l != null ? l.getExpectedAttributes() : null;
            return collection;
        }
        finally {
            this.popCoordinator();
        }
    }

    public StructureLoader getStructureLoader() {
        if (this.current.loader instanceof StructureLoader) {
            return (StructureLoader)this.current.loader;
        }
        return null;
    }

    public boolean shouldErrorBeReported() throws SAXException {
        if (logger.isLoggable(Level.FINEST)) {
            return true;
        }
        if (errorsCounter >= 0 && --errorsCounter == 0) {
            this.handleEvent(new ValidationEventImpl(0, Messages.ERRORS_LIMIT_EXCEEDED.format(new Object[0]), this.getLocator().getLocation(), null), true);
        }
        return errorsCounter >= 0;
    }

    static {
        LocatorImpl loc = new LocatorImpl();
        loc.setPublicId(null);
        loc.setSystemId(null);
        loc.setLineNumber(-1);
        loc.setColumnNumber(-1);
        DUMMY_INSTANCE = new LocatorExWrapper(loc);
        errorsCounter = 10;
        DEFAULT_ROOT_LOADER = new DefaultRootLoader();
        EXPECTED_TYPE_ROOT_LOADER = new ExpectedTypeRootLoader();
    }

    private static final class ExpectedTypeRootLoader
    extends Loader
    implements Receiver {
        private ExpectedTypeRootLoader() {
        }

        @Override
        public void childElement(State state, TagName ea) {
            UnmarshallingContext context = state.getContext();
            QName qn = new QName(ea.uri, ea.local);
            state.prev.target = new JAXBElement<Object>(qn, ((UnmarshallingContext)context).expectedType.jaxbType, null, null);
            state.receiver = this;
            state.loader = new XsiNilLoader(context.expectedType.getLoader(null, true));
        }

        @Override
        public void receive(State state, Object o) {
            JAXBElement e = (JAXBElement)state.target;
            e.setValue(o);
            state.getContext().recordOuterPeer(e);
            state.getContext().result = e;
        }
    }

    private static final class DefaultRootLoader
    extends Loader
    implements Receiver {
        private DefaultRootLoader() {
        }

        @Override
        public void childElement(State state, TagName ea) throws SAXException {
            Loader loader = state.getContext().selectRootLoader(state, ea);
            if (loader != null) {
                state.loader = loader;
                state.receiver = this;
                return;
            }
            JaxBeanInfo beanInfo = XsiTypeLoader.parseXsiType(state, ea, null);
            if (beanInfo == null) {
                this.reportUnexpectedChildElement(ea, false);
                return;
            }
            state.loader = beanInfo.getLoader(null, false);
            state.prev.backup = new JAXBElement<Object>(ea.createQName(), Object.class, null);
            state.receiver = this;
        }

        @Override
        public Collection<QName> getExpectedChildElements() {
            return UnmarshallingContext.getInstance().getJAXBContext().getValidRootNames();
        }

        @Override
        public void receive(State state, Object o) {
            if (state.backup != null) {
                ((JAXBElement)state.backup).setValue(o);
                o = state.backup;
            }
            if (state.nil) {
                ((JAXBElement)o).setNil(true);
            }
            state.getContext().result = o;
        }
    }

    private static class Factory {
        private final Object factorInstance;
        private final Method method;

        public Factory(Object factorInstance, Method method) {
            this.factorInstance = factorInstance;
            this.method = method;
        }

        public Object createInstance() throws SAXException {
            try {
                return this.method.invoke(this.factorInstance, new Object[0]);
            }
            catch (IllegalAccessException e) {
                UnmarshallingContext.getInstance().handleError(e, false);
            }
            catch (InvocationTargetException e) {
                UnmarshallingContext.getInstance().handleError(e, false);
            }
            return null;
        }
    }

    public final class State {
        private Loader loader;
        private Receiver receiver;
        private Intercepter intercepter;
        private Object target;
        private Object backup;
        private int numNsDecl;
        private String elementDefaultValue;
        private State prev;
        private State next;
        private boolean nil = false;
        private boolean mixed = false;

        public UnmarshallingContext getContext() {
            return UnmarshallingContext.this;
        }

        private State(State prev) {
            this.prev = prev;
            if (prev != null) {
                prev.next = this;
                if (prev.mixed) {
                    this.mixed = true;
                }
            }
        }

        private void push() {
            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "State.push");
            }
            if (this.next == null) {
                assert (UnmarshallingContext.this.current == this);
                this.next = new State(this);
            }
            this.nil = false;
            State n = this.next;
            n.numNsDecl = UnmarshallingContext.this.nsLen;
            UnmarshallingContext.this.current = n;
        }

        private void pop() {
            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "State.pop");
            }
            assert (this.prev != null);
            this.loader = null;
            this.nil = false;
            this.mixed = false;
            this.receiver = null;
            this.intercepter = null;
            this.elementDefaultValue = null;
            this.target = null;
            UnmarshallingContext.this.current = this.prev;
            this.next = null;
        }

        public boolean isMixed() {
            return this.mixed;
        }

        public Object getTarget() {
            return this.target;
        }

        public void setLoader(Loader loader) {
            if (loader instanceof StructureLoader) {
                this.mixed = !((StructureLoader)loader).getBeanInfo().hasElementOnlyContentModel();
            }
            this.loader = loader;
        }

        public void setReceiver(Receiver receiver) {
            this.receiver = receiver;
        }

        public State getPrev() {
            return this.prev;
        }

        public void setIntercepter(Intercepter intercepter) {
            this.intercepter = intercepter;
        }

        public void setBackup(Object backup) {
            this.backup = backup;
        }

        public void setTarget(Object target) {
            this.target = target;
        }

        public Object getBackup() {
            return this.backup;
        }

        public boolean isNil() {
            return this.nil;
        }

        public void setNil(boolean nil) {
            this.nil = nil;
        }

        public Loader getLoader() {
            return this.loader;
        }

        public String getElementDefaultValue() {
            return this.elementDefaultValue;
        }

        public void setElementDefaultValue(String elementDefaultValue) {
            this.elementDefaultValue = elementDefaultValue;
        }
    }
}

