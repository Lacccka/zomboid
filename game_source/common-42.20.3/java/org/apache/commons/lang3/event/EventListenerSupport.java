/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.event;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.function.FailableConsumer;

public class EventListenerSupport<L>
implements Serializable {
    private static final long serialVersionUID = 3593265990380473632L;
    private List<L> listeners = new CopyOnWriteArrayList<L>();
    private transient L proxy;
    private transient L[] prototypeArray;

    public static <T> EventListenerSupport<T> create(Class<T> listenerInterface) {
        return new EventListenerSupport<T>(listenerInterface);
    }

    private EventListenerSupport() {
    }

    public EventListenerSupport(Class<L> listenerInterface) {
        this(listenerInterface, Thread.currentThread().getContextClassLoader());
    }

    public EventListenerSupport(Class<L> listenerInterface, ClassLoader classLoader) {
        this();
        Objects.requireNonNull(listenerInterface, "listenerInterface");
        Objects.requireNonNull(classLoader, "classLoader");
        Validate.isTrue(listenerInterface.isInterface(), "Class %s is not an interface", listenerInterface.getName());
        super.initializeTransientFields(listenerInterface, classLoader);
    }

    public void addListener(L listener) {
        this.addListener(listener, true);
    }

    public void addListener(L listener, boolean allowDuplicate) {
        Objects.requireNonNull(listener, "listener");
        if (allowDuplicate || !this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    protected InvocationHandler createInvocationHandler() {
        return new ProxyInvocationHandler();
    }

    private void createProxy(Class<L> listenerInterface, ClassLoader classLoader) {
        this.proxy = listenerInterface.cast(Proxy.newProxyInstance(classLoader, new Class[]{listenerInterface}, this.createInvocationHandler()));
    }

    public L fire() {
        return this.proxy;
    }

    int getListenerCount() {
        return this.listeners.size();
    }

    public L[] getListeners() {
        return this.listeners.toArray(this.prototypeArray);
    }

    private void initializeTransientFields(Class<L> listenerInterface, ClassLoader classLoader) {
        this.prototypeArray = ArrayUtils.newInstance(listenerInterface, 0);
        this.createProxy(listenerInterface, classLoader);
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        Object[] srcListeners = (Object[])objectInputStream.readObject();
        this.listeners = new CopyOnWriteArrayList<Object>(srcListeners);
        Class<Object> listenerInterface = ArrayUtils.getComponentType(srcListeners);
        this.initializeTransientFields(listenerInterface, Thread.currentThread().getContextClassLoader());
    }

    public void removeListener(L listener) {
        Objects.requireNonNull(listener, "listener");
        this.listeners.remove(listener);
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        ArrayList<L> serializableListeners = new ArrayList<L>();
        ObjectOutputStream testObjectOutputStream = new ObjectOutputStream(new ByteArrayOutputStream());
        for (L listener : this.listeners) {
            try {
                testObjectOutputStream.writeObject(listener);
                serializableListeners.add(listener);
            }
            catch (IOException exception) {
                testObjectOutputStream = new ObjectOutputStream(new ByteArrayOutputStream());
            }
        }
        objectOutputStream.writeObject(serializableListeners.toArray(this.prototypeArray));
    }

    protected class ProxyInvocationHandler
    implements InvocationHandler {
        private final FailableConsumer<Throwable, IllegalAccessException> handler;

        public ProxyInvocationHandler() {
            this(ExceptionUtils::rethrow);
        }

        public ProxyInvocationHandler(FailableConsumer<Throwable, IllegalAccessException> handler) {
            this.handler = Objects.requireNonNull(handler);
        }

        protected void handle(Throwable t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            this.handler.accept(t);
        }

        @Override
        public Object invoke(Object unusedProxy, Method method, Object[] args2) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            for (Object listener : EventListenerSupport.this.listeners) {
                try {
                    method.invoke(listener, args2);
                }
                catch (Throwable t) {
                    this.handle(t);
                }
            }
            return null;
        }
    }
}

