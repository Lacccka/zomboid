/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.istack.SAXException2;
import com.sun.xml.bind.WhiteSpaceProcessor;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.impl.RuntimeModelBuilder;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElementRef;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor;
import com.sun.xml.bind.v2.runtime.reflect.ListTransducedAccessorImpl;
import com.sun.xml.bind.v2.runtime.reflect.Lister;
import com.sun.xml.bind.v2.runtime.reflect.Messages;
import com.sun.xml.bind.v2.runtime.reflect.Utils;
import com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx;
import com.sun.xml.bind.v2.runtime.unmarshaller.Patcher;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import java.io.IOException;
import java.util.concurrent.Callable;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public abstract class TransducedAccessor<BeanT> {
    public boolean useNamespace() {
        return false;
    }

    public void declareNamespace(BeanT o, XMLSerializer w) throws AccessorException, SAXException {
    }

    @Nullable
    public abstract CharSequence print(@NotNull BeanT var1) throws AccessorException, SAXException;

    public abstract void parse(BeanT var1, CharSequence var2) throws AccessorException, SAXException;

    public abstract boolean hasValue(BeanT var1) throws AccessorException;

    public static <T> TransducedAccessor<T> get(JAXBContextImpl context, RuntimeNonElementRef ref) {
        Transducer xducer = RuntimeModelBuilder.createTransducer(ref);
        RuntimePropertyInfo prop = ref.getSource();
        if (prop.isCollection()) {
            return new ListTransducedAccessorImpl(xducer, prop.getAccessor(), Lister.create(Utils.REFLECTION_NAVIGATOR.erasure(prop.getRawType()), prop.id(), prop.getAdapter()));
        }
        if (prop.id() == ID.IDREF) {
            return new IDREFTransducedAccessorImpl(prop.getAccessor());
        }
        if (xducer.useNamespace()) {
            return new CompositeContextDependentTransducedAccessorImpl(context, xducer, prop.getAccessor());
        }
        return new CompositeTransducedAccessorImpl(context, xducer, prop.getAccessor());
    }

    public abstract void writeLeafElement(XMLSerializer var1, Name var2, BeanT var3, String var4) throws SAXException, AccessorException, IOException, XMLStreamException;

    public abstract void writeText(XMLSerializer var1, BeanT var2, String var3) throws AccessorException, SAXException, IOException, XMLStreamException;

    private static final class IDREFTransducedAccessorImpl<BeanT, TargetT>
    extends DefaultTransducedAccessor<BeanT> {
        private final Accessor<BeanT, TargetT> acc;
        private final Class<TargetT> targetType;

        public IDREFTransducedAccessorImpl(Accessor<BeanT, TargetT> acc) {
            this.acc = acc;
            this.targetType = acc.getValueType();
        }

        @Override
        public String print(BeanT bean) throws AccessorException, SAXException {
            TargetT target = this.acc.get(bean);
            if (target == null) {
                return null;
            }
            XMLSerializer w = XMLSerializer.getInstance();
            try {
                String id = w.grammar.getBeanInfo(target, true).getId(target, w);
                if (id == null) {
                    w.errorMissingId(target);
                }
                return id;
            }
            catch (JAXBException e) {
                w.reportError(null, e);
                return null;
            }
        }

        private void assign(BeanT bean, TargetT t, UnmarshallingContext context) throws AccessorException {
            if (!this.targetType.isInstance(t)) {
                context.handleError(Messages.UNASSIGNABLE_TYPE.format(this.targetType, t.getClass()));
            } else {
                this.acc.set(bean, t);
            }
        }

        @Override
        public void parse(final BeanT bean, CharSequence lexical) throws AccessorException, SAXException {
            Object t;
            final String idref = WhiteSpaceProcessor.trim(lexical).toString();
            final UnmarshallingContext context = UnmarshallingContext.getInstance();
            final Callable callable = context.getObjectFromId(idref, this.acc.valueType);
            if (callable == null) {
                context.errorUnresolvedIDREF(bean, idref, context.getLocator());
                return;
            }
            try {
                t = callable.call();
            }
            catch (SAXException e) {
                throw e;
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                throw new SAXException2(e);
            }
            if (t != null) {
                this.assign(bean, t, context);
            } else {
                final LocatorEx.Snapshot loc = new LocatorEx.Snapshot(context.getLocator());
                context.addPatcher(new Patcher(){

                    @Override
                    public void run() throws SAXException {
                        try {
                            Object t = callable.call();
                            if (t == null) {
                                context.errorUnresolvedIDREF(bean, idref, loc);
                            } else {
                                IDREFTransducedAccessorImpl.this.assign(bean, t, context);
                            }
                        }
                        catch (AccessorException e) {
                            context.handleError(e);
                        }
                        catch (SAXException e) {
                            throw e;
                        }
                        catch (RuntimeException e) {
                            throw e;
                        }
                        catch (Exception e) {
                            throw new SAXException2(e);
                        }
                    }
                });
            }
        }

        @Override
        public boolean hasValue(BeanT bean) throws AccessorException {
            return this.acc.get(bean) != null;
        }
    }

    public static class CompositeTransducedAccessorImpl<BeanT, ValueT>
    extends TransducedAccessor<BeanT> {
        protected final Transducer<ValueT> xducer;
        protected final Accessor<BeanT, ValueT> acc;

        public CompositeTransducedAccessorImpl(JAXBContextImpl context, Transducer<ValueT> xducer, Accessor<BeanT, ValueT> acc) {
            this.xducer = xducer;
            this.acc = acc.optimize(context);
        }

        @Override
        public CharSequence print(BeanT bean) throws AccessorException {
            ValueT o = this.acc.get(bean);
            if (o == null) {
                return null;
            }
            return this.xducer.print(o);
        }

        @Override
        public void parse(BeanT bean, CharSequence lexical) throws AccessorException, SAXException {
            this.acc.set(bean, this.xducer.parse(lexical));
        }

        @Override
        public boolean hasValue(BeanT bean) throws AccessorException {
            return this.acc.getUnadapted(bean) != null;
        }

        @Override
        public void writeLeafElement(XMLSerializer w, Name tagName, BeanT o, String fieldName) throws SAXException, AccessorException, IOException, XMLStreamException {
            this.xducer.writeLeafElement(w, tagName, this.acc.get(o), fieldName);
        }

        @Override
        public void writeText(XMLSerializer w, BeanT o, String fieldName) throws AccessorException, SAXException, IOException, XMLStreamException {
            this.xducer.writeText(w, this.acc.get(o), fieldName);
        }
    }

    static class CompositeContextDependentTransducedAccessorImpl<BeanT, ValueT>
    extends CompositeTransducedAccessorImpl<BeanT, ValueT> {
        public CompositeContextDependentTransducedAccessorImpl(JAXBContextImpl context, Transducer<ValueT> xducer, Accessor<BeanT, ValueT> acc) {
            super(context, xducer, acc);
            assert (xducer.useNamespace());
        }

        @Override
        public boolean useNamespace() {
            return true;
        }

        @Override
        public void declareNamespace(BeanT bean, XMLSerializer w) throws AccessorException {
            Object o = this.acc.get(bean);
            if (o != null) {
                this.xducer.declareNamespace(o, w);
            }
        }

        @Override
        public void writeLeafElement(XMLSerializer w, Name tagName, BeanT o, String fieldName) throws SAXException, AccessorException, IOException, XMLStreamException {
            w.startElement(tagName, null);
            this.declareNamespace(o, w);
            w.endNamespaceDecls(null);
            w.endAttributes();
            this.xducer.writeText(w, this.acc.get(o), fieldName);
            w.endElement();
        }
    }
}

