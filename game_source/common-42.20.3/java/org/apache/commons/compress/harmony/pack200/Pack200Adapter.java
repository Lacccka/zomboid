/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.pack200;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class Pack200Adapter {
    protected static final int DEFAULT_BUFFER_SIZE = 8192;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final SortedMap<String, String> properties = new TreeMap<String, String>();

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }

    protected void completed(double value) throws IOException {
        this.firePropertyChange("pack.progress", null, String.valueOf((int)(100.0 * value)));
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) throws IOException {
        this.support.firePropertyChange(propertyName, oldValue, newValue);
    }

    public SortedMap<String, String> properties() {
        return this.properties;
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener(listener);
    }
}

