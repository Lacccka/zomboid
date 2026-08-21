/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class SwaRefAdapterMarker
extends XmlAdapter<String, DataHandler> {
    @Override
    public DataHandler unmarshal(String v) throws Exception {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public String marshal(DataHandler v) throws Exception {
        throw new IllegalStateException("Not implemented");
    }
}

