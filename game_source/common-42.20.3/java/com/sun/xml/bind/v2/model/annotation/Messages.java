/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.annotation;

import java.text.MessageFormat;
import java.util.ResourceBundle;

enum Messages {
    DUPLICATE_ANNOTATIONS,
    CLASS_NOT_FOUND;

    private static final ResourceBundle rb;

    public String toString() {
        return this.format(new Object[0]);
    }

    public String format(Object ... args2) {
        return MessageFormat.format(rb.getString(this.name()), args2);
    }

    static {
        rb = ResourceBundle.getBundle(Messages.class.getName());
    }
}

