/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect;

import java.text.MessageFormat;
import java.util.ResourceBundle;

enum Messages {
    UNABLE_TO_ACCESS_NON_PUBLIC_FIELD,
    UNASSIGNABLE_TYPE,
    NO_SETTER,
    NO_GETTER;

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

