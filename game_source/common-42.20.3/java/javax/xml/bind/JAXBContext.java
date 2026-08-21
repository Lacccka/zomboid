/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Map;
import javax.xml.bind.Binder;
import javax.xml.bind.ContextFinder;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;
import org.w3c.dom.Node;

public abstract class JAXBContext {
    public static final String JAXB_CONTEXT_FACTORY = "javax.xml.bind.JAXBContextFactory";

    protected JAXBContext() {
    }

    public static JAXBContext newInstance(String contextPath) throws JAXBException {
        return JAXBContext.newInstance(contextPath, JAXBContext.getContextClassLoader());
    }

    public static JAXBContext newInstance(String contextPath, ClassLoader classLoader) throws JAXBException {
        return JAXBContext.newInstance(contextPath, classLoader, Collections.emptyMap());
    }

    public static JAXBContext newInstance(String contextPath, ClassLoader classLoader, Map<String, ?> properties) throws JAXBException {
        return ContextFinder.find(JAXB_CONTEXT_FACTORY, contextPath, classLoader, properties);
    }

    public static JAXBContext newInstance(Class<?> ... classesToBeBound) throws JAXBException {
        return JAXBContext.newInstance(classesToBeBound, Collections.emptyMap());
    }

    public static JAXBContext newInstance(Class<?>[] classesToBeBound, Map<String, ?> properties) throws JAXBException {
        if (classesToBeBound == null) {
            throw new IllegalArgumentException();
        }
        for (int i = classesToBeBound.length - 1; i >= 0; --i) {
            if (classesToBeBound[i] != null) continue;
            throw new IllegalArgumentException();
        }
        return ContextFinder.find(classesToBeBound, properties);
    }

    public abstract Unmarshaller createUnmarshaller() throws JAXBException;

    public abstract Marshaller createMarshaller() throws JAXBException;

    @Deprecated
    public abstract Validator createValidator() throws JAXBException;

    public <T> Binder<T> createBinder(Class<T> domType) {
        throw new UnsupportedOperationException();
    }

    public Binder<Node> createBinder() {
        return this.createBinder(Node.class);
    }

    public JAXBIntrospector createJAXBIntrospector() {
        throw new UnsupportedOperationException();
    }

    public void generateSchema(SchemaOutputResolver outputResolver) throws IOException {
        throw new UnsupportedOperationException();
    }

    private static ClassLoader getContextClassLoader() {
        if (System.getSecurityManager() == null) {
            return Thread.currentThread().getContextClassLoader();
        }
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>(){

            @Override
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }
}

