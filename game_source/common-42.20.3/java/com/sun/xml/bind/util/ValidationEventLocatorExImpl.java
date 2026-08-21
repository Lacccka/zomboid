/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.util;

import com.sun.xml.bind.ValidationEventLocatorEx;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;

public class ValidationEventLocatorExImpl
extends ValidationEventLocatorImpl
implements ValidationEventLocatorEx {
    private final String fieldName;

    public ValidationEventLocatorExImpl(Object target, String fieldName) {
        super(target);
        this.fieldName = fieldName;
    }

    @Override
    public String getFieldName() {
        return this.fieldName;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("[url=");
        buf.append(this.getURL());
        buf.append(",line=");
        buf.append(this.getLineNumber());
        buf.append(",column=");
        buf.append(this.getColumnNumber());
        buf.append(",node=");
        buf.append(this.getNode());
        buf.append(",object=");
        buf.append(this.getObject());
        buf.append(",field=");
        buf.append(this.getFieldName());
        buf.append("]");
        return buf.toString();
    }
}

