/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.concurrent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import org.apache.commons.lang3.concurrent.BackgroundInitializer;
import org.apache.commons.lang3.concurrent.ConcurrentException;

public class MultiBackgroundInitializer
extends BackgroundInitializer<MultiBackgroundInitializerResults> {
    private final Map<String, BackgroundInitializer<?>> childInitializers = new HashMap();

    public MultiBackgroundInitializer() {
    }

    public MultiBackgroundInitializer(ExecutorService exec) {
        super(exec);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addInitializer(String name, BackgroundInitializer<?> backgroundInitializer) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(backgroundInitializer, "backgroundInitializer");
        MultiBackgroundInitializer multiBackgroundInitializer = this;
        synchronized (multiBackgroundInitializer) {
            if (this.isStarted()) {
                throw new IllegalStateException("addInitializer() must not be called after start()!");
            }
            this.childInitializers.put(name, backgroundInitializer);
        }
    }

    @Override
    public void close() throws ConcurrentException {
        ConcurrentException exception = null;
        for (BackgroundInitializer<?> child : this.childInitializers.values()) {
            try {
                child.close();
            }
            catch (Exception e) {
                if (exception == null) {
                    exception = new ConcurrentException();
                }
                if (e instanceof ConcurrentException) {
                    exception.addSuppressed(e.getCause());
                    continue;
                }
                exception.addSuppressed(e);
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    @Override
    protected int getTaskCount() {
        return 1 + this.childInitializers.values().stream().mapToInt(BackgroundInitializer::getTaskCount).sum();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected MultiBackgroundInitializerResults initialize() throws Exception {
        HashMap inits;
        MultiBackgroundInitializer multiBackgroundInitializer = this;
        synchronized (multiBackgroundInitializer) {
            inits = new HashMap(this.childInitializers);
        }
        ExecutorService exec = this.getActiveExecutor();
        inits.values().forEach(bi -> {
            if (bi.getExternalExecutor() == null) {
                bi.setExternalExecutor(exec);
            }
            bi.start();
        });
        HashMap results = new HashMap();
        HashMap excepts = new HashMap();
        inits.forEach((k, v) -> {
            try {
                results.put(k, v.get());
            }
            catch (ConcurrentException cex) {
                excepts.put(k, cex);
            }
        });
        return new MultiBackgroundInitializerResults(inits, results, excepts);
    }

    @Override
    public boolean isInitialized() {
        if (this.childInitializers.isEmpty()) {
            return false;
        }
        return this.childInitializers.values().stream().allMatch(BackgroundInitializer::isInitialized);
    }

    public static class MultiBackgroundInitializerResults {
        private final Map<String, BackgroundInitializer<?>> initializers;
        private final Map<String, Object> resultObjects;
        private final Map<String, ConcurrentException> exceptions;

        private MultiBackgroundInitializerResults(Map<String, BackgroundInitializer<?>> inits, Map<String, Object> results, Map<String, ConcurrentException> excepts) {
            this.initializers = inits;
            this.resultObjects = results;
            this.exceptions = excepts;
        }

        private BackgroundInitializer<?> checkName(String name) {
            BackgroundInitializer<?> init = this.initializers.get(name);
            if (init == null) {
                throw new NoSuchElementException("No child initializer with name " + name);
            }
            return init;
        }

        public ConcurrentException getException(String name) {
            this.checkName(name);
            return this.exceptions.get(name);
        }

        public BackgroundInitializer<?> getInitializer(String name) {
            return this.checkName(name);
        }

        public Object getResultObject(String name) {
            this.checkName(name);
            return this.resultObjects.get(name);
        }

        public Set<String> initializerNames() {
            return Collections.unmodifiableSet(this.initializers.keySet());
        }

        public boolean isException(String name) {
            this.checkName(name);
            return this.exceptions.containsKey(name);
        }

        public boolean isSuccessful() {
            return this.exceptions.isEmpty();
        }
    }
}

